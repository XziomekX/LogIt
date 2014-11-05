package io.github.lucaseasedup.logit.security.model;

import io.github.lucaseasedup.logit.security.AuthMePasswordHelper;

public final class AuthMeHashingModel implements HashingModel
{
    public AuthMeHashingModel(String encryptionMethod)
    {
        if (encryptionMethod == null)
            throw new IllegalArgumentException();
        
        this.encryptionMethod = encryptionMethod;
    }
    
    @Override
    public String getHash(String string)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getHash(String string, String salt)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean verify(String string, String hash)
    {
        return AuthMePasswordHelper.comparePasswordWithHash(
                string, hash, encryptionMethod
        );
    }
    
    @Override
    public boolean verify(String string, String salt, String hash)
    {
        return verify(string, hash);
    }
    
    @Override
    public String generateSalt()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String encode()
    {
        return "authme:" + encryptionMethod;
    }
    
    public String getEncryptionMethod()
    {
        return encryptionMethod;
    }
    
    private final String encryptionMethod;
}
