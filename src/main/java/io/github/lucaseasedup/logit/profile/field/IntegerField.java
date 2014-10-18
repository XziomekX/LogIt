package io.github.lucaseasedup.logit.profile.field;

public final class IntegerField extends Field
{
    public IntegerField(String name, int minValue, int maxValue)
    {
        super(name);
        
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public int getMinValue()
    {
        return minValue;
    }
    
    public int getMaxValue()
    {
        return maxValue;
    }
    
    private final int minValue;
    private final int maxValue;
}
