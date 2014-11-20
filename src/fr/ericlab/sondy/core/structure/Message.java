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

public class Message {
    private SimpleStringProperty author;
    private SimpleStringProperty text;
    private SimpleStringProperty time;
    
    /**
     *
     * @param ti
     * @param te
     * @param a
     */
    public Message(String ti, String te, String a){
        time = new SimpleStringProperty(ti);
        text = new SimpleStringProperty(te);
        author = new SimpleStringProperty(a);
    }

    /**
     *
     * @return
     */
    public String getTime() {
        return time.get();
    }

    /**
     *
     * @return
     */
    public String getAuthor() {
        return author.get();
    }

    /**
     *
     * @return
     */
    public String getText() {
        return text.get();
    }
}
