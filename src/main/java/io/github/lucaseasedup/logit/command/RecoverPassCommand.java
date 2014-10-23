package io.github.lucaseasedup.logit.command;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.common.ReportedException;
import io.github.lucaseasedup.logit.cooldown.LogItCooldowns;
import io.github.lucaseasedup.logit.mail.MailSender;
import io.github.lucaseasedup.logit.security.SecurityHelper;
import io.github.lucaseasedup.logit.util.IoUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.mail.MessagingException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class RecoverPassCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args)
    {
        final Player player;
        
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        else
        {
            player = null;
        }
        
        if (args.length <= 1)
        {
            if (player == null)
            {
                sendMsg(sender, t("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.recoverpass"))
            {
                sendMsg(player, t("noPerms"));
                
                return true;
            }
            
            if (args.length < 1)
            {
                sendMsg(player, t("paramMissing")
                        .replace("{0}", "email"));
                
                return true;
            }
            
            final String username = player.getName().toLowerCase();
            
            if (playerLocks.contains(username))
            {
                sendMsg(player, t("cmdPlayerLock"));
                
                return true;
            }
            
            if (getCooldownManager().isCooldownActive(player, LogItCooldowns.RECOVERPASS))
            {
                getMessageDispatcher().sendCooldownMessage(
                        username,
                        getCooldownManager().getCooldownMillis(
                                player, LogItCooldowns.RECOVERPASS
                        )
                );
                
                return true;
            }
            
            if (!getAccountManager().isRegistered(player.getName()))
            {
                sendMsg(player, t("notRegistered.self"));
                
                return true;
            }
            
            final String playerName = player.getName();
            final String paramEmail = args[0];
            
            final String newPassword = SecurityHelper.generatePassword(
                    getConfig("config.yml").getInt("passwordRecovery.passwordLength"),
                    getConfig("config.yml").getString("passwordRecovery.passwordCombination")
            );
            
            final String from = getConfig("config.yml")
                    .getString("mailSending.emailAddress");
            
            final String smtpHost = getConfig("config.yml")
                    .getString("mailSending.smtp.host");
            
            final int smtpPort = getConfig("config.yml")
                    .getInt("mailSending.smtp.port");
            
            final String smtpUser = getConfig("config.yml")
                    .getString("mailSending.smtp.user");
            
            final String smtpPassword = getConfig("config.yml")
                    .getString("mailSending.smtp.password");
            
            final String subject = getConfig("config.yml")
                    .getString("passwordRecovery.subject")
                    .replace("%player%", player.getName());
            
            String bodyTemplateFilename = getConfig("config.yml")
                    .getString("passwordRecovery.bodyTemplate");
            
            final File bodyTemplateFile = getDataFile(bodyTemplateFilename);
            
            final boolean htmlEnabled = getConfig("config.yml")
                    .getBoolean("passwordRecovery.htmlEnabled");
            
            playerLocks.add(username);
            
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        ReportedException.incrementRequestCount();
                        
                        Account account = getAccountManager().selectAccount(playerName,
                                Arrays.asList(
                                        keys().username(),
                                        keys().email()
                                )
                        );
                        
                        String email = account.getEmail();
                        
                        if (!paramEmail.equalsIgnoreCase(email))
                        {
                            sendMsg(player,
                                    t("recoverPassword.incorrectEmailAddress"));
                            
                            return;
                        }
                        
                        String to = email;
                        String bodyTemplate = IoUtils.toString(bodyTemplateFile);
                        String body = bodyTemplate
                                .replace("%player%", playerName)
                                .replace("%password%", newPassword);
                        body = getPlugin().replaceGlobalTokens(body);
                        
                        LogItCooldowns.activate(player,
                                LogItCooldowns.RECOVERPASS);
                        
                        MailSender.from(
                                smtpHost, smtpPort, smtpUser, smtpPassword
                        ).sendMail(
                                Arrays.asList(to), from,
                                subject, body, htmlEnabled
                        );
                        
                        account.changePassword(newPassword);
                        
                        sendMsg(sender, t("recoverPassword.success.self")
                                .replace("{0}", email));
                        log(Level.FINE, t("recoverPassword.success.log")
                                .replace("{0}", playerName)
                                .replace("{1}", to));
                    }
                    catch (ReportedException | IOException | MessagingException ex)
                    {
                        sendMsg(sender, t("recoverPassword.fail.self"));
                        
                        log(Level.WARNING, t("recoverPassword.fail.log")
                                .replace("{0}", playerName), ex);
                    }
                    finally
                    {
                        ReportedException.decrementRequestCount();
                        
                        playerLocks.remove(username);
                    }
                }
            }.runTaskAsynchronously(getPlugin());
        }
        else
        {
            sendMsg(sender, t("incorrectParamCombination"));
        }
        
        return true;
    }
    
    private final Set<String> playerLocks = new HashSet<>();
}
