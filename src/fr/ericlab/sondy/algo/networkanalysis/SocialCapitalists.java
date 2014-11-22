/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.networkanalysis;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.core.AppVariables;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import javafx.collections.FXCollections;
import org.graphstream.graph.Edge;
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

public class SocialCapitalists extends NetworkAnalysisAlgorithm{
    double threshold = 0.74;
    double rankScale = 10;
    
    public SocialCapitalists(){
        algoDescription = "Identifies users who might be social capitalists in a directed social network";   
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("threshold",""),new AlgorithmParameter("rankScale",""));
    }
    
    public String getName(){
        return "Social Capitalists";
    }

    @Override
    public HashMap<String, Integer> apply(DefaultGraph graph, AppVariables appVariables) {
        if(parameters.get(0).getValue()!=null && !parameters.get(0).getValue().equals("")){
            threshold = Integer.parseInt(parameters.get(0).getValue());
        }
        if(parameters.get(1).getValue()!=null && !parameters.get(1).getValue().equals("")){
            rankScale = Integer.parseInt(parameters.get(1).getValue());
        }
        int nbNodes = graph.getNodeCount();
        HashMap<String,Integer> nodesRank = new HashMap<>(nbNodes+5,1);
        for (Node node : graph) {
            node.removeAttribute("ui.hide");
            Collection<Edge> enteringEdgeSet = node.getEnteringEdgeSet();
            HashSet<String> A = new HashSet<>();
            for(Edge edge : enteringEdgeSet){
                A.add(edge.getSourceNode().getId());
            }
            Collection<Edge> leavingEdgeSet = node.getLeavingEdgeSet();
            HashSet<String> B = new HashSet<>();
            double intersection = 0;
            for(Edge edge : leavingEdgeSet){
                B.add(edge.getTargetNode().getId());
                if(A.contains(edge.getTargetNode().getId())){
                    intersection++;
                }
            }
            double overlap = intersection/Math.min(A.size(),B.size());
            overlap = (overlap>threshold)?overlap *rankScale:0;
            nodesRank.put(node.getId(),(int)(overlap));
        }
        return nodesRank;
    }

    @Override
    public String getReference() {
        return "<li><b>Social Capitalists:</b> N. Dugué and A. Perez. Social capitalists on Twitter: detection, evolution and behavioral analysis, <i>Social Network Analysis and Mining</i>, vol. 4(1) pp. 178-191, 2014.</li>";
    }
}