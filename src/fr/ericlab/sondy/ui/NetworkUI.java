/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.ui;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.algo.networkanalysis.NetworkAnalysisAlgorithm;
import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.DataManipulation;
import fr.ericlab.sondy.core.access.DBAccess;
import fr.ericlab.sondy.core.structure.Collection;
import fr.ericlab.sondy.core.structure.LogEntry;
import fr.ericlab.sondy.core.structure.Message;
import fr.ericlab.sondy.ui.misc.EditingCell;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFX;
import javafx.embed.swing.SwingView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;

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
 *   User interface of the network analysis service. It permits observing the 
 *   social network structure and finding, e.g. influential nodes or 
 *   communities. Visualizations are interactive, making it possible for users 
 *   to actively interact with the system.
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class NetworkUI {
    
    // Grids
    GridPane globalGrid;
    GridPane algoGrid;
    GridPane visualizationGrid;
    
    // Seprators
    Separator algoSeparator = new Separator();
    Separator visualizationSeparator = new Separator();
    
    // Algorithm
    Text algoText = new Text("Algorithms");
    Text algoDescription = new Text("algorithm-description");
    ListView<String> algoListView = new ListView<>();
    ObservableList<String> algoList = FXCollections.observableArrayList();
    NetworkAnalysisAlgorithm algo;
    Button refApplyAlgoButton = new Button("apply");
    TableView<AlgorithmParameter> algorithmParametersTable = new TableView<>();
    ObservableList<AlgorithmParameter> algorithmParameters = FXCollections.observableArrayList(new AlgorithmParameter("",""));
    HashMap<String,String> algoMap = new HashMap<>();
    
    // Results
    DefaultGraph userGraph;
    HashMap<String,Integer> results;
    String lastQuery = "";
    
    // Network visualization
    Viewer viewer;
    View view;
    SwingView swingView;
    Text visualizationText = new Text("Visualization");
    
    // - Network contextual menu
    MenuItem messagesMenuItem = new MenuItem("See messages");
    MenuItem nodeIdItem = new MenuItem("Node id: --");
    MenuItem zoomInItem = new MenuItem("Zoom in");
    MenuItem zoomOutItem = new MenuItem("Zoom out");
    MenuItem rotateItem = new MenuItem("Rotate view");
    ContextMenu contextMenuGraph = new ContextMenu(nodeIdItem,messagesMenuItem,new SeparatorMenuItem(),zoomInItem,zoomOutItem,rotateItem);
    String selectedNodeId = "--";
    
    // - ScatterChart
    ScatterChart<Number,Number> chart;
    NumberAxis xAxis;
    NumberAxis yAxis;
    Rectangle rectangleLegend = new Rectangle();
    Text rectangleText = new Text("coloring scheme: from lowest to highest rank");
    
    // Global
    AppVariables appVariablesRef;
    DBAccess dbAccess;
    
    /**
     *
     * @param db
     * @param appVariables
     * @throws IOException
     */
    public NetworkUI(DBAccess db, final AppVariables appVariables) throws IOException{
        ComponentScanner scanner = new ComponentScanner();
        Set<Class<?>> classes = scanner.getClasses(new ComponentQuery() {
            @Override
            protected void query() {
                select().from("fr.ericlab.sondy.algo.networkanalysis").returning(allExtending(NetworkAnalysisAlgorithm.class));
            }
        });
        for(Class<?> c : classes){
            try {
                NetworkAnalysisAlgorithm a = (NetworkAnalysisAlgorithm) Class.forName(c.getName()).newInstance();
                algoList.add(a.getName());
                algoMap.put(a.getName(),c.getName());
            } catch (    InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                Logger.getLogger(EventsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Global
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        appVariablesRef = appVariables;
        dbAccess = db;
        globalGrid = new GridPane();
        globalGrid.setPadding(new Insets(5,5,5,5));
        globalGrid.setVgap(5);
        globalGrid.setHgap(5);
        globalGrid.setStyle("-fx-background-color: linear-gradient(#ffffff, #e5e5e5)");
        algoText.setId("title");
        visualizationText.setId("title");
        GridPane.setConstraints(algoText, 0, 0);
        globalGrid.getChildren().add(algoText);
        GridPane.setConstraints(algoSeparator, 0, 1);
        globalGrid.getChildren().add(algoSeparator);
        
        // Algorithm
        algoGrid = new GridPane();
        algoGrid.setPadding(new Insets(0,5,0,5));
        algoGrid.setVgap(5);
        algoGrid.setHgap(5);
        algoListView.setOrientation(Orientation.HORIZONTAL);
        algoListView.setMinWidth(550);
        algoListView.setMaxWidth(550);
        algoListView.setMinHeight(65);
        algoListView.setMaxHeight(65);
        algoListView.setItems(algoList);
        appVariablesRef.applyAlgoButtonNetwork.setMinWidth(250);
        appVariablesRef.applyAlgoButtonNetwork.setMaxWidth(250);
        algoListView.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<String>() {
            @Override
                public void changed(ObservableValue<? extends String> ov, 
                    String old_val, String new_val) {
                    if(new_val!=null && appVariablesRef.getCurrentDatasetDiscretization().length()!=0){
                        appVariablesRef.applyAlgoButtonNetwork.setDisable(false);
                        if(!appVariables.currentDatasetText.getText().equalsIgnoreCase("no dataset selected") && !appVariables.currentDatasetDiscretizationText.getText().equalsIgnoreCase("()")){
                            try {
                                algo = (NetworkAnalysisAlgorithm) Class.forName(algoMap.get(new_val)).newInstance();
                                algorithmParametersTable.setItems(algo.parameters);
                                algoDescription.setText(algo.algoDescription);
                            } catch (                    InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                                Logger.getLogger(EventsUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                       
                    }else{
                        appVariablesRef.applyAlgoButtonNetwork.setDisable(true);
                    }
                }
            }
        );
        Callback<TableColumn, TableCell> cellFactory =
          new Callback<TableColumn, TableCell>() {
              @Override
              public TableCell call(TableColumn p) {
                  return new EditingCell();
              }
          };
        TableColumn algoParameterColumn = new TableColumn("parameter");
        algoParameterColumn.setMinWidth(124);
        algoParameterColumn.setMaxWidth(124);
        TableColumn algoValueColumn = new TableColumn("value");
        algoValueColumn.setMaxWidth(125);
        algoValueColumn.setMinWidth(125);
        algoParameterColumn.setCellValueFactory(
                new PropertyValueFactory<AlgorithmParameter, String>("parameter"));
        algoValueColumn.setCellValueFactory(
                new PropertyValueFactory<AlgorithmParameter, String>("value"));
        algoValueColumn.setCellFactory(cellFactory);
        algoValueColumn.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<AlgorithmParameter, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<AlgorithmParameter, String> t) {
                    ((AlgorithmParameter) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setValue(t.getNewValue());
                }
            }
        );
        algoValueColumn.setOnEditStart(
            new EventHandler<TableColumn.CellEditEvent<AlgorithmParameter, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<AlgorithmParameter, String> t) {
                    ((AlgorithmParameter) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setValue(t.getNewValue());
                }
            }
        );
        algorithmParametersTable.setItems(algorithmParameters);
        algorithmParametersTable.getColumns().addAll(algoParameterColumn, algoValueColumn);
        algorithmParametersTable.setEditable(true);
        algorithmParametersTable.setDisable(false);
        algorithmParametersTable.setMinSize(250, 65);
        algorithmParametersTable.setMaxSize(250, 65);
        GridPane.setConstraints(algoListView, 0, 0);
        algoGrid.getChildren().add(algoListView);
        GridPane.setConstraints(algorithmParametersTable, 1, 0);
        algoGrid.getChildren().add(algorithmParametersTable);
        GridPane.setConstraints(appVariablesRef.applyAlgoButtonNetwork, 1, 1);
        algoGrid.getChildren().add(appVariablesRef.applyAlgoButtonNetwork);
        algoDescription.setId("smalltext");
        GridPane.setConstraints(algoDescription, 0, 1);
        algoGrid.getChildren().add(algoDescription);
        GridPane.setConstraints(algoGrid, 0, 2);
        globalGrid.getChildren().add(algoGrid);
        
        // Network visualization
        visualizationGrid = new GridPane();
        visualizationGrid.setPadding(new Insets(0,5,0,5));
        visualizationGrid.setVgap(5);
        visualizationGrid.setHgap(5);
        GridPane.setConstraints(visualizationText, 0, 3);
        globalGrid.getChildren().add(visualizationText);
        GridPane.setConstraints(visualizationSeparator, 0, 4);
        globalGrid.getChildren().add(visualizationSeparator);
        
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        xAxis.setLabel("rank");
        yAxis.setLabel("# of users");
        chart = new ScatterChart<>(xAxis,yAxis);
        userGraph = new DefaultGraph("");
        userGraph = new DefaultGraph("");
        viewer = new Viewer(userGraph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        resetUsersGraph();
        view = viewer.addDefaultView(false); 
        view.resizeFrame(550, 350);

        if(!System.getProperty("os.name").contains("Mac")){
            SwingFX.init();
            swingView = new SwingView(view);
//            swingView.setStyle("-fx-background-color:  transparent;");
            swingView.setMinSize(550, 350);
            swingView.setMaxSize(550, 350);
            GridPane.setConstraints(swingView, 0, 0);
            visualizationGrid.getChildren().add(swingView);
            
            zoomInItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                    view.getCamera().setViewPercent(view.getCamera().getViewPercent()/2);
                }
            });
            
            zoomOutItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                    view.getCamera().setViewPercent(view.getCamera().getViewPercent()*2);
                }
            });
            
            rotateItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                    view.getCamera().setViewRotation(view.getCamera().getViewRotation()+10);
                }
            });
            
            EventHandler<MouseEvent> mouseHandlerGraphClick = new EventHandler<MouseEvent>() { 
                @Override 
                public void handle(MouseEvent mouseEvent) { 
                    if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                        if(mouseEvent.getButton() == MouseButton.SECONDARY){
                            contextMenuGraph.show(swingView,mouseEvent.getScreenX(),mouseEvent.getScreenY());
                            Node node = (Node) view.findNodeOrSpriteAt(mouseEvent.getX(), mouseEvent.getY());
                            if(node != null){
                                nodeIdItem.setText("Node id: "+node.getId());
                                selectedNodeId = node.getId();
                            }else{
                                nodeIdItem.setText("Node id: --");
                                selectedNodeId = "--";
                            }
                        }
                        if(mouseEvent.getButton() == MouseButton.PRIMARY){
                            double translateCoeff = view.getCamera().getViewPercent();
                            Point3 center = view.getCamera().getViewCenter();
                            if(mouseEvent.getY()>175){
                                view.getCamera().setViewCenter(center.x, center.y-5*translateCoeff, center.z);
                            }else{
                                view.getCamera().setViewCenter(center.x, center.y+5*translateCoeff, center.z);
                            }
                            if(mouseEvent.getX()>275){
                                view.getCamera().setViewCenter(center.x+5*translateCoeff, center.y, center.z);
                            }else{
                                view.getCamera().setViewCenter(center.x-5*translateCoeff, center.y, center.z);
                            }
                        }
                        if(mouseEvent.getButton() == MouseButton.MIDDLE){
                            view.getCamera().setViewPercent(view.getCamera().getViewPercent()/2);
                        }
                    }
                }
            };   
            swingView.setOnMousePressed(mouseHandlerGraphClick);
        }else{
            System.out.println("javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));
            ImageView macNetworkImage = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/mac_network.png")));
            macNetworkImage.setFitHeight(350);
            macNetworkImage.setFitWidth(550);
            GridPane.setConstraints(macNetworkImage, 0, 0);
            visualizationGrid.getChildren().add(macNetworkImage);
        }
        
        messagesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                createMessagesStage(dbAccess, selectedNodeId);
            }
        });
        
        chart.setLegendVisible(false);
        chart.setMinHeight(330);
        chart.setMaxHeight(330);
        chart.setMinWidth(250);
        chart.setMaxWidth(250);
        rectangleLegend.setHeight(15);
        rectangleLegend.setWidth(250);
        Stop[] stops = new Stop[] { new Stop(0, Color.STEELBLUE), new Stop(0.5, Color.YELLOW), new Stop(1, Color.RED)};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        rectangleLegend.setFill(lg1);
        VBox chartBox = new VBox(5);
        rectangleText.setId("smalltext");
        chartBox.getChildren().addAll(rectangleLegend,chart);
        VBox rectangleTextBox = new VBox();
        rectangleTextBox.setMinHeight(350);
        rectangleTextBox.setMaxHeight(350);
        rectangleTextBox.setAlignment(Pos.TOP_CENTER);
        rectangleTextBox.getChildren().add(rectangleText);
        
        GridPane.setConstraints(chartBox, 1, 0);
        visualizationGrid.getChildren().add(chartBox);
        GridPane.setConstraints(rectangleTextBox, 1, 0);
        visualizationGrid.getChildren().add(rectangleTextBox);
        GridPane.setConstraints(visualizationGrid, 0, 5);
        globalGrid.getChildren().add(visualizationGrid);
        
        // Network analysis
        appVariablesRef.applyAlgoButtonNetwork.setDisable(true);
        appVariablesRef.applyAlgoButtonNetwork.setOnAction(new EventHandler<ActionEvent>(){
        @Override
            public void handle(ActionEvent event) {
                if(appVariables.enableGraphVizualization){
                    Timestamp startTime = new Timestamp((long) (appVariables.getStartDay().getTime() + appVariables.startDaySelection*24*60*60*1000*1L));
                    Timestamp endTime = new Timestamp((long) (appVariables.getStartDay().getTime() + appVariables.endDaySelection*24*60*60*1000*1L));
                    final String query = "Select distinct msg_author, time_slice from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+startTime+"' and msg_post_time< '"+endTime+"' and msg_text like '%"+appVariables.selectedTerm.split(" ")[0]+"%' group by msg_author;";
                    appVariablesRef.updateApplicationStatus(-1);
                    disableInterface(true);
                    if(!query.equals(lastQuery)){
                        resetUsersGraph();
                    }
                    chart.getData().clear();
                    final Task<String> waitingTask = new Task<String>() {
                    @Override
                    public String call() throws Exception {
                        if(!query.equals(lastQuery)){
                            view.getCamera().resetView();
                            userGraph.addAttribute("ui.quality");
                            userGraph.addAttribute("ui.antialias");
                            DBAccess dbAccessAlgo = new DBAccess();
                            dbAccessAlgo.initialize(appVariables, false);
                            ResultSet rs1 = dbAccessAlgo.getStatement().executeQuery(query);
                            final HashSet<String> users = new HashSet<>();
                            while(rs1.next()){
                                String nodeId = rs1.getString(1);
                                userGraph.addNode(nodeId).addAttribute("ui.hide");
                                userGraph.getNode(nodeId).addAttribute("timeslice", rs1.getInt(2));
                                users.add(nodeId);
                            }
                            Statement statement = dbAccessAlgo.getStatement();
                            for(String user : users){
                                ResultSet rs2 = statement.executeQuery("Select connected_user from "+appVariables.configuration.getSchema()+"."+appVariables.currentDatasetText.getText()+"_network where user =\""+user+"\"");
                                while(rs2.next()){
                                    if(users.contains(rs2.getString(1))){
                                        userGraph.addEdge(user+"-"+rs2.getString(1), user, rs2.getString(1), true);
                                    }
                                }
                            }
                            removeIsolatedUsers();
                            dbAccessAlgo.close();
                            lastQuery = query;
                        }
                        results = algo.apply(userGraph, appVariables);
                        return "" ;
                    }
                };
                waitingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        if(results.entrySet() != null && results.entrySet().size() > 0) {
                            float maxRank = 0;
                            for (Map.Entry<String, Integer> entry : results.entrySet()) {
                                float rank = (float)entry.getValue();
                                if(rank > maxRank){
                                    maxRank = rank;
                                }
                            }
                            xAxis.setUpperBound(maxRank);
                            for(Node node : userGraph.getNodeSet()){
                                node.addAttribute("ui.color", ((float)results.get(node.getId()))/maxRank);
                            }
                            XYChart.Series<Number,Number> seriesDistribution = new XYChart.Series<>();
                            Iterator it = Collection.getDistribution(results).entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = (Map.Entry)it.next();
    //                            if((int)pairs.getValue()<=(int)maxRank){
                                    seriesDistribution.getData().add(new XYChart.Data(pairs.getKey(),pairs.getValue()));
    //                            }
                            }
                            chart.getData().add(seriesDistribution);
                        } 
                        appVariablesRef.updateApplicationStatus(0);
                        disableInterface(false);
                    }
                }); 
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(waitingTask);
                }
            }
        });
    }
    
    public void createMessagesStage(DBAccess dbAccess, String userId){
        final int NB_TERMS = 5;
        Label topicLabel = new Label("user: "+userId);
        topicLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
        TableColumn timeColumn = new TableColumn("time");
        timeColumn.setMaxWidth(170);
        timeColumn.setMinWidth(170);
        TableColumn authorColumn = new TableColumn("author");
        authorColumn.setMaxWidth(100);
        authorColumn.setMinWidth(100);
        TableColumn textColumn = new TableColumn("text");
        textColumn.setMinWidth(410);
        timeColumn.setCellValueFactory(
            new PropertyValueFactory<LogEntry, String>("time"));
        authorColumn.setCellValueFactory(
            new PropertyValueFactory<LogEntry, String>("author"));
        textColumn.setCellValueFactory(
            new PropertyValueFactory<LogEntry, String>("text"));
        final ObservableList<Message> messagesList = FXCollections.observableArrayList(dbAccess.getMessages(appVariablesRef, userId, 5000));
        TableView<Message> messagesTableView = new TableView<>();
        messagesTableView.setId("messagestable");
        messagesTableView.getColumns().addAll(timeColumn,authorColumn,textColumn);
        messagesTableView.setItems(messagesList);
        messagesTableView.setMinWidth(680);
        messagesTableView.setMaxWidth(680);
        messagesTableView.setMinHeight(200);
        messagesTableView.setMaxHeight(200);
        Button frequentTermsButton = new Button("extract frequent terms");
        frequentTermsButton.setMinWidth(680);
        frequentTermsButton.setMaxWidth(680);
        HBox boxFrequentTerms = new HBox(5);
        boxFrequentTerms.setAlignment(Pos.CENTER);
        final TextField[] frequentTerms = new TextField[NB_TERMS];
        for(int i = 0; i < NB_TERMS; i++){
            frequentTerms[i] = new TextField();
            frequentTerms[i].setMinWidth(130);
            frequentTerms[i].setMaxWidth(130);
            frequentTerms[i].setDisable(true);
        }
        boxFrequentTerms.getChildren().addAll(frequentTerms);
        final Stage messagesStage = new Stage();
        messagesStage.initStyle(StageStyle.UTILITY);
        messagesStage.initModality(Modality.WINDOW_MODAL);
        messagesStage.setScene(new Scene(VBoxBuilder.create().
            children(topicLabel,new Separator(),messagesTableView,frequentTermsButton,boxFrequentTerms).
            alignment(Pos.CENTER).padding(new Insets(10)).spacing(5).build()));
        messagesStage.setTitle("Messages");
        messagesStage.show();
        messagesStage.getScene().getStylesheets().add("resources/css/Style.css");
        frequentTermsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String allMessages = new String();
                for(Message m : messagesList){
                    allMessages += m.getText()+" ";
                }
                DataManipulation dm = new DataManipulation();
                String[] topTerms = dm.getFrequentCoocurringTerms(allMessages, NB_TERMS, appVariablesRef.selectedTerm, appVariablesRef);
                for(int j = 0; j < NB_TERMS; j++){
                    frequentTerms[j].setText(topTerms[j]);
                }
            }
        });
    }
    
    public void removeIsolatedUsers(){
        HashSet<Node> nodes = new HashSet<>();
        for(Node n : userGraph){
            if(n.getDegree() == 0){
                nodes.add(n);
            }
        }
        for(Node n : nodes){
            userGraph.removeNode(n);
        }
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
    
    private void resetUsersGraph(){
        userGraph.clear();
        userGraph.addAttribute("ui.quality");
        userGraph.addAttribute("ui.antialias");
        String css = ""
            + "graph { fill-mode: gradient-radial; fill-color : white,gray;}"
            + "edge  { fill-mode: plain; fill-color: rgba(75,75,75,75); size:0.1px; z-index: 1; arrow-shape: none;}" 
            + "node  { fill-mode: dyn-plain; fill-color: #4682B4,yellow,red ; size: 6px ;z-index : 2; stroke-mode: plain;}"
            + "node:clicked   { fill-color: black;}";
        userGraph.addAttribute("ui.stylesheet", css);
    }

}
