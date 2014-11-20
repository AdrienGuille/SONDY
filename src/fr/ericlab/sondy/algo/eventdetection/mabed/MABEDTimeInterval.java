/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection.mabed;

import java.util.HashSet;

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
public class MABEDTimeInterval implements Comparable {
    public int timeSliceA;
    public int timeSliceB;
           
    public MABEDTimeInterval(int a, int b){
        timeSliceA = a;
        timeSliceB = b;
    }
    
    public MABEDTimeInterval(String str){
        String[] split = str.split(":");
        timeSliceA = Integer.parseInt(split[0]);
        timeSliceB = Integer.parseInt(split[1]);
    }
    
    public String toString(){
        return timeSliceA+":"+timeSliceB;
    }
    
    public double intersection(MABEDTimeInterval ti){
        HashSet<Integer> set1 = new HashSet<>();
        HashSet<Integer> set2 = new HashSet<>();
        int intersectionSize = 0;
        for(int i = this.timeSliceA; i <= this.timeSliceB; i++){
            set1.add(i);
        }
        for(int j = ti.timeSliceA; j <= ti.timeSliceB; j++){
            set2.add(j);
        }
        for(int k : set1){
            if(set2.contains(k)){
                intersectionSize++;
            }
        }
        return intersectionSize;
    }
    
    public double intersectionProportion(MABEDTimeInterval ti){
        return intersection(ti)/(this.timeSliceB-this.timeSliceA+1);
    }
    
    @Override
    public int compareTo(Object o) {
        MABEDTimeInterval point = (MABEDTimeInterval)o;
        if(this.timeSliceA > point.timeSliceA){
            return 1;
        }else{
            if(this.timeSliceA == point.timeSliceA){
                return 0;
            }else{
                return -1;
            }
        }
    }
}
