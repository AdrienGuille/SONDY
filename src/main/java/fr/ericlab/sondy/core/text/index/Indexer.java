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

import com.google.common.collect.Lists;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import main.java.fr.ericlab.sondy.core.utils.HashMapUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public final class Indexer extends Thread {
    int threadId;
    int minWordLength = 2;
    String directory;
    int from;
    int to;
    HashMap<CalculationType, ArrayList<HashMap<String,Short>>> mapList;
    HashMap<CalculationType, HashMap<Integer,Integer>> messageCountDistribution;
    HashMap<CalculationType, Integer> messageCount;
    
    public Indexer(){
        
    }
    
    public Indexer(int id, String d, int a, int b){
        directory = d;
        threadId = id;
        from = a;
        to = b;
    }
    
    public ArrayList<String> getMostFrequentWords(String text, String sourceWord, int numberOfWords){
        HashMap<String,Short> map = indexString(text);
        Map<String, Short> sortedMap = HashMapUtils.sortByDescValue(map);
        ArrayList<String> list = new ArrayList<>();
        int count = 0;
        Set<Entry<String, Short>> entrySet = sortedMap.entrySet();
        Iterator iterator = entrySet.iterator();
        while(count <= numberOfWords && iterator.hasNext()){
            String word = (String)((Entry)(iterator.next())).getKey();
            if(!word.equals(sourceWord) && !AppParameters.stopwords.contains(word)){
                list.add(word);
                count++;
            }
        }
        return list;
    }
    
    public HashMap<String,Short> indexString(String text){
        HashMap<String,Short> map = new HashMap<>();
        Analyzer analyzer = new StandardAnalyzer();
        String cleanText = text.toLowerCase();
        List<String> strings = Tokenizer.tokenizeString(analyzer, cleanText);
        for(String string : strings){
            if(string.length()>=minWordLength){
                Short count = 0;
                if(map.containsKey(string)) {
                    count = map.get(string);
                }
                count++;
                map.put(string,count);
            }
        }
        return map;
    }
    
    public void indexFile(int i, String filePath) {
        try {
            List<String> lines = FileUtils.readLines(new File(filePath));
            Analyzer analyzer = new StandardAnalyzer();

            HashMap<CalculationType, Integer> messageCountFile = new HashMap<>();
            HashMap<CalculationType, HashMap<String,Short>> allMap = new HashMap<>();
            for (CalculationType type : CalculationType.values()) {
                messageCountFile.put(type, 0);
                allMap.put(type, new HashMap<>());
            }
            for(String line : lines){
                String cleanLine = line.toLowerCase();
                List<String> strings = Tokenizer.tokenizeString(analyzer, cleanLine).stream().filter(string -> string.length() >= minWordLength).collect(Collectors.toList());
                for (CalculationType type : CalculationType.values()) {
                    if (type == CalculationType.Mention && !line.contains("@"))
                        continue;
                    messageCountFile.put(type, messageCountFile.getOrDefault(type, 0) + 1);
                    HashMap<String,Short> map = allMap.get(type);
                    if (type == CalculationType.Existence) {
                        for (String string : strings.stream().distinct().collect(Collectors.toList())) {
                            Short count = 0;
                            if (map.containsKey(string)) {
                                count = map.get(string);
                            }
                            count++;
                            map.put(string,count);
                        }
                    }
                    else {
                        for (String string : strings) {
                            Short count = 0;
                            if (map.containsKey(string)) {
                                count = map.get(string);
                            }
                            count++;
                            map.put(string, count);
                        }
                    }
                }
            }
            for (CalculationType type : CalculationType.values()) {
                messageCountDistribution.get(type).put(i, messageCountFile.get(type));
                messageCount.put(type, messageCount.get(type) + messageCountFile.get(type));
                mapList.get(type).add(allMap.get(type));
            }
        } catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        messageCountDistribution = new HashMap<>();
        mapList = new HashMap<>();
        messageCount = new HashMap<>();
        for (CalculationType type : CalculationType.values()) {
            messageCountDistribution.put(type, new HashMap<>());
            mapList.put(type, new ArrayList<>(to-from));
            messageCount.put(type, 0);
        }
        NumberFormat formatter = new DecimalFormat("00000000");
        for(int i = from; i < to; i++){
            indexFile(i, directory + File.separator + formatter.format(i) + ".text");
        }
    }
    
    public static HashSet<String> getVocabulary(List<HashMap<String,Short>> mapList){
        HashSet<String> vocabulary = new HashSet<>();
        for(HashMap<String,Short> map : mapList){
            vocabulary.addAll(map.keySet());
        }
        return vocabulary;
    }
    
}
