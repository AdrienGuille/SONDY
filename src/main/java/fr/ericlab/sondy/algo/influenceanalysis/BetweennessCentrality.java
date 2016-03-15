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
package main.java.fr.ericlab.sondy.algo.influenceanalysis;

import java.util.HashMap;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import org.graphstream.graph.Node;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class BetweennessCentrality extends InfluenceAnalysisMethod {

    public BetweennessCentrality(){
        super();
    }
    
    @Override
    public String getName() {
        return "Betweenness Centrality";
    }

    @Override
    public String getCitation() {
        return "<li><b>Betweenness centrality:</b> L. Freeman (1977) A set of measures of centrality based on betweenness. Sociometry, vol. 40 pp. 35-41</li>";
    }

    @Override
    public String getDescription() {
        return "Computes the number of shortest paths from all users to all others that pass through each user";
    }

    @Override
    public void apply() {
        org.graphstream.algorithm.BetweennessCentrality bcb = new org.graphstream.algorithm.BetweennessCentrality();
        bcb.init(AppParameters.authorNetwork);
        bcb.compute();
        for (Node node : AppParameters.authorNetwork) {
            double rank = node.getAttribute("Cb");
            rankedUsers.add(node.getId(),(int)(rank));
        }
    }

}
