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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class GlobalIndexer {
    
    int numberOfThreads;
    boolean mention;
    int messageCount;
    
    public GlobalIndexer(int nbThreads, boolean m){
        numberOfThreads = nbThreads;
        mention = m;
    }
    
    public void index(String directory) throws InterruptedException, FileNotFoundException, IOException {
        ArrayList<HashMap<String,Short>> mapList = new ArrayList<>(1500);
        String[] fileArray = new File(directory).list();
        int fileCount = 0;
        for(String filename : fileArray){
            if(filename.endsWith(".text"))
                fileCount++;
        }
        int fileCountPerThread = fileCount/numberOfThreads;
        LinkedList<Indexer> indexers = new LinkedList<>();
        for(int i = 0; i < numberOfThreads; i++){
            int upperBound = (i==numberOfThreads-1)?fileCount-1:fileCountPerThread*(i+1);
            indexers.add(new Indexer(i,directory,fileCountPerThread*i+1,upperBound,mention,1));
            indexers.get(i).start();
        }
        for(Indexer indexer : indexers){
            indexer.join();
        }
        int messageCountDistribution[] = new int[fileCount];
        for(int i = 0; i < numberOfThreads; i++){
            Indexer indexer = indexers.get(i);
            mapList.addAll(indexer.mapList);
            messageCount += indexer.messageCount;
            for(Entry entry : indexer.messageCountDistribution.entrySet()){
                messageCountDistribution[(int) entry.getKey()] = (int) entry.getValue();
            }
        }
        indexers.clear();
        System.gc();
        mapList.trimToSize();
        HashSet<String> vocabulary = getVocabulary(mapList);
        ArrayList<String> vocabularyList = new ArrayList<>();
        vocabularyList.addAll(vocabulary);
        vocabularyList.trimToSize();
        vocabulary.clear();
        int numberOfWordsPerThread = vocabularyList.size()/numberOfThreads;
        HashSet<String> newVocabulary = new HashSet<>();
        LinkedList<Analyzer> analyzers = new LinkedList<>();
        for(int i = 0; i < numberOfThreads; i++){
            int upperBound = (i==numberOfThreads-1)?vocabularyList.size()-1:numberOfWordsPerThread*(i+1);
            analyzers.add(new Analyzer(i,numberOfWordsPerThread*i+1,upperBound,mapList,vocabularyList,fileCount));
            analyzers.get(i).start();
        }
        for(Analyzer analyzer : analyzers){
            analyzer.join();
        }
        for(Analyzer analyzer : analyzers){
            newVocabulary.addAll(analyzer.newVocabulary);
        }
        analyzers.clear();
        ArrayList<String> newVocabularyList = new ArrayList<>();
        newVocabularyList.addAll(newVocabulary);
        newVocabularyList.trimToSize();
        int newVocabularySize = newVocabularyList.size();
        newVocabulary.clear();
        short[][] frequencyMatrix = new short[newVocabularySize][fileCount];
        for(int i = 0; i < newVocabularySize; i++){
            String word = newVocabularyList.get(i); 
            for(int j = 0; j < fileCount-1; j++){
                Short count = mapList.get(j).get(word);
                count = (count==null)?0:count;
                frequencyMatrix[i][j] = count;
            }
        }
        directory = (directory.endsWith("/"))?directory:directory+"/";
        directory += "indexes/";
        File dir = new File(directory);
        if(!dir.exists()){
            dir.mkdir();
        }
        String matrixFilename = (mention)?"mentionFrequencyMatrix.dat":"frequencyMatrix.dat";
        FileOutputStream fosMatrix = new FileOutputStream(directory+matrixFilename);
        ObjectOutputStream oosMatrix = new ObjectOutputStream(fosMatrix);
        oosMatrix.writeObject(frequencyMatrix);
        String vocabularyFilename = (mention)?"mentionVocabulary.dat":"vocabulary.dat";
        FileOutputStream fosVocabulary = new FileOutputStream(directory+vocabularyFilename);
        ObjectOutputStream oosVocabulary = new ObjectOutputStream(fosVocabulary);
        oosVocabulary.writeObject(newVocabularyList);
        String messageCountFilename = (mention)?"messageCountMention.txt":"messageCount.txt";
        FileUtils.write(new File(directory+messageCountFilename), messageCount+"");
        String messageCountDistributionFilename = (mention)?"messageMentionCountDistribution.dat":"messageCountDistribution.dat";
        FileOutputStream fosDistribution = new FileOutputStream(directory+messageCountDistributionFilename);
        ObjectOutputStream oosDistribution = new ObjectOutputStream(fosDistribution);
        oosDistribution.writeObject(messageCountDistribution);
    }
    
    public static HashSet<String> getVocabulary(List<HashMap<String,Short>> mapList){
        HashSet<String> vocabulary = new HashSet<>();
        for(HashMap<String,Short> map : mapList){
            for(String string : map.keySet()){
                vocabulary.add(string);
            }
        }
        return vocabulary;
    }
}
