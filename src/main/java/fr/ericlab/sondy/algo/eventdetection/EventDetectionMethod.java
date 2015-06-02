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
package main.java.fr.ericlab.sondy.algo.eventdetection;

import java.text.NumberFormat;
import java.util.Locale;
import main.java.fr.ericlab.sondy.core.structures.Events;
import main.java.fr.ericlab.sondy.algo.Parameters;
import main.java.fr.ericlab.sondy.core.structures.Event;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public abstract class EventDetectionMethod implements Runnable {

    public abstract String getName();
    public abstract String getCitation();
    public abstract String getDescription();
    public Parameters parameters;
    public Events events;
    String log;
    
    @Override
    public void run(){
        apply();
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        for(Event event : events.list){
            String interval[] = event.getTemporalDescription().split(",");
            event.setTemporalDescription(formatter.format(Double.parseDouble(interval[0]))+","+formatter.format(Double.parseDouble(interval[1])));
        }
    }
    
    public EventDetectionMethod(){
        parameters = new Parameters();
        events = new Events();
        log = "";
    }
    
    public abstract void apply();
    
    public String getLog(){
        return events.list.size()+" events "+parameters.toString();
    }
}
