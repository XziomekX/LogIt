/*
 * UpdateChecker.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
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
package io.github.lucaseasedup.logit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class UpdateChecker
{
    private UpdateChecker()
    {
    }
    
    /**
     * Fetches the latest LogIt build version string available from BukkitDev.
     * 
     * @param currentFullName the current plugin's full name obtained
     *                        using {@code plugin.getDescription().getFullName()}.
     * 
     * @return the latest LogIt build version string available,
     *         or {@code null} if no update is available.
     * 
     * @throws IllegalArgumentException if {@code currentFullName} is {@code null}.
     * 
     * @throws IOException              if an I/O error occurred
     *                                  while downloading the RSS file.
     *                                  
     * @throws ParseException           if the RSS file could not be parsed.
     * 
     * @throws UnknownHostException     if a connection to the remote host
     *                                  could not be established.
     */
    public static String checkForUpdate(String currentFullName)
            throws IOException, ParseException
    {
        if (currentFullName == null)
            throw new IllegalArgumentException();
        
        try (InputStream is = new URL(RSS_URL).openConnection().getInputStream())
        {
            Document doc;
            
            try
            {
                doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            }
            catch (SAXException | ParserConfigurationException ex)
            {
                throw new ParseException("Could not parse an RSS document.", -1);
            }
            
            NodeList latestItemNodeList = doc.getElementsByTagName("item").item(0).getChildNodes();
            String latestFullName = latestItemNodeList.item(1).getTextContent();
            
            Matcher latestFullNameMatcher =
                    PLUGIN_FULL_NAME_PATTERN.matcher(latestFullName);
            Matcher currentFullNameMatcher =
                    PLUGIN_FULL_NAME_PATTERN.matcher(currentFullName);
            
            if (latestFullNameMatcher.find() && currentFullNameMatcher.find())
            {
                String currentMajor = currentFullNameMatcher.group(1);
                String currentMinor = currentFullNameMatcher.group(2);
                String currentPatch = currentFullNameMatcher.group(3);
                String currentQualifier = currentFullNameMatcher.group(4);
                String latestMajor = latestFullNameMatcher.group(1);
                String latestMinor = latestFullNameMatcher.group(2);
                String latestPatch = latestFullNameMatcher.group(3);
                String latestQualifier = latestFullNameMatcher.group(4);
                
                if (currentPatch == null)
                {
                    currentPatch = "0";
                }
                
                if (latestPatch == null)
                {
                    latestPatch = "0";
                }
                
                boolean updateFound = false;
                
                if (latestMajor.compareTo(currentMajor) > 0)
                {
                    updateFound = true;
                }
                else if (latestMajor.compareTo(currentMajor) == 0)
                {
                    if (latestMinor.compareTo(currentMinor) > 0)
                    {
                        updateFound = true;
                    }
                    else if (latestMinor.compareTo(currentMinor) == 0)
                    {
                        if (latestPatch.compareTo(currentPatch) > 0)
                        {
                            updateFound = true;
                        }
                        else if (latestPatch.compareTo(currentPatch) == 0)
                        {
                            if (latestQualifier == null && currentQualifier != null)
                            {
                                updateFound = true;
                            }
                        }
                    }
                }
                
                if (updateFound)
                {
                    String versionString = "v" + latestMajor
                                         + "." + latestMinor
                                         + "." + latestPatch;
                    
                    if (latestQualifier != null)
                    {
                        versionString += "-" + latestQualifier;
                    }
                    
                    return versionString;
                }
            }
            
            return null;
        }
    }
    
    public static final Pattern PLUGIN_FULL_NAME_PATTERN =
            Pattern.compile("^ *LogIt *"               // LogIt
                          + "v([0-9]+)"                // v<major>
                          + "\\.([0-9]+)"              // .<minor>
                          + "(?:\\.([0-9]+)|)"         // (.<patch>)
                          + "(?:\\-([A-Za-z0-9_-]+)|)" // (-<qualifier>)
                          + " *$");                    // 
    
    private static final String RSS_URL = "http://dev.bukkit.org/bukkit-plugins/logit/files.rss";
}
