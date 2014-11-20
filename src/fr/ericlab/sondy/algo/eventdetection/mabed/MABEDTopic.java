/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection.mabed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
public class MABEDTopic implements Comparable<MABEDTopic> {
    public String mainTerm;
    public MABEDWeightedTermList relatedTerms;
    public MABEDTimeInterval I;
    public double score;
    public ArrayList<Double> anomaly;
    
    public MABEDTopic(){
        mainTerm = "noMainTerm";
        score = 0;
        relatedTerms = new MABEDWeightedTermList();
        anomaly = new ArrayList<>();
    }
    
    public MABEDTopic(String t, MABEDTimeInterval tI, double s){
        mainTerm = t;
        I = tI;
        score = s;
        relatedTerms = new MABEDWeightedTermList();
        anomaly = new ArrayList<>();
    }
    
    public MABEDTopic(String t, MABEDTimeInterval tI, double s, ArrayList a){
        mainTerm = t;
        I = tI;
        score = s;
        relatedTerms = new MABEDWeightedTermList();
        anomaly = a;
    }
    
    public void setMainTerm(String t){
        mainTerm = t;
    }
    
    public Map<String,Object> getMainTermAttributes(){
        HashMap<String,Object> map = new HashMap<>();
        map.put("ui.class","mainTerm");
        map.put("ui.color",1);
        map.put("ui.color",1);
        map.put("I", I.timeSliceA+":"+I.timeSliceB);
        map.put("score",score);
        return map;
    }
    
    public MABEDTopic merge(MABEDTopic t){
        MABEDTopic t1 = new MABEDTopic(this.mainTerm+" "+t.mainTerm, this.I, this.score);
        for(MABEDWeightedTerm wt : this.relatedTerms.list){
            if(!t1.contains(wt.term)){
                t1.relatedTerms.add(wt);
            }
        }
        for(MABEDWeightedTerm wt : t.relatedTerms.list){
            if(!t1.contains(wt.term)){
                t1.relatedTerms.add(wt);
            }
        }
        return t1;
    }
    
    public MABEDTopic merge(MABEDTopicList tl){
        MABEDTopic t1 = new MABEDTopic("",this.I,this.score,this.anomaly);
        String mT = this.mainTerm;
        for(MABEDTopic t : tl.list){
            mT += " "+t.mainTerm;
        }
        t1.setMainTerm(mT);
        for(MABEDTopic t : tl.list){
            for(MABEDWeightedTerm wt : t.relatedTerms.list){
                    if(!t1.contains(wt.term)){
                        t1.relatedTerms.add(wt);
                    }
                }
        }
        for(MABEDWeightedTerm wt : this.relatedTerms.list){
            if(!t1.contains(wt.term)){
                t1.relatedTerms.add(wt);
            }
        }
        return t1;
    }
    
    public String toString(boolean printI){
        String str = "";
        if(printI){
            str = "["+I.toString()+"] ";
        }
        str += mainTerm+"("+score+"): ";
        for(MABEDWeightedTerm wt : relatedTerms.list){
            str += wt.term+"("+wt.weight+") ";
        }
        return str;
    }
    
    public String intervalAsString(String lang){
        
        return "";
    }
    
    public boolean contains(String term){
        return this.mainTerm.contains(term) || containsrelatedTerm(term);
    }
    
    public boolean containsrelatedTerm(String term){
        for(MABEDWeightedTerm wt : relatedTerms.list){
            if(wt.term.equals(term)){
                return true;
            }
        }
        return false;
    }
    
    public String relatedTermAsList(){
        String st = "";
        for(MABEDWeightedTerm wt : relatedTerms.list){
            st += wt.term+" ";
        }
        return st;
    }
    
    public String anomalyToString(){
        String string = "[";
        for(double d : anomaly){
            string += d+",";
        }
        string = string.substring(0,string.length());
        return string+"]";
    }

    @Override
    public int compareTo(MABEDTopic o) {
        if((o.score - this.score) == 0){
            return 0;
        }else{
            if(this.score > o.score){
                return -1;
            }else{
                return 1;
            }
        }
    }
}
