package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.command.wizard.ConfirmationWizard;
import io.github.lucaseasedup.logit.common.ReportedException;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BackupRestoreFileHubCommand extends HubCommand
{
    public BackupRestoreFileHubCommand()
    {
        super("backup restore file", new String[] {"filename"},
                new CommandAccess.Builder()
                        .permission("logit.backup.restore")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit backup restore file")
                        .descriptionLabel("subCmdDesc.backup.restore.filename")
                        .build());
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args)
    {
        File selectedBackup = getBackupManager().getBackupFile(args[0]);
        
        if (selectedBackup == null)
        {
            sendMsg(sender, t("restoreBackup.backupNotFound")
                    .replace("{0}", args[0]));
            
            return;
        }
        
        Date selectedBackupDate;
        
        try
        {
            selectedBackupDate = getBackupManager().parseBackupFilename(
                    selectedBackup.getName()
            );
        }
        catch (ParseException ex)
        {
            sendMsg(sender, t("restoreBackup.fail")
                    .replace("{0}", selectedBackup.getName()));
            
            return;
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, t("restoreBackup.confirm.header"));
        sendMsg(sender, t("restoreBackup.confirm.typeToProceed"));
        sendMsg(sender, "");
        sendMsg(sender, t("restoreBackup.confirm.filename")
                .replace("{0}", selectedBackup.getName()));
        sendMsg(sender, t("restoreBackup.confirm.date")
                .replace("{0}", selectedBackupDate.toString()));
        sendMsg(sender, "");
        sendMsg(sender, t("restoreBackup.confirm.typeToCancel"));
        
        new ConfirmationWizard(sender, "proceed", new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    boolean forceBeforeRestore = getConfig("config.yml")
                            .getBoolean("backup.forceBeforeRestore");
                    
                    if (forceBeforeRestore)
                    {
                        getBackupManager().createBackup();
                    }
                    
                    getBackupManager().restoreBackup(args[0]);
                    
                    if (sender instanceof Player)
                    {
                        sendMsg(sender, t("restoreBackup.success")
                                .replace("{0}", args[0]));
                    }
                }
                catch (FileNotFoundException | ReportedException ex)
                {
                    if (sender instanceof Player)
                    {
                        sendMsg(sender, t("restoreBackup.fail")
                                .replace("{0}", args[0]));
                    }
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }).createWizard();
    }
    
    @Override
    public List<String> complete(CommandSender sender, String[] args)
    {
        if (!getConfig("secret.yml").getBoolean("tabCompletion"))
            return null;
        
        if (args.length == 1)
        {
            return getTabCompleter().completeBackupFilename(args[0]);
        }
        
        return null;
    }
}
