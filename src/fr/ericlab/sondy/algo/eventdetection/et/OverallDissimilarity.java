/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.algo.eventdetection.et;

import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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
public class OverallDissimilarity implements DissimilarityMeasure {
    
    double alpha = 0.7;
    double beta = 0.3;
    
    public OverallDissimilarity(double a, double b){
        alpha = a;
        beta = b;
    }

    @Override
    public double computeDissimilarity(Experiment e, int ik1, int ik2) {
        ETExperiment et = (ETExperiment)e;
        String k1 = et.bigrams.get(ik1);
        String k2 = et.bigrams.get(ik2);
        Bursts b1 = et.mapBursts.get(k1);
        Bursts b2 = et.mapBursts.get(k2);
        double ASim = getAppearancePatternsSimilarity(b1,b2);
        double Csim = getContentSimilarity(et.mapCooccurences,k1,k2);
        return 1-(alpha*Csim+beta*ASim);
    }
    
    public static double getContentSimilarity(HashMap<String,LinkedList<String>> mapCooccurences, String k1, String k2){        
        LinkedList<String> FCB1 = mapCooccurences.get(k1);
        LinkedList<String> FCB2 = mapCooccurences.get(k2);
        HashSet<String> union = new HashSet<>();
        double intersectionSize = 0;
        for(String bigram : FCB1){
            union.add(bigram);
        }
        for(String bigram : FCB2){
            union.add(bigram);
            if(FCB1.contains(bigram)){
                intersectionSize++;
            }
        }
        double unionSize = union.size();
        if(unionSize > 0){
            return intersectionSize/unionSize;
        }else{
            return 0;
        }
    }
    
    public static double getAppearancePatternsSimilarity(Bursts b1, Bursts b2){
        HashSet<Integer> union = new HashSet<>();
        LinkedList<Integer> b1U = new LinkedList<>();
        double intersectionSize = 0;
        for(Burst burst : b1.list){
            union.add(burst.U);
            b1U.add(burst.U);
        }
        for(Burst burst : b2.list){
            union.add(burst.U);
            if(b1U.contains(burst.U)){
                intersectionSize++;
            }
        }
        double unionSize = union.size();
        if(unionSize > 0){
            return 1-intersectionSize/unionSize;
        }else{
            return 1;
        }
    }
    
}
