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

import static io.github.lucaseasedup.logit.GeneralResult.FAILURE;
import static io.github.lucaseasedup.logit.GeneralResult.SUCCESS;
import io.github.lucaseasedup.logit.LogItCore;
import static io.github.lucaseasedup.logit.LogItCore.IntegrationType.NONE;
import static io.github.lucaseasedup.logit.LogItCore.IntegrationType.PHPBB;
import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.db.AbstractRelationalDatabase;
import io.github.lucaseasedup.logit.hash.HashGenerator;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Account manager.
 * 
 * @author LucasEasedUp
 */
public class AccountManager
{
    public AccountManager(LogItCore core, AbstractRelationalDatabase database)
    {
        this.core     = core;
        this.database = database;
        this.table    = core.getConfig().getString("storage.accounts.table");
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
        if (core.getIntegration() == NONE)
        {
            return cPassword.containsKey(username.toLowerCase());
        }
        else if (core.getIntegration() == PHPBB)
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
    public void createAccount(String username, String password) throws SQLException, UnsupportedOperationException
    {
        if (isRegistered(username))
            throw new RuntimeException("Account already exists.");
        
        String salt = HashGenerator.generateSalt(core.getHashingAlgorithm());
        String hash = core.hash(password, salt);
        
        try
        {
            if (core.getIntegration() == NONE)
            {
                database.insert(table, new String[]{
                    core.getConfig().getString("storage.accounts.columns.username"),
                    core.getConfig().getString("storage.accounts.columns.salt"),
                    core.getConfig().getString("storage.accounts.columns.password"),
                    core.getConfig().getString("storage.accounts.columns.last_active")
                }, new String[]{
                    username.toLowerCase(),
                    salt,
                    hash,
                    String.valueOf(System.currentTimeMillis() / 1000L)
                });
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cSalt.put(username.toLowerCase(), salt);
            cPassword.put(username.toLowerCase(), hash);
            cIp.put(username.toLowerCase(), null);
            cLastActive.put(username.toLowerCase(), (int) (System.currentTimeMillis() / 1000L));
            
            core.log(Level.FINE, getMessage("CREATE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            Bukkit.getPluginManager().callEvent(new AccountCreateEvent(username, hash, SUCCESS));
        }
        catch (SQLException | UnsupportedOperationException ex)
        {
            core.log(Level.WARNING, getMessage("CREATE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            Bukkit.getPluginManager().callEvent(new AccountCreateEvent(username, hash, FAILURE));
            
            throw ex;
        }
    }
    
    /**
     * Removes an account with the specified username.
     * <p/>
     * Removing a player's account does not entail logging them out.
     * 
     * @param username Username.
     * @throws AccountNotFoundException Thrown if account does not exist.
     */
    public void removeAccount(String username) throws SQLException, UnsupportedOperationException
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        try
        {
            if (core.getIntegration() == NONE)
            {
                database.delete(table, new String[]{
                    core.getConfig().getString("storage.accounts.columns.username"), "=", username.toLowerCase()
                });
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cSalt.remove(username.toLowerCase());
            cPassword.remove(username.toLowerCase());
            cIp.remove(username.toLowerCase());
            cLastActive.remove(username.toLowerCase());
            
            core.log(Level.FINE, getMessage("REMOVE_ACCOUNT_SUCCESS_LOG").replace("%player%", username));
            Bukkit.getPluginManager().callEvent(new AccountRemoveEvent(username, SUCCESS));
        }
        catch (SQLException | UnsupportedOperationException ex)
        {
            core.log(Level.WARNING, getMessage("REMOVE_ACCOUNT_FAIL_LOG").replace("%player%", username));
            Bukkit.getPluginManager().callEvent(new AccountRemoveEvent(username, FAILURE));
            
            throw ex;
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
        
        if (core.getIntegration() == NONE)
        {
            return core.checkPassword(password, cPassword.get(username.toLowerCase()), cSalt.get(username.toLowerCase()));
        }
        else if (core.getIntegration() == PHPBB)
        {
            String result;
            
            try
            {
                URL url = new URL(core.getConfig().getString("integration-phpbb.logit-script") + "?action=check-password");
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
                Logger.getLogger(AccountManager.class.getName()).log(Level.WARNING, null, ex);
                
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
    public void changeAccountPassword(String username, String newPassword) throws SQLException, UnsupportedOperationException
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        String newSalt = HashGenerator.generateSalt(core.getHashingAlgorithm());
        String newHash = core.hash(newPassword, newSalt);
        String oldPassword = cPassword.get(username.toLowerCase());
        
        try
        {
            if (core.getIntegration() == NONE)
            {
                database.update(table, new String[]{
                    core.getConfig().getString("storage.accounts.columns.username"), "=", username.toLowerCase()
                }, new String[]{
                    core.getConfig().getString("storage.accounts.columns.salt"), newSalt,
                    core.getConfig().getString("storage.accounts.columns.password"), newHash
                });
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cSalt.put(username.toLowerCase(), newSalt);
            cPassword.put(username.toLowerCase(), newHash);
            
            core.log(Level.FINE, getMessage("CHANGE_PASSWORD_SUCCESS_LOG").replace("%player%", username));
            Bukkit.getPluginManager().callEvent(new AccountChangePasswordEvent(username, oldPassword, newHash, SUCCESS));
        }
        catch (SQLException | UnsupportedOperationException ex)
        {
            core.log(Level.WARNING, getMessage("CHANGE_PASSWORD_FAIL_LOG").replace("%player%", username));
            Bukkit.getPluginManager().callEvent(new AccountChangePasswordEvent(username, oldPassword, newHash, FAILURE));
            
            throw ex;
        }
    }
    
    public void changeEmail(String username, String newEmail) throws SQLException, UnsupportedOperationException
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        String oldEmail = cEmail.get(username.toLowerCase());
        
        try
        {
            if (core.getIntegration() == NONE)
            {
                database.update(table, new String[]{
                    core.getConfig().getString("storage.accounts.columns.username"), "=", username.toLowerCase()
                }, new String[]{
                    core.getConfig().getString("storage.accounts.columns.email"), newEmail
                });
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cEmail.put(username.toLowerCase(), newEmail);
            
            core.log(Level.FINE, getMessage("CHANGE_EMAIL_SUCCESS_LOG").replace("%player%", username));
            Bukkit.getPluginManager().callEvent(new AccountChangeEmailEvent(username, oldEmail, newEmail, SUCCESS));
        }
        catch (SQLException | UnsupportedOperationException ex)
        {
            core.log(Level.WARNING, getMessage("CHANGE_EMAIL_FAIL_LOG").replace("%player%", username));
            Bukkit.getPluginManager().callEvent(new AccountChangeEmailEvent(username, oldEmail, newEmail, FAILURE));
            
            throw ex;
        }
    }
    
    public String getEmail(String username)
    {
        return cEmail.get(username.toLowerCase());
    }
    
    /**
     * Attaches IP address to an account with the specified username.
     * 
     * @param username Username.
     * @param ip IP address.
     * @throws AccountNotFoundException Thrown if the account does not exist.
     */
    public void attachIp(String username, String ip) throws SQLException, UnsupportedOperationException
    {
        if (!isRegistered(username))
            throw new AccountNotFoundException();
        
        try
        {
            if (core.getIntegration() == NONE)
            {
                database.update(table, new String[]{
                    core.getConfig().getString("storage.accounts.columns.username"), "=", username.toLowerCase()
                }, new String[]{
                    core.getConfig().getString("storage.accounts.columns.ip"), ip
                });
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            
            cIp.put(username.toLowerCase(), ip);
            
            core.log(Level.FINE, getMessage("ATTACH_IP_SUCCESS_LOG").replace("%player%", username).replace("%ip%", ip));
            Bukkit.getPluginManager().callEvent(new AccountAttachIpEvent(username, ip, SUCCESS));
        }
        catch (SQLException | UnsupportedOperationException ex)
        {
            core.log(Level.WARNING, getMessage("ATTACH_IP_FAIL_LOG").replace("%player%", username).replace("%ip%", ip));
            Bukkit.getPluginManager().callEvent(new AccountAttachIpEvent(username, ip, FAILURE));
            
            throw ex;
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
    
    public void updateLastActiveDate(String username) throws SQLException
    {
        int now = (int) (System.currentTimeMillis() / 1000L);
        
        database.update(table, new String[]{
            core.getConfig().getString("storage.accounts.columns.username"), "=", username.toLowerCase()
        }, new String[]{
            core.getConfig().getString("storage.accounts.columns.last_active"), String.valueOf(now)
        });
        
        cLastActive.put(username.toLowerCase(), now);
    }
    
    public int getLastActiveDate(String username)
    {
        return cLastActive.get(username.toLowerCase());
    }
    
    public void saveLocation(String username, Location location) throws SQLException
    {
        database.update(table, new String[]{
            core.getConfig().getString("storage.accounts.columns.username"), "=", username.toLowerCase()
        }, new String[]{
            core.getConfig().getString("storage.accounts.columns.location_world"), location.getWorld().getName(),
            core.getConfig().getString("storage.accounts.columns.location_x"), String.valueOf(location.getX()),
            core.getConfig().getString("storage.accounts.columns.location_y"), String.valueOf(location.getY()),
            core.getConfig().getString("storage.accounts.columns.location_z"), String.valueOf(location.getZ()),
            core.getConfig().getString("storage.accounts.columns.location_yaw"), String.valueOf(location.getYaw()),
            core.getConfig().getString("storage.accounts.columns.location_pitch"), String.valueOf(location.getPitch()),
        });
    }
    
    public Location getLocation(String username) throws SQLException
    {
        ResultSet rs = database.select(table, new String[]{
            core.getConfig().getString("storage.accounts.columns.location_world"),
            core.getConfig().getString("storage.accounts.columns.location_x"),
            core.getConfig().getString("storage.accounts.columns.location_y"),
            core.getConfig().getString("storage.accounts.columns.location_z"),
            core.getConfig().getString("storage.accounts.columns.location_yaw"),
            core.getConfig().getString("storage.accounts.columns.location_pitch"),
        }, new String[]{
            core.getConfig().getString("storage.accounts.columns.username"), "=", username.toLowerCase()
        });
        
        if (rs.isBeforeFirst())
        {
            rs.next();
            
            return new Location(
                Bukkit.getWorld(rs.getString(core.getConfig().getString("storage.accounts.columns.location_world"))),
                Double.valueOf(rs.getString(core.getConfig().getString("storage.accounts.columns.location_x"))),
                Double.valueOf(rs.getString(core.getConfig().getString("storage.accounts.columns.location_y"))),
                Double.valueOf(rs.getString(core.getConfig().getString("storage.accounts.columns.location_z"))),
                Float.valueOf(rs.getString(core.getConfig().getString("storage.accounts.columns.location_yaw"))),
                Float.valueOf(rs.getString(core.getConfig().getString("storage.accounts.columns.location_pitch")))
            );
        }
        else
        {
            return null;
        }
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
            if (core.getIntegration() == NONE)
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
            cEmail.clear();
            cLastActive.clear();
            
            core.log(Level.INFO, getMessage("PURGE_SUCCESS"));
        }
        catch (SQLException | UnsupportedOperationException ex)
        {
            core.log(Level.WARNING, getMessage("PURGE_FAIL"));
            
            throw ex;
        }
    }
    
    /**
     * Loads accounts from the database.
     */
    public void loadAccounts() throws SQLException
    {
        cSalt.clear();
        cPassword.clear();
        cIp.clear();
        cEmail.clear();
        cLastActive.clear();
        
        if (core.getIntegration() == NONE)
        {
            try (ResultSet rs = database.select(table, new String[]{"*"}))
            {
                while (rs.next())
                {
                    String username = rs.getString(core.getConfig().getString("storage.accounts.columns.username"));
                    
                    if (username == null)
                        continue;
                    
                    cSalt.put(username,       rs.getString(core.getConfig().getString("storage.accounts.columns.salt")));
                    cPassword.put(username,   rs.getString(core.getConfig().getString("storage.accounts.columns.password")));
                    cIp.put(username,         rs.getString(core.getConfig().getString("storage.accounts.columns.ip")));
                    cEmail.put(username,      rs.getString(core.getConfig().getString("storage.accounts.columns.email")));
                    cLastActive.put(username,    rs.getInt(core.getConfig().getString("storage.accounts.columns.last_active")));
                }
                
                core.log(Level.FINE, getMessage("LOAD_ACCOUNTS_SUCCESS").replace("%num%", String.valueOf(cPassword.size())));
            }
            catch (SQLException ex)
            {
                core.log(Level.WARNING, getMessage("LOAD_ACCOUNTS_FAIL"));
                
                throw ex;
            }
        }
    }
    
    private final LogItCore core;
    private final AbstractRelationalDatabase database;
    private final String table;
    
    private final HashMap<String, String> cSalt = new HashMap<>();
    private final HashMap<String, String> cPassword = new HashMap<>();
    private final HashMap<String, String> cIp = new HashMap<>();
    private final HashMap<String, String> cEmail = new HashMap<>();
    private final HashMap<String, Integer> cLastActive = new HashMap<>();
}
