/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.eventdetection;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.core.access.IndexAccess;
import fr.ericlab.sondy.core.structure.Collection;
import fr.ericlab.sondy.core.structure.DetectionResult;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

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

public class TrendingScore extends EventDetectionAlgorithm {
    double minTermSupport = 0;
    double maxTermSupport = 1.0;
    double trendingThreshold = 10;
    
    public String getName(){
        return "Trending Score";
    }
    
    public ObservableList<DetectionResult> apply() {
        try {
            if(parameters.get(0).getValue()!=null && !parameters.get(0).getValue().equals("")){
                minTermSupport = Double.parseDouble(parameters.get(0).getValue());
            }
            if(parameters.get(1).getValue()!=null && !parameters.get(1).getValue().equals("")){
               maxTermSupport = Double.parseDouble(parameters.get(1).getValue());
            }
            if(parameters.get(2).getValue()!=null && !parameters.get(2).getValue().equals("")){
               trendingThreshold = Double.parseDouble(parameters.get(2).getValue());
            }
            long startNanoTime = System.nanoTime();
            IndexAccess indexAccess = new IndexAccess(appVariables);
            IndexReader r = indexAccess.reader;
            TermEnum allTerms = r.terms();
            HashMap<DetectionResult,Float> score = new HashMap<>();
            int intervalNumber = r.numDocs();
            float intervalDuration = ((float) appVariables.getCurrentDatasetInterval())/60;
            int minTermOccur = (int)(minTermSupport * appVariables.nbMessages), maxTermOccur = (int)(maxTermSupport * appVariables.nbMessages);
            int[] nbWordsPerDoc = new int[r.numDocs()];
            for (int luceneId : appVariables.globalIdMap.keySet()) {
                int sliceId = appVariables.globalIdMap.get(luceneId);
                Document doc = r.document(luceneId);
                String content = doc.get("content");
                int count = 0;
                for(int i = 0; i < content.length(); i++) {
                     if(Character.isWhitespace(content.charAt(i))) count++;
                }
                nbWordsPerDoc[sliceId] = count;
            }
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>1 && !appVariables.isStopWord(term)){
                    TermDocs termDocs = r.termDocs(allTerms.term());
                    float frequency[] = indexAccess.getTermFrequency(appVariables, termDocs);
                    float cf = frequency[intervalNumber];
                    if(cf>minTermOccur && cf<maxTermOccur){
                        double[] tfnorm = new double[intervalNumber];
                        double tfnormTotal = 0;
                        double[] trendingScore = new double[intervalNumber];
                        for(int i = appVariables.startTimeSlice; i <= appVariables.endTimeSlice; i++){
                            tfnorm[i] = (frequency[i]/nbWordsPerDoc[i])*Math.pow(10, 6);
                            tfnormTotal += tfnorm[i];
                        }
                        for(int i = appVariables.startTimeSlice; i <= appVariables.endTimeSlice; i++){
                            trendingScore[i] = tfnorm[i]/((tfnormTotal - tfnorm[i])/(intervalNumber-1));
                            if(trendingScore[i] > trendingThreshold){
                                float dayS = (i*intervalDuration)/24;
                                float dayE = ((i+1)*intervalDuration)/24;
                                score.put(new DetectionResult(term,formatter.format(dayS)+";"+formatter.format(dayE)),(float)trendingScore[i]);
                            }
                        }
                    }
                }
            }
            indexAccess.close();
            score = Collection.getSortedMapDesc(score);
            Set<Map.Entry<DetectionResult, Float>> entrySet = score.entrySet();
            results = FXCollections.observableArrayList();  
            for (Map.Entry<DetectionResult, Float> entry : entrySet) {
                results.add(0,entry.getKey());
            }
            long endNanoTime = System.nanoTime();
            long elapsedNanoTime = endNanoTime - startNanoTime;
            double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
            appVariables.addLogEntry("[event detection] computed trending scores, minTermSupport="+minTermSupport+", maxTermSupport="+maxTermSupport+", trendingThreshold="+trendingThreshold+". "+results.size()+" results in "+formatter.format(elaspedSecondTime)+"s");
            return results;
        } catch (IOException ex) {
            Logger.getLogger(PeakyTopics.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public TrendingScore() {
        super();
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("minTermSupport",""),new AlgorithmParameter("maxTermSupport",""), new AlgorithmParameter("trendingThreshold",""));
        algoDescription = "Scores event-related terms using a normalized term frequency based metric";
    }

    @Override
    public String getReference() {
        return "<li><b>Trending Score:</b> Benhardus J. and Kalita J. Streaming trend detection in Twitter, <i>International Journal of Web Based Communities, Vol. 9, No. 1, 2013</i>, pp. 122-139, 2013</li>";
    }
}
