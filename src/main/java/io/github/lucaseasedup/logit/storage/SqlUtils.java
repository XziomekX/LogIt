package io.github.lucaseasedup.logit.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class SqlUtils
{
    private SqlUtils()
    {
    }
    
    public static String escapeQuotes(
            String string, String quote, boolean escapeBackslashes
    )
    {
        if (string == null || quote == null)
            throw new IllegalArgumentException();
        
        string = string.replace(quote, quote + quote);
        
        if (escapeBackslashes)
        {
            string = string.replace("\\", "\\\\");
        }
        
        return string;
    }
    
    public static boolean resolveSelector(Selector selector, StorageEntry entry)
    {
        if (selector == null || entry == null)
            throw new IllegalArgumentException();
        
        if (selector instanceof SelectorConstant)
        {
            return ((SelectorConstant) selector).getValue();
        }
        else if (selector instanceof SelectorNegation)
        {
            return !resolveSelector(
                    ((SelectorNegation) selector).getOperand(),
                    entry
            );
        }
        else if (selector instanceof SelectorBinary)
        {
            SelectorBinary selectorBinary = (SelectorBinary) selector;
            
            switch (selectorBinary.getRelation())
            {
            case AND:
                return resolveSelector(selectorBinary.getLeftOperand(), entry)
                        && resolveSelector(selectorBinary.getRightOperand(), entry);
            
            case OR:
                return resolveSelector(selectorBinary.getLeftOperand(), entry)
                        || resolveSelector(selectorBinary.getRightOperand(), entry);
            
            default:
                throw new IllegalArgumentException(
                        "Unsupported relation: " + selectorBinary.getRelation()
                );
            }
        }
        else if (selector instanceof SelectorCondition)
        {
            SelectorCondition selectorCondition = (SelectorCondition) selector;
            String key = selectorCondition.getKey();
            String operandValue = selectorCondition.getValue();
            String actualValue = entry.get(key);
            
            switch (selectorCondition.getRelation())
            {
            case EQUALS:
                if (actualValue == null)
                {
                    return operandValue == null;
                }
                else
                {
                    return actualValue.equals(operandValue);
                }
                
            case LESS_THAN:
                try
                {
                    long actualLong = Long.parseLong(actualValue);
                    long operandLong = Long.parseLong(operandValue);

                    return actualLong < operandLong;
                }
                catch (NumberFormatException ex)
                {
                    return false;
                }
                
            case GREATER_THAN:
                try
                {
                    long actualLong = Long.parseLong(actualValue);
                    long operandLong = Long.parseLong(operandValue);

                    return actualLong > operandLong;
                }
                catch (NumberFormatException ex)
                {
                    return false;
                }
                
            case STARTS_WITH:
                return actualValue.startsWith(operandValue);
            
            case ENDS_WITH:
                return actualValue.endsWith(operandValue);
            
            case CONTAINS:
                return actualValue.contains(operandValue);
                
            default:
                throw new IllegalArgumentException(
                        "Unsupported relation: "
                                + selectorCondition.getRelation()
                );
            }
        }
        else
        {
            throw new IllegalArgumentException(
                    "Unsupported selector: " + selector.getClass().getName()
            );
        }
    }
    
    public static List<StorageEntry> copyResultSet(ResultSet rs)
            throws SQLException
    {
        if (rs == null)
            throw new IllegalArgumentException();
        
        List<StorageEntry> entries = new LinkedList<>();
        
        if (rs.isBeforeFirst())
        {
            while (rs.next())
            {
                StorageEntry.Builder entryBuilder = new StorageEntry.Builder();
                
                for (int i = 1, n = rs.getMetaData().getColumnCount(); i <= n; i++)
                {
                    entryBuilder.put(
                            rs.getMetaData().getColumnLabel(i), rs.getString(i)
                    );
                }
                
                entries.add(entryBuilder.build());
            }
            
            rs.close();
        }
        
        return entries;
    }
    
    public static String translateSelector(
            Selector selector, String columnQuote, String valueQuote
    )
    {
        if (selector == null || columnQuote == null || valueQuote == null)
            throw new IllegalArgumentException();
        
        if (selector instanceof SelectorConstant)
        {
            SelectorConstant selectorConstant = (SelectorConstant) selector;
            
            return (selectorConstant.getValue()) ? "1 = 1" : "1 = 0";
        }
        else if (selector instanceof SelectorNegation)
        {
            SelectorNegation selectorNegation = (SelectorNegation) selector;
            String translatedSelector = translateSelector(
                    selectorNegation.getOperand(), columnQuote, valueQuote
            );
            
            return "NOT (" + translatedSelector + ")";
        }
        else if (selector instanceof SelectorBinary)
        {
            SelectorBinary selectorBinary = (SelectorBinary) selector;
            StringBuilder sb = new StringBuilder();
            
            sb.append("(");
            sb.append(translateSelector(
                    selectorBinary.getLeftOperand(), columnQuote, valueQuote
            ));
            sb.append(") ");
            
            switch (selectorBinary.getRelation())
            {
            case AND:
                sb.append("AND");
                break;
            
            case OR:
                sb.append("OR");
                break;
            
            default:
                throw new IllegalArgumentException(
                        "Unsupported relation: " + selectorBinary.getRelation()
                );
            }
            
            sb.append(" (");
            sb.append(translateSelector(
                    selectorBinary.getRightOperand(), columnQuote, valueQuote
            ));
            sb.append(")");
            
            return sb.toString();
        }
        else if (selector instanceof SelectorCondition)
        {
            SelectorCondition selectorCondition = (SelectorCondition) selector;
            StringBuilder sb = new StringBuilder();
            
            sb.append("(");
            sb.append(columnQuote);
            sb.append(escapeQuotes(
                    selectorCondition.getKey(), columnQuote, true
            ));
            sb.append(columnQuote);
            sb.append(") ");
            
            switch (selectorCondition.getRelation())
            {
            case EQUALS:
                sb.append("=");
                break;
                
            case LESS_THAN:
                sb.append("<");
                break;
                
            case GREATER_THAN:
                sb.append(">");
                break;
                
            case STARTS_WITH:
            case ENDS_WITH:
            case CONTAINS:
                sb.append("LIKE");
                break;
                
            default:
                throw new IllegalArgumentException(
                        "Unsupported relation: "
                                + selectorCondition.getRelation()
                );
            }
            
            sb.append(" (");
            sb.append(valueQuote);
            
            if (selectorCondition.getRelation() == Infix.ENDS_WITH
                    || selectorCondition.getRelation() == Infix.CONTAINS)
            {
                sb.append("%");
            }
            
            sb.append(escapeQuotes(
                    selectorCondition.getValue(), valueQuote, true
            ));
            
            if (selectorCondition.getRelation() == Infix.STARTS_WITH
                    || selectorCondition.getRelation() == Infix.CONTAINS)
            {
                sb.append("%");
            }
            
            sb.append(valueQuote);
            sb.append(")");
            
            return sb.toString();
        }
        else
        {
            throw new IllegalArgumentException(
                    "Unsupported selector: " + selector.getClass().getName()
            );
        }
    }
    
    public static String encodeType(DataType type)
    {
        if (type == null)
            throw new IllegalArgumentException();
        
        switch (type)
        {
        case INTEGER:
            return "INTEGER";
        case REAL:
            return "REAL";
        
        case TINYTEXT:
            return "VARCHAR(255)";
        case MEDIUMTEXT:
            return "VARCHAR(1023)";
        case LONGTEXT:
            return "VARCHAR(10119)";
        case TEXT:
            return "TEXT";
            
        default:
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
    
    public static DataType decodeType(String type)
    {
        if (type == null)
            throw new IllegalArgumentException();
        
        type = type.toUpperCase();
        
        if (type.startsWith("INT")
                || type.startsWith("TINYINT")
                || type.startsWith("SMALLINT")
                || type.startsWith("MEDIUMINT")
                || type.startsWith("BIGINT"))
        {
            return DataType.INTEGER;
        }
        else if (type.startsWith("REAL")
                || type.startsWith("DOUBLE")
                || type.startsWith("FLOAT"))
        {
            return DataType.REAL;
        }
        else if (type.startsWith("VARCHAR")
                || type.startsWith("TEXT")
                || type.startsWith("CLOB")
                || type.startsWith("CHAR")
                || type.startsWith("VARYING CHARACTER")
                || type.startsWith("NVARCHAR")
                || type.startsWith("LONGVARCHAR")
                || type.startsWith("NCHAR")
                || type.startsWith("NTEXT")
                || type.startsWith("LONGTEXT")
                || type.startsWith("MEDIUMTEXT")
                || type.startsWith("NCLOB")
                || type.startsWith("TINYTEXT"))
        {
            return DataType.TEXT;
        }
        else
        {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
    
    public static String translateKeyList(List<String> keys, String columnQuote)
    {
        if (keys == null || columnQuote == null)
            throw new IllegalArgumentException();
        
        StringBuilder sb = new StringBuilder();
        
        for (String key : keys)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            
            sb.append(columnQuote);
            sb.append(escapeQuotes(key, columnQuote, true));
            sb.append(columnQuote);
        }
        
        return sb.toString();
    }
    
    public static String translateKeyTypeList(
            UnitKeys keys, String primaryKey, String columnQuote
    )
    {
        if (keys == null || columnQuote == null)
            throw new IllegalArgumentException();
        
        if (primaryKey != null && !keys.containsKey(primaryKey))
        {
            throw new IllegalArgumentException(
                    "Cannot create index on a non-existing key"
            );
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (Map.Entry<String, DataType> e : keys.entrySet())
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            
            sb.append(columnQuote);
            sb.append(escapeQuotes(e.getKey(), columnQuote, true));
            sb.append(columnQuote);
            sb.append(" ");
            sb.append(encodeType(e.getValue()));
            sb.append(" NOT NULL");
            
            if (primaryKey != null && primaryKey.equals(e.getKey()))
            {
                sb.append(" PRIMARY KEY");
            }
        }
        
        return sb.toString();
    }
    
    public static String translateEntryNames(
            StorageEntry entry, String columnQuote
    )
    {
        if (entry == null || columnQuote == null)
            throw new IllegalArgumentException();
        
        StringBuilder sb = new StringBuilder();
        
        for (StorageDatum datum : entry)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            
            sb.append(columnQuote);
            sb.append(escapeQuotes(datum.getKey(), columnQuote, true));
            sb.append(columnQuote);
        }
        
        return sb.toString();
    }
    
    public static String translateEntryValues(
            StorageEntry entry, String valueQuote
    )
    {
        if (entry == null || valueQuote == null)
            throw new IllegalArgumentException();
        
        StringBuilder sb = new StringBuilder();
        
        for (StorageDatum datum : entry)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            
            sb.append(valueQuote);
            sb.append(escapeQuotes(datum.getValue(), valueQuote, true));
            sb.append(valueQuote);
        }
        
        return sb.toString();
    }
    
    public static String translateEntrySubset(
            StorageEntry entrySubset, String columnQuote, String valueQuote
    )
    {
        if (entrySubset == null || columnQuote == null || valueQuote == null)
            throw new IllegalArgumentException();
        
        StringBuilder sb = new StringBuilder();
        
        for (StorageDatum datum : entrySubset)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            
            sb.append(columnQuote);
            sb.append(escapeQuotes(datum.getKey(), columnQuote, true));
            sb.append(columnQuote);
            sb.append(" = ");
            sb.append(valueQuote);
            sb.append(escapeQuotes(datum.getValue(), valueQuote, true));
            sb.append(valueQuote);
        }
        
        return sb.toString();
    }
}
