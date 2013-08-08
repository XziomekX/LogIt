package io.github.lucaseasedup.logit.account;

public class AccountPropertyUpdateEvent extends AccountEvent
{
    public AccountPropertyUpdateEvent(Account account, String property, String value)
    {
        super(account);
        
        this.property = property;
        this.value = value;
    }
    
    public String getProperty()
    {
        return property;
    }
    
    public String getValue()
    {
        return value;
    }
    
    private final String property;
    private final String value;
}
