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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.Date;

import com.google.common.base.Strings;
import main.java.fr.ericlab.sondy.algo.eventdetection.EventDetectionMethod;
import main.java.fr.ericlab.sondy.core.app.AppParameters;
import main.java.fr.ericlab.sondy.core.app.Main;
import main.java.fr.ericlab.sondy.core.structures.Event;
import main.java.fr.ericlab.sondy.core.structures.Events;
import main.java.fr.ericlab.sondy.algo.Parameter;
import main.java.fr.ericlab.sondy.core.text.index.CalculationType;
import main.java.fr.ericlab.sondy.core.ui.factories.EventTableContextMenu;
import main.java.fr.ericlab.sondy.core.utils.ArrayUtils;
import main.java.fr.ericlab.sondy.core.utils.UIUtils;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.java.fr.ericlab.sondy.core.structures.Message;
import org.reflections.Reflections;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class EventDetectionUI {
    public GridPane grid;
    
    // Methods
    ListView<String> methodList;
    // - parameters
    TableView<Parameter> parameterTable;
    HashMap<String,String> methodMap;
    // - apply
    Label methodDescriptionLabel;
    Button applyButton;
    EventDetectionMethod selectedMethod;
    
    // Detected events
    // - filter
    TextField filterEventsField;
    // - list
    TableView<Event> eventTable = new TableView<>();
    Events eventList = new Events();
    // - frequency chart
    NumberAxis xAxis;
    NumberAxis yAxis;
    LineChart<Number,Number> frequencyChart;
    int maxNumberOfCurves = 3;
    Rectangle rectangleSelection;

    // Saved events
    String directory;
    
    public EventDetectionUI(){
        // Initializing the main grid
        grid = new GridPane();
        grid.setPadding(new Insets(5, 5, 5, 5));
        
        // Adding separators
        grid.add(new Text("Available methods"), 0, 0);
        grid.add(new Separator(), 0, 1);
        grid.add(new Text("Detected events"), 0, 3);
        grid.add(new Separator(),0,4);

        directory = "";

        availabeMethodsUI();
        detectedEventsUI();        
    }

    public final void availabeMethodsUI(){
        initializeAvailableMethodList();
        methodDescriptionLabel = new Label("Selected method description");
        methodDescriptionLabel.setId("smalltext");
        UIUtils.setSize(methodDescriptionLabel,Main.columnWidthLEFT,24);
        VBox methodsLEFT = new VBox();
        methodsLEFT.getChildren().addAll(methodList, new Rectangle(0, 3), methodDescriptionLabel);
        // Right part
        applyButton = createApplyMethodButton();
        UIUtils.setSize(applyButton, Main.columnWidthRIGHT, 24);
        parameterTable = new TableView<>();
        UIUtils.setSize(parameterTable, Main.columnWidthRIGHT, 64);
        initializeParameterTable();
        VBox methodsRIGHT = new VBox();
        methodsRIGHT.getChildren().addAll(parameterTable, new Rectangle(0, 3), applyButton);
        // Both parts
        HBox methodsBOTH = new HBox(5);
        methodsBOTH.getChildren().addAll(methodsLEFT, methodsRIGHT);
        grid.add(methodsBOTH, 0, 2);
    }

    public final void detectedEventsUI(){
        // Detected events
        HBox detectedEventsBOTH = new HBox(0);
        initializeEventTable();
        initializeFrequencyChart();
        VBox detectedEventsRIGHT = new VBox(3);
        filterEventsField = new TextField();
        filterEventsField.setPromptText("Filter events");
        UIUtils.setSize(filterEventsField, Main.columnWidthRIGHT, 24);
        final EventHandler<KeyEvent> enterPressed =
            new EventHandler<KeyEvent>() {
                @Override
                public void handle(final KeyEvent keyEvent) {
                    if(keyEvent.getCode()==KeyCode.ENTER){
                        eventTable.getItems().clear();
                        selectedMethod.events.filterList(filterEventsField.getText());
                        eventTable.setItems(selectedMethod.events.observableList);
                    }
                }
            };
        filterEventsField.setOnKeyReleased(enterPressed);
        detectedEventsRIGHT.getChildren().addAll(filterEventsField,eventTable,createTimelineButton());
        detectedEventsBOTH.getChildren().addAll(frequencyChart, detectedEventsRIGHT);
        grid.add(detectedEventsBOTH, 0, 5);
        rectangleSelection = new Rectangle(0,240);
        rectangleSelection.setOpacity(0.22);
        rectangleSelection.setTranslateY(-28);
        grid.add(rectangleSelection,0,5);
    }
    
    public final void initializeAvailableMethodList(){
        methodMap = new HashMap<>();
        methodList = new ListView<>();
        methodList.setOrientation(Orientation.HORIZONTAL);
        UIUtils.setSize(methodList,Main.columnWidthLEFT,64);
        updateAvailableMethods();
        methodList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
            try {
                selectedMethod = (EventDetectionMethod) Class.forName(methodMap.get(new_val)).newInstance();
                methodDescriptionLabel.setText(selectedMethod.getDescription());
                parameterTable.setItems(selectedMethod.parameters.list);
                if (AppParameters.dataset.path != null && !Strings.isNullOrEmpty(AppParameters.dataset.corpus.preprocessing)) {
                    directory = AppParameters.dataset.path + File.separator + AppParameters.dataset.corpus.preprocessing + File.separator + "events" + File.separator + selectedMethod.getName();
                    File dir = new File(directory);
                    if (!dir.exists())
                        dir.mkdirs();
                    checkLoadResults();
                }
                else {
                    directory = "";
                }
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
            new EventHandler<CellEditEvent<Parameter, String>>() {
                @Override
                public void handle(CellEditEvent<Parameter, String> t) {
                    ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow())).setValue(t.getNewValue());
                    checkLoadResults();
                }
            }
        );
        parameterTable.getColumns().addAll(keyColumn, valueColumn);
    }
    
    public final void initializeEventTable(){
        eventTable = new TableView<>();
        eventTable.setItems(eventList.observableList);
        UIUtils.setSize(eventTable, Main.columnWidthRIGHT, 247);
        TableColumn textualDescription = new TableColumn("Textual desc.");
        textualDescription.setMinWidth(Main.columnWidthRIGHT * 0.35);
        TableColumn temporalDescription = new TableColumn("Temporal desc.");
        temporalDescription.setMinWidth(Main.columnWidthRIGHT * 0.35);
        TableColumn eventScore = new TableColumn("Score");
        eventScore.setMinWidth(Main.columnWidthRIGHT * 0.3);
        textualDescription.setCellValueFactory(new PropertyValueFactory<>("textualDescription"));
        temporalDescription.setCellValueFactory(new PropertyValueFactory<>("temporalDescription"));
        eventScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        EventTableContextMenu tableCellFactory = new EventTableContextMenu(createSelectedEventHandler(), new ContextMenu());
        textualDescription.setCellFactory(tableCellFactory);
        eventTable.getColumns().addAll(textualDescription,temporalDescription,eventScore);
    }
    
    public final Button createApplyMethodButton(){
        Button button = new Button("Apply method");
        button.setOnAction((ActionEvent ae) -> {
            try {
                if(!AppParameters.dataset.corpus.preprocessing.equals("")){
                    AppParameters.disableUI(true);
                    eventTable.getItems().clear();
                    frequencyChart.getData().clear();
                    rectangleSelection.setWidth(0);
                    LogUI.addLogEntry("Running '"+selectedMethod.getName()+"'...");
                    final Task<String> waitingTask = new Task<String>() {
                        @Override
                        public String call() throws Exception {
                            Thread job = new Thread(selectedMethod);
                            job.start();
                            job.join();
                            eventList = selectedMethod.events;
                            return "" ;
                        }
                    };
                    waitingTask.setOnSucceeded((WorkerStateEvent event1) -> {
                        eventTable.getItems().clear();
                        eventTable.getItems().addAll(eventList.observableList);
                        AppParameters.disableUI(false);
                        LogUI.addLogEntry("Done: "+selectedMethod.getLog());
                    }); 
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(waitingTask);
                }else{
                    LogUI.addLogEntry("Error: no dataset loaded");
                }
            } catch (Exception ex) {
                Logger.getLogger(EventDetectionUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return button;
    }
    
    public final void initializeFrequencyChart(){
        xAxis = new NumberAxis(0,1,1);
        yAxis = new NumberAxis();
        xAxis.setTickLength(5);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        xAxis.setLabel("Time (days)");
        frequencyChart = new LineChart<>(xAxis,yAxis);
        frequencyChart.setLegendVisible(true);
        frequencyChart.setCreateSymbols(false);
        frequencyChart.setTranslateX(-5);
        UIUtils.setSize(frequencyChart, Main.columnWidthLEFT + 5, 300);
    }
    
    public final Button createTimelineButton(){
        Button button = new Button("Generate timeline");
        UIUtils.setSize(button, Main.columnWidthRIGHT, 24);
        button.setOnAction((ActionEvent ae) -> {
            createTimelineStage();
        });
        return button;
    }
    
    public final void updateAvailableMethods(){
        Reflections reflections = new Reflections("main.java.fr.ericlab.sondy.algo.eventdetection");    
        Set<Class<? extends EventDetectionMethod>> classes = reflections.getSubTypesOf(EventDetectionMethod.class);   
        for(Class<? extends EventDetectionMethod> aClass : classes){
            try {
                EventDetectionMethod method = (EventDetectionMethod) Class.forName(aClass.getName()).newInstance();
                methodList.getItems().add(method.getName());
                methodMap.put(method.getName(),aClass.getName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(EventDetectionUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public final void updateFrequencyChart(){
        xAxis.setTickUnit(0.5);
        xAxis.setUpperBound(AppParameters.dataset.corpus.getLength());
        frequencyChart.getData().clear();
        String[] terms = AppParameters.event.getTextualDescription().split(" ");
        for(int j = 0; j < maxNumberOfCurves && j < terms.length; j++){
            Short[] frequency = AppParameters.dataset.corpus.getTermFrequency(CalculationType.Frequency, terms[j]);
            int windowSize = AppParameters.dataset.corpus.getMessageDistribution(CalculationType.Frequency).length/Main.columnWidthLEFT;
            float[] smoothedFrequency;
            if(windowSize > 1){
                smoothedFrequency = ArrayUtils.smoothArray(frequency, windowSize);    
            }else{
                smoothedFrequency = ArrayUtils.toFloatArray(frequency);
            }
            XYChart.Series series = new XYChart.Series();
            series.setName(terms[j]);
            for(int i = AppParameters.timeSliceA; i < AppParameters.timeSliceB; i++){
                double x = AppParameters.dataset.corpus.convertTimeSliceToDay(i);
                double y = smoothedFrequency[i];
                series.getData().add(new XYChart.Data(x,y));
            }
            frequencyChart.getData().add(series);
        }
        String[] interval = AppParameters.event.getTemporalDescription().split(",");
        double rectX = (Double.parseDouble(interval[0])/AppParameters.dataset.corpus.getLength())*(Main.columnWidthLEFT-5)+3;
        double rectY = (Double.parseDouble(interval[1])/AppParameters.dataset.corpus.getLength())*(Main.columnWidthLEFT-5)+3;
        rectangleSelection.setTranslateX(rectX);
        rectangleSelection.setWidth(rectY-rectX);
    }
    
    public final EventHandler<MouseEvent> createSelectedEventHandler(){
        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                AppParameters.event = (Event) eventTable.getItems().get(((TableCell)t.getSource()).getIndex());
                updateFrequencyChart();
                if(t.getButton() == MouseButton.SECONDARY){
                    eventRelatedMessages();
                }
            }
        };
        return eventHandler;
    }
    
    public final void eventRelatedMessages(){
        final Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Messages");
        
        TableView<Message> messageTable = new TableView<>();
        UIUtils.setSize(messageTable, Main.columnWidthLEFT, 450);

        TableColumn inEventColumn = new TableColumn("In Event");
        TableColumn authorColumn = new TableColumn("Author");
        TableColumn timeColumn = new TableColumn("Timestamp");
        TableColumn textColumn = new TableColumn("Text");
        inEventColumn.setCellValueFactory(new PropertyValueFactory<>("inEvent"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        textColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        messageTable.getColumns().addAll(inEventColumn,authorColumn,timeColumn,textColumn);
        messageTable.setItems(AppParameters.dataset.corpus.getMessages(AppParameters.event));
        
        Text tableText = new Text("Related messages ("+messageTable.getItems().size()+")");
        
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
        UIUtils.setSize(operatorChoiceBox, (Main.columnWidthLEFT - 5) / 3, 24);
        HBox filterMessagesHBox = new HBox(5);
        filterMessagesHBox.getChildren().add(filterMessagesField1);
        filterMessagesHBox.getChildren().add(operatorChoiceBox);
        
        Button filterMessagesButton = new Button("Filter messages");
        UIUtils.setSize(filterMessagesButton, Main.columnWidthLEFT, 24);
        filterMessagesButton.setOnAction((ActionEvent ae) -> {
            messageTable.getItems().clear();
            String[] words = filterMessagesField1.getText().split(" ");
            messageTable.setItems(AppParameters.dataset.corpus.getFilteredMessages(AppParameters.event, words, operatorChoiceBox.getSelectionModel().getSelectedIndex()));
            tableText.setText("Related messages (" + messageTable.getItems().size() + ")");
        });
        VBox box = new VBox();
        box.getChildren().addAll(new Text("Topic"),new Separator(),topicLabel,new Text("Time interval"),new Separator(),intervalLabel,tableText,new Separator(),messageTable,filterMessagesHBox,filterMessagesButton);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setSpacing(3);
        Scene scene = new Scene(box);
        scene.getStylesheets().add("resources/fr/ericlab/sondy/css/GlobalStyle.css");
        stage.setScene(scene);
        stage.show();
    }
    
    public void createTimelineStage(){
        HashMap<Integer,Integer> map = new HashMap<>();
        Path path = new Path();
        MoveTo moveTo = new MoveTo();
        moveTo.setX(0);
        moveTo.setY(0);
        LineTo lineTo = new LineTo();
        lineTo.setX(Main.columnWidthLEFT);
        lineTo.setY(0);
        path.getElements().add(moveTo);
        path.getElements().add(lineTo);
        path.setStrokeWidth(4);
        path.setStroke(Color.STEELBLUE);
        path.setTranslateY(30);
        path.setTranslateX(10);
        VBox timelineBox = new VBox(5);
        Group groupTimeline = new Group();
        groupTimeline.getChildren().add(path);
        for(Event event : selectedMethod.events.observableList){
            Date date = AppParameters.dataset.corpus.start;
            Instant fromInstant = date.toInstant();
            Instant toInstant = date.toInstant();
            String[] interval = event.getTemporalDescription().split(",");
            fromInstant = fromInstant.plusSeconds((long)(Double.parseDouble(interval[0])*24*60*60));
            toInstant = toInstant.plusSeconds((long)(Double.parseDouble(interval[1])*24*60*60));
            Tooltip tooltip = new Tooltip("topic: "+event.getTextualDescription()+"\ntime interval: from "+fromInstant+" to "+toInstant);
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
            int xPos = (int) ((Double.parseDouble(interval[0])/AppParameters.dataset.corpus.getLength())*Main.columnWidthLEFT+10);
            if(map.get(xPos) == null){
                 map.put(xPos, 1);
            }else{
                 int count = map.get(xPos);
                 yPos += 12*count;
                 map.put(xPos, count+1);
            }
            circle.setTranslateY(yPos);
            circle.setTranslateX(xPos);
            Tooltip.install(circle, tooltip);
            groupTimeline.getChildren().add(circle);
        }
        Label startTime = new Label(AppParameters.dataset.corpus.start.toString());
        startTime.setFont(new Font(null,10));
        startTime.setTranslateY(5);
        startTime.setTranslateX(10);
        Label endTime = new Label(AppParameters.dataset.corpus.end.toString());
        endTime.setFont(new Font(null,10));
        endTime.setTranslateY(5);
        endTime.setTranslateX(Main.columnWidthLEFT-145);
        groupTimeline.getChildren().addAll(startTime,endTime);
        final Stage dialogStageTimeline = new Stage();

        Button closeButton = new Button("Close");
        UIUtils.setSize(closeButton, Main.columnWidthLEFT, 24);
        closeButton.setOnAction((ActionEvent ae) -> {
            dialogStageTimeline.close();
        });
        
        timelineBox.getChildren().addAll(groupTimeline,closeButton);
        Scene sceneTimeline = new Scene(VBoxBuilder.create().children(groupTimeline,closeButton).alignment(Pos.CENTER).padding(new Insets(10)).spacing(3).build());
        sceneTimeline.getStylesheets().add("resources/fr/ericlab/sondy/css/GlobalStyle.css");
        dialogStageTimeline.initStyle(StageStyle.UNDECORATED);
        dialogStageTimeline.initModality(Modality.APPLICATION_MODAL);
        dialogStageTimeline.setScene(sceneTimeline);
        dialogStageTimeline.show();
    }


    private void checkLoadResults() {
        if (directory != "") {
            File fiEventLaunch = new File(directory + File.separator + selectedMethod.getUniqueParameterHash() + ".dat");
            if (fiEventLaunch.exists()) {
                AppParameters.disableUI(true);
                eventTable.getItems().clear();
                frequencyChart.getData().clear();
                rectangleSelection.setWidth(0);
                LogUI.addLogEntry("Loading '" + selectedMethod.getName() + "'...");
                final Task<String> waitingTask = new Task<String>() {
                    @Override
                    public String call() throws Exception {
                        try {
                            FileInputStream fisEvents = new FileInputStream(fiEventLaunch);
                            ObjectInputStream oisEvents = new ObjectInputStream(fisEvents);
                            Object obj = oisEvents.readObject();
                            oisEvents.close();
                            EventDetectionMethod oldResults = (EventDetectionMethod) obj;

                            selectedMethod.events.list = oldResults.events.list;
                            selectedMethod.events.setFullList();
                            eventList = selectedMethod.events;
                            return "Loaded from previous: " + selectedMethod.getLog();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        return "" ;
                    }
                };
                waitingTask.setOnSucceeded((WorkerStateEvent event1) -> {
                    eventTable.getItems().clear();
                    eventTable.getItems().addAll(eventList.observableList);
                    AppParameters.disableUI(false);
                    LogUI.addLogEntry(waitingTask.getValue());
                });
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(waitingTask);
            }
            else {
                eventTable.getItems().clear();
                frequencyChart.getData().clear();
                rectangleSelection.setWidth(0);
            }
        }
    }
}