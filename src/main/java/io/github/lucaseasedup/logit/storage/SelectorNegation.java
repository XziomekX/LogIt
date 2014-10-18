package io.github.lucaseasedup.logit.storage;

public final class SelectorNegation extends Selector
{
    public SelectorNegation(SelectorCondition operand)
    {
        if (operand == null)
            throw new IllegalArgumentException();
        
        this.operand = operand;
    }
    
    public SelectorCondition getOperand()
    {
        return operand;
    }
    
    private final SelectorCondition operand;
}
