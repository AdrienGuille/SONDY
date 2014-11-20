/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection.mabed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.ConnectedComponents.ConnectedComponent;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

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
public class MABEDTopicGraph {
    public Graph graph;
    Graph redundancyGraph;
    public HashMap<String,MABEDTopic> redundantTopics;
    double maximumScore;
    
    static Double _SIGMA_;
    final static int _MAINTERM_WEIGHT_ = 15;
    final static int _relatedTerm_WEIGHT_ = 8;
    final static int _EDGE_WEIGHT_ = 20;
    final static int _EDGE_SIZE_FACTOR = 8;
    final static float _GRAPH_CONVERGENCE_SPEED_ = 1.5f;
    final static String _GRAPH_CSS_ = 
                "graph {\n"+
                "    fill-mode: plain;\n"+
                "    fill-color: white;\n"+
                "}\n"+
                "edge {\n" +
                "    shape: blob;\n" +
                "    fill-mode: dyn-plain;\n" +
                "    fill-color: rgba(0,0,0,88),rgba(0,0,0,200);\n" +
                "    arrow-size: 0px,0px;\n" +
                "    size-mode: dyn-size;\n"+
                "}\n" +
                "node{\n" +
                "    text-color: rgba(0,0,0,200);\n"+
                "    text-background-mode: rounded-box;\n"+
                "    text-background-color: rgba(200,200,200,200);\n"+
                "    text-padding: 2px;\n"+
                "    text-alignment: at-right;\n"+
                "    stroke-mode: plain; \n"+
                "    fill-mode: dyn-plain;\n"+
                "    fill-color: rgb(108,128,146), rgb(88, 88, 88); \n"+
                "    size-mode: dyn-size;\n"+
                "}\n"+
                "node:clicked{\n"+
                "    fill-color: black;\n"+
                "}";
    
    public MABEDTopicGraph(double ms, Double sigma){
        _SIGMA_ = sigma;
        graph = new SingleGraph("");
        redundancyGraph = new SingleGraph("");
        maximumScore = ms;
        redundantTopics = new HashMap<>();
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        graph.addAttribute("ui.stylesheet", _GRAPH_CSS_);
        graph.addAttribute("layout.force", _GRAPH_CONVERGENCE_SPEED_);
        graph.addAttribute("layout.quality", 4);
        graph.addAttribute("layout.stabilization-limit", 0.90);
    }
    
    public double overlapSignificance(MABEDTopic t0, MABEDTopic t1){
        double intersection = t0.I.intersection(t1.I);
        double I0 = t0.I.timeSliceB - t0.I.timeSliceA, I1 = t1.I.timeSliceB - t1.I.timeSliceA;
        double overlapDegree = intersection/Math.min(I0,I1);
        return overlapDegree;
    }
    
