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
package main.java.fr.ericlab.sondy.algo.eventdetection.mabed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class MABEDEvent implements Comparable<MABEDEvent>, Serializable {
    public String mainTerm;
    public MABEDWeightedTermList relatedTerms;
    public MABEDTimeInterval I;
    public double score;
    public ArrayList<Double> anomaly;
    
    public MABEDEvent(){
        mainTerm = "noMainTerm";
        score = 0;
        relatedTerms = new MABEDWeightedTermList();
        anomaly = new ArrayList<>();
    }
    
    public MABEDEvent(String t, MABEDTimeInterval tI, double s){
        mainTerm = t;
        I = tI;
        score = s;
        relatedTerms = new MABEDWeightedTermList();
        anomaly = new ArrayList<>();
    }
    
    public MABEDEvent(String t, MABEDTimeInterval tI, double s, ArrayList a){
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
    
    public MABEDEvent merge(MABEDEvent t){
        MABEDEvent t1 = new MABEDEvent(this.mainTerm+", "+t.mainTerm, this.I, this.score);
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
    
    public MABEDEvent merge(MABEDEventList tl){
        MABEDEvent t1 = new MABEDEvent("",this.I,this.score,this.anomaly);
        String mT = this.mainTerm;
        for(MABEDEvent t : tl.list){
            mT += ", "+t.mainTerm;
        }
        t1.setMainTerm(mT);
        for(MABEDEvent t : tl.list){
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
    public int compareTo(MABEDEvent o) {
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
