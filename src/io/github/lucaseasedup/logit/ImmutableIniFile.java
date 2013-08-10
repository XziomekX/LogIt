package io.github.lucaseasedup.logit;

import java.io.File;
import java.io.IOException;

public class ImmutableIniFile extends IniFile
{
    public ImmutableIniFile(File f) throws IOException
    {
        super(f);
    }
    
    public ImmutableIniFile(String s) throws IOException
    {
        super(s);
    }
    
    public ImmutableIniFile(IniFile ini)
    {
        super(ini);
    }
    
    @Override
    public void putSection(String section)
    {
        throw new RuntimeException("Immutable data structure.");
    }
    
    @Override
    public void putString(String section, String key, String value)
    {
        throw new RuntimeException("Immutable data structure.");
    }
}
