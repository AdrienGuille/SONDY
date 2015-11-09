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
package main.java.fr.ericlab.sondy.core.ui;

import com.sun.javafx.css.StyleManager;
import impl.org.controlsfx.skin.CheckComboBoxSkin;
import javafx.scene.control.*;
import main.java.fr.ericlab.sondy.core.app.Main;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import main.java.fr.ericlab.sondy.core.structures.Dataset;
import main.java.fr.ericlab.sondy.core.structures.Datasets;
import main.java.fr.ericlab.sondy.core.text.index.CalculationType;
import main.java.fr.ericlab.sondy.core.text.stopwords.StopwordSets;
import main.java.fr.ericlab.sondy.core.utils.UIUtils;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.RangeSlider;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class DataManipulationUI {
    public GridPane grid;
    
    // Available datasets
    // - list
    ListView<String> datasetListView;
    Datasets availableDatasets;
    
    // Selected dataset
    // - info
    Label selectedDatasetIdLabel;
    Label selectedDatasetDescriptionLabel;
    Label messageCountLabel;
    Label userCountLabel;
    Label relationshipCountLabel;
    // - available preprocessed corpora
    ChoiceBox preprocessedCorpusList;    
    
    // Import
    TextField newDatasetIdentifierField;
    TextField newDatasetDescriptionField;
    // - property map
    Button messagesFileButton;
    Button networkFileButton;
    HashMap<String,String> newDatasetProperties;
    
    // - Preprocess
    ChoiceBox stemmingChoiceBox;
    ChoiceBox lemmatizationChoiceBox;
    ChoiceBox tokenizationChoiceBox;
    TextField timeSliceLengthField;
    CheckBox chkSpamRemover;
    
    // - Filter
    CheckComboBox<String> stopwordListsCheckComboBox;
    StopwordSets stopwordLists;
    RangeSlider resizeSlider;
    
    public DataManipulationUI(){
        StyleManager.getInstance().addUserAgentStylesheet(PropertySheet.class.getResource("propertysheet.css").toExternalForm());

        // Initializing the main grid
        grid = new GridPane();
        grid.setPadding(new Insets(5,5,5,5));
        
        // Adding separators
        grid.add(new Text("Available datasets"),0,0);
        grid.add(new Separator(),0,1);
        grid.add(new Text("Import a dataset"),0,3);
        grid.add(new Separator(),0,4);
        grid.add(new Text("Preprocess the selected dataset"),0,6);
        grid.add(new Separator(),0,7);
        grid.add(new Text("Filter the selected preprocessed dataset"),0,9);
        grid.add(new Separator(),0,10);
        
        // Initializing specific UIs
        availableDatasetsUI();
        newDatasetProperties = new HashMap<>();
        importUI();
        preprocessUI();
        filterUI();
    }
    
    public final void availableDatasetsUI(){
        initializeDatasetListView();
        selectedDatasetDescriptionLabel = new Label("Selected dataset description");
        selectedDatasetDescriptionLabel.setId("smalltext");
        UIUtils.setSize(selectedDatasetDescriptionLabel,Main.columnWidthLEFT,24);
        VBox existingDatasetLEFT = new VBox();
        existingDatasetLEFT.getChildren().addAll(datasetListView,new Rectangle(0,3),selectedDatasetDescriptionLabel);
        // Right part
        selectedDatasetIdLabel = new Label("Selected dataset id");
        selectedDatasetIdLabel.setId("smalltext");
        UIUtils.setSize(selectedDatasetIdLabel,Main.columnWidthRIGHT,16);
        messageCountLabel = new Label("Message count");
        messageCountLabel.setId("smalltext");
        UIUtils.setSize(messageCountLabel,Main.columnWidthRIGHT,16);
        userCountLabel = new Label("User count");
        userCountLabel.setId("smalltext");
        UIUtils.setSize(userCountLabel,Main.columnWidthRIGHT,16);
        relationshipCountLabel = new Label("Relationship count");
        relationshipCountLabel.setId("smalltext");
        UIUtils.setSize(relationshipCountLabel,Main.columnWidthRIGHT,16);
        initializePreprocessedCorpusList();
        UIUtils.setSize(preprocessedCorpusList, Main.columnWidthRIGHT, 24);
        preprocessedCorpusList.setItems(AppParameters.dataset.preprocessedCorpusList);
        
        VBox existingDatasetRIGHT = new VBox();
        existingDatasetRIGHT.getChildren().addAll(selectedDatasetIdLabel,messageCountLabel,userCountLabel,relationshipCountLabel,new Rectangle(0,3),preprocessedCorpusList);
        // Both parts
        HBox existingDatasetBOTH = new HBox(5);
        existingDatasetBOTH.getChildren().addAll(existingDatasetLEFT,existingDatasetRIGHT);
        grid.add(existingDatasetBOTH,0,2);
    }
            
    public final void importUI(){
        GridPane gridLEFT = new GridPane();
        // Labels
        Label messagesLabel = new Label("Messages file");
        UIUtils.setSize(messagesLabel, Main.columnWidthLEFT/2, 24);
        Label networkLabel = new Label("Network file");
        UIUtils.setSize(networkLabel, Main.columnWidthLEFT/2, 24);
        Label datasetIdentificatorLabel = new Label("Dataset ID");
        UIUtils.setSize(datasetIdentificatorLabel, Main.columnWidthLEFT/2, 24);
        Label datasetDescriptionLabel = new Label("Dataset description");
        UIUtils.setSize(datasetDescriptionLabel, Main.columnWidthLEFT/2, 24);
        gridLEFT.add(messagesLabel,0,0);
        gridLEFT.add(new Rectangle(0,3),0,1);
        gridLEFT.add(networkLabel,0,2);
        gridLEFT.add(new Rectangle(0,3),0,3);
        gridLEFT.add(datasetIdentificatorLabel,0,4);
        gridLEFT.add(new Rectangle(0,3),0,5);
        gridLEFT.add(datasetDescriptionLabel,0,6);
        
        // Values
        messagesFileButton = createChooseFileButton("messagesFile");
        UIUtils.setSize(messagesFileButton,Main.columnWidthLEFT/2, 24);
        networkFileButton = createChooseFileButton("networkFile");
        UIUtils.setSize(networkFileButton,Main.columnWidthLEFT/2, 24);
        newDatasetIdentifierField = new TextField();
        newDatasetIdentifierField.setPromptText("unique identifier");
        UIUtils.setSize(newDatasetIdentifierField,Main.columnWidthLEFT/2, 24);
        newDatasetDescriptionField = new TextField();
        newDatasetDescriptionField.setPromptText("short dataset description");
        UIUtils.setSize(newDatasetDescriptionField,Main.columnWidthLEFT/2, 24);
        gridLEFT.add(messagesFileButton,1,0);
        gridLEFT.add(networkFileButton,1,2);
        gridLEFT.add(newDatasetIdentifierField,1,4);
        gridLEFT.add(newDatasetDescriptionField,1,6);
        HBox importDatasetBOTH = new HBox(5);
        importDatasetBOTH.getChildren().addAll(gridLEFT,createImportButton());
        grid.add(importDatasetBOTH,0,5);
    }
    
    public final void preprocessUI(){
        GridPane gridLEFT = new GridPane();
        // Labels
        Label stemmingLabel = new Label("Stemming");
        UIUtils.setSize(stemmingLabel, Main.columnWidthLEFT/2, 24);
        Label LemmatizationLabel = new Label("Lemmatization");
        UIUtils.setSize(LemmatizationLabel, Main.columnWidthLEFT / 2, 24);
        Label TokenizationLabel = new Label("Tokenization");
        UIUtils.setSize(TokenizationLabel, Main.columnWidthLEFT / 2, 24);
        Label partitionLabel = new Label("Partition and index messages");
        UIUtils.setSize(partitionLabel, Main.columnWidthLEFT / 2, 24);
        Label spamRemoveLabel = new Label("Remove Spam Tweets of Same User");
        UIUtils.setSize(spamRemoveLabel , Main.columnWidthLEFT / 2, 24);

        gridLEFT.add(stemmingLabel,0,0);
        gridLEFT.add(new Rectangle(0,3),0,1);
        gridLEFT.add(LemmatizationLabel,0,2);
        gridLEFT.add(new Rectangle(0,3),0,3);
        gridLEFT.add(TokenizationLabel,0,4);
        gridLEFT.add(new Rectangle(0,3),0,5);
        gridLEFT.add(partitionLabel,0,6);
//        gridLEFT.add(spamRemoveLabel,0,7);
        
        // Values
        stemmingChoiceBox = new ChoiceBox();
        stemmingChoiceBox.getItems().addAll("disabled","French","English","Arabic","Persian");
        UIUtils.setSize(stemmingChoiceBox, Main.columnWidthLEFT / 2, 24);
        lemmatizationChoiceBox = new ChoiceBox();
        lemmatizationChoiceBox.getItems().addAll("disabled","French","English");
        UIUtils.setSize(lemmatizationChoiceBox, Main.columnWidthLEFT / 2, 24);
        tokenizationChoiceBox = new ChoiceBox();
        tokenizationChoiceBox.getItems().addAll("1-gram","2-gram","3-gram");
        UIUtils.setSize(tokenizationChoiceBox, Main.columnWidthLEFT / 2, 24);
        timeSliceLengthField = new TextField();
        timeSliceLengthField.setPromptText("time-slice length in minutes (e.g. 30)");
        UIUtils.setSize(timeSliceLengthField, Main.columnWidthLEFT / 2, 24);
        chkSpamRemover = new CheckBox();
//        chkSpamRemover.setText("Remove Spam Tweets");
//        UIUtils.setSize(chkSpamRemover, Main.columnWidthLEFT / 2, 24);
        gridLEFT.add(stemmingChoiceBox,1,0);
        gridLEFT.add(lemmatizationChoiceBox,1,2);
        gridLEFT.add(tokenizationChoiceBox,1,4);
        gridLEFT.add(timeSliceLengthField,1,6);
//        gridLEFT.add(chkSpamRemover,1,7);
        
        HBox preprocessDatasetBOTH = new HBox(5);
        preprocessDatasetBOTH.getChildren().addAll(gridLEFT,createPreprocessButton());
        grid.add(preprocessDatasetBOTH,0,8);
    }
    
    public final void filterUI(){
        GridPane gridLEFT = new GridPane();
        // Labels
        Label stopwordsLabel = new Label("Stop words removal");
        UIUtils.setSize(stopwordsLabel, Main.columnWidthLEFT/2, 24);
        Label resizingLabel = new Label("Resizing");
        UIUtils.setSize(resizingLabel, Main.columnWidthLEFT/2, 24);
        gridLEFT.add(stopwordsLabel,0,0);
        gridLEFT.add(new Rectangle(0,3),0,1);
        gridLEFT.add(resizingLabel,0,2);
        
        // Values
        stopwordLists = new StopwordSets();
        stopwordListsCheckComboBox = new CheckComboBox<>(stopwordLists.availableSets);
        stopwordListsCheckComboBox.setStyle("-fx-font-size: 12px;"); 
        stopwordListsCheckComboBox.skinProperty().addListener(new ChangeListener<Skin>() {
        @Override
        public void changed(ObservableValue<? extends Skin> observable, Skin oldValue, Skin newValue) {
             if(oldValue==null && newValue!=null){
                 CheckComboBoxSkin skin = (CheckComboBoxSkin)newValue;
                 ComboBox combo = (ComboBox)skin.getChildren().get(0);
                 combo.setPrefWidth(Main.columnWidthLEFT/2);
                 combo.setMaxWidth(Double.MAX_VALUE);
             }
        }
});
//        stopwordListsCheckComboBox.setMaxWidth(Double.MAX_VALUE);
                
//        UIUtils.setSize(stopwordListsCheckComboBox,Main.columnWidthLEFT/2, 24);
        gridLEFT.add(stopwordListsCheckComboBox,1,0);
        resizeSlider = new RangeSlider();
        resizeSlider.setBlockIncrement(0.1);
        UIUtils.setSize(resizeSlider,Main.columnWidthLEFT/2, 24);
        resizeSlider.resize(Main.columnWidthLEFT/2, 24);
        gridLEFT.add(resizeSlider,1,2);

        HBox filterDatasetBOTH = new HBox(5);
        filterDatasetBOTH.getChildren().addAll(gridLEFT,createFilterButton());
        grid.add(filterDatasetBOTH,0,11);
    }
    
    public final void initializeDatasetListView(){
        availableDatasets = new Datasets();
        availableDatasets.update();
        datasetListView = new ListView<>();
        datasetListView.setOrientation(Orientation.HORIZONTAL);
        datasetListView.setItems(availableDatasets.list);
        datasetListView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
            if(new_val != null){
                selectedDatasetIdLabel.setText(new_val);
                AppParameters.dataset.load(new_val);
                messageCountLabel.setText(AppParameters.dataset.corpus.messageCount+" messages");
                userCountLabel.setText(AppParameters.dataset.corpus.authorCount+" users");
                relationshipCountLabel.setText(AppParameters.dataset.network.relationshipCount+" relationships");
                selectedDatasetDescriptionLabel.setText(AppParameters.dataset.description);
                resizeSlider.setMin(0);
                resizeSlider.setLowValue(0);
                resizeSlider.setMax(AppParameters.dataset.corpus.getLength());
                resizeSlider.setHighValue(AppParameters.dataset.corpus.getLength());
                clearPreprocessUI();
            }
        });
        UIUtils.setSize(datasetListView,Main.columnWidthLEFT,64);
    }

    public final void initializePreprocessedCorpusList(){
        preprocessedCorpusList = new ChoiceBox();
        UIUtils.setSize(preprocessedCorpusList, Main.columnWidthRIGHT, 24);
        preprocessedCorpusList.setItems(AppParameters.dataset.preprocessedCorpusList);
        preprocessedCorpusList.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                clearFilterUI();
                if(t1 != null){
                    AppParameters.disableUI(true);
                    LogUI.addLogEntry("Loading '" + AppParameters.dataset.id + "' (" + t1 + ")... ");
                    final Task<String> waitingTask = new Task<String>() {
                        @Override
                        public String call() throws Exception {
                            AppParameters.dataset.corpus.loadFrequencies(t1);
                            AppParameters.timeSliceA = 0;
                            AppParameters.timeSliceB = AppParameters.dataset.corpus.getMessageDistribution(CalculationType.Frequency).length;
                            return "Done.";
                        }
                    };
                    waitingTask.setOnSucceeded((WorkerStateEvent event1) -> {
                        LogUI.addLogEntry(waitingTask.getValue());
                        AppParameters.disableUI(false);

                        resizeSlider.setMin(0);
                        resizeSlider.setLowValue(0);
                        resizeSlider.setMax(AppParameters.dataset.corpus.getLength());
                        resizeSlider.setHighValue(AppParameters.dataset.corpus.getLength());
                    });
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(waitingTask);
                }
            }    
        });
    }
    
    public final Button createChooseStopwordsButton(){
        Button button = new Button("Choose stopword sets...");
        button.setOnAction((ActionEvent event) -> {
            
        });
        return button;
    }
    
    public final Button createChooseFileButton(String buttonId){
        Button button = new Button("Choose file...");
        button.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(null);
            if(file != null){
                newDatasetProperties.put(buttonId, file.getAbsolutePath());
                button.setText(Paths.get(file.getPath()).getFileName().toString());
            }
        });
        return button;
    }
    
    public final VBox createImportButton(){
        VBox buttonBox = new VBox();
        buttonBox.setAlignment(Pos.CENTER);
        Button importButton = new Button("Import");
        UIUtils.setSize(importButton,Main.columnWidthRIGHT,24);
        importButton.setOnAction((ActionEvent event) -> {
            AppParameters.disableUI(true);
            newDatasetProperties.put("id",newDatasetIdentifierField.getText());
            newDatasetProperties.put("description", newDatasetDescriptionField.getText());
            Dataset dataset = new Dataset();
            AppParameters.disableUI(true);
            String messagesFilePath = newDatasetProperties.get("messagesFile");
            String networkFilePath = newDatasetProperties.get("networkFile");
            LogUI.addLogEntry("Importing '"+newDatasetIdentifierField.getText()+"' (messages file: "+messagesFilePath+", network file: "+networkFilePath+")...");
            final Task<String[]> waitingTask = new Task<String[]>() {
                    @Override
                    public String[] call() throws Exception {
                        return dataset.create(newDatasetProperties);
                    }
                };
                waitingTask.setOnSucceeded((WorkerStateEvent event1) -> {
                    LogUI.addLogEntry(waitingTask.getValue()[0]);
                    LogUI.addLogEntry(waitingTask.getValue()[1]);
                    newDatasetProperties.clear();
                    availableDatasets.update();
                    messagesFileButton.setText("Choose file...");
                    networkFileButton.setText("Choose file...");
                    newDatasetIdentifierField.clear();
                    newDatasetDescriptionField.clear();
                    AppParameters.disableUI(false);
            }); 
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(waitingTask);
        });
        buttonBox.getChildren().addAll(importButton);
        return buttonBox;
    }
    
    public final VBox createPreprocessButton(){
        VBox buttonBox = new VBox();
        buttonBox.setAlignment(Pos.CENTER);
        Button preprocessButton = new Button("Preprocess");
        UIUtils.setSize(preprocessButton,Main.columnWidthRIGHT,24);
        preprocessButton.setOnAction((ActionEvent event) -> {
            AppParameters.disableUI(true);
            int ngram = Integer.parseInt(tokenizationChoiceBox.getSelectionModel().getSelectedItem().toString().split("-")[0]);
            int timeSliceLength = Integer.parseInt(timeSliceLengthField.getText());
            String stemming = stemmingChoiceBox.getSelectionModel().getSelectedItem().toString();
            String lemmatization = lemmatizationChoiceBox.getSelectionModel().getSelectedItem().toString();
            LogUI.addLogEntry("Preprocessing '"+AppParameters.dataset.id+"' (stemming: "+stemming+", lemmatization: "+lemmatization+", "+ngram+"-gram, "+
                timeSliceLength+"-min time-slices, spamremoved: "+chkSpamRemover.isSelected()+")...");
            final Task<String> waitingTask = new Task<String>() {
                    @Override
                    public String call() throws Exception {
                        return AppParameters.dataset.preprocess(stemming,lemmatization,ngram,timeSliceLength,chkSpamRemover.isSelected());
                    }
                };
                waitingTask.setOnSucceeded((WorkerStateEvent event1) -> {
                    LogUI.addLogEntry(waitingTask.getValue());
//                    AppParameters.dataset.preprocess(stemmingChoiceBox.getSelectionModel().getSelectedItem().toString(),
//                        lemmatizationChoiceBox.getSelectionModel().getSelectedItem().toString(),ngram,timeSliceLength,chkSpamRemover.isSelected());
                    AppParameters.dataset.updatePreprocessedCorpusList();
                    AppParameters.disableUI(false);
                    clearPreprocessUI();
            }); 
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(waitingTask);
        });
        buttonBox.getChildren().addAll(preprocessButton);
        return buttonBox;
    }
    
    public final VBox createFilterButton(){
        VBox buttonBox = new VBox();
        buttonBox.setAlignment(Pos.CENTER);
        Button filterButton = new Button("Filter");
        UIUtils.setSize(filterButton,Main.columnWidthRIGHT,24);
        filterButton.setOnAction((ActionEvent event) -> {
            if(!AppParameters.dataset.corpus.preprocessing.equals("")){
                LogUI.addLogEntry("Filtering '"+AppParameters.dataset.id+"' (stop word set(s): "+stopwordListsCheckComboBox.getCheckModel().getCheckedItems()+", time period: ["+resizeSlider.getLowValue()+";"+resizeSlider.getHighValue()+"])");
                AppParameters.timeSliceA = AppParameters.dataset.corpus.convertDayToTimeSlice(resizeSlider.getLowValue());
                if(resizeSlider.getHighValue() != AppParameters.dataset.corpus.getLength())
                    AppParameters.timeSliceB = AppParameters.dataset.corpus.convertDayToTimeSlice(resizeSlider.getHighValue());
                AppParameters.updateStopwords(stopwordListsCheckComboBox.getCheckModel().getCheckedItems());
                LogUI.addLogEntry("Done.");
            }else{
                LogUI.addLogEntry("Error: no dataset loaded");
            }
        });
        buttonBox.getChildren().addAll(filterButton);
        return buttonBox;
    }

    public final void clearPreprocessUI(){
        stemmingChoiceBox.getSelectionModel().clearSelection();
        lemmatizationChoiceBox.getSelectionModel().clearSelection();
        tokenizationChoiceBox.getSelectionModel().clearSelection();
        timeSliceLengthField.clear();
        preprocessedCorpusList.getSelectionModel().clearSelection();
    }
    
    public final void clearFilterUI(){
        stopwordListsCheckComboBox.getCheckModel().clearChecks();
        resizeSlider.setHighValue(resizeSlider.getHighValue());
        resizeSlider.setLowValue(resizeSlider.getLowValue());
    }

}
