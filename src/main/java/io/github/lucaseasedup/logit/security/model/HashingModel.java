package io.github.lucaseasedup.logit.security.model;

public interface HashingModel
{
    public String getHash(String string);
    public String getHash(String string, String salt);
    public boolean verify(String string, String hash);
    public boolean verify(String string, String salt, String hash);
    public String generateSalt();
    public String encode();
}
