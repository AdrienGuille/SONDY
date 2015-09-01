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
package main.java.fr.ericlab.sondy.algo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class Parameters implements Serializable {
    public ObservableList<Parameter> list;

    public Parameters() {
        list = FXCollections.observableArrayList();
    }

    @Override
    public String toString() {
        if (list.size() > 0) {
            String string = "(";
            for (Parameter p : list) {
                string += p.getName() + "=" + p.getValue() + ", ";
            }
            string = string.substring(0, string.length() - 2);
            return string + ")";
        } else {
            return "";
        }
    }

    public void add(Parameter p) {
        list.add(p);
    }

    public double getParameterValue(String name) {
        for (Parameter p : list) {
            if (p.getName().equals(name)) {
                return Double.parseDouble(p.getValue());
            }
        }
        return 0;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(list.size());
        for (Parameter parameter : list) {
            out.writeObject(parameter);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException {
        int count = in.readInt();
        list = FXCollections.observableArrayList();
        for (int iCounter = 0; iCounter < count; iCounter++) {
            try {
                list.add((Parameter) in.readObject());
            } catch (ClassNotFoundException ignored) {
                throw new IOException(ignored);
            }
        }
    }
}