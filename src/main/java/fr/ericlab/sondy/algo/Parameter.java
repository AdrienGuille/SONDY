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

import javafx.beans.property.SimpleStringProperty;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class Parameter {
    SimpleStringProperty name = new SimpleStringProperty();
    SimpleStringProperty value = new SimpleStringProperty();
    SimpleStringProperty defaultValue = new SimpleStringProperty();
    
    public Parameter(String n, String dv){
        name.set(n);
        value.set(dv);
        defaultValue.set(dv);
    }
    
    public void setName(String n){
        name.set(n);
    }
    
    public void setValue(String v){
        if(v!=null && !v.equals("")){
            value.set(v);
        }else{
            value.set(defaultValue.get());
        }
    }
    
    public String getName(){
        return name.get();
    }
    
    public String getValue(){
        return value.get();
    }
}
