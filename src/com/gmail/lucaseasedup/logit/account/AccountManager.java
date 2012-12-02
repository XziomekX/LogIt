/*
 * AccountManager.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit.account;

import static com.gmail.lucaseasedup.logit.GeneralResult.*;
import com.gmail.lucaseasedup.logit.LogItCore;
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import com.gmail.lucaseasedup.logit.db.Database;
import static com.gmail.lucaseasedup.logit.util.MessageSender.sendMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import static java.util.logging.Level.*;

/**
 * Account manager.
 * 
 * @author LucasEasedUp
 */
public class AccountManager
{
    public AccountManager(LogItCore core, Database database)
    {
        this.core     = core;
        this.database = database;
        this.table    = core.getConfig().getStorageTable();
    }
    
    /**
     * Checks if an account with the specified username is created.
     * 
     * @param username Username.
     * @return True, if an account exists.
     */
    public boolean isAccountCreated(String username)
    {
        return passwords.containsKey(username.toLowerCase());
    }
    
    /**
     * Creates a new account with the given username and password.
     * 
     * The given password will be hashed using an algorithm specified in the config.
     * 
     * @param username Username.
     * @param password Password.
     */
    public void createAccount(String username, String password)
    {
        if (isAccountCreated(username))
            throw new RuntimeException("Account already exists.");
        
        // Hash the given password.
        String salt = core.generateSalt();
        String hash = core.hash(password, salt);
        
        try
        {
            // Create account.
            database.insert(table, "\"" + username.toLowerCase() + "\", \"" + salt + "\", \"" + hash + "\", \"\"");
            
            salts.put(username.toLowerCase(), salt);
            passwords.put(username.toLowerCase(), hash);
            ips.put(username.toLowerCase(), null);
            
            // Notify about the account creation.
            sendMessage(username, getMessage("CREATE_ACCOUNT_SUCCESS_SELF"));
            core.log(FINE, getMessage("CREATE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            callEvent(new AccountCreateEvent(username, hash, SUCCESS));
        }
        catch (SQLException ex)
        {
            // Notify about the failure.
            sendMessage(username, getMessage("CREATE_ACCOUNT_FAIL_SELF"));
            core.log(WARNING, getMessage("CREATE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            callEvent(new AccountCreateEvent(username, hash, FAILURE));
        }
    }
    
    /**
     * Removes an account with the specified username.
     * 
     * @param username Username.
     */
    public void removeAccount(String username)
    {
        if (!isAccountCreated(username))
            throw new AccountNotFoundException();
        
        try
        {
            // Remove account.
            database.delete(table, "username = \"" + username.toLowerCase() + "\"");
            
            salts.remove(username.toLowerCase());
            passwords.remove(username.toLowerCase());
            ips.remove(username.toLowerCase());
            
            // Notify about the account removal.
            sendMessage(username, getMessage("REMOVE_ACCOUNT_SUCCESS_SELF"));
            core.log(FINE, getMessage("REMOVE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            callEvent(new AccountRemoveEvent(username, SUCCESS));
        }
        catch (SQLException ex)
        {
            // Notify about the failure.
            sendMessage(username, getMessage("REMOVE_ACCOUNT_FAIL_SELF"));
            core.log(WARNING, getMessage("REMOVE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            callEvent(new AccountRemoveEvent(username, FAILURE));
        }
    }
    
    /**
     * Checks if the given password matches that of account with the specified username.
     * 
     * The given password will be hashed using an algorithm specified in the config.
     * 
     * @param username Username.
     * @param password Password to check
     * @return True, if they match.
     */
    public boolean checkAccountPassword(String username, String password)
    {
        if (!isAccountCreated(username))
            throw new AccountNotFoundException();
        
        return passwords.get(username.toLowerCase()).equals(core.hash(password, salts.get(username.toLowerCase())));
    }
    
    /**
     * Changes password of an account with the specified username.
     * 
     * The given password will be hashed using an algorithm specified in the config.
     * 
     * @param username Username.
     * @param password New password.
     */
    public void changeAccountPassword(String username, String newPassword)
    {
        if (!isAccountCreated(username))
            throw new AccountNotFoundException();
        
        // Hash the given password.
        String newSalt = core.generateSalt();
        String newHash = core.hash(newPassword, newSalt);
        
        // Back up old password.
        String oldPassword = passwords.get(username.toLowerCase());
        
        try
        {
            // Change password.
            database.update(table, "salt = \"" + newSalt + "\", password = \"" + newHash + "\"", "username = \"" + username.toLowerCase() + "\"");
            
            salts.put(username.toLowerCase(), newSalt);
            passwords.put(username.toLowerCase(), newHash);
            
            // Notify about the password change.
            sendMessage(username, getMessage("CHANGE_PASSWORD_SUCCESS_SELF"));
            core.log(FINE, getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            callEvent(new AccountChangePasswordEvent(username, oldPassword, newHash, SUCCESS));
        }
        catch (SQLException ex)
        {
            // Notify about the failure.
            sendMessage(username, getMessage("CHANGE_PASSWORD_FAIL_SELF"));
            core.log(FINE, getMessage("CHANGE_PASSWORD_FAIL_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            callEvent(new AccountChangePasswordEvent(username, oldPassword, newHash, FAILURE));
        }
    }
    
    /**
     * Attaches IP address to an account with the specified username.
     * 
     * @param username Username.
     * @param ip IP address.
     */
    public void attachIp(String username, String ip)
    {
        if (!isAccountCreated(username))
            throw new AccountNotFoundException();
        
        try
        {
            // Attach the given ip.
            database.update(table, "ip = \"" + ip + "\"", "username = \"" + username.toLowerCase() + "\"");
            ips.put(username.toLowerCase(), ip);
            
            // Notify about the attachment.
            core.log(FINE, getMessage("ATTACH_IP_SUCCESS_LOG").replace("%player%", username).replace("%ip%", ip));
            
            // Call the appropriate event.
            callEvent(new AccountAttachIpEvent(username, ip, SUCCESS));
        }
        catch (SQLException ex)
        {
            // Log failure.
            core.log(FINE, getMessage("ATTACH_IP_FAIL_LOG").replace("%player%", username).replace("%ip%", ip));
            
            // Call the appropriate event.
            callEvent(new AccountAttachIpEvent(username, ip, FAILURE));
        }
    }
    
    /**
     * Returns number of accounts with the given IP. If "ip" is an empty string, the returned value is 0.
     * 
     * @param ip IP address.
     * @return Number of accounts with the given IP.
     */
    public int countAccountsPerIp(String ip)
    {
        if (ip.isEmpty())
            return 0;
        
        int counter = 0;
        
        for (String s : ips.values())
        {
            if (s.equals(ip))
            {
                counter++;
            }
        }
        
        return counter;
    }
    
    /**
     * Purges the database from accounts.
     * 
     * @throws SQLException Thrown, if database truncation failed.
     */
    public void purge() throws SQLException
    {
        try
        {
            // Clear the database.
            database.truncate(table);
            
            // Clear the local hash maps.
            passwords.clear();
            ips.clear();
            
            // Notify about purging.
            core.log(INFO, getMessage("PURGE_SUCCESS"));
        }
        catch (SQLException ex)
        {
            // Log failure.
            core.log(WARNING, getMessage("PURGE_FAIL"));
            
            // Pass the exception off this scope.
            throw ex;
        }
    }
    
    /**
     * Loads accounts from the database.
     */
    public void loadAccounts()
    {
        // Clear the local hash maps.
        salts.clear();
        passwords.clear();
        ips.clear();
        
        try (ResultSet rs = database.select(table, "*"))
        {
            assert rs.getMetaData().getColumnCount() == 1;
            
            while (rs.next())
            {
                salts.put(rs.getString("username"), rs.getString("salt"));
                passwords.put(rs.getString("username"), rs.getString("password"));
                ips.put(rs.getString("username"), rs.getString("ip"));
            }
            
            // Notify about the number of loaded accounts.
            core.log(FINE, getMessage("LOAD_ACCOUNTS_SUCCESS").replace("%num%", String.valueOf(passwords.size())));
        }
        catch (SQLException ex)
        {
            // Log failure.
            core.log(WARNING, getMessage("LOAD_ACCOUNTS_FAIL"));
        }
    }
    
    private final LogItCore core;
    private final Database database;
    private final String table;
    
    private final HashMap<String, String> salts = new HashMap<>();
    private final HashMap<String, String> passwords = new HashMap<>();
    private final HashMap<String, String> ips = new HashMap<>();
}
