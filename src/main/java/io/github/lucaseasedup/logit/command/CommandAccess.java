package io.github.lucaseasedup.logit.command;

public final class CommandAccess
{
    private CommandAccess()
    {
    }
    
    public String getPermission()
    {
        return permission;
    }
    
    public boolean isPlayerOnly()
    {
        return playerOnly;
    }
    
    public boolean isRunningCoreRequired()
    {
        return runningCoreRequired;
    }
    
    public static final class Builder
    {
        public Builder permission(String permission)
        {
            if (permission == null)
                throw new IllegalArgumentException();
            
            this.permission = permission;
            
            return this;
        }
        
        public Builder playerOnly(boolean playerOnly)
        {
            this.playerOnly = playerOnly;
            
            return this;
        }
        
        public Builder runningCoreRequired(boolean runningCoreRequired)
        {
            this.runningCoreRequired = runningCoreRequired;
            
            return this;
        }
        
        public CommandAccess build()
        {
            CommandAccess commandAccess = new CommandAccess();
            
            commandAccess.permission = permission;
            commandAccess.playerOnly = playerOnly;
            commandAccess.runningCoreRequired = runningCoreRequired;
            
            return commandAccess;
        }
        
        private String permission;
        private boolean playerOnly;
        private boolean runningCoreRequired;
    }
    
    private String permission;
    private boolean playerOnly;
    private boolean runningCoreRequired;
}
