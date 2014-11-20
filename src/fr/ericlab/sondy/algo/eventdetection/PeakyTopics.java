/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.core.DataManipulation;
import fr.ericlab.sondy.core.access.IndexAccess;
import fr.ericlab.sondy.core.structure.Collection;
import fr.ericlab.sondy.core.structure.DetectionResult;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class PeakyTopics extends EventDetectionAlgorithm {
    
    double minTermSupport = 0;
    double maxTermSupport = 1.0;
    int smooth = 0;
    
    public String getName(){
        return "Peaky Topics";
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
               smooth = Integer.parseInt(parameters.get(2).getValue());
            }
            long startNanoTime = System.nanoTime();
            DataManipulation dataManipulation = new DataManipulation();
            IndexAccess indexAccess = new IndexAccess(appVariables);
            IndexReader r = indexAccess.reader;
            TermEnum allTerms = r.terms();
            HashMap<DetectionResult,Float> score = new HashMap<>();
            int intervalNumber = r.numDocs();
            float intervalDuration = ((float) appVariables.getCurrentDatasetInterval())/60;
            int minTermOccur = (int)(minTermSupport * appVariables.nbMessages), maxTermOccur = (int)(maxTermSupport * appVariables.nbMessages);
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>1 && !appVariables.isStopWord(term)){
                    TermDocs termDocs = r.termDocs(allTerms.term());
                    float frequency[] = indexAccess.getTermFrequency(appVariables, termDocs);
                    float cf = frequency[intervalNumber];
                    if(cf>minTermOccur && cf<maxTermOccur){
                        if(smooth>0){
                            frequency = dataManipulation.getSmoothedTermFrequency(frequency, smooth);
                        }
                        float tf = 0;
                        int peakIndex =0;
                        for(int i=appVariables.startTimeSlice; i<= appVariables.endTimeSlice; i++){
                            if(frequency[i]>tf){
                                tf = frequency[i];
                                peakIndex = i;
                            }
                        }
                        float peakDay = (peakIndex*intervalDuration)/24;
                        float peakDay1 = ((peakIndex+1)*intervalDuration)/24;
                        score.put(new DetectionResult(term,formatter.format(peakDay)+";"+formatter.format(peakDay1)),tf/cf);
                    }
                }
            }
            indexAccess.close();
            score = Collection.getSortedMapDesc(score);
            Set<Entry<DetectionResult, Float>> entrySet = score.entrySet();
            results = FXCollections.observableArrayList();  
            for (Entry<DetectionResult, Float> entry : entrySet) {
                results.add(0,entry.getKey());
            }
            long endNanoTime = System.nanoTime();
            long elapsedNanoTime = endNanoTime - startNanoTime;
            double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
            appVariables.addLogEntry("[event detection] computed peaky topics, minTermSupport="+minTermSupport+", maxTermSupport="+maxTermSupport+". "+results.size()+" results in "+formatter.format(elaspedSecondTime)+"s");
            return results;
        } catch (IOException ex) {
            Logger.getLogger(PeakyTopics.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public PeakyTopics() {
        super();
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("minTermSupport",""),new AlgorithmParameter("maxTermSupport",""), new AlgorithmParameter("smooth",""));
        algoDescription = "Identifies 'peaky topics' using a normalized term frequency based metric";
    }

    @Override
    public String getReference() {
        return "<li><b>Peaky Topics:</b> David A. Shamma, Lyndon Kennedy, Elizabeth F. Churchill. Peaks and persistence: modeling the shape of microblog conversations, <i>In Proceedings of the ACM 2011 conference on Computer supported cooperative work </i>, pp. 355-358, 2011</li>";
    }
    
}
