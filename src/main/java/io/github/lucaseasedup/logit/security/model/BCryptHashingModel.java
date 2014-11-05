package io.github.lucaseasedup.logit.security.model;

import io.github.lucaseasedup.logit.security.lib.BCrypt;

public final class BCryptHashingModel implements HashingModel
{
    @Override
    public String getHash(String string)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getHash(String string, String salt)
    {
        return BCrypt.hashpw(string, salt);
    }
    
    @Override
    public boolean verify(String string, String hash)
    {
        // Even though this method could be marked as unsupported
        // because it lacks the salt parameter, the latter will be
        // extracted by BCrypt from the hash and the verification can be done.
        
        try
        {
            return BCrypt.checkpw(string, hash);
        }
        catch (IllegalArgumentException | StringIndexOutOfBoundsException ex)
        {
            // BCrypt throws these exceptions if there is something
            // wrong with the hash. We can't do anything about it,
            // so we'll assume the verification failed.
            
            return false;
        }
    }
    
    @Override
    public boolean verify(String string, String salt, String hash)
    {
        return verify(string, hash);
    }
    
    @Override
    public String generateSalt()
    {
        return BCrypt.gensalt(12);
    }
    
    @Override
    public String encode()
    {
        return "bcrypt";
    }
}
