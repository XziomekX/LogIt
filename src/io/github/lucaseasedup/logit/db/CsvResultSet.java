/*
 * CsvResultSet.java
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

/**
 * @author LucasEasedUp
 */
public class CsvResultSet implements ResultSet
{
    public CsvResultSet(ArrayList<ArrayList<String>> values, ArrayList<String> columns)
    {
        this.values = values;
        this.columns = columns;
        this.length = values.size();
    }
    
    @Override
    public boolean next() throws SQLException
    {
        pos++;
        
        return (pos < length) && (pos >= 0);
    }

    @Override
    public void close() throws SQLException
    {
    }

    @Override
    public boolean wasNull() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getString(int columnIndex) throws SQLException
    {
        return values.get(pos).get(columnIndex);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException
    {
        return Boolean.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException
    {
        return Byte.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public short getShort(int columnIndex) throws SQLException
    {
        return Short.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public int getInt(int columnIndex) throws SQLException
    {
        return Integer.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public long getLong(int columnIndex) throws SQLException
    {
        return Long.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException
    {
        return Float.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException
    {
        return Double.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
    {
        return new BigDecimal(new BigInteger(values.get(pos).get(columnIndex)), scale);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException
    {
        return values.get(pos).get(columnIndex).getBytes();
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException
    {
        return Date.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException
    {
        return Time.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException
    {
        return Timestamp.valueOf(values.get(pos).get(columnIndex));
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException
    {
        return new ByteArrayInputStream(values.get(pos).get(columnIndex).getBytes());
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException
    {
        return new ByteArrayInputStream(values.get(pos).get(columnIndex).getBytes());
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException
    {
        return new ByteArrayInputStream(values.get(pos).get(columnIndex).getBytes());
    }

    @Override
    public String getString(String columnLabel) throws SQLException
    {
        return values.get(pos).get(columns.indexOf(columnLabel));
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException
    {
        return Boolean.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException
    {
        return Byte.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public short getShort(String columnLabel) throws SQLException
    {
        return Short.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public int getInt(String columnLabel) throws SQLException
    {
        return Integer.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public long getLong(String columnLabel) throws SQLException
    {
        return Long.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException
    {
        return Float.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException
    {
        return Double.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException
    {
        return new BigDecimal(new BigInteger(values.get(pos).get(columns.indexOf(columnLabel))), scale);
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException
    {
        return values.get(pos).get(columns.indexOf(columnLabel)).getBytes();
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException
    {
        return Date.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException
    {
        return Time.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException
    {
        return Timestamp.valueOf(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException
    {
        return new ByteArrayInputStream(values.get(pos).get(columns.indexOf(columnLabel)).getBytes());
    }
    
    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException
    {
        return new ByteArrayInputStream(values.get(pos).get(columns.indexOf(columnLabel)).getBytes());
    }
    
    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException
    {
        return new ByteArrayInputStream(values.get(pos).get(columns.indexOf(columnLabel)).getBytes());
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearWarnings() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCursorName() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException
    {
        return values.get(pos).get(columnIndex);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException
    {
        return values.get(pos).get(columns.indexOf(columnLabel));
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException
    {
        return columns.indexOf(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException
    {
        return new StringReader(values.get(pos).get(columnIndex));
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException
    {
        return new StringReader(values.get(pos).get(columns.indexOf(columnLabel)));
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException
    {
        return new BigDecimal(values.get(pos).get(columnIndex));
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException
    {
        return new BigDecimal(values.get(pos).get(columns.indexOf(columnLabel)));
    }
    
    @Override
    public boolean isBeforeFirst() throws SQLException
    {
        return pos == -1 && length > 0;
    }
    
    @Override
    public boolean isAfterLast() throws SQLException
    {
        return pos == length && length > 0;
    }
    
    @Override
    public boolean isFirst() throws SQLException
    {
        return pos == 0 && length > 0;
    }

    @Override
    public boolean isLast() throws SQLException
    {
        return pos == (length - 1) && length > 0;
    }

    @Override
    public void beforeFirst() throws SQLException
    {
        pos = -1;
    }

    @Override
    public void afterLast() throws SQLException
    {
        pos = length;
    }

    @Override
    public boolean first() throws SQLException
    {
        pos = 1;
        
        return (pos < length) && (pos >= 0);
    }

    @Override
    public boolean last() throws SQLException
    {
        pos = length - 1;
        
        return (pos < length) && (pos >= 0);
    }

    @Override
    public int getRow() throws SQLException
    {
        return pos;
    }

    @Override
    public boolean absolute(int row) throws SQLException
    {
        pos = row;
        
        return (pos < length) && (pos >= 0);
    }

    @Override
    public boolean relative(int rows) throws SQLException
    {
        pos += rows;
        
        return (pos < length) && (pos >= 0);
    }

    @Override
    public boolean previous() throws SQLException
    {
        pos--;
        
        return (pos < length) && (pos >= 0);
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFetchDirection() throws SQLException
    {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFetchSize() throws SQLException
    {
        return length;
    }

    @Override
    public int getType() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getConcurrency() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowUpdated() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowInserted() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowDeleted() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException
    {
        values.get(pos).set(columnIndex, "");
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException
    {
        values.get(pos).set(columnIndex, String.valueOf(x));
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException
    {
        values.get(pos).set(columnIndex, String.valueOf(x));
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException
    {
        values.get(pos).set(columnIndex, String.valueOf(x));
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException
    {
        values.get(pos).set(columnIndex, String.valueOf(x));
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException
    {
        values.get(pos).set(columnIndex, String.valueOf(x));
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException
    {
        values.get(pos).set(columnIndex, String.valueOf(x));
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException
    {
        values.get(pos).set(columnIndex, String.valueOf(x));
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
    {
        values.get(pos).set(columnIndex, x.toString());
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException
    {
        values.get(pos).set(columnIndex, x);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException
    {
        values.get(pos).set(columnIndex, new String(x));
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException
    {
        values.get(pos).set(columnIndex, x.toString());
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException
    {
        values.get(pos).set(columnIndex, x.toString());
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException
    {
        values.get(pos).set(columnIndex, x.toString());
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException
    {
        values.get(pos).set(columnIndex, x.toString());
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), "");
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), String.valueOf(x));
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), String.valueOf(x));
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), String.valueOf(x));
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), String.valueOf(x));
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), String.valueOf(x));
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), String.valueOf(x));
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), String.valueOf(x));
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), x.toString());
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), x);
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), new String(x));
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), x.toString());
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), x.toString());
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), x.toString());
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), x.toString());
    }

    @Override
    public void insertRow() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRow() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRow() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshRow() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelRowUpdates() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveToInsertRow() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveToCurrentRow() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement getStatement() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException
    {
        try
        {
            return new URL(values.get(pos).get(columnIndex));
        }
        catch (MalformedURLException ex)
        {
            return null;
        }
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException
    {
        try
        {
            return new URL(values.get(pos).get(columns.indexOf(columnLabel)));
        }
        catch (MalformedURLException ex)
        {
            return null;
        }
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHoldability() throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        return false;
    }
    
    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException
    {
        values.get(pos).set(columnIndex, nString);
    }
    
    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException
    {
        values.get(pos).set(columns.indexOf(columnLabel), nString);
    }
    
    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getNString(int columnIndex) throws SQLException
    {
        return values.get(pos).get(columnIndex);
    }
    
    @Override
    public String getNString(String columnLabel) throws SQLException
    {
        return values.get(pos).get(columns.indexOf(columnLabel));
    }
    
    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException
    {
        return (T) values.get(pos).get(columnIndex);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException
    {
        return (T) values.get(pos).get(columns.indexOf(columnLabel));
    }
    
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        throw new UnsupportedOperationException();
    }
    
    public String[] getRowArray()
    {
        return values.get(pos).toArray(new String[values.get(pos).size()]);
    }
    
    private final ArrayList<ArrayList<String>> values;
    private final ArrayList<String> columns;
    private final int length;
    private int pos = -1;
}
