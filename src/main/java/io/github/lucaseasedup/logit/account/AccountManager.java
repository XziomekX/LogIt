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
import io.github.lucaseasedup.logit.LogItCore.HashingAlgorithm;
import io.github.lucaseasedup.logit.LogItCore.IntegrationType;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.hash.HashGenerator;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.SelectorNegation;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.util.HashtableBuilder;
import io.github.lucaseasedup.logit.util.IniUtils;
import it.sauronsoftware.base64.Base64;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;
import org.bukkit.Bukkit;

public final class AccountManager extends LogItCoreObject implements Runnable
{
    public AccountManager(Storage storage, String unit, AccountKeys keys)
    {
        if (storage == null || unit == null || keys == null)
            throw new NullPointerException();
        
        this.storage = storage;
        this.unit = unit;
        this.keys = keys;
    }
    
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
    
    public boolean isRegistered(String username)
    {
        try
        {
            return getKey(username, keys.username()) != null;
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
        List<Hashtable<String, String>> result =
                storage.selectEntries(unit, Arrays.asList(keys.username()));
        
        for (Hashtable<String, String> entry : result)
        {
            usernames.add(entry.get(keys.username()));
        }
        
        return usernames;
    }
    
    /**
     * Creates a new account with the given username and password.
     * 
     * <p> The password will be hashed using the default algorithm.
     * 
     * @param username the username.
     * @param password the password.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws AccountAlreadyExistsException if an account with the given username already exists.
     * @throws ReportedException             if account creation failed. 
     */
    public CancelledState createAccount(String username, String password)
    {
        if (isRegistered(username))
            throw new AccountAlreadyExistsException();
        
        Hashtable<String, String> pairs = new Hashtable<>();
        String now = String.valueOf(System.currentTimeMillis() / 1000L);
        HashingAlgorithm algorithm = getCore().getDefaultHashingAlgorithm();
        
        if (!getConfig().getBoolean("password.disable-passwords"))
        {
            String hash;
            
            if (getConfig().getBoolean("password.use-salt"))
            {
                String salt = HashGenerator.generateSalt(algorithm);
                hash = getCore().hash(password, salt, algorithm);
                pairs.put(keys.salt(), salt);
            }
            else
            {
                hash = getCore().hash(password, algorithm);
            }
            
            pairs.put(keys.password(), hash);
            pairs.put(keys.hashing_algorithm(), algorithm.encode());
        }
        
        pairs.put(keys.username(), username.toLowerCase());
        pairs.put(keys.last_active_date(), now);
        pairs.put(keys.reg_date(), now);
        
        AccountEvent evt = new AccountCreateEvent(pairs);
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        try
        {
            storage.addEntry(unit, pairs);
            
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
     * <p> Removing an account does not entail logging out.
     * 
     * @param username a username representing the account to be removed.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws AccountNotFoundException if an account with this username does not exist.
     * @throws ReportedException        if account removal failed.
     */
    public CancelledState removeAccount(String username)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
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
     * Checks if a plain-text password is equal, after hashing,
     * to the password of an account represented by the specified username.
     * 
     * <p> The plain-text password will be hashed using the algorithm stored
     * in the account entry, or the one in the configuration file if the key was null.
     * 
     * @param username a username representing the account whose password will be checked.
     * @param password the plain-text password.
     * 
     * @return {@code true} if they match; {@code false} otherwise.
     * 
     * @throws AccountNotFoundException if no such account exists.
     * @throws ReportedException        if an error occurs.
     */
    public boolean checkAccountPassword(String username, String password)
    {
        if (getConfig().getBoolean("password.disable-passwords"))
            return true;
        
        try
        {
            List<Hashtable<String, String>> result = storage.selectEntries(unit,
                    Arrays.asList(keys.salt(), keys.password(), keys.hashing_algorithm()),
                    new SelectorCondition(keys.username(), Infix.EQUALS, username.toLowerCase()));
            
            if (result.isEmpty())
                throw new AccountNotFoundException();
            
            String actualHashedPassword = result.get(0).get(keys.password());
            HashingAlgorithm algorithm = getCore().getDefaultHashingAlgorithm();
            
            if (!getConfig().getBoolean("password.use-global-hashing-algorithm"))
            {
                String userAlgorithm = result.get(0).get(keys.hashing_algorithm());
                
                if (userAlgorithm != null)
                {
                    algorithm = HashingAlgorithm.decode(userAlgorithm);
                }
            }
            
            if (getConfig().getBoolean("password.use-salt"))
            {
                String actualSalt = result.get(0).get(keys.salt());
                
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
     * <p> The password will be hashed using the default algorithm.
     * 
     * @param username    a username representing the account to be subject to password change.
     * @param newPassword the new password.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws AccountNotFoundException if an account with this username does not exist.
     * @throws ReportedException        if this operation failed.
     */
    public CancelledState changeAccountPassword(String username, String newPassword)
    {
        if (getConfig().getBoolean("password.disable-passwords"))
            return CancelledState.NOT_CANCELLED;
        
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        AccountEvent evt = new AccountChangePasswordEvent(username, newPassword);
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        Hashtable<String, String> pairs = new Hashtable<>();
        HashingAlgorithm algorithm = getCore().getDefaultHashingAlgorithm();
        String newHash;
        
        if (getConfig().getBoolean("password.use-salt"))
        {
            String newSalt = HashGenerator.generateSalt(algorithm);
            newHash = getCore().hash(newPassword, newSalt, algorithm);
            pairs.put(keys.salt(), newSalt);
        }
        else
        {
            newHash = getCore().hash(newPassword, algorithm);
        }
        
        pairs.put(keys.hashing_algorithm(), algorithm.encode());
        pairs.put(keys.password(), newHash);
        
        try
        {
            storage.updateEntries(unit, pairs,
                    new SelectorCondition(keys.username(), Infix.EQUALS, username.toLowerCase()));
            
            log(Level.FINE,
                    getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
            evt.executeSuccessTasks();
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    getMessage("CHANGE_PASSWORD_FAIL_LOG").replace("%player%", username), ex);
            evt.executeFailureTasks();
            
            ReportedException.throwNew(ex);
        }
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Attaches IP address to an account with the specified username.
     * 
     * @param username a username representing the account to be subject of IP attachment.
     * @param ip       the new IP address.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws AccountNotFoundException if an account with this username does not exist.
     * @throws ReportedException        if this operation failed.
     */
    public CancelledState attachIp(String username, String ip)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        AccountEvent evt = new AccountAttachIpEvent(username, ip);
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        try
        {
            if (getCore().getIntegration() == IntegrationType.PHPBB2)
            {
                ip = DatatypeConverter.printHexBinary(InetAddress.getByName(ip).getAddress())
                        .toLowerCase();
            }
            
            updateKeys(username, new HashtableBuilder<String, String>()
                    .add(keys.ip(), ip)
                    .build());
            
            log(Level.FINE, getMessage("ATTACH_IP_SUCCESS_LOG").replace("%player%", username)
                    .replace("%ip%", ip));
            evt.executeSuccessTasks();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, getMessage("ATTACH_IP_FAIL_LOG").replace("%player%", username)
                    .replace("%ip%", ip), ex);
            evt.executeFailureTasks();
            
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
            List<Hashtable<String, String>> result = storage.selectEntries(unit,
                    Arrays.asList(keys.ip()), new SelectorCondition(keys.ip(), Infix.EQUALS, ip));
            
            return result.size();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew(ex);
        }
        
        return 0;
    }
    
    /**
     * Counts how many unique, non-null, IP addresses are in the database.
     * 
     * @return number of unique IP addresses.
     */
    public int countUniqueIps()
    {
        List<String> ips = new ArrayList<>();
        
        try
        {
            List<Hashtable<String, String>> result = storage.selectEntries(unit, Arrays.asList(keys.ip()),
                    new SelectorNegation(new SelectorCondition(keys.ip(), Infix.EQUALS, "")));
            
            for (Map<String, String> entry : result)
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
        return getKey(username, keys.email());
    }
    
    /**
     * Changes e-mail address of an account with the specified username.
     * 
     * @param username a username representing the account to be subject to e-mail address change.
     * @param newEmail the new e-mail address.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws AccountNotFoundException if an account with this username does not exist.
     * @throws ReportedException        if this operation failed.
     */
    public CancelledState changeEmail(String username, String newEmail)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        AccountEvent evt = new AccountChangeEmailEvent(username, newEmail);
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        try
        {
            updateKeys(username, new HashtableBuilder<String, String>()
                    .add(keys.email(), newEmail)
                    .build());
            
            log(Level.FINE, getMessage("CHANGE_EMAIL_SUCCESS_LOG").replace("%player%", username));
            evt.executeSuccessTasks();
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    getMessage("CHANGE_EMAIL_FAIL_LOG").replace("%player%", username), ex);
            evt.executeFailureTasks();
            
            ReportedException.throwNew(ex);
        }
        
        return CancelledState.NOT_CANCELLED;
    }
    
    public String getLoginSession(String username) throws IOException
    {
        return getKey(username, keys.login_session());
    }
    
    public void saveLoginSession(String username, String ip, long time) throws IOException
    {
        updateKeys(username, new HashtableBuilder<String, String>()
                .add(keys.login_session(), ip + ";" + time)
                .build());
    }
    
    public void eraseLoginSession(String username) throws IOException
    {
        updateKeys(username, new HashtableBuilder<String, String>()
                .add(keys.login_session(), "")
                .build());
    }
    
    public long getLastActiveDate(String username) throws IOException
    {
        return Long.parseLong(getKey(username, keys.last_active_date()));
    }
    
    public void updateLastActiveDate(String username) throws IOException
    {
        updateKeys(username, new HashtableBuilder<String, String>()
                .add(keys.last_active_date(), String.valueOf(System.currentTimeMillis() / 1000L))
                .build());
    }
    
    public Map<String, String> getAccountPersistence(String username) throws IOException
    {
        String persistenceBase64String = getKey(username, keys.persistence());
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
    
    public String getAccountPersistence(String username, String key) throws IOException
    {
        return getAccountPersistence(username).get(key);
    }
    
    public void updateAccountPersistence(String username, String key, String value)
    {
        Map<String, Map<String, String>> persistence = new HashMap<>(1);
        
        try
        {
            persistence.put("persistence", getAccountPersistence(username));
            persistence.get("persistence").put(key, value);
            
            updateKeys(username, new HashtableBuilder<String, String>()
                    .add(keys.persistence(), Base64.encode(IniUtils.serialize(persistence)))
                    .build());
        }
        catch (IOException ex)
        {
            log(Level.WARNING, "Could not update account persistance: " + key + ".", ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    public int getAccountCount() throws IOException
    {
        return storage.selectEntries(unit, Arrays.asList(keys.username())).size();
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
    
    private String getKey(String username, String key) throws IOException
    {
        List<Hashtable<String, String>> result = storage.selectEntries(unit, Arrays.asList(key),
                new SelectorCondition(keys.username(), Infix.EQUALS, username.toLowerCase()));
        
        if (result.isEmpty())
            return null;
        
        return result.get(0).get(key);
    }
    
    private void updateKeys(String username, Hashtable<String, String> pairs) throws IOException
    {
        storage.updateEntries(unit, pairs,
                new SelectorCondition(keys.username(), Infix.EQUALS, username.toLowerCase()));
    }
    
    public static final long TASK_PERIOD = (5 * 60) * 20;
    
    private final Storage storage;
    private final String unit;
    private final AccountKeys keys;
}
