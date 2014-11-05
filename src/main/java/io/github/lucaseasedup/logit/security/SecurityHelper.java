package io.github.lucaseasedup.logit.security;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.security.model.HashingModel;
import io.github.lucaseasedup.logit.security.model.HashingModelDecoder;
import java.util.Random;
import java.util.regex.Pattern;

public final class SecurityHelper extends LogItCoreObject
{
    /**
     * Returns the default hashing model specified in the config file.
     * 
     * @return the default hashing model.
     */
    public HashingModel getDefaultHashingModel()
    {
        return HashingModelDecoder.decode(
                getConfig("config.yml").getString("passwords.hashingAlgorithm")
        );
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
    public String generatePassword(int length, String combination)
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
