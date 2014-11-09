package org.mcsg.double0negative.tabapi;

import io.github.lucaseasedup.logit.util.ArrayUtils;

/**
 * Holds tab information such as plugin and tab strings.
 * 
 * @author Drew
 * @modifiedby LucasEU
 */
/* package */ final class TabHolder
{
    public TabHolder(TabAPI tabApi)
    {
        if (tabApi == null)
            throw new IllegalArgumentException();
        
        this.tabApi = tabApi;
        
        tabs = new String[tabApi.getHorizSize()][tabApi.getVertSize()];
        tabPings = new int[tabApi.getHorizSize()][tabApi.getVertSize()];
    }
    
    public TabHolder copy()
    {
        TabHolder copy = new TabHolder(tabApi);
        
        copy.tabs = ArrayUtils.copy(String.class, tabs);
        copy.tabPings = ArrayUtils.copy(tabPings);
        
        return copy;
    }
    
    final TabAPI tabApi;
    
    String[][] tabs;
    int[][] tabPings;
    
    int maxh = 0;
    int maxv = 0;
}
