/*
 * AccountKeys.java
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

import io.github.lucaseasedup.logit.storage.Storage.Type;
import org.apache.tools.ant.util.LinkedHashtable;

public final class AccountKeys extends LinkedHashtable<String, Type>
{
    public AccountKeys(String username,
                       String salt,             
                       String password,
                       String hashing_algorithm,
                       String ip,
                       String login_session,
                       String email,
                       String last_active_date,
                       String reg_date,
                       String persistence)
    {
        put(username,          Type.TINYTEXT);
        put(salt,              Type.TINYTEXT);
        put(password,          Type.MEDIUMTEXT);
        put(hashing_algorithm, Type.TINYTEXT);
        put(ip,                Type.TINYTEXT);
        put(login_session,     Type.TINYTEXT);
        put(email,             Type.TINYTEXT);
        put(last_active_date,  Type.REAL);
        put(reg_date,          Type.INTEGER);
        put(persistence,       Type.TEXT);
        
        this.username = username;
        this.salt = salt;
        this.password = password;
        this.hashing_algorithm = hashing_algorithm;
        this.ip = ip;
        this.login_session = login_session;
        this.email = email;
        this.last_active_date = last_active_date;
        this.reg_date = reg_date;
        this.persistence = persistence;
    }
    
    public String username()
    {
        return username;
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
    
    public String persistence()
    {
        return persistence;
    }
    
    private final String username;
    private final String salt;
    private final String password;
    private final String hashing_algorithm;
    private final String ip;
    private final String login_session;
    private final String email;
    private final String last_active_date;
    private final String reg_date;
    private final String persistence;
}

