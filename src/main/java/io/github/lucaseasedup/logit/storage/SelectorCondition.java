package io.github.lucaseasedup.logit.storage;

public final class SelectorCondition extends Selector
{
    public SelectorCondition(String key, Relation relation, String value)
    {
        if (key == null || relation == null)
            throw new IllegalArgumentException();
        
        this.key = key;
        this.relation = relation;
        this.value = value;
    }
    
    public String getKey()
    {
        return key;
    }
    
    public Relation getRelation()
    {
        return relation;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public enum Relation
    {
        EQUALS, LESS_THAN, GREATER_THAN, STARTS_WITH, ENDS_WITH, CONTAINS;
    }
    
    private final String key;
    private final Relation relation;
    private final String value;
}
