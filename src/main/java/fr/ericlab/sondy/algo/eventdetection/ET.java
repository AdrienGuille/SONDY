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
public class ET extends EventDetectionMethod {
    double minTermSupport = 0.0001;
    double maxTermSupport = 0.01;
    
    public ET(){
        super();
        parameters.add(new Parameter("minTermSupport",minTermSupport+""));
        parameters.add(new Parameter("maxTermSupport",maxTermSupport+""));
    }

    @Override
    public String getName() {
        return "ET";
    }

    @Override
    public String getCitation() {
        return "<li><b>ET:</b> Ruchi P. and Kamalakar K. (2013) ET: Events from tweets, In Proceedings of the 2013 ACM international conference on World Wide Web companion, pp. 613-620.</li>";
    }
    
    @Override
    public String getDescription() {
        return "Detects events based on textual and temporal features using hierarchical clustering";
    }

    @Override
    public void apply() {
        
    }
}
