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
package main.java.fr.ericlab.sondy.algo.eventdetection;

import ch.epfl.lis.jmod.modularity.community.Community;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import main.java.fr.ericlab.sondy.core.structures.Event;
import main.java.fr.ericlab.sondy.algo.Parameter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.java.fr.ericlab.sondy.algo.eventdetection.edcow.EDCoWEvent;
import main.java.fr.ericlab.sondy.algo.eventdetection.edcow.EDCoWKeyword;
import main.java.fr.ericlab.sondy.algo.eventdetection.edcow.EDCoWModularityDetection;
import main.java.fr.ericlab.sondy.algo.eventdetection.edcow.EDCoWThreshold;
import main.java.fr.ericlab.sondy.core.structures.Events;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class EDCoW extends EventDetectionMethod {
    int delta = 8;
    int delta2 = 48;
    int gamma = 5;  
    double minTermSupport = 0.0001;
    double maxTermSupport = 0.01;
    HashMap<String,short[]> termDocMap;
    LinkedList<EDCoWEvent> eventList;
    
    public EDCoW(){
        super();
        parameters.add(new Parameter("delta",delta+""));
        parameters.add(new Parameter("delta2",delta2+""));
        parameters.add(new Parameter("gamma",gamma+""));
        parameters.add(new Parameter("minTermSupport",minTermSupport+""));
        parameters.add(new Parameter("maxTermSupport",maxTermSupport+""));
    }

    @Override
    public String getName() {
        return "EDCoW";
    }

    @Override
    public String getCitation() {
        return "<li><b>EDCoW:</b> J. Weng and B. Lee (2011) Event Detection in Twitter, In Proceedings of the 2011 AAAI Conference on Weblogs and Social Media (ICWSM), pp. 401-408</li>";
    }
    
    @Override
    public String getDescription() {
        return "Event detection with clustering of wavelet-based signals";
    }

    @Override
    public void apply() {
        double minTermOccur = parameters.getParameterValue("minTermSupport") * AppParameters.dataset.corpus.messageCount;
        double maxTermOccur = parameters.getParameterValue("maxTermSupport") * AppParameters.dataset.corpus.messageCount;
        delta = (int) parameters.getParameterValue("delta");
        delta2 = (int) parameters.getParameterValue("delta2");
        gamma = (int) parameters.getParameterValue("gamma"); 
    
        int windows = (AppParameters.timeSliceB-AppParameters.timeSliceA)/delta2;
        termDocMap = new HashMap<>();
        eventList = new LinkedList<>();
        for(int i = AppParameters.timeSliceA; i < AppParameters.timeSliceB; i++){
            String term = AppParameters.dataset.corpus.vocabulary.get(i);
            if(term.length()>1 && !AppParameters.stopwords.contains(term)){
                short[] frequency = AppParameters.dataset.corpus.termFrequencies[i];
                int cf = 0;
                for(short freq : frequency){
                    cf += freq;
                }
                if(cf > minTermOccur && cf < maxTermOccur){
                    termDocMap.put(term, frequency);
                }
            }
        }
        for(int i = 0; i < windows ;i++){
            processWindow(i);
        }
        Collections.sort(eventList);
        events = new Events();
        for(EDCoWEvent event : eventList){
            events.list.add(new Event(event.getKeywordsAsString(),AppParameters.dataset.corpus.convertTimeSliceToDay((int)event.endSlice)+","+AppParameters.dataset.corpus.convertTimeSliceToDay((int)event.startSlice)));
        }
        events.setFullList();
    }
    
    public void processWindow(int window){
    	try{
            LinkedList<EDCoWKeyword> keyWords = new LinkedList<>();
            int[] distributioni = AppParameters.dataset.corpus.messageDistribution;       
            double[] distributiond = new double[delta2];
            int startSlice = window*delta2;
            int endSlice = startSlice+delta2-1;
            for(int i = startSlice; i < endSlice;  i++){
                distributiond[i-startSlice] = (double) distributioni[i]; 
            }
            for(Map.Entry<String, short[]> entry : termDocMap.entrySet()){
                short frequencyf[] = entry.getValue();
                double frequencyd[] = new double[delta2];
                for(int i = startSlice; i < endSlice; i++){
                    frequencyd[i-startSlice] = (double) frequencyf[i];
                }
                keyWords.add(new EDCoWKeyword(entry.getKey(),frequencyd,delta,distributiond));
            }
            double[] autoCorrelationValues = new double[keyWords.size()];
            for(int i = 0; i < keyWords.size(); i++){
                autoCorrelationValues[i] = keyWords.get(i).getAutoCorrelation();
            }
            EDCoWThreshold th1 = new EDCoWThreshold();
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
            EDCoWModularityDetection modularity = new EDCoWModularityDetection(keyWordsList1,bigMatrix,startSlice,endSlice);

            double thresholdE = 0.1;
            ArrayList<Community> finalArrCom= modularity.getCommunitiesFiltered(thresholdE);
            for(Community c : finalArrCom){
                System.out.println(c.getCommunitySize());
                modularity.saveEventFromCommunity(c);
            }
            eventList.addAll(modularity.getEvents());
        } catch (IOException ex) {
            Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
