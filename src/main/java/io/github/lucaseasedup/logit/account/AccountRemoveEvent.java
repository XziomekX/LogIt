package io.github.lucaseasedup.logit.account;

public final class AccountRemoveEvent extends AccountEvent
{
    /* package */ AccountRemoveEvent(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        this.username = username;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    private final String username;
}
