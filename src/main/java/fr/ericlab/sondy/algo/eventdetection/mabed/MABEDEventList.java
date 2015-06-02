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
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class MABEDEventList implements Serializable {
    public LinkedList<MABEDEvent> list;
    public Timestamp corpusStart;
    public int timeSliceLength;
    
    public MABEDEventList(){
        list = new LinkedList<>();
    }
        
    public Timestamp toDate(int timeSlice){
        long dateLong = corpusStart.getTime() + timeSlice*timeSliceLength*60*1000L;
        return new Timestamp(dateLong);
    }
            
    public void scoreEvolution(){
        for(int i = 1; i <= list.size(); i++){
            System.out.print(i+",");
        }
        for(MABEDEvent event : list){
            System.out.print(event.score+",");
        }
    }
    
    public void add(MABEDEvent t){
        list.add(t);
    }
    
    public void sort(){
        Collections.sort(list);
    }
    
    public int size(){
        return list.size();
    }
    
    public MABEDEvent get(int i){
        return list.get(i);
    }
    
    public void addAll(MABEDEventList tl){
        list.addAll(tl.list);
    }
}
