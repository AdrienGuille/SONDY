/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.ui;

import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.DataManipulation;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.widget.DoubleSlider;

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
 *   User interface of the data manipulation service. It allows importing and 
 *   preparing the data in order to optimize their exploitation and processing. 
 *   This component includes stop-words removal, content stemming and 
 *   lemmatization, message stream discretization, and message stream resizing.
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class DataUI {
    
    // Grids
    GridPane globalGrid;
    GridPane loadGrid;
    GridPane importGridLeft;
    GridPane globalImportGrid;
    GridPane prepareGrid;
    GridPane filterGrid;
    
    // Separators
    Separator loadSeparator = new Separator();
    Separator importSeparator = new Separator();
    Separator prepareSeparator = new Separator();
    Separator filterSeparator = new Separator();
    
    // Dataset
    // - List of available datasets
    Text loadText = new Text("Load existing dataset");
    ListView<String> datasetList = new ListView<>(); 

    // - Loaded dataset
    Text datasetInfo = new Text("dataset-name\n# messages\n# users\n# relationships");
    Text datasetDescription = new Text("dataset-description");
    ComboBox datasetAvailableDiscretizations = new ComboBox();
    
    // - Dataset import
    Text importText = new Text("Import new data");
    Label importNetwork = new Label("Network file (optional): ");
    Label importMessages = new Label("Messages file: ");
    Label importName = new Label("Dataset ID (only letters, numbers and '_'): ");
    Label importDescription = new Label("Dataset description (optional): ");
    Button networkButton = new Button("choose file (csv)");
    Button messagesButton = new Button("choose file (csv)");
    Button importButton = new Button("import dataset");
    TextField nameField = new TextField();
    TextField descriptionField = new TextField();
    File networkFile;
    File messagesFile;
    
    // Pre-processing
    Text prepareText = new Text("Prepare dataset");
    Text filterText = new Text("Filter dataset");
    Button prepareDatasetButton = new Button("prepare dataset");    
    
    // - Dataset discretization
    Label prepareDiscretize = new Label("Discretize and index message stream: ");
    Label discretizeUnitLabel = new Label(" min");
    TextField intervalDurationField = new TextField();
    
    // - Tokenization
    Label tokenTypeLabel = new Label("Tokenization: ");
    ComboBox tokenTypeBox = new ComboBox();
    
    // - Filtering (resizing and stopwords removal)
    Label stopwordsRemovalLabel = new Label("Stop-words removal: ");
    Button filterDatasetButton = new Button("filter dataset");
    Label datasetResizeText = new Label("Select subperiod: ");
    Text datasetResizeStartText = new Text("(-");
    Text datasetResizeEndText = new Text("-)  ");
    DoubleSlider datasetResizeSlider = new DoubleSlider();
    ComboBox stopwordsBox = new ComboBox();
    String selectedStopwords = "";
    
    // - Lemmatization
    Label LemmatizationLabel = new Label("Lemmatization (english only): ");
    ToggleButton activateLemmatizationButton = new ToggleButton("activate");
    
    // - Stemming
    Label stemmingLabel = new Label("Stemming: ");
    ComboBox stemmingBox = new ComboBox();
   
    // Global
    AppVariables appVariablesRef;
           
    /**
     *
     * @param appVariables
     * @throws IOException
     */
    public DataUI(AppVariables appVariables) throws IOException{

        // Global
        appVariablesRef = appVariables;
        final DataManipulation dataManipulation = new DataManipulation();
        loadText.setId("title");
        importText.setId("title");
        prepareText.setId("title");
        filterText.setId("title");
        
        globalGrid = new GridPane();
        globalGrid.setPadding(new Insets(5, 5, 5, 5));
        globalGrid.setVgap(5);
        globalGrid.setHgap(5);
        globalGrid.setStyle("-fx-background-color: linear-gradient(#ffffff, #e5e5e5)");
        GridPane.setConstraints(loadText, 0, 0);
        globalGrid.getChildren().add(loadText);
        GridPane.setConstraints(loadSeparator, 0, 1);
        globalGrid.getChildren().add(loadSeparator);

        // Dataset loading
        datasetAvailableDiscretizations.setItems(appVariables.preparedStreamList);
        datasetAvailableDiscretizations.valueProperty().addListener(new ChangeListener<String>() {
            @Override 
            public void changed(ObservableValue ov, String t, String t1) {     
                if(t1!=null && t1.length()>3){
                    appVariablesRef.loadDiscretization(t1);
                    datasetResizeSlider.setMin(0);
                    datasetResizeSlider.setMax(appVariablesRef.streamDuration);
                    appVariablesRef.disableAlgo(false);
                }
            }    
        });
        datasetDescription.setId("smalltext");
        appVariablesRef.updateDatasetList();
        datasetList.setItems(appVariablesRef.datasetListContent);
        datasetList.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<String>() {
            @Override
                public void changed(ObservableValue<? extends String> ov, 
                    String old_val, String new_val) {
                    appVariablesRef.loadDataset(new_val);
                    datasetInfo.setText(appVariablesRef.datasetInfo);
                    datasetDescription.setText(appVariablesRef.datasetDescription);
                    appVariablesRef.updatePreparedStreamList();
                    appVariablesRef.disableAlgo(true);
                }
            }
        );
        datasetList.setOrientation(Orientation.HORIZONTAL);
        datasetList.setMinHeight(65);
        datasetList.setMaxHeight(65);
        VBox boxDatasetList = new VBox();
        boxDatasetList.getChildren().add(datasetList);
        boxDatasetList.setMinWidth(550);
        boxDatasetList.setMaxWidth(550);
        VBox.setVgrow(datasetList, Priority.ALWAYS);
        loadGrid = new GridPane();
        loadGrid.setPadding(new Insets(0, 5, 0, 5));
        loadGrid.setVgap(5);
        loadGrid.setHgap(5);
        GridPane.setConstraints(boxDatasetList, 0, 0);
        loadGrid.getChildren().add(boxDatasetList);
        
        VBox boxDatasetInfo = new VBox();
        datasetInfo.setId("smalltext");
        boxDatasetInfo.getChildren().add(datasetInfo);
        boxDatasetInfo.setMinWidth(250);
        boxDatasetInfo.setMaxWidth(250);
        GridPane.setConstraints(boxDatasetInfo, 1, 0);
        loadGrid.getChildren().add(boxDatasetInfo);
        
        HBox boxDatasetDescription = new HBox(0);
        boxDatasetDescription.setMinWidth(550);
        boxDatasetDescription.setMaxWidth(550);
        boxDatasetDescription.setAlignment(Pos.CENTER_LEFT);
        boxDatasetDescription.getChildren().add(datasetDescription);
        HBox boxAvailableDiscretization = new HBox(0);
        boxAvailableDiscretization.setMinWidth(250);
        boxAvailableDiscretization.setMaxWidth(250);
        datasetAvailableDiscretizations.setPromptText("select stream preparation");
        datasetAvailableDiscretizations.setMinWidth(250);
        datasetAvailableDiscretizations.setMaxWidth(250);
        boxAvailableDiscretization.setAlignment(Pos.CENTER);
        boxAvailableDiscretization.getChildren().add(datasetAvailableDiscretizations);
        GridPane.setConstraints(boxDatasetDescription, 0, 1);
        loadGrid.getChildren().add(boxDatasetDescription);
        GridPane.setConstraints(boxAvailableDiscretization, 1, 1);
        loadGrid.getChildren().add(boxAvailableDiscretization);
        
        GridPane.setConstraints(loadGrid, 0, 2);
        globalGrid.getChildren().add(loadGrid);
        
        // Filtering
        appVariablesRef.availableStopWords = dataManipulation.getAvailableStopwords(appVariablesRef);
        stopwordsBox.setItems(appVariablesRef.availableStopWords);
        stopwordsBox.setPromptText("select list");
        HBox boxDatasetResizeLabels = new HBox(5);
        boxDatasetResizeLabels.setAlignment(Pos.TOP_RIGHT);
        boxDatasetResizeLabels.setMinWidth(550);
        boxDatasetResizeLabels.setMaxWidth(550);
        boxDatasetResizeLabels.getChildren().addAll(datasetResizeStartText,datasetResizeEndText);
        datasetResizeStartText.setId("smalltext");
        datasetResizeEndText.setId("smalltext");
        datasetResizeSlider.value1Property().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> arg0,
                                Number arg1, Number arg2) {
                    float val = arg2.floatValue();
                        datasetResizeStartText.setText("("+Math.round(val)+"");
                }
            });
        datasetResizeSlider.value2Property().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> arg0,
                                Number arg1, Number arg2) {
                    float val = arg2.floatValue();
                        datasetResizeEndText.setText(Math.round(val)+")  ");
                }
            });
        
        intervalDurationField.setPromptText("interval duration");
        intervalDurationField.setMinWidth(155);
        intervalDurationField.setMaxWidth(155);
        prepareGrid = new GridPane();
        prepareGrid.setPadding(new Insets(0, 5, 0, 5));
        prepareGrid.setVgap(5);
        prepareGrid.setHgap(5);
        HBox discretizationParametersBox = new HBox(5);
        discretizationParametersBox.setAlignment(Pos.CENTER_RIGHT);
        discretizationParametersBox.setMinWidth(250);
        discretizationParametersBox.setMaxWidth(250);
        discretizeUnitLabel.setMinWidth(40);
        discretizeUnitLabel.setMaxWidth(40);
        discretizationParametersBox.getChildren().addAll(intervalDurationField,discretizeUnitLabel);
        
        HBox prepareDiscretizeBox = new HBox(5);
        prepareDiscretizeBox.setMinWidth(300);
        prepareDiscretizeBox.setMaxWidth(300);
        prepareDiscretizeBox.getChildren().add(prepareDiscretize);
        
        HBox discretizeBox = new HBox(5);
        discretizeBox.getChildren().addAll(prepareDiscretizeBox,discretizationParametersBox);
        discretizeBox.setMinWidth(550);
        discretizeBox.setMaxWidth(550);
        GridPane.setConstraints(discretizeBox, 0, 0);
        prepareGrid.getChildren().add(discretizeBox);
        HBox performDiscretizationBox = new HBox(5);
        performDiscretizationBox.setAlignment(Pos.CENTER);
        performDiscretizationBox.setMinWidth(250);
        performDiscretizationBox.setMaxWidth(250);       
        
        HBox resizeDatasetTextBox = new HBox(5);
        resizeDatasetTextBox.setMinWidth(350);
        resizeDatasetTextBox.setMaxWidth(350);
        resizeDatasetTextBox.getChildren().add(datasetResizeText);
        
        datasetResizeSlider.setMinWidth(195);
        datasetResizeSlider.setMaxWidth(195);
        
        HBox resizeBoxSlider = new HBox(5);
        resizeBoxSlider.setAlignment(Pos.CENTER_RIGHT);
        resizeBoxSlider.setMinWidth(550);
        resizeBoxSlider.setMaxWidth(550);
        resizeBoxSlider.getChildren().addAll(resizeDatasetTextBox,datasetResizeSlider);
        
        filterGrid = new GridPane();
        filterGrid.setPadding(new Insets(0, 5, 0, 5));
        filterGrid.setVgap(5);
        filterGrid.setHgap(5);
        
        GridPane.setConstraints(resizeBoxSlider, 0, 0);
        filterGrid.getChildren().add(resizeBoxSlider);
        
        HBox boxFilterDataset = new HBox(5);
        boxFilterDataset.setMinWidth(250);
        boxFilterDataset.setMaxWidth(250);
        boxFilterDataset.getChildren().add(filterDatasetButton);
        filterDatasetButton.setMaxWidth(220);
        filterDatasetButton.setMinWidth(220);
        boxFilterDataset.setAlignment(Pos.CENTER);
        GridPane.setConstraints(boxFilterDataset, 1, 1);
        filterGrid.getChildren().add(boxFilterDataset);
        
        HBox performResizeBox = new HBox(5);
        performResizeBox.setAlignment(Pos.CENTER);
        performResizeBox.setMinWidth(220);
        performResizeBox.setMaxWidth(220);       
        GridPane.setConstraints(boxDatasetResizeLabels, 0, 1);
        filterGrid.getChildren().add(boxDatasetResizeLabels);
        
        HBox removeStopwordsBox = new HBox(5);
        removeStopwordsBox.setAlignment(Pos.CENTER);
        removeStopwordsBox.setMinWidth(250);
        removeStopwordsBox.setMaxWidth(250); 
        
        HBox filteringBox = new HBox(5);
        filteringBox.setAlignment(Pos.CENTER_LEFT);
        filteringBox.setMinWidth(550);
        filteringBox.setMaxWidth(550); 
        HBox filterLabelBox = new HBox(5);
        filterLabelBox.setAlignment(Pos.CENTER_LEFT);
        filterLabelBox.setMinWidth(350);
        filterLabelBox.setMaxWidth(350); 
        filterLabelBox.getChildren().addAll(stopwordsRemovalLabel);
        
        filterDatasetButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
                if(!appVariablesRef.currentDatasetDiscretizationText.getText().equals("")){
                    if(selectedStopwords.length()>0){
                        if(selectedStopwords.contains("stopwords: ")){
                            DataManipulation dm = new DataManipulation();
                            appVariablesRef.currentStopWords.addAll(dm.getStopwords(appVariablesRef, selectedStopwords.replace("stopwords: ","")).getSet());
                            appVariablesRef.addLogEntry("[data] filtered message stream with "+selectedStopwords);
                        }
                    }
                    if(datasetResizeSlider.getValue1() != 0 || datasetResizeSlider.getValue2() != 0){
                        appVariablesRef.updateBounds(datasetResizeSlider.getValue1(), datasetResizeSlider.getValue2());
                        appVariablesRef.addLogEntry("[data] selected sub-period: "+(datasetResizeStartText.getText().equals("-")?"0":datasetResizeStartText.getText())+":"+datasetResizeEndText.getText());
                    }
                }
            }
        });

        stopwordsBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override 
            public void changed(ObservableValue ov, String t, String t1) {     
                if(t1!=null){
                    selectedStopwords = t1;
                }
            }    
        });
        stopwordsBox.setMinWidth(195);
        stopwordsBox.setMaxWidth(195);
        filteringBox.getChildren().addAll(filterLabelBox,stopwordsBox);
        GridPane.setConstraints(filteringBox, 0, 2);
        filterGrid.getChildren().add(filteringBox);
        
        prepareDatasetButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
                disableInterface(true);
                appVariablesRef.updateApplicationStatus(-1);
                final Task<String> waitingTask = new Task<String>() {
                    @Override
                    public String call() throws Exception {
                        if(!appVariablesRef.datasetName.equals("no dataset selected") && appVariablesRef.datasetName.length() > 0){
                            String stemLanguage = "standard";
                            boolean lemmatization = false;
                            if(!stemmingBox.getSelectionModel().isEmpty()){
                                stemLanguage = stemmingBox.getSelectionModel().getSelectedItem().toString();
                            }
                            if(activateLemmatizationButton.isSelected()){
                                lemmatization = true;
                            }
                            String selectedNgram = "";
                            int ngram = 1;
                            if(!tokenTypeBox.getSelectionModel().isEmpty()){
                                selectedNgram = tokenTypeBox.getSelectionModel().getSelectedItem().toString();
                            }
                            if(selectedNgram.equals("bigram")){
                                ngram = 2;
                            }else{
                                if(selectedNgram.equals("trigram")){
                                    ngram = 3;
                                }
                            }
                            appVariablesRef.addLogEntry("[data] preparing dataset: "+appVariablesRef.currentDatasetText.getText()+" ["+Integer.parseInt(intervalDurationField.getText())+"min, "+stemLanguage.toLowerCase()+" analyzer, lemmatization="+lemmatization+", "+ngram+"-gram]");
                            dataManipulation.prepareStream(appVariablesRef.currentDatasetText.getText(), Integer.parseInt(intervalDurationField.getText()), ngram, stemLanguage, lemmatization, appVariablesRef);
                        }
                        return "";
                    }
                };
                waitingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        appVariablesRef.updatePreparedStreamList();
                        disableInterface(false);
                        appVariablesRef.updateApplicationStatus(0);
                    }
                }); 
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(waitingTask);
            }
        });
        
        GridPane.setConstraints(prepareGrid, 0, 9);
        globalGrid.getChildren().add(prepareGrid);
         GridPane.setConstraints(filterText, 0, 10);
        globalGrid.getChildren().add(filterText);
        GridPane.setConstraints(filterSeparator, 0, 11);
        globalGrid.getChildren().add(filterSeparator);
        GridPane.setConstraints(filterGrid, 0, 12);
        globalGrid.getChildren().add(filterGrid);
                
        // Dataset import
        importButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
            public void handle(ActionEvent event) {
                disableInterface(true);
                appVariablesRef.updateApplicationStatus(-1);
                final Task<String> waitingTask = new Task<String>() {
                    @Override
                    public String call() throws Exception {
                        appVariablesRef.addLogEntry("[data] importing new dataset: "+nameField.getText());
                        DataManipulation manipulation = new DataManipulation();
                        if(networkFile != null){
                            manipulation.importDataset(nameField.getText(), descriptionField.getText(), networkFile.getAbsolutePath(), messagesFile.getAbsolutePath(), appVariablesRef);
                        }else{
                            manipulation.importDataset(nameField.getText(), descriptionField.getText(), null, messagesFile.getAbsolutePath(), appVariablesRef);                    
                        }
                        return "" ;
                    }
                };
                waitingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        appVariablesRef.addLogEntry("[data] import completed");
                        appVariablesRef.updateDatasetList();
                        appVariablesRef.updateApplicationStatus(0);
                        disableInterface(false);
                    }
                }); 
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(waitingTask);
            }
        });
        
        networkButton.setMinWidth(195);
        networkButton.setMaxWidth(195);
        networkButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);
                networkFile = fileChooser.showOpenDialog(null);
                if(networkFile != null){
                    String label = networkFile.getPath().replace("\\","/");
                    networkButton.setText(label.substring(label.lastIndexOf('/')+1));
                }
            }
        });
        
        messagesButton.setMinWidth(195);
        messagesButton.setMaxWidth(195);
        messagesButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);
                messagesFile = fileChooser.showOpenDialog(null);
                if(messagesFile != null){
                    String label = messagesFile.getPath().replace("\\","/");
                    messagesButton.setText(label.substring(label.lastIndexOf('/')+1));
                }
            }
        });
         
        GridPane.setConstraints(importText, 0, 4);
        globalGrid.getChildren().add(importText);
        GridPane.setConstraints(importSeparator, 0, 5);
        globalGrid.getChildren().add(importSeparator);
        
        VBox boxImportMessages = new VBox();
        boxImportMessages.getChildren().add(importMessages);
        VBox boxImportNetwork = new VBox();
        boxImportNetwork.getChildren().add(importNetwork);
        VBox boxImportName = new VBox();
        boxImportName.getChildren().add(importName);
        VBox boxImportDescription = new VBox();
        boxImportDescription.getChildren().add(importDescription);
        boxImportMessages.setMinWidth(350);
        boxImportMessages.setMaxWidth(350);
        boxImportNetwork.setMinWidth(350);
        boxImportNetwork.setMaxWidth(350);
        boxImportName.setMinWidth(350);
        boxImportName.setMaxWidth(350);
        boxImportDescription.setMinWidth(350);
        boxImportDescription.setMaxWidth(350);
        
        importGridLeft = new GridPane();
        importGridLeft.setPadding(new Insets(0, 0, 0, 0));
        importGridLeft.setVgap(5);
        importGridLeft.setHgap(5);
        GridPane.setConstraints(boxImportNetwork, 0, 0);
        importGridLeft.getChildren().add(boxImportNetwork);
        GridPane.setConstraints(boxImportMessages, 0, 1);
        importGridLeft.getChildren().add(boxImportMessages);
        GridPane.setConstraints(boxImportName, 0, 2);
        importGridLeft.getChildren().add(boxImportName);        
        GridPane.setConstraints(boxImportDescription, 0, 3);
        importGridLeft.getChildren().add(boxImportDescription);
        GridPane.setConstraints(networkButton, 1, 0);
        importGridLeft.getChildren().add(networkButton);
        GridPane.setConstraints(messagesButton, 1, 1);
        importGridLeft.getChildren().add(messagesButton);
        
        descriptionField.setMaxWidth(195);
        descriptionField.setMinWidth(195);
        nameField.setMaxWidth(195);
        nameField.setMinWidth(195);
        nameField.setPromptText("name");
        descriptionField.setPromptText("description");
        
        GridPane.setConstraints(nameField, 1, 2);
        importGridLeft.getChildren().add(nameField);
        GridPane.setConstraints(descriptionField, 1, 3);
        importGridLeft.getChildren().add(descriptionField);
        
        globalImportGrid = new GridPane();
        globalImportGrid.setPadding(new Insets(0, 5, 0, 5));
        globalImportGrid.setVgap(5);
        globalImportGrid.setHgap(5);
        GridPane.setConstraints(importGridLeft, 0, 0);
        globalImportGrid.getChildren().add(importGridLeft);

        VBox boxImportButton = new VBox(5);
        boxImportButton.setAlignment(Pos.CENTER);
        importButton.setMinWidth(220);
        importButton.setMaxWidth(220);
        boxImportButton.getChildren().addAll(importButton);
        boxImportButton.setMinWidth(250);
        boxImportButton.setMaxWidth(250);        
        GridPane.setConstraints(boxImportButton, 1, 0);
        globalImportGrid.getChildren().add(boxImportButton);
        
        GridPane.setConstraints(globalImportGrid, 0, 6);
        globalGrid.getChildren().add(globalImportGrid);
        
        GridPane.setConstraints(prepareText, 0, 7);
        globalGrid.getChildren().add(prepareText);
        GridPane.setConstraints(prepareSeparator, 0, 8);
        globalGrid.getChildren().add(prepareSeparator);
        
        HBox performStemmingBox = new HBox(5);
        performStemmingBox.setAlignment(Pos.CENTER);
        performStemmingBox.setMinWidth(250);
        performStemmingBox.setMaxWidth(250); 
        prepareDatasetButton.setMinWidth(220);
        prepareDatasetButton.setMaxWidth(220);
        
        HBox tokenizationSBox = new HBox(5);
        tokenizationSBox.setAlignment(Pos.CENTER_LEFT);
        tokenizationSBox.setMinWidth(550);
        tokenizationSBox.setMaxWidth(550); 
        HBox tokenizationLabelBox = new HBox(5);
        tokenizationLabelBox.setAlignment(Pos.CENTER_LEFT);
        tokenizationLabelBox.setMinWidth(350);
        tokenizationLabelBox.setMaxWidth(350); 
        tokenizationLabelBox.getChildren().addAll(tokenTypeLabel);

        tokenTypeBox.setPromptText("unigram");
        ObservableList<String> tokenList = FXCollections.observableArrayList("unigram","bigram","trigram");
        tokenTypeBox.setItems(tokenList);
        tokenTypeBox.setMinWidth(195);
        tokenTypeBox.setMaxWidth(195);
        tokenizationSBox.getChildren().addAll(tokenizationLabelBox,tokenTypeBox);
        GridPane.setConstraints(tokenizationSBox, 0, 1);
        prepareGrid.getChildren().add(tokenizationSBox);
        
        HBox stemmingSBox = new HBox(5);
        stemmingSBox.setAlignment(Pos.CENTER_LEFT);
        stemmingSBox.setMinWidth(550);
        stemmingSBox.setMaxWidth(550); 
        HBox stemmingLabelBox = new HBox(5);
        stemmingLabelBox.setAlignment(Pos.CENTER_LEFT);
        stemmingLabelBox.setMinWidth(350);
        stemmingLabelBox.setMaxWidth(350); 
        stemmingLabelBox.getChildren().addAll(stemmingLabel);

        stemmingBox.setPromptText("standard");
        ObservableList<String> stemList = FXCollections.observableArrayList("standard","English","French","Chinese");
        stemmingBox.setItems(stemList);
        stemmingBox.setMinWidth(195);
        stemmingBox.setMaxWidth(195);
        stemmingSBox.getChildren().addAll(stemmingLabelBox,stemmingBox);
        GridPane.setConstraints(stemmingSBox, 0, 2);
        prepareGrid.getChildren().add(stemmingSBox);
        
        performStemmingBox.getChildren().add(prepareDatasetButton);
        GridPane.setRowSpan(performStemmingBox, 2);
        GridPane.setConstraints(performStemmingBox, 1, 1);
        prepareGrid.getChildren().add(performStemmingBox);
        
        HBox lemmatizationBox = new HBox(5);
        lemmatizationBox.setAlignment(Pos.CENTER_LEFT);
        lemmatizationBox.setMinWidth(350);
        lemmatizationBox.setMaxWidth(350); 
        activateLemmatizationButton.setMinWidth(195);
        activateLemmatizationButton.setMaxWidth(195);
        HBox lemmBox2 = new HBox(5);
        lemmatizationBox.getChildren().add(LemmatizationLabel);
        lemmBox2.getChildren().addAll(lemmatizationBox,activateLemmatizationButton);
        
        GridPane.setConstraints(lemmBox2, 0, 3);
        prepareGrid.getChildren().add(lemmBox2);
        
        activateLemmatizationButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
                if(activateLemmatizationButton.isSelected()){
                    appVariablesRef.lemmatization = true;
                    activateLemmatizationButton.setText("activated");
                }else{
                    appVariablesRef.lemmatization = false;
                    activateLemmatizationButton.setText("activate");
                }
            }
        });
    }
    
    /**
     *
     * @return
     */
    public GridPane getGrid(){
        return globalGrid;
    }
    
    public void disableInterface(boolean b){
        appVariablesRef.disablePane(b);
    }
    
}   
