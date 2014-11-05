package io.github.lucaseasedup.logit.security;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.config.TimeUnit;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public final class GlobalPasswordManager extends LogItCoreObject implements Runnable
{
    @Override
    public void dispose()
    {
        if (passwords != null)
        {
            passwords.clear();
            passwords = null;
        }
    }
    
    /**
     * Internal method. Do not call directly.
     */
    @Override
    public void run()
    {
        Iterator<Map.Entry<String, Long>> it = passwords.entrySet().iterator();
        
        while (it.hasNext())
        {
            Map.Entry<String, Long> e = it.next();
            
            if (e.getValue() <= 0)
            {
                it.remove();
                
                continue;
            }
            
            e.setValue(e.getValue() - TASK_PERIOD);
        }
    }
    
    public boolean checkPassword(String password)
    {
        if (password == null)
            throw new IllegalArgumentException();
        
        Iterator<Map.Entry<String, Long>> it = passwords.entrySet().iterator();
        
        while (it.hasNext())
        {
            Map.Entry<String, Long> e = it.next();
            
            if (e.getKey().equals(password) && e.getValue() > 0)
            {
                it.remove();
                
                return true;
            }
        }
        
        return false;
    }
    
    public String generatePassword()
    {
        int length = getConfig("config.yml").getInt("globalPassword.length");
        String password = getSecurityHelper().generatePassword(
                length, "0123456789"
        );
        long lifetimeTicks = getConfig("config.yml")
                .getTime("globalPassword.invalidateAfter", TimeUnit.TICKS);
        
        passwords.put(password, lifetimeTicks);
        
        return password;
    }
    
    public void invalidatePassword(String password)
    {
        passwords.remove(password);
    }
    
    /**
     * Recommended task period of {@code GlobalPasswordManager} running as a Bukkit task.
     */
    public static final long TASK_PERIOD = TimeUnit.SECONDS.convertTo(1, TimeUnit.TICKS);
    
    private Map<String, Long> passwords = new Hashtable<>();
}
