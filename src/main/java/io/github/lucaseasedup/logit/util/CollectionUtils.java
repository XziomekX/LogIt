package io.github.lucaseasedup.logit.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public final class CollectionUtils
{
    private CollectionUtils()
    {
    }
    
    public static boolean containsIgnoreCase(String needle, Collection<String> collection)
    {
        for (String s : collection)
        {
            if (s.equalsIgnoreCase(needle))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adds an element to a collection only if the latter does not contain it
     * and the element is not null.
     * 
     * @param collection the collection to be populated with the given element.
     * @param element the element to be added to the given collection.
     */
    public static <T> void addNonnullIfMissing(Collection<T> collection, T element)
    {
        if (collection == null)
            throw new IllegalArgumentException();
        
        if (element != null && !collection.contains(element))
        {
            collection.add(element);
        }
    }
    
    public static boolean collectionsMatch(Collection<?> coll1, Collection<?> coll2)
    {
        if (coll1 == null && coll2 == null)
        {
            return true;
        }
        else if (coll1 != null && coll2 != null)
        {
            return new HashSet<>(coll1).equals(new HashSet<>(coll2));
        }
        else
        {
            return false;
        }
    }
    
    public static boolean isSubset(Collection<?> subset, Collection<?> of)
    {
        for (Object obj : subset)
        {
            if (!of.contains(obj))
            {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean containsDuplicates(Collection<?> coll)
    {
        if (coll == null)
            throw new IllegalArgumentException();
        
        Set<Object> set = new HashSet<>(coll);
        
        return set.size() != coll.size();
    }
    
    public static String toString(Enumeration<?> enumeration)
    {
        StringBuilder sb = new StringBuilder();
        
        while (enumeration.hasMoreElements())
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            
            sb.append(enumeration.nextElement());
        }
        
        return sb.toString();
    }
}
