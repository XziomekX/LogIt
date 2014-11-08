package io.github.lucaseasedup.logit.storage;

public final class SelectorBinary extends Selector
{
    public SelectorBinary(
            Selector leftOperand,
            SelectorBinary.Relation relation,
            Selector rightOperand
    )
    {
        if (leftOperand == null || relation == null || rightOperand == null)
            throw new IllegalArgumentException();
        
        this.leftOperand = leftOperand;
        this.relation = relation;
        this.rightOperand = rightOperand;
    }
    
    public Selector getLeftOperand()
    {
        return leftOperand;
    }
    
    public SelectorBinary.Relation getRelation()
    {
        return relation;
    }
    
    public Selector getRightOperand()
    {
        return rightOperand;
    }
    
    public enum Relation
    {
        AND, OR;
    }
    
    private final Selector leftOperand;
    private final SelectorBinary.Relation relation;
    private final Selector rightOperand;
}
