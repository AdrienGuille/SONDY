/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core.structure;

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

public class Point2D implements Comparable {

    /**
     *
     */
    public int x;
    /**
     *
     */
    public int y;
    
    /**
     *
     * @param _x
     * @param _y
     */
    public Point2D(int _x, int _y){
        x = _x;
        y = _y;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        Point2D point = (Point2D)o;
        if(this.x > point.x){
            return 1;
        }else{
            if(this.x == point.x){
                return 0;
            }else{
                return -1;
            }
        }
    }
    
    
}
