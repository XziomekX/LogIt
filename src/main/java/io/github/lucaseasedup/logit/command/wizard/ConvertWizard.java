/*
 * ConvertWizard.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit.command.wizard;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.FatalReportedException;
import io.github.lucaseasedup.logit.config.PropertyType;
import io.github.lucaseasedup.logit.config.validators.DbTypeValidator;
import io.github.lucaseasedup.logit.db.Table;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ConvertWizard extends Wizard
{
    public ConvertWizard(CommandSender sender, String[] args)
    {
        super(sender, args, Step.WELCOME);
    }
    
    @Override
    protected void onCreate()
    {
        sendMessage("");
        sendMessage(getMessage("CONVERT_WELCOME"));
        sendMessage(getMessage("ORANGE_HORIZONTAL_LINE"));
        sendMessage(getMessage("CONVERT_WELCOME_CHOICE"));
        
        updateStep(Step.WELCOME_CHOICE);
    }
    
    @Override
    protected void onMessage(String message)
    {
        if (getCurrentStep() == Step.WELCOME_CHOICE)
        {
            if (message.equals("proceed"))
            {
                sendMessage(getMessage("CONVERT_ENTER_DBTYPE"));
                updateStep(Step.ENTER_DBTYPE);
            }
            else
            {
                sendMessage(getMessage("WIZARD_CANCELLED"));
                cancelWizard();
            }
        }
        else if (getCurrentStep() == Step.ENTER_DBTYPE)
        {
            if (!new DbTypeValidator()
                    .validate("storage.accounts.db-type", PropertyType.STRING, message))
            {
                sendMessage(getMessage("CONVERT_INVALID_DBTYPE")
                        .replace("%dbtype%", message));
            }
            else
            {
                dbtype = message;
                
                sendMessage(getMessage("CONVERT_ENTERED_DBTYPE")
                        .replace("%dbtype%", message));
                
                switch (dbtype)
                {
                case "sqlite":
                case "h2":
                    sendMessage(getMessage("CONVERT_ENTER_FILENAME"));
                    updateStep(Step.ENTER_FILENAME);
                    break;
                    
                case "mysql":
                    sendMessage(getMessage("CONVERT_ENTER_HOST"));
                    updateStep(Step.ENTER_HOST);
                    break;
                    
                case "csv":
                    sendMessage(getMessage("CONVERT_ENTER_TABLE"));
                    updateStep(Step.ENTER_TABLE);
                    break;
                }
            }
        }
        else if (getCurrentStep() == Step.ENTER_FILENAME)
        {
            filename = message;

            sendMessage(getMessage("CONVERT_ENTERED_FILENAME")
                    .replace("%filename%", message));
            
            switch (dbtype)
            {
            case "sqlite":
            case "h2":
                sendMessage(getMessage("CONVERT_ENTER_TABLE"));
                updateStep(Step.ENTER_TABLE);
                break;
            }
        }
        else if (getCurrentStep() == Step.ENTER_HOST)
        {
            host = message;

            sendMessage(getMessage("CONVERT_ENTERED_HOST")
                    .replace("%host%", message));
            
            switch (dbtype)
            {
            case "mysql":
                sendMessage(getMessage("CONVERT_ENTER_USER"));
                updateStep(Step.ENTER_USER);
                break;
            }
        }
        else if (getCurrentStep() == Step.ENTER_USER)
        {
            user = message;
            
            sendMessage(getMessage("CONVERT_ENTERED_USER")
                    .replace("%user%", message));
            
            switch (dbtype)
            {
            case "mysql":
                sendMessage(getMessage("CONVERT_ENTER_PASSWORD"));
                updateStep(Step.ENTER_PASSWORD);
                break;
            }
        }
        else if (getCurrentStep() == Step.ENTER_PASSWORD)
        {
            password = message;
            
            sendMessage(getMessage("CONVERT_ENTERED_PASSWORD")
                    .replace("%password%", message.replaceAll(".", "*")));
            
            switch (dbtype)
            {
            case "mysql":
                sendMessage(getMessage("CONVERT_ENTER_DATABASE"));
                updateStep(Step.ENTER_DATABASE);
                break;
            }
        }
        else if (getCurrentStep() == Step.ENTER_DATABASE)
        {
            database = message;
            
            sendMessage(getMessage("CONVERT_ENTERED_DATABASE")
                    .replace("%database%", message));
            
            switch (dbtype)
            {
            case "mysql":
                sendMessage(getMessage("CONVERT_ENTER_TABLE"));
                updateStep(Step.ENTER_TABLE);
                break;
            }
        }
        else if (getCurrentStep() == Step.ENTER_TABLE)
        {
            table = message;
            
            sendMessage(getMessage("CONVERT_ENTERED_TABLE")
                    .replace("%table%", message));
            sendMessage(getMessage("CONVERT_COPY_OR_LEAVE"));
            updateStep(Step.COPY_OR_LEAVE);
        }
        else if (getCurrentStep() == Step.COPY_OR_LEAVE)
        {
            copyAccounts = message.equalsIgnoreCase("copy");
            
            sendMessage(getMessage("CONVERT_FINISH_CHOICE"));
            updateStep(Step.FINISH_CHOICE);
        }
        else if (getCurrentStep() == Step.FINISH_CHOICE)
        {
            if (message.equals("proceed"))
            {
                getConfig().set("storage.accounts.db-type", dbtype);
                
                switch (dbtype)
                {
                case "sqlite":
                    getConfig().set("storage.accounts.sqlite.filename", filename);
                    break;
                    
                case "h2":
                    getConfig().set("storage.accounts.h2.filename", filename);
                    break;
                    
                case "mysql":
                    getConfig().set("storage.accounts.mysql.host", host);
                    getConfig().set("storage.accounts.mysql.user", user);
                    getConfig().set("storage.accounts.mysql.password", password);
                    getConfig().set("storage.accounts.mysql.database", database);
                    break;
                }
                
                getConfig().set("storage.accounts.table", table);
                
                try
                {
                    List<Map<String, String>> rs = null;
                    
                    if (copyAccounts)
                    {
                        rs = getAccountManager().getTable().select();
                    }
                    
                    getCore().restart();
                    
                    if (copyAccounts)
                    {
                        Table table = getAccountManager().getTable();
                        
                        for (Map<String, String> row : rs)
                        {
                            Set<String> columns = row.keySet();
                            Collection<String> values = row.values();
                            
                            table.insert(columns.toArray(new String[columns.size()]),
                                    values.toArray(new String[values.size()]));
                        }
                        
                        getAccountManager().loadAccounts();
                    }
                    
                    if (getSender() instanceof Player)
                    {
                        sendMessage(getMessage("CONVERT_SUCCESS")
                                .replace("%dbtype%", dbtype));
                    }
                    
                    log(Level.INFO, getMessage("CONVERT_SUCCESS_LOG")
                            .replace("%dbtype%", dbtype));
                    
                    updateStep(Step.SUCCESS);
                }
                catch (FatalReportedException | SQLException ex)
                {
                    if (getSender() instanceof Player)
                    {
                        sendMessage(getMessage("CONVERT_FAIL"));
                    }
                    
                    log(Level.SEVERE, getMessage("CONVERT_FAIL"), ex);
                    
                    updateStep(Step.FAIL);
                }
                
                cancelWizard();
            }
            else
            {
                sendMessage(getMessage("WIZARD_CANCELLED"));
                cancelWizard();
            }
        }
    }
    
    public static enum Step
    {
        WELCOME, WELCOME_CHOICE,
        
        ENTER_DBTYPE, ENTER_FILENAME, ENTER_HOST, ENTER_USER,
        ENTER_PASSWORD, ENTER_DATABASE, ENTER_TABLE, COPY_OR_LEAVE,
        
        FINISH_CHOICE, SUCCESS, FAIL
    }
    
    private String dbtype;
    private String filename;
    private String host;
    private String user;
    private String password;
    private String database;
    private String table;
    private boolean copyAccounts;
}
