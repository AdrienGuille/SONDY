/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core.structure.list;

import fr.ericlab.sondy.core.structure.Point2D;
import java.util.Collections;
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
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class Point2DList {
    /**
     *
     */
    public LinkedList<Point2D> list;
    
    /**
     *
     */
    public Point2DList(){
        list = new LinkedList<>();
    }
    
    /**
     *
     * @param point
     * @return
     */
    public boolean addPoint(Point2D point){
        return list.add(point);
    }
    
    /**
     *
     */
    public void sortList(){
        Collections.sort(list);
    }
    
    /**
     *
     * @return
     */
    public int size(){
        return list.size();
    }
    
    /**
     *
     * @param i
     * @return
     */
    public Point2D get(int i){
        return list.get(i);
    }
    
    /**
     *
     */
    public void clearList(){
        list.clear();
    }
}
