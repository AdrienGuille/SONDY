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
package main.java.fr.ericlab.sondy.algo.influenceanalysis;

import java.util.HashMap;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class LogKShellDecomposition extends InfluenceAnalysisMethod {

    public LogKShellDecomposition(){
        super();
    }
    
    @Override
    public String getName() {
        return "log-k-Shell Decomposition";
    }

    @Override
    public String getCitation() {
        return "<li><b>Log K-cores Decomposition:</b> P. Brown and J. Feng (2011) Measuring user influence on twitter using modified k-shell decomposition, In Proceedings of the Workshops of the 2011 AAAI Conference on Weblogs and Social Media (ICWSM) Workshops";
    }

    @Override
    public String getDescription() {
        return "A modified k-shell-decomposition with logarithmic mapping in order to produce fewer k-shell values";
    }

    @Override
    public void apply() {
        HashMap<Integer,String> nodesIndex = new HashMap<>(AppParameters.authorNetwork.getNodeCount()+5,1);
        HashMap<String,Integer> reverseNodesIndex = new HashMap<>(AppParameters.authorNetwork.getNodeCount()+5,1);
        int nodePos = 1;
        int[] deg = new int[AppParameters.authorNetwork.getNodeCount()+1];
        for(Node node : AppParameters.authorNetwork){
            nodesIndex.put(nodePos,node.getId());
            reverseNodesIndex.put(node.getId(),nodePos);
            deg[nodePos] = node.getInDegree();
            nodePos++;
        }
        int n,d,md,i,start,num;
        int v,u,w,du,pu,pw;
        int[] vert = new int[AppParameters.authorNetwork.getNodeCount()+1];
        int[] pos = new int[AppParameters.authorNetwork.getNodeCount()+1];
        int[] bin = new int[AppParameters.authorNetwork.getNodeCount()];
        n = AppParameters.authorNetwork.getNodeCount();
        md = 0;
        for( v = 1; v<=n ; v++){
            d = AppParameters.authorNetwork.getNode(nodesIndex.get(v)).getInDegree();
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
            Node nodeV = AppParameters.authorNetwork.getNode(nodesIndex.get(v));
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
            rankedUsers.add(nodesIndex.get(iDeg),logk);
        }
    }

}
