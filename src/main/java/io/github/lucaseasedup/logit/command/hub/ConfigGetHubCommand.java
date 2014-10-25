package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.config.PredefinedConfiguration;
import io.github.lucaseasedup.logit.config.Property;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class ConfigGetHubCommand extends HubCommand
{
    public ConfigGetHubCommand()
    {
        super("config get", new String[] {"path"},
                new CommandAccess.Builder()
                        .permission("logit.config.get")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit config get")
                        .descriptionLabel("subCmdDesc.config.get")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        String hyphenatedPath = args[0];
        String camelCasePath = PredefinedConfiguration.getCamelCasePath(hyphenatedPath);
        Property property;
        
        if (!getConfig("config.yml").contains(hyphenatedPath))
        {
            if (!getConfig("config.yml").contains(camelCasePath))
            {
                sendMsg(sender, t("config.propertyNotFound")
                        .replace("{0}", hyphenatedPath));
                
                return;
            }
            else
            {
                property = getConfig("config.yml").getProperty(camelCasePath);
            }
        }
        else
        {
            property = getConfig("config.yml").getProperty(hyphenatedPath);
        }
        
        sendMsg(sender, t("config.get.property")
                .replace("{0}", property.getPath())
                .replace("{1}", property.getStringifiedValue()));
    }
    
    @Override
    public List<String> complete(CommandSender sender, String[] args)
    {
        if (!getConfig("secret.yml").getBoolean("tabCompletion"))
            return null;
        
        if (args.length == 1)
        {
            return getTabCompleter().completeConfigProperty(args[0]);
        }
        
        return null;
    }
}
