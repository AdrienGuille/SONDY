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

import main.java.fr.ericlab.sondy.core.app.AppParameters;
import main.java.fr.ericlab.sondy.core.structures.Event;
import main.java.fr.ericlab.sondy.algo.Parameter;
import main.java.fr.ericlab.sondy.algo.eventdetection.mabed.MABEDComponent1;
import main.java.fr.ericlab.sondy.algo.eventdetection.mabed.MABEDComponent2;
import main.java.fr.ericlab.sondy.algo.eventdetection.mabed.MABEDEvent;
import main.java.fr.ericlab.sondy.algo.eventdetection.mabed.MABEDEventGraph;
import main.java.fr.ericlab.sondy.algo.eventdetection.mabed.MABEDEventList;
import main.java.fr.ericlab.sondy.core.app.Configuration;
import main.java.fr.ericlab.sondy.core.text.index.CalculationType;
import main.java.fr.ericlab.sondy.core.utils.HashMapUtils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.java.fr.ericlab.sondy.core.structures.Events;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class MABED extends EventDetectionMethod {
    int k = 10;
    double sigma = 0.7;
    double theta = 0.7;
    int p = 10;
    double minTermSupport = 0.0001;
    double maxTermSupport = 0.01;
    int _MIN_RELATED_WORDS_ = 2;
    
    public MABED(){
        super();
        parameters.add(new Parameter("k",k+""));
        parameters.add(new Parameter("p",p+""));
        parameters.add(new Parameter("theta",theta+""));
        parameters.add(new Parameter("sigma",sigma+""));
        parameters.add(new Parameter("minTermSupport",minTermSupport+""));
        parameters.add(new Parameter("maxTermSupport",maxTermSupport+""));
    }

    @Override
    public String getName() {
        return "MABED";
    }

    @Override
    public String getCitation() {
        return "<li><b>MABED:</b> A. Guille and C. Favre (2014) Mention-anomaly-based Event Detection and Tracking in Twitter. In Proceedings of the 2014 ACM/IEEE International Conference on Advances in Social Network Mining and Analysis (ASONAM), pp. 375-382</li>";
    }
    
    @Override
    public String getDescription() {
        return "A statistical, mention-anomaly-based, method for detecting events from a social stream";
    }

    @Override
    public void apply() {
        double minTermOccur = parameters.getParameterValue("minTermSupport") * AppParameters.dataset.corpus.messageCount;
        double maxTermOccur = parameters.getParameterValue("maxTermSupport") * AppParameters.dataset.corpus.messageCount;
        k = (int) parameters.getParameterValue("k");
        p = (int) parameters.getParameterValue("p");
        theta = parameters.getParameterValue("theta");
        sigma = parameters.getParameterValue("sigma");
        Map<Event,Double> scores = new HashMap<>();
        
        LinkedList<MABEDComponent1> c1Threads = new LinkedList<>();
        int numberOfWordsPerThread = AppParameters.dataset.corpus.termFrequencies.get(CalculationType.Mention).getTerms().size()/Configuration.numberOfCores;
        for(int i = 0; i < Configuration.numberOfCores; i++){
            int upperBound = (i==Configuration.numberOfCores-1)?AppParameters.dataset.corpus.termFrequencies.get(CalculationType.Mention).getTerms().size()-1:numberOfWordsPerThread*(i+1);
            c1Threads.add(new MABEDComponent1(i,numberOfWordsPerThread*i+1,upperBound,(int)minTermOccur,(int)maxTermOccur));
            c1Threads.get(i).start();
        }
        for(MABEDComponent1 c1 : c1Threads){
            try {
                c1.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(MABED.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        MABEDEventList basicEvents = new MABEDEventList();
        for(MABEDComponent1 c1 : c1Threads){
            basicEvents.addAll(c1.events);
        }
        basicEvents.sort();
        c1Threads.clear();
        
        int nbFinalEvents = 0;
        int i = 0;
        MABEDEventGraph eventGraph = null;
        if(basicEvents.size() > 0){
            eventGraph = new MABEDEventGraph(basicEvents.get(0).score, sigma);
            while(nbFinalEvents < k && i < basicEvents.size()-Configuration.numberOfCores){
                int numberOfC2Threads = ((k - nbFinalEvents)<=Configuration.numberOfCores)?(k-nbFinalEvents):Configuration.numberOfCores;
                MABEDEvent[] refinedEvents = new MABEDEvent[numberOfC2Threads];
                LinkedList<MABEDComponent2> c2Threads = new LinkedList<>();
                for(int j = 0; j < numberOfC2Threads; j++){
                    c2Threads.add(new MABEDComponent2(j,basicEvents.get(i+j),p,theta));
                    c2Threads.get(j).start();
                }
                for(MABEDComponent2 c2 : c2Threads){
                    try {
                        c2.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MABED.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                for(MABEDComponent2 c2 : c2Threads){
                    refinedEvents[c2.threadId] = c2.refinedEvent;
                }
                for(MABEDEvent refinedEvent : refinedEvents){
                    if(refinedEvent.relatedTerms.size() >= _MIN_RELATED_WORDS_){
                        nbFinalEvents += eventGraph.addEvent(refinedEvent);
                    }
                    i++;
                }
            }
            mergeRedundantEvents(eventGraph);
            MABEDEventList eventList = eventGraph.toEventList();
            for(MABEDEvent event : eventList.list){
                scores.put(new Event(event.mainTerm+" "+event.relatedTermAsList(),AppParameters.dataset.corpus.convertTimeSliceToDay(event.I.timeSliceA)+","+AppParameters.dataset.corpus.convertTimeSliceToDay(event.I.timeSliceB)), event.score);
            }
            scores = HashMapUtils.sortByDescValue(scores);
            Set<Map.Entry<Event, Double>> entrySet = scores.entrySet();
            events = new Events();
            for (Map.Entry<Event, Double> entry : entrySet) {
                events.list.add(entry.getKey());
            }
            events.setFullList();
        }
    }
    
    void mergeRedundantEvents(MABEDEventGraph eventGraph){
        eventGraph.identifyConnectedComponents();
    }
}
