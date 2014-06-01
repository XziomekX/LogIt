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
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.cooldown.LogItCooldowns;
import io.github.lucaseasedup.logit.mail.MailSender;
import io.github.lucaseasedup.logit.security.SecurityHelper;
import io.github.lucaseasedup.logit.util.IoUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
        final Player p;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        else
        {
            p = null;
        }
        
        if (args.length <= 1)
        {
            if (p == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            else if (!p.hasPermission("logit.recoverpass"))
            {
                sendMsg(p, _("noPerms"));
                
                return true;
            }
            else if (args.length < 1)
            {
                sendMsg(p, _("paramMissing")
                        .replace("{0}", "email"));
                
                return true;
            }
            
            final String username = p.getName().toLowerCase();
            
            if (getCooldownManager().isCooldownActive(p, LogItCooldowns.RECOVERPASS))
            {
                getMessageDispatcher().sendCooldownMessage(username,
                        getCooldownManager().getCooldownMillis(p, LogItCooldowns.RECOVERPASS));
                
                return true;
            }
            
            if (!getAccountManager().isRegistered(p.getName()))
            {
                sendMsg(p, _("notRegistered.self"));
                
                return true;
            }
            
            final String playerName = p.getName();
            final String argEmail = args[0];
            
            final String newPassword = SecurityHelper.generatePassword(
                    getConfig().getInt("password-recovery.password-length"),
                    getConfig().getString("password-recovery.password-combination")
            );
            
            final String from = getConfig().getString("mail.sending.email-address");
            final String smtpHost = getConfig().getString("mail.sending.smtp-host");
            final int smtpPort = getConfig().getInt("mail.sending.smtp-port");
            final String smtpUser = getConfig().getString("mail.sending.smtp-user");
            final String smtpPassword = getConfig().getString("mail.sending.smtp-password");
            
            final long cooldownMillis =
                    getConfig().getTime("cooldowns.recoverpass", TimeUnit.MILLISECONDS);
            final String subject = getConfig().getString("password-recovery.subject")
                    .replace("%player%", p.getName());
            final File bodyTemplateFile =
                    getDataFile(getConfig().getString("password-recovery.body-template"));
            final boolean htmlEnabled = getConfig().getBoolean("password-recovery.html-enabled");
            
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        ReportedException.incrementRequestCount();
                        
                        String email = getAccountManager().getEmail(playerName);
                        
                        if (!argEmail.equals(email))
                        {
                            sendMsg(p, _("recoverPassword.incorrectEmailAddress"));
                            
                            return;
                        }
                        
                        String to = email;
                        String bodyTemplate = IoUtils.toString(bodyTemplateFile);
                        String body = bodyTemplate
                                .replace("%player%", playerName)
                                .replace("%password%", newPassword);
                        
                        getCooldownManager().activateCooldown(p,
                                LogItCooldowns.RECOVERPASS, cooldownMillis);
                        
                        MailSender.from(smtpHost, smtpPort, smtpUser, smtpPassword)
                                .sendMail(Arrays.asList(to),from, subject, body, htmlEnabled);
                        
                        getAccountManager().changeAccountPassword(playerName, newPassword);
                        
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
}
