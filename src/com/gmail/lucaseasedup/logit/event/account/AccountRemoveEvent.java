/*
 * AccountUnregisterEvent.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit.event.account;

import com.gmail.lucaseasedup.logit.GeneralResult;
import static com.gmail.lucaseasedup.logit.GeneralResult.SUCCESS;

/**
 * @author LucasEasedUp
 */
public class AccountRemoveEvent extends AccountEvent
{
    public AccountRemoveEvent(String username, GeneralResult result)
    {
        super(username);
        
        this.result = result;
    }
    
    public boolean isSuccessful()
    {
        if (result.equals(SUCCESS))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private final GeneralResult result;
}
