package io.github.lucaseasedup.logit.security.model;

import io.github.lucaseasedup.logit.security.lib.Whirlpool;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public final class CommonHashingModel implements HashingModel
{
    public CommonHashingModel(Algorithm algorithm, int rounds)
    {
        if (algorithm == null || rounds <= 0)
            throw new IllegalArgumentException();
        
        this.algorithm = algorithm;
        this.rounds = rounds;
    }
    
    @Override
    public String getHash(String string)
    {
        String output = "";
        
        for (int i = 0; i < rounds; i++)
        {
            output = getSingleHash(output + string);
        }
        
        return output;
    }
    
    @Override
    public String getHash(String string, String salt)
    {
        String output = "";
        
        for (int i = 0; i < rounds; i++)
        {
            output = getSingleHash(output + string + salt);
        }
        
        return output;
    }
    
    private String getSingleHash(String string)
    {
        switch (algorithm)
        {
            case PLAIN:
                return string;
                
            case MD2:
                return getMd2(string);
                
            case MD5:
                return getMd5(string);
                
            case SHA1:
                return getSha1(string);
                
            case SHA256:
                return getSha256(string);
                
            case SHA384:
                return getSha384(string);
                
            case SHA512:
                return getSha512(string);
                
            case WHIRLPOOL:
                return getWhirlpool(string);
                
            default:
            {
                throw new IllegalArgumentException(
                        "Unsupported algorithm: " + algorithm
                );
            }
        }
    }
    
    @Override
    public boolean verify(String string, String hash)
    {
        return hash.equals(getHash(string));
    }
    
    @Override
    public boolean verify(String string, String salt, String hash)
    {
        return hash.equals(getHash(string, salt));
    }
    
    @Override
    public String generateSalt()
    {
        char[] charTable = {
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
    
    @Override
    public String encode()
    {
        return algorithm.encode() + "(" + rounds + ")";
    }
    
    public Algorithm getAlgorithm()
    {
        return algorithm;
    }
    
    public int getRounds()
    {
        return rounds;
    }
    
    /**
     * Returns an MD2 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getMd2(String string)
    {
        return getOriginHash(string, "MD2");
    }
    
    /**
     * Returns an MD5 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getMd5(String string)
    {
        return getOriginHash(string, "MD5");
    }
    
    /**
     * Returns a SHA-1 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getSha1(String string)
    {
        return getOriginHash(string, "SHA-1");
    }
    
    /**
     * Returns a SHA-256 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getSha256(String string)
    {
        return getOriginHash(string, "SHA-256");
    }
    
    /**
     * Returns a SHA-384 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getSha384(String string)
    {
        return getOriginHash(string, "SHA-384");
    }
    
    /**
     * Returns a SHA-512 hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getSha512(String string)
    {
        return getOriginHash(string, "SHA-512");
    }
    
    /**
     * Returns a Whirlpool hash of the given string.
     * 
     * @param string the string to be hashed.
     * @return hashed string.
     */
    public static String getWhirlpool(String string)
    {
        Whirlpool whirlpool = new Whirlpool();
        byte[] digest = new byte[64];
        
        whirlpool.NESSIEinit();
        whirlpool.NESSIEadd(string);
        whirlpool.NESSIEfinalize(digest);
        
        return Whirlpool.display(digest);
    }
    
    private static String getOriginHash(String string, String algorithm)
    {
        MessageDigest digest;
        
        try
        {
            digest = MessageDigest.getInstance(algorithm);
            digest.update(string.getBytes());
        }
        catch (NoSuchAlgorithmException ex)
        {
            return null;
        }
        
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        
        for (byte b : bytes)
        {
            sb.append(Integer.toString((b & 0xFF) + 0x100, 16).substring(1));
        }
        
        return sb.toString();
    }
    
    public static enum Algorithm
    {
        PLAIN("plain"),
        MD2("md2"), MD5("md5"),
        SHA1("sha-1"), SHA256("sha-256"), SHA384("sha-384"), SHA512("sha-512"),
        WHIRLPOOL("whirlpool");
        
        private Algorithm(String name)
        {
            if (name == null)
                throw new IllegalArgumentException("Null name");
            
            this.name = name;
        }
        
        /**
         * Returns a string representation of this {@code Algorithm}.
         * 
         * @return the string representation of this {@code Algorithm}.
         */
        public String encode()
        {
            return name;
        }
        
        /**
         * Decodes a string into a {@code Algorithm}.
         * 
         * @param name string representation of a {@code Algorithm}.
         * 
         * @return the corresponding {@code Algorithm}, or {@code null} if
         *         no {@code Algorithm} was found for the given string.
         */
        public static Algorithm decode(String name)
        {
            for (Algorithm value : values())
            {
                if (value.encode().equals(name))
                {
                    return value;
                }
            }
            
            return null;
        }
        
        private final String name;
    }
    
    private final Algorithm algorithm;
    private final int rounds;
}
