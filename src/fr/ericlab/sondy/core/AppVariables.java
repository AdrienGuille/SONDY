/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core;

import fr.ericlab.sondy.core.access.DBAccess;
import fr.ericlab.sondy.core.access.IndexAccess;
import fr.ericlab.sondy.core.access.MentionIndexAccess;
import fr.ericlab.sondy.core.misc.StopWords;
import fr.ericlab.sondy.core.structure.Configuration;
import fr.ericlab.sondy.core.structure.DetectionResult;
import fr.ericlab.sondy.core.structure.LogEntry;
import fr.ericlab.sondy.core.structure.MessageSet;
import fr.ericlab.sondy.utils.Utils;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

////////////////////////////////////////////////////////////////////////////////
//  This file is part of SONDY.                                               //
//                                                                            //
//  SONDY is free software: you can redistribute it and/or modify             //
//  it under the terms of the GNU General Public License as published by      //
//  the Free Software Foundation, either version 3 of the License, or         //
//  (at your option) any later version.                                       //
//                                                                            //
//  SONDY is distributed in the hope that it will be useful,                  //
//  but WITHOUT ANY WARRANTY; without even the implied warranty of            //
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
//  GNU General Public License for more details.                              //
//                                                                            //
//  You should have received a copy of the GNU General Public License         //
//  along with SONDY.  If not, see <http://www.gnu.org/licenses/>.            //
////////////////////////////////////////////////////////////////////////////////

/**
 *   Singleton class used to communicate between services.
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 *   @author Ivica PADEN, Département Informatique et Statistiques, Université Lumière Lyon 2
 */

public class AppVariables {
    
    // Dataset
    public MessageSet messageSet;
    public String datasetName;
    public String datasetInfo;
    public String datasetDescription;
    public int nbMessages;
    public int nbUsers;
    public int nbRelationships;
    public Text currentDatasetText;
    public Text currentDatasetDiscretizationText;
    public ObservableList<String> preparedStreamList;
    public ObservableList<String> datasetListContent; 
    public String stemmingLanguage = "";
    public boolean lemmatization = false;
    public int intervalDurationMin;
    public HashMap<Integer,Integer> globalIdMap;
    public HashMap<Integer,Integer> mentionIdMap;
    public Text currentDatasetPeriod;
    public float streamDuration;
    public long streamDurationMin;
    public double startDaySelection = 0;
    public double endDaySelection = 0;
    Timestamp[] bounds;
    public int startTimeSlice;
    public int endTimeSlice;
    public int ngram;
    
    // Results
    public String selectedTerm = "";

    public DetectionResult selectedResult;
    public String lastAppliedDetectionAlgo = "";
    public String exportTimeline = "no export";

    // Log
    public ObservableList<LogEntry> logEntries;
    public Date date;
    
    // Stopwords
    public StopWords currentStopWords;
    public ObservableList<String> availableStopWords;
    
    // Global UI
    public Runtime runInfo = Runtime.getRuntime();
    public Text applicationStatus = new Text("");
    public ProgressBar applicationProgress;
    public ImageView databaseStatusImage;
    public ProgressIndicator databaseStatusIndicator = new ProgressIndicator(0);
    TabPane globalPane;
    
    // Detection UI
    public Button applyAlgoButtonDetection = new Button("apply");
            
    // Network UI
    public Button applyAlgoButtonNetwork = new Button("apply");
    
    // Global configuration
    public boolean enableGraphVizualization = true;
    public Configuration configuration;
    private static AppVariables instance;
    
    /**
     * 
     * @param config the configuration 
     */
    private AppVariables(Configuration config, TabPane pane){
        configuration = config;
        currentDatasetText = new Text("");
        datasetListContent = FXCollections.observableArrayList();
        preparedStreamList = FXCollections.observableArrayList();
        logEntries = FXCollections.observableArrayList();
        databaseStatusImage = new ImageView();
        currentDatasetDiscretizationText = new Text("()");
        currentDatasetPeriod = new Text("");
        applicationProgress = new ProgressBar(1);
        applicationProgress.setMaxSize(130, 15);
        applicationProgress.setMinSize(130, 15);
        databaseStatusIndicator.setMinSize(35, 25);
        databaseStatusIndicator.setMaxSize(35, 25);
        globalPane = pane;
    }
    
    /**
     * Returns a new instance of AppVariable if it hasn't already been created.
     * @param config the configuration 
     */
    public static AppVariables getInstance(Configuration config, TabPane pane){
        if (instance == null){
            instance = new AppVariables(config, pane);
        }
        return instance;
    }
    
    /**
     * Loads the dataset chosen by the user. Updates basic information about the new dataset.
     * @param dataset the name of the dataset to load
     */
    public void loadDataset(String dataset){
        if(dataset != null){
            // Fetching dataset properties
            datasetName = dataset;
            messageSet = new MessageSet();
            messageSet.datasetName = dataset;
            currentDatasetText.setText(datasetName);
            currentStopWords = new StopWords();
            stemmingLanguage = "";
            messageSet.stemming = "";
            lemmatization = false;
            messageSet.lemmatization = false;
            DBAccess dbAccess = new DBAccess();
            dbAccess.initialize(this, false);
            bounds = dbAccess.getDatasetBounds(this);
            int info[] = dbAccess.getDatasetInfo(this);
            datasetDescription = dbAccess.getDatasetDescription(this);
            nbMessages = info[0];
            messageSet.nbMessages = info[0];
            nbUsers = info[1];
            messageSet.nbUsers = info[1];
            nbRelationships = info[2];
            messageSet.nbRelationships = info[2];
            float streamDurationMs = (bounds[1].getTime()-bounds[0].getTime())*1L;
            streamDurationMin = (long) ((streamDurationMs/1000)/60);
            streamDuration = ((float)streamDurationMin/60)/24;
            currentDatasetPeriod.setText((streamDuration)+" days of activity from "+(bounds[0].getYear()+1900)+"-"+(bounds[0].getMonth()+1)+"-"+bounds[0].getDate());
            datasetInfo = ""+datasetName+"\n"+NumberFormat.getNumberInstance(Locale.US).format(nbMessages)+" messages\n"+NumberFormat.getNumberInstance(Locale.US).format(nbUsers)+" users\n"+NumberFormat.getNumberInstance(Locale.US).format(nbRelationships)+" relationships"; 
            intervalDurationMin = 0; 
            messageSet.timeSliceLength = 0;
            // Closing access to the database
            dbAccess.close();
        }else{
            currentDatasetText.setText("no dataset selected");
            currentDatasetDiscretizationText.setText("()");
            datasetInfo = "dataset-name\n# messages\n# users\n# relationships"; 
        }
    }
    
