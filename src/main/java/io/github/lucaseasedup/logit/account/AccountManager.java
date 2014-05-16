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

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.CancelledState;
import io.github.lucaseasedup.logit.Disposable;
import io.github.lucaseasedup.logit.IntegrationType;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.security.HashingAlgorithm;
import io.github.lucaseasedup.logit.security.SecurityHelper;
import io.github.lucaseasedup.logit.session.SessionManager;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.SelectorNegation;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.util.IniUtils;
import it.sauronsoftware.base64.Base64;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class AccountManager extends LogItCoreObject implements Runnable, Disposable
{
    /**
     * Creates a new {@code AccountManager}.
     * 
     * @param storage the storage which this {@code AccountManager} will operate on.
     * @param unit    a name of the storage unit.
     * @param keys    the storage keys.
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
    }
    
    /**
     * Internal method. Do not call directly.
     */
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
    
    /**
     * Fetches all data belonging to an account with the given username
     * from the underlying storage unit.
     * 
     * @param username the username.
     * 
     * @return a storage entry with account data,
     *         or {@code null} if there is no account with the specified username.
     * 
     * @throws IOException              if an I/O error occurred.
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public Storage.Entry queryAccount(String username) throws IOException
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        List<Storage.Entry> entries = storage.selectEntries(unit,
                new SelectorCondition(keys.username(), Infix.EQUALS, username.toLowerCase()));
        
        if (entries.isEmpty())
            return null;
        
        return entries.get(0);
    }
    
    /**
     * Fetches certain data belonging to an account with the given username
     * from the underlying storage unit.
     * 
     * @param username  the username.
     * @param queryKeys the keys to be returned.
     * 
     * @return a storage entry with account data,
     *         or {@code null} if there is no account with the specified username.
     * 
     * @throws IOException              if an I/O error occurred.
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public Storage.Entry queryAccount(String username, List<String> queryKeys) throws IOException
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        if (!queryKeys.contains(keys.username()))
        {
            queryKeys.add(keys.username());
        }
        
        List<Storage.Entry> entries = storage.selectEntries(unit, queryKeys,
                new SelectorCondition(keys.username(), Infix.EQUALS, username.toLowerCase()));
        
        if (entries.isEmpty())
            return null;
        
        return entries.get(0);
    }
    
    /**
     * Checks if an account with the given username has been registered
     * in the underlying storage unit.
     * 
     * @param username the username.
     * 
     * @return {@code true} if the account has been registered; {@code false} otherwise.
     * 
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     * @throws ReportedException        if an I/O error occured,
     *                                  and it was reported to the logger.
     */
    public boolean isRegistered(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        try
        {
            return queryAccount(username, Arrays.asList(keys.username())) != null;
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
        
        return false;
    }
    
    /**
     * Fetches all registered usernames in the underlying storage unit.
     * 
     * @return a {@code Set} of usernames.
     * 
     * @throws IOException if an I/O error occured.
     */
    public Set<String> getRegisteredUsernames() throws IOException
    {
        Set<String> usernames = new HashSet<>();
        List<Storage.Entry> entries = storage.selectEntries(unit, Arrays.asList(keys.username()));
        
        for (Storage.Entry entry : entries)
        {
            usernames.add(entry.get(keys.username()));
        }
        
        return usernames;
    }
    
    /**
     * Creates a new account with the given username and password
     * in the underlying storage unit.
     * 
     * <p> The password will be hashed using
     * the default algorithm specified in the config file.
     * The username may not preserve its original letter case
     * after it's saved in the storage.
     * 
     * <p> This method emits the {@code AccountCreateEvent} event.
     * 
     * @param username the username.
     * @param password the password.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code AccountCreateEvent} handlers.
     * 
     * @throws IllegalArgumentException      if {@code username} or
     *                                       {@code password} is {@code null}.
     *                                       
     * @throws AccountAlreadyExistsException if an account with the
     *                                       given username already exists.
     *                                       
     * @throws ReportedException             if an I/O error occured,
     *                                       and it was reported to the logger.
     */
    public CancelledState createAccount(String username, String password)
    {
        if (username == null || password == null)
            throw new IllegalArgumentException();
        
        if (isRegistered(username))
            throw new AccountAlreadyExistsException();
        
        Storage.Entry entry = new Storage.Entry();
        String now = String.valueOf(System.currentTimeMillis() / 1000L);
        HashingAlgorithm algorithm = getCore().getDefaultHashingAlgorithm();
        
        if (!getConfig().getBoolean("password.disable-passwords"))
        {
            String hash;
            
            if (getConfig().getBoolean("password.use-salt"))
            {
                String salt = SecurityHelper.generateSalt(algorithm);
                hash = SecurityHelper.hash(password, salt, algorithm);
                entry.put(keys.salt(), salt);
            }
            else
            {
                hash = SecurityHelper.hash(password, algorithm);
            }
            
            entry.put(keys.password(), hash);
            entry.put(keys.hashing_algorithm(), algorithm.encode());
        }
        
        entry.put(keys.username(), username.toLowerCase());
        entry.put(keys.last_active_date(), now);
        entry.put(keys.reg_date(), now);
        entry.put(keys.is_locked(), "0");
        
        AccountEvent evt = new AccountCreateEvent(entry);
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        try
        {
            storage.addEntry(unit, entry);
            
            log(Level.FINE, getMessage("CREATE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            evt.executeSuccessTasks();
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    getMessage("CREATE_ACCOUNT_FAIL_LOG").replace("%player%", username), ex);
            evt.executeFailureTasks();
            
            ReportedException.throwNew(ex);
        }
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Removes an account with the given username
     * from the underlying storage unit.
     * 
     * <p> Removing an account does not entail logging out the corresponding player.
     * To log out a player, use {@link SessionManager#endSession(String)}
     * or {@link SessionManager#endSession(Player)}.
     * 
     * <p> This method emits the {@code AccountRemoveEvent} event.
     * 
     * @param username the username.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code AccountRemoveEvent} handlers.
     * 
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     * @throws ReportedException        if an I/O error occured,
     *                                  and it was reported to the logger.
     */
    public CancelledState removeAccount(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        AccountEvent evt = new AccountRemoveEvent(username);
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        try
        {
            storage.removeEntries(unit,
                    new SelectorCondition(keys.username(), Infix.EQUALS, username.toLowerCase()));
            
            log(Level.FINE, getMessage("REMOVE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            evt.executeSuccessTasks();
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    getMessage("REMOVE_ACCOUNT_FAIL_LOG").replace("%player%", username), ex);
            evt.executeFailureTasks();
            
            ReportedException.throwNew(ex);
        }
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Checks if a password is equal, after hashing,
     * to a password of an account represented by the given username
     * in the underlying storage unit.
     * 
     * <p> The password will be hashed using the algorithm specified
     * in the appropriate key of the account entry.
     * If no hashing algorithm was specified in the account entry,
     * the global hashing algorithm stored in the config file will be used instead.
     * 
     * @param username the username.
     * @param password the password to be checked.
     * 
     * @return {@code true} if the password is correct; {@code false} otherwise.
     * 
     * @throws IllegalArgumentException if {@code username} or
     *                                  {@code password} is {@code null}.
     *                                       
     * @throws ReportedException        if an I/O error occured,
     *                                  and it was reported to the logger.
     */
    public boolean checkAccountPassword(String username, String password)
    {
        if (username == null || password == null)
            throw new IllegalArgumentException();
        
        if (getConfig().getBoolean("password.disable-passwords"))
            return true;
        
        try
        {
            Storage.Entry entry = queryAccount(username, Arrays.asList(
                    keys.username(),
                    keys.salt(),
                    keys.password(),
                    keys.hashing_algorithm()
                ));
            
            if (entry == null)
                return false;
            
            String actualHashedPassword = entry.get(keys.password());
            HashingAlgorithm algorithm = getCore().getDefaultHashingAlgorithm();
            
            if (!getConfig().getBoolean("password.global-hashing-algorithm"))
            {
                String userAlgorithm = entry.get(keys.hashing_algorithm());
                
                if (userAlgorithm != null)
                {
                    algorithm = HashingAlgorithm.decode(userAlgorithm);
                }
            }
            
            if (getConfig().getBoolean("password.use-salt"))
            {
                String actualSalt = entry.get(keys.salt());
                
                return getCore().checkPassword(password, actualHashedPassword, actualSalt, algorithm);
            }
            else
            {
                return getCore().checkPassword(password, actualHashedPassword, algorithm);
            }
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
        
        return false;
    }
    
    /**
     * Changes password of an account with the given username
     * in the underlying storage unit.
     * 
     * <p> The password will be hashed
     * using the default algorithm specified in the config file.
     * 
     * @param username    the username.
     * @param newPassword the new password.
     * 
     * @throws IllegalArgumentException if {@code username} or
     *                                  {@code newPassword} is {@code null}.
     *                                       
     * @throws ReportedException        if an I/O error occured,
     *                                  and it was reported to the logger.
     */
    public void changeAccountPassword(String username, String newPassword)
    {
        if (username == null || newPassword == null)
            throw new IllegalArgumentException();
        
        if (getConfig().getBoolean("password.disable-passwords"))
            return;
        
        Storage.Entry entry = new Storage.Entry();
        HashingAlgorithm algorithm = getCore().getDefaultHashingAlgorithm();
        String newHash;
        
        if (getConfig().getBoolean("password.use-salt"))
        {
            String newSalt = SecurityHelper.generateSalt(algorithm);
            newHash = SecurityHelper.hash(newPassword, newSalt, algorithm);
            entry.put(keys.salt(), newSalt);
        }
        else
        {
            newHash = SecurityHelper.hash(newPassword, algorithm);
        }
        
        entry.put(keys.hashing_algorithm(), algorithm.encode());
        entry.put(keys.password(), newHash);
        
        try
        {
            updateEntry(username, entry);
            
            log(Level.FINE,
                    getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    getMessage("CHANGE_PASSWORD_FAIL_LOG").replace("%player%", username), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    /**
     * Attaches an IP address to an account with the given username
     * in the underlying storage unit.
     * 
     * @param username the username.
     * @param ip       the new IP address.
     * 
     * @throws IllegalArgumentException if {@code username} or
     *                                  {@code ip} is {@code null}.
     *                                       
     * @throws ReportedException        if an I/O error occured,
     *                                  and it was reported to the logger.
     */
    public void attachIp(String username, String ip)
    {
        if (username == null || ip == null)
            throw new IllegalArgumentException();
        
        try
        {
            if (getCore().getIntegration() == IntegrationType.PHPBB2)
            {
                ip = DatatypeConverter.printHexBinary(InetAddress.getByName(ip).getAddress())
                        .toLowerCase();
            }
            
            updateEntry(username, new Storage.Entry.Builder()
                    .put(keys.ip(), ip)
                    .build());
            
            log(Level.FINE, getMessage("ATTACH_IP_SUCCESS_LOG").replace("%player%", username)
                    .replace("%ip%", ip));
        }
        catch (IOException ex)
        {
            log(Level.WARNING, getMessage("ATTACH_IP_FAIL_LOG").replace("%player%", username)
                    .replace("%ip%", ip), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    /**
     * Checks how many accounts with the given IP address are registered
     * in the underlying storage unit.
     * 
     * <p> If {@code ip} is an empty string, the returned value is {@code 0}.
     * 
     * @param ip the IP address.
     * 
     * @return number of accounts with the given IP.
     */
    public int countAccountsWithIp(String ip)
    {
        if (ip == null)
            throw new IllegalArgumentException();
        
        if (ip.isEmpty())
            return 0;
        
        try
        {
            List<Storage.Entry> entries = storage.selectEntries(unit,
                    Arrays.asList(keys.ip()), new SelectorCondition(keys.ip(), Infix.EQUALS, ip));
            
            return entries.size();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
        
        return 0;
    }
    
    /**
     * Checks how many unique, not empty, IP addresses are
     * in the underlying storage unit.
     * 
     * @return number of unique IP addresses.
     */
    public int countUniqueIps()
    {
        List<String> ips = new ArrayList<>();
        
        try
        {
            List<Storage.Entry> entries = storage.selectEntries(unit, Arrays.asList(keys.ip()),
                    new SelectorNegation(new SelectorCondition(keys.ip(), Infix.EQUALS, "")));
            
            for (Storage.Entry entry : entries)
            {
                ips.add(entry.get(keys.ip()));
            }
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
        
        return new HashSet<>(ips).size();
    }
    
    /**
     * Fetches an e-mail address of an account with the given username
     * from the underlying storage unit.
     * 
     * @param username the username.
     * 
     * @return an e-mail address, or {@code null}
     *         if no account with this username was found.
     * 
     * @throws IOException              if an I/O error occured.
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public String getEmail(String username) throws IOException
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        Storage.Entry entry = queryAccount(username, Arrays.asList(
                keys.username(),
                keys.email()
            ));
        
        if (entry == null)
            return null;
        
        return entry.get(keys.email());
    }
    
    /**
     * Changes e-mail address of an account with the given username
     * in the underlying storage unit.
     * 
     * @param username the username.
     * @param newEmail the new e-mail address.
     * 
     * @throws IllegalArgumentException if {@code username} or
     *                                  {@code newMail} is {@code null}.
     * 
     * @throws ReportedException        if an I/O error occured,
     *                                  and it was reported to the logger.
     */
    public void changeEmail(String username, String newEmail)
    {
        if (username == null || newEmail == null)
            throw new IllegalArgumentException();
        
        try
        {
            updateEntry(username, new Storage.Entry.Builder()
                    .put(keys.email(), newEmail)
                    .build());
            
            log(Level.FINE, getMessage("CHANGE_EMAIL_SUCCESS_LOG").replace("%player%", username));
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    getMessage("CHANGE_EMAIL_FAIL_LOG").replace("%player%", username), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    /**
     * Saves login session for an account with the given username
     * to the underlying storage unit.
     * 
     * @param username the username.
     * @param ip       the player IP address.
     * @param time     the UNIX time of when the login session was created.
     * 
     * @throws IOException              if an I/O error occured.
     * @throws IllegalArgumentException if {@code username} or
     *                                  {@code ip} is {@code null},
     *                                  or {@code time} is negative.
     */
    public void saveLoginSession(String username, String ip, long time) throws IOException
    {
        if (username == null || ip == null || time < 0)
            throw new IllegalArgumentException();
        
        updateEntry(username, new Storage.Entry.Builder()
                .put(keys.login_session(), ip + ";" + time)
                .build());
    }
    
    /**
     * Erases login session of an account with the given username
     * in the underlying storage unit.
     * 
     * @param username the username.
     * 
     * @throws IOException              if an I/O error occured.
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public void eraseLoginSession(String username) throws IOException
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        updateEntry(username, new Storage.Entry.Builder()
                .put(keys.login_session(), "")
                .build());
    }
    
    /**
     * Updates last-active date of an account with the given username,
     * overwriting it with the current time, in the underlying storage unit.
     * 
     * @param username the username.
     * 
     * @throws IOException              if an I/O error occured.
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public void updateLastActiveDate(String username) throws IOException
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        updateEntry(username, new Storage.Entry.Builder()
                .put(keys.last_active_date(), String.valueOf(System.currentTimeMillis() / 1000L))
                .build());
    }
    
    /**
     * Fetches persistence data from an account with the given username
     * from the underlying storage unit.
     * 
     * @param username the username.
     * 
     * @return the persistence data, or {@code null}
     *         if no account with this username was found.
     * 
     * @throws IOException              if an I/O error occured.
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public Map<String, String> getAccountPersistence(String username) throws IOException
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        Storage.Entry entry = queryAccount(username, Arrays.asList(
                keys.username(),
                keys.persistence()
            ));
        
        if (entry == null)
            return null;
        
        String persistenceBase64String = entry.get(keys.persistence());
        Map<String, String> persistence = new LinkedHashMap<>();
        
        if (persistenceBase64String != null)
        {
            String persistenceString = Base64.decode(persistenceBase64String);
            persistence = IniUtils.unserialize(persistenceString).get("persistence");
            
            if (persistence == null)
            {
                return new LinkedHashMap<>();
            }
        }
        
        return persistence;
    }
    
    /**
     * Overwrites persistence data of an account with the given username
     * in the underlying storage unit.
     * 
     * @param username    the username.
     * @param persistence the persistence data.
     * 
     * @throws IOException              if an I/O error occured.
     * @throws IllegalArgumentException if {@code username} or
     *                                  {@code persistence} is {@code null}.
     */
    public void updateAccountPersistence(String username, Map<String, String> persistence)
            throws IOException
    {
        if (username == null || persistence == null)
            throw new IllegalArgumentException();
        
        Map<String, Map<String, String>> persistenceIni = new HashMap<>(1);
        
        persistenceIni.put("persistence", persistence);
        
        updateEntry(username, new Storage.Entry.Builder()
                .put(keys.persistence(), Base64.encode(IniUtils.serialize(persistenceIni)))
                .build());
    }
    
    /**
     * Checks how many accounts are registered in the underlying storage unit.
     * 
     * @return the number of accounts registered.
     * 
     * @throws IOException if an I/O error occured.
     */
    public int countAccounts() throws IOException
    {
        return storage.selectEntries(unit, Arrays.asList(keys.username())).size();
    }
    
    /**
     * Returns the {@code Storage} that this {@code AccountManager} operates on.
     * 
     * @return the storage.
     */
    public Storage getStorage()
    {
        return storage;
    }
    
    /**
     * Returns a name of the storage unit that this {@code AccountManager} operates on.
     * 
     * @return the storage unit name.
     */
    public String getUnit()
    {
        return unit;
    }
    
    /**
     * Returns a {@code Hashtable}, with the storage keys of this {@code AccountManager}
     * as pairs of <i>name</i> and <i>data type</i> (both strings),
     * as an {@code AccountKeys} subclass.
     * 
     * @return the account keys.
     */
    public AccountKeys getKeys()
    {
        return keys;
    }
    
    private void updateEntry(String username, Storage.Entry entry) throws IOException
    {
        storage.updateEntries(unit, entry,
                new SelectorCondition(keys.username(), Infix.EQUALS, username.toLowerCase()));
    }
    
    /**
     * Recommended task period of {@code AccountManager} running as a Bukkit task.
     */
    public static final long TASK_PERIOD = (5 * 60) * 20;
    
    private Storage storage;
    private String unit;
    private AccountKeys keys;
}
