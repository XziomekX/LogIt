/*
 * IniUtils.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IniUtils
{
    private IniUtils()
    {
    }
    
    public static Map<String, Map<String, String>> unserialize(InputStream in) throws IOException
    {
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
    
    public static Map<String, Map<String, String>> unserialize(String in) throws IOException
    {
        return unserialize(new ByteArrayInputStream(in.getBytes()));
    }
    
    public static void serialize(Map<String, Map<String, String>> in, OutputStream out)
            throws IOException
    {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out)))
        {
            for (Entry<String, Map<String, String>> section : in.entrySet())
            {
                bw.write("[");
                bw.write(section.getKey());
                bw.write("]");
                bw.newLine();
                
                for (Entry<String, String> kv : section.getValue().entrySet())
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
    
    public static String serialize(Map<String, Map<String, String>> in) throws IOException
    {
        OutputStream outputStream = new ByteArrayOutputStream();
        
        serialize(in, outputStream);
        
        return outputStream.toString();
    }
    
    private static final Pattern SECTION_PATTERN = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("\\s*([^=]*)=(.*)");
}
