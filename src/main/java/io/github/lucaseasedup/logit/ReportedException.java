/*
 * ReportedException.java
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

/**
 * Exception that does not need any further logging.
 */
public class ReportedException extends RuntimeException
{
    public ReportedException()
    {
    }
    
    public ReportedException(String msg)
    {
        super(msg);
    }
    
    public ReportedException(Throwable cause)
    {
        super(cause);
    }
    
    public void rethrow()
    {
        decrementRequestCount();
        
        throw this;
    }
    
    public void rethrowAsFatal() throws FatalReportedException
    {
        decrementRequestCount();
        
        throw new FatalReportedException(getCause());
    }
    
    public static void throwNew()
    {
        if (shouldSignal())
        {
            throw new ReportedException();
        }
    }
    
    public static void throwNew(String msg)
    {
        if (shouldSignal())
        {
            throw new ReportedException(msg);
        }
    }
    
    public static void throwNew(Throwable cause)
    {
        if (shouldSignal())
        {
            throw new ReportedException(cause);
        }
    }
    
    public static void incrementRequestCount()
    {
        RequestCounter counter = requestCounter.get();

        if (counter == null)
        {
            requestCounter.set(counter = new RequestCounter());
        }
        
        counter.count++;
    }
    
    public static void decrementRequestCount()
    {
        RequestCounter counter = requestCounter.get();

        if (counter == null)
        {
            requestCounter.set(counter = new RequestCounter());
        }
        
        counter.count--;
    }
    
    public static boolean shouldSignal()
    {
        RequestCounter counter = requestCounter.get();
        
        if (counter == null)
        {
            requestCounter.set(counter = new RequestCounter());
        }
        
        return counter.count > 0;
    }
    
    private static class RequestCounter
    {
        public int count = 0;
    }
    
    private static final ThreadLocal<RequestCounter> requestCounter = new ThreadLocal<RequestCounter>();
}
