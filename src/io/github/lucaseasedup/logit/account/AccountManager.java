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
import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.LogItCore.HashingAlgorithm;
import io.github.lucaseasedup.logit.LogItCore.IntegrationType;
import io.github.lucaseasedup.logit.db.SetClause;
import io.github.lucaseasedup.logit.db.Table;
import io.github.lucaseasedup.logit.db.WhereClause;
import io.github.lucaseasedup.logit.hash.HashGenerator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * @author LucasEasedUp
 */
public class AccountManager
{
    public AccountManager(LogItCore core, Table accounts)
    {
        this.core     = core;
        this.accounts = accounts;
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
        if (core.getIntegration() == IntegrationType.NONE || core.getIntegration() == IntegrationType.PHPBB2)
        {
            return cPassword.containsKey(username.toLowerCase());
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
        
        HashingAlgorithm algorithm = core.getDefaultHashingAlgorithm();
        String salt = HashGenerator.generateSalt(algorithm);
        String hash = core.hash(password, salt, algorithm);
        
        try
        {
            if (core.getIntegration() == IntegrationType.NONE || core.getIntegration() == IntegrationType.PHPBB2)
            {
                accounts.insert(new String[]{
                    "logit.accounts.username",
                    "logit.accounts.salt",
                    "logit.accounts.password",
                    "logit.accounts.hashing_algorithm",
                    "logit.accounts.last_active",
                    "logit.accounts.reg_date",
                }, new String[]{
                    username.toLowerCase(),
                    salt,
                    hash,
                    algorithm.encode(),
                    String.valueOf(System.currentTimeMillis() / 1000L),
                    String.valueOf(System.currentTimeMillis() / 1000L),
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
            if (core.getIntegration() == IntegrationType.NONE || core.getIntegration() == IntegrationType.PHPBB2)
            {
                accounts.delete(new WhereClause[]{
                    new WhereClause("logit.accounts.username", WhereClause.EQUAL, username.toLowerCase())
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
        
        if (accounts.isColumnDisabled("logit.accounts.password"))
            return true;
        
        if (core.getIntegration() == IntegrationType.NONE || core.getIntegration() == IntegrationType.PHPBB2)
        {
            HashingAlgorithm algorithm = core.getDefaultHashingAlgorithm();
            String userAlgorithm = null;
            
            try
            {
                userAlgorithm = getAccountValue(username, "logit.accounts.hashing_algorithm");
            }
            catch (SQLException ex)
            {
            }
            
            if (userAlgorithm != null)
            {
                algorithm = HashingAlgorithm.decode(userAlgorithm);
            }
            
            if (!accounts.isColumnDisabled("logit.accounts.salt"))
            {
                return core.checkPassword(password, cPassword.get(username.toLowerCase()),
                        cSalt.get(username.toLowerCase()), algorithm);
            }
            else
            {
                return core.checkPassword(password, cPassword.get(username.toLowerCase()), algorithm);
            }
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
        
        HashingAlgorithm algorithm = core.getDefaultHashingAlgorithm();
        String newSalt = HashGenerator.generateSalt(algorithm);
        String newHash = core.hash(newPassword, newSalt, algorithm);
        String oldPassword = cPassword.get(username.toLowerCase());
        
        try
        {
            if (core.getIntegration() == IntegrationType.NONE || core.getIntegration() == IntegrationType.PHPBB2)
            {
                accounts.update(new WhereClause[]{
                    new WhereClause("logit.accounts.username", WhereClause.EQUAL, username.toLowerCase())
                }, new SetClause[]{
                    new SetClause("logit.accounts.salt", newSalt),
                    new SetClause("logit.accounts.password", newHash),
                    new SetClause("logit.accounts.hashing_algorithm", algorithm.encode()),
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
            if (core.getIntegration() == IntegrationType.NONE || core.getIntegration() == IntegrationType.PHPBB2)
            {
                accounts.update(new WhereClause[]{
                    new WhereClause("logit.accounts.username", WhereClause.EQUAL, username.toLowerCase()),
                }, new SetClause[]{
                    new SetClause("logit.accounts.email", newEmail),
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
            if (core.getIntegration() == IntegrationType.NONE)
            {
                accounts.update(new WhereClause[]{
                    new WhereClause("logit.accounts.username", WhereClause.EQUAL, username.toLowerCase()),
                }, new SetClause[]{
                    new SetClause("logit.accounts.ip", ip),
                });
            }
            else if (core.getIntegration() == IntegrationType.PHPBB2)
            {
                String hexIp = "";
                
                try
                {
                    hexIp = DatatypeConverter.printHexBinary(InetAddress.getByName(ip).getAddress()).toLowerCase();
                }
                catch (UnknownHostException ex)
                {
                }
                
                accounts.update(new WhereClause[]{
                    new WhereClause("logit.accounts.username", WhereClause.EQUAL, username.toLowerCase()),
                }, new SetClause[]{
                    new SetClause("logit.accounts.ip", hexIp),
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
    
    public int countUniqueIps()
    {
        return new HashSet<String>(cIp.values()).size();
    }
    
    public void updateLastActiveDate(String username) throws SQLException
    {
        int now = (int) (System.currentTimeMillis() / 1000L);
        
        accounts.update(new WhereClause[]{
            new WhereClause("logit.accounts.username", WhereClause.EQUAL, username.toLowerCase()),
        }, new SetClause[]{
            new SetClause("logit.accounts.last_active", String.valueOf(now)),
        });
        
        cLastActive.put(username.toLowerCase(), now);
    }
    
    public int getLastActiveDate(String username)
    {
        return cLastActive.get(username.toLowerCase());
    }
    
    public void saveLocation(String username, Location location) throws SQLException
    {
        accounts.update(new WhereClause[]{
            new WhereClause("logit.accounts.username", WhereClause.EQUAL, username.toLowerCase()),
        }, new SetClause[]{
            new SetClause("logit.accounts.world", location.getWorld().getName()),
            new SetClause("logit.accounts.x", String.valueOf(location.getX())),
            new SetClause("logit.accounts.y", String.valueOf(location.getY())),
            new SetClause("logit.accounts.z", String.valueOf(location.getZ())),
            new SetClause("logit.accounts.yaw", String.valueOf(location.getYaw())),
            new SetClause("logit.accounts.pitch", String.valueOf(location.getPitch())),
        });
    }
    
    public Location getLocation(String username) throws SQLException
    {
        List<Map<String, String>> rs = accounts.select(new String[]{
            "logit.accounts.world",
            "logit.accounts.x",
            "logit.accounts.y",
            "logit.accounts.z",
            "logit.accounts.yaw",
            "logit.accounts.pitch",
        }, new WhereClause[]{
            new WhereClause("logit.accounts.username", WhereClause.EQUAL, username.toLowerCase()),
        });
        
        if (rs.isEmpty())
            return null;
        
        return new Location(
            Bukkit.getWorld(rs.get(0).get("logit.accounts.world")),
            Double.valueOf(rs.get(0).get("logit.accounts.x")),
            Double.valueOf(rs.get(0).get("logit.accounts.y")),
            Double.valueOf(rs.get(0).get("logit.accounts.z")),
            Float.valueOf(rs.get(0).get("logit.accounts.yaw")),
            Float.valueOf(rs.get(0).get("logit.accounts.pitch"))
        );
    }
    
    public String getAccountValue(String username, String column) throws SQLException
    {
        List<Map<String, String>> rs = accounts.select(new String[]{
            column,
        }, new WhereClause[]{
            new WhereClause("logit.accounts.username", WhereClause.EQUAL, username.toLowerCase()),
        });
        
        if (!rs.isEmpty())
        {
            return rs.get(0).get(column);
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
     * Loads accounts from the database.
     */
    public void loadAccounts() throws SQLException
    {
        cSalt.clear();
        cPassword.clear();
        cIp.clear();
        cEmail.clear();
        cLastActive.clear();
        
        if (core.getIntegration() == IntegrationType.NONE || core.getIntegration() == IntegrationType.PHPBB2)
        {
            try
            {
                List<Map<String, String>> rs = accounts.select();
                
                for (Map<String, String> m : rs)
                {
                    String username = m.get("logit.accounts.username");
                    
                    if (username == null)
                        continue;
                    
                    username = username.toLowerCase();
                    
                    if (username.equals("anonymous") && core.getIntegration() == IntegrationType.PHPBB2)
                        continue;
                    
                    cSalt.put(username,       m.get("logit.accounts.salt"));
                    cPassword.put(username,   m.get("logit.accounts.password"));
                    cIp.put(username,         m.get("logit.accounts.ip"));
                    cEmail.put(username,      m.get("logit.accounts.email"));
                    cLastActive.put(username, Integer.valueOf(m.get("logit.accounts.last_active")));
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
    private final Table accounts;
    
    private final HashMap<String, String> cSalt = new HashMap<>();
    private final HashMap<String, String> cPassword = new HashMap<>();
    private final HashMap<String, String> cIp = new HashMap<>();
    private final HashMap<String, String> cEmail = new HashMap<>();
    private final HashMap<String, Integer> cLastActive = new HashMap<>();
}
