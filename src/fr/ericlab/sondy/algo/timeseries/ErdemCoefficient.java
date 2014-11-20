/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.timeseries;

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

public class ErdemCoefficient extends CorrelationCoefficient {
    
    @Override
    public float compute(float[] ref, float[] comp, int a, int b){
        double scores1[] = new double[b-a+1], scores2[] = new double[b-a+1]; 
        for(int i = a; i <= b; i++){
            scores1[i-a] = ref[i];
            scores2[i-a] = comp[i];
        }
        double result = 0;
        double A12 = 0, A1 = 0, A2 = 0;
        for(int i=2;i<scores1.length;i++){
            A12 += (scores1[i]-scores1[i-1])*(scores2[i]-scores2[i-1]);
            A1 += (scores1[i]-scores1[i-1])*(scores1[i]-scores1[i-1]);
            A2 += (scores2[i]-scores2[i-1])*(scores2[i]-scores2[i-1]);
        }
        A1 = Math.sqrt(A1/(scores1.length-1));
        A2 = Math.sqrt(A2/(scores1.length-1));
        result = A12/((scores1.length-1)*A1*A2);
        return (float) (result+1)/2;
    }
}
