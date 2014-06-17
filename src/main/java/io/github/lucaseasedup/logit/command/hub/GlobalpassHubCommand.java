/*
 * GlobalpassHubCommand.java
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
package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.locale.Locale;
import org.bukkit.command.CommandSender;

public final class GlobalpassHubCommand extends HubCommand
{
    public GlobalpassHubCommand()
    {
        super("globalpass", new String[] {}, "logit.globalpass.generate", false, true,
                new CommandHelpLine.Builder()
                        .command("logit globalpass")
                        .descriptionLabel("subCmdDesc.globalpass")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        String password = getGlobalPasswordManager().generatePassword();
        long lifetimeSecs =
                getConfig("config.yml").getTime("password.global-password.invalidate-after",
                        TimeUnit.SECONDS);
        Locale activeLocale = getLocaleManager().getActiveLocale();
        
        sendMsg(sender, _("globalpass.generated")
                .replace("{0}", password));
        sendMsg(sender, _("globalpass.invalidationInfo")
                .replace("{0}", activeLocale.stringifySeconds((int) lifetimeSecs)));
    }
}
