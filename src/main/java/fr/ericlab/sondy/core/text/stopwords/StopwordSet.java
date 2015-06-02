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
package main.java.fr.ericlab.sondy.core.text.stopwords;

import main.java.fr.ericlab.sondy.core.app.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class StopwordSet {
    public HashSet<String> loadedStopwordSet;
    
    public StopwordSet(){
        loadedStopwordSet = new HashSet<>();
    }
    
    public void load(String set){
        try {
            loadedStopwordSet.addAll(FileUtils.readLines(new File(Configuration.stopwordSets+File.separator+set)));
        } catch (IOException ex) {
            Logger.getLogger(StopwordSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void load(HashSet<String> sets){
        loadedStopwordSet.clear();
        for(String set : sets){
            load(set);
        }
    }
    
    public boolean contains(String word){
        return loadedStopwordSet.contains(word);
    }
}
