/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.core.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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

public class Collection {
    
    /**
     * Sorts a hasmap in descending order.
     * @param hmap
     * @return
     */
    public static HashMap getSortedMapDesc(HashMap hmap){
        HashMap map = new LinkedHashMap();
        List mapKeys = new ArrayList(hmap.keySet());
        List mapValues = new ArrayList(hmap.values());
        hmap.clear();
        TreeSet sortedSet = new TreeSet(mapValues);
        Object[] sortedArray = sortedSet.toArray();
        int size = sortedArray.length;
        for (int i=0; i<size; i++){
            map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), sortedArray[i]);
        }
        return map;
    }
    
    /**
     * Sorts a hasmap in ascending order.
     * @param hmap
     * @return
     */
    public static HashMap getSortedMapAsc(HashMap hmap){
        HashMap map = new LinkedHashMap();
        List mapKeys = new ArrayList(hmap.keySet());
        List mapValues = new ArrayList(hmap.values());
        hmap.clear();
        TreeSet sortedSet = new TreeSet(mapValues);
        Object[] sortedArray = sortedSet.toArray();
        int size = sortedArray.length;
        for (int i=size-1; i>=0; i--){
            map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), sortedArray[i]);
        }
        return map;
    }
    
    /**
     *
     * @param hmap
     * @return
     */
    public static HashMap getDistribution(HashMap hmap){
        HashMap map = new LinkedHashMap();
        Iterator it = hmap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            int count = 0;
            if(map.get(pairs.getValue())!=null){
                count = (int) map.get(pairs.getValue());
            }
            count++;
            map.put(pairs.getValue(),count);
            it.remove();
        }
        return map;
    }
}
