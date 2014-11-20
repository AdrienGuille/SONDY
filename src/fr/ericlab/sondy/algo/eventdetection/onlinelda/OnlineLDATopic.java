package fr.ericlab.sondy.algo.eventdetection.onlinelda;

import java.text.DecimalFormat;
import java.util.ArrayList;

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
public class OnlineLDATopic implements Comparable<OnlineLDATopic> {
    public ArrayList<String> terms;
    public ArrayList<Double> probabilities;
    public int timeSlice;
    public double score;
    public int id;
    
    public OnlineLDATopic(){
        terms = new ArrayList<>();
        probabilities = new ArrayList<>();
    }
    
    public String printTerms(int limit){
        String str = "";
        DecimalFormat df = new DecimalFormat("0.000");
        for(int i = 0; (i < terms.size()) && (i < limit); i++){
            str += terms.get(i)+" ("+df.format(probabilities.get(i))+"), ";
        }
        return (str.length()>2)?str.substring(0,str.length()-2):"";
    }
    
    @Override
    public int compareTo(OnlineLDATopic o) {
        return (int) (o.score*10000-this.score*10000);
    }
}
