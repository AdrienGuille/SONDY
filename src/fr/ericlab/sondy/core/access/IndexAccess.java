/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.core.access;

import fr.ericlab.sondy.core.AppVariables;
import fr.ericlab.sondy.core.DataManipulation;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

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
 *   Class that manages the access to the Lucene index.
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class IndexAccess {

    public IndexReader reader;
    public IndexWriter writer;
    
    /**
     *
     * @param appVariables
     */
    public IndexAccess(AppVariables appVariables){
        try {
            Analyzer analyzer;
            if(appVariables.stemmingLanguage.equalsIgnoreCase("Standard")){
                analyzer = new StandardAnalyzer(Version.LUCENE_36);
            }else{
                if(appVariables.stemmingLanguage.equals("Chinese")){
                    analyzer = new SmartChineseAnalyzer(Version.LUCENE_36);
                }else{
                    String packageName = appVariables.stemmingLanguage.substring(0, 2).toLowerCase();
                    Class cl = Class.forName("org.apache.lucene.analysis."+packageName+"."+appVariables.stemmingLanguage+"Analyzer");
                    Class[] types = new Class[]{Version.class, Set.class};
                    Constructor ct = cl.getConstructor(types);
                    analyzer = (Analyzer) ct.newInstance(Version.LUCENE_36);
                }
            }
            FSDirectory indexDiscret = FSDirectory.open(new File(appVariables.configuration.getWorkspace()+"/datasets/"+appVariables.currentDatasetText.getText()+"/"+appVariables.getCurrentDatasetDiscretization()));
            IndexWriterConfig configDiscret = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            writer = new IndexWriter(indexDiscret, configDiscret);
            reader = IndexReader.open(writer,true);
        } catch (IOException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(IndexAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     *
     * @return
     */
    public HashMap<Integer,Integer> getIdsMap(){
        HashMap<Integer,Integer> idsMap = new HashMap<>();
        for (int i = 0; i < reader.maxDoc(); i++) {
            try {
                Document doc = reader.document(i);
                if(doc != null) {
                    doc.get("id");
                    idsMap.put(i,Integer.parseInt(doc.get("id")));
                }
            } catch (CorruptIndexException ex) {
                Logger.getLogger(IndexAccess.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(IndexAccess.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return idsMap;
    }
    
    public float[] getTermFrequency(AppVariables appVariables, TermDocs termDocs){
        try {
            float[] freqTab = new float[reader.numDocs()+1];
            int totalFreq = 0;
            int nbDoc = 0;
            while(termDocs.next()){
                int freq = termDocs.freq();
                totalFreq += freq;
                int id = appVariables.globalIdMap.get(termDocs.doc());
                freqTab[id] = freq;
            }
            freqTab[reader.numDocs()] = totalFreq;
            return freqTab;
        } catch (IOException ex) {
            Logger.getLogger(IndexAccess.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public float[] getTermFrequency(AppVariables appVariables, String term){
        try {
            TermDocs termDocs = reader.termDocs();
            int numDocs = reader.numDocs();
            termDocs.seek(new Term("content", term));
                if(term!=null){
                    int totalFreq = 0;
                    float[] freqTab = new float[numDocs+1];
                    while(termDocs.next()){
                        int doc = termDocs.doc();
                        int freq = termDocs.freq();
                        int id = appVariables.globalIdMap.get(doc);
                        totalFreq += freq;
                        freqTab[id] = freq;
                    }
                    freqTab[numDocs] = totalFreq;
                    return freqTab;
            }else{
                return new float[numDocs];
            }
        } catch (IOException ex) {
            Logger.getLogger(IndexAccess.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    static public int getTermOccurenceCount(IndexReader reader, String term){
        try {
            int totalFreq = 0;          
            TermDocs termDocs = reader.termDocs();
            termDocs.seek(new Term("content", term));
            while(termDocs.next()){
                totalFreq += termDocs.freq();
            }
            return totalFreq;
        } catch (IOException ex) {
            Logger.getLogger(IndexAccess.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
    
    /**
     *
     */
    public void close(){
        try {
            reader.close();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
