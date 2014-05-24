/*
 * ConfirmationWizard.java
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
package io.github.lucaseasedup.logit.command.wizard;

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import org.bukkit.command.CommandSender;

public final class ConfirmationWizard extends Wizard
{
    /**
     * @param sender   a {@code CommandSender} that ran this wizard.
     * @param keyword  a keyword that, when sent by the specified {@code CommandSender},
     *                 will tell this wizard to proceed.
     * @param callback a callback that will be run when the given keyword will be typed. 
     * 
     * @throws IllegalArgumentException if {@code sender}, {@code keyword},
     *                                  or {@code callback} is {@code null}.
     */
    public ConfirmationWizard(CommandSender sender, String keyword, Runnable callback)
    {
        super(sender, null);
        
        if (sender == null || keyword == null || callback == null)
            throw new IllegalArgumentException();
        
        this.keyword = keyword;
        this.callback = callback;
    }
    
    @Override
    protected void onCreate()
    {
    }
    
    @Override
    protected void onMessage(String message)
    {
        if (message.equalsIgnoreCase(keyword))
        {
            callback.run();
        }
        else
        {
            sendMessage(_("wizardCancelled"));
        }
        
        cancelWizard();
    }
    
    private final String keyword;
    private final Runnable callback;
}
