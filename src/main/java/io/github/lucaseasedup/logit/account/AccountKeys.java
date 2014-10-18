package io.github.lucaseasedup.logit.account;

import com.google.common.collect.ImmutableList;
import io.github.lucaseasedup.logit.storage.Storage.DataType;
import java.util.List;
import org.apache.tools.ant.util.LinkedHashtable;

public final class AccountKeys extends LinkedHashtable<String, DataType>
{
    public AccountKeys(String username,
                       String uuid,
                       String salt,
                       String password,
                       String hashing_algorithm,
                       String ip,
                       String login_session,
                       String email,
                       String last_active_date,
                       String reg_date,
                       String is_locked,
                       String login_history,
                       String display_name,
                       String persistence)
    {
        if (       username == null
                || uuid == null
                || salt == null
                || password == null
                || hashing_algorithm == null
                || ip == null
                || login_session == null
                || email == null
                || last_active_date == null
                || reg_date == null
                || is_locked == null
                || login_history == null
                || display_name == null
                || persistence == null)
        {
            throw new IllegalArgumentException();
        }
        
        put(username,          DataType.TINYTEXT);
        put(uuid,              DataType.TINYTEXT);
        put(salt,              DataType.TINYTEXT);
        put(password,          DataType.MEDIUMTEXT);
        put(hashing_algorithm, DataType.TINYTEXT);
        put(ip,                DataType.TINYTEXT);
        put(login_session,     DataType.TINYTEXT);
        put(email,             DataType.TINYTEXT);
        put(last_active_date,  DataType.INTEGER);
        put(reg_date,          DataType.INTEGER);
        put(is_locked,         DataType.INTEGER);
        put(login_history,     DataType.LONGTEXT);
        put(display_name,      DataType.TINYTEXT);
        put(persistence,       DataType.TEXT);
        
        this.names = new ImmutableList.Builder<String>().add(
            username,
            uuid,
            salt,
            password,
            hashing_algorithm,
            ip,
            login_session,
            email,
            last_active_date,
            reg_date,
            is_locked,
            login_history,
            display_name,
            persistence).build();
        
        this.username = username;
        this.uuid = uuid;
        this.salt = salt;
        this.password = password;
        this.hashing_algorithm = hashing_algorithm;
        this.ip = ip;
        this.login_session = login_session;
        this.email = email;
        this.last_active_date = last_active_date;
        this.reg_date = reg_date;
        this.is_locked = is_locked;
        this.login_history = login_history;
        this.display_name = display_name;
        this.persistence = persistence;
    }
    
    public String username()
    {
        return username;
    }
    
    public String uuid()
    {
        return uuid;
    }
    
    public String salt()
    {
        return salt;
    }
    
    public String password()
    {
        return password;
    }
    
    public String hashing_algorithm()
    {
        return hashing_algorithm;
    }
    
    public String ip()
    {
        return ip;
    }
    
    public String login_session()
    {
        return login_session;
    }
    
    public String email()
    {
        return email;
    }
    
    public String last_active_date()
    {
        return last_active_date;
    }
    
    public String reg_date()
    {
        return reg_date;
    }
    
    public String is_locked()
    {
        return is_locked;
    }
    
    public String login_history()
    {
        return login_history;
    }
    
    public String display_name()
    {
        return display_name;
    }
    
    public String persistence()
    {
        return persistence;
    }
    
    public List<String> getNames()
    {
        return names;
    }
    
    public static final AccountKeys DEFAULT = new AccountKeys(
            "username",
            "uuid",
            "salt",
            "password",
            "hashing_algorithm",
            "ip",
            "login_session",
            "email",
            "last_active_date",
            "reg_date",
            "is_locked",
            "login_history",
            "display_name",
            "persistence"
    );
    
    private static final long serialVersionUID = 1L;
    
    private final List<String> names;
    private final String username;
    private final String uuid;
    private final String salt;
    private final String password;
    private final String hashing_algorithm;
    private final String ip;
    private final String login_session;
    private final String email;
    private final String last_active_date;
    private final String reg_date;
    private final String is_locked;
    private final String login_history;
    private final String display_name;
    private final String persistence;
}
