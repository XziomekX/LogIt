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
        Whirlpool w = new Whirlpool();
        
        byte[] digest = new byte[64];
        
        w.NESSIEinit();
        w.NESSIEadd(string);
        w.NESSIEfinalize(digest);
        
        return Whirlpool.display(digest);
    }
    
    private static String getHash(String string, String algorithm)
    {
        MessageDigest md;
        
        try
        {
            md = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException ex)
        {
            return null;
        }
        
        md.update(string.getBytes());
        byte bytes[] = md.digest();
        StringBuilder sb = new StringBuilder();
        
        for (byte b : bytes)
        {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        
        return sb.toString();
    }
}
