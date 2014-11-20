/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.ui;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.algo.eventdetection.EventDetectionAlgorithm;
import fr.ericlab.sondy.algo.eventdetection.MACD;
import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.DataManipulation;
import fr.ericlab.sondy.core.structure.TimelineEvent;
import fr.ericlab.sondy.core.structure.Timeline;
import fr.ericlab.sondy.core.access.DBAccess;
import fr.ericlab.sondy.core.access.IndexAccess;
import fr.ericlab.sondy.core.access.MentionIndexAccess;
import fr.ericlab.sondy.core.structure.DetectionResult;
import fr.ericlab.sondy.core.structure.list.DetectionResultList;
import fr.ericlab.sondy.core.structure.LogEntry;
import fr.ericlab.sondy.core.structure.Message;
import fr.ericlab.sondy.core.structure.SimpleTopic;
import fr.ericlab.sondy.ui.misc.ContextMenuTableCell;
import fr.ericlab.sondy.ui.misc.EditingCell;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.EllipseBuilder;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;
import org.apache.lucene.index.IndexReader;

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
 *   User interface of the topic and event detection detection service. 
 *   It allows identifying and temporally locating trending topics and events. 
 *   It encapsulates a set of configurable algorithms for trends detection 
 *   combined with results visualization under several customizable forms.
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class EventsUI {
    
    // Grids
    GridPane globalGrid;
    GridPane algoGrid;
    GridPane resultsGrid;
    
    // Separators
    Separator algoSeparator = new Separator();
    Separator resultsSeparator = new Separator();
    
    // Algorithm
    Text algoText = new Text("Algorithms");
    ListView<String> algoListView = new ListView<>();
    ObservableList<String> algoList = FXCollections.observableArrayList();
    EventDetectionAlgorithm algo;
    Text algoDescription = new Text("algorithm-description");
    TableView<AlgorithmParameter> algorithmParametersTable = new TableView<>();
    ObservableList<AlgorithmParameter> algorithmParameters = FXCollections.observableArrayList(new AlgorithmParameter("",""));
    HashMap<String,String> algoMap = new HashMap<>();
   
    // Results
    TextField filterTerm = new TextField();
    Text resultsText = new Text("Results");
    TableView<DetectionResult> resultsTableView = new TableView<>();
    DetectionResultList detectionResultList = new DetectionResultList();
    
    // - results list contextual menu
    MenuItem MACDMenuItem = new MenuItem("Compute MACD");
    MenuItem stopwordsMenuItem = new MenuItem("Mark as stopword");
    MenuItem compareMenuItem = new MenuItem("Compare");
    MenuItem mentionMenuItem = new MenuItem("View mention frequency");
    ContextMenu contextMenuList = new ContextMenu(MACDMenuItem,stopwordsMenuItem,compareMenuItem,mentionMenuItem);
    
    // - timeline
    Button extractTimelineButton = new Button("extract timeline");
    
    // Graph
    NumberAxis xAxis;
    NumberAxis yAxis;
    LineChart<Number,Number> chart;
    Rectangle rectangleSelection = new Rectangle(0,310);

    // - graph contextual menu
    MenuItem messagesMenuItem = new MenuItem("See messages");
    ContextMenu contextMenuChart = new ContextMenu(messagesMenuItem);
    
    // Global
    AppVariables appVariablesRef;
    DBAccess dbAccessRef;
    

    /**
     *
     * @param dbAccess
     * @param appVariables
     * @throws IOException
     */
    public EventsUI(DBAccess dbAccess,final AppVariables appVariables) throws IOException{
        appVariablesRef = appVariables;
        dbAccessRef = dbAccess;
        
        // Global
        globalGrid = new GridPane();
        globalGrid.setPadding(new Insets(5,5,5,5));
        globalGrid.setVgap(5);
        globalGrid.setHgap(5);
        globalGrid.setStyle("-fx-background-color: linear-gradient(#ffffff, #e5e5e5)");
        algoText.setId("title");
        resultsText.setId("title");
        GridPane.setConstraints(algoText, 0, 0);
        globalGrid.getChildren().add(algoText);
        GridPane.setConstraints(algoSeparator, 0, 1);
        globalGrid.getChildren().add(algoSeparator);
        
        // Algorithm
        ComponentScanner scanner = new ComponentScanner();
        Set<Class<?>> classes = scanner.getClasses(new ComponentQuery() {
            @Override
            protected void query() {
                select().from("fr.ericlab.sondy.algo.eventdetection").returning(allExtending(EventDetectionAlgorithm.class));
            }
        });
        for(Class<?> c : classes){
            try {
                EventDetectionAlgorithm a = (EventDetectionAlgorithm) Class.forName(c.getName()).newInstance();
                algoList.add(a.getName());
                algoMap.put(a.getName(),c.getName());
            } catch (    InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                Logger.getLogger(EventsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
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
        GridPane.setConstraints(algoListView, 0, 0);
        algoGrid.getChildren().add(algoListView);
        GridPane.setConstraints(algoGrid, 0, 2);
        globalGrid.getChildren().add(algoGrid);
        algoDescription.setId("smalltext");
        
        MACDMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                createMACDStage();
            }
        });
        
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
            new EventHandler<CellEditEvent<AlgorithmParameter, String>>() {
                @Override
                public void handle(CellEditEvent<AlgorithmParameter, String> t) {
                    ((AlgorithmParameter) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setValue(t.getNewValue());
                }
            }
        );
        algoValueColumn.setOnEditStart(
            new EventHandler<CellEditEvent<AlgorithmParameter, String>>() {
                @Override
                public void handle(CellEditEvent<AlgorithmParameter, String> t) {
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
        appVariablesRef.applyAlgoButtonDetection.setMinWidth(250);
        appVariablesRef.applyAlgoButtonDetection.setMaxWidth(250);
        appVariablesRef.applyAlgoButtonDetection.setDisable(true);
        
        GridPane.setConstraints(algorithmParametersTable, 1, 0);
        algoGrid.getChildren().add(algorithmParametersTable);
        GridPane.setConstraints(algoDescription, 0, 1);
        algoGrid.getChildren().add(algoDescription);
        GridPane.setConstraints(appVariablesRef.applyAlgoButtonDetection, 1, 1);
        algoGrid.getChildren().add(appVariablesRef.applyAlgoButtonDetection);
        
        algoListView.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<String>() {
            @Override
                public void changed(ObservableValue<? extends String> ov, 
                    String old_val, String new_val) {
                    if(new_val!=null && appVariablesRef.getCurrentDatasetDiscretization().length()!=0){
                        if(!appVariables.currentDatasetText.getText().equalsIgnoreCase("no dataset selected") && !appVariables.currentDatasetDiscretizationText.getText().equalsIgnoreCase("()")){
                            try {
                                algo = (EventDetectionAlgorithm) Class.forName(algoMap.get(new_val)).newInstance();
                                algorithmParametersTable.setItems(algo.parameters);
                                algoDescription.setText(algo.algoDescription);
                            } catch (                    InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                                Logger.getLogger(EventsUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                       
                    }else{
                        
                    }
                }
            }
        );
        
        appVariablesRef.applyAlgoButtonDetection.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            try {
                clear();
                appVariables.lastAppliedDetectionAlgo = algo.getName();
                algo.appVariables = appVariables;
                appVariablesRef.updateApplicationStatus(-1);
                disableInterface(true);
                final Task<String> waitingTask = new Task<String>() {
                    @Override
                    public String call() throws Exception {
                        Thread job = new Thread(algo);
                        job.start();
                        job.join();
                        return "" ;
                    }
                };
                waitingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        detectionResultList = new DetectionResultList(algo.getResults());
                        resultsTableView.setItems(detectionResultList.observableList);
                        appVariablesRef.updateApplicationStatus(0);
                        disableInterface(false);
                    }
                }); 
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(waitingTask);
            } catch (Exception ex) {
                Logger.getLogger(EventsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
        });
        
        // Results
        filterTerm.setPromptText("filter topics");
        final EventHandler<KeyEvent> enterPressed =
            new EventHandler<KeyEvent>() {
                @Override
                public void handle(final KeyEvent keyEvent) {
                    if(keyEvent.getCode()==KeyCode.ENTER){
                        resultsTableView.getItems().clear();
                        if(filterTerm.getText().length()>0){
                            detectionResultList.filterList(filterTerm.getText());
                            resultsTableView.setItems(detectionResultList.observableList);
                        }else{
                            detectionResultList.setFullList();
                            resultsTableView.setItems(detectionResultList.observableList);
                        }
                    }
                }
            };
        filterTerm.setOnKeyReleased(enterPressed);
        
        resultsTableView.setItems(detectionResultList.observableList);
        resultsTableView.setMinWidth(250);
        resultsTableView.setMaxWidth(250);
        resultsTableView.setMinHeight(300);
        resultsTableView.setMaxHeight(300);
        TableColumn resultMainTermColumn = new TableColumn("topic");
        resultMainTermColumn.setMinWidth(124);
        resultMainTermColumn.setMaxWidth(124);
        TableColumn resultInfoColumn = new TableColumn("time interval");
        resultInfoColumn.setMinWidth(125);
        resultMainTermColumn.setCellValueFactory(
                new PropertyValueFactory<DetectionResult, String>("mainTerm"));
        resultInfoColumn.setCellValueFactory(
                new PropertyValueFactory<DetectionResult, String>("info"));
        EventHandler<MouseEvent> clickResultsTable = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(t.getButton() == MouseButton.PRIMARY){
                    DetectionResult result = (DetectionResult) resultsTableView.getItems().get(((TableCell)t.getSource()).getIndex());
                    appVariablesRef.selectedTerm = result.getMainTerm();
                    appVariablesRef.selectedResult = result;
                    updateSimpleChart(appVariables);
                    String[] split = result.getInfo().split(";");
                    appVariablesRef.startDaySelection = Float.parseFloat(split[0]);
                    appVariablesRef.endDaySelection = Float.parseFloat(split[1]);
                    Node chartPlotArea;
                    float chartZeroX;
                    chartPlotArea = chart.lookup(".chart-plot-background");
                    chartZeroX = (float) chartPlotArea.getLayoutX();
                    float rectX = (Float.parseFloat(split[0])/(float)appVariablesRef.streamDuration)*508+chartZeroX+2;
                    float rectY = (Float.parseFloat(split[1])/(float)appVariablesRef.streamDuration)*508+chartZeroX+2;
                    rectangleSelection.setTranslateX(rectX);
                    rectangleSelection.setWidth(rectY-rectX);
                }
            }
        };
        ContextMenuTableCell tableCellFactory = new ContextMenuTableCell(clickResultsTable, contextMenuList);
        resultMainTermColumn.setCellFactory(tableCellFactory);
        resultsTableView.getColumns().addAll(resultMainTermColumn,resultInfoColumn);
        resultsTableView.setId("resultstable");
        
        resultsGrid = new GridPane();
        resultsGrid.setPadding(new Insets(0,5,0,5));
        resultsGrid.setVgap(5);
        resultsGrid.setHgap(5);
        
        stopwordsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                createStopwordsStage();
            }
        });
        
        compareMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                createCompareStage();
            }
        });
        
        mentionMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                updateMentionChart(appVariablesRef);
            }
        });
        
        // Timeline
        extractTimelineButton.setMinWidth(250);
        extractTimelineButton.setMaxWidth(250);
        VBox resultsTimelineBox = new VBox(5);
        resultsTimelineBox.getChildren().addAll(filterTerm,resultsTableView,extractTimelineButton);
        extractTimelineButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                createPreTimelineStage();
            }
        });
        
        // Graph
        xAxis = new NumberAxis(0,appVariablesRef.streamDuration,1);
        yAxis = new NumberAxis();
        xAxis.setLabel("time (days)");
        yAxis.setLabel("frequency");
        chart = new LineChart<>(xAxis,yAxis);
        chart.setLegendVisible(false);
        chart.setMaxSize(550, 350);
        chart.setMinSize(550, 350);
        
        rectangleSelection.setOpacity(0.33);
        rectangleSelection.setTranslateX(100);
        rectangleSelection.setTranslateY(-16);
        GridPane.setConstraints(chart, 0, 0);
        resultsGrid.getChildren().add(chart);
        GridPane.setConstraints(rectangleSelection, 0, 0);
        resultsGrid.getChildren().add(rectangleSelection);
        GridPane.setConstraints(resultsTimelineBox, 1, 0);
        resultsGrid.getChildren().add(resultsTimelineBox);
        
        GridPane.setConstraints(resultsText, 0, 3);
        globalGrid.getChildren().add(resultsText);
        GridPane.setConstraints(resultsSeparator, 0, 4);
        globalGrid.getChildren().add(resultsSeparator);
        
        GridPane.setConstraints(resultsGrid, 0, 5);
        globalGrid.getChildren().add(resultsGrid);
                
        EventHandler<MouseEvent> mouseHandlerChartSelection = new EventHandler<MouseEvent>() { 
            @Override 
            public void handle(MouseEvent mouseEvent) { 
                if(mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    if(mouseEvent.getButton() == MouseButton.SECONDARY){
                        
                    }else{
                        double clickRelativeX = mouseEvent.getX()-45;
                        double day = (double)((clickRelativeX/508)*(double)appVariables.streamDuration);
                        rectangleSelection.setX(mouseEvent.getX());
                        rectangleSelection.setWidth(0);
                        rectangleSelection.setTranslateX(mouseEvent.getX());
                        appVariables.startDaySelection = day;
                    }
                }
                if(mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) { 
                    double duration = Math.abs(rectangleSelection.getX()-mouseEvent.getX());
                    rectangleSelection.setWidth(duration);
                    appVariables.endDaySelection = appVariables.startDaySelection + (duration/503)*(double)appVariables.streamDuration;
                }
            }
        };    
        EventHandler<MouseEvent> mouseHandlerRectangleSelection = new EventHandler<MouseEvent>() { 
            @Override 
            public void handle(MouseEvent mouseEvent) { 
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    if(mouseEvent.getButton() == MouseButton.SECONDARY){
                        contextMenuChart.show(chart,mouseEvent.getScreenX(),mouseEvent.getScreenY());
                    }
                }
            }
        };    
        chart.setOnMousePressed(mouseHandlerChartSelection);
        chart.setOnMouseReleased(mouseHandlerChartSelection);
        chart.setOnMouseDragged(mouseHandlerChartSelection);
        rectangleSelection.setOnMousePressed(mouseHandlerRectangleSelection);
        messagesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                createMessagesStage();
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
    
    /**
     *
     * @param appVariables
     * @param lWindow
     * @param sWindow
     * @param exponent
     * @param smooth
     * @return
     */
    public float[] updateMACDChart(AppVariables appVariables, String lWindow, String sWindow, String exponent, String smooth){
        MACD macd = new MACD();
        IndexAccess indexAccess = new IndexAccess(appVariables);
        float[][] MACDResult = macd.apply(appVariables, indexAccess, appVariables.selectedTerm, Integer.parseInt(sWindow), Integer.parseInt(lWindow), (float)Float.parseFloat(exponent), Integer.parseInt(smooth));
        indexAccess.close();
        XYChart.Series seriesSmoothedMACD = new XYChart.Series();   
        seriesSmoothedMACD.setName(appVariables.selectedTerm);
        seriesSmoothedMACD.getData().clear();
        float intervalDuration = ((float) appVariables.getCurrentDatasetInterval())/60;
        for(int i = appVariables.startTimeSlice; i<appVariables.endTimeSlice;i++){
            float day = ((i)*intervalDuration)/24;
            seriesSmoothedMACD.getData().add(new XYChart.Data(day,MACDResult[4][i]));
        }
        if(!chart.getData().isEmpty()){
            chart.getData().remove(2, chart.getData().size());
        }
        chart.getData().add(seriesSmoothedMACD);
        float tab[] = new float[2];
        tab[0] = (MACDResult[5][0]*intervalDuration)/24;
        tab[1] = (MACDResult[5][1]*intervalDuration)/24;
        return tab;
    }
        
    /**
     *
     * @param appVariables
     */
    public void updateSimpleChart(AppVariables appVariables){
        if(appVariables.selectedTerm != null){
            String split[] = appVariables.selectedTerm.split(" ");
            rectangleSelection.setWidth(0);
            DataManipulation dataManipulation = new DataManipulation();
            xAxis.setUpperBound(appVariables.streamDuration);
            XYChart.Series seriesFreq = new XYChart.Series();
            seriesFreq.setName(split[0]);
            XYChart.Series seriesSmooth = new XYChart.Series();
            IndexAccess indexAccess = new IndexAccess(appVariables);
            IndexReader r = indexAccess.reader;
            int numDocs = r.numDocs();
            float[] termFrequency = indexAccess.getTermFrequency(appVariables,split[0]);
            float[] smoothedTermFrequency = dataManipulation.getSmoothedTermFrequency(termFrequency, numDocs/35);
            indexAccess.close();
            for(int i = appVariables.startTimeSlice; i<=appVariables.endTimeSlice ;i++){
                float day = ((i*((float)appVariables.intervalDurationMin))/60)/24;
                seriesFreq.getData().add(new XYChart.Data(day,termFrequency[i]));
                seriesSmooth.getData().add(new XYChart.Data(day,smoothedTermFrequency[i]));
            }
            if(!chart.getData().isEmpty()){
                chart.getData().remove(0, chart.getData().size());
            }
            chart.getData().add(seriesFreq);
            chart.getData().add(seriesSmooth);
        }else{
            System.out.println("updateSimpleChart: no term selected "+appVariables.selectedTerm);
        }
    }
    
    public void updateMentionChart(AppVariables appVariables){
        if(appVariables.selectedTerm != null){
            String split[] = appVariables.selectedTerm.split(" ");
            rectangleSelection.setWidth(0);
            DataManipulation dataManipulation = new DataManipulation();
            xAxis.setUpperBound(appVariables.streamDuration);
            XYChart.Series seriesFreq = new XYChart.Series();
            seriesFreq.setName(split[0]);
            XYChart.Series seriesSmooth = new XYChart.Series();
            MentionIndexAccess indexAccess = new MentionIndexAccess(appVariables);
            IndexReader r = indexAccess.mentionReader;
            int numDocs = r.numDocs();
            float[] termFrequency = indexAccess.getTermFrequency(appVariables,split[0]);
            float[] smoothedTermFrequency = dataManipulation.getSmoothedTermFrequency(termFrequency, numDocs/35);
            indexAccess.close();
            for(int i = appVariables.startTimeSlice; i<=appVariables.endTimeSlice ;i++){
                float day = ((i*((float)appVariables.intervalDurationMin))/60)/24;
                seriesFreq.getData().add(new XYChart.Data(day,termFrequency[i]));
                seriesSmooth.getData().add(new XYChart.Data(day,smoothedTermFrequency[i]));
            }
            if(!chart.getData().isEmpty()){
                chart.getData().remove(2, chart.getData().size());
            }
            chart.getData().add(seriesFreq);
            chart.getData().add(seriesSmooth);
        }
    }
    
    /**
     *
     * @param appVariables
     * @param compareToTerm
     */
    public void updateComparisonChart(AppVariables appVariables, String compareToTerm){
        if(compareToTerm != null){
            DataManipulation dataManipulation = new DataManipulation();
            xAxis.setUpperBound(appVariables.streamDuration);
            XYChart.Series seriesFreq = new XYChart.Series();
            seriesFreq.setName(compareToTerm);
            XYChart.Series seriesSmooth = new XYChart.Series();
            IndexAccess indexAccess = new IndexAccess(appVariables);
            IndexReader r = indexAccess.reader;
            int numDocs = r.numDocs();
            float[] termFrequency = indexAccess.getTermFrequency(appVariables, compareToTerm);
            float[] smoothedTermFrequency = dataManipulation.getSmoothedTermFrequency(termFrequency, numDocs/35);
            indexAccess.close();
            for(int i = appVariables.startTimeSlice; i<=appVariables.endTimeSlice ;i++){
                float day = ((i*((float)appVariables.intervalDurationMin))/60)/24;
                seriesFreq.getData().add(new XYChart.Data(day,termFrequency[i]));
                seriesSmooth.getData().add(new XYChart.Data(day,smoothedTermFrequency[i]));
            }
            if(!chart.getData().isEmpty()){
                chart.getData().remove(2, chart.getData().size());
            }
            chart.getData().add(seriesFreq);
            chart.getData().add(seriesSmooth);
        }
    }
    
    /**
     *
     */
    public void createMACDStage(){
        Label labelShortWindow = new Label("short window size: ");
        labelShortWindow.setMinHeight(20);
        labelShortWindow.setMinWidth(140);
        labelShortWindow.setMaxWidth(140);
        Label labelLongWindow = new Label("long window size: ");
        labelLongWindow.setMinHeight(20);
        labelLongWindow.setMinWidth(140);
        labelLongWindow.setMaxWidth(140);
        Label labelExponent = new Label("exponent: ");
        labelExponent.setMinHeight(20);
        labelExponent.setMinWidth(140);
        labelExponent.setMaxWidth(140);
        Label labelSmooth = new Label("smooth: ");
        labelSmooth.setMinHeight(20);
        labelExponent.setMinWidth(140);
        labelExponent.setMaxWidth(140);
        VBox boxMACDLabel = new VBox(5);
        boxMACDLabel.getChildren().addAll(labelShortWindow,labelLongWindow,labelExponent,labelSmooth);
        final TextField fieldShortWindow = new TextField();
        fieldShortWindow.setMaxHeight(20);
        fieldShortWindow.setMinWidth(140);
        fieldShortWindow.setMaxWidth(140);
        fieldShortWindow.setTooltip(new Tooltip("shortWindow>0"));
        final TextField fieldLongWindow = new TextField();
        fieldLongWindow.setMaxHeight(20);
        fieldLongWindow.setMinWidth(140);
        fieldLongWindow.setMaxWidth(140);
        fieldLongWindow.setTooltip(new Tooltip("longWindow>shortWindow"));
        final TextField fieldExponent = new TextField();
        fieldExponent.setMaxHeight(20);
        fieldExponent.setMinWidth(140);
        fieldExponent.setMaxWidth(140);
        fieldExponent.setTooltip(new Tooltip("0<exponent<=1"));
        final TextField fieldSmooth = new TextField();
        fieldSmooth.setTooltip(new Tooltip("smooth>=0"));
        fieldSmooth.setMaxHeight(20);
        fieldSmooth.setMinWidth(140);
        fieldSmooth.setMaxWidth(140);
        final RadioButton autoSelectButton = new RadioButton("Auto-detect trending period");
        autoSelectButton.setSelected(true);
        autoSelectButton.setMinWidth(200);
        VBox boxMACDField = new VBox(5);
        boxMACDField.getChildren().addAll(fieldShortWindow,fieldLongWindow,fieldExponent,fieldSmooth);
        Button computeMACDButton = new Button("Ok");
        computeMACDButton.setMinWidth(75);
        computeMACDButton.setMaxWidth(75);
        computeMACDButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
                final float[] tab = updateMACDChart(appVariablesRef,fieldShortWindow.getText(),fieldLongWindow.getText(),fieldExponent.getText(),fieldSmooth.getText());
                appVariablesRef.addLogEntry("[detection] computed MACD: short window="+fieldShortWindow.getText()+", long window="+fieldLongWindow.getText()+", exponent="+fieldExponent.getText()+", smooth="+fieldSmooth.getText());
                if(autoSelectButton.isSelected()){
                    float rectX = (tab[0]/(float)appVariablesRef.streamDuration)*503+45;
                    float rectY = (tab[1]/(float)appVariablesRef.streamDuration)*503+45;
                    rectangleSelection.setTranslateX(rectX);
                    rectangleSelection.setWidth(rectY-rectX);
                    appVariablesRef.startDaySelection = tab[0];
                    appVariablesRef.endDaySelection = tab[1];
                }
            }
        });
        HBox boxParameters = new HBox(5);
        HBox boxApply = new HBox(5);
        boxParameters.getChildren().addAll(boxMACDLabel,boxMACDField);
        boxApply.setAlignment(Pos.CENTER);
        boxApply.getChildren().addAll(autoSelectButton,computeMACDButton);
        final Stage dialogStageMACD = new Stage();
        dialogStageMACD.initStyle(StageStyle.UTILITY);
        dialogStageMACD.initModality(Modality.WINDOW_MODAL);
        dialogStageMACD.setScene(new Scene(VBoxBuilder.create().
            children(boxParameters,boxApply).
            alignment(Pos.CENTER).padding(new Insets(10)).spacing(5).build()));
        dialogStageMACD.setTitle("MACD parameters");
        dialogStageMACD.show();
        dialogStageMACD.getScene().getStylesheets().add("resources/css/Style.css");
    }
    
    /**
     *
     */
    public void createMessagesStage(){
        final int NB_TERMS = 5;
        Label topicLabel = new Label("topic: "+appVariablesRef.selectedTerm.split(" ")[0]);
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
        final ObservableList<Message> messagesList = FXCollections.observableArrayList(dbAccessRef.getMessages(appVariablesRef, 5000));
        TableView<Message> messagesTableView = new TableView<>();
        messagesTableView.setId("messagestable");
        messagesTableView.getColumns().addAll(timeColumn,authorColumn,textColumn);
        messagesTableView.setItems(messagesList);
        messagesTableView.setMinWidth(680);
        messagesTableView.setMaxWidth(680);
        messagesTableView.setMinHeight(200);
        messagesTableView.setMaxHeight(200);
        Button frequentTermsButton = new Button("extract frequent co-occurences");
        frequentTermsButton.setMinWidth(680);
        frequentTermsButton.setMaxWidth(680);
        HBox boxFrequentTerms = new HBox(5);
        boxFrequentTerms.setAlignment(Pos.CENTER);
        final TextField[] frequentTerms = new TextField[NB_TERMS];
        for(int i = 0; i < NB_TERMS; i++){
            frequentTerms[i] = new TextField();
            frequentTerms[i].setMaxWidth(130);
            frequentTerms[i].setMinWidth(130);
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
                String[] topTerms = dm.getFrequentCoocurringTerms(allMessages, NB_TERMS, appVariablesRef.selectedTerm.split(" ")[0], appVariablesRef);
                for(int j = 0; j < NB_TERMS; j++){
                    frequentTerms[j].setText(topTerms[j]);
                }
            }
        });
    }
    
    /**
     *
     */
    public void createPreTimelineStage(){
        Text textParametersMACD = new Text("MACD parameters");
        textParametersMACD.setFont(Font.font(null, FontWeight.BOLD, 12));
        Text textParametersTimeline = new Text("Timeline parameters");
        textParametersTimeline.setFont(Font.font(null, FontWeight.BOLD, 12));
        Label labelTimelineSize = new Label("number of events: ");
        labelTimelineSize.setMinWidth(140);
        labelTimelineSize.setMaxWidth(140);
        labelTimelineSize.setMaxHeight(20);
        final TextField fieldTimelineSize = new TextField();
        fieldTimelineSize.setMinWidth(140);
        fieldTimelineSize.setMaxWidth(140);
        fieldTimelineSize.setMaxHeight(20);
        Label labelExport = new Label("export timeline: ");
        labelExport.setMinWidth(140);
        labelExport.setMaxWidth(140);
        labelExport.setMinHeight(20);
        labelExport.setMaxHeight(20);
        Button buttonSaveFile = new Button("choose file");
        buttonSaveFile.setMinWidth(140);
        buttonSaveFile.setMaxWidth(140);
        buttonSaveFile.setMinHeight(20);
        buttonSaveFile.setMaxHeight(20);
        HBox boxSaveFile = new HBox(5);
        boxSaveFile.getChildren().addAll(labelExport,buttonSaveFile);
        HBox boxTimelineParameters = new HBox(5);
        boxTimelineParameters.getChildren().addAll(labelTimelineSize,fieldTimelineSize);
        Button generateTimelineButton = new Button("apply");
        generateTimelineButton.setMinWidth(285);
        generateTimelineButton.setMaxWidth(285);
        final Stage preTimelineStage = new Stage();
        Group preTimelineGroup = new Group();
        Scene preTimelineScene = new Scene(preTimelineGroup);
        preTimelineGroup.getChildren().addAll(VBoxBuilder.create().children(textParametersTimeline,new Separator(),boxTimelineParameters,boxSaveFile,generateTimelineButton).alignment(Pos.CENTER).padding(new Insets(10)).spacing(5).build());
        preTimelineStage.initStyle(StageStyle.UNDECORATED);
        preTimelineStage.initModality(Modality.WINDOW_MODAL);
        preTimelineStage.setScene(preTimelineScene);
        preTimelineStage.show();
        preTimelineStage.getScene().getStylesheets().add("resources/css/Style.css");
        buttonSaveFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(preTimelineStage);
                System.out.println(file.getAbsolutePath());
                if(file != null){
                    appVariablesRef.exportTimeline = file.getAbsolutePath();
                }
            }
        });
        generateTimelineButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int numEvents = Integer.parseInt(fieldTimelineSize.getText());
                createTimelineStage(numEvents);
                preTimelineStage.close();
            }
        });
    }
    
    /**
     *
     * @param numEvents
     */
    public void createTimelineStage(int numEvents){
        Timeline timeline = new Timeline(resultsTableView.getItems(), numEvents, appVariablesRef);
        HashMap<Integer,Integer> map = new HashMap<>();
        if(appVariablesRef.exportTimeline.equals("no export")){
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            moveTo.setX(0);
            moveTo.setY(0);
            LineTo lineTo = new LineTo();
            lineTo.setX(680);
            lineTo.setY(0);
            path.getElements().add(moveTo);
            path.getElements().add(lineTo);
            path.setStrokeWidth(4);
            path.setStroke(Color.STEELBLUE);
            path.setTranslateY(30);
            path.setTranslateX(10);
            Group groupTimeline = new Group();
            groupTimeline.getChildren().add(path);
            for(int i = 0; i<numEvents && i<timeline.eventsList.size(); i++){
                TimelineEvent e = timeline.eventsList.get(i);
                SimpleTopic topic = (SimpleTopic) e.topic;
                String eventDescription = "[topic: "+topic.mainTerm+"]";
                int nbTerms = 4;
                for(String term : topic.termsList){
                    nbTerms--;
                    eventDescription += "\n"+term;
                    if(nbTerms == 0){
                        break;
                    }
                }
                eventDescription += "\n["+e.pattern.startDay+"->"+e.pattern.endDay+"]";
                Tooltip tooltip = new Tooltip(eventDescription);
                tooltip.getStyleClass().add("ttip");
//                Rectangle r = new Rectangle();
//                r.setHeight(12);
//                r.setArcWidth(0);
//                r.setArcHeight(0);
//                r.setStrokeWidth(2);
//                r.setStroke(Color.web("#f2f2f2"));
//                r.setFill(Color.STEELBLUE);
                Ellipse circle = EllipseBuilder.create()
                .centerX(0)
                .centerY(0)
                .radiusX(6)
                .radiusY(6)
                .strokeWidth(3)
                .stroke(Color.web("#f2f2f2"))
                .fill(Color.STEELBLUE)
                .build();
               int yPos = 30;
               int xPos = (int) ((e.pattern.startDay/(float)appVariablesRef.streamDuration)*680+10);
               int xPos0 = (int) ((e.pattern.endDay/(float)appVariablesRef.streamDuration)*680+10);
               if(map.get(xPos) == null){
                   map.put(xPos, 1);
               }else{
                   int count = map.get(xPos);
                   yPos += 12*count;
                   map.put(xPos, count+1);
               }
//               r.setX(xPos);
//               r.setY(yPos);
//               r.setWidth(xPos0-xPos);
//               tooltip.getStyleClass().add("ttip");
//               Tooltip.install(r, tooltip);
//               groupTimeline.getChildren().add(r);
               circle.setTranslateY(yPos);
               circle.setTranslateX(xPos);
               tooltip.getStyleClass().add("ttip");
               Tooltip.install(circle, tooltip);
               groupTimeline.getChildren().add(circle);
            }
            Label startTime = new Label(appVariablesRef.getStartDay().toString());
            startTime.setFont(new Font(null,10));
            startTime.setTranslateY(5);
            startTime.setTranslateX(10);
            Label endTime = new Label(appVariablesRef.getEndDay().toString());
            endTime.setFont(new Font(null,10));
            endTime.setTranslateY(5);
            endTime.setTranslateX(575);
            groupTimeline.getChildren().addAll(startTime,endTime);
            Scene sceneTimeline = new Scene(groupTimeline, 805, 150, Color.web("#f2f2f2"));
            sceneTimeline.getStylesheets().add("resources/css/Timeline.css");
            final Stage dialogStageTimeline = new Stage();
            dialogStageTimeline.initStyle(StageStyle.UTILITY);
            dialogStageTimeline.initModality(Modality.WINDOW_MODAL);
            dialogStageTimeline.setScene(sceneTimeline);
            dialogStageTimeline.setTitle("Timeline");
            dialogStageTimeline.show();
        }
        appVariablesRef.exportTimeline = "no export";
    }
    
    /**
     *
     */
    public void createStopwordsStage(){
        Text stopwordText = new Text("stopword: "+appVariablesRef.selectedTerm);
        stopwordText.setFont(Font.font(null, FontWeight.BOLD, 12));
        Label existingSetLabel = new Label("add to existing set: ");
        existingSetLabel.setMinHeight(20);
        existingSetLabel.setMaxHeight(20);
        existingSetLabel.setMinWidth(140);
        existingSetLabel.setMaxWidth(140);
        Label newSetLabel = new Label("or create new set: ");
        newSetLabel.setMinHeight(20);
        newSetLabel.setMaxHeight(20);
        newSetLabel.setMinWidth(140);
        newSetLabel.setMaxWidth(140);
        ComboBox existingSetsBox = new ComboBox();
        existingSetsBox.setItems(appVariablesRef.availableStopWords);
        existingSetsBox.setMinHeight(20);
        existingSetsBox.setMaxHeight(20);
        existingSetsBox.setMaxWidth(140);
        existingSetsBox.setMinWidth(140);
        final TextField newSetField = new TextField();
        newSetField.setPromptText("enter a name");
        newSetField.setMinHeight(20);
        newSetField.setMaxHeight(20);
        newSetField.setMinWidth(140);
        newSetField.setMaxWidth(140);
        Button addStopwordButton = new Button("Ok");
        addStopwordButton.setMinWidth(285);
        addStopwordButton.setMaxWidth(285);
        final Stage stopwordsStage = new Stage();
        existingSetsBox.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<String>() {
            @Override
                public void changed(ObservableValue<? extends String> ov, 
                    String old_val, String new_val) {
                    newSetField.setText(new_val.replace("stopwords: ",""));
                }
            }
        );
        addStopwordButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DataManipulation dm = new DataManipulation();
                dm.updateStopwords(appVariablesRef, newSetField.getText(), appVariablesRef.selectedTerm);
                stopwordsStage.close();
            }
        });
        VBox boxLabels = new VBox(5);
        boxLabels.getChildren().addAll(existingSetLabel,newSetLabel);
        VBox optionsBox = new VBox(5);
        optionsBox.getChildren().addAll(existingSetsBox,newSetField);
        HBox globalStopwordsBox = new HBox(5);
        globalStopwordsBox.getChildren().addAll(boxLabels,optionsBox);
        stopwordsStage.initStyle(StageStyle.UTILITY);
        stopwordsStage.initModality(Modality.WINDOW_MODAL);
        stopwordsStage.setScene(new Scene(VBoxBuilder.create().
            children(stopwordText,globalStopwordsBox,addStopwordButton).
            alignment(Pos.CENTER).padding(new Insets(10)).spacing(5).build()));
        stopwordsStage.setTitle("Add a stopword");
        stopwordsStage.getScene().getStylesheets().add("resources/css/Style.css");
        stopwordsStage.show();
    }
    
    /**
     *
     */
    public void createCompareStage(){
        Label compareLabel = new Label("compare with: ");
        compareLabel.setMinWidth(140);
        compareLabel.setMaxWidth(140);
        compareLabel.setMaxHeight(20);
        final TextField compareField = new TextField();
        compareField.setMinWidth(140);
        compareField.setMaxWidth(140);
        compareField.setMaxHeight(20);
        HBox compareBox = new HBox(5);
        compareBox.getChildren().addAll(compareLabel,compareField);
        Button compareButton = new Button("apply");
        compareButton.setMinWidth(285);
        compareButton.setMaxWidth(285);
        compareButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                updateComparisonChart(appVariablesRef, compareField.getText());
            }
        });
        final Stage compareStage = new Stage();
        compareStage.initStyle(StageStyle.UTILITY);
        compareStage.initModality(Modality.WINDOW_MODAL);
        compareStage.setScene(new Scene(VBoxBuilder.create().
            children(compareBox,compareButton).
            alignment(Pos.CENTER).padding(new Insets(10)).spacing(5).build()));
        compareStage.setTitle("Compare");
        compareStage.getScene().getStylesheets().add("resources/css/Style.css");
        compareStage.show();
    }
    
    /**
     *
     */
    public void updatePattern(){
        float maxX = 0, maxY = 0;
        float startValue = 0, endValue = 0;
        boolean onStartValue = true;
        for(Data<Number, Number> data : chart.getData().get(1).getData()){
            if(data.getXValue().floatValue()>=appVariablesRef.startDaySelection && data.getXValue().floatValue()<=appVariablesRef.endDaySelection){
                if(onStartValue){
                    startValue = data.getYValue().floatValue();
                    onStartValue = false;
                }
                if(data.getYValue().floatValue()>maxY){
                    maxY = data.getYValue().floatValue();
                    maxX = data.getXValue().floatValue();
                }
                endValue = data.getYValue().floatValue();
            }
        }
        XYChart.Series seriesPattern = new XYChart.Series();
        seriesPattern.getData().addAll(new XYChart.Data(appVariablesRef.startDaySelection,startValue),new XYChart.Data(maxX,maxY),new XYChart.Data(appVariablesRef.endDaySelection,endValue));
        chart.getData().add(seriesPattern);
    }
       
    /**
     *
     */
    public void clear(){
        rectangleSelection.setWidth(0);
        chart.getData().clear();
        resultsTableView.setItems(null);
        detectionResultList.list.clear();
        detectionResultList.observableList.clear();
    }
    
    public void disableInterface(boolean b){
        appVariablesRef.disablePane(b);
    }
}
