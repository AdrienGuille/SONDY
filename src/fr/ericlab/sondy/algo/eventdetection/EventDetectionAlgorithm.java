/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.eventdetection;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.structure.DetectionResult;
import java.text.NumberFormat;
import java.util.Locale;
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
 *   Abstract base class that defines the skeleton of topic detection 
 *   algorithms. 
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public abstract class EventDetectionAlgorithm implements Runnable{
    /**
     *
     */
    public String algoDescription;
    /**
     *
     */
    public ObservableList<AlgorithmParameter> parameters;
    /**
     *
     */
    public NumberFormat formatter = NumberFormat.getInstance(Locale.US);
    /**
     *
     */
    public AppVariables appVariables;
    /**
     *
     */
    ObservableList<DetectionResult> results;
    /**
     *
     */
    public EventDetectionAlgorithm(){
        formatter.setMaximumFractionDigits(2);
    }
    
    /**
     *
     * @return
     */
    public abstract String getName();
    
    /**
     *
     * @return
     */
    public abstract String getReference();
        
    /**
     *
     * @param appVariables
     * @return
     */
    public abstract ObservableList<DetectionResult> apply();
    
    public ObservableList<DetectionResult> getResults(){
        return results;
    }

    @Override
    public void run() {
        apply();
    }
}
