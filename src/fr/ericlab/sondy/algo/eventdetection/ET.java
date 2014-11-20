/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.eventdetection;

import ch.usi.inf.sape.hac.HierarchicalAgglomerativeClusterer;
import ch.usi.inf.sape.hac.agglomeration.AgglomerationMethod;
import ch.usi.inf.sape.hac.agglomeration.CompleteLinkage;
import ch.usi.inf.sape.hac.dendrogram.Dendrogram;
import ch.usi.inf.sape.hac.dendrogram.DendrogramBuilder;
import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.algo.eventdetection.et.Burst;
import fr.ericlab.sondy.algo.eventdetection.et.Bursts;
import fr.ericlab.sondy.algo.eventdetection.et.ETExperiment;
import fr.ericlab.sondy.algo.eventdetection.et.ETEvent;
import fr.ericlab.sondy.algo.eventdetection.et.Frequency;
import fr.ericlab.sondy.algo.eventdetection.et.OverallDissimilarity;
import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.DataManipulation;
import fr.ericlab.sondy.core.access.DBAccess;
import fr.ericlab.sondy.core.access.IndexAccess;
import fr.ericlab.sondy.core.structure.DetectionResult;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

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
public class ET extends EventDetectionAlgorithm {
    double minTermSupport = 0;
    double maxTermSupport = 1.0;
    double gamma = 1/3000;
    double alpha = 0.6;
    double beta = 1 - alpha;
    
    public String getName(){
        return "ET";
    }

