/*
 * AccountChangeEmailEvent.java
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

public final class AccountChangeEmailEvent extends AccountEvent
{
    public AccountChangeEmailEvent(Account account, String email)
    {
        this.account = account;
        this.email = email;
    }
    
    public Account getAccount()
    {
        return account;
    }
    
    /**
     * Equal to <code>getAccount().get("logit.accounts.username")</code>.
     * 
     * @return Username.
     */
    public String getUsername()
    {
        return account.getString("logit.accounts.username");
    }
    
    public String getEmail()
    {
        return email;
    }
    
    private final Account account;
    private final String email;
}
