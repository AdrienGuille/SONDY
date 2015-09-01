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
package main.java.fr.ericlab.sondy.core.app;

import com.sun.javafx.css.StyleManager;
import main.java.fr.ericlab.sondy.core.structures.DocumentTermFrequencyItem;
import main.java.fr.ericlab.sondy.core.structures.DocumentTermMatrix;
import main.java.fr.ericlab.sondy.core.ui.GlobalUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.control.PropertySheet;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class Main extends Application {
    
    public static final int windowHeight = 710;
    public static final int columnWidthRIGHT = 250;
    public static final int columnWidthLEFT = 600;
    public static final int windowWidth = columnWidthRIGHT+columnWidthLEFT+15;
    public static final String version = "1.0 (alpha)";
    
    @Override
    public void start(Stage primaryStage){
        Locale.setDefault(Locale.US);
        Configuration.initialize();
        
        GlobalUI globalUI = new GlobalUI();
        Scene scene = new Scene(globalUI.globalGridPane,windowWidth,windowHeight);
        scene.getStylesheets().add("resources/fr/ericlab/sondy/css/GlobalStyle.css");
        primaryStage.setTitle("SONDY "+version);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
 
    public static void main(String[] args)
    {
        StyleManager.getInstance().addUserAgentStylesheet(PropertySheet.class.getResource("propertysheet.css").toExternalForm());
        launch(args);
    }
}

