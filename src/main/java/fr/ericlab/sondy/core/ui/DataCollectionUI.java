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

import java.nio.file.Paths;
import main.java.fr.ericlab.sondy.core.app.Main;
import main.java.fr.ericlab.sondy.core.sources.DataSources;
import main.java.fr.ericlab.sondy.core.utils.UIUtils;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class DataCollectionUI {
    public GridPane grid;
    
    // Available sources
    // - list
    ListView<String> sourceListView;
    DataSources availableSources;
    
    // Create a source
    ChoiceBox sourceTypeList;
    TextField newSourceIdentifierField;
    TextArea configurationTextArea;
    
    public DataCollectionUI(){
        grid = new GridPane();
        grid.setPadding(new Insets(5, 5, 5, 5));
        
        // Adding separators
        grid.add(new Text("Available sources"),0,0);
        grid.add(new Separator(),0,1);
        grid.add(new Text("Create a source"),0,3);
        grid.add(new Separator(),0,4);
        grid.add(new Text("Collect data from the selected source"),0,6);
        grid.add(new Separator(),0,7);
        
        availableSourcesUI();
        newSourceUI();
        collectDataUI();
    }
    
    public final void availableSourcesUI(){
        availableSources = new DataSources();
        availableSources.update();
        sourceListView = new ListView<>();
        sourceListView.setOrientation(Orientation.HORIZONTAL);
        sourceListView.setItems(availableSources.list);
        sourceListView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
            
        });
        UIUtils.setSize(sourceListView,Main.columnWidthLEFT,64);
        Label sourceDescriptionLabel = new Label("https://dev.twitter.com/overview/api");
        sourceDescriptionLabel.setId("smalltext");
        UIUtils.setSize(sourceDescriptionLabel,Main.columnWidthLEFT,24);
        VBox availableSourcesLEFT = new VBox();
        availableSourcesLEFT.getChildren().addAll(sourceListView,new Rectangle(0,3),sourceDescriptionLabel);
        
        HBox availableSourcesBOTH = new HBox(5);
        availableSourcesBOTH.getChildren().addAll(availableSourcesLEFT,new Rectangle(Main.columnWidthRIGHT,0));
        grid.add(availableSourcesBOTH,0,2);
    }
    
    public final void newSourceUI(){
        GridPane gridLEFT = new GridPane();
        // Labels
        Label sourceIdentifierLabel = new Label("Source ID");
        UIUtils.setSize(sourceIdentifierLabel, Main.columnWidthLEFT/2, 24);
        Label sourceTypeLabel = new Label("Source type");
        UIUtils.setSize(sourceTypeLabel, Main.columnWidthLEFT/2, 24);
        Label sourceConfigLabel = new Label("Source configuration");
        sourceConfigLabel.setAlignment(Pos.CENTER_LEFT);
        UIUtils.setSize(sourceConfigLabel, Main.columnWidthLEFT/2, 150);
        
        gridLEFT.add(sourceTypeLabel,0,0);
        gridLEFT.add(new Rectangle(0,3),0,1);
        gridLEFT.add(sourceIdentifierLabel,0,2);
        gridLEFT.add(new Rectangle(0,3),0,3);
        gridLEFT.add(sourceConfigLabel,0,4);
        
        // Values
        sourceTypeList = new ChoiceBox();
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("Twitter");
        sourceTypeList.setItems(list);
        UIUtils.setSize(sourceTypeList,Main.columnWidthLEFT/2, 24);
        newSourceIdentifierField = new TextField();
        newSourceIdentifierField.setPromptText("unique identifier");
        UIUtils.setSize(newSourceIdentifierField,Main.columnWidthLEFT/2, 24);
        configurationTextArea = new TextArea();
        configurationTextArea.setText("# This is a configuration file for Twitter\n" +
"# It is formatted as a Java properties file\n" +
"# i.e. a property (key = value) per line \n" +
"OAuthConsumerKey = w9ixmKqezBWtyughn4y7w\n" +
"OAuthConsumerSecret = mQ7L6cfSRPRAdUoiIOSWRYaHBeU5yBTRPGgc8fFdY\n" +
"OAuthAccessToken = 2371904670-XAnOV6XquVDuWzXwwhAvKiZ9T1DI9ziM3r7Cz3s\n" +
"OAuthAccessTokenSecret = wRwJhSq1m7zZeQYeTgivVSZ6H7acsv0KNiznF3StoH4TU");
        UIUtils.setSize(configurationTextArea, Main.columnWidthLEFT/2, 150);
        
        gridLEFT.add(sourceTypeList,1,0);
        gridLEFT.add(newSourceIdentifierField,1,2);
        gridLEFT.add(configurationTextArea,1,4);
        HBox importDatasetBOTH = new HBox(5);
        importDatasetBOTH.getChildren().addAll(gridLEFT,createNewSourceButton());
        grid.add(importDatasetBOTH,0,5);
    }
    
    final public void collectDataUI(){
        GridPane gridLEFT = new GridPane();
        Label queryLabel = new Label("Query");
        UIUtils.setSize(queryLabel,Main.columnWidthLEFT/2, 24);
        TextField queryField = new TextField();
        queryField.setPromptText("formatted query");
        UIUtils.setSize(queryField,Main.columnWidthLEFT/2, 24);
        Label durationLabel = new Label("Duration");
        UIUtils.setSize(durationLabel,Main.columnWidthLEFT/2, 24);
        TextField durationField = new TextField();
        durationField.setPromptText("duration in days (e.g. 2)");
        UIUtils.setSize(durationField,Main.columnWidthLEFT/2, 24);
        Label datasetIDLabel = new Label("Dataset ID");
        UIUtils.setSize(datasetIDLabel,Main.columnWidthLEFT/2, 24);
        TextField datasetIDField = new TextField();
        datasetIDField.setPromptText("unique identifier");
        UIUtils.setSize(datasetIDField,Main.columnWidthLEFT/2, 24);
        
        gridLEFT.add(queryLabel,0,0);
        gridLEFT.add(queryField,1,0);
        gridLEFT.add(new Rectangle(0,3),0,1);
        gridLEFT.add(durationLabel,0,2);
        gridLEFT.add(durationField,1,2);
        gridLEFT.add(new Rectangle(0,3),0,3);
        gridLEFT.add(datasetIDLabel,0,4);
        gridLEFT.add(datasetIDField,1,4);
        
        Button button = new Button("Collect");
        UIUtils.setSize(button,Main.columnWidthRIGHT, 24);
        button.setAlignment(Pos.CENTER);
        VBox buttonBox = new VBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(button);
        
        HBox collectDataBOTH = new HBox(5);
        collectDataBOTH.getChildren().addAll(gridLEFT,buttonBox);
        grid.add(collectDataBOTH,0,8);
    }
    
    public final VBox createNewSourceButton(){
        VBox buttonBox = new VBox();
        buttonBox.setAlignment(Pos.CENTER);
        Button newSourceButton = new Button("Import");
        UIUtils.setSize(newSourceButton,Main.columnWidthRIGHT,24);
        newSourceButton.setOnAction((ActionEvent event) -> {
            int i = 0;
        });
        buttonBox.getChildren().addAll(newSourceButton);
        return buttonBox;
    }

}
