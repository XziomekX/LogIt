/*
 * AccountObserver.java
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
package io.github.lucaseasedup.logit.account;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.config.Property;
import java.util.Observable;
import java.util.Observer;

/**
 * @author LucasEasedUp
 */
public abstract class AccountObserver extends LogItCoreObject implements Observer
{
    @Override
    public final void update(Observable o, Object arg)
    {
        if (o instanceof Property && arg instanceof String)
        {
            update((Property) o, (String) arg);
        }
    }
    
    public abstract void update(Account account, String propertyChanged);
}
