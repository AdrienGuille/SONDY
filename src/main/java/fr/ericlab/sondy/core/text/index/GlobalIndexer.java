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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import main.java.fr.ericlab.sondy.core.structures.DocumentTermMatrix;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class GlobalIndexer {
    
    int numberOfThreads;

    public GlobalIndexer(int nbThreads){
        numberOfThreads = nbThreads;
    }
    
    public void index(String directory) throws InterruptedException, IOException {
        int messageCount;
        ArrayList<HashMap<String,Short>> mapList = new ArrayList<>(1500);
        String[] fileArray = new File(directory).list(new WildcardFileFilter("*.text"));
        int fileCount = fileArray.length;
        int fileCountPerThread = fileCount/numberOfThreads;
        LinkedList<Indexer> indexers = new LinkedList<>();
        for(int i = 0; i < numberOfThreads; i++){
            int upperBound = (i==numberOfThreads-1)?fileCount:fileCountPerThread*(i+1);
            indexers.add(new Indexer(i,directory,fileCountPerThread*i,upperBound));
            indexers.get(i).start();
        }
        for(Indexer indexer : indexers){
            indexer.join();
        }
        directory = (directory.endsWith("/")) ? directory : directory + "/";
        directory += "indexes/";
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (CalculationType type : CalculationType.values()) {
            Integer messageCountDistribution[] = new Integer[fileCount];
            mapList.clear();
            messageCount = 0;
            for (int i = 0; i < numberOfThreads; i++) {
                Indexer indexer = indexers.get(i);
                mapList.addAll(indexer.mapList.get(type));
                messageCount += indexer.messageCount.get(type);
                for (Entry entry : indexer.messageCountDistribution.get(type).entrySet()) {
                    messageCountDistribution[(int) entry.getKey()] = (int) entry.getValue();
                }
            }
            mapList.trimToSize();
            HashSet<String> vocabulary = getVocabulary(mapList);
            ArrayList<String> vocabularyList = new ArrayList<>();
            vocabularyList.addAll(vocabulary);
            vocabularyList.trimToSize();
            vocabulary.clear();
            int numberOfWordsPerThread = vocabularyList.size() / numberOfThreads;
            HashSet<String> newVocabulary = new HashSet<>();
            LinkedList<Analyzer> analyzers = new LinkedList<>();
            for (int i = 0; i < numberOfThreads; i++) {
                int upperBound = (i == numberOfThreads - 1) ? vocabularyList.size() : numberOfWordsPerThread * (i + 1);
                analyzers.add(new Analyzer(i, numberOfWordsPerThread * i, upperBound, mapList, vocabularyList, fileCount));
                analyzers.get(i).start();
            }
            for (Analyzer analyzer : analyzers) {
                analyzer.join();
            }
            for (Analyzer analyzer : analyzers) {
                newVocabulary.addAll(analyzer.newVocabulary);
            }
            analyzers.clear();
            DocumentTermMatrix dtm = new DocumentTermMatrix();
            ArrayList<String> newVocabularyList = new ArrayList<>();
            newVocabularyList.addAll(newVocabulary);
            newVocabularyList.trimToSize();
            dtm.setTerms(newVocabularyList);
            dtm.setNumberOfDocuments(messageCountDistribution);
            dtm.prepareDocumentTermSize(mapList.stream().mapToInt(doc -> doc.size()).sum());

            try {
                IntStream.range(0, fileCount)
                    .parallel()
                    .forEach(doc -> {
                        try {
                            mapList.get(doc).forEach((term, freq) -> {
                                try {
                                    dtm.setTermDocumentFrequencyWithoutCheck(term, doc, freq);
                                } catch (Exception exp) {
                                    exp.printStackTrace();
                                }
                            });
                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }
                    });
            } catch (Exception exp) {
                exp.printStackTrace();
            }
            String matrixFilename = type.name() + "FrequencyMatrix.dat";
            FileOutputStream fosMatrix = new FileOutputStream(directory + matrixFilename);
            ObjectOutputStream oosMatrix = new ObjectOutputStream(fosMatrix);
            oosMatrix.writeObject(dtm);
            oosMatrix.close();
            String messageCountFilename = type.name() + "MessageCount.txt";
            FileUtils.write(new File(directory + messageCountFilename), messageCount + "");
        }
        indexers.clear();
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
