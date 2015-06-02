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
package main.java.fr.ericlab.sondy.algo.eventdetection.edcow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.VectorEntry;
import ch.epfl.lis.jmod.Jmod;
import ch.epfl.lis.jmod.JmodNetwork;
import ch.epfl.lis.jmod.JmodSettings;
import ch.epfl.lis.jmod.modularity.community.Community;
import ch.epfl.lis.jmod.modularity.community.RootCommunity;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.EdgeFactory;
import ch.epfl.lis.networks.NetworkException;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.NodeFactory;
import ch.epfl.lis.networks.Structure;

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
 *   @author Yue HE, Falitokiniaina RABEARISON, Département Informatique et Statistiques, Université Lumière Lyon 2
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class EDCoWModularityDetection {
    List<Node> nodeList;	
    ArrayList<Community> arrayCommunities;
    Structure<Node, Edge<Node>> structure;
    LinkedList<EDCoWEvent> events;
    float startSlice;
    float endSlice;


    // Add two arguments : start and end index of the frequency array
    public EDCoWModularityDetection(LinkedList<EDCoWKeyword> keywords, double[][] correlations, float startDay, float endDay) throws NetworkException, Exception{
        this.startSlice = startDay;
        this.endSlice = endDay;

        // instantiate structure
        NodeFactory<Node> nodeFactory = new NodeFactory<>(new Node());
        EdgeFactory<Edge<Node>> edgeFactory = new EdgeFactory<>(new Edge<Node>());
        structure = new Structure<>(nodeFactory, edgeFactory);
        // Adding weighted edges between keywords
        for(int i = 0; i < keywords.size(); i++){
            for(int j = i; j < keywords.size(); j++){
                if(i != j && correlations[i][j] > 0.1){
                    if(!structure.containsNode(keywords.get(i).getKeyWord()))
                        structure.addNode(keywords.get(i).getKeyWord());
                    if(!structure.containsNode(keywords.get(j).getKeyWord()))
                        structure.addNode(keywords.get(j).getKeyWord());
                    structure.addEdge(new Edge(structure.getNode(keywords.get(i).getKeyWord()),structure.getNode(keywords.get(j).getKeyWord()),correlations[i][j]));
                }
            }
        }
        nodeList = structure.getNodesOrderedByNames();
        System.out.println("Structure between slices "+startSlice+" and "+endSlice+": "+structure.getSize()+" nodes and " + structure.getNumEdges()+" edges");

        if(structure.getNumEdges()>0){
            // instantiate JmodNetwork
            JmodNetwork network = new JmodNetwork(structure);
            JmodSettings settings = JmodSettings.getInstance();
            settings.setUseMovingVertex(true);
            settings.setUseGlobalMovingVertex(true);
            // run modularity detection
            Jmod jmod = new Jmod();		 
            jmod.runModularityDetection(network);	
            events = new LinkedList<>();
            RootCommunity rc = jmod.getRootCommunity();
            arrayCommunities = rc.getIndivisibleCommunities();
        }
    }

    public double computeEdgesWeight(Community c, List<Node> nodeList){		
        double totalWeight =0;
        if(c.getChild1() == null){
            DenseVector nodesC = c.getVertexIndexes();			
            for(VectorEntry ve : nodesC){
                int id = (int)ve.get();												
                Set<Edge<Node>> edges = structure.getEdges(nodeList.get(id));
                for(Edge e:edges){
                    if(e.getSource().equals(nodeList.get(id)))
                            totalWeight += e.getWeight(); 
                }
            }
        }else{
            computeEdgesWeight(c.getChild1(),nodeList);
            computeEdgesWeight(c.getChild2(),nodeList);
        }
        return totalWeight;
    }

    public void saveEventFromCommunity(Community c){
        DenseVector nodesC = c.getVertexIndexes();
        if(nodesC.size() > 1){
            EDCoWEvent event = new EDCoWEvent();
            for(VectorEntry ve : nodesC){
                int id = (int)ve.get();
                String keyword = nodeList.get(id).getName();
                event.keywords.add(keyword);
            }
            event.setEpsylon(computeE(c));
            event.setStartSlice(startSlice);
            event.setEndSlice(endSlice);
            events.add(event);
        }
    }

    public void explore(Community c){
        if(c.getChild1() == null){
            DenseVector nodesC = c.getVertexIndexes();
            EDCoWEvent event = new EDCoWEvent();
            for(VectorEntry ve : nodesC){
                int id = (int)ve.get();
                event.keywords.add(nodeList.get(id).getName());
                event.setEpsylon(computeE(c));
                event.setStartSlice(startSlice);
                event.setEndSlice(endSlice);				
            }
            events.add(event);
        }else{
            explore(c.getChild1());
            explore(c.getChild2());
        }
    }	

    public ArrayList<Community> getCommunitiesFiltered(double thresholdE) {
        ArrayList<Community> ComFiltered = new ArrayList<>();
        for(Community c:arrayCommunities){				
            double tempComE= computeE(c);
            if(tempComE>thresholdE) ComFiltered.add(c);
        }				
        return ComFiltered;
    }

    private double computeE(Community c) {
        int n = c.getCommunitySize();
        double totalWeight =  computeEdgesWeight(c,nodeList);
        double e = totalWeight * (Math.exp(1.5*n)/2*n);
        return e;
    }

    public ArrayList<Community> getArrayCommunities(){
        return arrayCommunities;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public LinkedList<EDCoWEvent> getEvents(){
        return events;
    }
}
