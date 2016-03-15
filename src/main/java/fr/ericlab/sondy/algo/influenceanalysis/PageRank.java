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

import main.java.fr.ericlab.sondy.algo.Parameter;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import org.graphstream.graph.Node;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class PageRank extends InfluenceAnalysisMethod {
    double dampingFactor = 0.15;
    double rankCoefficient = 3;

    public PageRank(){
        super();
        parameters.add(new Parameter("dampingFactor",dampingFactor+""));
        parameters.add(new Parameter("rankCoefficient",rankCoefficient+""));
    }
    
    @Override
    public String getName() {
        return "Page Rank";
    }

    @Override
    public String getCitation() {
        return "<li><b>Page-Rank:</b> L. Page, S. Brin, R. Motwani, and T. Winograd (1998) The pagerank citation ranking: Bringing order to the web, In Proceedings of the 1998 ACM International World Wide Web Conference, pp. 161–172</li>";
    }

    @Override
    public String getDescription() {
        return "Computes the probability that a 'random surfer' visits each node to estimate how important users are";
    }

    @Override
    public void apply() {
        dampingFactor = parameters.getParameterValue("dampingFactor");
        rankCoefficient = parameters.getParameterValue("rankCoefficient");
        org.graphstream.algorithm.PageRank pageRank = new org.graphstream.algorithm.PageRank();
        pageRank.setDampingFactor(dampingFactor);
        pageRank.init(AppParameters.authorNetwork);
        pageRank.compute();
        int nbNodes = AppParameters.authorNetwork.getNodeCount();
        for (Node node : AppParameters.authorNetwork) {
            double rank = pageRank.getRank(node);
            rankedUsers.add(node.getId(),(int)(rank*nbNodes*rankCoefficient));
        }
    }

}
