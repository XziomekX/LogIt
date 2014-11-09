package org.mcsg.double0negative.tabapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a list of tab information for a player.
 * 
 * @modifiedby LucasEU
 */
public final class TabObject
{
    public void setPriority(TabAPI tabApi, int priority)
    {
        if (tabApi == null || priority < -2 || priority > 2)
            throw new IllegalArgumentException();
        
        for (int i = -1; i < 4; i++)
        {
            if (tabs.get(i) != null && tabs.get(i).tabApi == tabApi)
            {
                tabs.put(i, null);
            }
        }
        
        if (priority > -2)
        {
            tabs.put(priority, new TabHolder(tabApi));
        }
    }
    
    /* package */ TabHolder getTab()
    {
        int i = 3;
        
        while (tabs.get(i) == null && i > -3)
        {
            i--;
        }
        
        if (i == -2)
        {
            return new TabHolder(null);
        }
        
        return tabs.get(i);
    }
    
    public void setTab(TabAPI tabApi, int x, int y, String msg, int ping)
    {
        int i = -1;
        
        while ((tabs.get(i) == null || tabs.get(i).tabApi != tabApi) && i < 3)
        {
            i++;
        }
        
        if (i == 3 && (tabs.get(i) == null || tabs.get(i).tabApi != tabApi))
        {
            setPriority(tabApi, 0);
            
            i = 0;
        }
        
        TabHolder holder = tabs.get(i);
        
        holder.tabs[y][x] = msg;
        holder.tabPings[y][x] = ping;
        holder.maxh = 3;
        holder.maxv = Math.max(x + 1, holder.maxv);
    }
    
    private final Map<Integer, TabHolder> tabs = new HashMap<>();
}
