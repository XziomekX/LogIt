package io.github.lucaseasedup.logit.command.hub;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public final class HubCommands
{
    private HubCommands()
    {
    }
    
    public static Iterator<HubCommand> iterator()
    {
        return new Iterator<HubCommand>()
        {
            @Override
            public boolean hasNext()
            {
                return it.hasNext();
            }
            
            @Override
            public HubCommand next()
            {
                return it.next();
            }
            
            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
            
            private final Iterator<HubCommand> it = hubCommands.iterator();
        };
    }
    
    private static final Set<HubCommand> hubCommands = new LinkedHashSet<>();
    
    static
    {
        hubCommands.add(new HelpHubCommand());
        hubCommands.add(new VersionHubCommand());
        hubCommands.add(new StartHubCommand());
        hubCommands.add(new StopHubCommand());
        hubCommands.add(new ReloadHubCommand());
        hubCommands.add(new BackupForceHubCommand());
        hubCommands.add(new BackupRestoreFileHubCommand());
        hubCommands.add(new BackupRestoreTimeHubCommand());
        hubCommands.add(new BackupRemoveHubCommand());
        hubCommands.add(new GotowrHubCommand());
        hubCommands.add(new GlobalpassHubCommand());
        hubCommands.add(new AccountStatusHubCommand());
        hubCommands.add(new AccountInfoHubCommand());
        hubCommands.add(new AccountRenameHubCommand());
        hubCommands.add(new AccountDatumHubCommand());
        hubCommands.add(new IpcountHubCommand());
        hubCommands.add(new ConfigGetHubCommand());
        hubCommands.add(new ConfigSetHubCommand());
        hubCommands.add(new ConfigListHubCommand());
        hubCommands.add(new ConfigReloadHubCommand());
        hubCommands.add(new ConvertHubCommand());
        hubCommands.add(new StatsHubCommand());
        hubCommands.add(new ImportAuthMeHubCommand());
    }
}
