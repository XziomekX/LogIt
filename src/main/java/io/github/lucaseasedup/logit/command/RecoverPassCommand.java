/*
 * RecoverPassCommand.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit.command;

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.account.Account;
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
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.recoverpass"))
            {
                sendMsg(player, _("noPerms"));
                
                return true;
            }
            
            if (args.length < 1)
            {
                sendMsg(player, _("paramMissing")
                        .replace("{0}", "email"));
                
                return true;
            }
            
            final String username = player.getName().toLowerCase();
            
            if (playerLocks.contains(username))
            {
                sendMsg(player, _("cmdPlayerLock"));
                
                return true;
            }
            
            if (getCooldownManager().isCooldownActive(player, LogItCooldowns.RECOVERPASS))
            {
                getMessageDispatcher().sendCooldownMessage(username,
                        getCooldownManager().getCooldownMillis(player, LogItCooldowns.RECOVERPASS));
                
                return true;
            }
            
            if (!getAccountManager().isRegistered(player.getName()))
            {
                sendMsg(player, _("notRegistered.self"));
                
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
                            sendMsg(player, _("recoverPassword.incorrectEmailAddress"));
                            
                            return;
                        }
                        
                        String to = email;
                        String bodyTemplate = IoUtils.toString(bodyTemplateFile);
                        String body = bodyTemplate
                                .replace("%player%", playerName)
                                .replace("%password%", newPassword);
                        
                        LogItCooldowns.activate(player, LogItCooldowns.RECOVERPASS);
                        
                        MailSender.from(smtpHost, smtpPort, smtpUser, smtpPassword)
                                .sendMail(Arrays.asList(to),from, subject, body, htmlEnabled);
                        
                        account.changePassword(newPassword);
                        
                        sendMsg(sender, _("recoverPassword.success.self")
                                .replace("{0}", email));
                        log(Level.FINE, _("recoverPassword.success.log")
                                .replace("{0}", playerName)
                                .replace("{1}", to));
                    }
                    catch (ReportedException | IOException | MessagingException ex)
                    {
                        sendMsg(sender, _("recoverPassword.fail.self"));
                        
                        log(Level.WARNING, _("recoverPassword.fail.log")
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
            sendMsg(sender, _("incorrectParamCombination"));
        }
        
        return true;
    }
    
    private final Set<String> playerLocks = new HashSet<>();
}
