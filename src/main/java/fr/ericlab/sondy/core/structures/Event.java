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

import javafx.beans.property.SimpleStringProperty;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class Event {
    private final SimpleStringProperty textualDescription;
    private final SimpleStringProperty temporalDescription;
    
    public Event(String text, String temp){
        textualDescription = new SimpleStringProperty(text);
        temporalDescription = new SimpleStringProperty(temp);
    }

    public String getTextualDescription() {
        return textualDescription.get();
    }
    
    public String getTemporalDescription() {
        return temporalDescription.get();
    }
    
    public void setTextualDescription(String newText){
        textualDescription.set(newText);
    }
    
    public void setTemporalDescription(String newTemp){
        temporalDescription.set(newTemp);
    }
}

