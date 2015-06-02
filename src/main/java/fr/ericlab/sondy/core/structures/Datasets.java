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
package main.java.fr.ericlab.sondy.core.structures;

import main.java.fr.ericlab.sondy.core.app.Configuration;
import java.io.File;
import java.nio.file.Paths;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class Datasets {
    public ObservableList<String> list;
    
    public Datasets(){
        list = FXCollections.observableArrayList();
    }
    
    public void update(){
        list.clear();
        for(String filename : Configuration.datasets.toFile().list()){
            File file = Paths.get(Configuration.datasets+File.separator+filename).toFile();
            if(file.isDirectory()){
                list.add(filename);
            }
        }
    }
}
