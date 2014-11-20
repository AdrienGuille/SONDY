/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.ui.misc;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

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
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class ContextMenuTableCell implements Callback<TableColumn,TableCell> {
    
    ContextMenu menu;
    EventHandler click;
    
    public ContextMenuTableCell(EventHandler click, ContextMenu menu) {
        this.menu = menu;
        this.click = click;
    }

    public TableCell call(TableColumn p) {
        TableCell cell = new TableCell() {
            @Override 
            protected void updateItem(Object item, boolean empty) {
                 super.updateItem(item, empty);
                 if(item != null) {
                     setText(item.toString());
                     setTooltip(new Tooltip(item.toString()));
                 }
            }
       };
       if(menu != null) {
          cell.setContextMenu(menu);
       }
       if(click != null) {
          cell.setOnMouseClicked(click);
       }
       return cell;
    }
}
