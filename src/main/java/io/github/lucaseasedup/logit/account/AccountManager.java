/*
 * AccountManager.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit.account;

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import io.github.lucaseasedup.logit.CancelledState;
import io.github.lucaseasedup.logit.CustomLevel;
import io.github.lucaseasedup.logit.Disposable;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.QueuedMap;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.session.SessionManager;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.Selector;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.storage.Storage.Entry.Datum;
import io.github.lucaseasedup.logit.util.CollectionUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class AccountManager extends LogItCoreObject implements Runnable, Disposable
{
    /**
     * Constructs a new {@code AccountManager}.
     * 
     * @param storage the storage that this {@code AccountManager} will operate on.
     * @param unit    the name of a unit eligible for account storage.
     * @param keys    the account keys present in the specified unit.
     */
    public AccountManager(Storage storage, String unit, AccountKeys keys)
    {
        if (storage == null || unit == null || keys == null)
            throw new IllegalArgumentException();
        
        this.storage = storage;
        this.unit = unit;
        this.keys = keys;
    }
    
    @Override
    public void dispose()
    {
        storage = null;
        unit = null;
        keys = null;
        
        if (pingerTask != null)
        {
            pingerTask.cancel();
            pingerTask = null;
        }
        
        if (buffer != null)
        {
            buffer.clear();
            buffer = null;
        }
    }
    
    /**
     * Internal method. Do not call directly.
     */
    @Override
    public void run()
    {
        if (pingerTask == null)
        {
            pingerTask = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        storage.ping();
                    }
                    catch (IOException ex)
                    {
                        log(Level.WARNING, "Could not ping the database.", ex);
                    }
                }
            }.runTaskTimer(getPlugin(), 20L, TimeUnit.MINUTES.convert(5, TimeUnit.TICKS));
        }
        
        flushBuffer();
    }
    
    /**
     * Selects an account with the given username from the underlying storage unit.
     * 
     * @param username  the username of an account to be selected.
     * @param queryKeys the account keys to be returned by this query.
     * 
     * @return an {@code Account} object, or {@code null}
     *         if there was no account with the given username
     *         or an I/O error occurred.
     * 
     * @throws IllegalArgumentException if {@code username} or
     *                                  {@code queryKeys} is {@code null}.
     *                                  
     * @throws ReportedException        if an I/O error occurred,
     *                                  and it was reported to the logger.
     */
    public Account selectAccount(String username, List<String> queryKeys)
    {
        if (username == null || queryKeys == null)
            throw new IllegalArgumentException();
        
        if (!queryKeys.contains(keys.username()))
            throw new IllegalArgumentException("Missing query key: username");
        
        username = username.toLowerCase();
        
        Account cachedAccount = null;
        
        // If the buffer contains some information about this account.
        if (buffer.containsKey(username))
        {
            cachedAccount = buffer.get(username);
            
            // The account is known not to exist.
            if (cachedAccount == null)
            { 
                return null;
            }
            // The account exists in the buffer.
            else
            {
                // All the query keys can be found in the cached entry.
                if (CollectionUtils.isSubset(queryKeys, cachedAccount.getEntry().getKeys()))
                {
                    return cachedAccount;
                }
                // Some keys need to be fetched from the storage
                // in order to fulfill the selection request.
                else
                {
                    // Remove the keys that have already been fetched;
                    // we only need those that hasn't been.
                    queryKeys = new ArrayList<>(queryKeys);
                    queryKeys.removeAll(cachedAccount.getEntry().getKeys());
                    
                    // If the username key has been removed
                    // (actually, it is always the case),
                    // then put it back into the key list.
                    if (!queryKeys.contains(keys.username()))
                    {
                        queryKeys.add(keys.username());
                    }
                }
            }
        }
        
        List<Storage.Entry> entries = null;
        
        try
        {
            entries = storage.selectEntries(unit, queryKeys,
                    new SelectorCondition(keys.username(), Infix.EQUALS, username));
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
        
        // If an I/O error occurred, return null.
        if (entries == null)
            return null;
        
        // If no such account exists in the storage,
        // mark it in the buffer as non-existing and return null.
        if (entries.isEmpty())
        {
            buffer.put(username, null);
            
            return null;
        }
        
        // If the account is just partially cached,
        // fill the missing keys with the values fetched from the storage.
        if (cachedAccount != null)
        {
            for (Datum datum : entries.get(0))
            {
                if (!cachedAccount.getEntry().containsKey(datum.getKey()))
                {
                    cachedAccount.getEntry().put(datum.getKey(), datum.getValue());
                    cachedAccount.getEntry().clearKeyDirty(datum.getKey());
                }
            }
        }
        
        // If there was no cached account in the buffer,
        // create a new Account object for it and put it into the buffer.
        if (!buffer.containsKey(username))
        {
            cachedAccount = new Account(entries.get(0));
            
            buffer.put(username, cachedAccount);
        }
        
        return cachedAccount;
    }
    
    public boolean isRegistered(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        return selectAccount(username, Arrays.asList(keys.username())) != null;
    }
    
    public List<Account> selectAccounts(List<String> queryKeys, Selector selector)
    {
        if (selector == null || queryKeys == null)
            throw new IllegalArgumentException();
        
        if (!queryKeys.contains(keys.username()))
            throw new IllegalArgumentException("Missing query key: username");
        
        List<Storage.Entry> entries = null;
        
        try
        {
            entries = storage.selectEntries(unit, queryKeys, selector);
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
        
        if (entries == null)
            return null;
        
        List<Account> accounts = new ArrayList<>(entries.size());
        
        for (Storage.Entry entry : entries)
        {
            String username = entry.get(keys().username());
            
            if (buffer.get(username) != null)
            {
                for (Datum datum : buffer.get(username).getEntry())
                {
                    entry.put(datum.getKey(), datum.getValue());
                }
            }
            
            Account account = new Account(entry);
            
            if (buffer.get(username) == null)
            {
                buffer.put(username, account);
            }
            
            accounts.add(account);
        }
        
        return accounts;
    }
    
    public CancelledState insertAccount(Account account)
    {
        if (account == null)
            throw new IllegalArgumentException();
        
        AccountEvent event = new AccountInsertEvent(account.getEntry());
        
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled())
            return CancelledState.CANCELLED;
        
        buffer.remove(account.getUsername());
        
        try
        {
            storage.addEntry(unit, account.getEntry());
            
            log(Level.WARNING, _("createAccount.success.log")
                    .replace("{0}", account.getUsername()));
            
            event.executeSuccessTasks();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, _("createAccount.fail.log")
                    .replace("{0}", account.getUsername()), ex);
            
            event.executeFailureTasks();
            
            ReportedException.throwNew(ex);
        }
        
        return CancelledState.NOT_CANCELLED;
    }
    
    public void insertAccounts(Account... accounts)
    {
        if (accounts == null)
            throw new IllegalArgumentException();
        
        try
        {
            storage.setAutobatchEnabled(true);
            
            for (Account account : accounts)
            {
                insertAccount(account);
            }
            
            storage.executeBatch();
            storage.clearBatch();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
        finally
        {
            storage.setAutobatchEnabled(false);
        }
    }
    
    public void renameAccount(String username, String newUsername)
    {
        if (username == null || newUsername == null || newUsername.isEmpty())
            throw new IllegalArgumentException();
        
        username = username.toLowerCase();
        newUsername = newUsername.toLowerCase();
        
        try
        {
            storage.updateEntries(unit,
                    new Storage.Entry.Builder()
                            .put(keys().username(), newUsername)
                            .build(),
                    new SelectorCondition(keys.username(), Infix.EQUALS, username)
            );
            
            if (buffer.get(username) != null)
            {
                buffer.put(newUsername, buffer.remove(username));
            }
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    /**
     * Removes an account with the given username from the underlying storage unit.
     * 
     * <p> Removing an account does not entail logging out the corresponding player.
     * To log out a player, use {@link SessionManager#endSession(String)}
     * or {@link SessionManager#endSession(Player)}.
     * 
     * <p> This method emits the {@code AccountRemoveEvent} event.
     * 
     * @param username the username of an account to be removed.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code AccountRemoveEvent} handlers.
     * 
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     * 
     * @throws ReportedException        if an I/O error occurred,
     *                                  and it was reported to the logger.
     */
    public CancelledState removeAccount(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        username = username.toLowerCase();
        
        AccountEvent event = new AccountRemoveEvent(username);
        
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled())
            return CancelledState.CANCELLED;
        
        try
        {
            storage.removeEntries(unit,
                    new SelectorCondition(keys.username(), Infix.EQUALS, username)
            );
            
            buffer.put(username, null);
            
            log(Level.WARNING, _("removeAccount.success.log")
                    .replace("{0}", username));
            
            event.executeSuccessTasks();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, _("removeAccount.fail.log")
                    .replace("{0}", username), ex);
            
            event.executeFailureTasks();
            
            ReportedException.throwNew(ex);
        }
        
        return CancelledState.NOT_CANCELLED;
    }
    
    public void removeAccounts(String... usernames)
    {
        if (usernames == null)
            throw new IllegalArgumentException();
        
        try
        {
            storage.setAutobatchEnabled(true);
            
            for (String username : usernames)
            {
                removeAccount(username);
            }
            
            storage.executeBatch();
            storage.clearBatch();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
        finally
        {
            storage.setAutobatchEnabled(false);
        }
    }
    
    public void flushBuffer()
    {
        if (buffer == null || buffer.isEmpty())
            return;
        
        QueuedMap<String, Storage.Entry> dirtyEntries = new QueuedMap<>();
        
        while (!buffer.isEmpty())
        {
            Map.Entry<String, Account> e = buffer.remove();
            
            if (e.getValue() == null)
                continue;
            
            Storage.Entry dirtyEntry = e.getValue().getEntry().copyDirty();
            
            if (!dirtyEntry.getKeys().isEmpty())
            {
                dirtyEntries.put(e.getKey(), dirtyEntry);
            }
        }
        
        if (dirtyEntries.isEmpty())
            return;
        
        log(CustomLevel.INTERNAL, "AccountManager#flushBuffer() {"
                + "dirtyEntries.size() = " + dirtyEntries.size() + "}");
        
        try
        {
            storage.setAutobatchEnabled(true);
            
            for (Map.Entry<String, Storage.Entry> e : dirtyEntries.entrySet())
            {
                try
                {
                    storage.updateEntries(unit, e.getValue(), new SelectorCondition(
                            keys.username(),
                            Infix.EQUALS,
                            e.getKey().toLowerCase()
                    ));
                }
                catch (IOException ex)
                {
                    log(Level.WARNING, ex);
                }
            }
            
            storage.executeBatch();
            storage.clearBatch();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
        }
        finally
        {
            storage.setAutobatchEnabled(false);
        }
        
        log(CustomLevel.INTERNAL, "end-of #flushBuffer()");
    }
    
    public void discardBuffer()
    {
        buffer.clear();
    }
    
    public Storage getStorage()
    {
        return storage;
    }
    
    public String getUnit()
    {
        return unit;
    }
    
    public AccountKeys getKeys()
    {
        return keys;
    }
    
    private Storage storage;
    private String unit;
    private AccountKeys keys;
    private BukkitTask pingerTask;
    private QueuedMap<String, Account> buffer = new QueuedMap<>();
}
