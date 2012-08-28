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

import static com.gmail.lucaseasedup.logit.GeneralResult.FAILURE;
import static com.gmail.lucaseasedup.logit.GeneralResult.SUCCESS;
import static com.gmail.lucaseasedup.logit.LogItPlugin.getMessage;
import static com.gmail.lucaseasedup.logit.LogItPlugin.sendMessage;
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
        this.core = core;
        this.database = core.getDatabase();
        this.storageTable = core.getConfig().getStorageTable();
        this.storageColumnsUsername = core.getConfig().getStorageColumnsUsername();
        this.storageColumnsPassword = core.getConfig().getStorageColumnsPassword();
    }
    
    public boolean isAccountCreated(String username)
    {
        return passwords.containsKey(username.toLowerCase());
    }
    
    public void createAccount(String username, String password)
    {
        if (passwords.containsKey(username.toLowerCase()))
        {
            throw new RuntimeException("Account already exists.");
        }
        
        try
        {
            String hash = core.hash(password);
            
            database.insert(storageTable, "\"" + username.toLowerCase() + "\", \"" + hash + "\"");
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
    
    public void removeAccount(String username)
    {
        if (!passwords.containsKey(username.toLowerCase()))
        {
            throw new RuntimeException("Account does not exist.");
        }
        
        try
        {
            database.delete(storageTable, storageColumnsUsername + " = \"" + username.toLowerCase() + "\"");
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
    
    public void changeAccountPassword(String username, String password)
    {
        if (!passwords.containsKey(username.toLowerCase()))
        {
            throw new RuntimeException("Account does not exist.");
        }
        
        passwords.put(username.toLowerCase(), core.hash(password));
        
        // Notify about the password change.
        sendMessage(username, getMessage("CHANGE_PASSWORD_SUCCESS_SELF"));
        core.log(FINE, getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
        
        // Call the appropriate event.
        core.callEvent(new AccountChangePasswordEvent(username));
    }
    
    public void purge() throws SQLException
    {
        try
        {
            database.truncate(storageTable);
            passwords.clear();
            
            core.log(INFO, getMessage("PURGE_SUCCESS"));
        }
        catch (SQLException ex)
        {
            // Log failure.
            core.log(WARNING, getMessage("PURGE_FAIL"));
            
            // Pass the exception out of this scope.
            throw ex;
        }
    }
    
    public void loadAccounts()
    {
        passwords.clear();
        
        try (ResultSet rs = database.select(storageTable, "*"))
        {
            assert rs.getMetaData().getColumnCount() == 1;

            while (rs.next())
            {
                passwords.put(rs.getString(storageColumnsUsername), rs.getString(storageColumnsPassword));
            }
        }
        catch (SQLException ex)
        {
            core.log(WARNING, getMessage("LOAD_ACCOUNTS_FAIL"));
        }
    }
    
    public void saveAccounts()
    {
        try
        {
            for (String username : passwords.keySet())
            {
                database.update(storageTable, storageColumnsPassword + " = \"" + passwords.get(username) + "\"",
                        storageColumnsUsername + " = \"" + username + "\"");
            }
        }
        catch (SQLException ex)
        {
            core.log(WARNING, getMessage("SAVE_ACCOUNTS_FAIL"));
        }
    }
    
    private final LogItCore core;
    private final Database database;
    private final String storageTable;
    private final String storageColumnsUsername;
    private final String storageColumnsPassword;
    
    private final HashMap<String, String> passwords = new HashMap<>();
}
