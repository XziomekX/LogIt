/*
 * HashGenerator.java
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
package io.github.lucaseasedup.logit.hash;

import io.github.lucaseasedup.logit.LogItCore.HashingAlgorithm;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Provides a hashing tool for different algorithms.
 * 
 * @author LucasEasedUp
 */
public class HashGenerator
{
    private HashGenerator()
    {
    }
    
    /**
     * Returns an MD2 hash of the given string.
     * 
     * @param string String to be hashed.
     * @return Hashed string.
     */
    public static String getMd2(String string)
    {
        return getHash(string, "MD2");
    }
    
    /**
     * Returns an MD5 hash of the given string.
     * 
     * @param string String to be hashed.
     * @return Hashed string.
     */
    public static String getMd5(String string)
    {
        return getHash(string, "MD5");
    }
    
    /**
     * Returns a SHA-1 hash of the given string.
     * 
     * @param string String to be hashed.
     * @return Hashed string.
     */
    public static String getSha1(String string)
    {
        return getHash(string, "SHA-1");
    }
    
    /**
     * Returns a SHA-256 hash of the given string.
     * 
     * @param string String to be hashed.
     * @return Hashed string.
     */
    public static String getSha256(String string)
    {
        return getHash(string, "SHA-256");
    }
    
    /**
     * Returns a SHA-384 hash of the given string.
     * 
     * @param string String to be hashed.
     * @return Hashed string.
     */
    public static String getSha384(String string)
    {
        return getHash(string, "SHA-384");
    }
    
    /**
     * Returns a SHA-512 hash of the given string.
     * 
     * @param string String to be hashed.
     * @return Hashed string.
     */
    public static String getSha512(String string)
    {
        return getHash(string, "SHA-512");
    }
    
    /**
     * Returns a Whirlpool hash of the given string.
     * 
     * @param string String to be hashed.
     * @return Hashed string.
     */
    public static String getWhirlpool(String string)
    {
        Whirlpool w = new Whirlpool();
        byte[]    digest = new byte[64];
        
        w.NESSIEinit();
        w.NESSIEadd(string);
        w.NESSIEfinalize(digest);
        
        return Whirlpool.display(digest);
    }
    
    public static String getBCrypt(String string, String salt)
    {
        return BCrypt.hashpw(string, salt);
    }
    
    private static String getHash(String string, String algorithm)
    {
        StringBuilder stringBuilder = new StringBuilder();
        MessageDigest messageDigest;
        
        try
        {
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(string.getBytes());
        }
        catch (NoSuchAlgorithmException ex)
        {
            return null;
        }
        
        byte bytes[] = messageDigest.digest();
        
        for (byte b : bytes)
            stringBuilder.append(Integer.toString((b & 0xFF) + 0x100, 16).substring(1));
        
        return stringBuilder.toString();
    }
    
    public static String generateSalt(HashingAlgorithm hashingAlgorithm)
    {
        if (hashingAlgorithm == HashingAlgorithm.BCRYPT)
        {
            return BCrypt.gensalt();
        }
        else
        {
            SecureRandom sr   = new SecureRandom();
            byte[]       salt = new byte[20];
            
            sr.nextBytes(salt);
            
            // Replace backslashes with forwardslashes to avoid escaping problems with different DBMSs.
            for (int i = 0; i < salt.length; i++)
            {
                if (salt[i] == '\\')
                {
                    salt[i] = '/';
                }
            }
            
            return new String(salt);
        }
    }
}
