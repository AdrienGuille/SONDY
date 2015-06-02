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

import java.util.Collection;
import java.util.HashSet;
import main.java.fr.ericlab.sondy.algo.Parameter;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class SocialCapitalists extends InfluenceAnalysisMethod {
    double overlapThreshold = 0.74;
    double rankScale = 10;

    public SocialCapitalists(){
        super();
        parameters.add(new Parameter("overlapThreshold",overlapThreshold+""));
        parameters.add(new Parameter("rankScale",rankScale+""));
    }
    
    @Override
    public String getName() {
        return "Social Capitalists";
    }

    @Override
    public String getCitation() {
        return "<li><b>Social Capitalists:</b> N. Dugué and A. Perez (2014) Social capitalists on Twitter: detection, evolution and behavioral analysis, Social Network Analysis and Mining, vol. 4(1) pp. 178-191</li>";
    }

    @Override
    public String getDescription() {
        return "Identifies users who might be social capitalists based on neighborhood comparisons";
    }

    @Override
    public void apply() {
        overlapThreshold = parameters.getParameterValue("overlapThreshold");
        rankScale = parameters.getParameterValue("rankScale");
        for (Node node : AppParameters.authorNetwork) {
            Collection<Edge> enteringEdgeSet = node.getEnteringEdgeSet();
            HashSet<String> A = new HashSet<>();
            for(Edge edge : enteringEdgeSet){
                A.add(edge.getSourceNode().getId());
            }
            Collection<Edge> leavingEdgeSet = node.getLeavingEdgeSet();
            HashSet<String> B = new HashSet<>();
            double intersection = 0;
            for(Edge edge : leavingEdgeSet){
                B.add(edge.getTargetNode().getId());
                if(A.contains(edge.getTargetNode().getId())){
                    intersection++;
                }
            }
            double overlap = intersection/Math.min(A.size(),B.size());
            overlap = (overlap>overlapThreshold)?overlap *rankScale:0;
            rankedUsers.add(node.getId(),(int)(overlap));
        }
    }

}
