/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core.structure;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 *   Class that stores info about the repository and database.
 * 
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class Configuration {

    private String workspace = new String();
    private String host = new String();
    private String username = new String();
    private String password = new String();
    private String schema = new String();
    
    /**
     *
     */
    public Configuration(){

    }
    
    /**
     *
     */
    public void readConfiguration(){
        try {
            File inputFile = new File("sondy-config.properties");
            if(!inputFile.exists()){
                inputFile.createNewFile();
            }
            FileReader fileReader = new FileReader(inputFile);
            Properties prop = new Properties();
            if(fileReader!=null){
                prop.load(fileReader);
                workspace = (prop.getProperty("workspace","//").endsWith("/"))?prop.getProperty("workspace","//").substring(0,prop.getProperty("workspace","//").length()-1):prop.getProperty("workspace","/");
                host = prop.getProperty("host");
                username = prop.getProperty("username");
                password = prop.getProperty("password");
                schema = prop.getProperty("schema");
                fileReader.close();
            }else{
                prop.put("worskpace", "");
                prop.put("host", "");
                prop.put("username", "");
                prop.put("password", "");
                prop.put("schema", "");
                prop.store(new FileWriter(inputFile), "empty configuration");
            }
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     *
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @return
     */
    public String getSchema() {
        return schema;
    }

    /**
     *
     * @param workspace
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     *
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @param schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
}
