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
import main.java.fr.ericlab.sondy.core.structures.DocumentTermFrequencyItem;
import main.java.fr.ericlab.sondy.core.structures.DocumentTermMatrix;
import main.java.fr.ericlab.sondy.core.structures.Event;
import main.java.fr.ericlab.sondy.algo.Parameter;
import main.java.fr.ericlab.sondy.core.text.index.CalculationType;
import main.java.fr.ericlab.sondy.core.utils.HashMapUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import main.java.fr.ericlab.sondy.core.structures.Events;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class PeakyTopics extends EventDetectionMethod {
    double minTermSupport = 0.0001;
    double maxTermSupport = 0.01;
    double peakMinPercentage = 0.5;
    
    public PeakyTopics() {
        super();
        parameters.add(new Parameter("minTermSupport", minTermSupport + ""));
        parameters.add(new Parameter("maxTermSupport", maxTermSupport + ""));
        parameters.add(new Parameter("peakMinPercentage", peakMinPercentage + ""));
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
        peakMinPercentage = parameters.getParameterValue("peakMinPercentage");
        events = new Events();
        IntStream.range(0, AppParameters.dataset.corpus.termFrequencies.get(CalculationType.Existence).getTerms().size())
            .parallel()
            .mapToObj(i -> {
                String term = AppParameters.dataset.corpus.termFrequencies.get(CalculationType.Existence).getTerms().get(i);
                if (term.length() > 1 && !AppParameters.stopwords.contains(term)) {
                    double cf = 0;
                    List<DocumentTermFrequencyItem> itmes = AppParameters.dataset.corpus.termFrequencies.get(CalculationType.Existence)
                        .getDocumentsContainingTerm(i)
                        .stream()
                        .filter(dti -> dti.doc_id >= AppParameters.timeSliceA && dti.doc_id < AppParameters.timeSliceB)
                        .collect(Collectors.toList());
                    cf = itmes.stream().mapToInt(dti -> dti.frequency).sum();
                    if (cf > minTermOccur && cf < maxTermOccur) {
                        DocumentTermFrequencyItem peak = itmes.stream().max((dti1, dti2) -> Integer.compare(dti1.frequency, dti2.frequency)).get();
                        if (peak.frequency / cf > peakMinPercentage)
                            return new Event(term, AppParameters.dataset.corpus.convertTimeSliceToDay(peak.doc_id) + "," + AppParameters.dataset.corpus.convertTimeSliceToDay(peak.doc_id + 1) + "", peak.frequency / cf);
                    }
                }
                return null;
            })
            .filter(evt -> evt != null)
            .sorted((evt1, evt2) -> Double.compare(evt2.getScore(), evt1.getScore()))
            .forEachOrdered(evt -> events.list.add(evt));
        events.setFullList();
    }
}