/*
 * Pinger.java
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
package io.github.lucaseasedup.logit.db;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Pinger implements Runnable
{
    public Pinger(Database database)
    {
        this.database = database;
    }
    
    @Override
    public void run()
    {
        try
        {
            database.ping();
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Pinger.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    private final Database database;
}
