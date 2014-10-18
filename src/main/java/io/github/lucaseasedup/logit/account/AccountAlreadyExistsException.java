package io.github.lucaseasedup.logit.account;

public final class AccountAlreadyExistsException extends RuntimeException
{
    public AccountAlreadyExistsException(String username)
    {
        super(username);
    }
    
    private static final long serialVersionUID = 1L;
}
