package io.github.lucaseasedup.logit.command;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.command.wizard.ProfileEditWizard;
import io.github.lucaseasedup.logit.command.wizard.ProfileViewWizard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ProfileCommand extends LogItCoreObject
        implements CommandExecutor
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
        
        if (args.length == 1 && args[0].equalsIgnoreCase("view"))
        {
            if (player == null)
            {
                sendMsg(sender, t("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.profile.view.self"))
            {
                sendMsg(sender, t("noPerms"));
                
                return true;
            }
            
            new ProfileViewWizard(sender, player.getName()).createWizard();
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("view"))
        {
            if (player != null && !player.hasPermission("logit.profile.view.others"))
            {
                sendMsg(sender, t("noPerms"));
                
                return true;
            }
            
            if (!getProfileManager().containsProfile(args[1]))
            {
                sendMsg(sender, t("profile.view.profileNotFound"));
                
                return true;
            }
            
            new ProfileViewWizard(sender, args[1]).createWizard();
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("edit"))
        {
            if (player == null)
            {
                sendMsg(sender, t("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.profile.edit.self"))
            {
                sendMsg(sender, t("noPerms"));
                
                return true;
            }
            
            new ProfileEditWizard(sender, player.getName()).createWizard();
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("edit"))
        {
            if (player != null && !player.hasPermission("logit.profile.edit.others"))
            {
                sendMsg(sender, t("noPerms"));
                
                return true;
            }
            
            new ProfileEditWizard(sender, args[1]).createWizard();
        }
        else
        {
            sendMsg(sender, t("incorrectParamCombination"));
        }
        
        return true;
    }
}
