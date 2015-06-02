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

import main.java.fr.ericlab.sondy.algo.Parameters;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import main.java.fr.ericlab.sondy.core.structures.RankedUsers;
import org.graphstream.graph.Node;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public abstract class InfluenceAnalysisMethod implements Runnable {

    public abstract String getName();
    public abstract String getCitation();
    public abstract String getDescription();
    public Parameters parameters;
    public RankedUsers rankedUsers;
    String log;
    
    @Override
    public void run(){
        rankedUsers = new RankedUsers();
        apply();
        colorNodes();
    }
    
    public InfluenceAnalysisMethod(){
        parameters = new Parameters();
        log = "";
    }
    
    public void colorNodes(){
        for(Node node : AppParameters.authorNetwork.getNodeSet()){
            node.addAttribute("ui.color", (double)rankedUsers.getRank(node.getId())/(double)rankedUsers.maxRank);
        }
    }
    
    public abstract void apply();
    
    public String getLog(){
        return AppParameters.authorNetwork.getNodeCount()+" users, "+AppParameters.authorNetwork.getEdgeCount()+" relationships "+parameters.toString();
    }
}
