/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection.mabed;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;

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
public class MABEDWeightedTermList {
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
    
    @Override
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
