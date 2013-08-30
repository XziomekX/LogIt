/*
 * IniFile.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
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
package io.github.lucaseasedup.logit;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniFile
{
    public IniFile()
    {
    }
    
    public IniFile(File file) throws IOException
    {
        if (file == null)
            throw new NullPointerException();
        
        try (InputStream is = new FileInputStream(file))
        {
            loadFromStream(is);
        }
    }
    
    public IniFile(String string)
    {
        if (string == null)
            throw new NullPointerException();
        
        loadFromString(string);
    }
    
    public IniFile(IniFile ini)
    {
        if (ini == null)
            throw new NullPointerException();
        
        Map<String, Map<String, String>> entries = new HashMap<>();
        
        for (String section : ini.getSections())
        {
            Map<String, String> keys = new HashMap<>();
            
            for (String key : ini.getSectionKeys(section))
            {
                keys.put(key, ini.getString(section, key));
            }
            
            entries.put(section, keys);
        }
    }
    
    public Set<String> getSections()
    {
        return ImmutableSet.copyOf(entries.keySet());
    }
    
    public boolean hasSection(String section)
    {
        return entries.containsKey(section);
    }
    
    public void putSection(String section)
    {
        entries.put(section, new LinkedHashMap<String, String>());
    }
    
    public void removeSection(String section)
    {
        entries.remove(section);
    }
    
    public void removeSectionKey(String section, String key)
    {
        Map<String, String> keys = entries.get(section);
        
        if (keys != null)
        {
            keys.remove(key);
        }
    }
    
    public void putString(String section, String key, String value)
    {
        entries.get(section).put(key, value);
    }
    
    public Set<String> getSectionKeys(String section)
    {
        Map<String, String> keys = entries.get(section);
        
        if (keys == null)
            return null;
        
        return ImmutableSet.copyOf(keys.keySet());
    }
    
    public String getString(String section, String key, String defaultValue)
    {
        Map<String, String> keys = entries.get(section);
        
        if (keys == null)
            return defaultValue;
        
        String value = keys.get(key);
        
        if (value == null)
            return defaultValue;
        
        return value;
    }
    
    public String getString(String section, String key)
    {
        return getString(section, key, null);
    }
    
    public int getInt(String section, String key, int defaultValue)
    {
        String value = getString(section, key, String.valueOf(defaultValue));
        
        return Integer.parseInt(value);
    }
    
    public float getFloat(String section, String key, float defaultValue)
    {
        String value = getString(section, key, String.valueOf(defaultValue));
        
        return Float.parseFloat(value);
    }
    
    public double getDouble(String section, String key, double defaultValue)
    {
        String value = getString(section, key, String.valueOf(defaultValue));
        
        return Double.parseDouble(value);
    }
    
    public boolean getBoolean(String section, String key, boolean defaultValue)
    {
        String value = getString(section, key, String.valueOf(defaultValue));
        
        return Boolean.parseBoolean(value);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(); 
        
        for (Entry<String, Map<String, String>> section : entries.entrySet())
        {
            if (sb.length() != 0)
            {
                sb.append("\n");
            }
            
            sb.append("[");
            sb.append(section.getKey());
            sb.append("]\n");
            
            for (Entry<String, String> kv : section.getValue().entrySet())
            {
                if (kv.getValue() != null)
                {
                    sb.append(kv.getKey());
                    sb.append("=");
                    sb.append(kv.getValue());
                    sb.append("\n");
                }
            }
        }
        
        return sb.toString();
    }
    
    public void save(OutputStream os) throws IOException
    {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os)))
        {
            for (Entry<String, Map<String, String>> section : entries.entrySet())
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
    
    private void loadFromStream(InputStream is) throws IOException
    {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is)))
        {
            String line;
            String section = null;
            
            while ((line = br.readLine()) != null)
            {
                Matcher matcher = sectionPattern.matcher(line);
                
                if (matcher.matches())
                {
                    section = matcher.group(1).trim();
                    
                    entries.put(section, new LinkedHashMap<String, String>());
                }
                else if (section != null)
                {
                    matcher = keyValuePattern.matcher(line);
                    
                    if (matcher.matches())
                    {
                        String key = matcher.group(1).trim();
                        String value = matcher.group(2).trim();
                        
                        entries.get(section).put(key, value);
                    }
                }
            }
        }
    }
    
    private void loadFromString(String s)
    {
        String line;
        String section = null;
        
        try (Scanner scanner = new Scanner(s))
        {
            while (scanner.hasNextLine())
            {
                line = scanner.nextLine();
                
                Matcher matcher = sectionPattern.matcher(line);
                
                if (matcher.matches())
                {
                    section = matcher.group(1).trim();
                    
                    entries.put(section, new LinkedHashMap<String, String>());
                }
                else if (section != null)
                {
                    matcher = keyValuePattern.matcher(line);
                    
                    if (matcher.matches())
                    {
                        String key = matcher.group(1).trim();
                        String value = matcher.group(2).trim();
                        
                        entries.get(section).put(key, value);
                    }
                }
            }
        }
    }
    
    private final Pattern sectionPattern = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private final Pattern keyValuePattern = Pattern.compile("\\s*([^=]*)=(.*)");
    protected Map<String, Map<String, String>> entries = new LinkedHashMap<>();
}
