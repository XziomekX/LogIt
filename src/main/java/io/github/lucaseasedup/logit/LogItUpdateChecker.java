/*
 * LogItUpdateChecker.java
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
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class LogItUpdateChecker
{
    private LogItUpdateChecker()
    {
    }
    
    /**
     * Fetches the latest LogIt build number available from BukkitDev.
     * 
     * @return the latest LogIt build number available.
     * 
     * @throws IOException    if an I/O error occurred while downloading the RSS.
     * @throws ParseException if the RSS could not be parsed.
     */
    public static int fetchLatestBuildNumber() throws IOException, ParseException
    {
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
            
            NodeList latestItem = doc.getElementsByTagName("item").item(0).getChildNodes();
            String title = latestItem.item(1).getTextContent();
            Matcher titleMatcher = ITEM_TITLE_PATTERN.matcher(title);
            
            if (titleMatcher.find())
            {
                return Integer.parseInt(titleMatcher.group(1));
            }
            else
            {
                throw new ParseException("Invalid item title: " + title, -1);
            }
        }
    }
    
    public static final Pattern ITEM_TITLE_PATTERN =
            Pattern.compile("^ *LogIt +(?:[A-Za-z ]+ |)"
                          + "v(?:[0-9]+\\.[0-9]+\\.[0-9]+(?:\\.[0-9]+|)|)"
                          + "\\-b([0-9]+) *$");
    
    private static final String RSS_URL = "http://dev.bukkit.org/bukkit-plugins/logit/files.rss";
}
