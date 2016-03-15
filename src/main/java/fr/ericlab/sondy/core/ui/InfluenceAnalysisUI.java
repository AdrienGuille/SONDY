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

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import main.java.fr.ericlab.sondy.core.app.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.java.fr.ericlab.sondy.algo.Parameter;
import main.java.fr.ericlab.sondy.algo.influenceanalysis.InfluenceAnalysisMethod;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import main.java.fr.ericlab.sondy.core.structures.Message;
import main.java.fr.ericlab.sondy.core.utils.ArrayUtils;
import main.java.fr.ericlab.sondy.core.utils.CustomSwingNode;
import main.java.fr.ericlab.sondy.core.utils.UIUtils;
import org.apache.commons.lang3.StringUtils;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.reflections.Reflections;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class InfluenceAnalysisUI {
    public GridPane grid;
    
    // Methods
    ListView<String> methodList;
    // - parameters
    TableView<Parameter> parameterTable;
    HashMap<String,String> methodMap;
    // - apply
    Label methodDescriptionLabel;
    Button applyButton;
    InfluenceAnalysisMethod selectedMethod;
    
    // Influence analysis
    // - network visualization
    CustomSwingNode swingNode;
    View view;
    Button zoomInButton;
    Button zoomOutButton;
    // - rank distribution chart
    CategoryAxis xAxis;
    NumberAxis yAxis;
    BarChart<String,Number> rankDistributionChart;
    
    public InfluenceAnalysisUI(){
        // Initializing the main grid
        grid = new GridPane();
        grid.setPadding(new Insets(5, 5, 5, 5));
        
        // Adding separators
        grid.add(new Text("Available methods"),0,0);
        grid.add(new Separator(),0,1);
        grid.add(new Text("Network visualization"),0,3);
        grid.add(new Separator(),0,4);
        
        availabeMethodsUI();
        rankAnalysisUI();
    }
    
    public final void availabeMethodsUI(){
        initializeAvailableMethodList();
        methodDescriptionLabel = new Label("Selected method description");
        methodDescriptionLabel.setId("smalltext");
        UIUtils.setSize(methodDescriptionLabel,Main.columnWidthLEFT,24);
        VBox methodsLEFT = new VBox();
        methodsLEFT.getChildren().addAll(methodList,new Rectangle(0,3),methodDescriptionLabel);
        // Right part
        applyButton = createApplyMethodButton();
        UIUtils.setSize(applyButton, Main.columnWidthRIGHT, 24);
        parameterTable = new TableView<>();
        UIUtils.setSize(parameterTable, Main.columnWidthRIGHT, 64);
        initializeParameterTable();
        VBox methodsRIGHT = new VBox();
        methodsRIGHT.getChildren().addAll(parameterTable,new Rectangle(0,3),applyButton);
        // Both parts
        HBox methodsBOTH = new HBox(5);
        methodsBOTH.getChildren().addAll(methodsLEFT,methodsRIGHT);
        grid.add(methodsBOTH,0,2);
    }
    
    public final void rankAnalysisUI(){
        initializeRankDistributionChart();
        HBox rankAnalysisBOTH = new HBox(0);
        rankAnalysisBOTH.getChildren().addAll(createNetworkVisualization(),rankDistributionChart);
        grid.add(rankAnalysisBOTH,0,5);
    }
    
    public final Button createApplyMethodButton(){
        Button button = new Button("Apply method");
        button.setOnAction((ActionEvent ae) -> {
            try {
                if(AppParameters.event != null){
                    AppParameters.disableUI(true);
                    rankDistributionChart.getData().clear();
                    LogUI.addLogEntry("Running '"+selectedMethod.getName()+"'...");
                    final Task<String> waitingTask = new Task<String>() {
                        @Override
                        public String call() throws Exception {
                            AppParameters.dataset.network.updateAuthorNetwork();
                            Thread job = new Thread(selectedMethod);
                            job.start();
                            job.join();
                            return "" ;
                        }
                    };
                    waitingTask.setOnSucceeded((WorkerStateEvent event1) -> {
                        updateRankDistributionChart();
                        AppParameters.disableUI(false);
                        LogUI.addLogEntry("Done: "+selectedMethod.getLog());
                    }); 
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(waitingTask);
                }else{
                    LogUI.addLogEntry("Error: no event selected");
                }
            } catch (Exception ex) {
                Logger.getLogger(EventDetectionUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return button;
    }
    
    public final void initializeAvailableMethodList(){
        methodMap = new HashMap<>();
        methodList = new ListView<>();
        methodList.setOrientation(Orientation.HORIZONTAL);
        UIUtils.setSize(methodList,Main.columnWidthLEFT,64);
        updateAvailableMethods();
        methodList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
            try {
                selectedMethod = (InfluenceAnalysisMethod) Class.forName(methodMap.get(new_val)).newInstance();
                methodDescriptionLabel.setText(selectedMethod.getDescription());
                parameterTable.setItems(selectedMethod.parameters.list);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(EventDetectionUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    public final void initializeParameterTable(){
        parameterTable.setEditable(true);
        TableColumn keyColumn = new TableColumn("Parameter");
        keyColumn.setMinWidth(Main.columnWidthRIGHT/2-1);
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn valueColumn = new TableColumn("Value");
        valueColumn.setMinWidth(Main.columnWidthRIGHT/2-1);
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value")); 
        valueColumn.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<Parameter, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Parameter, String> t) {
                    ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow())).setValue(t.getNewValue());
                }
            }
        );
        parameterTable.getColumns().addAll(keyColumn,valueColumn);
    }
    
    public final VBox createNetworkVisualization(){
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Viewer viewer = new Viewer(AppParameters.authorNetwork, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        view = viewer.addDefaultView(false); 
        viewer.enableAutoLayout();
        view.resizeFrame(Main.columnWidthLEFT, 290);
        swingNode = new CustomSwingNode();
        swingNode.setContent(view);
        swingNode.resize(Main.columnWidthLEFT, 290);
        EventHandler<MouseEvent> mouseHandlerGraphClick = new EventHandler<MouseEvent>() { 
            @Override 
            public void handle(MouseEvent mouseEvent) { 
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    if(mouseEvent.getButton() == MouseButton.SECONDARY){
                        Node node = (Node) view.findNodeOrSpriteAt(mouseEvent.getX(), mouseEvent.getY());
                        if(node != null){
                            userMessages(node.getId());
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
        EventHandler<ScrollEvent> mouseHandlerGraphScroll = new EventHandler<ScrollEvent>() { 
            @Override
            public void handle(ScrollEvent event) {
                if(event.getDeltaY() < 0){
                    view.getCamera().setViewPercent(view.getCamera().getViewPercent()*2);
                }else{
                    view.getCamera().setViewPercent(view.getCamera().getViewPercent()/2);
                }
            }
        };   
        swingNode.setOnMousePressed(mouseHandlerGraphClick);
        swingNode.setOnScroll(mouseHandlerGraphScroll);
        VBox graphBox = new VBox();
        graphBox.getChildren().addAll(new Rectangle(Main.columnWidthLEFT,0),swingNode);
        initializeNetworkVisualizationStyle();
        return graphBox;
    }
    
    public void initializeNetworkVisualizationStyle(){
        AppParameters.authorNetwork.addAttribute("event","no event");
        AppParameters.authorNetwork.addAttribute("ui.quality");
        AppParameters.authorNetwork.addAttribute("ui.antialias");
        String css = ""
            + "graph { fill-mode: plain; fill-color : #f4f4f4;}"
            + "edge  { fill-mode: plain; fill-color: rgba(0,0,0,85); size:0.25px; z-index: 1; arrow-shape: arrow; arrow-size: 4px, 2px;}" 
            + "node  { fill-mode: dyn-plain; fill-color: #4682B4,yellow,red ; size: 6px ;z-index : 2; stroke-mode: plain;}"
            + "node:clicked   { fill-color: black;}";
        AppParameters.authorNetwork.addAttribute("ui.stylesheet", css);
    }
    
    public final void initializeRankDistributionChart(){
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        xAxis.setLabel("Rank");
        xAxis.setTickLength(5);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        rankDistributionChart = new BarChart(xAxis,yAxis);
        rankDistributionChart.setLegendVisible(false);
        UIUtils.setSize(rankDistributionChart, Main.columnWidthRIGHT+5, 300);
        rankDistributionChart.setCategoryGap(0);
    }
    
    public final void updateRankDistributionChart(){
        Stop[] stops = new Stop[] { new Stop(0, Color.STEELBLUE), new Stop(0.5, Color.YELLOW), new Stop(1, Color.RED)};

        rankDistributionChart.getData().clear();
        int[] rankDistribution = selectedMethod.rankedUsers.extractRankDistribution();
        XYChart.Series series = new XYChart.Series();
        for(int i = 0; i < rankDistribution.length; i++){
            series.getData().add(new XYChart.Data(i+"",rankDistribution[i]));
        }
        rankDistributionChart.getData().add(series);
    }
    
    public final void updateAvailableMethods(){
        Reflections reflections = new Reflections("main.java.fr.ericlab.sondy.algo.influenceanalysis");    
        Set<Class<? extends InfluenceAnalysisMethod>> classes = reflections.getSubTypesOf(InfluenceAnalysisMethod.class);   
        for(Class<? extends InfluenceAnalysisMethod> aClass : classes){
            try {
                InfluenceAnalysisMethod method = (InfluenceAnalysisMethod) Class.forName(aClass.getName()).newInstance();
                methodList.getItems().add(method.getName());
                methodMap.put(method.getName(),aClass.getName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(EventDetectionUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public final void userMessages(String user){
        final Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Messages");
        
        TableView<Message> messageTable = new TableView<>();
        UIUtils.setSize(messageTable, Main.columnWidthLEFT, 360);
        
        TableColumn authorColumn = new TableColumn("Author");
        TableColumn timeColumn = new TableColumn("Timestamp");
        TableColumn textColumn = new TableColumn("Text");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        textColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        messageTable.getColumns().addAll(authorColumn,timeColumn,textColumn);
        messageTable.setItems(AppParameters.dataset.corpus.getMessages(user));
        
        Text tableText = new Text("User messages ("+messageTable.getItems().size()+")");
        Label topicLabel = new Label();
        topicLabel.setText(AppParameters.event.getTextualDescription());
        Date date = AppParameters.dataset.corpus.start;
        Instant fromInstant = date.toInstant();
        Instant toInstant = date.toInstant();
        String[] interval = AppParameters.event.getTemporalDescription().split(",");
        fromInstant = fromInstant.plusSeconds((long)(Double.parseDouble(interval[0])*24*60*60));
        toInstant = toInstant.plusSeconds((long)(Double.parseDouble(interval[1])*24*60*60));
        topicLabel.setText(AppParameters.event.getTextualDescription());
        Label intervalLabel = new Label();
        intervalLabel.setText("From "+fromInstant+" to "+toInstant);
        
        TextField filterMessagesField1 = new TextField("");
        filterMessagesField1.setPromptText("Type a list of words (separated by a space)");
        UIUtils.setSize(filterMessagesField1, 2*(Main.columnWidthLEFT-5)/3, 24);
        ChoiceBox operatorChoiceBox = new ChoiceBox();
        ObservableList<String> operatorList = FXCollections.observableArrayList();
        operatorList.add("and");
        operatorList.add("or");
        operatorChoiceBox.setItems(operatorList);
        operatorChoiceBox.getSelectionModel().select(0);
        UIUtils.setSize(operatorChoiceBox, (Main.columnWidthLEFT-5)/3, 24);
        HBox filterMessagesHBox = new HBox(5);
        filterMessagesHBox.getChildren().add(filterMessagesField1);
        filterMessagesHBox.getChildren().add(operatorChoiceBox);
        
        Button filterMessagesButton = new Button("Filter messages");
        UIUtils.setSize(filterMessagesButton, Main.columnWidthLEFT, 24);
        filterMessagesButton.setOnAction((ActionEvent ae) -> {
            String[] words = filterMessagesField1.getText().split(" ");
            ObservableList<Message> messages = FXCollections.observableArrayList();
            int operator = operatorChoiceBox.getSelectionModel().getSelectedIndex();
            for(Message message : messageTable.getItems()){
                String text = message.getText();
                short[] test = new short[words.length];
                for(int j = 0; j < words.length; j++){
                    if(StringUtils.containsIgnoreCase(text,words[j])){
                        test[j] = 1;
                    }else{
                        test[j] = 0;
                    }
                }
                int testSum = ArrayUtils.sum(test, 0, test.length-1);
                if(operator==0 && testSum == test.length){
                    messages.add(message);
                }
                if(operator==1 && testSum > 0){
                    messages.add(message);
                }
            }
            messageTable.getItems().clear();
            messageTable.setItems(messages);
            tableText.setText("User messages ("+messageTable.getItems().size()+")");
        });
        
        Scene scene = new Scene(VBoxBuilder.create().children(new Text("User"),new Separator(),new Label(user),tableText,new Separator(),messageTable,filterMessagesHBox,filterMessagesButton).alignment(Pos.CENTER).padding(new Insets(10)).spacing(3).build());
        scene.getStylesheets().add("resources/fr/ericlab/sondy/css/GlobalStyle.css");
        stage.setScene(scene);
        stage.show();
    }
    
}
