/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.networkanalysis;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.core.AppVariables;
import java.util.HashMap;
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

public class LogKShellDecomposition extends NetworkAnalysisAlgorithm {
    
    public LogKShellDecomposition(){
        algoDescription = "A modified k-shell decomposition for measuring user influence";   
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("directed",""));
    }
    
    public HashMap<String,Integer> apply(DefaultGraph graph, AppVariables appVariables){
        long startNanoTime = System.nanoTime();
        HashMap<Integer,String> nodesIndex = new HashMap<>(graph.getNodeCount()+5,1);
        HashMap<String,Integer> reverseNodesIndex = new HashMap<>(graph.getNodeCount()+5,1);
        int nodePos = 1;
        int[] deg = new int[graph.getNodeCount()+1];
        for(Node node : graph.getNodeSet()){
            node.removeAttribute("ui.hide");
            nodesIndex.put(nodePos,node.getId());
            reverseNodesIndex.put(node.getId(),nodePos);
            deg[nodePos] = node.getInDegree();
            nodePos++;
        }
        int n,d,md,i,start,num;
        int v,u,w,du,pu,pw;
        int[] vert = new int[graph.getNodeCount()+1];
        int[] pos = new int[graph.getNodeCount()+1];
        int[] bin = new int[graph.getNodeCount()];
        n = graph.getNodeCount();
        md = 0;
        for( v = 1; v<=n ; v++){
            d = graph.getNode(nodesIndex.get(v)).getInDegree();
            if(d > md){
                md = d;
            }
        }
        for( d = 0; d<=md ; d++){
            bin[d] = 0;
        }
        for( v = 1; v<= n; v++){
            bin[deg[v]]++;
        }
        start = 1;
        for( d = 0; d<=md ; d++){
            num = bin[d];
            bin[d] = start;
            start += num;
        }
        for( v = 1; v<=n ; v++){
            pos[v] = bin[deg[v]];
            vert[pos[v]] = v;
            bin[deg[v]]++;
        }
        for( d = md ; d>=1 ; d--){
            bin[d] = bin[d-1];
        }
        bin[0] = 1;
        for( i = 1 ; i<=n ; i++){
            v = vert[i];
            Node nodeV = graph.getNode(nodesIndex.get(v));
            for(Edge currentEdge : nodeV.getEnteringEdgeSet()){
                Node nodeU = currentEdge.getSourceNode();
                u = reverseNodesIndex.get(nodeU.getId());
                if(deg[u] > deg[v]){
                    du = deg[u];
                    pu =pos[u];
                    pw = bin[du];
                    w = vert[pw];
                    if(u == w){
                        pos[u] = pw;
                        pos[w] = pu;
                        vert[pu] = w;
                        vert[pw] = u;
                    }
                    bin[du]++;
                    deg[u]--;
                }
            }
        }
        HashMap<String,Integer> nodesCore = new HashMap<>(n+5,1);
        for( int iDeg = 1; iDeg < deg.length; iDeg++){
            int logk = (int) (Math.log(deg[iDeg]+1)/Math.log(2));
            nodesCore.put(nodesIndex.get(iDeg),logk);
        }
        long endNanoTime = System.nanoTime();
        long elapsedNanoTime = endNanoTime - startNanoTime;
        double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
        appVariables.addLogEntry("[network analysis] computed log k-shell decomposition, "+graph.getNodeCount()+" nodes and "+graph.getEdgeCount()+" edges, in "+formatter.format(elaspedSecondTime)+"s");
        return nodesCore;
    }

    public String getName() {
        return "Log K-Shell Decomposition";
    }

    @Override
    public String getReference() {
        return "<li><b>Log K-cores Decomposition:</b> P. Brown and J. Feng. Measuring user influence on twitter using modified k-shell decomposition, <i>In ICWSM ’11 Workshops</i>, 2011.</li>";
    }
}
