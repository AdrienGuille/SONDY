/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core.structure;

import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.access.DBAccess;
import fr.ericlab.sondy.core.access.IndexAccess;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;

////////////////////////////////////////////////////////////////////////////////
//  This file is part of SONDY.                                               //
//                                                                            //
//  SONDY is free software: you can redistribute it and/or modify             //
//  it under the terms of the GNU General Public License as published by      //
//  the Free Software Foundation, either version 3 of the License, or         //
//  (at your option) any later version.                                       //
//                                                                            //
//  SONDY is distributed in the hope that it will be useful,                  //
//  but WITHOUT ANY WARRANTY; without even the implied warranty of            //
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
//  GNU General Public License for more details.                              //
//                                                                            //
//  You should have received a copy of the GNU General Public License         //
//  along with SONDY.  If not, see <http://www.gnu.org/licenses/>.            //
////////////////////////////////////////////////////////////////////////////////

/**
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class Timeline {
    public ArrayList<TimelineEvent> eventsList;
    
    public Timeline(ObservableList<DetectionResult> topicList, int numTopics, AppVariables appVariables){
        boolean save = (!appVariables.exportTimeline.equals("no export"));
        if(save){
            BufferedWriter output = null;
            try {
                File file = new File(appVariables.exportTimeline);
                if(!file.exists()){
                    file.createNewFile();
                }
                output = new BufferedWriter(new FileWriter(appVariables.exportTimeline,true));
                eventsList = new ArrayList<>(numTopics+1);
                for(int i = 0; i<numTopics && i<topicList.size(); i++){
                    TimelineEvent event = new TimelineEvent(topicList.get(i));
                    eventsList.add(event);
                    SimpleTopic topic = (SimpleTopic)event.topic;
                    output.write(topic.mainTerm+","+topic.termsList.get(0)+","+topic.termsList.get(1)+"\n");
                }
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(Timeline.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            eventsList = new ArrayList<>(numTopics+1);
            for(int i = 0; i<numTopics && i<topicList.size(); i++){
                TimelineEvent event = new TimelineEvent(topicList.get(i));
                eventsList.add(event);
            }
        }
    }
}
