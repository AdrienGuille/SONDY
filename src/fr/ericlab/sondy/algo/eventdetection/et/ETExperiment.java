/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.eventdetection.et;

import ch.usi.inf.sape.hac.experiment.Experiment;
import fr.ericlab.sondy.core.AppVariables;
import java.util.HashMap;
import java.util.LinkedList;

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
public class ETExperiment implements Experiment {
    LinkedList<String> bigrams;
    HashMap<String,Bursts> mapBursts;
    HashMap<String,LinkedList<String>> mapCooccurences;
    AppVariables dataset;
    
    public ETExperiment(AppVariables d, LinkedList<String> keywords, HashMap<String,Bursts> mb, HashMap<String,LinkedList<String>> mc){
        bigrams = keywords;
        mapBursts = mb;
        mapCooccurences = mc;
        dataset = d;
    }

    @Override
    public int getNumberOfObservations() {
        return bigrams.size();
    }
 
 
}
