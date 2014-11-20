/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection;

import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.DataManipulation;
import fr.ericlab.sondy.core.access.IndexAccess;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

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

public class MACD {
    
    public float[][] apply(AppVariables appVariables, IndexAccess indexAccess, String term, int shortWindow, int longWindow, float exponent, int smooth){
        try {
            TermDocs termDocs = indexAccess.reader.termDocs();
            termDocs.seek(new Term("content", term));
            int numDocs = indexAccess.reader.numDocs();
            float[] freqBase = new float[numDocs];
            float[] freqLongAverage = new float[numDocs];
            float[] freqShortAverage = new float[numDocs];
            float[] trendingMomentum = new float[numDocs];
            float[] momentum = new float[numDocs];
            float[][] global = new float[6][2];
            DataManipulation dataManipulation = new DataManipulation();
            float[] termFrequency = indexAccess.getTermFrequency(appVariables,term);
            for(int i = 0; i<numDocs; i++){
                freqBase[i] = termFrequency[i];
                freqLongAverage[i] = movingAverage(freqBase,i,longWindow);
                freqShortAverage[i] = movingAverage(freqBase,i,shortWindow);
                trendingMomentum[i] = (float) (freqShortAverage[i]-Math.pow(freqLongAverage[i],exponent));
                momentum[i] = centeredMovingAverage(trendingMomentum,i,smooth);
            }
            global[0] = freqBase;
            global[1] = freqShortAverage;
            global[2] = freqLongAverage;
            global[3] = trendingMomentum;
            global[4] = momentum;
            int maximumPoint = globalMaximumPoint(momentum);
            int minimumPoint = globalMinimumPoint(momentum);
            global[5][0] = minimumPoint;
            global[5][1] = maximumPoint;
            int periodStart, periodEnd;
            if(maximumPoint > minimumPoint){
                periodStart = inflectionPointBefore(freqBase,minimumPoint);
                periodEnd = inflectionPointAfter(freqBase,maximumPoint);
            }else{
                periodEnd = inflectionPointBefore(freqBase,minimumPoint);
                periodStart = inflectionPointAfter(freqBase,maximumPoint);
            }
            if(periodEnd>periodStart){
                global[5][0] = periodStart;
                global[5][1] = periodEnd;
            }else{
                global[5][0] = periodEnd;
                global[5][1] = periodStart;
            }
            return global;
        } catch (IOException ex) {
            Logger.getLogger(MACD.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public float movingAverage(float[] tab, int index, int windowSize){
        int possibleWindow = (index>=windowSize)?windowSize:index;
        float total = 0;
        for(int currentIndex = index-possibleWindow; currentIndex<index; currentIndex++ ){
            total += tab[currentIndex];
        }
        if(possibleWindow==0){
            return tab[0];
        }else{
            return total/(float)possibleWindow;
        }
    }
    
    public float centeredMovingAverage(float[] tab, int index, int windowSize){
        int halfWindowSize = windowSize/2;
        int possibleLeftWindow = (index >= halfWindowSize)?halfWindowSize:index;
        int possibleRightWindow = (index+halfWindowSize < tab.length)? halfWindowSize:tab.length-1-index;
        int i1 = index - possibleLeftWindow, i2 = index + possibleRightWindow;
        float total = 0;
        for(int i = i1; i <= i2; i++){
            total += tab[i];
        }
        return total/(float)(possibleLeftWindow+possibleRightWindow);
    }
    
    public int globalMaximumPoint(float[] tab){
        int maxPoint = 0;
        float maxValue = tab[0];
        for(int i = 0; i<tab.length; i++){
            if(tab[i]>maxValue){
                maxPoint = i;
                maxValue = tab[i];
            }
        }
        return maxPoint;
    }
    
    public int localMinimumPointBefore(float[] tab, int index){
        if(index>0){
            for(int i = index-1; i>=0; i--){
                if(tab[i]>tab[i+1]){
                    return i+1;
                }
            }
        }
        return -1;
    }
    
    public int localMinimumPointAfter(float[] tab, int index){
        if(index<tab.length-1){
            for(int i = index+1; i<tab.length-1; i++){
                if(tab[i+1]>tab[i]){
                    return i;
                }
            }
        }
        return -1;
    }
    
    public int globalMinimumPoint(float[] tab){
        int minPoint = 0;
        float minValue = tab[0];
        for(int i = 0; i<tab.length; i++){
            if(tab[i]<minValue){
                minPoint = i;
                minValue = tab[i];
            }
        }
        return minPoint;
    }
    
    public int inflectionPointBefore(float[] tab, int index){
        int pointIndex = - 1;
        if(index>0){
            int sign = (tab[index]>0)?1:-1;
            for(int i = index-1; i>=0; i--){
                int currentSign = (tab[i]>0)?1:-1;
                if((sign+currentSign) == 0){
                    return i;
                }
            }
        }
        return pointIndex;
    }
    
    public int inflectionPointAfter(float[] tab, int index){
        int pointIndex = - 1;
        if(index<tab.length-1){
            int sign = (tab[index]>0)?1:-1;
            for(int i = index+1; i<tab.length; i++){
                int currentSign = (tab[i]>0)?1:-1;
                if((sign+currentSign) == 0){
                    return i;
                }
            }
        }
        return pointIndex;
    }
}
