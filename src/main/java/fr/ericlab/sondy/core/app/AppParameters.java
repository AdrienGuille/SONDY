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
package main.java.fr.ericlab.sondy.core.app;

import main.java.fr.ericlab.sondy.core.structures.Dataset;
import main.java.fr.ericlab.sondy.core.structures.Event;
import main.java.fr.ericlab.sondy.core.text.stopwords.StopwordSet;
import main.java.fr.ericlab.sondy.core.ui.GlobalUI;
import main.java.fr.ericlab.sondy.core.ui.LogUI;
import java.util.HashSet;
import javafx.collections.ObservableList;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class AppParameters {
    public static StopwordSet stopwords = new StopwordSet();
    public static Dataset dataset = new Dataset();
    public static Event event = new Event("","");
    public static DefaultGraph authorNetwork = new DefaultGraph("");
    public static int timeSliceA;
    public static int timeSliceB;
        
    public static void updateStopwords(ObservableList<String> stopwordSets){
        if(stopwordSets.size() > 0){
            HashSet<String> hashset = new HashSet<>();
            for(String stopwordSet : stopwordSets){
                hashset.add(stopwordSet);
            }
            stopwords.load(hashset);
        }
    }
    
    public static void disableUI(boolean value){
        GlobalUI.tabPane.setDisable(value);
        LogUI.progressBar.setProgress(value?-1.0:0.0);
    }
    
    
}
