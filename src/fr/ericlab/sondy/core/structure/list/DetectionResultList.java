/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core.structure.list;

import fr.ericlab.sondy.core.structure.DetectionResult;
import java.util.LinkedList;
import javafx.collections.FXCollections;
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

public class DetectionResultList {
    /**
     *
     */
    public LinkedList<DetectionResult> list = new LinkedList<>();
    /**
     *
     */
    public ObservableList<DetectionResult> observableList;
    
    /**
     *
     * @param ol
     */
    public DetectionResultList(ObservableList<DetectionResult> ol){
            observableList = ol;
            for(DetectionResult dr : observableList){
                list.add(dr);
            }
        
    }

    /**
     *
     */
    public DetectionResultList() {
        observableList = FXCollections.observableArrayList();
    }
    
    /**
     *
     */
    public void setFullList(){
        observableList.addAll(list);
    }
    
    /**
     *
     * @param term
     */
    public void filterList(String term){
        ObservableList<DetectionResult> newObservableList = FXCollections.observableArrayList();
        for(DetectionResult dr : list){
            if(dr.getMainTerm().contains(term)){
                newObservableList.add(dr);
            }
        }
        observableList = newObservableList;
    }
}
