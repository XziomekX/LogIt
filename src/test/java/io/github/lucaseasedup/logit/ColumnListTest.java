/*
 * ColumnListTest.java
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
package io.github.lucaseasedup.logit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.github.lucaseasedup.logit.db.ColumnList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ColumnListTest
{
    @Test
    public void test()
    {
        ColumnList columnList = new ColumnList();
        
        assertNotNull(columnList);
        
        columnList.add("Column");
        
        assertTrue(columnList.contains("column"));
        assertFalse(columnList.contains("Column "));
        
        List<String> list = Arrays.asList(new String[]{"Column", "column", "col"});
        columnList.addAll(list);
        
        assertTrue(columnList.size() == 2);
        
        columnList.clear();
        columnList.add("SampleColumn");
        columnList.addAll(0, list);
        
        assertTrue(columnList.size() == 3);
        assertTrue(columnList.indexOf("column") == 0);
    }
}
