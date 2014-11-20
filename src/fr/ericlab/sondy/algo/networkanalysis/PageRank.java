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

public class PageRank extends NetworkAnalysisAlgorithm{
    double dampingFactor = 0.85;
    double rankCoefficient = 3;
    
    public PageRank(){
        algoDescription = "Computes the probability that a 'random surfer' visits each node";   
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("dampingFactor",""),new AlgorithmParameter("rankCoefficient",""));
    }
    
    public String getName(){
        return "Page Rank";
    }

    @Override
    public HashMap<String, Integer> apply(DefaultGraph graph, AppVariables appVariables) {
        if(parameters.get(0).getValue()!=null && !parameters.get(0).getValue().equals("")){
            dampingFactor = Integer.parseInt(parameters.get(0).getValue());
        }
        if(parameters.get(1).getValue()!=null && !parameters.get(1).getValue().equals("")){
            rankCoefficient = Integer.parseInt(parameters.get(1).getValue());
        }
        long startNanoTime = System.nanoTime();
        org.graphstream.algorithm.PageRank pageRank = new org.graphstream.algorithm.PageRank();
        pageRank.setDampingFactor(dampingFactor);
        pageRank.init(graph);
        pageRank.compute();
        int nbNodes = graph.getNodeCount();
        HashMap<String,Integer> nodesRank = new HashMap<>(nbNodes+5,1);
        for (Node node : graph) {
            node.removeAttribute("ui.hide");
            double rank = pageRank.getRank(node);
            nodesRank.put(node.getId(),(int)(rank*nbNodes*rankCoefficient));
        }
        long endNanoTime = System.nanoTime();
        long elapsedNanoTime = endNanoTime - startNanoTime;
        double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
        appVariables.addLogEntry("[network analysis] computed Page Rank, "+graph.getNodeCount()+" nodes and "+graph.getEdgeCount()+" edges, in "+formatter.format(elaspedSecondTime)+"s");
        return nodesRank;
    }

    @Override
    public String getReference() {
        return "<li><b>Page-Rank:</b> L. Page, S. Brin, R. Motwani, and T. Winograd. The pagerank citation ranking: Bringing order to the web, <i>In Proceedings of the 7th International World Wide Web Conference</i>, pp. 161–172, 1998.</li>";
    }
}