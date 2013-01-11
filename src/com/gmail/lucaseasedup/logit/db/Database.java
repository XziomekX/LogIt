/*
 * Database.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author LucasEasedUp
 */
public interface Database
{
    public void connect(String host, String user, String password, String database) throws SQLException;
    public boolean executeStatement(String sql) throws SQLException;
    public ResultSet executeQuery(String sql) throws SQLException;
    public boolean create(String table, String... columns) throws SQLException;
    public ResultSet select(String table, String... columns) throws SQLException;
    public boolean insert(String table, String... values) throws SQLException;
    public boolean insert(String table, String[] columns, String... values) throws SQLException;
    public boolean update(String table, String[] where, String... set) throws SQLException;
    public boolean delete(String table, String[] where) throws SQLException;
    public boolean truncate(String table) throws SQLException;
    public boolean isConnected();
    public void close() throws SQLException;
}
