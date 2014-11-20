/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core.access;

import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.structure.Configuration;
import fr.ericlab.sondy.core.structure.Message;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

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
 *   Class that manages the access to the database.
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class DBAccess {
    Connection connection;
    Statement statement;
    
    public DBAccess(){
       super(); 
    }
    
    /**
     *
     * @param appVariables
     * @param test a boolean: if true the connection status will be checked
     * @return true if the connection to the database is established
     */
    public boolean initialize(AppVariables appVariables, boolean test){
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://"+appVariables.configuration.getHost(), appVariables.configuration.getUsername(), appVariables.configuration.getPassword());
            if(test){
                appVariables.databaseStatusIndicator.setProgress(1);
                appVariables.databaseStatusImage.setImage(new Image(getClass().getResourceAsStream("/resources/images/ok.png")));
                appVariables.addLogEntry("[global] connected to the database");
            }
            if(connection != null){
                statement = connection.createStatement();
            }
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            appVariables.databaseStatusIndicator.setProgress(0);
            appVariables.databaseStatusImage.setImage(new Image(getClass().getResourceAsStream("/resources/images/nok.png")));
            appVariables.addLogEntry("[global] can't connect to the database");
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (connection != null);
    }
    
    public LinkedList<Message> getMessages(AppVariables appVariables, String userId, int limit){
        LinkedList<Message> messagesList = new LinkedList<>();
        try {
            ResultSet rs;
            if(limit>0){
                 rs = statement.executeQuery("select msg_post_time,msg_author,msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+appVariables.getStartDay()+"' and msg_post_time<'"+appVariables.getEndDay()+"' and msg_author = '"+userId+"' limit "+limit);
            }else{
                rs = statement.executeQuery("select msg_post_time,msg_author,msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+appVariables.getStartDay()+"' and msg_post_time<'"+appVariables.getEndDay()+"' and msg_author = '"+userId+"'");
            }
            while(rs.next()){
                messagesList.add(new Message(rs.getString(1),rs.getString(3),rs.getString(2)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return messagesList;
    }
    
    /**
     *
     * @param appVariables
     * @param limit
     * @return
     */
    public LinkedList<Message> getMessages(AppVariables appVariables, int limit){
        LinkedList<Message> messagesList = new LinkedList<>();
        try {
            Timestamp start = new Timestamp((long) (appVariables.getStartDay().getTime()+appVariables.startDaySelection*24*60*60*1000L));
            Timestamp end = new Timestamp((long) (appVariables.getStartDay().getTime()+appVariables.endDaySelection*24*60*60*1000L));
            ResultSet rs;
            String space = (appVariables.stemmingLanguage.equals("Chinese"))?"":" ";
            if(limit>0){
                rs = statement.executeQuery("select msg_post_time,msg_author,msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+start+"' and msg_post_time<'"+end+"' and msg_text like '%"+space+appVariables.selectedTerm.split(" ")[0]+space+"%' limit "+limit);
            }else{
                rs = statement.executeQuery("select msg_post_time,msg_author,msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+start+"' and msg_post_time<'"+end+"' and msg_text like '%"+space+appVariables.selectedTerm.split(" ")[0]+space+"%'");
            }
            while(rs.next()){
                messagesList.add(new Message(rs.getString(1),rs.getString(3),rs.getString(2)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return messagesList;
    }
    
    /**
     *
     * @param configuration
     * @param appVariables
     * @param term
     * @param startDay
     * @param endDay
     * @param limit
     * @return
     */
    public LinkedList<Message> getMessagesAsList(Configuration configuration, AppVariables appVariables, String term, Timestamp startDay, Timestamp endDay, int limit){
        LinkedList<Message> messagesList = new LinkedList<>();
        try {
            ResultSet rs;
            String space = (appVariables.stemmingLanguage.equals("Chinese"))?"":" ";
            if(limit>0){
                rs = statement.executeQuery("select msg_post_time,msg_author,msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+startDay+"' and msg_post_time<'"+endDay+"' and msg_text like '%"+space+appVariables.selectedTerm.split(" ")[0]+space+"%' limit "+limit);
            }else{
                rs = statement.executeQuery("select msg_post_time,msg_author,msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+startDay+"' and msg_post_time<'"+endDay+"' and msg_text like '%"+space+appVariables.selectedTerm.split(" ")[0]+space+"%'");
            }
            while(rs.next()){
                messagesList.add(new Message(rs.getString(1),rs.getString(3),rs.getString(2)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return messagesList;
    }
    
    /**
     *
     * @param configuration
     * @param appVariables
     * @param term
     * @param startDay
     * @param endDay
     * @param limit
     * @return
     */
    public String getMessagesAsString(Configuration configuration, AppVariables appVariables, String term, Timestamp startDay, Timestamp endDay, int limit){
        String messages = "";
        try {
            ResultSet rs;
            if(limit>0){
                rs = statement.executeQuery("select msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+startDay+"' and msg_post_time<'"+endDay+"' and msg_text like '%"+term+"%' limit "+limit);
            }else{
                rs = statement.executeQuery("select msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+startDay+"' and msg_post_time<'"+endDay+"' and msg_text like '%"+term+"%'");
            }
            while(rs.next()){
                messages += rs.getString(1)+" ";
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return messages;
    }
    
    /**
     *
     * @param configuration
     * @param appVariables
     * @param term
     * @param startDay
     * @param endDay
     * @param limit
     */
    public void getMessagesAsFile(Configuration configuration, AppVariables appVariables, String term, Timestamp startDay, Timestamp endDay, int limit){
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter("tmp.msg"));
            try {
                ResultSet rs;
                if(limit>0){
                    rs = statement.executeQuery("select msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+startDay+"' and msg_post_time<'"+endDay+"' and msg_text like '%"+term+"%' limit "+limit);
                }else{
                    rs = statement.executeQuery("select msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where msg_post_time>='"+startDay+"' and msg_post_time<'"+endDay+"' and msg_text like '%"+term+"%'");
                }
                while(rs.next()){
                    output.write(rs.getString(1)+"\n");
                }
            } catch (SQLException ex) {
                Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public String getMessagesAsString(AppVariables appVariables, String term, int timeSlice){
        try {
            String messages = "";
            ResultSet rs;
            rs = statement.executeQuery("select msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where time_slice="+timeSlice+" and msg_text like '% "+term+" %';");
            while(rs.next()){
                messages += rs.getString(1)+"\n";
            }
            return messages;
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public String getMessagesAsString(AppVariables appVariables, String term, int timeSliceA, int timeSliceB){
        try {
            String messages = "";
            ResultSet rs;
            String space = (appVariables.stemmingLanguage.equals("Chinese"))?"":" ";
            rs = statement.executeQuery("select msg_text from "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" where time_slice>="+timeSliceA+" and time_slice<="+timeSliceB+" and msg_text like '%"+space+term+space+"%';");
            while(rs.next()){
                messages += rs.getString(1)+"\n";
            }
            return messages;
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    /**
     *
     * @return
     */
    public Statement getStatement(){
        return statement;
    }
        
    /**
     *
     * @param appVariables
     * @return
     */
    public Timestamp[] getDatasetBounds(AppVariables appVariables){
        try {
            Timestamp bounds[] = new Timestamp[2];
            String query = "Select min(msg_post_time), max(msg_post_time) from "+appVariables.configuration.getSchema()+"."+appVariables.currentDatasetText.getText()+"_messages";
            ResultSet rsDate = getStatement().executeQuery(query);
            rsDate.next();
            bounds[0] = rsDate.getTimestamp(1);
            bounds[1] = rsDate.getTimestamp(2);
            return bounds;
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    /**
     *
     * @param appVariables
     * @return
     */
    public int[] getDatasetInfo(AppVariables appVariables){
        try {
            int info[] = new int[3];
            String query = "Select nb_messages, nb_users, nb_edges from "+appVariables.configuration.getSchema()+"."+appVariables.currentDatasetText.getText()+"_info";
            ResultSet rsInfo = getStatement().executeQuery(query);
            rsInfo.next();
            info[0] = rsInfo.getInt(1);
            info[1] = rsInfo.getInt(2);
            info[2] = rsInfo.getInt(3);
            return info;
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    /**
     *
     * @param appVariables
     * @return
     */
    public String getDatasetDescription(AppVariables appVariables){
        try {
            String query = "Select description from "+appVariables.configuration.getSchema()+"."+appVariables.currentDatasetText.getText()+"_info";
            ResultSet rsDesc = getStatement().executeQuery(query);
            rsDesc.next();
            return rsDesc.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    /**
     *
     */
    public void close(){
        try {
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
