/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection.mabed;

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
public class MABEDWeightedTerm implements Comparable<MABEDWeightedTerm>{
    public String term;
    public double weight;
    
    public MABEDWeightedTerm(String t){
        term = t;
        weight = 0;
    }
    
    public MABEDWeightedTerm(String t, double w){
        term = t;
        weight = w;
    }
    
    @Override
    public int compareTo(MABEDWeightedTerm o) {
        if(o.weight-this.weight<0){
            return -1;
        }else{
            if(o.weight-this.weight>0){
                return 1;
            }else{
                return 0;
            }
        }
    }
}
