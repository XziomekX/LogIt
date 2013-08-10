/*
 * ImmutableIniFile.java
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

import java.io.File;
import java.io.IOException;

/**
 * @author LucasEasedUp
 */
public class ImmutableIniFile extends IniFile
{
    public ImmutableIniFile(File f) throws IOException
    {
        super(f);
    }
    
    public ImmutableIniFile(String s)
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
    public void removeSection(String section)
    {
        throw new RuntimeException("Immutable data structure.");
    }
    
    @Override
    public void removeSectionKey(String section, String key)
    {
        throw new RuntimeException("Immutable data structure.");
    }
    
    @Override
    public void putString(String section, String key, String value)
    {
        throw new RuntimeException("Immutable data structure.");
    }
}
