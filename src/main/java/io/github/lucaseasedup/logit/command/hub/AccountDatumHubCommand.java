package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class AccountDatumHubCommand extends HubCommand
{
    public AccountDatumHubCommand()
    {
        super("account datum", new String[] {"username", "key"},
                new CommandAccess.Builder()
                        .permission("logit.account.datum")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit account datum")
                        .descriptionLabel("subCmdDesc.account.datum")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        List<String> queryKeys = new ArrayList<>();
        
        if (!args[1].equals(keys().username()))
        {
            queryKeys.add(keys().username());
        }
        
        queryKeys.add(args[1]);
        
        Account account = getAccountManager().selectAccount(args[0], queryKeys);
        
        if (account == null)
        {
            sendMsg(sender, t("notRegistered.others")
                    .replace("{0}", args[0]));
            
            return;
        }
        
        if (!account.getEntry().containsKey(args[1]))
        {
            sendMsg(sender, t("accountDatum.keyNotFound")
                    .replace("{0}", args[1]));
            
            return;
        }
        
        sendMsg(sender, t("accountDatum.result")
                .replace("{0}", args[0].toLowerCase())
                .replace("{1}", args[1])
                .replace("{2}", account.getEntry().get(args[1])));
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
