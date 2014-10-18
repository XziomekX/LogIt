package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class IpcountHubCommand extends HubCommand
{
    public IpcountHubCommand()
    {
        super("ipcount", new String[] {"ip"},
                new CommandAccess.Builder()
                        .permission("logit.ipcount")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit ipcount")
                        .descriptionLabel("subCmdDesc.ipcount")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        List<Account> accounts = getAccountManager().selectAccounts(
                Arrays.asList(
                        keys().username(),
                        keys().ip()
                ),
                new SelectorCondition(keys().ip(), Infix.EQUALS, args[0])
        );
        
        sendMsg(sender, t("ipcount")
                .replace("{0}", args[0])
                .replace("{1}", (accounts != null) ? String.valueOf(accounts.size()) : "?"));
    }
}
