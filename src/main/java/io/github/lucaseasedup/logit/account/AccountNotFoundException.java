package io.github.lucaseasedup.logit.account;

public final class AccountNotFoundException extends RuntimeException
{
    public AccountNotFoundException(String username)
    {
        super(username);
    }
    
    private static final long serialVersionUID = 1L;
}
