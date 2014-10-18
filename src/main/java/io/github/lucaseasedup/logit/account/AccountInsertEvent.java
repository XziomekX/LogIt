package io.github.lucaseasedup.logit.account;

import io.github.lucaseasedup.logit.storage.Storage;

public final class AccountInsertEvent extends AccountEvent
{
    /* package */ AccountInsertEvent(Storage.Entry entry)
    {
        if (entry == null)
            throw new IllegalArgumentException();
        
        this.entry = entry;
    }
    
    public String getDatumValue(String key)
    {
        return entry.get(key);
    }
    
    private final Storage.Entry entry;
}
