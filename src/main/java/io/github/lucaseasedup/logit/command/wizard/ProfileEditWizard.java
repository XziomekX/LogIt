/*
 * ProfileEditWizard.java
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
import io.github.lucaseasedup.logit.profile.field.Field;
import io.github.lucaseasedup.logit.profile.field.FloatField;
import io.github.lucaseasedup.logit.profile.field.IntegerField;
import io.github.lucaseasedup.logit.profile.field.SetField;
import io.github.lucaseasedup.logit.profile.field.StringField;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class ProfileEditWizard extends Wizard
{
    public ProfileEditWizard(CommandSender sender, String playerName)
    {
        super(sender, new String[0], Step.VIEW);
        
        this.playerName = playerName;
        this.fields = getCore().getProfileManager().getDefinedFields();
    }
    
    @Override
    protected void onCreate()
    {
        viewProfile(null);
        
        if (!fields.isEmpty())
        {
            updateStep(Step.ENTER_FIELD_NUMBER);
        }
        else
        {
            sendMessage(getMessage("ORANGE_HORIZONTAL_LINE"));
            cancelWizard();
        }
    }
    
    @Override
    protected void onMessage(String message)
    {
        if (getCurrentStep() == Step.ENTER_FIELD_NUMBER)
        {
            if (message.equalsIgnoreCase("cancel"))
            {
                sendMessage(getMessage("ORANGE_HORIZONTAL_LINE"));
                cancelWizard();
                
                return;
            }
            
            int number;
            
            try
            {
                number = Integer.parseInt(message);
            }
            catch (NumberFormatException ex)
            {
                sendMessage(getMessage("PROFILE_EDIT_UNKNOWN_FIELD").replace("%field%", message));
                
                return;
            }
            
            if (number > fields.size() || number <= 0)
            {
                sendMessage(getMessage("PROFILE_EDIT_UNKNOWN_FIELD").replace("%field%", message));
                
                return;
            }
            
            field = fields.get(number - 1);
            
            if (field instanceof StringField)
            {
                StringField stringField = (StringField) field;
                
                sendMessage(getMessage("PROFILE_EDIT_ENTER_FIELD_VALUE_STRING")
                        .replace("%field%", field.getName()));
            }
            else if (field instanceof IntegerField)
            {
                IntegerField integerField = (IntegerField) field;
                
                sendMessage(getMessage("PROFILE_EDIT_ENTER_FIELD_VALUE_INTEGER")
                        .replace("%field%", field.getName()));
            }
            else if (field instanceof FloatField)
            {
                FloatField floatField = (FloatField) field;
                
                sendMessage(getMessage("PROFILE_EDIT_ENTER_FIELD_VALUE_FLOAT")
                        .replace("%field%", field.getName()));
            }
            else if (field instanceof SetField)
            {
                SetField setField = (SetField) field;
                StringBuilder values = new StringBuilder();
                
                for (String value : setField.getAcceptedValues())
                {
                    if (values.length() > 0)
                    {
                        values.append(getMessage("PROFILE_EDIT_ENTER_FIELD_VALUE_SET_SEPARATOR"));
                    }
                    
                    values.append(getMessage("PROFILE_EDIT_ENTER_FIELD_VALUE_SET_VALUE")
                            .replace("%value%", value));
                }
                
                sendMessage(getMessage("PROFILE_EDIT_ENTER_FIELD_VALUE_SET")
                        .replace("%field%", field.getName())
                        .replace("%values%", values.toString()));
            }
            else
            {
                throw new RuntimeException("Unknown field type: "
                        + field.getClass().getSimpleName());
            }
            
            updateStep(Step.ENTER_FIELD_VALUE);
        }
        else if (getCurrentStep() == Step.ENTER_FIELD_VALUE)
        {
            if (field instanceof StringField)
            {
                StringField stringField = (StringField) field;
                
                if (message.length() < stringField.getMinLength())
                {
                    sendMessage(getMessage("PROFILE_EDIT_STRING_TOO_SHORT")
                            .replace("%field%", field.getName())
                            .replace("%min-length%", String.valueOf(stringField.getMinLength())));
                    
                    return;
                }
                
                if (message.length() > stringField.getMaxLength())
                {
                    sendMessage(getMessage("PROFILE_EDIT_STRING_TOO_LONG")
                            .replace("%field%", field.getName())
                            .replace("%max-length%", String.valueOf(stringField.getMaxLength())));
                    
                    return;
                }
                
                getCore().getProfileManager().setProfileString(playerName, field.getName(), message);
            }
            else if (field instanceof IntegerField)
            {
                IntegerField integerField = (IntegerField) field;
                
                int value;
                
                try
                {
                    value = Integer.parseInt(message);
                }
                catch (NumberFormatException ex)
                {
                    sendMessage(getMessage("PROFILE_EDIT_INVALID_VALUE")
                            .replace("%value%", message));
                    
                    return;
                }
                
                if (value < integerField.getMinValue())
                {
                    sendMessage(getMessage("PROFILE_EDIT_NUMBER_TOO_SMALL")
                            .replace("%field%", field.getName())
                            .replace("%min-value%", String.valueOf(integerField.getMinValue())));
                    
                    return;
                }
                
                if (value > integerField.getMaxValue())
                {
                    sendMessage(getMessage("PROFILE_EDIT_NUMBER_TOO_BIG")
                            .replace("%field%", field.getName())
                            .replace("%max-value%", String.valueOf(integerField.getMaxValue())));
                    
                    return;
                }
                
                getCore().getProfileManager().setProfileInteger(playerName, field.getName(), value);
            }
            else if (field instanceof FloatField)
            {
                FloatField floatField = (FloatField) field;
                
                double value;
                
                try
                {
                    value = Double.parseDouble(message);
                }
                catch (NumberFormatException ex)
                {
                    sendMessage(getMessage("PROFILE_EDIT_INVALID_VALUE")
                            .replace("%value%", message));
                    
                    return;
                }
                
                if (value < floatField.getMinValue())
                {
                    sendMessage(getMessage("PROFILE_EDIT_NUMBER_TOO_SMALL")
                            .replace("%field%", field.getName())
                            .replace("%min-value%", String.valueOf(floatField.getMinValue())));
                    
                    return;
                }
                
                if (value > floatField.getMaxValue())
                {
                    sendMessage(getMessage("PROFILE_EDIT_NUMBER_TOO_BIG")
                            .replace("%field%", field.getName())
                            .replace("%max-value%", String.valueOf(floatField.getMaxValue())));
                    
                    return;
                }
                
                getCore().getProfileManager().setProfileFloat(playerName, field.getName(), value);
            }
            else if (field instanceof SetField)
            {
                SetField setField = (SetField) field;
                String trimmerMessage = message.trim();
                
                if (!setField.isAccepted(trimmerMessage))
                {
                    sendMessage(getMessage("PROFILE_EDIT_INVALID_VALUE")
                            .replace("%value%", message));
                    
                    return;
                }
                
                getCore().getProfileManager().setProfileString(playerName,
                        field.getName(), trimmerMessage);
            }
            else
            {
                throw new RuntimeException("Unknown field type: "
                        + field.getClass().getSimpleName());
            }
            
            viewProfile(field.getName());
            updateStep(Step.ENTER_FIELD_NUMBER);
        }
    }
    
    private void viewProfile(String updatedFieldName)
    {
        sendMessage("");
        sendMessage(getMessage("PROFILE_HEADER")
                .replace("%player%", playerName));
        sendMessage(getMessage("ORANGE_HORIZONTAL_LINE"));
        
        if (fields.isEmpty())
        {
            sendMessage(getMessage("PROFILE_EDIT_NO_FIELDS"));
        }
        else
        {
            int i = 1;
            
            for (Field field : fields)
            {
                Object value = getCore().getProfileManager()
                        .getProfileObject(playerName, field.getName());
                
                if (value == null)
                {
                    value = "";
                }
                
                String messageName = (field.getName().equals(updatedFieldName))
                        ? "PROFILE_EDIT_FIELD_UPDATED"
                                : "PROFILE_EDIT_FIELD";
                
                sendMessage(getMessage(messageName)
                        .replace("%number%", String.valueOf(i))
                        .replace("%field%", field.getName())
                        .replace("%value%", value.toString()));
                
                i++;
            }
            
            sendMessage("");
            sendMessage(getMessage("PROFILE_EDIT_ENTER_FIELD_NUMBER"));
        }
    }
    
    public static enum Step
    {
        VIEW, ENTER_FIELD_NUMBER, ENTER_FIELD_VALUE
    }
    
    private final String playerName;
    private final List<Field> fields;
    private Field field;
}
