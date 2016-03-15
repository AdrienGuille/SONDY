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

import main.java.fr.ericlab.sondy.core.app.Main;
import main.java.fr.ericlab.sondy.core.utils.UIUtils;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.java.fr.ericlab.sondy.algo.eventdetection.EventDetectionMethod;
import main.java.fr.ericlab.sondy.algo.influenceanalysis.InfluenceAnalysisMethod;
import main.java.fr.ericlab.sondy.core.app.Configuration;
import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class GlobalUI {
    // Global UI component
    public GridPane globalGridPane;
    MenuBar globalMenu;
    public static TabPane tabPane;
    
    // UI components
    DataCollectionUI dataCollectionUI;
    DataManipulationUI dataManipulationUI;
    EventDetectionUI eventDetectionUI;
    InfluenceAnalysisUI influenceAnalysisUI;
    LogUI logUI;
    
    public GlobalUI(){
        globalGridPane = new GridPane();
//        dataCollectionUI = new DataCollectionUI();
        dataManipulationUI = new DataManipulationUI();
        eventDetectionUI = new EventDetectionUI();
        influenceAnalysisUI = new InfluenceAnalysisUI();
        logUI = new LogUI();
        menuBar();
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
//        Tab dataCollectionTab = new Tab("Data Collection");
//        dataCollectionTab.setContent(dataCollectionUI.grid);
        Tab dataManipulationTab = new Tab("Data Manipulation");
        dataManipulationTab.setContent(dataManipulationUI.grid);
        Tab eventTab = new Tab("Event Detection");
        eventTab.setContent(eventDetectionUI.grid);
        Tab influenceTab = new Tab("Influence Analysis");
        influenceTab.setContent(influenceAnalysisUI.grid);
        tabPane.getTabs().addAll(dataManipulationTab,eventTab,influenceTab);
        tabPane.getSelectionModel().select(0);
        globalGridPane.add(globalMenu,0,0);
        globalGridPane.add(tabPane,0,1);
        globalGridPane.add(logUI.logGrid,0,2);
        LogUI.addLogEntry("Application started - available cores: "+Configuration.numberOfCores+", workspace: "+Configuration.workspace);
    }
    
    public final void menuBar(){
        globalMenu = new MenuBar();
        globalMenu.setMinWidth(Main.windowWidth);
        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");
        Menu aboutMenu = new Menu("About");
        globalMenu.getMenus().addAll(fileMenu,editMenu,aboutMenu);
        MenuItem exitItem = new MenuItem("Quit SONDY");
        fileMenu.getItems().add(exitItem);
        exitItem.setOnAction((ActionEvent t) -> {
            Platform.exit();
            System.exit(0);
        });
        MenuItem configurationItem = new MenuItem("Edit configuration");
        editMenu.getItems().add(configurationItem);
        configurationItem.setOnAction((ActionEvent t) -> {
            editConfigurationFile();
        });
        MenuItem aboutItem = new MenuItem("About SONDY");
        aboutMenu.getItems().add(aboutItem);
        aboutItem.setOnAction((ActionEvent t) -> {
            about();
        });
    }
    
    public final void editConfigurationFile(){
        try {
            TextArea textArea = new TextArea();
            textArea.setText(FileUtils.readFileToString(Paths.get("./configuration.properties").toFile()));
            UIUtils.setSize(textArea, Main.columnWidthLEFT/2+10, 100);
            final Stage stage = new Stage();
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Edit configuration");
            Button saveButton = new Button("Save changes");
            UIUtils.setSize(saveButton, Main.columnWidthLEFT/2+10, 24);
            Button cancelButton = new Button("Cancel changes");
            cancelButton.setOnAction((ActionEvent t) -> {
                stage.close();
            });
            saveButton.setOnAction((ActionEvent t) -> {
                try {
                    FileUtils.write(Paths.get("./configuration.properties").toFile(),textArea.getText());
                    stage.close();
                } catch (IOException ex) {
                    Logger.getLogger(GlobalUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            UIUtils.setSize(cancelButton, Main.columnWidthLEFT/2+10, 24);
            Label label = new Label();
            label.setId("smalltext");
            label.setText("SONDY needs to restart for the changes to take effect");
            Scene scene = new Scene(VBoxBuilder.create().children(textArea,label,saveButton,cancelButton).alignment(Pos.CENTER).padding(new Insets(10)).spacing(3).build());
            scene.getStylesheets().add("resources/fr/ericlab/sondy/css/GlobalStyle.css");
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(GlobalUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void about(){
        final Stage stage = new Stage();
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("About SONDY");
        WebView webView = new WebView();
        webView.getEngine().loadContent(getReferences());
        webView.setMaxWidth(Main.columnWidthLEFT);
        webView.setMinWidth(Main.columnWidthLEFT);
        webView.setMaxHeight(Main.columnWidthLEFT);
        webView.setMinHeight(Main.columnWidthLEFT);
        Scene scene = new Scene(VBoxBuilder.create().children(new Label("SONDY "+Main.version),new Label("Main developper: Adrien Guille <adrien.guille@univ-lyon2.fr>"),webView).alignment(Pos.CENTER).padding(new Insets(10)).spacing(3).build());
        scene.getStylesheets().add("resources/fr/ericlab/sondy/css/GlobalStyle.css");
        stage.setScene(scene);
        stage.show();
    }
    
    public String getReferences(){
        String references = "<html><body><h1>Event detection</h1><ul>";
        Reflections reflections = new Reflections("main.java.fr.ericlab.sondy.algo.eventdetection");    
        Set<Class<? extends EventDetectionMethod>> classes = reflections.getSubTypesOf(EventDetectionMethod.class);   
        for(Class<? extends EventDetectionMethod> aClass : classes){
            try {
                EventDetectionMethod method = (EventDetectionMethod) Class.forName(aClass.getName()).newInstance();
                references += method.getCitation();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(EventDetectionUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        references += "</ul><h1>Influence analysis</h1><ul>";
        reflections = new Reflections("main.java.fr.ericlab.sondy.algo.influenceanalysis");    
        Set<Class<? extends InfluenceAnalysisMethod>> classes1 = reflections.getSubTypesOf(InfluenceAnalysisMethod.class);   
        for(Class<? extends InfluenceAnalysisMethod> aClass : classes1){
            try {
                InfluenceAnalysisMethod method = (InfluenceAnalysisMethod) Class.forName(aClass.getName()).newInstance();
                references += method.getCitation();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(EventDetectionUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return references+"</ul></body></html>";
    }
}
