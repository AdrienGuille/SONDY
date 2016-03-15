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
package main.java.fr.ericlab.sondy.core.app;

import main.java.fr.ericlab.sondy.core.utils.PropertiesFileUtils;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class Configuration {
    public static Path workspace;
    public static Path stopwordSets;
    public static Path datasets;
    public static Path currentDataset;
    public static int numberOfCores;
    
    public static void initialize(){
        workspace = Paths.get(PropertiesFileUtils.readProperty(Paths.get("./configuration.properties").toString(),"workspace"));
        setWorkspace(workspace.toString());
        numberOfCores = Runtime.getRuntime().availableProcessors()/2;
        numberOfCores = (numberOfCores<1)?1:numberOfCores;
    }
    
    public static void setWorkspace(String path){
        workspace = Paths.get(path);
        stopwordSets = Paths.get(workspace+"/stopwordLists");
        datasets = Paths.get(workspace+"/datasets");
        currentDataset = null;
        stopwordSets.normalize();
        datasets.normalize();
        mkdir(workspace);
        mkdir(stopwordSets);
        mkdir(datasets);
    }
    
    public static void changeWorkspace(String newPath){
        PropertiesFileUtils.writeProperty(Paths.get("./configuration.properties").toString(),"workspace",newPath);
        setWorkspace(newPath);
    }
    
    public static void mkdir(Path path){
        File file = path.toFile();
        if(!file.exists()){
            file.mkdir();
        }
    }
    
    public static Path getDatasetPath(String datasetName, String preprocessing){
        return Paths.get(datasets.toString()+"/"+datasetName+"/"+preprocessing).normalize();
    }
    
    
}
