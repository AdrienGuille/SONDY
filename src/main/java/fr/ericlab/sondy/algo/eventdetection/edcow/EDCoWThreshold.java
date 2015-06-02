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
package main.java.fr.ericlab.sondy.algo.eventdetection.edcow;

import org.apache.commons.math3.stat.descriptive.rank.Median;

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
public class EDCoWThreshold {

    public double mad(double [] autoCorrelationValues){
        double [] tempTable = new double[autoCorrelationValues.length];
        Median m = new Median();
        double medianValue = m.evaluate(autoCorrelationValues);
        for(int i=0 ; i<autoCorrelationValues.length ;i++){
                tempTable[i] = Math.abs(autoCorrelationValues[i] - medianValue);
        }
        return m.evaluate(tempTable); //return the median of tempTable, the equation (13) in the paper
    }

    public double theta1(double [] autoCorrelationValues, double gama){
        Median m = new Median();
        return  (m.evaluate(autoCorrelationValues) + (gama * mad(autoCorrelationValues)));				
    }

    public double[] transformMatrix(double [][] matrix){
        int a = matrix[0].length * matrix.length;
        double[] vector = new double[a];
        int v=0;
        for(int i=0; i<matrix.length; i++){
            for(int j=0; j<matrix[0].length; j++){				
                vector[v] =  matrix[i][j];
                v++;
            }
        }
        return vector;
    }

    public double theta2(double [][] crossCorrelationValues, double gama){
        double[] vecCrossCorrelation = transformMatrix(crossCorrelationValues);
        Median m = new Median();		
        return (m.evaluate(vecCrossCorrelation) + (gama * mad(vecCrossCorrelation)));		
    }	
}
