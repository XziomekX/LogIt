/*
 * LogItCooldowns.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
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
package io.github.lucaseasedup.logit.cooldown;

public final class LogItCooldowns
{
    private LogItCooldowns()
    {
    }
    
    public static final Cooldown REGISTER = new Cooldown("logit.register");
    public static final Cooldown UNREGISTER = new Cooldown("logit.unregister");
    public static final Cooldown CHANGEPASS = new Cooldown("logit.changepass");
    public static final Cooldown CHANGEEMAIL = new Cooldown("logit.changeemail");
    public static final Cooldown RECOVERPASS = new Cooldown("logit.recoverpass");
}