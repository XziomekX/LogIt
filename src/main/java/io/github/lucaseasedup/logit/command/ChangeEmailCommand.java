package io.github.lucaseasedup.logit.command;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.cooldown.LogItCooldowns;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import io.github.lucaseasedup.logit.util.Validators;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class ChangeEmailCommand extends LogItCoreObject
        implements TabExecutor
{
    @Override
    public boolean onCommand(
            CommandSender sender, Command cmd, String label, String[] args
    )
    {
        Player player = null;
        
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 3)
        {
            if (player != null && !player.hasPermission("logit.changeemail.others"))
            {
                sendMsg(sender, t("noPerms"));
                
                return true;
            }
            
            if (args.length < 2)
            {
                sendMsg(sender, t("paramMissing")
                        .replace("{0}", "player"));
                
                return true;
            }
            
            if (args.length < 3)
            {
                sendMsg(sender, t("paramMissing")
                        .replace("{0}", "newemail"));
                
                return true;
            }
            
            if (!Validators.validateEmail(args[2]))
            {
                sendMsg(sender, t("changeEmail.invalidEmailAddress"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(args[1], Arrays.asList(
                    keys().username()
            ));
            
            if (account == null)
            {
                sendMsg(sender, t("notRegistered.others")
                        .replace("{0}", PlayerUtils.getPlayerRealName(args[1])));
                
                return true;
            }
            
            account.setEmail(args[2]);
            
            sendMsg(args[1], t("changeEmail.success.self")
                    .replace("{0}", args[2].toLowerCase()));
            sendMsg(sender, t("changeEmail.success.others")
                    .replace("{0}", PlayerUtils.getPlayerRealName(args[1]))
                    .replace("{1}", args[2].toLowerCase()));
        }
        else if (args.length <= 1)
        {
            if (player == null)
            {
                sendMsg(sender, t("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.changeemail.self"))
            {
                sendMsg(player, t("noPerms"));
                
                return true;
            }
            
            if (args.length < 1)
            {
                sendMsg(player, t("paramMissing")
                        .replace("{0}", "newemail"));
                
                return true;
            }
            
            if (getCooldownManager().isCooldownActive(player, LogItCooldowns.CHANGEEMAIL))
            {
                getMessageDispatcher().sendCooldownMessage(
                        player.getName(),
                        getCooldownManager().getCooldownMillis(
                                player, LogItCooldowns.CHANGEEMAIL
                        )
                );
                
                return true;
            }
            
            if (!Validators.validateEmail(args[0]))
            {
                sendMsg(player, t("changeEmail.invalidEmailAddress"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(
                    player.getName(),
                    Arrays.asList(
                            keys().username()
                    )
            );
            
            if (account == null)
            {
                sendMsg(player, t("notRegistered.self"));
                
                return true;
            }
            
            int accountsWithEmail = getAccountManager().selectAccounts(
                    Arrays.asList(keys().username(), keys().email()),
                    new SelectorCondition(keys().email(), Infix.EQUALS, args[0].toLowerCase())
            ).size();
            
            if (accountsWithEmail >= getConfig("config.yml").getInt("accountsPerEmail"))
            {
                sendMsg(player, t("accountsPerEmailLimitReached"));
                
                return true;
            }
            
            account.setEmail(args[0]);
            
            sendMsg(sender, t("changeEmail.success.self")
                    .replace("{0}", args[0].toLowerCase()));
            
            LogItCooldowns.activate(player, LogItCooldowns.CHANGEEMAIL);
        }
        else
        {
            sendMsg(sender, t("incorrectParamCombination"));
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(
            CommandSender sender, Command cmd, String label, String[] args
    )
    {
        if (!getConfig("secret.yml").getBoolean("tabCompletion"))
            return null;
        
        if (args.length == 2 && args[0].equals("-x"))
        {
            if (sender instanceof Player
                    && !sender.hasPermission("logit.changeemail.others"))
                return null;
            
            return getTabCompleter().completeUsername(args[1]);
        }
        
        return null;
    }
}
