/*
 * HashGenerator.java
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
package io.github.lucaseasedup.logit.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Provides a hashing tool for different algorithms.
 */
public final class HashGenerator
{
    private HashGenerator()
    {
    }
    
    /**
     * Returns an MD2 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getMd2(String string)
    {
        return getHash(string, "MD2");
    }
    
    /**
     * Returns an MD5 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getMd5(String string)
    {
        return getHash(string, "MD5");
    }
    
    /**
     * Returns a SHA-1 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getSha1(String string)
    {
        return getHash(string, "SHA-1");
    }
    
    /**
     * Returns a SHA-256 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getSha256(String string)
    {
        return getHash(string, "SHA-256");
    }
    
    /**
     * Returns a SHA-384 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getSha384(String string)
    {
        return getHash(string, "SHA-384");
    }
    
    /**
     * Returns a SHA-512 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getSha512(String string)
    {
        return getHash(string, "SHA-512");
    }
    
    /**
     * Returns a Whirlpool hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
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
        StringBuilder sb = new StringBuilder();
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
        {
            sb.append(Integer.toString((b & 0xFF) + 0x100, 16).substring(1));
        }
        
        return sb.toString();
    }
    
    public static String generateSalt(HashingAlgorithm hashingAlgorithm)
    {
        if (hashingAlgorithm == HashingAlgorithm.BCRYPT)
        {
            return BCrypt.gensalt(12);
        }
        else
        {
            char[] charTable = new char[]{
                '1','2','3','4','5','6','7','8','9','0','_',
                'a','b','c','d','e','f','g','h','i','j','k',
                'l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                'A','B','C','D','E','F','G','H','I','J','K',
                'L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            };
            
            StringBuilder sb = new StringBuilder(20);
            Random random = new Random();
            
            for (int i = 0, n = charTable.length; i < 20; i++)
            {
                sb.append(charTable[random.nextInt(n)]);
            }
            
            return sb.toString();
        }
    }
}
