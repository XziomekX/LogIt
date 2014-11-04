package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorBinary;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.SelectorNegation;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AccountInfoHubCommand extends HubCommand
{
    public AccountInfoHubCommand()
    {
        super("account info", new String[] {"username"},
                new CommandAccess.Builder()
                        .permission("logit.account.info")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit account info")
                        .descriptionLabel("subCmdDesc.account.info")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        Account account = getAccountManager().selectAccount(args[0], Arrays.asList(
                keys().username(),
                keys().ip(),
                keys().email(),
                keys().last_active_date(),
                keys().reg_date(),
                keys().is_locked(),
                keys().display_name()
        ));
        
        if (account == null)
        {
            sendMsg(sender, t("notRegistered.others")
                    .replace("{0}", PlayerUtils.getPlayerRealName(args[0])));
            
            return;
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, t("accountInfo.header"));
        sendMsg(sender, t("accountInfo.username")
                .replace("{0}", account.getUsername()));
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.ip"))
        {
            sendMsg(sender, t("accountInfo.ip")
                    .replace("{0}", account.getIp()));
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.email"))
        {
            sendMsg(sender, t("accountInfo.email")
                    .replace("{0}", account.getEmail()));
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.lastactivedate"))
        {
            long lastActiveDate = account.getLastActiveDate();
            
            if (lastActiveDate < 0)
            {
                sendMsg(sender, t("accountInfo.lastActiveDate.never"));
            }
            else
            {
                String registrationDateString = new Date(
                        account.getLastActiveDate() * 1000L
                ).toString();
                
                sendMsg(sender, t("accountInfo.lastActiveDate")
                        .replace("{0}", registrationDateString));
            }
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.regdate"))
        {
            long registrationDate = account.getRegistrationDate();
            
            if (registrationDate < 0)
            {
                sendMsg(sender, t("accountInfo.registrationDate.never"));
            }
            else
            {
                String registrationDateString = new Date(
                        account.getRegistrationDate() * 1000L
                ).toString();
                
                sendMsg(sender, t("accountInfo.registrationDate")
                        .replace("{0}", registrationDateString));
            }
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.displayname"))
        {
            sendMsg(sender, t("accountInfo.displayName")
                    .replace("{0}", account.getDisplayName()));
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.islocked"))
        {
            if (account.isLocked())
            {
                sendMsg(sender, "");
                sendMsg(sender, t("accountInfo.locked")
                        .replace("{0}", account.getUsername()));
            }
        }
        
        if (!StringUtils.isBlank(account.getIp()))
        {
            List<Account> otherAccounts = getAccountManager().selectAccounts(
                    Arrays.asList(
                            keys().username(),
                            keys().ip()
                    ),
                    new SelectorBinary(
                            new SelectorNegation(
                                    new SelectorCondition(
                                            keys().username(),
                                            Infix.EQUALS,
                                            account.getUsername()
                                    )
                            ),
                            Infix.AND,
                            new SelectorCondition(
                                    keys().ip(),
                                    Infix.EQUALS,
                                    account.getIp()
                            )
                    )
            );
            
            if (otherAccounts != null && !otherAccounts.isEmpty())
            {
                sendMsg(sender, "");
                sendMsg(sender, t("accountInfo.otherAccounts"));
                
                for (Account otherAccount : otherAccounts)
                {
                    sendMsg(sender, t("accountInfo.otherAccounts.username")
                            .replace("{0}", otherAccount.getUsername()));
                }
            }
        }
        
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
