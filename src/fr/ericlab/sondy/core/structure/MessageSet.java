/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core.structure;

/**
 *
 * @author adrien
 */
public class MessageSet {
    public String datasetName;
    public int nbMessages;
    public int nbUsers;
    public int nbRelationships;
    public int[] distribution;
    public int timeSliceLength;
    public int nbTimeSlice;
    public boolean lemmatization;
    public String stemming;
    public int ngram;
    
    public String getTableName(){
        String lemStr = (lemmatization)?"lem1":"lem0";
        return datasetName+"_"+timeSliceLength+"min_"+stemming+"_"+lemStr+"_"+ngram+"gram";
    }
}
