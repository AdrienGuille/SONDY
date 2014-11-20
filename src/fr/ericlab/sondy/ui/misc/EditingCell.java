/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.ui.misc;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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

public class EditingCell extends TableCell<AlgorithmParameter, String> {
 
      private TextField textField;
     
      public EditingCell() {}
     
      @Override
      public void startEdit() {
          super.startEdit();
         
          if (textField == null) {
              createTextField();
          }
         
          setGraphic(textField);
          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          textField.selectAll();
      }
     
      @Override
      public void cancelEdit() {
          super.cancelEdit();
         
          setText(String.valueOf(getItem()));
          setContentDisplay(ContentDisplay.TEXT_ONLY);
      }
 
      public void updateItem(Double item, boolean empty) {
         
          if (empty) {
              setText(null);
              setGraphic(null);
          } else {
              if (isEditing()) {
                  if (textField != null) {
                      textField.setText(getString());
                  }
                  setGraphic(textField);
                  setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
              } else {
                  setText(getString());
                  setContentDisplay(ContentDisplay.TEXT_ONLY);
              }
          }
      }
 
      private void createTextField() {
          textField = new TextField(getString());
          textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);
          textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
             
              public void handle(KeyEvent t) {
                  if (t.getCode() == KeyCode.ENTER) {
                      commitEdit(textField.getText());
                      setText(textField.getText());
                      setContentDisplay(ContentDisplay.TEXT_ONLY);
                  } else if (t.getCode() == KeyCode.ESCAPE) {
                      cancelEdit();
                  }
              }
          });
      }

            
//      private void commitEdit(String input){
//          value = input;
//      }
//     
      private String getString() {
          return getItem() == null ? "" : getItem().toString();
      }
  }
