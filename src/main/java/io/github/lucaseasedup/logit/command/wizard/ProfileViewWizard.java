package io.github.lucaseasedup.logit.command.wizard;

import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.profile.field.Field;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class ProfileViewWizard extends Wizard
{
    public ProfileViewWizard(CommandSender sender, String playerName)
    {
        super(sender, Step.VIEW);
        
        this.playerName = playerName;
    }
    
    @Override
    protected void onCreate()
    {
        List<Field> fields = getProfileManager().getDefinedFields();
        
        sendMessage("");
        sendMessage(t("profile.view.header")
                .replace("{0}", playerName));
        sendMessage(t("wizard.orangeHorizontalLine"));
        
        if (!fields.isEmpty())
        {
            for (Field field : fields)
            {
                Object value = getProfileManager()
                        .getProfileObject(playerName, field.getName());
                
                if (value == null)
                {
                    value = "";
                }
                
                sendMessage(t("profile.view.field")
                        .replace("{0}", field.getName())
                        .replace("{1}", value.toString()));
            }
        }
        else
        {
            sendMessage(t("profile.view.noFields"));
        }
        
        sendMessage(t("wizard.orangeHorizontalLine"));
        cancelWizard();
    }
    
    @Override
    protected void onMessage(String message)
    {
        // ProfileViewWizard is cancelled as soon as it is created.
    }
    
    public static enum Step
    {
        VIEW
    }
    
    private final String playerName;
}
