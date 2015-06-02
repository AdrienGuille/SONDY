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
package main.java.fr.ericlab.sondy.core.utils;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class ArrayUtils {
    static public int sum(short tab[], int a, int b){
        int sum = 0;
        for(int i = a; i <= b; i++){
            sum += tab[i];
        }
        return sum;
    }
    
    static public float sum(float tab[], int a, int b){
        float sum = 0;
        for(int i = a; i <= b; i++){
            sum += tab[i];
        }
        return sum;
    }
    
    static public float[] toFloatArray(short[] array){
        float[] newArray = new float[array.length];
        for(int i = 0; i < array.length; i++){
            newArray[i] = array[i];
        }
        return newArray;
    }
    
    static public float[] smoothArray(short array[], int windowSize){
        float[] smoothedArray = new float[array.length];
        for(int i = 0; i < array.length-1; i++){
            smoothedArray[i] = centeredMovingAverage(array, i, windowSize);
        }
        return smoothedArray;
    }
    
    static public float centeredMovingAverage(short[] array, int index, int windowSize){
        int halfWindowSize = windowSize/2;
        int possibleLeftWindow = (index >= halfWindowSize)?halfWindowSize:index;
        int possibleRightWindow = (index+halfWindowSize < array.length-1)? halfWindowSize:array.length-2-index;
        int i1 = index - possibleLeftWindow, i2 = index + possibleRightWindow;
        float total = 0;
        for(int i = i1; i <= i2; i++){
            total += array[i];
        }
        return total/(float)(possibleLeftWindow+possibleRightWindow);
    }
}
