/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.networkanalysis;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.core.AppVariables;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;

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

public class ReplayDiffusion extends NetworkAnalysisAlgorithm {
    double speed = 2;
    
    public ReplayDiffusion(){
        algoDescription = "Replays the activation sequence";   
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("speed",""));
    }
    
    public HashMap<String,Integer> apply(DefaultGraph graph, AppVariables appVariables){
        if(parameters.get(0).getValue()!=null && !parameters.get(0).getValue().equals("")){
            speed = Double.parseDouble(parameters.get(0).getValue());
        }

        appVariables.addLogEntry("[network analysis] replaying the activation sequence");
        int nbNodes = graph.getNodeCount();
        HashMap<String,Integer> nodesRank = new HashMap<>(nbNodes+5,1);
        int firstTimeslice = appVariables.endTimeSlice;
        for(Node node : graph){
            if((int)node.getAttribute("timeslice") < firstTimeslice){
                firstTimeslice = node.getAttribute("timeslice");
            }
        }
        for(Node node : graph){
            nodesRank.put(node.getId(), (int)node.getAttribute("timeslice")-firstTimeslice);
            node.removeAttribute("ui.hide");
            node.setAttribute("ui.size",12);
            try {
                Thread.sleep((long) (100/speed));
            } catch (InterruptedException ex) {
                Logger.getLogger(ReplayDiffusion.class.getName()).log(Level.SEVERE, null, ex);
            }
            node.setAttribute("ui.size",6);
        }
        return nodesRank;
    }

    public String getName() {
        return "Replay Diffusion";
    }

    @Override
    public String getReference() {
        return "";
    }
}
