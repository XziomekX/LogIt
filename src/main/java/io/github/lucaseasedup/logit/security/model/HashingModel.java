package io.github.lucaseasedup.logit.security.model;

public abstract class HashingModel
{
    public abstract String getHash(String string);
    public abstract String getHash(String string, String salt);
    public abstract boolean verify(String string, String hash);
    public abstract boolean verify(String string, String salt, String hash);
    public abstract String generateSalt();
    public abstract String encode();
}
