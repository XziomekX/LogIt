package io.github.lucaseasedup.logit.message;

import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCore;
import org.bukkit.entity.Player;

public final class QuitMessageGenerator
{
    private QuitMessageGenerator()
    {
    }
    
    public static String generate(Player player)
    {
        LogItCore core = LogItCore.getInstance();
        String message;
        
        if (core.getConfig("config.yml").getBoolean("messages.beautify"))
        {
            message = t("quit.beautified");
        }
        else
        {
            message = t("quit.native");
        }
        
        return message.replace("{0}", player.getName());
    }
}
