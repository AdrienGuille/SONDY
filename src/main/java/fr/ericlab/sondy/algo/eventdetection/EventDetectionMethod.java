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

import java.io.*;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;

import main.java.fr.ericlab.sondy.core.app.AppParameters;
import main.java.fr.ericlab.sondy.core.structures.Events;
import main.java.fr.ericlab.sondy.algo.Parameters;
import main.java.fr.ericlab.sondy.core.structures.Event;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public abstract class EventDetectionMethod implements Runnable, Serializable {

    public abstract String getName();
    public abstract String getCitation();
    public abstract String getDescription();
    public Parameters parameters;
    public Events events;

    @Override
    public void run(){
        apply();
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        for(Event event : events.list){
            String interval[] = event.getTemporalDescription().split(",");
            event.setTemporalDescription(formatter.format(Double.parseDouble(interval[0]))+","+formatter.format(Double.parseDouble(interval[1])));
        }
        String directory = AppParameters.dataset.path + File.separator + AppParameters.dataset.corpus.preprocessing + File.separator + "events" + File.separator + getName();
        File dir = new File(directory);
        if (!dir.exists())
            dir.mkdirs();

        try {
            FileOutputStream fosEvents = new FileOutputStream(directory + File.separator + getUniqueParameterHash() + ".dat");
            ObjectOutputStream oosEvents = new ObjectOutputStream(fosEvents);
            oosEvents.writeObject(this);
            oosEvents.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getUniqueParameterHash() {
        return parameters.toString().hashCode();
    }
    
    public EventDetectionMethod(){
        parameters = new Parameters();
        events = new Events();
    }
    
    public abstract void apply();
    
    public String getLog(){
        return events.list.size()+" events "+parameters.toString();
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(parameters);
        out.writeObject(events);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException {
        try {
            this.parameters = (Parameters)in.readObject();
            this.events = (Events)in.readObject();
        } catch (ClassNotFoundException ignored) {
            throw new IOException(ignored);
        }
    }
}
