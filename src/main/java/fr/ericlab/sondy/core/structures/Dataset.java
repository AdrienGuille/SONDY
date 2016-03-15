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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class Dataset {
    public String id;
    public String description;
    public Corpus corpus;
    public Network network;
    public int userCount;
    public ObservableList<String> preprocessedCorpusList;
    public Path path;
    
    public Dataset(){
        id = "no dataset selected";
        corpus = new Corpus();
        network = new Network();
        preprocessedCorpusList = FXCollections.observableArrayList();
    }
    
    public void load(String datasetId){
        id = datasetId;
        path = Paths.get(Configuration.datasets+File.separator+id);
        String propertiesFilePath = Paths.get(path+File.separator+"dataset.properties").toString();
        description = PropertiesFileUtils.readProperty(propertiesFilePath,"description");
        corpus.loadProperties(path);
        network.loadProperties(path);
        updatePreprocessedCorpusList();
    }
    
    public void updatePreprocessedCorpusList(){
        preprocessedCorpusList.clear();
        File file = path.toFile();
        String[] directories = file.list((File current, String name) -> new File(current, name).isDirectory());
        preprocessedCorpusList.addAll(Arrays.asList(directories));
        preprocessedCorpusList.remove("network");
    }
    
    public String[] create(HashMap<String,String> datasetProperties){
        String datasetId = datasetProperties.get("id");
        File dir = Paths.get(Configuration.datasets+File.separator+datasetId).toFile();
        String messagesFilePath = datasetProperties.get("messagesFile");
        String networkFilePath = datasetProperties.get("networkFile");
        String[] log = {"Messages: error.","Network: error."};
        if(!dir.exists() && messagesFilePath!=null){
            log[0] = corpus.create(datasetId, datasetProperties.get("messagesFile"));
            log[1] = network.create(datasetId, datasetProperties.get("networkFile"));
            PropertiesFileUtils.writeProperty(Paths.get(Configuration.datasets+File.separator+datasetId+File.separator+"dataset.properties").toString(),"description",datasetProperties.get("description"));
            return log;
        }
        return log;
    }

    public String preprocess(String stemming, String lemmatization, int ngram, int timeSliceLength){
        return corpus.preprocess(path,stemming,lemmatization,ngram,timeSliceLength);
    }

}
