package com.gmail.lucaseasedup.logit;

import com.gmail.lucaseasedup.logit.db.Database;
import java.sql.SQLException;

/**
 * @author LucasEasedUp
 */
public class Pinger implements Runnable
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
            database.executeStatement("SELECT 1");
        }
        catch (SQLException ex)
        {
        }
    }
    
    private Database database;
}
