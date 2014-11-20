/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.app;

import fr.ericlab.sondy.core.structure.Configuration;
import fr.ericlab.sondy.ui.GlobalUI;
import fr.ericlab.sondy.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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
 *   Main class of the application.
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class Main extends Application {
    
    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        launch(args);
    }
     
    /**
     *
     * @param primaryStage
     * @throws IOException
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        Font.loadFont(Main.class.getResource("/resources/fonts/DroidSans.ttf").toExternalForm(), 13);
        Font.loadFont(Main.class.getResource("/resources/fonts/OpenSans-Bold.ttf").toExternalForm(), 13);
        Locale.setDefault(Locale.US);
        Configuration configuration = new Configuration();
        configuration.readConfiguration();
        File dir = new File(configuration.getWorkspace()+"/algorithms/");
        if(!dir.exists()){
            dir.mkdirs();
        }
        try {
            Utils.addPathToClassPath(configuration.getWorkspace()+"/algorithms/");
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }    
        GlobalUI globalInterface = new GlobalUI(configuration);
        final Group root = new Group();
        final Scene scene = new Scene(root,825,755);
        scene.getStylesheets().addAll("resources/css/Chart.css","resources/css/Style.css","resources/css/Timeline.css",Main.class.getResource("/resources/css/double_slider.css").toExternalForm());
        primaryStage.setTitle("SONDY");
        primaryStage.setMaxHeight(780);
        primaryStage.setMaxWidth(825);
        primaryStage.setMinHeight(780);
        primaryStage.setMinWidth(825);
        root.getChildren().add(globalInterface.getGrid());
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/app-logo-base.png")));
        primaryStage.setScene(scene);
        root.setStyle("-fx-background-color: linear-gradient(#e5e5e5,#e5e5e5);");
        primaryStage.show();
    }
}
