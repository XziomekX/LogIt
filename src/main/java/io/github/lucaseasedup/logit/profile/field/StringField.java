package io.github.lucaseasedup.logit.profile.field;

public final class StringField extends Field
{
    public StringField(String name, int minLength, int maxLength)
    {
        super(name);
        
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
    
    public int getMinLength()
    {
        return minLength;
    }
    
    public int getMaxLength()
    {
        return maxLength;
    }
    
    private final int minLength;
    private final int maxLength;
}
