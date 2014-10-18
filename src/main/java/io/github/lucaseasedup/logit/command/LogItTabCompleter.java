package io.github.lucaseasedup.logit.command;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class LogItTabCompleter extends LogItCoreObject
{
    public List<String> completeUsername(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        List<Account> accounts = getAccountManager().selectAccounts(
                Arrays.asList(keys().username()),
                new SelectorCondition(
                        keys().username(),
                        Infix.STARTS_WITH,
                        username
                )
        );
        
        if (accounts == null)
            return null;
        
        List<String> suggestions = new ArrayList<>();
        
        for (Account account : accounts)
        {
            suggestions.add(account.getUsername());
        }
        
        Collections.sort(suggestions);
        
        return suggestions.subList(0,
                (suggestions.size() < MAX_SUGGESTIONS)
                        ? suggestions.size() : MAX_SUGGESTIONS);
    }
    
    public List<String> completeBackupFilename(String filename)
    {
        if (filename == null)
            throw new IllegalArgumentException();
        
        File[] backups = getBackupManager().getBackups();
        List<String> suggestions = new ArrayList<>();
        
        for (File backup : backups)
        {
            if (backup.getName().startsWith(filename))
            {
                suggestions.add(backup.getName());
            }
            
            if (suggestions.size() >= MAX_SUGGESTIONS)
            {
                break;
            }
        }
        
        return suggestions;
    }
    
    private static final int MAX_SUGGESTIONS = 16;
}
