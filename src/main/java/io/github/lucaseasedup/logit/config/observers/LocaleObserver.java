package io.github.lucaseasedup.logit.config.observers;

import io.github.lucaseasedup.logit.config.Property;
import io.github.lucaseasedup.logit.config.PropertyObserver;
import java.io.IOException;
import java.util.logging.Level;

public final class LocaleObserver extends PropertyObserver
{
    @Override
    public void update(Property p)
    {
        try
        {
            getPlugin().reloadMessages(p.getString());
            
            if (getLocaleManager() != null)
            {
                getLocaleManager().switchActiveLocale(p.getString());
            }
        }
        catch (IOException ex)
        {
            log(Level.WARNING, "Could not load messages.", ex);
        }
    }
}
