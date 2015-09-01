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
package main.java.fr.ericlab.sondy.algo.eventdetection.mabed;

import main.java.fr.ericlab.sondy.core.app.AppParameters;
import main.java.fr.ericlab.sondy.core.text.index.CalculationType;
import main.java.fr.ericlab.sondy.core.utils.ArrayUtils;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class MABEDComponent1 extends Thread {
    public MABEDEventList events = new MABEDEventList();
    int from;
    int to;
    int minTermOccur;
    int maxTermOccur;
    int threadId;
    
    public MABEDComponent1(int id, int a, int b, int min, int max){
        from = a;
        to = b;
        minTermOccur = min;
        maxTermOccur = max;
        threadId = id;
    }
    
    float expectation(int timeSlice, float tmf){
        return AppParameters.dataset.corpus.termFrequencies.get(CalculationType.Mention).getNumberOfDocuments()[timeSlice]*(tmf/AppParameters.dataset.corpus.messageCount);
    }
    
    float anomaly(float expectation, float real){
        return real - expectation;
    }
    
    @Override
    public void run() {
        int m = AppParameters.timeSliceB;
        for(int t = from; t <= to; t++){
            String term = AppParameters.dataset.corpus.termFrequencies.get(CalculationType.Mention).getTerms().get(t);
            if(term.length() > 2 && !AppParameters.stopwords.contains(term)){
                Short[] mf = AppParameters.dataset.corpus.getTermFrequency(CalculationType.Mention, term);
                int tmf = AppParameters.dataset.corpus.termFrequencies.get(CalculationType.Mention).getTotalTermFrequency(t);
                int tgf = tmf;
                if(tgf>minTermOccur && tgf<maxTermOccur){
                    float expectation;
                    float scoreSequence[] = new float[m];
                    for(int i = AppParameters.timeSliceA; i < m; i++){
                        expectation = expectation(i,tmf);
                        scoreSequence[i] = anomaly(expectation, mf[i]);
                    }
                    LinkedList<MABEDTimeInterval> I = new LinkedList<>();
                    LinkedList<Float> L = new LinkedList<>();
                    LinkedList<Float> R = new LinkedList<>();
                    ArrayList<Float> anomaly = new ArrayList<>();
                    for(int i = AppParameters.timeSliceA; i < m; i++){
                        anomaly.add(scoreSequence[i]>0?scoreSequence[i]:0);
                        if(scoreSequence[i]>0){
                            int k = I.size();
                            float Lk = 0, Rk = ArrayUtils.sum(scoreSequence,0,i);
                            if(i>0){
                                Lk = ArrayUtils.sum(scoreSequence,0,i-1);
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
                                MABEDTimeInterval Ik = new MABEDTimeInterval(I.get(j).timeSliceA,i);
                                for(int p = j; p<k; p++){
                                    I.removeLast();
                                    L.removeLast();
                                    R.removeLast();
                                }
                                k = j;
                                I.add(Ik);
                                L.add(ArrayUtils.sum(scoreSequence,0,Ik.timeSliceA-1));
                                R.add(ArrayUtils.sum(scoreSequence,0,Ik.timeSliceB));
                            }else{
                                I.add(new MABEDTimeInterval(i,i));
                                L.add(Lk);
                                R.add(Rk);
                            }
                        }
                    }
                    if(I.size()>0){
                        MABEDTimeInterval maxI = I.get(0);
                        for(MABEDTimeInterval Ii : I){
                            if(ArrayUtils.sum(scoreSequence,Ii.timeSliceA,Ii.timeSliceB)>ArrayUtils.sum(scoreSequence,maxI.timeSliceA,maxI.timeSliceB)){
                                maxI.timeSliceA = Ii.timeSliceA;
                                maxI.timeSliceB = Ii.timeSliceB;
                            }
                        }
                        double score = ArrayUtils.sum(scoreSequence,I.get(0).timeSliceA,I.get(0).timeSliceB);
                        events.add(new MABEDEvent(term,maxI,score,anomaly));
                    }
                }
            }
        }
    }
}
