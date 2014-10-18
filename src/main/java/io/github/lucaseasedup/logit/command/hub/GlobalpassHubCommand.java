package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.config.TimeUnit;
import io.github.lucaseasedup.logit.locale.Locale;
import org.bukkit.command.CommandSender;

public final class GlobalpassHubCommand extends HubCommand
{
    public GlobalpassHubCommand()
    {
        super("globalpass", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.globalpass.generate")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit globalpass")
                        .descriptionLabel("subCmdDesc.globalpass")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        String password = getGlobalPasswordManager().generatePassword();
        Locale activeLocale = getLocaleManager().getActiveLocale();
        long lifetimeSecs = getConfig("config.yml")
                .getTime("globalPassword.invalidateAfter", TimeUnit.SECONDS);
        
        sendMsg(sender, t("globalpass.generated")
                .replace("{0}", password));
        sendMsg(sender, t("globalpass.invalidationInfo")
                .replace("{0}", activeLocale.stringifySeconds((int) lifetimeSecs)));
    }
}
