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
public class EDCoWKeyword {
    double[] nt;
	int delta;	
	double[] nwt;
	double[] SW1;
	double[] SW2;
	String keyWord;
	double autoCorrelation;
	double[] crossCorrelation;
		
	public EDCoWKeyword(String keyword_, double[] nwt_, int delta_, double[] distribution){
            nwt = nwt_;
            delta = delta_;
            nt = distribution;
            keyWord = keyword_;
            computations();
	}

	public final void computations(){
            int lev = (int) (Math.log(delta)/Math.log(2));
            EDCoWSignalConstruction signWavelet = new EDCoWSignalConstruction();
            signWavelet.firstSignalConstruction(nwt, nt);
            signWavelet.secondSignalConstruction(signWavelet.getSw(), delta, lev);		
            SW1 = signWavelet.getSw();
            SW2 = signWavelet.getSw2();
            EDCoWCrossCorrelationZeroTime cc = new EDCoWCrossCorrelationZeroTime();
            autoCorrelation = cc.autoCorrelationZeroTime(SW2);	
	}
		
	public void computeCrossCorrelation(LinkedList<EDCoWKeyword> keyWordsList1) {
            crossCorrelation = new double[keyWordsList1.size()];
            EDCoWCrossCorrelationZeroTime cc = new EDCoWCrossCorrelationZeroTime();
            for(int i=0; i<keyWordsList1.size(); i++){
                    crossCorrelation[i] = cc.correlationZeroTime(SW2, keyWordsList1.get(i).getSW2());
            }
	}		

	public double[] getNwt() {
		return nwt;
	}

	public void setNwt(double[] nwt) {
		this.nwt = nwt;
	}

	public double[] getSW1() {
		return SW1;
	}

	public void setSW1(double[] sW1) {
		SW1 = sW1;
	}

	public double[] getSW2() {
		return SW2;
	}

	public void setSW2(double[] sW2) {
		SW2 = sW2;
	}

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public double getAutoCorrelation() {
		return autoCorrelation;
	}

	public void setAutoCorrelation(double autoCorrelation) {
		this.autoCorrelation = autoCorrelation;
	}

	public double[] getCrossCorrelation() {
		return crossCorrelation;
	}

	public void setCrossCorrelation(double[] crossCorrelation) {
		this.crossCorrelation = crossCorrelation;
	}
        
        public void getCorrelations(double[] frequency, LinkedList<EDCoWKeyword> keyWordsList){
            nwt = frequency;
            computations();
            computeCrossCorrelation(keyWordsList);
        }
}
