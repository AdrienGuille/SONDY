/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection;

import fr.ericlab.sondy.core.structure.DetectionResult;
import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.core.DataManipulation;
import fr.ericlab.sondy.core.access.IndexAccess;
import fr.ericlab.sondy.core.structure.Collection;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

public class PersistentConversations extends EventDetectionAlgorithm {
    
    double minTermSupport = 0;
    double maxTermSupport = 1.0;
    
    public String getName(){
        return "Persistent Conversations";
    }
    
    public ObservableList<DetectionResult> apply() {        
        try {
            if(parameters.get(0).getValue()!=null && !parameters.get(0).getValue().equals("")){
                minTermSupport = Double.parseDouble(parameters.get(0).getValue());
            }
            if(parameters.get(1).getValue()!=null && !parameters.get(1).getValue().equals("")){
               maxTermSupport = Double.parseDouble(parameters.get(1).getValue());
            }
            long startNanoTime = System.nanoTime();
            DataManipulation dataManipulation = new DataManipulation();
            IndexAccess indexAccess = new IndexAccess(appVariables);
            IndexReader r = indexAccess.reader;
            TermEnum allTerms = r.terms();
            HashMap<DetectionResult,Float> score = new HashMap<>();
            results = FXCollections.observableArrayList();  
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
                        float tf = 0;
                        int maxDoc = 0;
                        for(int i=appVariables.startTimeSlice; i<=appVariables.endTimeSlice; i++){
                            if(frequency[i]>tf){
                                tf = frequency[i];
                                maxDoc = i;
                            }
                        }
                        float prePeakntf = 0;
                        for(int i=0; i<maxDoc-1; i++){
                            prePeakntf += frequency[i]/cf;
                        }
                        prePeakntf = prePeakntf/(maxDoc-1);
                        float postPeaskntf = 0;
                        for(int i=maxDoc+1; i<intervalNumber; i++){
                            postPeaskntf += frequency[i]/cf;
                        }
                        postPeaskntf = postPeaskntf/(intervalNumber-maxDoc);
                        if(prePeakntf>0){
                            float peakDay = (maxDoc*intervalDuration)/24;
                            float peakDay1 = ((maxDoc+1)*intervalDuration)/24;
                            score.put(new DetectionResult(term,formatter.format(peakDay)+";"+formatter.format(peakDay1)),postPeaskntf/prePeakntf);
                        }
                    }
                }
            }
            score = Collection.getSortedMapDesc(score);
            for (Map.Entry<DetectionResult, Float> entry : score.entrySet()) {
                DetectionResult key = entry.getKey();
                results.add(0,key);
            }
            indexAccess.close();
            long endNanoTime = System.nanoTime();
            long elapsedNanoTime = endNanoTime - startNanoTime;
            double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;            
            appVariables.addLogEntry("[event detection] computed persistent conversations, minTermSupport="+minTermSupport+", maxTermSupport="+maxTermSupport+". "+results.size()+" results in "+formatter.format(elaspedSecondTime)+"s");
            return results;
        }catch (IOException ex) {
            Logger.getLogger(PersistentConversations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public PersistentConversations() {
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("minTermSupport",""),new AlgorithmParameter("maxTermSupport",""));
        algoDescription = "Identifies 'persistent conversations' using a normalized term frequency based metric";
    }

    @Override
    public String getReference() {
        return "<li><b>Persistent Conversations:</b> David A. Shamma, Lyndon Kennedy, Elizabeth F. Churchill. Peaks and persistence: modeling the shape of microblog conversations, <i>In Proceedings of the ACM 2011 conference on Computer supported cooperative work </i>, pp. 355-358, 2011</li>";
    }
    
}
