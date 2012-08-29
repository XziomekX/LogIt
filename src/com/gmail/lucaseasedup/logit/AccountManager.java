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
package com.gmail.lucaseasedup.logit;

import static com.gmail.lucaseasedup.logit.GeneralResult.*;
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import com.gmail.lucaseasedup.logit.db.Database;
import com.gmail.lucaseasedup.logit.event.account.AccountChangePasswordEvent;
import com.gmail.lucaseasedup.logit.event.account.AccountCreateEvent;
import com.gmail.lucaseasedup.logit.event.account.AccountRemoveEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import static java.util.logging.Level.*;

/**
 * @author LucasEasedUp
 */
public class AccountManager
{
    public AccountManager(LogItCore core)
    {
        this.core           = core;
        this.database       = core.getDatabase();
        this.table          = core.getConfig().getStorageTable();
        this.columnUsername = core.getConfig().getStorageColumnsUsername();
        this.columnPassword = core.getConfig().getStorageColumnsPassword();
        this.columnIp       = core.getConfig().getStorageColumnsIp();
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
     * @param username Username.
     * @param password Password.
     */
    public void createAccount(String username, String password)
    {
        if (passwords.containsKey(username.toLowerCase()))
        {
            throw new RuntimeException("Account already exists.");
        }
        
        try
        {
            // Hash the given password.
            String hash = core.hash(password);
            
            // Create account.
            database.insert(table, "\"" + username.toLowerCase() + "\", \"" + hash + "\", \"\"");
            passwords.put(username.toLowerCase(), hash);
            
            // Notify about the account creation.
            sendMessage(username, getMessage("CREATE_ACCOUNT_SUCCESS_SELF"));
            core.log(FINE, getMessage("CREATE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            core.callEvent(new AccountCreateEvent(username, SUCCESS));
        }
        catch (SQLException ex)
        {
            // Notify about the failure.
            sendMessage(username, getMessage("CREATE_ACCOUNT_FAIL_SELF"));
            core.log(WARNING, getMessage("CREATE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            core.callEvent(new AccountCreateEvent(username, FAILURE));
        }
    }
    
    /**
     * Removes an account with the specified username.
     * 
     * @param username Username.
     */
    public void removeAccount(String username)
    {
        if (!passwords.containsKey(username.toLowerCase()))
        {
            throw new RuntimeException("Account does not exist.");
        }
        
        try
        {
            // Remove account.
            database.delete(table, columnUsername + " = \"" + username.toLowerCase() + "\"");
            passwords.remove(username.toLowerCase());
            
            // Notify about the account removal.
            sendMessage(username, getMessage("REMOVE_ACCOUNT_SUCCESS_SELF"));
            core.log(FINE, getMessage("REMOVE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            core.callEvent(new AccountRemoveEvent(username, SUCCESS));
        }
        catch (SQLException ex)
        {
            // Notify about the failure.
            sendMessage(username, getMessage("REMOVE_ACCOUNT_FAIL_SELF"));
            core.log(WARNING, getMessage("REMOVE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            core.callEvent(new AccountRemoveEvent(username, FAILURE));
        }
    }
    
    public boolean checkAccountPassword(String username, String password)
    {
        return passwords.get(username.toLowerCase()).equals(core.hash(password));
    }
    
    /**
     * Changes password of an account with the specified username.
     * 
     * @param username Username.
     * @param password New password.
     */
    public void changeAccountPassword(String username, String password)
    {
        if (!passwords.containsKey(username.toLowerCase()))
        {
            throw new RuntimeException("Account does not exist.");
        }
        
        String hash = core.hash(password);
        
        try
        {
            // Change password.
            database.update(table, columnPassword + " = \"" + hash + "\"", columnUsername + " = \"" + username.toLowerCase() + "\"");
            passwords.put(username.toLowerCase(), hash);
        
            // Notify about the password change.
            sendMessage(username, getMessage("CHANGE_PASSWORD_SUCCESS_SELF"));
            core.log(FINE, getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
        
            // Call the appropriate event.
            core.callEvent(new AccountChangePasswordEvent(username, SUCCESS));
        }
        catch (SQLException ex)
        {
            // Notify about the failure.
            sendMessage(username, getMessage("CHANGE_PASSWORD_FAIL_SELF"));
            core.log(FINE, getMessage("CHANGE_PASSWORD_FAIL_LOG").replace("%player%", username));
            
            // Call the appropriate event.
            core.callEvent(new AccountChangePasswordEvent(username, FAILURE));
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
        if (!passwords.containsKey(username.toLowerCase()))
        {
            throw new RuntimeException("Account does not exist.");
        }
        
        try
        {
            // Attach the given ip.
            database.update(table, columnIp + " = \"" + ip + "\"", columnUsername + " = \"" + username.toLowerCase() + "\"");
            ips.put(username.toLowerCase(), ip);
        }
        catch (SQLException ex)
        {
        }
    }
    
    public int countAccountsPerIp(String ip)
    {
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
            
            // Clear the local hash map.
            passwords.clear();
            
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
        passwords.clear();
        
        try (ResultSet rs = database.select(table, "*"))
        {
            assert rs.getMetaData().getColumnCount() == 1;
            
            while (rs.next())
            {
                passwords.put(rs.getString(columnUsername), rs.getString(columnPassword));
                ips.put(rs.getString(columnUsername), rs.getString(columnIp));
            }
        }
        catch (SQLException ex)
        {
            core.log(WARNING, getMessage("LOAD_ACCOUNTS_FAIL"));
        }
    }
    
    private final LogItCore core;
    private final Database database;
    private final String table;
    private final String columnUsername;
    private final String columnPassword;
    private final String columnIp;
    
    private final HashMap<String, String> passwords = new HashMap<>();
    private final HashMap<String, String> ips = new HashMap<>();
}
