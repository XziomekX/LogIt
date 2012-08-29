package com.gmail.lucaseasedup.logit.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author LucasEasedUp
 */
public class HashGenerator
{
    private HashGenerator()
    {
    }
    
    public static String getMd2(String string)
    {
        return getHash(string, "MD2");
    }
    
    public static String getMd5(String string)
    {
        return getHash(string, "MD5");
    }
    
    public static String getSha1(String string)
    {
        return getHash(string, "SHA-1");
    }
    
    public static String getSha256(String string)
    {
        return getHash(string, "SHA-256");
    }
    
    public static String getSha384(String string)
    {
        return getHash(string, "SHA-384");
    }
    
    public static String getSha512(String string)
    {
        return getHash(string, "SHA-512");
    }
    
    public static String getWhirlpool(String string)
    {
        Whirlpool w      = new Whirlpool();
        byte[]    digest = new byte[64];
        
        w.NESSIEinit();
        w.NESSIEadd(string);
        w.NESSIEfinalize(digest);
        
        return Whirlpool.display(digest);
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
        {
            stringBuilder.append(Integer.toString((b & 0xFF) + 0x100, 16).substring(1));
        }
        
        return stringBuilder.toString();
    }
}
