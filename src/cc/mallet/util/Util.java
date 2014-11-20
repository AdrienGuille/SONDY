/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package cc.mallet.util;

/**
 * 
 * 
 * @author <a href="mailto:casutton@cs.umass.edu">Charles Sutton</a>
 * @version $Id: ArrayUtils.java,v 1.1 2007/10/22 21:37:40 mccallum Exp $
 */
public class Util {
    /**
     * Returns the Jensen-Shannon divergence.
     */
    public static double jensenShannonDivergence(double[] p1, double[] p2) {
      assert(p1.length == p2.length);
      double[] average = new double[p1.length];
      for (int i = 0; i < p1.length; ++i) {
        average[i] += (p1[i] + p2[i])/2;
      }
      return (klDivergence(p1, average) + klDivergence(p2, average))/2;
    }

    
   public static final double log2 = Math.log(2);
    /**
     * Returns the KL divergence, K(p1 || p2).
     *
     * The log is w.r.t. base 2. <p>
     *
     * *Note*: If any value in <tt>p2</tt> is <tt>0.0</tt> then the KL-divergence
     * is <tt>infinite</tt>. Limin changes it to zero instead of infinite. 
     * 
     */
    public static double klDivergence(double[] p1, double[] p2) {


      double klDiv = 0.0;

      for (int i = 0; i < p1.length; ++i) {
        if (p1[i] == 0) { continue; }
        if (p2[i] == 0.0) { continue; } // Limin

      klDiv += p1[i] * Math.log( p1[i] / p2[i] );
      }

      return klDiv / log2; // moved this division out of the loop -DM
    }
}