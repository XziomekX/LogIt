package io.github.lucaseasedup.logit.storage;

import io.github.lucaseasedup.logit.storage.Storage.DataType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class SqlUtils
{
    private SqlUtils()
    {
    }
    
    public static String escapeQuotes(String string, String quote, boolean escapeBackslashes)
    {
        string = string.replace(quote, quote + quote);
        
        if (escapeBackslashes)
        {
            string = string.replace("\\", "\\\\");
        }
        
        return string;
    }
    
    public static boolean resolveSelector(Selector selector, Storage.Entry entry)
    {
        if (selector instanceof SelectorConstant)
        {
            return ((SelectorConstant) selector).getValue();
        }
        else if (selector instanceof SelectorNegation)
        {
            return !resolveSelector(((SelectorNegation) selector).getOperand(), entry);
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
                throw new RuntimeException("Unsupported relation: "
                                         + selectorBinary.getRelation());
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
                    return operandValue == null;
                else
                    return actualValue.equals(operandValue);
                
            case LESS_THAN:
                try
                {
                    return Long.parseLong(actualValue) < Long.parseLong(operandValue);
                }
                catch (NumberFormatException ex)
                {
                    return false;
                }
                
            case GREATER_THAN:
                try
                {
                    return Long.parseLong(actualValue) > Long.parseLong(operandValue);
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
                throw new RuntimeException("Unsupported relation: "
                                           + selectorCondition.getRelation());
            }
        }
        else
        {
            throw new RuntimeException("Unsupported selector: " + selector.getClass().getName());
        }
    }
    
    public static List<Storage.Entry> copyResultSet(ResultSet rs) throws SQLException
    {
        List<Storage.Entry> entries = new LinkedList<>();
        
        if (rs != null && rs.isBeforeFirst())
        {
            while (rs.next())
            {
                Storage.Entry.Builder entryBuilder = new Storage.Entry.Builder();
                
                for (int i = 1, n = rs.getMetaData().getColumnCount(); i <= n; i++)
                {
                    entryBuilder.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
                }
                
                entries.add(entryBuilder.build());
            }
            
            rs.close();
        }
        
        return entries;
    }
    
    public static String translateSelector(Selector selector,
                                           String columnQuote,
                                           String valueQuote)
    {
        if (selector == null)
            throw new IllegalArgumentException();
        
        if (selector instanceof SelectorConstant)
        {
            SelectorConstant selectorConstant = (SelectorConstant) selector;
            
            return (selectorConstant.getValue()) ? "1 = 1" : "1 = 0";
        }
        else if (selector instanceof SelectorNegation)
        {
            SelectorNegation selectorNegation = (SelectorNegation) selector;
            
            return "NOT ("
                 + translateSelector(selectorNegation.getOperand(), columnQuote, valueQuote)
                 + ")";
        }
        else if (selector instanceof SelectorBinary)
        {
            SelectorBinary selectorBinary = (SelectorBinary) selector;
            StringBuilder sb = new StringBuilder();
            
            sb.append("(");
            sb.append(translateSelector(selectorBinary.getLeftOperand(), columnQuote, valueQuote));
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
                throw new RuntimeException("Unsupported relation: "
                                         + selectorBinary.getRelation());
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
            sb.append(escapeQuotes(selectorCondition.getKey(), columnQuote, true));
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
                throw new RuntimeException("Unsupported relation: "
                                           + selectorCondition.getRelation());
            }
            
            sb.append(" (");
            sb.append(valueQuote);
            
            if (selectorCondition.getRelation().equals(Infix.ENDS_WITH)
                    || selectorCondition.getRelation().equals(Infix.CONTAINS))
            {
                sb.append("%");
            }
            
            sb.append(escapeQuotes(selectorCondition.getValue(), valueQuote, true));
            
            if (selectorCondition.getRelation().equals(Infix.STARTS_WITH)
                    || selectorCondition.getRelation().equals(Infix.CONTAINS))
            {
                sb.append("%");
            }
            
            sb.append(valueQuote);
            sb.append(")");
            
            return sb.toString();
        }
        else
        {
            throw new RuntimeException("Unsupported selector: " + selector.getClass().getName());
        }
    }
    
    public static String encodeType(DataType type)
    {
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
            throw new RuntimeException("Unknown type: " + type);
        }
    }
    
    public static DataType decodeType(String type)
    {
        type = type.toUpperCase();
        
        if (type.startsWith("INT") || type.startsWith("TINYINT") || type.startsWith("SMALLINT")
                || type.startsWith("MEDIUMINT") || type.startsWith("BIGINT"))
        {
            return DataType.INTEGER;
        }
        else if (type.startsWith("REAL") || type.startsWith("DOUBLE") || type.startsWith("FLOAT"))
        {
            return DataType.REAL;
        }
        else if (type.startsWith("VARCHAR") || type.startsWith("TEXT")
                || type.startsWith("CLOB") || type.startsWith("CHAR")
                || type.startsWith("VARYING CHARACTER")  || type.startsWith("NVARCHAR")
                || type.startsWith("LONGVARCHAR") || type.startsWith("NCHAR")
                || type.startsWith("NTEXT") || type.startsWith("LONGTEXT")
                || type.startsWith("MEDIUMTEXT") || type.startsWith("NCLOB")
                || type.startsWith("TINYTEXT"))
        {
            return DataType.TEXT;
        }
        else
        {
            throw new RuntimeException("Unknown type: " + type);
        }
    }
    
    public static String translateKeyList(List<String> keys, String columnQuote)
    {
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
    
    public static String translateKeyTypeList(Hashtable<String, DataType> keys,
                                              String primaryKey,
                                              String columnQuote)
    {
        if (primaryKey != null && !keys.containsKey(primaryKey))
            throw new IllegalArgumentException("Cannot create index on a non-existing key");
        
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
    
    public static String translateEntryNames(Storage.Entry entry, String columnQuote)
    {
        StringBuilder sb = new StringBuilder();
        
        for (Storage.Entry.Datum datum : entry)
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
    
    public static String translateEntryValues(Storage.Entry entry, String valueQuote)
    {
        StringBuilder sb = new StringBuilder();
        
        for (Storage.Entry.Datum datum : entry)
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
    
    public static String translateEntrySubset(Storage.Entry entrySubset,
                                              String columnQuote,
                                              String valueQuote)
    {
        StringBuilder sb = new StringBuilder();
        
        for (Storage.Entry.Datum datum : entrySubset)
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
