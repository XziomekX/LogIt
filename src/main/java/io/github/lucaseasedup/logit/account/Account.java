package io.github.lucaseasedup.logit.account;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.common.ReportedException;
import io.github.lucaseasedup.logit.security.model.HashingModel;
import io.github.lucaseasedup.logit.security.model.HashingModelDecoder;
import io.github.lucaseasedup.logit.storage.StorageEntry;
import io.github.lucaseasedup.logit.util.IniUtils;
import io.github.lucaseasedup.logit.util.Validators;
import it.sauronsoftware.base64.Base64;
import java.io.IOException;
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
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * Represents a single account in an {@code AccountManager}.
 *
 * <p> Every {@code Account} has its own {@code StorageEntry} instance
 * underlain so that it could be saved to a {@code Storage} as well as
 * selected and reconstructed using {@link AccountManager#selectAccount}.
 *
 * <p>Default values for entry keys:<br><br>
 *
 * <table>
 *  <tr><td><b>Key</b></td><td><b>Default value</b></td></tr>
 *  <tr><td>username</td><td><i>n/a; required</i></td></tr>
 *  <tr><td>uuid</td><td>{@code ""}</td></tr>
 *  <tr><td>salt</td><td>{@code ""}</td></tr>
 *  <tr><td>password</td><td>{@code ""}</td></tr>
 *  <tr><td>hashing_algorithm</td><td>{@code ""}</td></tr>
 *  <tr><td>ip</td><td>{@code ""}</td></tr>
 *  <tr><td>login_session</td><td>{@code ""}</td></tr>
 *  <tr><td>email</td><td>{@code ""}</td></tr>
 *  <tr><td>last_active_date</td><td>{@code "-1"}</td></tr>
 *  <tr><td>reg_date</td><td>{@code "-1"}</td></tr>
 *  <tr><td>is_locked</td><td>{@code "0"}</td></tr>
 *  <tr><td>login_history</td><td>{@code ""}</td></tr>
 *  <tr><td>display_name</td><td>{@code ""}</td></tr>
 *  <tr><td>persistence</td><td>{@code ""}</td></tr>
 * </table>
 */
public final class Account extends LogItCoreObject
{
    /**
     * Creates a new {@code Account} object, with all entry keys filled with
     * their defaults.
     *
     * <p> A new {@code StorageEntry} instance will be created for
     * this account to hold its data in a storage-oriented manner.
     *
     * <p> After you finish filling this {@code Account} with data,
     * use {@code AccountManager#insertAccount(Account)} or
     * {@code AccountManager#insertAccounts(Account...)} to insert the new
     * account to the storage.
     *
     * @param username
     *       A username for this account, which will be saved to the underlying
     *       storage entry as soon as it is created.
     *
     * @throws IllegalArgumentException
     *        If {@code username} is {@code null} or blank.
     */
    public Account(String username)
    {
        if (StringUtils.isBlank(username))
            throw new IllegalArgumentException("Null or blank username");
        
        this.entry = new StorageEntry();
        this.entry.put(keys().username(), username.toLowerCase());
        
        fillWithDefaults();
    }
    
    /**
     * Creates a new {@code Account} object based on a {@code StorageEntry}.
     *
     * @param entry
     *       A storage entry to hold data for this account.
     *
     * @param fillWithDefaults
     *       If {@code true}, the missing entry keys will be filled with their
     *       defaults.
     *
     * @throws IllegalArgumentException
     *        If {@code entry} is {@code null}, does not contain
     *        the username key or the username in this entry is {@code null}
     *        or blank.
     */
    /* package */ Account(StorageEntry entry, boolean fillWithDefaults)
    {
        if (entry == null)
            throw new IllegalArgumentException("Null storage entry");
        
        if (!entry.containsKey(keys().username()))
            throw new IllegalArgumentException("Missing entry key: username");
        
        if (StringUtils.isBlank(entry.get(keys().username())))
            throw new IllegalArgumentException("Null or blank username");
        
        this.entry = entry;
        
        if (fillWithDefaults)
        {
            fillWithDefaults();
        }
    }
    
    /**
     * Creates a new {@code Account} object based on a {@code StorageEntry},
     * filling all the missing keys with their defaults.
     *
     * @param entry
     *       A storage entry to hold data for this account.
     *
     * @throws IllegalArgumentException
     *        If {@code entry} is {@code null}, does not contain
     *        the username key or the username in this entry is {@code null}
     *        or blank.
     *
     * @see #Account(String)
     */
    public Account(StorageEntry entry)
    {
        this(entry, true);
    }
    
    /**
     * Returns the username.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>username</i>.
     *
     * @return The username.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public String getUsername()
    {
        if (!entry.containsKey(keys().username()))
            throw new IllegalArgumentException("Missing entry key: username");
        
        return entry.get(keys().username()).toLowerCase();
    }
    
    /**
     * Returns the UUID.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>uuid</i>.
     *
     * @return The UUID.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public String getUuid()
    {
        if (!entry.containsKey(keys().uuid()))
            throw new IllegalArgumentException("Missing entry key: uuid");
        
        return entry.get(keys().uuid());
    }
    
    /**
     * Changes the UUID.
     *
     * @param uuid
     *       The new UUID.
     *
     * @throws IllegalArgumentException
     *        If {@code uuid} is {@code null}.
     */
    public void setUuid(UUID uuid)
    {
        if (uuid == null)
            throw new IllegalArgumentException("Null uuid");
        
        entry.put(keys().uuid(), uuid.toString());
    }
    
    /**
     * Removes the UUID.
     */
    public void removeUuid()
    {
        entry.put(keys().uuid(), "");
    }
    
    /**
     * Checks whether passwords match.
     *
     * <p> The given password will be hashed using the algorithm specified
     * in the hashing_algorithm key. If this key does not represent a valid
     * hashing algorithm, the default hashing algorithm (stored in the config
     * file) will be used instead.
     *
     * <p> If passwords have been disabled as of the config file,
     * this method will always return {@code true}.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>salt</i>, <i>password</i>, <i>hashing_algorithm</i>.
     *
     * @param password
     *       The password to be checked.
     *
     * @return {@code true} if the password is correct; {@code false} otherwise.
     *
     * @throws IllegalArgumentException
     *        If {@code password} is {@code null}, or if the underlying entry
     *        does not contain the required keys.
     */
    public boolean checkPassword(String password)
    {
        if (password == null)
            throw new IllegalArgumentException("Null password");
        
        if (getConfig("secret.yml").getBoolean("passwords.disable"))
            return true;
        
        if (!entry.containsKey(keys().salt()))
            throw new IllegalArgumentException("Missing entry key: salt");
        
        if (!entry.containsKey(keys().password()))
            throw new IllegalArgumentException("Missing entry key: password");
        
        if (!entry.containsKey(keys().hashing_algorithm()))
            throw new IllegalArgumentException("Missing entry key: hashing_algorithm");
        
        String hash = entry.get(keys().password());
        HashingModel hashingModel =
                getSecurityHelper().getDefaultHashingModel();
        
        if (!getConfig("secret.yml").getBoolean("debug.forceHashingAlgorithm"))
        {
            String userHashingAlgorithm = entry.get(keys().hashing_algorithm());
            
            if (!StringUtils.isBlank(userHashingAlgorithm))
            {
                hashingModel = HashingModelDecoder.decode(userHashingAlgorithm);
            }
        }
        
        if (getConfig("secret.yml").getBoolean("passwords.useSalt"))
        {
            String salt = entry.get(keys().salt());
            
            return hashingModel.verify(password, salt, hash);
        }
        else
        {
            return hashingModel.verify(password, hash);
        }
    }
    
    /**
     * Changes the password.
     * 
     * <p> The password will be hashed
     * using the default algorithm specified in the config file.
     * 
     * <p> If passwords have been disabled as of the config file,
     * no action will be taken.
     * 
     * @param newPassword
     *       The new password.
     * 
     * @throws IllegalArgumentException
     *        If {@code newPassword} is {@code null}.
     */
    public void changePassword(String newPassword)
    {
        if (newPassword == null)
            throw new IllegalArgumentException("Null newPassword");
        
        if (getConfig("secret.yml").getBoolean("passwords.disable"))
            return;
        
        HashingModel hashingModel =
                getSecurityHelper().getDefaultHashingModel();
        String newHash;
        
        if (getConfig("secret.yml").getBoolean("passwords.useSalt"))
        {
            String newSalt = hashingModel.generateSalt();
            
            newHash = hashingModel.getHash(newPassword, newSalt);
            
            entry.put(keys().salt(), newSalt);
        }
        else
        {
            newHash = hashingModel.getHash(newPassword);
        }
        
        entry.put(keys().password(), newHash);
        entry.put(keys().hashing_algorithm(), hashingModel.encode());
    }
    
    /**
     * Returns the IP address.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>ip</i>.
     *
     * @return The IP address.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public String getIp()
    {
        if (!entry.containsKey(keys().ip()))
            throw new IllegalArgumentException("Missing entry key: ip");
        
        return entry.get(keys().ip());
    }
    
    /**
     * Changes the IP address.
     * 
     * @param ip
     *       The new IP address.
     * 
     * @throws IllegalArgumentException
     *        If {@code ip} is {@code null} or is not a valid IPv4/6 address.
     */
    public void setIp(String ip)
    {
        if (ip == null)
            throw new IllegalArgumentException("Null ip");
        
        if (!Validators.validateIp(ip))
            throw new IllegalArgumentException("ip is not a valid IPv4/6 address");
        
        entry.put(keys().ip(), ip);
    }
    
    /**
     * Removes the IP address.
     */
    public void removeIp()
    {
        entry.put(keys().ip(), "");
    }
    
    /**
     * Returns the login-session string.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>login_session</i>.
     *
     * @return The login-session string.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public String getLoginSession()
    {
        if (!entry.containsKey(keys().login_session()))
            throw new IllegalArgumentException("Missing entry key: login_session");
        
        return entry.get(keys().login_session());
    }
    
    /**
     * Saves login session.
     *
     * @param ip
     *       The player IP address.
     * @param time
     *       The UNIX time of when the login session was saved.
     *
     * @throws IllegalArgumentException
     *        If {@code ip} is {@code null} or is not a valid IPv4/6 address,
     *        or if {@code time} is negative.
     */
    public void saveLoginSession(String ip, long time)
    {
        if (ip == null)
            throw new IllegalArgumentException("Null ip");
        
        if (!Validators.validateIp(ip))
            throw new IllegalArgumentException("ip is not a valid IPv4/6 address");
        
        if (time < 0)
            throw new IllegalArgumentException("Negative time");
        
        entry.put(keys().login_session(), ip + ";" + time);
    }
    
    /**
     * Erases the login session.
     */
    public void eraseLoginSession()
    {
        entry.put(keys().login_session(), "");
    }
    
    /**
     * Returns the e-mail address.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>email</i>.
     *
     * @return The e-mail address.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public String getEmail()
    {
        if (!entry.containsKey(keys().email()))
            throw new IllegalArgumentException("Missing entry key: email");
        
        return entry.get(keys().email()).toLowerCase();
    }
    
    /**
     * Changes the e-mail address.
     * 
     * @param email
     *       The new e-mail address.
     *
     * @throws IllegalArgumentException
     *        If {@code email} is {@code null} or is not a valid e-mail address.
     */
    public void setEmail(String email)
    {
        if (email == null)
            throw new IllegalArgumentException("Null email");
        
        if (!Validators.validateEmail(email))
            throw new IllegalArgumentException("email is not a valid e-mail address");
        
        entry.put(keys().email(), email.toLowerCase());
    }
    
    public void removeEmail()
    {
        entry.put(keys().email(), "");
    }
    
    /**
     * Returns the last-active date.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>last_active_date</i>.
     *
     * @return The last-active date in UNIX time.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public long getLastActiveDate()
    {
        if (!entry.containsKey(keys().last_active_date()))
            throw new IllegalArgumentException("Missing entry key: last_active_date");
        
        return Long.parseLong(entry.get(keys().last_active_date()));
    }
    
    /**
     * Changes the last-active date.
     * 
     * @param unixTime
     *       The new last-active date in UNIX time.
     */
    public void setLastActiveDate(long unixTime)
    {
        entry.put(keys().last_active_date(), String.valueOf(unixTime));
    }
    
    /**
     * Returns the registration date.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>reg_date</i>.
     *
     * @return The registration date in UNIX time.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public long getRegistrationDate()
    {
        if (!entry.containsKey(keys().reg_date()))
            throw new IllegalArgumentException("Missing entry key: reg_date");
        
        return Long.parseLong(entry.get(keys().reg_date()));
    }
    
    /**
     * Changes the registration date.
     *
     * @param unixTime
     *       The new registration date in UNIX time.
     */
    public void setRegistrationDate(long unixTime)
    {
        entry.put(keys().reg_date(), String.valueOf(unixTime));
    }
    
    /**
     * Checks whether this account has been locked.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>is_locked</i>.
     *
     * @return {@code true} if this account is locked; {@code false} otherwise.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public boolean isLocked()
    {
        if (!entry.containsKey(keys().is_locked()))
            throw new IllegalArgumentException("Missing entry key: is_locked");
        
        return entry.get(keys().is_locked()).equals("1");
    }
    
    /**
     * Locks or unlocks this account.
     *
     * <p> Locked accounts disallow their owners to join the game.
     *
     * @param locked
     *       Whether this account should be locked or unlocked.
     */
    public void setLocked(boolean locked)
    {
        entry.put(keys().is_locked(), locked ? "1" : "0");
    }
    
    /**
     * Returns the login history.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>login_history</i>.
     *
     * @return The login history.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public List<String> getLoginHistory()
    {
        if (!entry.containsKey(keys().login_history()))
            throw new IllegalArgumentException("Missing entry key: login_history");
        
        return new ArrayList<>(Arrays.asList(
                LOGIN_HISTORY_SEPARATOR_PATTERN.split(
                        entry.get(keys().login_history())
                )
        ));
    }
    
    /**
     * Records a player login.
     *
     * @param unixTime
     *       The UNIX time of the recorded login.
     *
     * @param ip
     *       An IP address of the player who tried to log in.
     *
     * @param succeeded
     *       Whether the login succeeded or failed. By <i>succeeded</i> I mean
     *       that the entered password was correct.
     *
     * @throws IllegalArgumentException
     *        If {@code unixTime} is negative, or if {@code ip} is not null
     *        but is not a valid IPv4/6 address.
     */
    public void recordLogin(long unixTime, String ip, boolean succeeded)
    {
        if (unixTime < 0)
            throw new IllegalArgumentException("Negative unixTime");
        
        if (ip != null && !Validators.validateIp(ip))
            throw new IllegalArgumentException("ip is not a valid IPv4/6 address");
        
        if (!entry.containsKey(keys().login_history()))
            throw new IllegalArgumentException("Missing entry key: login_history");
        
        List<String> records = getLoginHistory();
        int recordsToKeep = getConfig("config.yml")
                .getInt("loginHistory.recordsToKeep");
        
        for (int i = 0, n = records.size() - recordsToKeep + 1;  i < n; i++)
        {
            records.remove(0);
        }
        
        if (ip == null)
        {
            records.add(unixTime + ";?.?.?.?;" + succeeded);
        }
        else
        {
            records.add(unixTime + ";" + ip + ";" + succeeded);
        }
        
        StringBuilder historyBuilder = new StringBuilder();
        
        for (String record : records)
        {
            if (!record.isEmpty())
            {
                historyBuilder.append(record);
                historyBuilder.append(LOGIN_HISTORY_SEPARATOR);
            }
        }
        
        entry.put(keys().login_history(), historyBuilder.toString());
    }
    
    /**
     * Returns the display name.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>display_name</i>.
     *
     * @return The display name.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     */
    public String getDisplayName()
    {
        if (!entry.containsKey(keys().display_name()))
            throw new IllegalArgumentException("Missing entry key: display_name");
        
        return entry.get(keys().display_name());
    }
    
    /**
     * Changes the display name.
     *
     * @param displayName
     *       The new display name.
     *
     * @throws IllegalArgumentException
     *        If {@code displayName} is {@code null}.
     */
    public void setDisplayName(String displayName)
    {
        if (displayName == null)
            throw new IllegalArgumentException("Null displayName");
        
        entry.put(keys().display_name(), displayName);
    }
    
    /**
     * Returns the persistence data as a {@code Map<String, String>}.
     *
     * <p> This method requires the following keys to exist in the underlying
     * storage entry: <i>persistence</i>.
     *
     * @return The persistence data, or {@code null} if an I/O error occurred
     *         whilst the deserialization process.
     *
     * @throws IllegalArgumentException
     *        If the underlying entry does not contain the required keys.
     *
     * @throws ReportedException
     *        If an I/O error occurred while deserializing the persistence,
     *        and the error was reported to the logger.
     */
    public Map<String, String> getPersistence()
    {
        if (!entry.containsKey(keys().persistence()))
            throw new IllegalArgumentException("Missing entry key: persistence");
        
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
                persistence = IniUtils.unserialize(
                        persistenceString
                ).get("persistence");
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
     * Saves persistence data.
     * 
     * @param persistence
     *       The new persistence data.
     * 
     * @throws IllegalArgumentException
     *        If {@code persistence} is {@code null}.
     * 
     * @throws ReportedException
     *        If an I/O error occurred while serializing the persistence,
     *        and the error was reported to the logger.
     */
    public void savePersistence(Map<String, String> persistence)
    {
        if (persistence == null)
            throw new IllegalArgumentException("Null persistence");
        
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
    
    /**
     * Clones this {@code Account}.
     *
     * <p> A new {@code StorageEntry} is created as a copy of the
     * original entry.
     *
     * @param username
     *       A username for the cloned account.
     *
     * @return The cloned {@code Account} object.
     */
    public Account clone(String username)
    {
        if (StringUtils.isBlank(username))
            throw new IllegalArgumentException("Null or blank username");
        
        StorageEntry entryClone = entry.copy();
        
        entryClone.put(keys().username(), username.toLowerCase());
        entryClone.clearKeyDirty(keys().username());
        
        Account accountClone = new Account(entryClone, false);
        
        return accountClone;
    }
    
    /**
     * Enqueues a new save-callback to be called when this account
     * gets updated in a {@code Storage}.
     *
     * <p> Once the callback gets called, it is removed from the queue.
     *
     * @param callback
     *       The save-callback to be enqueued.
     */
    public void enqueueSaveCallback(SaveCallback callback)
    {
        if (callback == null)
            throw new IllegalArgumentException("Null callback");
        
        saveCallbacks.add(callback);
    }
    
    /* package */ void runSaveCallbacks(boolean success)
    {
        while (!saveCallbacks.isEmpty())
        {
            saveCallbacks.remove().onSave(success);
        }
    }
    
    /**
     * Fills with defaults keys that are missing in this account.
     */
    private void fillWithDefaults()
    {
        if (!entry.containsKey(keys().uuid()))
        {
            entry.put(keys().uuid(), "");
        }
        
        if (!entry.containsKey(keys().salt()))
        {
            entry.put(keys().salt(), "");
        }
        
        if (!entry.containsKey(keys().password()))
        {
            entry.put(keys().password(), "");
        }
        
        if (!entry.containsKey(keys().hashing_algorithm()))
        {
            entry.put(keys().hashing_algorithm(), "");
        }
        
        if (!entry.containsKey(keys().ip()))
        {
            entry.put(keys().ip(), "");
        }
        
        if (!entry.containsKey(keys().login_session()))
        {
            entry.put(keys().login_session(), "");
        }
        
        if (!entry.containsKey(keys().email()))
        {
            entry.put(keys().email(), "");
        }
        
        if (!entry.containsKey(keys().last_active_date()))
        {
            entry.put(keys().last_active_date(), "-1");
        }
        
        if (!entry.containsKey(keys().reg_date()))
        {
            entry.put(keys().reg_date(), "-1");
        }
        
        if (!entry.containsKey(keys().is_locked()))
        {
            entry.put(keys().is_locked(), "0");
        }
        
        if (!entry.containsKey(keys().login_history()))
        {
            entry.put(keys().login_history(), "");
        }
        
        if (!entry.containsKey(keys().display_name()))
        {
            entry.put(keys().display_name(), "");
        }
        
        if (!entry.containsKey(keys().persistence()))
        {
            entry.put(keys().persistence(), "");
        }
    }
    
    /**
     * Returns the underlying storage entry.
     *
     * <p> <b>Do not use unless you know what you're doing!</b>
     *
     * @return The account entry.
     */
    public StorageEntry getEntry()
    {
        return entry;
    }
    
    /* package */ void setEntry(StorageEntry entry)
    {
        if (entry == null)
            throw new IllegalArgumentException();
        
        this.entry = entry;
    }
    
    public void bufferLock()
    {
        if (bufferLocked)
            throw new IllegalStateException("Account already buffer-locked");
        
        bufferLocked = true;
    }
    
    public void bufferUnlock()
    {
        bufferLocked = false;
    }
    
    public boolean isBufferLocked()
    {
        return bufferLocked;
    }
    
    /**
     * @see Account#enqueueSaveCallback(SaveCallback)
     */
    public static interface SaveCallback
    {
        /**
         * Called after a save process.
         * 
         * @param success
         *       Whether the account was successfully saved in a
         *       {@code Storage}.
         */
        public void onSave(boolean success);
    }
    
    /**
     * Used for {@link #recordLogin(long, String, boolean)}.
     */
    public static final boolean LOGIN_SUCCESS = true;
    
    /**
     * Used for {@link #recordLogin(long, String, boolean)}.
     */
    public static final boolean LOGIN_FAIL = false;
    
    public static final String LOGIN_HISTORY_SEPARATOR = "|";
    private static final Pattern LOGIN_HISTORY_SEPARATOR_PATTERN =
            Pattern.compile(Pattern.quote(LOGIN_HISTORY_SEPARATOR));
    
    private StorageEntry entry;
    private final Queue<SaveCallback> saveCallbacks = new LinkedList<>();
    private boolean bufferLocked = false;
}
