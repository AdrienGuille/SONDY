/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.ui;

import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.DataManipulation;
import fr.ericlab.sondy.core.access.DBAccess;
import fr.ericlab.sondy.core.structure.Configuration;
import fr.ericlab.sondy.core.structure.LogEntry;
import fr.ericlab.sondy.ui.misc.Credits;
import fr.ericlab.sondy.utils.Utils;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


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
 *   Global user interface.
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class GlobalUI {

    // Panels
    DataUI dataInterface;
    EventsUI detectionInterface;
    NetworkUI networkInterface;
    
    // Main UI components
    GridPane globalGrid;
    MenuBar menuBar;
    TabPane globalPane;
    GridPane infoGrid;
    GridPane subInfoGrid;
    Text infoText = new Text("Info");
    Separator separatorInfo = new Separator();
    Label currentDatasetLabel = new Label("Current dataset: ");
    Label databaseStatusLabel = new Label("Database connection status: ");
    Label memoryStateLabel = new Label(" Memory (free, total, max): ");
    Label applicationStatusLabel = new Label(" App status: ");
    TableView<LogEntry> appLog = new TableView<>();
    
    // Global
    AppVariables appVariables;
    File jarFile;
    File classFile;
    
    // App properties
    final String APP_NAME = "SONDY";
    final String APP_VERSION = "0.5.0";
    
    /**
     *
     * @param configuration
     */
    public GlobalUI(Configuration configuration){
        globalPane = new TabPane();
        globalPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        appVariables = AppVariables.getInstance(configuration, globalPane);
        appVariables.addLogEntry("[global] app startup");
        DBAccess dbAccess = new DBAccess();
        dbAccess.initialize(appVariables,true);
        
        menuBar = createMenuBar(configuration,appVariables, dbAccess);
        try {
            dataInterface = new DataUI(appVariables);
            detectionInterface = new EventsUI(dbAccess,appVariables);
            networkInterface = new NetworkUI(dbAccess, appVariables);
            infoText.setId("title");
        } catch (IOException ex) {
            Logger.getLogger(GlobalUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        Tab dataTab = new Tab();
        dataTab.setText("Data");
        Tab detectionTab = new Tab();
        detectionTab.setText("Topic and Event Detection");
        Tab networkTab = new Tab();
        networkTab.setText("Network Analysis");
        networkTab.setContent(networkInterface.getGrid());
        dataTab.setContent(dataInterface.getGrid());
        globalPane.getTabs().add(dataTab);
        detectionTab.setContent(detectionInterface.getGrid());
        globalPane.getTabs().add(detectionTab);
        globalPane.getTabs().add(networkTab);

        globalGrid = new GridPane();
        globalGrid.setPadding(new Insets(0, 0, 10, 0));
        globalGrid.setVgap(0);
        globalGrid.setHgap(0);
        globalGrid.setStyle("-fx-background-color: linear-gradient(#e5e5e5, #e5e5e5);");
        GridPane.setConstraints(menuBar, 0, 0);
        globalGrid.getChildren().add(menuBar);
        GridPane.setConstraints(globalPane, 0, 1);
        globalGrid.getChildren().add(globalPane);
        
        infoGrid = new GridPane();
        infoGrid.setPadding(new Insets(0, 5, 0, 5));
        infoGrid.setVgap(5);
        infoGrid.setHgap(5);
        subInfoGrid = new GridPane();
        subInfoGrid.setPadding(new Insets(0, 5, 0, 5));
        subInfoGrid.setVgap(5);
        subInfoGrid.setHgap(5);
        
        GridPane.setConstraints(infoText, 0, 0);
        infoGrid.getChildren().add(infoText);
        GridPane.setConstraints(separatorInfo, 0, 1);
        infoGrid.getChildren().add(separatorInfo);
        HBox currentDatasetBox = new HBox(5);
        currentDatasetBox.getChildren().addAll(currentDatasetLabel,appVariables.currentDatasetText,appVariables.currentDatasetDiscretizationText,appVariables.currentDatasetPeriod);
        HBox appStatusBox = new HBox(5);
        appStatusBox.setAlignment(Pos.CENTER);
        appStatusBox.getChildren().addAll(applicationStatusLabel,appVariables.applicationProgress); 
        appStatusBox.setMinWidth(250);
        appStatusBox.setMaxWidth(250);
        HBox databaseStatusBox = new HBox(5);
        databaseStatusBox.getChildren().addAll(databaseStatusLabel,appVariables.databaseStatusImage,memoryStateLabel,appVariables.applicationStatus);
        databaseStatusBox.setMinWidth(550);
        databaseStatusBox.setMaxWidth(550);
        HBox statusBox = new HBox(5);
        statusBox.getChildren().addAll(databaseStatusBox,appStatusBox);
        
        GridPane.setConstraints(currentDatasetBox, 0, 0);
        subInfoGrid.getChildren().add(currentDatasetBox);
        GridPane.setConstraints(statusBox, 0, 1);
        subInfoGrid.getChildren().add(statusBox);
        TableColumn logTimeColumn = new TableColumn("time");
        logTimeColumn.setMaxWidth(60);
        logTimeColumn.setMinWidth(60);
        TableColumn logDetailsColumn = new TableColumn("log");
        logDetailsColumn.setMinWidth(743);
        logTimeColumn.setCellValueFactory(
                new PropertyValueFactory<LogEntry, String>("time"));
        logDetailsColumn.setCellValueFactory(
                new PropertyValueFactory<LogEntry, String>("log"));
        appLog.setEditable(false);
        appLog.setDisable(false);
        appLog.setItems(appVariables.logEntries);
        appLog.getColumns().addAll(logTimeColumn, logDetailsColumn);
        appLog.setMinWidth(805);
        appLog.setMaxWidth(805);
        appLog.setMinHeight(100);
        appLog.setMaxHeight(100);
        appLog.setId("logtable");
        
        GridPane.setConstraints(subInfoGrid, 0, 2);
        infoGrid.getChildren().add(subInfoGrid);
        GridPane.setConstraints(appLog, 0, 2);
        subInfoGrid.getChildren().add(appLog);
        GridPane.setConstraints(infoGrid, 0, 2);
        globalGrid.getChildren().add(infoGrid); 
        
        appVariables.updateApplicationStatus(0);
    }
    
    /**
     *
     * @return
     */
    public GridPane getGrid(){
        return globalGrid;
    }
    
    /**
     *
     * @param configuration
     * @param appVariables
     * @param dbAccess
     * @return
     */
    final public MenuBar createMenuBar(final Configuration configuration, final AppVariables appVariables, final DBAccess dbAccess){
        MenuBar menu = new MenuBar();
        menu.setMinWidth(825);
        menu.setMaxWidth(825);
        Menu menuFile = new Menu("File");
        Menu menuEdit = new Menu("Edit");
        Menu menuHelp = new Menu("Help");
        menu.getMenus().addAll(menuFile,menuEdit,menuHelp);
        
        final DataManipulation dataManipulation = new DataManipulation();
        
        MenuItem aboutItem = new MenuItem("About");
        MenuItem docItem = new MenuItem("Documentation");
        MenuItem updateItem = new MenuItem("Check for updates");
        menuHelp.getItems().addAll(aboutItem,updateItem,docItem); 
        
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem importItem = new MenuItem("Import algorithm");
        menuFile.getItems().addAll(importItem,new SeparatorMenuItem(),exitItem); 
        
        MenuItem databaseItem = new MenuItem("Configure database");
        
        docItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                WebView infoView = new WebView();
                    WebEngine engine = infoView.getEngine();
                    engine.load("http://mediamining.univ-lyon2.fr/people/guille/inapp_doc.php");
                    infoView.setMinWidth(910);
                    infoView.setMaxWidth(910);
                    infoView.setMinHeight(720);
                    infoView.setMaxHeight(720);
                    final Stage dialogStageAbout = new Stage();
                    dialogStageAbout.initModality(Modality.WINDOW_MODAL);
                    dialogStageAbout.initStyle(StageStyle.UTILITY);
                    dialogStageAbout.setTitle("Documentation");
                    dialogStageAbout.setScene(new Scene(VBoxBuilder.create().
                        children(infoView).
                        alignment(Pos.CENTER).padding(new Insets(10)).spacing(5).build()));
                    dialogStageAbout.getScene().getStylesheets().add("resources/css/Style.css");
                    dialogStageAbout.show();
            }
        });
        
        updateItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    Text textAppName = new Text(APP_NAME);
                    textAppName.setFont(Font.loadFont(GlobalUI.class.getResource("/resources/fonts/Raleway-Thin.otf").openStream(), 32));
                    ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/app-logo-base.png")));
                    logoView.setFitHeight(111);
                    logoView.setFitWidth(115);
                    Text textVersion = new Text("Version "+APP_VERSION);
                    textVersion.setId("title");
                    final Text latestVersionText = new Text("Checking for updates...");
                    latestVersionText.setId("smalltext");
                    latestVersionText.setTextAlignment(TextAlignment.CENTER);
                    URL sondyVersion = new URL("http://mediamining.univ-lyon2.fr/people/guille/sondy_update.php");
                    BufferedReader in = new BufferedReader(
                    new InputStreamReader(sondyVersion.openStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null)
                        if(inputLine.contains("body")){
                            String lVersion = inputLine.substring(6,11);
                            if(lVersion.compareTo(APP_VERSION) == 1){
                                latestVersionText.setText("A new version is available ("+lVersion+")\nDownload it at:\nhttp://mediamining.univ-lyon2.fr/sondy");
                            }else{
                                latestVersionText.setText("You are running the latest version.");
                            }
                            
                        }
                    in.close();
                    final Stage dialogStageAbout = new Stage();
                    dialogStageAbout.initModality(Modality.WINDOW_MODAL);
                    dialogStageAbout.initStyle(StageStyle.UTILITY);
                    dialogStageAbout.setTitle("Check for updates");
                    dialogStageAbout.setScene(new Scene(VBoxBuilder.create().
                        children(logoView,textAppName,textVersion,new Separator(),latestVersionText).
                        alignment(Pos.CENTER).padding(new Insets(10)).spacing(5).build()));
                    dialogStageAbout.getScene().getStylesheets().add("resources/css/Style.css");
                    dialogStageAbout.show();
                } catch (IOException ex) {
                    Logger.getLogger(GlobalUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        aboutItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    Text textAppName = new Text(APP_NAME);
                    textAppName.setFont(Font.loadFont(GlobalUI.class.getResource("/resources/fonts/Raleway-Thin.otf").openStream(), 32));
                    ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/app-logo-base.png")));
                    logoView.setFitHeight(111);
                    logoView.setFitWidth(115);
                    WebView infoView = new WebView();
                    WebEngine engine = infoView.getEngine();
                    Credits credits = new Credits();
                    engine.loadContent(credits.getHtml());
                    infoView.setMinWidth(380);
                    infoView.setMaxWidth(380);
                    infoView.setMinHeight(150);
                    infoView.setMaxHeight(150);
                    Text textVersion = new Text("Version "+APP_VERSION);
                    textVersion.setId("title");
                    final Stage dialogStageAbout = new Stage();
                    dialogStageAbout.initModality(Modality.WINDOW_MODAL);
                    dialogStageAbout.initStyle(StageStyle.UTILITY);
                    dialogStageAbout.setTitle("About");
                    dialogStageAbout.setScene(new Scene(VBoxBuilder.create().
                        children(logoView,textAppName,textVersion,new Separator(),infoView).
                        alignment(Pos.CENTER).padding(new Insets(10)).spacing(5).build()));
                    dialogStageAbout.getScene().getStylesheets().add("resources/css/Style.css");
                    dialogStageAbout.show();
                } catch (IOException ex) {
                    Logger.getLogger(GlobalUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        importItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
               Label importJarText = new Label("From a JAR file: ");
               importJarText.setMinWidth(145);
               Label importClassText = new Label("From a Class file: ");
               importClassText.setMinWidth(145);
               final Button fileJarButton = new Button("choose file");
               fileJarButton.setMinWidth(150);
               final Button fileClassButton = new Button("choose file");
               fileClassButton.setMinWidth(150);
               WebView infoView = new WebView();
               WebEngine engine = infoView.getEngine();
               infoView.setMinHeight(100);
               infoView.setMinWidth(300);
               infoView.setMaxHeight(100);
               infoView.setMaxWidth(300);
               final Stage dialogStageImport = new Stage();
               dialogStageImport.initModality(Modality.WINDOW_MODAL);
               dialogStageImport.initStyle(StageStyle.UTILITY);
               dialogStageImport.setTitle("Import algorithm");
               HBox fileJarBox = new HBox(5);
               fileJarBox.getChildren().addAll(importJarText,fileJarButton);
               HBox fileClassBox = new HBox(5);
               fileClassBox.getChildren().addAll(importClassText,fileClassButton);
               VBox globalBox = new VBox(5);
               final Button importButton = new Button("import");
               importButton.setMinWidth(300);
               globalBox.getChildren().addAll(fileJarBox, fileClassBox, importButton, infoView);
               dialogStageImport.setScene(new Scene(HBoxBuilder.create().
                   children(globalBox).
                   alignment(Pos.CENTER).padding(new Insets(10)).spacing(5).build()));
               dialogStageImport.getScene().getStylesheets().add("resources/css/Style.css");
               dialogStageImport.show();
               fileJarButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                        jarFile = null;
                        FileChooser fileChooser = new FileChooser();
                        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JAR files (*.jar)", "*.jar");
                        fileChooser.getExtensionFilters().add(extFilter);
                        jarFile = fileChooser.showOpenDialog(null);
                        if(jarFile != null){
                            fileJarButton.setText(jarFile.getPath().substring(jarFile.getPath().lastIndexOf('/')+1));
                        }
                    }
                });
               fileClassButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                        classFile = null;
                        FileChooser fileChooser = new FileChooser();
                        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Class files (*.class)", "*.class");
                        fileChooser.getExtensionFilters().add(extFilter);
                        classFile = fileChooser.showOpenDialog(null);
                        if(classFile != null){
                            fileClassButton.setText(classFile.getPath().substring(classFile.getPath().lastIndexOf('/')+1));
                        }
                    }
                });
               importButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                        File dir = new File(configuration.getWorkspace()+"/algorithms/");
                        boolean error = false;
                        if(!dir.exists()){
                            try {
                                dir.mkdirs();
                                if(classFile!=null){
                                    Utils.copyFile(classFile, new File(configuration.getWorkspace()+"/algorithms/"+classFile.getPath().substring(classFile.getPath().lastIndexOf('/')+1)));
                                }else{
                                    if(jarFile!=null){
                                        Utils.copyFile(jarFile, new File(configuration.getWorkspace()+"/algorithms/"+classFile.getPath().substring(classFile.getPath().lastIndexOf('/')+1)));
                                    }
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
                                error = true;
                                appVariables.addLogEntry("[global] can't access workspace");
                            }
                        }
                    }
                });
            }
        });
        
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        
        databaseItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                final TextField dbAddress = new TextField();
                dbAddress.setPromptText("host");
                final TextField dbUser = new TextField();
                dbUser.setPromptText("user");
                final PasswordField dbPassword = new PasswordField();
                dbPassword.setPromptText("password");
                final TextField dbSchema = new TextField();
                dbSchema.setPromptText("schema");
                Button buttonSaveDB = new Button("save");
                final Stage dialogStageDatabase = new Stage();
                dialogStageDatabase.initModality(Modality.WINDOW_MODAL);
                dialogStageDatabase.initStyle(StageStyle.UTILITY);
                dialogStageDatabase.setTitle("Database - configuration");
                dialogStageDatabase.setScene(new Scene(VBoxBuilder.create().
                    children(dbAddress, dbUser, dbPassword, dbSchema, buttonSaveDB).
                    alignment(Pos.CENTER).padding(new Insets(5)).spacing(5).build()));
                dialogStageDatabase.getScene().getStylesheets().add("resources/css/Style.css");
                buttonSaveDB.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                        appVariables.addLogEntry("[global] changed database configuration");
                        configuration.setHost(dbAddress.getText());
                        configuration.setUsername(dbUser.getText());
                        configuration.setPassword(dbPassword.getText());
                        configuration.setSchema(dbSchema.getText());
                        dbAccess.initialize(appVariables,true);
                        dialogStageDatabase.close();
                        try {
                          File input = new File("sondy-config.properties");
                            Properties prop = new Properties();
                            prop.load(new FileReader(input));
                            prop.setProperty("host", dbAddress.getText());
                            prop.setProperty("username", dbUser.getText());
                            prop.setProperty("password", dbPassword.getText());
                            prop.setProperty("schema", dbSchema.getText());
                            prop.store(new FileWriter(input), "changed database configuration");
                        } catch (IOException ex) {
                            Logger.getLogger(GlobalUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                dialogStageDatabase.show();
            }
        });
        MenuItem repositoryItem = new MenuItem("Configure workspace");
        repositoryItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                final TextField workspacePath = new TextField();
                workspacePath.setMinHeight(20);
                workspacePath.setPromptText("workspace path");
                Button buttonSaveWorkspace = new Button("save");
                final Stage dialogStageWorkspace = new Stage();
                dialogStageWorkspace.setTitle("Workspace - configuration");
                dialogStageWorkspace.initModality(Modality.WINDOW_MODAL);
                dialogStageWorkspace.setScene(new Scene(VBoxBuilder.create().
                    children(workspacePath,buttonSaveWorkspace).
                    alignment(Pos.CENTER).padding(new Insets(5)).spacing(5).build()));
                dialogStageWorkspace.initStyle(StageStyle.UTILITY);
                dialogStageWorkspace.getScene().getStylesheets().add("resources/css/Style.css");
                buttonSaveWorkspace.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        configuration.setWorkspace(workspacePath.getText());
                        appVariables.datasetListContent.clear();
                        appVariables.datasetListContent.addAll(dataManipulation.getDatasetList(configuration.getWorkspace()+"/datasets/"));
                        appVariables.availableStopWords = dataManipulation.getAvailableStopwords(appVariables);
                        dataManipulation.copyBuiltinAlgorithms(appVariables);
                        appVariables.addLogEntry("[global] changed workspace: "+workspacePath.getText());
                        dialogStageWorkspace.close();
                        try {
                            File input = new File("sondy-config.properties");
                            Properties prop = new Properties();
                            prop.load(new FileReader(input));
                            prop.setProperty("workspace", workspacePath.getText());
                            prop.store(new FileWriter(input), "changed workspace");
                        } catch (IOException ex) {
                            Logger.getLogger(GlobalUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                dialogStageWorkspace.show();
            }
        });
        menuEdit.getItems().addAll(databaseItem,repositoryItem); 
        return menu;
    }
}
