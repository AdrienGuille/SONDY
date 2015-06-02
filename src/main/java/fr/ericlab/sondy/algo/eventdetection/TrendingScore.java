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
public class TrendingScore extends EventDetectionMethod {
    double minTermSupport = 0.0001;
    double maxTermSupport = 0.01;
    double trendingThreshold = 10;
    
    public TrendingScore(){
        super();
        parameters.add(new Parameter("minTermSupport",minTermSupport+""));
        parameters.add(new Parameter("maxTermSupport",maxTermSupport+""));
        parameters.add(new Parameter("trendingThreshold",trendingThreshold+""));
    }

    @Override
    public String getName() {
        return "Trending Score";
    }

    @Override
    public String getCitation() {
        return "<li><b>Trending Score:</b> J. Benhardus J. and J. Kalita (2013) Streaming trend detection in Twitter, International Journal of Web Based Communities, 9(1), pp. 122-139.</li>";
    }
    
    @Override
    public String getDescription() {
        return "Scores event-related terms using a normalized term frequency based metric";
    }

    @Override
    public void apply() {
        double minTermOccur = parameters.getParameterValue("minTermSupport") * AppParameters.dataset.corpus.messageCount;
        double maxTermOccur = parameters.getParameterValue("maxTermSupport") * AppParameters.dataset.corpus.messageCount;
        trendingThreshold = parameters.getParameterValue("trendingThreshold");
        Map<Event,Double> scores = new HashMap<>();
        int[] nbTermsPerTimeSlice = new int[AppParameters.dataset.corpus.messageDistribution.length];
        for(int i = AppParameters.timeSliceA; i < AppParameters.timeSliceB; i++){
            nbTermsPerTimeSlice[i] = AppParameters.dataset.corpus.getNumberOfTermsInTimeSlice(i);
        }
        for(int i = AppParameters.timeSliceA; i < AppParameters.timeSliceB; i++){
            String term = AppParameters.dataset.corpus.vocabulary.get(i);
            if(term.length()>1 && !AppParameters.stopwords.contains(term)){
                short[] frequency = AppParameters.dataset.corpus.termFrequencies[i];
                int cf = 0;
                for(short freq : frequency){
                    cf += freq;
                }
                if(cf > minTermOccur && cf < maxTermOccur){
                    double[] tfnorm = new double[AppParameters.dataset.corpus.messageDistribution.length];
                    double tfnormTotal = 0;
                    double[] trendingScore = new double[AppParameters.dataset.corpus.messageDistribution.length];
                    for(int j = 0; j < AppParameters.dataset.corpus.messageDistribution.length; j++){
                        tfnorm[j] = ((double)frequency[j]/nbTermsPerTimeSlice[j])*Math.pow(10, 6);
                        tfnormTotal += tfnorm[j];
                    }
                    for(int j = 0; j < AppParameters.dataset.corpus.messageDistribution.length; j++){
                        trendingScore[j] = tfnorm[j]/((tfnormTotal - tfnorm[j])/(AppParameters.dataset.corpus.messageDistribution.length-1));
                        if(trendingScore[j] > trendingThreshold){
                            scores.put(new Event(term,AppParameters.dataset.corpus.convertTimeSliceToDay(j)+","+AppParameters.dataset.corpus.convertTimeSliceToDay(j+1)),trendingScore[j]);
                        }
                    }
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