    public int addTopic(MABEDTopic topic){
        int added = 0;
        boolean redundant = false;
        if(graph.getNode(topic.mainTerm) != null){
            for(MABEDWeightedTerm wt : topic.relatedTerms.list){
                Node wtNode = graph.getNode(wt.term);
                if(wtNode != null){
                    if(wtNode.getAttribute("ui.class").equals("mainTerm") && wtNode.hasEdgeFrom(graph.getNode(topic.mainTerm))){
                        MABEDTopic topic1 = getTopic(wtNode);
                        double intersection = Math.max(topic.I.intersectionProportion(topic1.I), topic1.I.intersectionProportion(topic.I));
                        if(intersection > _SIGMA_){
                            redundant = true;
                            redundantTopics.put(topic.mainTerm,topic);
                            redundantTopics.put(topic1.mainTerm,topic1);
                            // new way of managing redundancy
                            if(redundancyGraph.getNode(topic1.mainTerm) == null){
                                redundancyGraph.addNode(topic1.mainTerm);
                            }
                            if(redundancyGraph.getNode(topic.mainTerm) == null){
                                redundancyGraph.addNode(topic.mainTerm);
                            }
                            if(redundancyGraph.getEdge(topic.mainTerm+"-"+topic1.mainTerm) == null){
                                redundancyGraph.addEdge(topic.mainTerm+"-"+topic1.mainTerm, topic.mainTerm, topic1.mainTerm, false);
                            }
                        }
                    }
                }
            }
        }
        if(!redundant){
            if(topic.mainTerm != null){
                if(graph.getNode(topic.mainTerm) == null){
                    graph.addNode(topic.mainTerm);
                    graph.getNode(topic.mainTerm).addAttribute("layout.weight", _MAINTERM_WEIGHT_);
                }
                graph.getNode(topic.mainTerm).addAttributes(topic.getMainTermAttributes());
                graph.getNode(topic.mainTerm).setAttribute("ui.size",20+(topic.score/maximumScore)*10);
//                graph.getNode(topic.mainTerm).addAttribute("ui.label", "["+dataset.toDate(topic.I.timeSliceA)+"::"+dataset.toDate(topic.I.timeSliceB)+"]:"+topic.mainTerm);
                graph.getNode(topic.mainTerm).addAttribute("anomaly",topic.anomaly);
                for(MABEDWeightedTerm wt : topic.relatedTerms.list){
                    if(wt.term != null){
                        if(graph.getNode(wt.term)==null){
                            graph.addNode(wt.term);
                            graph.getNode(wt.term).addAttribute("ui.label", wt.term);
                            graph.getNode(wt.term).setAttribute("ui.class","relatedTerm");
                            graph.getNode(wt.term).addAttribute("layout.weight", _relatedTerm_WEIGHT_);
                            graph.getNode(wt.term).addAttribute("ui.size",15);
                        }
                        graph.addEdge("["+topic.I.timeSliceA+":"+topic.I.timeSliceB+"]"+topic.mainTerm+"-"+wt.term,wt.term,topic.mainTerm,true);
                        graph.getEdge("["+topic.I.timeSliceA+":"+topic.I.timeSliceB+"]"+topic.mainTerm+"-"+wt.term).addAttribute("ui.size", wt.weight*_EDGE_SIZE_FACTOR);
                        graph.getEdge("["+topic.I.timeSliceA+":"+topic.I.timeSliceB+"]"+topic.mainTerm+"-"+wt.term).addAttribute("weight", wt.weight);
                        graph.getEdge("["+topic.I.timeSliceA+":"+topic.I.timeSliceB+"]"+topic.mainTerm+"-"+wt.term).addAttribute("layout.weight", _EDGE_WEIGHT_);
                        graph.getEdge("["+topic.I.timeSliceA+":"+topic.I.timeSliceB+"]"+topic.mainTerm+"-"+wt.term).addAttribute("ui.color", 1);
                    }
                }
            }
            added = 1;
        }
        return added;
    }
    
    public MABEDTopicList identifyConnectedComponents(){
        ConnectedComponents ccs = new ConnectedComponents(redundancyGraph);
        ccs.setCountAttribute("component");
        int i = 0;
        MABEDTopicList globalTopics = new MABEDTopicList();
        for (ConnectedComponent cc : ccs) {
            MABEDTopicList ccTopics = new MABEDTopicList();
            for(Node node : cc.getEachNode()){
                MABEDTopic topic = redundantTopics.get(node.getId());
                ccTopics.add(topic);
                removeTopic(topic);
            }
            ccTopics.sort();
            MABEDTopic mainTopic = ccTopics.list.pop();
            MABEDTopic globalTopic = mainTopic.merge(ccTopics);
            globalTopics.add(globalTopic);
            addTopic(globalTopic);
            i++;
        }
        return globalTopics;
    }
    
    public void removeTopic(MABEDTopic topic){
        Node mainNode = graph.getNode(topic.mainTerm);
        if(mainNode != null){
            if(mainNode.getAttribute("ui.class").equals("mainTerm")){
                Collection<Edge> edges = mainNode.getEnteringEdgeSet();
                if(edges != null){
                    for(Edge edge : edges){
                        if(edge != null){
                            // remove obsolete edge
                            graph.removeEdge(edge);
                            if(edge.getSourceNode().getDegree() == 0){
                                graph.removeNode(edge.getSourceNode());
                            }
                        }
                    }
                }
                if(mainNode.getOutDegree() == 0){
                    graph.removeNode(mainNode);
                }else{
                    mainNode.setAttribute("ui.class", "relatedTerm");
                }
            }
        }
    }
    
