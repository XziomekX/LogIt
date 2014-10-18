package io.github.lucaseasedup.logit.storage;

public final class Infix
{
    private Infix()
    {
    }
    
    public static final SelectorBinary.Relation AND =
            SelectorBinary.Relation.AND;
    public static final SelectorBinary.Relation OR =
            SelectorBinary.Relation.OR;
    
    public static final SelectorCondition.Relation EQUALS =
            SelectorCondition.Relation.EQUALS;
    public static final SelectorCondition.Relation LESS_THAN =
            SelectorCondition.Relation.LESS_THAN;
    public static final SelectorCondition.Relation GREATER_THAN =
            SelectorCondition.Relation.GREATER_THAN;
    public static final SelectorCondition.Relation STARTS_WITH =
            SelectorCondition.Relation.STARTS_WITH;
    public static final SelectorCondition.Relation ENDS_WITH =
            SelectorCondition.Relation.ENDS_WITH;
    public static final SelectorCondition.Relation CONTAINS =
            SelectorCondition.Relation.CONTAINS;
}
