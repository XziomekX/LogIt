package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.test.SelfTestBootstrap;
import io.github.lucaseasedup.logit.test.SelfTestException;
import java.util.Date;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SelftestHubCommand extends HubCommand
{
    public SelftestHubCommand()
    {
        super("selftest", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.selftest")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit selftest")
                        .descriptionLabel("subCmdDesc.selftest")
                        .build(),
                HelpVisibility.HIDDEN);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!getConfig("secret.yml").getBoolean("debug.enableSelfTests"))
        {
            sendMsg(sender, ChatColor.RED + "Self-tests are not enabled." +
                    " Enable them only if you don't care about all your" +
                    " data DESTROYED.");
            
            return;
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, "----------------------------------------------------");
        sendMsg(sender, "Self-testing LogIt");
        sendMsg(sender, "-------------------------------------------------");
        
        try
        {
            long startTime = System.currentTimeMillis();
            
            new SelfTestBootstrap().run();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            sendMsg(sender, "-------------------------------------------------");
            sendMsg(sender, ChatColor.GREEN + "ALL TESTS SUCCESSFULLY PASSED");
            sendMsg(sender, "-------------------------------------------------");
            sendMsg(sender, "Total time: " + (totalTime / 1000.0D) + " s");
            sendMsg(sender, "Finished at: " + new Date().toString());
            
        }
        catch (SelfTestException ex)
        {
            sendMsg(sender, "-------------------------------------------------");
            sendMsg(sender, ChatColor.RED + "SELF-TEST FAILED");
            sendMsg(sender, "-------------------------------------------------");
            sendMsg(sender, "Finished at: " + new Date().toString());
            sendMsg(sender, "Exception stack trace: ");
            sendMsg(sender, "");
            sendMsg(sender, ExceptionUtils.getStackTrace(ex));
        }
        
        sendMsg(sender, "----------------------------------------------------");
    }
}
