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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class StopwordSets {
    public ObservableList<String> availableSets;
    
    public StopwordSets(){
        availableSets = FXCollections.observableArrayList();
        update();
    }
    
    public final void update(){
        availableSets.clear();
        for(File file : Configuration.stopwordSets.toFile().listFiles()){
            if(!file.getName().startsWith(".")){
                availableSets.add(file.getName());
            }
        }
    }
}
