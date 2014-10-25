package io.github.lucaseasedup.logit.security;

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.regex.Pattern;

public final class SecurityHelper extends LogItCoreObject
{
    /**
     * Checks if a password is equal, after hashing, to {@code hashedPassword}.
     * 
     * <p> If the <i>debug.forceHashingAlgorithm</i>
     * secret setting is set to <i>true</i>,
     * the global hashing algorithm (specified in the config file)
     * will be used instead of the provided {@code hashingAlgorithm}.
     * 
     * @param password         the password to be checked.
     * @param hashedPassword   the hashed password.
     * @param hashingAlgorithm the algorithm used when hashing {@code hashedPassword}.
     * 
     * @return {@code true} if passwords match; {@code false} otherwise.
     * 
     * @throws IllegalArgumentException if {@code password}, {@code hashedPassword}
     *                                  or {@code hashingAlgorithm} is {@code null}.
     * 
     * @see #checkPassword(String, String, String, String)
     */
    public boolean checkPassword(String password,
                                 String hashedPassword,
                                 String hashingAlgorithm)
    {
        if (password == null || hashedPassword == null
                || hashingAlgorithm == null)
        {
            throw new IllegalArgumentException();
        }
        
        if (getConfig("secret.yml").getBoolean("debug.forceHashingAlgorithm"))
        {
            hashingAlgorithm = getDefaultHashingAlgorithm().name();
        }
        
        HashingAlgorithm algorithmType = HashingAlgorithm.decode(hashingAlgorithm);
        
        if (algorithmType == null)
        {
            return false;
        }
        else if (algorithmType == HashingAlgorithm.BCRYPT)
        {
            return BCrypt.checkpw(password, hashedPassword);
        }
        else if (algorithmType == HashingAlgorithm.AUTHME)
        {
            return AuthMePasswordHelper.comparePasswordWithHash(password, hashedPassword,
                    hashingAlgorithm.replaceAll("^authme:", ""));
        }
        else
        {
            return hashedPassword.equals(SecurityHelper.getHash(password, algorithmType));
        }
    }
    
    /**
     * Checks if a password (with a salt appended) is equal,
     * after hashing, to {@code hashedPassword}.
     * 
     * <p> If the <i>debug.forceHashingAlgorithm</i>
     * secret setting is set to <i>true</i>,
     * the global hashing algorithm (specified in the config file)
     * will be used instead of the provided {@code hashingAlgorithm}.
     * 
     * @param password         the password to be checked.
     * @param hashedPassword   the hashed password.
     * @param salt             the salt for the passwords.
     * @param hashingAlgorithm the algorithm used when hashing {@code hashedPassword}.
     * 
     * @return {@code true} if passwords match; {@code false} otherwise.
     * 
     * @throws IllegalArgumentException if {@code password}, {@code hashedPassword},
     *                                  {@code salt} or {@code hashingAlgorithm} is
     *                                  {@code null}.
     * 
     * @see #checkPassword(String, String, String)
     */
    public boolean checkPassword(String password,
                                 String hashedPassword,
                                 String salt,
                                 String hashingAlgorithm)
    {
        if (password == null || hashedPassword == null
                || salt == null || hashingAlgorithm == null)
        {
            throw new IllegalArgumentException();
        }
        
        if (getConfig("secret.yml").getBoolean("debug.forceHashingAlgorithm"))
        {
            hashingAlgorithm = getDefaultHashingAlgorithm().name();
        }
        
        HashingAlgorithm algorithmType = HashingAlgorithm.decode(hashingAlgorithm);
        
        if (algorithmType == null)
        {
            return false;
        }
        else if (algorithmType == HashingAlgorithm.BCRYPT)
        {
            try
            {
                return BCrypt.checkpw(password, hashedPassword);
            }
            catch (IllegalArgumentException ex)
            {
                return false;
            }
        }
        else if (algorithmType == HashingAlgorithm.AUTHME)
        {
            return AuthMePasswordHelper.comparePasswordWithHash(password, hashedPassword,
                    hashingAlgorithm.replaceAll("^authme:", ""));
        }
        else
        {
            if (getConfig("secret.yml").getBoolean("passwords.useSalt"))
            {
                return hashedPassword.equals(
                        SecurityHelper.getHash(password, salt, algorithmType)
                );
            }
            else
            {
                return hashedPassword.equals(
                        SecurityHelper.getHash(password, algorithmType)
                );
            }
        }
    }
    
