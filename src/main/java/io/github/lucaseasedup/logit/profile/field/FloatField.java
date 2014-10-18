package io.github.lucaseasedup.logit.profile.field;

public final class FloatField extends Field
{
    public FloatField(String name, double minValue, double maxValue)
    {
        super(name);
        
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public double getMinValue()
    {
        return minValue;
    }
    
    public double getMaxValue()
    {
        return maxValue;
    }
    
    private final double minValue;
    private final double maxValue;
}
