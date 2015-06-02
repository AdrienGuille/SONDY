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

import main.java.fr.ericlab.sondy.algo.Parameter;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class OnlineLDA extends EventDetectionMethod {

    
    public OnlineLDA(){
        super();
    }

    @Override
    public String getName() {
        return "On-line LDA";
    }

    @Override
    public String getCitation() {
        return "<li><b>On-line LDA:</b> J H Lau, N Collier, T Baldwin (2012) On-line Trend Analysis with Topic Models: #twitter Trends Detection Topic Model Online, In Proceedings of the 2012 International Conference on Computational Linguistics, pp. 1519-1534.</li>";
    }
    
    @Override
    public String getDescription() {
        return "A topic modeling-based methodology to track emerging events in microblogs";
    }

    @Override
    public void apply() {
        
    }
}
