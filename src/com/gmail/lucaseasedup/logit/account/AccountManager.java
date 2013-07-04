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
import java.util.HashSet;
import java.util.Set;
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
    
    public Set<String> getRegisteredUsernames()
    {
        return cPassword.keySet();
    }
    
    /**
     * Checks if the given username is registered.
     * <p/>
     * If integration mode is set to phpBB, it always returns true.
     * 
     * @param username Username.
     * @throws UnsupportedOperationException Thrown if current integration mode disallows this operation.
     * @return True if the username is registered.
     */
    public boolean isRegistered(String username)
    {
        if (core.getConfig().getIntegration() == NONE)
        {
            return cPassword.containsKey(username.toLowerCase());
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
     * <p/>
     * The given password will be hashed using an algorithm specified in the config.
     * 
     * @param username Username.
     * @param password Password.
     */
    public void createAccount(String username, String password)
    {
        if (isRegistered(username))
            throw new RuntimeException("Account already exists.");
        
        String salt = HashGenerator.generateSalt();
        String hash = core.hash(password, salt);
        
        try
        {
            if (core.getConfig().getIntegration() == NONE)
            {
                database.insert(table, new String[]{
                        core.getConfig().getStorageColumnsUsername(),
                        core.getConfig().getStorageColumnsSalt(),
                        core.getConfig().getStorageColumnsPassword(),
                        core.getConfig().getStorageColumnsLastActive()
                    },
                    username.toLowerCase(), salt, hash, String.valueOf(System.currentTimeMillis() / 1000L));
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cSalt.put(username.toLowerCase(), salt);
            cPassword.put(username.toLowerCase(), hash);
            cIp.put(username.toLowerCase(), null);
            cLastActive.put(username.toLowerCase(), (int) (System.currentTimeMillis() / 1000L));
            
            sendMessage(username, getMessage("CREATE_ACCOUNT_SUCCESS_SELF"));
            core.log(FINE, getMessage("CREATE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            
            callEvent(new AccountCreateEvent(username, hash, SUCCESS));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            sendMessage(username, getMessage("CREATE_ACCOUNT_FAIL_SELF"));
            core.log(WARNING, getMessage("CREATE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            core.log(WARNING, getMessage("CAUGHT_ERROR").replace("%error%", ex.getMessage()));
            
            callEvent(new AccountCreateEvent(username, hash, FAILURE));
        }
    }
    
    /**
     * Removes an account with the specified username.
     * <p/>
     * Removing a player's account does not entail logging them out.
     * 
     * @param username Username.
     * @throws AccountNotFoundException Thrown if the account does not exist.
     */
    public void removeAccount(String username)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        try
        {
            if (core.getConfig().getIntegration() == NONE)
            {
                database.delete(table, new String[]{core.getConfig().getStorageColumnsUsername(), username.toLowerCase()});
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cSalt.remove(username.toLowerCase());
            cPassword.remove(username.toLowerCase());
            cIp.remove(username.toLowerCase());
            cLastActive.remove(username.toLowerCase());
            
            sendMessage(username, getMessage("REMOVE_ACCOUNT_SUCCESS_SELF"));
            core.log(FINE, getMessage("REMOVE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            
            callEvent(new AccountRemoveEvent(username, SUCCESS));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            sendMessage(username, getMessage("REMOVE_ACCOUNT_FAIL_SELF"));
            core.log(WARNING, getMessage("REMOVE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            core.log(WARNING, getMessage("CAUGHT_ERROR").replace("%error%", ex.getMessage()));
            
            callEvent(new AccountRemoveEvent(username, FAILURE));
        }
    }
    
    /**
     * Checks if the given password matches that of account with the specified username.
     * <p/>
     * The given password will be hashed using an algorithm specified in the config.
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
        
        if (core.getConfig().getIntegration() == NONE)
        {
            return cPassword.get(username.toLowerCase()).equals(core.hash(password, cSalt.get(username.toLowerCase())));
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
     * <p/>
     * The given password will be hashed using an algorithm specified in the config.
     * 
     * @param username Username.
     * @param newPassword New password.
     */
    public void changeAccountPassword(String username, String newPassword)
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        String newSalt = HashGenerator.generateSalt();
        String newHash = core.hash(newPassword, newSalt);
        
        // Back up old password.
        String oldPassword = cPassword.get(username.toLowerCase());
        
        try
        {
            if (core.getConfig().getIntegration() == NONE)
            {
                database.update(table, new String[]{core.getConfig().getStorageColumnsUsername(), username.toLowerCase()},
                    core.getConfig().getStorageColumnsSalt(), newSalt,
                    core.getConfig().getStorageColumnsPassword(), newHash);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cSalt.put(username.toLowerCase(), newSalt);
            cPassword.put(username.toLowerCase(), newHash);
            
            sendMessage(username, getMessage("CHANGE_PASSWORD_SUCCESS_SELF"));
            core.log(FINE, getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
            
            callEvent(new AccountChangePasswordEvent(username, oldPassword, newHash, SUCCESS));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            sendMessage(username, getMessage("CHANGE_PASSWORD_FAIL_SELF"));
            core.log(WARNING, getMessage("CHANGE_PASSWORD_FAIL_LOG").replace("%player%", username));
            core.log(WARNING, getMessage("CAUGHT_ERROR").replace("%error%", ex.getMessage()));
            
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
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        try
        {
            if (core.getConfig().getIntegration() == NONE)
            {
                database.update(table, new String[]{core.getConfig().getStorageColumnsUsername(), username.toLowerCase()},
                    core.getConfig().getStorageColumnsIp(), ip);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cIp.put(username.toLowerCase(), ip);
            
            core.log(FINE, getMessage("ATTACH_IP_SUCCESS_LOG").replace("%player%", username).replace("%ip%", ip));
            
            callEvent(new AccountAttachIpEvent(username, ip, SUCCESS));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            core.log(WARNING, getMessage("ATTACH_IP_FAIL_LOG").replace("%player%", username).replace("%ip%", ip));
            core.log(WARNING, getMessage("CAUGHT_ERROR").replace("%error%", ex.getMessage()));
            
            callEvent(new AccountAttachIpEvent(username, ip, FAILURE));
        }
    }
    
    /**
     * Returns number of accounts with the given IP. If "ip" is an empty string, the returned value is 0.
     * 
     * @param ip IP address.
     * @return Number of accounts with the given IP.
     */
    public int countAccountsWithIp(String ip)
    {
        if (ip.isEmpty())
            return 0;
        
        return Collections.frequency(cIp.values(), ip);
    }
    
    /**
     * Counts unique IP addresses.
     * 
     * @return Number of unique IP addresses.
     */
    public int countUniqueIps()
    {
        return new HashSet<String>(cIp.values()).size();
    }
    
    public void updateLastActiveDate(String username)
    {
        int now = (int) (System.currentTimeMillis() / 1000L);
        
        try
        {
            database.update(table, new String[]{core.getConfig().getStorageColumnsUsername(), username.toLowerCase()},
                core.getConfig().getStorageColumnsLastActive(), String.valueOf(now));
        }
        catch (SQLException ex)
        {
            return;
        }
        
        cLastActive.put(username.toLowerCase(), now);
    }
    
    public int getLastActiveDate(String username)
    {
        return cLastActive.get(username.toLowerCase());
    }
    
    public int getAccountCount()
    {
        return cPassword.size();
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
                database.truncateTable(table);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cSalt.clear();
            cPassword.clear();
            cIp.clear();
            cLastActive.clear();
            
            core.log(INFO, getMessage("PURGE_SUCCESS"));
        }
        catch (SQLException|UnsupportedOperationException ex)
        {
            core.log(WARNING, getMessage("PURGE_FAIL"));
            core.log(WARNING, getMessage("CAUGHT_ERROR").replace("%error%", ex.getMessage()));
            
            throw ex;
        }
    }
    
    /**
     * Loads accounts from the database.
     */
    public void loadAccounts()
    {
        cSalt.clear();
        cPassword.clear();
        cIp.clear();
        cLastActive.clear();
        
        if (core.getConfig().getIntegration() == NONE)
        {
            try (ResultSet rs = database.select(table, "*"))
            {
                assert rs.getMetaData().getColumnCount() == 1;

                while (rs.next())
                {
                    String username = rs.getString(core.getConfig().getStorageColumnsUsername());
                    
                    if (username == null)
                        continue;
                    
                    cSalt.put(username, rs.getString(core.getConfig().getStorageColumnsSalt()));
                    cPassword.put(username, rs.getString(core.getConfig().getStorageColumnsPassword()));
                    cIp.put(username, rs.getString(core.getConfig().getStorageColumnsIp()));
                    cLastActive.put(username, rs.getInt(core.getConfig().getStorageColumnsLastActive()));
                }
                
                core.log(FINE, getMessage("LOAD_ACCOUNTS_SUCCESS").replace("%num%", String.valueOf(cPassword.size())));
            }
            catch (SQLException ex)
            {
                core.log(WARNING, getMessage("LOAD_ACCOUNTS_FAIL"));
                core.log(WARNING, getMessage("CAUGHT_ERROR").replace("%error%", ex.getMessage()));
            }
        }
    }
    
    private final LogItCore core;
    private final Database database;
    private final String table;
    
    private final HashMap<String, String> cSalt = new HashMap<>();
    private final HashMap<String, String> cPassword = new HashMap<>();
    private final HashMap<String, String> cIp = new HashMap<>();
    private final HashMap<String, Integer> cLastActive = new HashMap<>();
}
