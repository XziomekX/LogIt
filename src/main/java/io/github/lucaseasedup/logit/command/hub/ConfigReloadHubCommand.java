package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.config.InvalidPropertyValueException;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

public final class ConfigReloadHubCommand extends HubCommand
{
    public ConfigReloadHubCommand()
    {
        super("config reload", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.config.reload")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit config reload")
                        .descriptionLabel("subCmdDesc.config.reload")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        try
        {
            getConfigurationManager().loadAll();
            
            log(Level.INFO, t("reloadConfig.success"));
            
            if (sender instanceof Player)
            {
                sendMsg(sender, t("reloadConfig.success"));
            }
        }
        catch (IOException | InvalidConfigurationException | InvalidPropertyValueException ex)
        {
            ex.printStackTrace();
            
            sendMsg(sender, t("reloadConfig.fail"));
        }
    }
}
