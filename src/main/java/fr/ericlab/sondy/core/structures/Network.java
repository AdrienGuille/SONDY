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
package main.java.fr.ericlab.sondy.core.structures;

import main.java.fr.ericlab.sondy.core.app.Configuration;
import main.java.fr.ericlab.sondy.core.utils.PropertiesFileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import org.apache.commons.io.FileUtils;
import org.graphstream.graph.Node;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class Network {
    // Properties
    public int relationshipCount;
    public Path path;
    
    public String[] splitString(String str) {
        return str.split("\t");
    }
    
    public void loadProperties(Path p) {
        path = p;
        String propertiesFilePath = Paths.get(path+File.separator+"network.properties").toString();
        relationshipCount = Integer.parseInt(PropertiesFileUtils.readProperty(propertiesFilePath, "relationshipCount"));
    }
    
    public String create(String id, String csvFilePath) {
        Properties properties = new Properties();
        if(csvFilePath != null){
            File dir = Paths.get(Configuration.datasets.toString() + File.separator + id + File.separator + "network").toFile();
            dir.mkdir();
            int skippedLineCount = 0;
            try {
                List<String> lines = FileUtils.readLines(new File(csvFilePath));
                relationshipCount = 0;
                for(String line : lines) {
                    String[] components = splitString(line);
                    if (components.length == 2) {
                        FileUtils.writeStringToFile(new File(dir.getAbsolutePath() + File.separator +components[0]+".connectedusers"), components[1] + "\n", true);
                        relationshipCount++;
                    }else{
                        skippedLineCount++;
                    }
                }
                properties.setProperty("relationshipCount", relationshipCount+"");
                PropertiesFileUtils.saveProperties(Paths.get(Configuration.datasets + File.separator + id + File.separator + "network.properties").toString(),properties);
            } catch (IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
            return "Network: done (imported "+relationshipCount+" relationships, skipped "+skippedLineCount+" misformatted lines).";
        }
        properties.setProperty("relationshipCount", "0");
        PropertiesFileUtils.saveProperties(Paths.get(Configuration.datasets + File.separator + id + File.separator + "network.properties").toString(),properties);
        return "Network: empty.";
    }
    
    public void clearAuthorNetwork(){
        Collection<Node> nodeSet = AppParameters.authorNetwork.getNodeSet();
        HashSet<String> idSet = new HashSet<>();
        for(Node node : nodeSet){
            idSet.add(node.getId());
        }
        for(String id : idSet){
            AppParameters.authorNetwork.removeNode(id);
        }
    }
    
    public void updateAuthorNetwork(){
        try {
            if(!AppParameters.authorNetwork.getAttribute("event").equals(AppParameters.event.getTextualDescription()+AppParameters.event.getTemporalDescription())){
                clearAuthorNetwork();
                AppParameters.authorNetwork.setAttribute("event", AppParameters.event.getTextualDescription()+AppParameters.event.getTemporalDescription());
                HashSet<String> authors = AppParameters.dataset.corpus.getAuthors(AppParameters.event);
                for(String author : authors){
                    AppParameters.authorNetwork.addNode(author);
                }
                for(String author : authors){
                    File authorFile = new File(path+File.separator+"network"+File.separator+author+".connectedusers");
                    if(authorFile.exists()){
                        List<String> connectedUsers = FileUtils.readLines(authorFile);
                        for(String user : connectedUsers){
                            if(authors.contains(user)){
                                AppParameters.authorNetwork.addEdge(user+"->"+author,user,author,true);
                            }
                        }
                    }
                }
                Collection<Node> nodeSet = AppParameters.authorNetwork.getNodeSet();
                HashSet<String> isolatedNodes = new HashSet<>();
                for(Node node : nodeSet){
                    if(node.getDegree() == 0){
                        isolatedNodes.add(node.getId());
                    }
                }
                for(String id : isolatedNodes){
                    AppParameters.authorNetwork.removeNode(id);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
