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
package main.java.fr.ericlab.sondy.core.structures;

import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.beans.property.SimpleStringProperty;
import javax.swing.text.DateFormatter;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class LogEntry {
    private final SimpleStringProperty time;
    private final SimpleStringProperty info;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
    
    public LogEntry(String t, String l){
        time = new SimpleStringProperty(t);
        info = new SimpleStringProperty(l);
    }
    
    public LogEntry(String l){
        time = new SimpleStringProperty(getCurrentTime());
        info = new SimpleStringProperty(l);
    }

    public String getTime() {
        return time.get();
    }

    public String getInfo() {
        return info.get();
    }
    
    private String getCurrentTime(){
        Date date = new Date();
        return dateFormat.format(date);
    }
}
