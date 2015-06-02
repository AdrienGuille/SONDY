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
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class MABEDWeightedTermList  implements Serializable {
    public LinkedList<MABEDWeightedTerm> list;
    
    public MABEDWeightedTermList(){
        list = new LinkedList<>();
    }
    
    public void add(MABEDWeightedTerm t){
        list.add(t);
    }
    
    public void sort(){
        Collections.sort(list);
    }
    
    public int size(){
        return list.size();
    }
    
    public MABEDWeightedTerm get(int i){
        return list.get(i);
    }
    
    public void addAll(MABEDWeightedTermList tl){
        list.addAll(tl.list);
    }
    
    public String toString(){
        sort();
        String str = "related terms: ";
        DecimalFormat df = new DecimalFormat("0.00");
        for(MABEDWeightedTerm t: list){
            str += t.term+" ("+df.format(t.weight)+"), ";
        }
        return str.substring(0,str.length()-2);
    }
}
