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
package main.java.fr.ericlab.sondy.core.text.index;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class Analyzer extends Thread {
    final int minimumFrequency = 10;
    int threadId;
    int from;
    int to;
    int fileCount;
    ArrayList<HashMap<String,Short>> mapList;
    ArrayList<String> vocabulary;
    public ArrayList<String> newVocabulary;
    
    public Analyzer(int id, int a, int b, ArrayList<HashMap<String,Short>> ml, ArrayList<String> voc, int fc){
        from = a;
        to = b;
        threadId = id;
        mapList = ml;
        vocabulary = voc;
        fileCount = fc;
    }
    
    @Override
    public void run() {
        newVocabulary = new ArrayList<>(to-from);
        for(int j = from; j < to; j++){
            String word = vocabulary.get(j);
            int total = 0;
            for(int i = 0; i < fileCount; i++){
                Short count = mapList.get(i).get(word);
                count = (count==null)?0:count;
                total += count;
            }
            if(total > minimumFrequency){
                newVocabulary.add(word);
            }
            else {
                for(int i = 0; i < fileCount; i++) {
                    mapList.get(i).remove(word);
                }
            }
        }
        newVocabulary.trimToSize();
    }
}
