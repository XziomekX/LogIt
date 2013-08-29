package io.github.lucaseasedup.logit.account;

public final class AccountPropertyUpdateEvent extends AccountEvent
{
    public AccountPropertyUpdateEvent(Account account, String property, String value)
    {
        this.account = account;
        this.property = property;
        this.value = value;
    }
    
    public Account getAccount()
    {
        return account;
    }
    
    /**
     * Equal to <code>getAccount().get("logit.accounts.username")</code>.
     * 
     * @return Username.
     */
    public String getUsername()
    {
        return account.getString("logit.accounts.username");
    }
    
    public String getProperty()
    {
        return property;
    }
    
    public String getValue()
    {
        return value;
    }
    
    private final Account account;
    private final String property;
    private final String value;
}
