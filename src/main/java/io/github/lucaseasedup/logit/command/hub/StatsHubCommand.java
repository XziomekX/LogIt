package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.storage.SelectorConstant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class StatsHubCommand extends HubCommand
{
    public StatsHubCommand()
    {
        super("stats", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.stats")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit stats")
                        .descriptionLabel("subCmdDesc.stats")
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
                new SelectorConstant(true)
        );
        
        Set<String> uniqueIps = null;
        
        if (accounts != null)
        {
            uniqueIps = new HashSet<>();
            
            for (Account account : accounts)
            {
                String ip = account.getIp();
                
                if (!StringUtils.isBlank(ip))
                {
                    uniqueIps.add(ip);
                }
            }
        }
        
        int backupCount = getBackupManager().getBackups().length;
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, t("stats.header"));
        sendMsg(sender, t("stats.accountCount")
                .replace("{0}", (accounts != null) ? String.valueOf(accounts.size()) : "?"));
        sendMsg(sender, t("stats.uniqueIps")
                .replace("{0}", (uniqueIps != null) ? String.valueOf(uniqueIps.size()) : "?"));
        sendMsg(sender, t("stats.backupCount")
                .replace("{0}", String.valueOf(backupCount)));
        
        if (getConfig("config.yml").getBoolean("stats.enabled"))
        {
            int logins = getConfig("stats.yml").getInt("logins");
            int passwordChanges = getConfig("stats.yml").getInt("passwordChanges");
            
            sendMsg(sender, "");
            sendMsg(sender, t("stats.logins")
                    .replace("{0}", String.valueOf(logins)));
            sendMsg(sender, t("stats.passwordChanges")
                    .replace("{0}", String.valueOf(passwordChanges)));
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
    }
}
