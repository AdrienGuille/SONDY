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
public class EDCoWVector {
	
	double[] vec;
	double norm;
	double sum;

	
	EDCoWVector(double[] vec1){
		vec = vec1;
		norm();
		sumVector();
	}

	public void sumVector(){
		sum = 0;
		for(double x: vec){
			sum = sum + x;
		}
	}
	
	
	public void norm() {
		// TODO Auto-generated method stub
		norm = 0;
		for(double x : vec){
			norm = (float) (norm + Math.pow(x, 2));
		}
		norm = (float) Math.sqrt(norm);
	}


	public double getNorm() {
		return norm;
	}
	public double getSum() {		
		return sum;
	}


}
