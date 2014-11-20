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
import fr.ericlab.sondy.core.structure.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
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

public class Discrepancy extends EventDetectionAlgorithm {
    
    double minTermSupport = 0;
    double maxTermSupport = 1.0;
    int smooth = 0;
    
    public String getName(){
        return "Discrepancy Model";
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
            HashMap<DetectionResult,Float> scores = new HashMap<>();
            int m = r.numDocs();
            float intervalDuration = ((float) appVariables.getCurrentDatasetInterval())/60;
            int minTermOccur = (int)(minTermSupport * appVariables.nbMessages), maxTermOccur = (int)(maxTermSupport * appVariables.nbMessages);            
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>1 && !appVariables.isStopWord(term)){
                    TermDocs termDocs = r.termDocs(allTerms.term());
                    float frequency[] = indexAccess.getTermFrequency(appVariables, termDocs);
                    float cf = frequency[m];
                    if(cf>minTermOccur && cf<maxTermOccur){
                        if(smooth>0){
                            frequency = dataManipulation.getSmoothedTermFrequency(frequency, smooth);
                        }
                        float scoreSequence[] = new float[m];
                        for(int i=0; i<m; i++){
                            scoreSequence[i] = burstiness(m,cf,frequency[i]);
                        }
                        LinkedList<Point2D> I = new LinkedList<>();
                        LinkedList<Float> L = new LinkedList<>();
                        LinkedList<Float> R = new LinkedList<>();
                        for(int i=appVariables.startTimeSlice; i<=appVariables.endTimeSlice; i++){
                            if(scoreSequence[i]>0){
                                int k = I.size();
                                float Lk = 0, Rk = sum(scoreSequence,0,i);
                                if(i>0){
                                    Lk = sum(scoreSequence,0,i-1);
                                }
                                int j = 0;
                                boolean foundJ = false;
                                for(int l=k-1; l>=0 && !foundJ; l--){
                                    if(L.get(l)<Lk){
                                        foundJ = true;
                                        j = l;
                                    }
                                }
                                if(foundJ && R.get(j)<Rk){
                                     Point2D Ik = new Point2D(I.get(j).x,i);
                                     for(int p = j; p<k; p++){
                                         I.removeLast();
                                         L.removeLast();
                                         R.removeLast();
                                     }
                                     k = j;
                                     I.add(Ik);
                                     L.add(sum(scoreSequence,0,Ik.x-1));
                                     R.add(sum(scoreSequence,0,Ik.y));
                                }else{
                                    I.add(new Point2D(i,i));
                                    L.add(Lk);
                                    R.add(Rk);
                                }
                            }
                        }
                        if(I.size()>0){
                            Point2D maxI = I.get(0);
                            for(Point2D Ii : I){
                                if(sum(scoreSequence,Ii.x,Ii.y)>sum(scoreSequence,maxI.x,maxI.y)){
                                    maxI.x = Ii.x;
                                    maxI.y = Ii.y;
                                }
                            }
                            float startDay = (maxI.x*intervalDuration)/24;
                            float endDay = (maxI.y*intervalDuration)/24;
                            scores.put(new DetectionResult(term,formatter.format(startDay)+";"+formatter.format(endDay)),sum(scoreSequence,I.get(0).x,I.get(0).y));
                        }
                    }
                }
            }
            indexAccess.close();
            scores = Collection.getSortedMapDesc(scores);
            Set<Entry<DetectionResult, Float>> entrySet = scores.entrySet();
            results = FXCollections.observableArrayList();  
            for (Entry<DetectionResult, Float> entry : entrySet) {
                results.add(0,entry.getKey());
            }
            long endNanoTime = System.nanoTime();
            long elapsedNanoTime = endNanoTime - startNanoTime;
            double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
            appVariables.addLogEntry("[event detection] computed discrepancy model of burstiness, minTermSupport="+minTermSupport+", maxTermSupport="+maxTermSupport+". "+results.size()+" results in "+formatter.format(elaspedSecondTime)+"s");
            return results;
        } catch (IOException ex) {
            Logger.getLogger(Discrepancy.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public float sum(float tab[], int a, int b){
        float sum = 0;
        for(int i = a; i<=b; i++){
            sum += tab[i];
        }
        return sum;
    }
    
    public float burstiness(float m, float cf, int[] frequency, Point2D I){
        float freqI = 0;
        for(int i = I.x; i<=I.y; i++){
            freqI += frequency[i];
        }
        return Math.abs((I.y-I.x+1)/m-freqI/cf);
    }
    
    public float burstiness(float m, float cf, float freqK){
        return freqK/cf-1/m;
    }

    public Discrepancy() {
        super();
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("minTermSupport",""),new AlgorithmParameter("maxTermSupport",""),new AlgorithmParameter("smooth",""));
        algoDescription = "Identifies bursty intervals of terms using a discrepancy-based model of burstiness";
    }

    @Override
    public String getReference() {
        return "<li><b>Discrepancy Model of Burstiness:</b> T. Lappas, B. Arai, M. Platakis, D. Kotsakos, and D. Gunopulos. On burstiness-aware search for document sequences, <i>In Proceedings of the 15th ACM SIGKDD international conference on Knowledge discovery and data mining</i>, pp. 477-486, 2009.</li>";
    }
}
