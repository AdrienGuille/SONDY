/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core.structure;

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
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class DetectionResult {
    private SimpleStringProperty mainTerm;
    private SimpleStringProperty info;
    
    /**
     *
     * @param mt
     * @param i
     */
    public DetectionResult(String mt, String i){
        mainTerm = new SimpleStringProperty(mt);
        info = new SimpleStringProperty(i);
    }

    /**
     *
     * @return
     */
    public String getMainTerm() {
        return mainTerm.get();
    }
    
    /**
     *
     * @return
     */
    public String getInfo() {
        return info.get();
    }
    
    /**
     *
     * @param newMt
     */
    public void setMainTerm(String newMt){
        mainTerm.set(newMt);
    }
    
    /**
     *
     * @param newInfo
     */
    public void setInfo(String newInfo){
        mainTerm.set(newInfo);
    }
}
