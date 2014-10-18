package io.github.lucaseasedup.logit.config;

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.util.Observable;
import java.util.Observer;

public abstract class PropertyObserver extends LogItCoreObject implements Observer
{
    @Override
    public final void update(Observable o, Object arg)
    {
        if (!(o instanceof Property))
            throw new RuntimeException("Illegal use of PropertyObserver.");
        
        update((Property) o);
    }
    
    public abstract void update(Property p);
}
