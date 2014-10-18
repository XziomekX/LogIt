package io.github.lucaseasedup.logit.command.wizard;

import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public final class ConfirmationWizard extends Wizard
{
    /**
     * @param sender   a {@code CommandSender} that ran this wizard.
     * @param keyword  a keyword that, when sent by the specified {@code CommandSender},
     *                 will tell this wizard to proceed.
     * @param callback a callback that will be run when the given keyword will be typed.
     * 
     * @throws IllegalArgumentException if {@code sender} or {@code callback} is {@code null}; or
     *                                  {@code keyword} is {@code null}, blank or starts with "/".
     */
    public ConfirmationWizard(CommandSender sender, String keyword, Runnable callback)
    {
        super(sender, null);
        
        if (sender == null)
            throw new IllegalArgumentException();
        
        if (StringUtils.isBlank(keyword) || keyword.startsWith("/"))
            throw new IllegalArgumentException();
        
        if (callback == null)
            throw new IllegalArgumentException();
        
        this.keyword = keyword;
        this.callback = callback;
    }
    
    @Override
    protected void onCreate()
    {
        // Left for optional implementation by extending classes.
    }
    
    @Override
    protected void onMessage(String message)
    {
        if (message.equalsIgnoreCase(keyword))
        {
            callback.run();
        }
        else
        {
            sendMessage(t("wizardCancelled"));
        }
        
        cancelWizard();
    }
    
    private final String keyword;
    private final Runnable callback;
}
