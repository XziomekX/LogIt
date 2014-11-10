package io.github.lucaseasedup.logit.command.hub;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;

public abstract class HubCommand extends LogItCoreObject
{
    public HubCommand(
            String subcommand,
            String[] params,
            CommandAccess commandAccess,
            CommandHelpLine helpLine
    )
    {
        if (subcommand == null || params == null
                || commandAccess == null || helpLine == null)
        {
            throw new IllegalArgumentException();
        }
                    
        this.subcommand = subcommand;
        this.params = Arrays.asList(params);
        this.commandAccess = commandAccess;
        this.helpLine = helpLine;
    }
    
    public abstract void execute(CommandSender sender, String[] args);
    
    @SuppressWarnings("unused")
    public List<String> complete(CommandSender sender, String[] args)
    {
        return (args.length > getParams().size())
                ? new ArrayList<String>()
                : null;
    }
    
    public String getSubcommand()
    {
        return subcommand;
    }
    
    public List<String> getParams()
    {
        return Collections.unmodifiableList(params);
    }
    
    public String getPermission()
    {
        return commandAccess.getPermission();
    }
    
    public boolean isPlayerOnly()
    {
        return commandAccess.isPlayerOnly();
    }
    
    public boolean isRunningCoreRequired()
    {
        return commandAccess.isRunningCoreRequired();
    }
    
    public CommandHelpLine getHelpLine()
    {
        return helpLine;
    }
    
    private final String subcommand;
    private final List<String> params;
    private final CommandAccess commandAccess;
    private final CommandHelpLine helpLine;
}
