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
package main.java.fr.ericlab.sondy.core.structures;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class RankedUsers {
    public HashMap<String,Integer> ranks;
    public int maxRank;
    
    public RankedUsers(){
        ranks = new HashMap<>();
        maxRank = 0;
    }
    
    public void add(String user, int rank){
        ranks.put(user, rank);
        if(rank > maxRank){
            maxRank = rank;
        }
    }
    
    public int getRank(String user){
        return ranks.get(user);
    }
    
    public int[] extractRankDistribution(){
        int[] array = new int[maxRank+1];
        for(Entry entry : ranks.entrySet()){
            int rank = (int)entry.getValue();
            array[rank]++;
        }
        return array;
    }
}