    /**
     * Updates the dataset to use the new stream discretization. Maps time-slices numbers with Lucene docIds.
     * @param discretization the name of the new stream discretization (i.e. the name of the Lucene index)
     */
    public void loadDiscretization(String discretization){
        currentDatasetDiscretizationText.setText("("+discretization+")");
        addLogEntry("[data] selected prepared stream: "+discretization);
        String split[] = discretization.split("-");
        if(split.length>=2){
            intervalDurationMin = Integer.parseInt(split[0].substring(0, split[0].length()-3));
            stemmingLanguage = split[1];
            messageSet.stemming = split[1];
            messageSet.timeSliceLength = intervalDurationMin;
        }
        if(discretization.contains("lem1")){
            messageSet.lemmatization = true;
        }else{
            messageSet.lemmatization = false;
        }
        if(discretization.contains("1gram")){
            messageSet.ngram = 1;
        }else{
            if(discretization.contains("2gram")){
                messageSet.ngram = 2;
            }else{
                messageSet.ngram = 3;
            }
        }
        IndexAccess indexAccess = new IndexAccess(this);
        MentionIndexAccess mentionIndexAccess = new MentionIndexAccess(this);
        globalIdMap = indexAccess.getIdsMap();
        mentionIdMap = mentionIndexAccess.getIdsMap();
        startTimeSlice = 0;
        endTimeSlice = indexAccess.reader.numDocs()-1;
        messageSet.nbTimeSlice = indexAccess.reader.numDocs();
        indexAccess.close();
        mentionIndexAccess.close();
    }
    
    /**
     * Returns the timestamp of the first message in the stream.
     * @return the timestamp of the first message
     */
    public Timestamp getStartDay(){
        return bounds[0];
    }
    
    /**
     * Returns the timestamp of the last message in the stream.
     * @return the timestamp of the last message
     */
    public Timestamp getEndDay(){
        return bounds[1];
    }
    
    /**
     * Add a new entry to the log.
     * @param text the textual content of the new log entry
     */
    public void addLogEntry(String text){
        logEntries.add(0,new LogEntry(Utils.getDate(),text));
    }
    
    /**
     *
     * @return
     */
    public String getCurrentDatasetDiscretization(){
        String currentDiscretization = currentDatasetDiscretizationText.getText();
        return currentDiscretization.substring(1, currentDiscretization.length()-1);
    }
    
    /**
     *
     * @return
     */
    public int getCurrentDatasetInterval(){
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(currentDatasetDiscretizationText.getText());
        matcher.find();
        return Integer.parseInt(matcher.group(1));
    }
    
    /**
     *
     */
    public void updateDatasetList(){
        datasetListContent.clear();
        DataManipulation dataManipulation = new DataManipulation();
        datasetListContent.addAll(dataManipulation.getDatasetList(configuration.getWorkspace()+"/datasets/"));
    }
    
    /**
     *
     */
    public void updatePreparedStreamList(){
        preparedStreamList.clear();
        DataManipulation dataManipulation = new DataManipulation();
        preparedStreamList.addAll(dataManipulation.getAvailablePreparedStreams(configuration.getWorkspace()+"/datasets/",currentDatasetText.getText()));
    }
    
    public void updateBounds(double a, double b){
        startTimeSlice = (int)((a*24.0*60.0)/(double)intervalDurationMin);
        endTimeSlice = (int)((b*24.0*60.0)/(double)intervalDurationMin);
    }
    
    public void updateApplicationStatus(int st){
        int mb = 1024 * 1024;
        int freeMem = (int) (runInfo.freeMemory()/mb);
        int totMem = (int) (runInfo.totalMemory()/mb);
        int maxMem = (int) (runInfo.maxMemory()/mb);
        applicationProgress.setProgress(st);
        applicationStatus.setText(freeMem+"mb, "+totMem+"mb, "+maxMem+"mb");
        runInfo.gc();
    }
    
    public void disablePane(boolean b){
        globalPane.setDisable(b);
    }
    
    public void disableAlgo(boolean b){
        applyAlgoButtonDetection.setDisable(b);
        applyAlgoButtonNetwork.setDisable(b);
    }
    
    public boolean isStopWord(String word){
        String tokens[] = word.split("=");
        for(String token : tokens){
            if(currentStopWords.stopWords.contains(token)){
                return true;
            }
        }
        return false;
    }
    
    public String getDatasetPath(){
        return configuration.getWorkspace()+"/datasets/"+currentDatasetText.getText()+"/"+currentDatasetDiscretizationText.getText().substring(1,currentDatasetDiscretizationText.getText().length()-1);
    }
}
