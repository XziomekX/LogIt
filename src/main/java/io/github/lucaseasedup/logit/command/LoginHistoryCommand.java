/*
 * LoginHistoryCommand.java
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
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LoginHistoryCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        
        if (args.length < 3)
        {
            String playerName;
            
            if (args.length == 0)
            {
                if (player == null)
                {
                    sendMsg(sender, _("onlyForPlayers"));
                    
                    return true;
                }
                
                if (!player.hasPermission("logit.loginhistory.self"))
                {
                    sendMsg(sender, _("noPerms"));
                    
                    return true;
                }
                
                playerName = player.getName();
            }
            else if (args[0].equals("-x"))
            {
                if (player != null && !player.hasPermission("logit.loginhistory.others"))
                {
                    sendMsg(sender, _("noPerms"));
                    
                    return true;
                }
                
                if (args.length < 2)
                {
                    sendMsg(sender, _("paramMissing")
                            .replace("{0}", "username"));
                    
                    return true;
                }
                
                playerName = args[1];
            }
            else
            {
                sendMsg(sender, _("incorrectParamCombination"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(playerName, Arrays.asList(
                    keys().username(),
                    keys().login_history()
            ));
            
            if (account == null)
            {
                if (args.length == 0)
                {
                    sendMsg(sender, _("notRegistered.self"));
                }
                else
                {
                    sendMsg(sender, _("notRegistered.others")
                            .replace("{0}", PlayerUtils.getPlayerRealName(playerName)));
                }
                
                return true;
            }
            
            List<String> records = account.getLoginHistory();
            
            sendMsg(sender, "");
            sendMsg(sender, _("loginHistory.header"));
            
            String lastIp = null;
            int equalRecords = 1;
            
            for (int i = 0, n = records.size(); i < n - 1; i++)
            {
                String record = records.get(i);
                String[] split = record.split(";");
                String nextRecord = records.get(i + 1);
                String[] nextSplit = nextRecord.split(";");
                
                if (split.length < 3 || nextSplit.length < 3)
                    continue;
                
                boolean nextRecordEqual = split[0].equals(nextSplit[0])
                        && split[1].equals(nextSplit[1])
                        && split[2].equals(nextSplit[2]);
                
                if (nextRecordEqual)
                {
                    equalRecords++;
                }
                
                if (!nextRecordEqual)
                {
                    printLoginRecord(sender, split[0], split[1], split[2],
                            equalRecords, lastIp);
                    
                    equalRecords = 1;
                }
                
                // If the next record is the last one.
                if (i + 1 >= n - 1)
                {
                    printLoginRecord(sender, nextSplit[0], nextSplit[1], nextSplit[2],
                            equalRecords, split[1]);
                }
                
                lastIp = split[1];
            }
            
            sendMsg(sender, "");
        }
        else
        {
            sendMsg(sender, _("incorrectParamCombination"));
        }
        
        return true;
    }
    
    private void printLoginRecord(CommandSender sender,
                                  String unixTime,
                                  String ip,
                                  String succeeded,
                                  int equalRecords,
                                  String lastIp)
    {
        if (sender == null || unixTime == null || ip == null
                || succeeded == null || equalRecords < 0)
            throw new IllegalArgumentException();
        
        if (equalRecords == 0)
            return;
        
        String messageLabel;
        
        if (succeeded.equals("true"))
        {
            messageLabel = "loginHistory.record.success";
        }
        else
        {
            messageLabel = "loginHistory.record.fail";
        }
        
        String repetition;
        
        if (equalRecords > 1)
        {
            repetition = _("loginHistory.record.repetition")
                    .replace("{0}", String.valueOf(equalRecords));
        }
        else
        {
            repetition = "";
        }
        
        if (ip.equals(lastIp))
        {
            ip = _("loginHistory.record.ipDitto");
        }
        
        sendMsg(sender, _(messageLabel)
                .replace("{0}", new Date(Long.parseLong(unixTime) * 1000L).toString())
                .replace("{1}", ip)
                .replace("{2}", repetition));
    }
}