    public void replaceTopic(Node nodeT0, MABEDTopic t1){
        nodeT0.setAttribute("ui.class","relatedTerm");
        Collection<Edge> edges = nodeT0.getEnteringEdgeSet();
        graph.addNode(t1.mainTerm);
        graph.getNode(t1.mainTerm).addAttributes(t1.getMainTermAttributes());
        graph.getNode(t1.mainTerm).setAttribute("ui.size",20+(t1.score/maximumScore)*10);
//        graph.getNode(t1.mainTerm).addAttribute("ui.label", "["+dataset.toDate(t1.I.timeSliceA)+"::"+dataset.toDate(t1.I.timeSliceB)+"]:"+t1.mainTerm);        
        graph.getNode(t1.mainTerm).setAttribute("anomaly",t1.anomaly);
        if(edges != null){
            for(Edge edge : edges){
                if(edge != null){
                    // remove obsolete edge
                    graph.removeEdge(edge);
                    // add edge toward the new main term
                    if(!t1.mainTerm.contains(edge.getSourceNode().getId())){
                        String edgeId = "["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+edge.getSourceNode().getId();
                        graph.addEdge(edgeId,edge.getSourceNode().getId(),t1.mainTerm,true);
                        graph.getEdge(edgeId).addAttribute("ui.size", edge.getAttribute("ui.size"));
                        graph.getEdge(edgeId).addAttribute("weight", edge.getAttribute("weight"));
                        graph.getEdge(edgeId).addAttribute("layout.weight", _EDGE_WEIGHT_);
                        graph.getEdge(edgeId).addAttribute("ui.color", 1);
                    }
                    if(edge.getSourceNode().getDegree() == 0){
                        graph.removeNode(edge.getSourceNode());
                    }
                }
            }
        }
        graph.removeNode(nodeT0);
        for(MABEDWeightedTerm wt : t1.relatedTerms.list){
            if(wt.term != null){
                if(graph.getNode(wt.term)==null){
                    graph.addNode(wt.term);
                    graph.getNode(wt.term).addAttribute("ui.label", wt.term);
                    graph.getNode(wt.term).setAttribute("ui.class","relatedTerm");
                    graph.getNode(wt.term).addAttribute("layout.weight", _relatedTerm_WEIGHT_);
                    graph.getNode(wt.term).addAttribute("ui.size",15);
                }
                if(graph.getEdge("["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+wt.term) == null){
                    graph.addEdge("["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+wt.term,wt.term,t1.mainTerm,true);
                }
                graph.getEdge("["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+wt.term).addAttribute("ui.size", wt.weight*_EDGE_SIZE_FACTOR);
                graph.getEdge("["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+wt.term).addAttribute("weight", wt.weight);
                graph.getEdge("["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+wt.term).addAttribute("layout.weight", _EDGE_WEIGHT_);
                graph.getEdge("["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+wt.term).addAttribute("ui.color", 1);
            }
        }
//        if(nodeT0.getDegree() == 0){
//            System.out.println("   - "+nodeT0.getId()+" is a leaf node.");
//            graph.removeNode(nodeT0);
//        } 
   }
    
    public MABEDTopic getTopic(Node mainNode){
        MABEDTopic topic = new MABEDTopic();
        if(mainNode.getAttribute("ui.class")!=null && mainNode.getAttribute("ui.class").equals("mainTerm")){
            topic = new MABEDTopic(mainNode.getId(), new MABEDTimeInterval((String) mainNode.getAttribute("I")), (double) mainNode.getAttribute("score"), (ArrayList<Double>) mainNode.getAttribute("anomaly"));
            for(Edge edge : mainNode.getEnteringEdgeSet()){
                topic.relatedTerms.add(new MABEDWeightedTerm(edge.getSourceNode().getId(), (double) edge.getAttribute("weight")));
            }
        }
        return topic;
    }
    
    public MABEDTopicList toTopicList(){
        MABEDTopicList topics = new MABEDTopicList();
        for(Node node : graph){
            if(node.getAttribute("ui.class")!=null && node.getAttribute("ui.class").equals("mainTerm")){
                MABEDTopic topic = new MABEDTopic(node.getId(), new MABEDTimeInterval((String) node.getAttribute("I")), (double) node.getAttribute("score"), (ArrayList<Double>) node.getAttribute("anomaly"));
                for(Edge edge : node.getEnteringEdgeSet()){
                    topic.relatedTerms.add(new MABEDWeightedTerm(edge.getSourceNode().getId(), (double) edge.getAttribute("weight")));
                }
                topics.add(topic);
            }
        }
        topics.sort();
        return topics;
    }
    
}