    public boolean containsLowercaseLetters(String password)
    {
        if (password == null)
            throw new IllegalArgumentException();
        
        return LOWERCASE_LETTERS.matcher(password).find();
    }
    
    public boolean containsUppercaseLetters(String password)
    {
        if (password == null)
            throw new IllegalArgumentException();
        
        return UPPERCASE_LETTERS.matcher(password).find();
    }
    
    public boolean containsNumbers(String password)
    {
        if (password == null)
            throw new IllegalArgumentException();
        
        return NUMBERS.matcher(password).find();
    }
    
    public boolean containsSpecialSymbols(String password)
    {
        if (password == null)
            throw new IllegalArgumentException();
        
        return SPECIAL_SYMBOLS.matcher(password).find();
    }
    
    public boolean isSimplePassword(String password)
    {
        if (password == null)
            throw new IllegalArgumentException();
        
        for (int i = 0; i < password.length(); i++)
        {
            if (password.charAt(i) != ('1' + i))
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns the default hashing algorithm specified in the config file.
     * 
     * @return the default hashing algorithm.
     */
    public HashingAlgorithm getDefaultHashingAlgorithm()
    {
        return HashingAlgorithm.decode(
                getConfig("config.yml").getString("passwords.hashingAlgorithm")
        );
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
    
    /**
     * Hashes a string using the specified algorithm.
     * 
     * @param string           the string to be hashed.
     * @param hashingAlgorithm the hashing algorithm to be used.
     * 
     * @return the resulting hash.
     * 
     * @throws IllegalArgumentException if this method does not support the given algorithm.
     * 
     * @see #getHash(String, String, HashingAlgorithm)
     */
    public static String getHash(String string,
                                 HashingAlgorithm hashingAlgorithm)
    {
        switch (hashingAlgorithm)
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
                
            case BCRYPT:
                return getBCrypt(string, "");
                
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + hashingAlgorithm);
        }
    }
    
    /**
     * Hashes a string with a salt using the specified algorithm.
     * 
     * @param string           the string to be hashed.
     * @param salt             the salt to be appended to {@code string}.
     * @param hashingAlgorithm the hashing algorithm to be used.
     * 
     * @return resulting hash.
     * 
     * @see #getHash(String, HashingAlgorithm)
     */
    public static String getHash(String string,
                                 String salt,
                                 HashingAlgorithm hashingAlgorithm)
    {
        String hash;
        
        if (hashingAlgorithm == HashingAlgorithm.BCRYPT)
        {
            hash = getBCrypt(string, salt);
        }
        else if (hashingAlgorithm == HashingAlgorithm.PLAIN)
        {
            hash = getHash(string, hashingAlgorithm);
        }
        else
        {
            hash = getHash(string + salt, hashingAlgorithm);
        }
        
        return hash;
    }
    
    private static String getHash(String string, String algorithm)
    {
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
        StringBuilder sb = new StringBuilder();
        
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
    
    /**
     * Generates a random password of length equal to {@code length},
     * consisting only of the characters contained in {@code combination}.
     * 
     * <p> If {@code combination} contains more than one occurrence of a character,
     * the overall probability of using it in password generation will be higher.
     * 
     * @param length      the desired password length.
     * @param combination the letterset used in the generation process.
     * 
     * @return the generated password.
     */
    public static String generatePassword(int length, String combination)
    {
        char[] charArray = combination.toCharArray();
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        
        for (int i = 0, n = charArray.length; i < length; i++)
        {
            sb.append(charArray[random.nextInt(n)]);
        }
        
        return sb.toString();
    }
    
    private static final Pattern LOWERCASE_LETTERS =
            Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_LETTERS =
            Pattern.compile("[A-Z]");
    private static final Pattern NUMBERS =
            Pattern.compile("\\d");
    private static final Pattern SPECIAL_SYMBOLS =
            Pattern.compile("[^A-Za-z0-9]");
}
