/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.eventdetection;

import ch.epfl.lis.jmod.modularity.community.Community;
import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.algo.eventdetection.edcow.EDCoWEvent;
import fr.ericlab.sondy.algo.eventdetection.edcow.ModularityDetection;
import fr.ericlab.sondy.algo.eventdetection.edcow.EDCoWKeyword;
import fr.ericlab.sondy.algo.eventdetection.edcow.Threshold;
import fr.ericlab.sondy.core.DataManipulation;
import fr.ericlab.sondy.core.access.IndexAccess;
import fr.ericlab.sondy.core.structure.DetectionResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
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
 *   @author Yue HE, Falitokiniaina RABEARISON, Département Informatique et Statistiques, Université Lumière Lyon 2
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class EDCoW extends EventDetectionAlgorithm {
    double minTermSupport = 0.001;
    double maxTermSupport = 1.0;
    int delta = 8;
    int delta2 = 48;
    int gamma = 5;    
    LinkedList<EDCoWEvent> events;
    HashMap<String,float[]> termDocsMap;
    
    @Override
    public String getName() {
        return "EDCoW";
    }

    @Override
    public String getReference() {
        return "<li><b>EDCoW:</b> Weng J. and Lee B. Event Detection in Twitter, <i>In Proceedings of the 5th international AAAI conference on Weblogs and Social Media</i>, pp. 401-408, 2011</li>";
    }
    
    public EDCoW() {
        super();
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("minTermSupport",""),new AlgorithmParameter("maxTermSupport",""), new AlgorithmParameter("delta",""), new AlgorithmParameter("gamma",""), new AlgorithmParameter("delta2",""));
        algoDescription = "Event detection with clustering of wavelet-based signals";
    }

    @Override
    public ObservableList<DetectionResult> apply() {
        try {
            if(parameters.get(0).getValue()!=null && !parameters.get(0).getValue().equals("")){
                minTermSupport = Double.parseDouble(parameters.get(0).getValue());
            }
            if(parameters.get(1).getValue()!=null && !parameters.get(1).getValue().equals("")){
               maxTermSupport = Double.parseDouble(parameters.get(1).getValue());
            }
            if(parameters.get(2).getValue()!=null && !parameters.get(2).getValue().equals("")){
               delta = Integer.parseInt(parameters.get(2).getValue());
            }
            if(parameters.get(3).getValue()!=null && !parameters.get(3).getValue().equals("")){
               gamma = Integer.parseInt(parameters.get(3).getValue());
            }  
            if(parameters.get(4).getValue()!=null && !parameters.get(4).getValue().equals("")){
               delta2 = Integer.parseInt(parameters.get(4).getValue());
            } 
            long startNanoTime = System.nanoTime();
            int intervals = appVariables.messageSet.nbTimeSlice;
            int windows = intervals/delta2;
            events = new LinkedList<>();
            
            termDocsMap = new HashMap<>();
            IndexAccess indexAccess = new IndexAccess(appVariables);
            IndexReader r = indexAccess.reader;
            TermEnum allTerms = r.terms();
            int minTermOccur = (int)(minTermSupport * appVariables.nbMessages), maxTermOccur = (int)(maxTermSupport * appVariables.nbMessages);
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>1 && !appVariables.isStopWord(term)){
                    TermDocs termDocs = r.termDocs(allTerms.term());
                    float frequencyf[] = indexAccess.getTermFrequency(appVariables, termDocs);
                    float cf = frequencyf[r.numDocs()];
                    if(cf>minTermOccur && cf<maxTermOccur){
                        termDocsMap.put(term, frequencyf);
                    }
                }
            }
            indexAccess.close();
            for(int i = 0; i < windows ;i++){
            	processWindow(i);
            }
            Collections.sort(events);
            results = FXCollections.observableArrayList();
            float intervalDuration = ((float) appVariables.getCurrentDatasetInterval())/60;
            for(EDCoWEvent ev : events){
                double interval[] = ev.getInterval(intervalDuration);
            	results.add(new DetectionResult(ev.getKeywordsAsString(),formatter.format(interval[0])+";"+formatter.format(interval[1])));
            }
            long endNanoTime = System.nanoTime();
            long elapsedNanoTime = endNanoTime - startNanoTime;
            double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
            appVariables.addLogEntry("[event detection] computed EDCoW, minTermSupport="+minTermSupport+", maxTermSupport="+maxTermSupport+", delta="+delta+", gamma="+gamma+". "+results.size()+" results in "+formatter.format(elaspedSecondTime)+"s");
            return results;
        } catch (NumberFormatException | IOException ex) {
            Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void processWindow(int window){
    	try{
            DataManipulation dataManipulation = new DataManipulation();
            IndexAccess indexAccess = new IndexAccess(appVariables);
            LinkedList<EDCoWKeyword> keyWords = new LinkedList<>();
            int[] distributioni = dataManipulation.getDistribution(appVariables);        
            double[] distributiond = new double[delta2];
            int startSlice = window*delta2;
            int endSlice = startSlice+delta2-1;
            for(int i = startSlice; i < endSlice;  i++){
                distributiond[i-startSlice] = (double) distributioni[i]; 
            }
            for(Entry<String, float[]> entry : termDocsMap.entrySet()){
                float frequencyf[] = entry.getValue();
                double frequencyd[] = new double[delta2];
                for(int i = startSlice; i < endSlice; i++){
                    frequencyd[i-startSlice] = (double) frequencyf[i];
                }
                keyWords.add(new EDCoWKeyword(entry.getKey(),frequencyd,delta,distributiond));
            }
            indexAccess.close();
            double[] autoCorrelationValues = new double[keyWords.size()];
            for(int i = 0; i < keyWords.size(); i++){
                autoCorrelationValues[i] = keyWords.get(i).getAutoCorrelation();
            }
            Threshold th1 = new Threshold();
            double theta1 = th1.theta1(autoCorrelationValues, gamma);

            // Removing trivial keywords based on theta1
            LinkedList<EDCoWKeyword> keyWordsList1 = new LinkedList<>();
            for(EDCoWKeyword k : keyWords){
                if(k.getAutoCorrelation() > theta1){
                    keyWordsList1.add(k);
                }
            }        
            for(EDCoWKeyword kw1 : keyWordsList1){
                kw1.computeCrossCorrelation(keyWordsList1);
            }
            double[][] bigMatrix = new double[keyWordsList1.size()][keyWordsList1.size()];
            for(int i=0; i<keyWordsList1.size(); i++){
                bigMatrix[i] = keyWordsList1.get(i).getCrossCorrelation();
            }

            //compute theta2 using the bigmatrix
            double theta2 = th1.theta2(bigMatrix, gamma);        
            for(int i = 0; i < keyWordsList1.size(); i++){
                for(int j = i+1; j < keyWordsList1.size(); j++){
                    bigMatrix[i][j] = (bigMatrix[i][j] < theta2)?0:bigMatrix[i][j];
                }
            }
            ModularityDetection modularity = new ModularityDetection(keyWordsList1,bigMatrix,startSlice,endSlice);

            double thresholdE = 0.1;
            ArrayList<Community> finalArrCom= modularity.getCommunitiesFiltered(thresholdE);
            for(Community c : finalArrCom){
                modularity.saveEventFromCommunity(c);
            }
            events.addAll(modularity.getEvents());
        } catch (IOException ex) {
            Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
