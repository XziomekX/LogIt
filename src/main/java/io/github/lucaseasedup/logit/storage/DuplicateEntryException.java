package io.github.lucaseasedup.logit.storage;

import java.io.IOException;

public final class DuplicateEntryException extends IOException
{
    public DuplicateEntryException()
    {
    }
    
    public DuplicateEntryException(String message)
    {
        super(message);
    }
    
    private static final long serialVersionUID = 437422491380524839L;
}
