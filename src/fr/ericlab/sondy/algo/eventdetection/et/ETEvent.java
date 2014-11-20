/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.eventdetection.et;

import java.io.Serializable;
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
public class ETEvent implements Comparable<ETEvent>, Serializable{
    public LinkedList<String> relevantBigrams = new LinkedList<>();
    
    @Override
    public String toString(){
        String str = "";
        for(String s : relevantBigrams){
            str += s+" ";
        }
        return str;
    }

    @Override
    public int compareTo(ETEvent o) {
        if(relevantBigrams.size() > o.relevantBigrams.size()){
            return -1;
        }else{
            if(relevantBigrams.size() < o.relevantBigrams.size()){
                return 1;
            }else{
                return 0;
            }
        }
    }
}
