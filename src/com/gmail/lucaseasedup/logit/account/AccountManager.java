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

import static com.gmail.lucaseasedup.logit.GeneralResult.FAILURE;
import static com.gmail.lucaseasedup.logit.GeneralResult.SUCCESS;
import static com.gmail.lucaseasedup.logit.LogItConfiguration.IntegrationType.NONE;
import static com.gmail.lucaseasedup.logit.LogItConfiguration.IntegrationType.PHPBB;
import com.gmail.lucaseasedup.logit.LogItCore;
import static com.gmail.lucaseasedup.logit.LogItPlugin.callEvent;
import static com.gmail.lucaseasedup.logit.LogItPlugin.getMessage;
import com.gmail.lucaseasedup.logit.db.Database;
import com.gmail.lucaseasedup.logit.hash.HashGenerator;
import static com.gmail.lucaseasedup.logit.util.MessageSender.sendMessage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
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
     * @return True if the account exists.
     */
    public boolean isAccountCreated(String username)
    {
        if (core.getConfig().getIntegration() == NONE)
        {
            return passwords.containsKey(username.toLowerCase());
        }
        else if (core.getConfig().getIntegration() == PHPBB)
        {
            return true;
        }
        else
        {
            throw new UnsupportedOperationException();
        }
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
        
        String salt = HashGenerator.generateSalt();
        String hash = core.hash(password, salt);
        
        try
        {
            if (core.getConfig().getIntegration() == NONE)
            {
                database.insert(table, "\"" + username.toLowerCase() + "\", \"" + salt + "\", \"" + hash + "\", \"\"");
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            salts.put(username.toLowerCase(), salt);
            passwords.put(username.toLowerCase(), hash);
            ips.put(username.toLowerCase(), null);
            
            sendMessage(username, getMessage("CREATE_ACCOUNT_SUCCESS_SELF"));
            core.log(FINE, getMessage("CREATE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            
            callEvent(new AccountCreateEvent(username, hash, SUCCESS));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            sendMessage(username, getMessage("CREATE_ACCOUNT_FAIL_SELF"));
            core.log(WARNING, getMessage("CREATE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            
            callEvent(new AccountCreateEvent(username, hash, FAILURE));
        }
    }
    
    /**
     * Removes an account with the specified username.
     * 
     * @param username Username.
     * @throws AccountNotFoundException Thrown if the account does not exist.
     */
    public void removeAccount(String username)
    {
        if (!isAccountCreated(username))
            throw new AccountNotFoundException();
        
        try
        {
            if (core.getConfig().getIntegration() == NONE)
            {
                database.delete(table, "username = \"" + username.toLowerCase() + "\"");
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            salts.remove(username.toLowerCase());
            passwords.remove(username.toLowerCase());
            ips.remove(username.toLowerCase());
            
            sendMessage(username, getMessage("REMOVE_ACCOUNT_SUCCESS_SELF"));
            core.log(FINE, getMessage("REMOVE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            
            callEvent(new AccountRemoveEvent(username, SUCCESS));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            sendMessage(username, getMessage("REMOVE_ACCOUNT_FAIL_SELF"));
            core.log(WARNING, getMessage("REMOVE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            
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
     * @return True if they match.
     */
    public boolean checkAccountPassword(String username, String password)
    {
        if (!isAccountCreated(username))
            throw new AccountNotFoundException();
        
        if (core.getConfig().getIntegration() == NONE)
        {
            return passwords.get(username.toLowerCase()).equals(core.hash(password, salts.get(username.toLowerCase())));
        }
        else if (core.getConfig().getIntegration() == PHPBB)
        {
            String result;
            
            try
            {
                URL url = new URL(core.getConfig().getIntegrationPhpbbLogItScript() + "?action=check-password");
                HttpURLConnection uc = (HttpURLConnection) url.openConnection();
                uc.setRequestMethod("POST");
                uc.setDoInput(true);
                uc.setDoOutput(true);
                uc.setUseCaches(false);
                uc.setAllowUserInteraction(false);
                uc.setInstanceFollowRedirects(false);
                
                try (DataOutputStream out = new DataOutputStream(uc.getOutputStream()))
                {
                    out.writeBytes("username=" + username);
                    out.writeBytes("&password=" + password);
                    out.flush();
                }
                
                try (BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream())))
                {
                    result = in.readLine();
                }
            }
            catch (IOException ex)
            {
                return false;
            }
            
            return (result != null) ? result.equals("ok") : false;
        }
        else
        {
            throw new UnsupportedOperationException();
        }
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
        
        String newSalt = HashGenerator.generateSalt();
        String newHash = core.hash(newPassword, newSalt);
        
        // Back up old password.
        String oldPassword = passwords.get(username.toLowerCase());
        
        try
        {
            if (core.getConfig().getIntegration() == NONE)
            {
                database.update(table, "salt = \"" + newSalt + "\", password = \"" + newHash + "\"", "username = \"" + username.toLowerCase() + "\"");
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            salts.put(username.toLowerCase(), newSalt);
            passwords.put(username.toLowerCase(), newHash);
            
            sendMessage(username, getMessage("CHANGE_PASSWORD_SUCCESS_SELF"));
            core.log(FINE, getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
            
            callEvent(new AccountChangePasswordEvent(username, oldPassword, newHash, SUCCESS));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            sendMessage(username, getMessage("CHANGE_PASSWORD_FAIL_SELF"));
            core.log(FINE, getMessage("CHANGE_PASSWORD_FAIL_LOG").replace("%player%", username));
            
            callEvent(new AccountChangePasswordEvent(username, oldPassword, newHash, FAILURE));
        }
    }
    
    /**
     * Attaches IP address to an account with the specified username.
     * 
     * @param username Username.
     * @param ip IP address.
     * @throws AccountNotFoundException Thrown if the account does not exist.
     */
    public void attachIp(String username, String ip)
    {
        if (!isAccountCreated(username))
            throw new AccountNotFoundException();
        
        try
        {
            if (core.getConfig().getIntegration() == NONE)
            {
                database.update(table, "ip = \"" + ip + "\"", "username = \"" + username.toLowerCase() + "\"");
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            ips.put(username.toLowerCase(), ip);
            
            core.log(FINE, getMessage("ATTACH_IP_SUCCESS_LOG").replace("%player%", username).replace("%ip%", ip));
            
            callEvent(new AccountAttachIpEvent(username, ip, SUCCESS));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            core.log(FINE, getMessage("ATTACH_IP_FAIL_LOG").replace("%player%", username).replace("%ip%", ip));
            
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
        
        return Collections.frequency(ips.values(), ip);
    }
    
    /**
     * Purges the database from accounts.
     * 
     * @throws SQLException Thrown if database truncation failed.
     */
    public void purge() throws SQLException
    {
        try
        {
            if (core.getConfig().getIntegration() == NONE)
            {
                database.truncate(table);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            passwords.clear();
            ips.clear();
            
            core.log(INFO, getMessage("PURGE_SUCCESS"));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            core.log(WARNING, getMessage("PURGE_FAIL"));
            
            throw ex;
        }
    }
    
    /**
     * Loads accounts from the database.
     */
    public void loadAccounts()
    {
        salts.clear();
        passwords.clear();
        ips.clear();
        
        if (core.getConfig().getIntegration() == NONE)
        {
            try (ResultSet rs = database.select(table, "*"))
            {
                assert rs.getMetaData().getColumnCount() == 1;

                while (rs.next())
                {
                    salts.put(rs.getString("username"), rs.getString("salt"));
                    passwords.put(rs.getString("username"), rs.getString("password"));
                    ips.put(rs.getString("username"), rs.getString("ip"));
                }
                
                core.log(FINE, getMessage("LOAD_ACCOUNTS_SUCCESS").replace("%num%", String.valueOf(passwords.size())));
            }
            catch (SQLException ex)
            {
                core.log(WARNING, getMessage("LOAD_ACCOUNTS_FAIL"));
            }
        }
    }
    
    private final LogItCore core;
    private final Database database;
    private final String table;
    
    private final HashMap<String, String> salts = new HashMap<>();
    private final HashMap<String, String> passwords = new HashMap<>();
    private final HashMap<String, String> ips = new HashMap<>();
}
