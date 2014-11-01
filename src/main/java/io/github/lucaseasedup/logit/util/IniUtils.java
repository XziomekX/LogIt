package io.github.lucaseasedup.logit.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IniUtils
{
    private IniUtils()
    {
    }
    
    public static Map<String, Map<String, String>> unserialize(InputStream in)
            throws IOException
    {
        if (in == null)
            throw new IllegalArgumentException();
        
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in)))
        {
            String line;
            String section = null;
            
            while ((line = br.readLine()) != null)
            {
                Matcher matcher = SECTION_PATTERN.matcher(line);
                
                if (matcher.matches())
                {
                    section = matcher.group(1).trim();
                    
                    result.put(section, new LinkedHashMap<String, String>());
                }
                else if (section != null)
                {
                    matcher = KEY_VALUE_PATTERN.matcher(line);
                    
                    if (matcher.matches())
                    {
                        String key = matcher.group(1).trim();
                        String value = matcher.group(2).trim();
                        
                        result.get(section).put(key, value);
                    }
                }
            }
        }
        
        return result;
    }
    
    public static Map<String, Map<String, String>> unserialize(String in)
            throws IOException
    {
        return unserialize(new ByteArrayInputStream(in.getBytes()));
    }
    
    public static void serialize(Map<String, Map<String, String>> in,
                                 OutputStream out)
            throws IOException
    {
        if (in == null || out == null)
            throw new IllegalArgumentException();
        
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out)))
        {
            for (Map.Entry<String, Map<String, String>> section : in.entrySet())
            {
                bw.write("[");
                bw.write(section.getKey());
                bw.write("]");
                bw.newLine();
                
                for (Map.Entry<String, String> kv : section.getValue().entrySet())
                {
                    if (kv.getValue() != null)
                    {
                        bw.write(kv.getKey());
                        bw.write("=");
                        bw.write(kv.getValue());
                        bw.newLine();
                    }
                }
                
                bw.newLine();
            }
        }
    }
    
    public static String serialize(Map<String, Map<String, String>> in)
            throws IOException
    {
        if (in == null)
            throw new IllegalArgumentException();
        
        OutputStream outputStream = new ByteArrayOutputStream();
        
        serialize(in, outputStream);
        
        return outputStream.toString();
    }
    
    private static final Pattern SECTION_PATTERN = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("\\s*([^=]*)=(.*)");
}
