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
import main.java.fr.ericlab.sondy.core.utils.HashMapUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import main.java.fr.ericlab.sondy.core.structures.Events;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class PeakyTopics extends EventDetectionMethod {
    double minTermSupport = 0.0001;
    double maxTermSupport = 0.01;
    
    public PeakyTopics(){
        super();
        parameters.add(new Parameter("minTermSupport",minTermSupport+""));
        parameters.add(new Parameter("maxTermSupport",maxTermSupport+""));
    }

    @Override
    public String getName() {
        return "Peaky Topics";
    }

    @Override
    public String getCitation() {
        return "<li><b>Peaky Topics:</b> David A. Shamma, Lyndon Kennedy, Elizabeth F. Churchill (2011) Peaks and persistence: modeling the shape of microblog conversations, In Proceedings of the 2011 ACM Conference on Computer Supported Cooperative Work (CSCW), pp. 355-358</li>";
    }
    
    @Override
    public String getDescription() {
        return "Identifies highly localized events using a normalized term frequency based metric";
    }

    @Override
    public void apply() {
        double minTermOccur = parameters.getParameterValue("minTermSupport") * AppParameters.dataset.corpus.messageCount;
        double maxTermOccur = parameters.getParameterValue("maxTermSupport") * AppParameters.dataset.corpus.messageCount;
        Map<Event,Double> scores = new HashMap<>();
        for(int i = AppParameters.timeSliceA; i < AppParameters.timeSliceB; i++){
            String term = AppParameters.dataset.corpus.vocabulary.get(i);
            if(term.length()>1 && !AppParameters.stopwords.contains(term)){
                double tf = 0, cf = 0;
                int peakIndex = 0;
                for(int j = 0; j < AppParameters.dataset.corpus.messageDistribution.length; j++){
                    cf += AppParameters.dataset.corpus.termFrequencies[i][j];
                    if(AppParameters.dataset.corpus.termFrequencies[i][j]>tf){
                        tf = AppParameters.dataset.corpus.termFrequencies[i][j];
                        peakIndex = j;
                    }
                }
                if(cf > minTermOccur && cf < maxTermOccur){
                    scores.put(new Event(term,AppParameters.dataset.corpus.convertTimeSliceToDay(peakIndex)+","+AppParameters.dataset.corpus.convertTimeSliceToDay(peakIndex+1)+""), tf/cf);
                }
            }
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
