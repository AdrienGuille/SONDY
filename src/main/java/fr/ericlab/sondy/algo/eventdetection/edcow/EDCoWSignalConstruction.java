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
public class EDCoWSignalConstruction {
	
    double[] nwt; //number of tweets which contain word w as INPUT
    double[] nt; //number of all tweets in the same period of time as INPUT
    double[] sw; //signal in the first step
    double[] sw2; // signal in the second step
    int delta; // size of sliding window

    public void firstSignalConstruction(double[] nwt, double [] nt){
        this.nwt = nwt;
        this.nt = nt;
        sw = new double[nwt.length];

        EDCoWVector ni = new EDCoWVector(nt);
        EDCoWVector nwi = new EDCoWVector(nwt);
        double tempRatio = ni.getSum()/nwi.getSum();

        for(int i=0; i<nwt.length ;i++){
                if(nt[i]==0)sw[i]=0;
                else sw[i] = (nwt[i] / nt[i]) * (Math.log(tempRatio)/Math.log(2)); 
        }		
    }

    public void secondSignalConstruction(double[] sw, int delta, int level){		
        this.sw = sw;
        this.delta = delta;
        int sizeSw2 = (sw.length/delta) - 1;
        sw2 = new double[sizeSw2];

        for (int i=0, k=0; i < sw2.length*delta; i=i+delta, k++){
            double[] signDtPrime = new double[delta];
            double[] signDtStar = new double[delta*2];

            for (int j=0; j <delta; j++){
                signDtPrime[j] = sw[i+j];				
            }	

            for (int j=0; j <delta*2; j++){
                signDtStar[j] = sw[i+j];				
            }

            EDCoWDwt dwtDtPrime = new EDCoWDwt(signDtPrime,level);
            EDCoWDwt dwtDtStar = new EDCoWDwt(signDtStar,level);
            dwtDtPrime.hMeasure();
            dwtDtStar.hMeasure();

            double htPrime  = dwtDtPrime.gethMeasure();
            double htStar = dwtDtStar.gethMeasure();

            if (htStar > htPrime)
                sw2[k] = (htStar - htPrime)/htPrime;
            else
                sw2[k] = 0;
        }
    }

    public double[] getSw() {
            return sw;
    }
    public double[] getSw2() {
            return sw2;
    }	
	
}
