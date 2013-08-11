/*
 * AccountManager.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
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
import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.LogItCore.HashingAlgorithm;
import io.github.lucaseasedup.logit.LogItCore.IntegrationType;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.db.Table;
import io.github.lucaseasedup.logit.hash.HashGenerator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * @author LucasEasedUp
 */
public final class AccountManager extends LogItCoreObject
{
    public AccountManager(LogItCore core, Table accounts)
    {
        super(core);
        
        this.table = accounts;
    }
    
    public Set<String> getRegisteredUsernames()
    {
        return accountMap.keySet();
    }
    
    /**
     * Checks if the given username is registered.
     * 
     * @param username Username.
     * @return True if the username is registered.
     */
    public boolean isRegistered(String username)
    {
        return accountMap.containsKey(username);
    }
    
    /**
     * Creates a new account with the given username and password.
     * <p/>
     * The given password will be hashed using an algorithm specified in the config.
     * 
     * @param username Username.
     * @param password Password.
     * @return True if account has been created,
     *         false if creation has been cancelled by an outside event listener.
     */
    public boolean createAccount(String username, String password)
    {
        if (isRegistered(username))
            throw new RuntimeException("Account already exists.");
        
        HashingAlgorithm algorithm = core.getDefaultHashingAlgorithm();
        String salt = HashGenerator.generateSalt(algorithm);
        String hash = core.hash(password, salt, algorithm);
        String now = String.valueOf(System.currentTimeMillis() / 1000L);
        
        Map<String, String> m = new HashMap<>();
        
        m.put("logit.accounts.username", username.toLowerCase());
        m.put("logit.accounts.salt", salt);
        m.put("logit.accounts.password", hash);
        m.put("logit.accounts.hashing_algorithm", algorithm.encode());
        m.put("logit.accounts.last_active", now);
        m.put("logit.accounts.reg_date", now);
        
        AccountEvent evt = new AccountCreateEvent(m);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        try
        {
            accountMap.put(username, new Account(table, m));
            
            log(Level.FINE, getMessage("CREATE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, getMessage("CREATE_ACCOUNT_FAIL_LOG").replace("%player%", username), ex);
            
            ReportedException.throwNew(ex);
        }
        
        return true;
    }
    
    /**
     * Removes an account with the specified username.
     * <p/>
     * Removing a player's account does not entail logging them out.
     * 
     * @param username Username.
     * @throws AccountNotFoundException Thrown if account does not exist.
     * @return True if account has been removed,
     *         false if removal has been cancelled by an outside event listener.
     */
    public boolean removeAccount(String username)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        Account account = accountMap.get(username);
        AccountEvent evt = new AccountRemoveEvent(account);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        try
        {
            accountMap.remove(username);
            
            log(Level.FINE, getMessage("REMOVE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, getMessage("REMOVE_ACCOUNT_FAIL_LOG").replace("%player%", username), ex);
            
            ReportedException.throwNew(ex);
        }
        
        return true;
    }
    
    /**
     * Checks if the given password matches that of account with the specified username.
     * <p/>
     * The given password will be hashed using an algorithm specified
     * in the database or in the config.
     * 
     * @param username Username.
     * @param password Password to check.
     * @throws AccountNotFoundException Thrown if no such account exists.
     * @return True if they match.
     */
    public boolean checkAccountPassword(String username, String password)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        if (table.isColumnDisabled("logit.accounts.password"))
            return true;
        
        Account account = accountMap.get(username);
        HashingAlgorithm algorithm = core.getDefaultHashingAlgorithm();
        String userAlgorithm = account.get("logit.accounts.hashing_algorithm");
        
        if (userAlgorithm != null)
        {
            algorithm = HashingAlgorithm.decode(userAlgorithm);
        }
        
        if (!table.isColumnDisabled("logit.accounts.salt"))
        {
            return core.checkPassword(password, account.get("logit.accounts.password"),
                    account.get("logit.accounts.salt"), algorithm);
        }
        else
        {
            return core.checkPassword(password, account.get("logit.accounts.password"), algorithm);
        }
    }
    
    /**
     * Changes password of an account with the given username.
     * <p/>
     * The given password will be hashed using an algorithm specified in the config.
     * 
     * @param username Username.
     * @param newPassword New password.
     * @return True if password has been changed,
     *         false if operation has been cancelled by an outside event listener.
     */
    public boolean changeAccountPassword(String username, String newPassword)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        Account account = accountMap.get(username);
        AccountEvent evt = new AccountChangePasswordEvent(account, newPassword);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        HashingAlgorithm algorithm = core.getDefaultHashingAlgorithm();
        String newSalt = HashGenerator.generateSalt(algorithm);
        String newHash = core.hash(newPassword, newSalt, algorithm);
        
        try
        {
            account.update("logit.accounts.salt", newSalt);
            account.update("logit.accounts.password", newHash);
            account.update("logit.accounts.hashing_algorithm", algorithm.encode());
            
            log(Level.FINE, getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, getMessage("CHANGE_PASSWORD_FAIL_LOG").replace("%player%", username), ex);
            
            ReportedException.throwNew(ex);
        }
        
        return true;
    }
    
    /**
     * Changes e-mail address of an account with the given username.
     * 
     * @param username Username.
     * @param newEmail New e-mail address.
     * @return True if e-mail address has been created,
     *         false if operation has been cancelled by an outside event listener.
     */
    public boolean changeEmail(String username, String newEmail)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        Account account = accountMap.get(username);
        AccountEvent evt = new AccountChangeEmailEvent(account, newEmail);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        try
        {
            account.update("logit.accounts.email", newEmail);
            
            log(Level.FINE, getMessage("CHANGE_EMAIL_SUCCESS_LOG").replace("%player%", username));
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, getMessage("CHANGE_EMAIL_FAIL_LOG").replace("%player%", username), ex);
            
            ReportedException.throwNew(ex);
        }
        
