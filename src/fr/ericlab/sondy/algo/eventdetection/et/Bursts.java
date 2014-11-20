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
public class Bursts implements Comparable<Bursts>, Serializable{
    public LinkedList<Burst> list = new LinkedList<>();
    
    public double getMaxIncrease(){
        double max = 0;
        for(Burst burst : list){
            if(burst.h > max){
                max = burst.h;
            }
        }
        return max;
    }
    
    public double getTotalIncrease(){
        double sum = 0;
        for(Burst burst : list){
            sum += burst.h;
        }
        return sum;
    }

    @Override
    public int compareTo(Bursts b) {
        int comp;
        if(this.getMaxIncrease() > b.getMaxIncrease()){
            comp = 1;
        }else{
            if(this.getMaxIncrease() < b.getMaxIncrease()){
                comp = -1;
            }else{
                comp = 0;
            }
        }
        return -comp;
    }
    
    
}
