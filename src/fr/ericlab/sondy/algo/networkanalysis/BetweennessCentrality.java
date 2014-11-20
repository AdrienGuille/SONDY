/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.networkanalysis;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.core.AppVariables;
import java.util.HashMap;
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

public class BetweennessCentrality extends NetworkAnalysisAlgorithm{
    
    
    public BetweennessCentrality(){
        algoDescription = "Computes the number of shortest paths from all nodes to all others that pass through each node";   
        parameters = FXCollections.observableArrayList();
    }
    
    public String getName(){
        return "Betweenness Centrality";
    }

    @Override
    public HashMap<String, Integer> apply(DefaultGraph graph, AppVariables appVariables) {
        long startNanoTime = System.nanoTime();
        
        org.graphstream.algorithm.BetweennessCentrality bcb = new org.graphstream.algorithm.BetweennessCentrality();
        bcb.init(graph);
        bcb.compute();

        int nbNodes = graph.getNodeCount();
        HashMap<String,Integer> nodesRank = new HashMap<>(nbNodes+5,1);
        for (Node node : graph) {
            node.removeAttribute("ui.hide");
            double rank = node.getAttribute("Cb");
            nodesRank.put(node.getId(),(int)(rank));
        }
        long endNanoTime = System.nanoTime();
        long elapsedNanoTime = endNanoTime - startNanoTime;
        double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
        appVariables.addLogEntry("[network analysis] computed betweenness centrality, "+graph.getNodeCount()+" nodes and "+graph.getEdgeCount()+" edges, in "+formatter.format(elaspedSecondTime)+"s");
        return nodesRank;
    }

    @Override
    public String getReference() {
        return "<li><b>Betweenness Centrality:</b> L. Freeman. A set of measures of centrality based on betweenness, <i>In Sociometry (40)</i>, pp. 35-41, 1977.</li>";
    }
}