        return true;
    }
    
    public String getEmail(String username)
    {
        return accountMap.get(username).get("logit.accounts.email");
    }
    
    /**
     * Attaches IP address to an account with the specified username.
     * 
     * @param username Username.
     * @param ip IP address.
     * @return True if IP has been attached,
     *         false if operation has been cancelled by an outside event listener.
     * @throws AccountNotFoundException Thrown if the account does not exist.
     */
    public boolean attachIp(String username, String ip)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        Account account = accountMap.get(username);
        AccountEvent evt = new AccountAttachIpEvent(account, ip);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        try
        {
            if (core.getIntegration() == IntegrationType.PHPBB2)
            {
                try
                {
                    ip = DatatypeConverter.printHexBinary(InetAddress.getByName(ip).getAddress()).toLowerCase();
                }
                catch (UnknownHostException ex)
                {
                }
            }
            
            account.update("logit.accounts.ip", ip);
            
            log(Level.FINE, getMessage("ATTACH_IP_SUCCESS_LOG").replace("%player%", username)
                    .replace("%ip%", ip));
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, getMessage("ATTACH_IP_FAIL_LOG").replace("%player%", username)
                    .replace("%ip%", ip), ex);
            
            ReportedException.throwNew(ex);
        }
        
        return true;
    }
    
    /**
     * Returns number of accounts with the given IP.
     * If "ip" is null or an empty string, the returned value is 0.
     * 
     * @param ip IP address.
     * @return Number of accounts with the given IP.
     */
    public int countAccountsWithIp(String ip)
    {
        if (ip == null || ip.isEmpty())
            return 0;
        
        int count = 0;
        
        for (Account account : accountMap.values())
        {
            if (account.get("logit.accounts.ip").equalsIgnoreCase(ip))
            {
                count++;
            }
        }
        
        return count;
    }
    
    public int countUniqueIps()
    {
        List<String> ips = new ArrayList<>(accountMap.size());
        
        for (Account account : accountMap.values())
        {
            ips.add(account.get("logit.accounts.ip"));
        }
        
        return new HashSet<String>(ips).size();
    }
    
    public void updateLastActiveDate(String username)
    {
        String now = String.valueOf((int) (System.currentTimeMillis() / 1000L));
        
        try
        {
            accountMap.get(username).update("logit.accounts.last_active", now);
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not update last-active date.", ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    public int getLastActiveDate(String username)
    {
        return Integer.parseInt(accountMap.get(username).get("logit.accounts.last_active"));
    }
    
    public String getPersistence(String username, String key)
    {
        Account account = accountMap.get(username);
        
        if (account == null)
            throw new AccountNotFoundException();
        
        return account.getPersistence(key);
    }
    
    public void updatePersistence(String username, String key, String value)
    {
        Account account = accountMap.get(username);
        
        if (account == null)
            throw new AccountNotFoundException();
        
        try
        {
            account.updatePersistence(key, value);
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not update persistance: " + key + ".", ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    public void updatePersistenceLocation(String username, Location location)
    {
        Account account = accountMap.get(username);
        
        if (account == null)
            throw new AccountNotFoundException();
        
        try
        {
            account.updatePersistence("world", location.getWorld().getName());
            account.updatePersistence("x", String.valueOf(location.getX()));
            account.updatePersistence("y", String.valueOf(location.getY()));
            account.updatePersistence("z", String.valueOf(location.getZ()));
            account.updatePersistence("yaw", String.valueOf(location.getYaw()));
            account.updatePersistence("pitch", String.valueOf(location.getPitch()));
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not update player persistence location.", ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    public Location getPersistenceLocation(String username)
    {
        Account account = accountMap.get(username);
        
        if (account == null)
            throw new AccountNotFoundException();
        
        return new Location(
            Bukkit.getWorld(account.getPersistence("world")),
            Double.valueOf(account.getPersistence("x")),
            Double.valueOf(account.getPersistence("y")),
            Double.valueOf(account.getPersistence("z")),
            Float.valueOf(account.getPersistence("yaw")),
            Float.valueOf(account.getPersistence("pitch"))
        );
    }
    
    public String getAccountProperty(String username, String property)
    {
        return accountMap.get(username).get(property);
    }
    
    public boolean isColumnDisabled(String id)
    {
        return table.isColumnDisabled(id);
    }
    
    public Collection<Account> getAccounts()
    {
        return accountMap.values();
    }
    
    public int getAccountCount()
    {
        return accountMap.size();
    }
    
    public Table getTable()
    {
        return table;
    }
    
    /**
     * Loads accounts from the database.
     */
    public void loadAccounts()
    {
        Map<String, Account> loadedAccounts = new HashMap<>();
        
        try
        {
            List<Map<String, String>> rs = table.select();
            
            for (Map<String, String> m : rs)
            {
                String username = m.get("logit.accounts.username");
                
                if (username == null || loadedAccounts.containsKey(username))
                    continue;
                
                username = username.toLowerCase();
                
                if (core.getIntegration() == IntegrationType.PHPBB2)
                {
                    if (username.equals("anonymous"))
                    {
                        continue;
                    }
                }
                
                Map<String, String> account = new HashMap<>();
                
                for (Entry<String, String> e : m.entrySet())
                {
                    account.put(e.getKey(), e.getValue());
                }
                
                loadedAccounts.put(username, new Account(table, account));
            }
            
            accountMap = new AccountMap(table, loadedAccounts);
            
            log(Level.FINE, getMessage("LOAD_ACCOUNTS_SUCCESS")
                    .replace("%num%", String.valueOf(accountMap.size())));
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, getMessage("LOAD_ACCOUNTS_FAIL"), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    private final Table table;
    private AccountMap accountMap = null;
}
