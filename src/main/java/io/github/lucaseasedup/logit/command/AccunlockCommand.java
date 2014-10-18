package io.github.lucaseasedup.logit.command;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class AccunlockCommand extends LogItCoreObject implements TabExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        
        if (args.length <= 1)
        {
            if (player != null && !player.hasPermission("logit.accunlock"))
            {
                sendMsg(sender, t("noPerms"));
                
                return true;
            }
            
            if (args.length < 1)
            {
                sendMsg(sender, t("paramMissing")
                        .replace("{0}", "username"));
                
                return true;
            }
            
            if (player != null && player.getName().equalsIgnoreCase(args[0]))
            {
                sendMsg(sender, t("accunlock.cannotUnlockYourself"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(args[0], Arrays.asList(
                    keys().username(),
                    keys().is_locked()
            ));
            
            if (account == null)
            {
                sendMsg(sender, t("notRegistered.others")
                        .replace("{0}", args[0]));
                
                return true;
            }
            
            account.setLocked(false);
            
            sendMsg(args[0], t("accunlock.success.self"));
            sendMsg(sender, t("accunlock.success.others")
                    .replace("{0}", PlayerUtils.getPlayerRealName(args[0])));
        }
        else
        {
            sendMsg(sender, t("incorrectParamCombination"));
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command cmd,
                                      String label,
                                      String[] args)
    {
        if (!getConfig("secret.yml").getBoolean("tabCompletion"))
            return null;
        
        if (args.length == 1)
        {
            if (sender instanceof Player
                    && !sender.hasPermission("logit.accunlock"))
                return null;
            
            return getTabCompleter().completeUsername(args[0]);
        }
        
        return null;
    }
}
