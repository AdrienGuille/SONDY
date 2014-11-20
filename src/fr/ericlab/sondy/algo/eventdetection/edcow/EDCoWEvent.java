/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.eventdetection.edcow;


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
 *   @author yue HE, Falitokiniaina RABEARISON, Département Informatique et Statistiques, Université Lumière Lyon 2
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class EDCoWEvent implements Comparable<EDCoWEvent>{
    public LinkedList<String> keywords;
    public double epsylon;
    public float startSlice;
    public float endSlice;
    
    public EDCoWEvent(LinkedList<String> keywords_, float startDay_, float endDay_){
        keywords = keywords_;
        startSlice = startDay_;
        endSlice = endDay_;
    }
    
    public EDCoWEvent(){
        keywords = new LinkedList<>();
    }
    
    public String getKeywordsAsString(){
        String str = "";
        for(String keyword : keywords){
            str += keyword+" ";
        }
        return str;
    }
    
    public String getIntervalAsString(){
    	return startSlice+";"+endSlice;
    }
    
    public double[] getInterval(float intervalDuration){
        double array[] = {(startSlice*intervalDuration)/24, (endSlice*intervalDuration)/24};
        return array;
    }

    public void setEpsylon(double epsylon) {
            this.epsylon = epsylon;
    }

    public void setStartSlice(float startDay) {
            this.startSlice = startDay;
    }

    public void setEndSlice(float endDay) {
            this.endSlice = endDay;
    }

    public double getEpsylon() {
            return epsylon;
    }

    @Override
    public int compareTo(EDCoWEvent event0) {
        if(this.epsylon < event0.epsylon){
            return -1;
        }else{
            if(this.epsylon > event0.epsylon){
                return 1;
            }else{
                return 0;
            }
        }
    }
}
