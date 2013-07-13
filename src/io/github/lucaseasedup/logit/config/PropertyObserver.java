package io.github.lucaseasedup.logit.config;

import java.util.Observable;
import java.util.Observer;

/**
 * @author LucasEasedUp
 */
public abstract class PropertyObserver implements Observer
{
    @Override
    public void update(Observable o, Object arg)
    {
        if (o instanceof Property)
        {
            update((Property) o);
        }
    }
    
    public abstract void update(Property p);
}
