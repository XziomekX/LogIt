package io.github.lucaseasedup.logit.command.wizard;

import static io.github.lucaseasedup.logit.message.MessageHelper.t;
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
        super(sender, Step.VIEW);
        
        this.playerName = playerName;
        this.fields = getProfileManager().getDefinedFields();
    }
    
    @Override
    protected void onCreate()
    {
        viewProfile(null);
        
        if (!fields.isEmpty())
        {
            updateStep(Step.CHOOSE_ACTION);
        }
        else
        {
            sendMessage(t("wizard.orangeHorizontalLine"));
            cancelWizard();
        }
    }
    
    @Override
    protected void onMessage(String message)
    {
        if (getCurrentStep() == Step.CHOOSE_ACTION)
        {
            if (message.equalsIgnoreCase("done"))
            {
                sendMessage(t("wizard.orangeHorizontalLine"));
                cancelWizard();
            }
            else if (message.equalsIgnoreCase("edit"))
            {
                sendMessage(t("profile.edit.enterFieldNumber"));
                updateStep(Step.EDIT_FIELD);
            }
            else if (message.equalsIgnoreCase("erase"))
            {
                sendMessage(t("profile.edit.enterFieldNumber"));
                updateStep(Step.ERASE_FIELD);
            }
        }
        else if (getCurrentStep() == Step.EDIT_FIELD || getCurrentStep() == Step.ERASE_FIELD)
        {
            int number;
            
            try
            {
                number = Integer.parseInt(message);
            }
            catch (NumberFormatException ex)
            {
                sendMessage(t("profile.edit.unknownField"));
                
                return;
            }
            
            if (number > fields.size() || number <= 0)
            {
                sendMessage(t("profile.edit.unknownField"));
                
                return;
            }
            
            field = fields.get(number - 1);
            
            if (getCurrentStep() == Step.EDIT_FIELD)
            {
                sendFieldEditingPrompt();
                
                updateStep(Step.ENTER_FIELD_VALUE);
            }
            else if (getCurrentStep() == Step.ERASE_FIELD)
            {
                getProfileManager().removeProfileObject(playerName, field.getName());
                
                viewProfile(field.getName());
                updateStep(Step.CHOOSE_ACTION);
            }
        }
        else if (getCurrentStep() == Step.ENTER_FIELD_VALUE)
        {
            if (field instanceof StringField)
            {
                StringField stringField = (StringField) field;
                
                if (message.length() < stringField.getMinLength())
                {
                    sendMessage(t("profile.edit.stringTooShort")
                            .replace("{0}", field.getName())
                            .replace("{1}", String.valueOf(stringField.getMinLength())));
                    
                    return;
                }
                
                if (message.length() > stringField.getMaxLength())
                {
                    sendMessage(t("profile.edit.stringTooLong")
                            .replace("{0}", field.getName())
                            .replace("{1}", String.valueOf(stringField.getMaxLength())));
                    
                    return;
                }
                
                getProfileManager().setProfileString(playerName,
                        field.getName(), message);
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
                    sendMessage(t("profile.edit.invalidValue")
                            .replace("{0}", message));
                    
                    return;
                }
                
                if (value < integerField.getMinValue())
                {
                    sendMessage(t("profile.edit.numberTooSmall")
                            .replace("{0}", field.getName())
                            .replace("{1}", String.valueOf(integerField.getMinValue())));
                    
                    return;
                }
                
                if (value > integerField.getMaxValue())
                {
                    sendMessage(t("profile.edit.numberTooBig")
                            .replace("{0}", field.getName())
                            .replace("{1}", String.valueOf(integerField.getMaxValue())));
                    
                    return;
                }
                
                getProfileManager().setProfileInteger(playerName, field.getName(), value);
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
                    sendMessage(t("profile.edit.invalidValue")
                            .replace("{0}", message));
                    
                    return;
                }
                
                if (value < floatField.getMinValue())
                {
                    sendMessage(t("profile.edit.numberTooSmall")
                            .replace("{0}", field.getName())
                            .replace("{1}", String.valueOf(floatField.getMinValue())));
                    
                    return;
                }
                
                if (value > floatField.getMaxValue())
                {
                    sendMessage(t("profile.edit.numberTooBig")
                            .replace("{0}", field.getName())
                            .replace("{1}", String.valueOf(floatField.getMaxValue())));
                    
                    return;
                }
                
                getProfileManager().setProfileFloat(playerName, field.getName(), value);
            }
            else if (field instanceof SetField)
            {
                SetField setField = (SetField) field;
                String trimmedMessage = message.trim();
                
                if (!setField.isAccepted(trimmedMessage))
                {
                    sendMessage(t("profile.edit.invalidValue")
                            .replace("{0}", message));
                    
                    return;
                }
                
                getProfileManager().setProfileString(playerName,
                        field.getName(), trimmedMessage);
            }
            else
            {
                throw new RuntimeException("Unknown field type: "
                        + field.getClass().getSimpleName());
            }
            
            viewProfile(field.getName());
            updateStep(Step.CHOOSE_ACTION);
        }
    }
    
    private void viewProfile(String updatedFieldName)
    {
        sendMessage("");
        sendMessage(t("profile.edit.header")
                .replace("{0}", playerName));
        sendMessage(t("wizard.orangeHorizontalLine"));
        
        if (fields.isEmpty())
        {
            sendMessage(t("profile.edit.noFields"));
        }
        else
        {
            int i = 1;
            
            for (Field field : fields)
            {
                Object value = getProfileManager()
                        .getProfileObject(playerName, field.getName());
                
                if (value == null)
                {
                    value = "";
                }
                
                if (field.getName().equals(updatedFieldName))
                {
                    sendMessage(t("profile.edit.updatedField")
                            .replace("{0}", String.valueOf(i))
                            .replace("{1}", field.getName())
                            .replace("{2}", value.toString()));
                }
                else
                {
                    sendMessage(t("profile.edit.field")
                            .replace("{0}", String.valueOf(i))
                            .replace("{1}", field.getName())
                            .replace("{2}", value.toString()));
                    
                }
                
                i++;
            }
            
            sendMessage("");
            sendMessage(t("profile.edit.chooseAction"));
        }
    }
    
    private void sendFieldEditingPrompt()
    {
        if (field instanceof StringField)
        {
            sendMessage(t("profile.edit.enterFieldValue.string")
                    .replace("{0}", field.getName()));
        }
        else if (field instanceof IntegerField)
        {
            sendMessage(t("profile.edit.enterFieldValue.integer")
                    .replace("{0}", field.getName()));
        }
        else if (field instanceof FloatField)
        {
            sendMessage(t("profile.edit.enterFieldValue.float")
                    .replace("{0}", field.getName()));
        }
        else if (field instanceof SetField)
        {
            SetField setField = (SetField) field;
            StringBuilder values = new StringBuilder();
            
            for (String value : setField.getAcceptedValues())
            {
                if (values.length() > 0)
                {
                    values.append(t("profile.edit.enterFieldValue.set.separator"));
                }
                
                values.append(t("profile.edit.enterFieldValue.set.value")
                        .replace("{0}", value));
            }
            
            sendMessage(t("profile.edit.enterFieldValue.set")
                    .replace("{0}", field.getName())
                    .replace("{1}", values.toString()));
        }
        else
        {
            throw new RuntimeException("Unknown field type: "
                    + field.getClass().getSimpleName());
        }
    }
    
    public static enum Step
    {
        VIEW, CHOOSE_ACTION,
        
        EDIT_FIELD, ENTER_FIELD_VALUE,
        
        ERASE_FIELD,
    }
    
    private final String playerName;
    private final List<Field> fields;
    private Field field;
}
