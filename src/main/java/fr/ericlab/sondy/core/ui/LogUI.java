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
import main.java.fr.ericlab.sondy.core.structures.LogEntry;
import main.java.fr.ericlab.sondy.core.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class LogUI {
    public GridPane logGrid;
    public static Label memoryLabel;
    public static TableView<LogEntry> logTable;
    public static ProgressBar progressBar;
    
    public LogUI(){
        // Initializing the main grid
        logGrid = new GridPane();
        logGrid.setPadding(new Insets(5, 5, 5, 5));
        
        // Adding separators
        logGrid.add(new Text("Log"),0,0);
        logGrid.add(new Separator(),0,1);
        
        // App status monitoring
        memoryLabel = new Label();
        UIUtils.setSize(memoryLabel, Main.columnWidthLEFT, 24);
        progressBar = new ProgressBar(0);
        UIUtils.setSize(progressBar, Main.columnWidthRIGHT, 12);
        HBox appStatusBox = new HBox(5);
        appStatusBox.setAlignment(Pos.CENTER);
        appStatusBox.getChildren().addAll(memoryLabel,progressBar);
        logGrid.add(appStatusBox,0,2);
        
        // Creating the log table
        logTable = new TableView<>();
        UIUtils.setSize(logTable,Main.windowWidth-10,150);
        TableColumn logTimeColumn = new TableColumn("Time");
        logTimeColumn.setMinWidth(90);
        logTimeColumn.setMaxWidth(90);
        TableColumn logDetailsColumn = new TableColumn("Log");
        logDetailsColumn.setMinWidth(Main.windowWidth-85);
        logTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        logDetailsColumn.setCellValueFactory(new PropertyValueFactory<>("info"));
        logTable.getColumns().addAll(logTimeColumn,logDetailsColumn);
        logGrid.add(logTable,0,3);
    }
    
    public static void addLogEntry(String message){
        LogEntry logEntry = new LogEntry(message);
        logTable.getItems().add(0, logEntry);
        updateMemoryLabel();
    }
    
    public static void updateMemoryLabel(){
        int total = Math.abs((int)Runtime.getRuntime().totalMemory()/(1024*1024));
        int max = Math.abs((int)Runtime.getRuntime().maxMemory()/(1024*1024));
        int free = Math.abs((int)Runtime.getRuntime().freeMemory()/(1024*1024));
        memoryLabel.setText("Maximum memory: "+max+"mb, total memory: "+total+"mb, free memory: "+free+"mb");
    }
}
