/* 
 * Copyright (C) 2015 Adrien Guille <adrien.guille@univ-lyon2.fr>
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
package main.java.fr.ericlab.sondy.core.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class PropertiesFileUtils {
    
    public static void saveProperties(String filePath, Properties properties){
        FileOutputStream propertiesFOS = null;
        try {
            propertiesFOS = new FileOutputStream(filePath);
            properties.store(propertiesFOS, "");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PropertiesFileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PropertiesFileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                propertiesFOS.close();
            } catch (IOException ex) {
                Logger.getLogger(PropertiesFileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static String readProperty(String filePath, String key){
        FileInputStream propertiesFIS = null;
        try {
            String value = null;
            Properties properties = new Properties();
            propertiesFIS = new FileInputStream(filePath);
            properties.load(propertiesFIS);
            value = properties.getProperty(key);
            return value;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PropertiesFileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PropertiesFileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                propertiesFIS.close();
            } catch (IOException ex) {
                Logger.getLogger(PropertiesFileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "";
    }
    
    public static void writeProperty(String filePath, String key, String value){
        FileOutputStream propertiesFOS = null;
        try {
            Properties properties = new Properties();
            propertiesFOS = new FileOutputStream(filePath);
            properties.setProperty(key,value);
            properties.store(propertiesFOS, "");
            propertiesFOS.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PropertiesFileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PropertiesFileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
