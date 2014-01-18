/*
 * CsvDatabaseTest.java
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
import static org.junit.Assert.assertTrue;
import io.github.lucaseasedup.logit.db.CsvDatabase;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CsvDatabaseTest
{
    @Test
    public void test() throws SQLException
    {
        try (CsvDatabase db = new CsvDatabase(folder.getRoot()))
        {
            db.connect();
            
            db.createTable("test.csv", new String[]{
                "username", "TEXT",
                "ip", "TEXT",
                "notes", "TEXT"
            });
            
            assertTrue(new File(folder.getRoot(), "test.csv").exists());
            assertTrue(db.getColumnNames("test.csv").contains("notes"));
            
            db.insert("test.csv", new String[]{"ip", "username", "notes"}, new String[]{
                "127.0.01",
                "geong",
                "No notes."
            });
            db.insert("test.csv", new String[]{
                "geong2",
                "153.451.2.2",
                "blah blah",
            });
            db.insert("test.csv", new String[]{
                "gongo",
                "253.451.2.3",
                "bla \r\nbla",
            });
            
            db.addColumn("test.csv", "last_active", "TEXT");
            
            assertTrue(db.getColumnNames("test.csv").contains("last_active"));
            
            db.delete("test.csv", new String[]{
                "username", "=", "gongo"
            });
            
            db.update("test.csv", new String[]{
                "username", "=", "geong"
            }, new String[]{
                "notes", "Some notes."
            });
            
            List<Map<String, String>> rs =
                    db.select("test.csv", new String[]{"username", "ip", "notes"});
            
            for (Map<String, String> row : rs)
            {
                assertFalse(row.get("username").equals("gongo"));
                
                if ("geong".equals(row.get("username")))
                {
                    assertTrue(row.get("notes").equals("Some notes."));
                }
            }
        }
    }
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
}
