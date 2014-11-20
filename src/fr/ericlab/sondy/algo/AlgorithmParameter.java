/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo;

import javafx.beans.property.SimpleStringProperty;

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
 *   Class that defines a generic parameter.  
 * 
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class AlgorithmParameter {
    private SimpleStringProperty parameter;
    private SimpleStringProperty value;
    
    /**
     *
     * @param p
     * @param v
     */
    public AlgorithmParameter(String p, String v){
        parameter = new SimpleStringProperty(p);
        value = new SimpleStringProperty(v);
    }

    /**
     *
     * @return
     */
    public String getParameter() {
        return parameter.get();
    }

    /**
     *
     * @return
     */
    public String getValue() {
        return value.get();
    }
    
    /**
     *
     * @param newValue
     */
    public void setValue(String newValue){
        value.set(newValue);
    }
}
