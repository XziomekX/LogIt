package io.github.lucaseasedup.logit.command;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.config.TimeUnit;
import io.github.lucaseasedup.logit.locale.Locale;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class RememberCommand extends LogItCoreObject
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
        
        if (args.length == 0)
        {
            if (player == null)
            {
                sendMsg(sender, t("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.remember"))
            {
                sendMsg(sender, t("noPerms"));
                
                return true;
            }
            
            if (!getSessionManager().isSessionAlive(player))
            {
                sendMsg(sender, t("notLoggedIn.self"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(
                    player.getName(),
                    Arrays.asList(
                            keys().username()
                    )
            );
            
            if (!getAccountManager().isRegistered(player.getName()))
            {
                sendMsg(sender, t("notRegistered.self"));
                
                return true;
            }
            
            String playerIp = PlayerUtils.getPlayerIp(player);
            long validnessTime;
            
            if (playerIp == null)
            {
                validnessTime = 0;
            }
            else
            {
                account.saveLoginSession(playerIp,
                        System.currentTimeMillis() / 1000L);
                validnessTime = getConfig("config.yml")
                        .getTime("loginSessions.validnessTime", TimeUnit.SECONDS);
            }
            
            Locale activeLocale = getLocaleManager().getActiveLocale();
            String localeValidnessTime =
                    activeLocale.stringifySeconds(validnessTime);
            
            sendMsg(sender, t("rememberLogin.success")
                    .replace("{0}", localeValidnessTime));
        }
        else
        {
            sendMsg(sender, t("incorrectParamCombination"));
        }
        
        return true;
    }
}
