package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AccountStatusHubCommand extends HubCommand
{
    public AccountStatusHubCommand()
    {
        super("account status", new String[] {"username"},
                new CommandAccess.Builder()
                        .permission("logit.account.status")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit account status")
                        .descriptionLabel("subCmdDesc.account.status")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, t("accountStatus.header"));
        sendMsg(sender, t("accountStatus.username")
                .replace("{0}", args[0].toLowerCase()));
        
        String status;
        
        if (getAccountManager().isRegistered(args[0]))
        {
            status = t("accountStatus.status.registered");
        }
        else
        {
            status = t("accountStatus.status.notRegistered");
        }
        
        sendMsg(sender, t("accountStatus.status")
                .replace("{0}", status));
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
    }
    
    @Override
    public List<String> complete(CommandSender sender, String[] args)
    {
        if (!getConfig("secret.yml").getBoolean("tabCompletion"))
            return null;
        
        if (args.length == 1)
        {
            return getTabCompleter().completeUsername(args[0]);
        }
        
        return null;
    }
}