    public ObservableList<DetectionResult> apply(){
        try {
            if(parameters.get(0).getValue()!=null && !parameters.get(0).getValue().equals("")){
               minTermSupport = Double.parseDouble(parameters.get(0).getValue());
            }
            if(parameters.get(1).getValue()!=null && !parameters.get(1).getValue().equals("")){
               maxTermSupport = Double.parseDouble(parameters.get(1).getValue());
            }
            if(parameters.get(2).getValue()!=null && !parameters.get(2).getValue().equals("")){
               gamma = Double.parseDouble(parameters.get(2).getValue());
            }
            if(parameters.get(3).getValue()!=null && !parameters.get(3).getValue().equals("")){
               alpha = Double.parseDouble(parameters.get(3).getValue());
            }
            
            long startNanoTime = System.nanoTime();
            HashMap<String,Bursts> mapBursts = new HashMap<>();
            HashMap<String,LinkedList<String>> mapCooccurences = new HashMap<>();
            LinkedList<String> bigrams = new LinkedList<>();
            int minTermOccur = (int) (minTermSupport*appVariables.nbMessages);
            int maxTermOccur = (int) (maxTermSupport*appVariables.nbMessages);
                
            DBAccess dbAccess = new DBAccess();
            dbAccess.initialize(appVariables, false);
            DataManipulation dataManipulation = new DataManipulation();
            IndexAccess indexAccess = new IndexAccess(appVariables);
            IndexReader r = indexAccess.reader;
            int intervalNumber = r.numDocs();
            TermEnum allTerms = r.terms();
            LinkedList<Frequency> frequencies = new LinkedList<>();
            while(allTerms.next()){
                String k = allTerms.term().text();
                if(!appVariables.isStopWord(k)){
                    TermDocs termDocs = r.termDocs(allTerms.term());
                    float frequency[] = indexAccess.getTermFrequency(appVariables, termDocs);
                    float cf = frequency[intervalNumber];
                    if(cf > minTermOccur && cf < maxTermOccur){
                        bigrams.add(k);
                        frequencies.add(new Frequency(k,frequency,cf));
                    }
                }
            }
            HashSet<String> bigramSet = new HashSet<>();
            bigramSet.addAll(bigrams);

            // Pre-computing AF total in all time-slices
            double AFtotal[] = new double[intervalNumber];
            for(int a = 0; a < intervalNumber; a++){
                double AFa = 0;
                for(int b = 0; b < bigrams.size(); b++){
                    AFa += frequencies.get(b).AF[a];
                }
                AFtotal[a] = AFa;
            }
            
            int[] distribution = dataManipulation.getDistribution(appVariables);
            // Computing PF(kj_i) for all bigrams (j) in all time-slices (i)
            for(int j = 0; j < bigrams.size(); j++){
                String k = bigrams.get(j);
                Frequency fr = frequencies.get(j);
                float AF[] = fr.AF;
                for(int i = 1; i < intervalNumber; i++){
                    double AFk = AF[i];
                    double PFk = AFk/AFtotal[i];
                    if(PFk > gamma){
                        // Storing bursty intervals
                        Bursts bursts;
                        if(mapBursts.get(k) != null){
                            bursts = mapBursts.get(k);
                        }else{
                            bursts = new Bursts();
                        }
                        // Calculating the increase
                        double h = PFk - frequencies.get(j).AF[i-1]/distribution[i-1];
                        if(h > 0){
                            bursts.list.add(new Burst(i,h));
                            mapBursts.put(k, bursts);
                        }
                    }
                }
                Bursts bursts = mapBursts.get(k);
                String tweets = "";
                if(bursts != null){
                    for(Burst burst : bursts.list){
                        tweets += dbAccess.getMessagesAsString(appVariables, k, burst.U);
                    }
                }
                LinkedList<String> FCB = getFrequentBigrams(tweets,bigramSet);
                mapCooccurences.put(k,FCB);
            }
            // Freeing memory
            frequencies = null;
            indexAccess.close();
            
            mapBursts = getSortedMapDesc(mapBursts);
            // Clustering keywords
            String strEvents = performHAC(appVariables, bigrams, mapBursts, mapCooccurences);
            long endNanoTime = System.nanoTime();
            long elapsedNanoTime = endNanoTime - startNanoTime;
            double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
            readEventsFromString(strEvents);
            appVariables.addLogEntry("[event detection] computed ET, minTermSupport="+minTermSupport+", maxTermSupport="+maxTermSupport+", gamma="+gamma+", alpha="+alpha+". "+results.size()+" results in "+formatter.format(elaspedSecondTime)+"s");
            return results;
        } catch (IOException ex) {
            Logger.getLogger(ET.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static LinkedList<String> getFrequentBigrams(String tweets, HashSet<String> bigrams){
        try {
            LinkedList<String> FCB = new LinkedList<String>();
            WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_36);
            RAMDirectory temporaryIndex = new RAMDirectory();
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            IndexWriter temporaryWriter = new IndexWriter(temporaryIndex, config);
            Document doc = new Document();
            doc.add(new Field("content", tweets, Field.Store.NO, Field.Index.ANALYZED,Field.TermVector.YES));
            temporaryWriter.addDocument(doc);
            temporaryWriter.commit();
            IndexReader temporaryReader = IndexReader.open(temporaryWriter, true);
            TermEnum allTerms = temporaryReader.terms();
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(bigrams.contains(term)){
                    FCB.add(term);
                }
            }
            temporaryWriter.close();
            temporaryReader.close();
            temporaryIndex.close();
            return FCB;
        } catch (LockObtainFailedException ex) {
            Logger.getLogger(ET.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ET.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new LinkedList<>();
    }
    
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
    
    public String performHAC(AppVariables appVariables, LinkedList<String> bigrams, HashMap<String,Bursts> mapBursts, HashMap<String,LinkedList<String>> mapCooccurences){
        ObjectOutputStream oosMap = null;
        HashMap<String,Bursts> map2 = new HashMap<>();
        LinkedList<String> bigrams2 = new LinkedList<>();
        int topKeywords = 97000;
        mapBursts = getSortedMapDesc(mapBursts);
        Set<Map.Entry<String, Bursts>> entrySet = mapBursts.entrySet();
        int count = 0;
        for(Map.Entry<String, Bursts> entry : entrySet){
            if(count == topKeywords){
                break;
            }
            Bursts bursts = entry.getValue();
            map2.put(entry.getKey(),entry.getValue());
            bigrams2.add(entry.getKey());
            count++;
        }
        ETExperiment experiment = new ETExperiment(appVariables, bigrams2, map2, mapCooccurences);
        OverallDissimilarity dissimilarityMeasure = new OverallDissimilarity(alpha,beta);
        AgglomerationMethod agglomerationMethod = new CompleteLinkage();
        DendrogramBuilder dendrogramBuilder = new DendrogramBuilder(experiment.getNumberOfObservations());
        HierarchicalAgglomerativeClusterer clusterer = new HierarchicalAgglomerativeClusterer(experiment, dissimilarityMeasure, agglomerationMethod);
        clusterer.cluster(dendrogramBuilder);
        Dendrogram dendrogram = dendrogramBuilder.getDendrogram();
        String eventsStr = dendrogram.dump3(bigrams2);
        return eventsStr;
    }
    
    public ObservableList<DetectionResult> readEventsFromString(String eventString){
        LinkedList<ETEvent> events = new LinkedList<>();
        results = FXCollections.observableArrayList();  
        Scanner scanner = new Scanner(eventString);
        boolean event = false;
        ETEvent e = new ETEvent();
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(!line.contains("merge")){
                if(!event){
                    e = new ETEvent();
                }
                event = true;
                e.relevantBigrams.add(line);
            }else{
                if(event){
                    event = false;
                    ETEvent e0 = new ETEvent();
                    e0.relevantBigrams.addAll(e.relevantBigrams);
                    events.add(e0);
                }
            }
        }
        Collections.sort(events);
        for(ETEvent ev : events){
            System.out.println(ev.toString());
            results.add(new DetectionResult(ev.toString(),"0.0;0.0"));
        }
        return results;
    }
    
    public ET() {
        super();
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("minTermSupport",""),new AlgorithmParameter("maxTermSupport",""), new AlgorithmParameter("gamma",""), new AlgorithmParameter("alpha",""));
        algoDescription = "Detects events based on textual and temporal features using hierarchical clustering";
    }
    
    @Override
    public String getReference() {
        return "<li><b>ET:</b> Ruchi P. and Kamalakar K. ET: Events from tweets, <i>In Proceedings of the 22nd international conference on World Wide Web companion</i>, pp. 613-620, 2013</li>";
    }
}
