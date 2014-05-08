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

public final class AccountManager extends LogItCoreObject implements Runnable, Disposable
{
    /**
     * Creates a new account manager.
     * 
     * @param storage the storage.
     * @param unit name of the unit.
     * @param keys the storage keys.
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
     * Returns account data belonging to an account with the specified username.
     * 
     * <p> Take caution that this method may imply quering the storage
     * every time it's called, therefore consider storing the result
     * for future use when possible.
     * 
     * @param username the username.
     * 
     * @return a storage entry with account data,
     *         or {@code null} if there is no account with the specified username.
     * 
     * @throws IllegalArgumentException if {@code username} is null.
     * @throws IOException              if an I/O error occurred.
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
     * Returns specific keys of account data
     * belonging to an account with the specified username.
     * 
     * <p> Take caution that this method may imply quering the storage
     * every time it's called, therefore consider storing the result
     * for future use when possible.
     * 
     * @param username  the username.
     * @param queryKeys the keys to be returned.
     * 
     * @return a storage entry with account data,
     *         or {@code null} if there is no account with the specified username.
     * 
     * @throws IllegalArgumentException if {@code username} is null.
     * @throws IOException              if an I/O error occurred.
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
    
    public boolean isRegistered(String username)
    {
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
     * Creates a new account with the given username and password.
     * 
     * <p> The password will be hashed using
     * the default algorithm specified in the config file.
     * 
     * <p> Also, note that the username may not preserve its original letter case
     * when stored in the storage.
     * 
     * @param username the username.
     * @param password the password.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws AccountAlreadyExistsException if an account with the given username already exists.
     * @throws ReportedException             if account creation failed
     *                                       and was reported to the logger.
     */
    public CancelledState createAccount(String username, String password)
    {
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
     * Removes an account with the specified username.
     * 
     * <p> Removing an account does not entail logging out the underlying player.
     * To log out a player, see {@link SessionManager#endSession(String)}.
     * 
     * @param username a username representing the account to be removed.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws ReportedException if account removal failed
     *                           and was reported to the logger.
     */
    public CancelledState removeAccount(String username)
    {
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
     * to the password of an account represented by the specified username.
     * 
     * <p> The password will be hashed using the algorithm specified
     * in the appropriate key of the account entry in the current fstorage.
     * 
     * <p> If no hashing algorithm was specified in the account entry,
     * the global one stored in the config file will be used instead.
     * 
     * @param username a username representing the account whose password will be checked.
     * @param password the password to be checked.
     * 
     * @return {@code true} if they match; {@code false} otherwise.
     * 
     * @throws ReportedException if an error occured
     *                           and was reported to the logger.
     */
    public boolean checkAccountPassword(String username, String password)
    {
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
     * Changes password of an account with the specified username.
     * 
     * <p> The password will be hashed
     * using the default algorithm specified in the config file.
     * 
     * @param username    a username representing the account
     *                    to be subject to password change.
     * @param newPassword the new password.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws ReportedException if this operation failed.
     *                           and was reported to the logger.
     */
    public CancelledState changeAccountPassword(String username, String newPassword)
    {
        if (getConfig().getBoolean("password.disable-passwords"))
            return CancelledState.NOT_CANCELLED;
        
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
            storage.updateEntries(unit, entry,
                    new SelectorCondition(keys.username(), Infix.EQUALS, username.toLowerCase()));
            
            log(Level.FINE,
                    getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    getMessage("CHANGE_PASSWORD_FAIL_LOG").replace("%player%", username), ex);
            
            ReportedException.throwNew(ex);
        }
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Attaches an IP address to an account with the specified username.
     * 
     * @param username a username representing the account to be subject of IP attachment.
     * @param ip       the new IP address.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws ReportedException if this operation failed
     *                           and was reported to the logger.
     */
    public CancelledState attachIp(String username, String ip)
    {
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
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Returns number of accounts with the given IP address.
     * 
     * <p> If {@code ip} is {@code null} or an empty string, the returned value is {@code 0}.
     * 
     * @param ip the IP address.
     * 
     * @return number of accounts with the given IP.
     */
    public int countAccountsWithIp(String ip)
    {
        if (ip == null || ip.isEmpty())
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
     * Counts how many unique, non-null, IP addresses are in the current storage.
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
    
    public String getEmail(String username) throws IOException
    {
        Storage.Entry entry = queryAccount(username, Arrays.asList(
                keys.username(),
                keys.email()
            ));
        
        if (entry == null)
            return null;
        
        return entry.get(keys.email());
    }
    
    /**
     * Changes e-mail address of an account with the specified username.
     * 
     * @param username a username representing the account
     *                 to be subject to e-mail address change.
     * @param newEmail the new e-mail address.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws ReportedException if this operation failed
     *                           and was reported to the logger.
     */
    public CancelledState changeEmail(String username, String newEmail)
    {
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
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Saves login session for an account with the specified username.
     * 
     * @param username the username.
     * @param ip       the player IP address.
     * @param time     the UNIX time of when the login session was created.
     * 
     * @throws IOException if an I/O error occured.
     */
    public void saveLoginSession(String username, String ip, long time) throws IOException
    {
        updateEntry(username, new Storage.Entry.Builder()
                .put(keys.login_session(), ip + ";" + time)
                .build());
    }
    
    /**
     * Erases login session of an account with the specified username.
     * 
     * @param username the username.
     * 
     * @throws IOException if an I/O error occured.
     */
    public void eraseLoginSession(String username) throws IOException
    {
        updateEntry(username, new Storage.Entry.Builder()
                .put(keys.login_session(), "")
                .build());
    }
    
    /**
     * Updates last-active date of an account with the specified username
     * overwriting it with the current time.
     * 
     * @param username the username.
     * 
     * @throws IOException if an I/O error occured.
     */
    public void updateLastActiveDate(String username) throws IOException
    {
        updateEntry(username, new Storage.Entry.Builder()
                .put(keys.last_active_date(), String.valueOf(System.currentTimeMillis() / 1000L))
                .build());
    }
    
    /**
     * Reads persistence data from an account with the specified username.
     * 
     * @param username the username.
     * 
     * @return the persistence data.
     * 
     * @throws IOException if an I/O error occured.
     */
    public Map<String, String> getAccountPersistence(String username) throws IOException
    {
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
     * Overwrites persistence data of an account with the specified username.
     * 
     * @param username the username.
     * @param persistence the persistence data.
     * 
     * @throws IOException if an I/O error occured.
     */
    public void updateAccountPersistence(String username, Map<String, String> persistence)
            throws IOException
    {
        Map<String, Map<String, String>> persistenceIni = new HashMap<>(1);
        
        persistenceIni.put("persistence", persistence);
        
        updateEntry(username, new Storage.Entry.Builder()
                .put(keys.persistence(), Base64.encode(IniUtils.serialize(persistenceIni)))
                .build());
    }
    
    /**
     * Queries the current storage and returns the number of accounts registered.
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
     * Returns the storage this account manager operates on.
     * 
     * @return the storage.
     */
    public Storage getStorage()
    {
        return storage;
    }
    
    /**
     * Returns name of the unit this account manager operates on.
     * 
     * @return the unit name.
     */
    public String getUnit()
    {
        return unit;
    }
    
    /**
     * Returns a hashtable of the current storage keys
     * as pairs of <i>name</i> and <i>data type</i>.
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
