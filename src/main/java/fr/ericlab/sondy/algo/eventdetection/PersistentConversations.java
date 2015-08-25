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
public class PersistentConversations extends EventDetectionMethod {
    double minTermSupport = 0.0001;
    double maxTermSupport = 0.01;
    
    public PersistentConversations(){
        super();
        parameters.add(new Parameter("minTermSupport",minTermSupport+""));
        parameters.add(new Parameter("maxTermSupport",maxTermSupport+""));
    }

    @Override
    public String getName() {
        return "Persistent Conversations";
    }

    @Override
    public String getCitation() {
        return "<li><b>Persistent Conversations:</b> D.A. Shamma, L. Kennedy, E. F. Churchill (2011) Peaks and persistence: modeling the shape of microblog conversations, In Proceedings of the 2011 ACM Conference on Computer Supported Cooperative Work (CSCW), pp. 355-358</li>";
    }
    
    @Override
    public String getDescription() {
        return "Identifies persistent conversations using a normalized term frequency based metric";
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
                    double avgBeforePeak = 0;
                    for(int k = 0; k <= peakIndex; k++){
                        avgBeforePeak += AppParameters.dataset.corpus.termFrequencies[i][k];
                    }
                    avgBeforePeak = avgBeforePeak/(peakIndex+1);
                    double avgAfterPeak = 0;
                    for(int k = peakIndex; k < AppParameters.dataset.corpus.messageDistribution.length; k++){
                        avgAfterPeak += AppParameters.dataset.corpus.termFrequencies[i][k];
                    }
                    avgAfterPeak = avgAfterPeak/(AppParameters.dataset.corpus.messageDistribution.length-peakIndex);
                    scores.put(new Event(term,AppParameters.dataset.corpus.convertTimeSliceToDay(peakIndex)+","+AppParameters.dataset.corpus.convertTimeSliceToDay(peakIndex+1)), avgAfterPeak/avgBeforePeak);
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
