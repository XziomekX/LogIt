/*
 * Account.java
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

import io.github.lucaseasedup.logit.IntegrationType;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.security.HashingAlgorithm;
import io.github.lucaseasedup.logit.security.SecurityHelper;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.util.IniUtils;
import it.sauronsoftware.base64.Base64;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;

public final class Account extends LogItCoreObject
{
    public Account(Storage.Entry entry)
    {
        this(entry, true);
    }
    
    public Account(String username, Storage.Entry entry)
    {
        if (username == null || username.isEmpty() || entry == null)
            throw new IllegalArgumentException();
        
        entry.put(keys().username(), username.toLowerCase());
        
        this.entry = entry;
        
        fillWithDefaults();
    }
    
    /* package */ Account(Storage.Entry entry, boolean fillWithDefaults)
    {
        if (entry == null)
            throw new IllegalArgumentException();
        
        if (!entry.containsKey(keys().username()))
            throw new IllegalArgumentException("Missing key: username");
        
        this.entry = entry;
        
        if (fillWithDefaults)
        {
            fillWithDefaults();
        }
    }
    
    public String getUsername()
    {
        if (!entry.containsKey(keys().username()))
            throw new IllegalArgumentException("Missing key: username");
        
        return entry.get(keys().username()).toLowerCase();
    }
    
    public String getUuid()
    {
        if (!entry.containsKey(keys().uuid()))
            throw new IllegalArgumentException("Missing key: uuid");
        
        return entry.get(keys().uuid());
    }
    
    public void setUuid(UUID uuid)
    {
        if (uuid == null)
            throw new IllegalArgumentException();
        
        entry.put(keys().uuid(), uuid.toString());
    }
    
    /**
     * Checks if a password is equal, after hashing,
     * to this password of this account.
     * 
     * <p> The password will be hashed using the algorithm specified
     * in the appropriate key of the account entry.
     * If no hashing algorithm was specified in the account entry,
     * the global hashing algorithm stored in the config file will be used instead.
     * 
     * <p> If passwords have been disabled as of the config file,
     * this method will always return {@code true}.
     * 
     * @param password the password to be checked.
     * 
     * @return {@code true} if the password is correct;
     *         {@code false} otherwise or if an I/O error occurred.
     * 
     * @throws IllegalArgumentException if {@code password} is {@code null}.
     */
    public boolean checkPassword(String password)
    {
        if (password == null)
            throw new IllegalArgumentException();
        
        if (getConfig("config.yml").getBoolean("passwords.disable"))
            return true;
        
        if (!entry.containsKey(keys().salt()))
            throw new IllegalArgumentException("Missing key: salt");
        
        if (!entry.containsKey(keys().password()))
            throw new IllegalArgumentException("Missing key: password");
        
        if (!entry.containsKey(keys().hashing_algorithm()))
            throw new IllegalArgumentException("Missing key: hashing_algorithm");
        
        String actualHashedPassword = entry.get(keys().password());
        String hashingAlgorithm = getSecurityHelper().getDefaultHashingAlgorithm().name();
        
        if (!getConfig("secret.yml").getBoolean("debug.forceHashingAlgorithm"))
        {
            String userHashingAlgorithm = entry.get(keys().hashing_algorithm());
            
            if (userHashingAlgorithm != null)
            {
                hashingAlgorithm = userHashingAlgorithm;
            }
        }
        
        if (getConfig("config.yml").getBoolean("passwords.useSalt"))
        {
            String actualSalt = entry.get(keys().salt());
            
            return getSecurityHelper().checkPassword(password, actualHashedPassword,
                    actualSalt, hashingAlgorithm);
        }
        else
        {
            return getSecurityHelper().checkPassword(password, actualHashedPassword,
                    hashingAlgorithm);
        }
    }
    
    /**
     * Changes password of this {@code Account}.
     * 
     * <p> The password will be hashed
     * using the default algorithm specified in the config file.
     * 
     * <p> If passwords have been disabled as of the config file,
     * no action will be taken.
     * 
     * @param newPassword the new password.
     * 
     * @throws IllegalArgumentException if {@code newPassword} is {@code null}.
     */
    public void changePassword(String newPassword)
    {
        if (newPassword == null)
            throw new IllegalArgumentException();
        
        if (getConfig("config.yml").getBoolean("passwords.disable"))
            return;
        
        HashingAlgorithm hashingAlgorithm = getSecurityHelper().getDefaultHashingAlgorithm();
        String newHash;
        
        if (getConfig("config.yml").getBoolean("passwords.useSalt"))
        {
            String newSalt = SecurityHelper.generateSalt(hashingAlgorithm);
            
            newHash = SecurityHelper.hash(newPassword, newSalt, hashingAlgorithm);
            
            entry.put(keys().salt(), newSalt);
        }
        else
        {
            newHash = SecurityHelper.hash(newPassword, hashingAlgorithm);
        }
        
        entry.put(keys().password(), newHash);
        entry.put(keys().hashing_algorithm(), hashingAlgorithm.encode());
    }
    
    public String getIp()
    {
        if (!entry.containsKey(keys().ip()))
            throw new IllegalArgumentException("Missing key: ip");
        
        return entry.get(keys().ip());
    }
    
    /**
     * Attaches an IP address to this {@code AccountData}.
     * 
     * @param ip the IP address to be attached.
     * 
     * @throws IllegalArgumentException if {@code ip} is {@code null}.
     */
    public void setIp(String ip)
    {
        if (ip == null)
            throw new IllegalArgumentException();
        
        try
        {
            if (getCore().getIntegration() == IntegrationType.PHPBB2)
            {
                byte[] rawAddress = InetAddress.getByName(ip).getAddress();
                
                ip = DatatypeConverter.printHexBinary(rawAddress).toLowerCase();
            }
            
            entry.put(keys().ip(), ip);
        }
        catch (IOException ex)
        {
        }
    }
    
    public String getLoginSession()
    {
        if (!entry.containsKey(keys().login_session()))
            throw new IllegalArgumentException("Missing key: login_session");
        
        return entry.get(keys().login_session());
    }
    
    /**
     * Saves login session for this {@code Account}.
     * 
     * @param ip   the player IP address.
     * @param time the UNIX time of when the login session was saved.
     * 
     * @throws IllegalArgumentException if {@code ip} is {@code null},
     *                                  or {@code time} is negative.
     *                                  
     * @throws ReportedException        if an I/O error occurred,
     *                                  and it was reported to the logger.
     */
    public void saveLoginSession(String ip, long time)
    {
        if (ip == null || time < 0)
            throw new IllegalArgumentException();
        
        entry.put(keys().login_session(), ip + ";" + time);
    }
    
    /**
     * Erases login session of this {@code Account}.
     */
    public void eraseLoginSession()
    {
        entry.put(keys().login_session(), "");
    }
    
    public String getEmail()
    {
        if (!entry.containsKey(keys().email()))
            throw new IllegalArgumentException("Missing key: email");
        
        return entry.get(keys().email()).toLowerCase();
    }
    
    public void setEmail(String email)
    {
        if (email == null)
            throw new IllegalArgumentException();
        
        entry.put(keys().email(), email.toLowerCase());
    }
    
    public long getLastActiveDate()
    {
        if (!entry.containsKey(keys().last_active_date()))
            throw new IllegalArgumentException("Missing key: last_active_date");
        
        return Long.parseLong(entry.get(keys().last_active_date()));
    }
    
    /**
     * Updates last-active date of this {@code Account}, overwriting it
     * with the current time retrieved when calling this method.
     * 
     * @param unixTime the last-active date in UNIX time.
     */
    public void setLastActiveDate(long unixTime)
    {
        entry.put(keys().last_active_date(), String.valueOf(unixTime));
    }
    
    public long getRegistrationDate()
    {
        if (!entry.containsKey(keys().reg_date()))
            throw new IllegalArgumentException("Missing key: reg_date");
        
        return Long.parseLong(entry.get(keys().reg_date()));
    }
    
    public void setRegistrationDate(long unixTime)
    {
        entry.put(keys().reg_date(), String.valueOf(unixTime));
    }
    
    public boolean isLocked()
    {
        if (!entry.containsKey(keys().is_locked()))
            throw new IllegalArgumentException("Missing key: is_locked");
        
        return entry.get(keys().is_locked()).equals("1");
    }
    
    public void setLocked(boolean locked)
    {
        entry.put(keys().is_locked(), locked ? "1" : "0");
    }
    
    public List<String> getLoginHistory()
    {
        if (!entry.containsKey(keys().login_history()))
            throw new IllegalArgumentException("Missing key: login_history");
        
        return new ArrayList<>(Arrays.asList(entry.get(keys().login_history()).split("\\|")));
    }
    
    public void recordLogin(long unixTime, String ip, boolean succeeded)
    {
        if (unixTime < 0 || ip == null)
            throw new IllegalArgumentException();
        
        if (!entry.containsKey(keys().login_history()))
            throw new IllegalArgumentException("Missing key: login_history");
        
        String historyString = entry.get(keys().login_history());
        List<String> records = new ArrayList<>(Arrays.asList(historyString.split("\\|")));
        int recordsToKeep = getConfig("config.yml").getInt("loginHistory.recordsToKeep");
        
        for (int i = 0, n = records.size() - recordsToKeep + 1;  i < n; i++)
        {
            records.remove(0);
        }
        
        records.add(String.valueOf(unixTime) + ";" + ip + ";" + succeeded);
        
        StringBuilder historyBuilder = new StringBuilder();
        
        for (String record : records)
        {
            if (!record.isEmpty())
            {
                historyBuilder.append(record);
                historyBuilder.append("|");
            }
        }
        
        entry.put(keys().login_history(), historyBuilder.toString());
    }
    
    public String getDisplayName()
    {
        if (!entry.containsKey(keys().display_name()))
            throw new IllegalArgumentException("Missing key: display_name");
        
        return entry.get(keys().display_name());
    }
    
    public void setDisplayName(String displayName)
    {
        if (displayName == null)
            throw new IllegalArgumentException();
        
        entry.put(keys().display_name(), displayName);
    }
    
    /**
     * Returns persistence data from this {@code Account}
     * as a {@code Map}.
     * 
     * @return the persistence data; {@code null}
     *         if an I/O error occurred.
     * 
     * @throws ReportedException if an I/O error occurred,
     *                           and it was reported to the logger.
     */
    public Map<String, String> getPersistence()
    {
        if (!entry.containsKey(keys().persistence()))
            throw new IllegalArgumentException("Missing key: persistence");
        
        String persistenceString = entry.get(keys().persistence());
        Map<String, String> persistence = new LinkedHashMap<>();
        
        if (persistenceString != null)
        {
            if (getConfig("secret.yml").getBoolean("debug.encodePersistence"))
            {
                persistenceString = Base64.decode(persistenceString);
            }
            
            try
            {
                persistence = IniUtils.unserialize(persistenceString).get("persistence");
            }
            catch (IOException ex)
            {
                log(Level.WARNING, "Could not unserialize persistence"
                                 + " {username: " + getUsername() + "}", ex);
                
                ReportedException.throwNew(ex);
                
                return null;
            }
            
            if (persistence == null)
            {
                return new LinkedHashMap<>();
            }
        }
        
        return persistence;
    }
    
    /**
     * Saves persistence data to this {@code Account}.
     * 
     * @param persistence the persistence data.
     * 
     * @throws IllegalArgumentException if {@code persistence} is {@code null}.
     * 
     * @throws ReportedException        if an I/O error occurred,
     *                                  and it was reported to the logger.
     */
    public void savePersistence(Map<String, String> persistence)
    {
        if (persistence == null)
            throw new IllegalArgumentException();
        
        if (!getConfig("secret.yml").getBoolean("debug.writePersistence"))
            return;
        
        Map<String, Map<String, String>> persistenceIni = new HashMap<>(1);
        
        persistenceIni.put("persistence", persistence);
        
        try
        {
            String persistenceString = IniUtils.serialize(persistenceIni);
            
            if (getConfig("secret.yml").getBoolean("debug.encodePersistence"))
            {
                persistenceString = Base64.encode(persistenceString);
            }
            
            entry.put(keys().persistence(), persistenceString);
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
            
            ReportedException.throwNew();
        }
    }
    
    public Account clone(String username)
    {
        if (username == null || username.isEmpty())
            throw new IllegalArgumentException();
        
        Storage.Entry entryClone = entry.copy();
        
        entryClone.put(keys().username(), username.toLowerCase());
        entryClone.clearKeyDirty(keys().username());
        
        Account accountClone = new Account(entryClone, false);
        
        return accountClone;
    }
    
    public void enqueueSaveCallback(SaveCallback callback)
    {
        saveCallbacks.add(callback);
    }
    
    /* package */ void runSaveCallbacks(boolean success)
    {
        while (!saveCallbacks.isEmpty())
        {
            saveCallbacks.remove().onSave(success);
        }
    }
    
    private void fillWithDefaults()
    {
        entry.put(keys().uuid(), "");
        entry.put(keys().salt(), "");
        entry.put(keys().password(), "");
        entry.put(keys().hashing_algorithm(), "");
        entry.put(keys().ip(), "");
        entry.put(keys().login_session(), "");
        entry.put(keys().email(), "");
        entry.put(keys().last_active_date(), "-1");
        entry.put(keys().reg_date(), "-1");
        entry.put(keys().is_locked(), "0");
        entry.put(keys().login_history(), "");
        entry.put(keys().display_name(), "");
        entry.put(keys().persistence(), "");
    }
    
    /**
     * Do not use unless you know what you're doing!
     * 
     * @return the account entry.
     */
    public Storage.Entry getEntry()
    {
        return entry;
    }
    
    public static interface SaveCallback
    {
        public void onSave(boolean success);
    }
    
    private final Storage.Entry entry;
    private final Queue<SaveCallback> saveCallbacks = new LinkedList<>();
}
