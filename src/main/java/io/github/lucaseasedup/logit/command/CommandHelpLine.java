package io.github.lucaseasedup.logit.command;

public final class CommandHelpLine
{
    private CommandHelpLine(
            String command, String descriptionLabel, String optionalParam
    )
    {
        this.command = command;
        this.descriptionLabel = descriptionLabel;
        this.optionalParam = optionalParam;
    }
    
    public String getCommand()
    {
        return command;
    }
    
    public String getDescriptionLabel()
    {
        return descriptionLabel;
    }
    
    public String getOptionalParam()
    {
        return optionalParam;
    }
    
    public boolean hasOptionalParam()
    {
        return optionalParam != null;
    }
    
    public static final class Builder
    {
        public Builder command(String command)
        {
            this.command = command;
            
            return this;
        }
        
        public Builder descriptionLabel(String descriptionLabel)
        {
            this.descriptionLabel = descriptionLabel;
            
            return this;
        }
        
        public Builder optionalParam(String optionalParam)
        {
            this.optionalParam = optionalParam;
            
            return this;
        }
        
        public CommandHelpLine build()
        {
            return new CommandHelpLine(command, descriptionLabel, optionalParam);
        }
        
        private String command;
        private String descriptionLabel;
        private String optionalParam;
    }
    
    private final String command;
    private final String descriptionLabel;
    private final String optionalParam;
}
