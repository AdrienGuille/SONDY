/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ericlab.sondy.core;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import fr.ericlab.sondy.core.misc.StopWords;
import fr.ericlab.sondy.core.structure.TermInfo;
import fr.ericlab.sondy.core.structure.list.TermInfoList;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
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
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */

public class DataManipulation {
       
    static final String[] _ALGO_RESOURCES_ = {"README-online_lda.txt","lda.py","run_lda.sh","stopwords.txt","vocabulary.py","vocabulary.pyc"};
    static final int _BULK_SIZE_ = 100;

    public void updateStopwords(AppVariables appVariables, String name, String newWord){
        try {
            File file = new File(appVariables.configuration.getWorkspace()+"/stopwords/"+name);
            if(!file.exists()){
                file.createNewFile();
            }
            FileUtils.writeStringToFile(file, newWord+"\n", true);
        } catch (IOException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public StopWords getStopwords(AppVariables appVariables, String name){
        StopWords stopWords = new StopWords();
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(new File(appVariables.configuration.getWorkspace()+"/stopwords/"+name), "UTF-8");
            while (it.hasNext()) {
                stopWords.add(it.nextLine());
            }
        } catch (IOException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            LineIterator.closeQuietly(it);
        }
        return stopWords;
    }
    
    public void copyBuiltinAlgorithms(AppVariables appVariables){
        File dir = new File(appVariables.configuration.getWorkspace()+"/algorithms/");
        if(dir.exists()){
            for(String s : _ALGO_RESOURCES_){
                InputStream inputStream = null;
                OutputStream outStream = null;
                try {
                    if(DataManipulation.class.getResource("/resources/algo/"+s) != null){
                        inputStream = DataManipulation.class.getResource("/resources/algo/"+s).openStream();
                        outStream = new FileOutputStream(new File(appVariables.configuration.getWorkspace()+"/algorithms/"+s));
                        IOUtils.copy(inputStream,outStream);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outStream);
                }
            }
        }else{
            appVariables.addLogEntry("[global] can't access workspace");
        }
    }
        
    public ObservableList<String> getAvailableStopwords(AppVariables appVariables){
        ObservableList<String> stopwordsList = FXCollections.observableArrayList();
        File dir = new File(appVariables.configuration.getWorkspace()+"/stopwords/");
        boolean error = false;
        if(!dir.exists()){
            try {
                dir.mkdirs();
                InputStream twitterInputStream = DataManipulation.class.getResource("/resources/stopwords/twitter(en)").openStream();
                InputStream commonInputStream = DataManipulation.class.getResource("/resources/stopwords/common(en)").openStream();
                OutputStream twitterOutStream = new FileOutputStream(new File(appVariables.configuration.getWorkspace()+"/stopwords/twitter(en)"));
                OutputStream commonOutStream = new FileOutputStream(new File(appVariables.configuration.getWorkspace()+"/stopwords/common(en)"));
                IOUtils.copy(twitterInputStream,twitterOutStream);
                IOUtils.copy(commonInputStream,commonOutStream);
                IOUtils.closeQuietly(twitterInputStream);
                IOUtils.closeQuietly(twitterOutStream);
                IOUtils.closeQuietly(commonInputStream);
                IOUtils.closeQuietly(commonOutStream);
            } catch (IOException ex) {
                Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
                error = true;
                appVariables.addLogEntry("[global] can't access workspace");
            }
        }
        if(!error){
            List<File> files = (List<File>) FileUtils.listFiles(dir, HiddenFileFilter.VISIBLE, null);
            for (File file : files) {
                stopwordsList.add("stopwords: "+file.getName());
            }
        }
        return stopwordsList;
    }
    
    public float centeredMovingAverage(float[] tab, int index, int windowSize){
        int halfWindowSize = windowSize/2;
        int possibleLeftWindow = (index >= halfWindowSize)?halfWindowSize:index;
        int possibleRightWindow = (index+halfWindowSize < tab.length-1)? halfWindowSize:tab.length-2-index;
        int i1 = index - possibleLeftWindow, i2 = index + possibleRightWindow;
        float total = 0;
        for(int i = i1; i <= i2; i++){
            total += tab[i];
        }
        return total/(float)(possibleLeftWindow+possibleRightWindow);
    }
       
    public int getTermOccurenceCount(IndexReader reader, String term){
        try {
            int totalFreq = 0;          
            TermDocs termDocs = reader.termDocs();
            termDocs.seek(new Term("content", term));
            while(termDocs.next()){
                totalFreq += termDocs.freq();
            }
            return totalFreq;
        } catch (IOException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
    
    public float[] getSmoothedTermFrequency(float tab[], int windowSize){
        float[] smoothedTab = new float[tab.length];
        for(int i = 0; i < tab.length-1; i++){
            smoothedTab[i] = centeredMovingAverage(tab, i, windowSize);
        }
        return smoothedTab;
    }
    
    public LinkedList<String> getDatasetList(String repositoryPath){
        LinkedList<String> datasetList = new LinkedList<>();
        File dir = new File(repositoryPath+"/");
        if(dir.exists()){
            String[] files = dir.list(DirectoryFileFilter.INSTANCE);
            datasetList.addAll(Arrays.asList(files));
        }else{
            dir.mkdir();
        }
        return datasetList;
    }
    
    public LinkedList<String> getAvailablePreparedStreams(String repositoryPath, String datasetName){
        LinkedList<String> discretizationsList = new LinkedList<>();
        File dir = new File(repositoryPath+"/"+datasetName+"/");
        if(dir.exists()){
            String[] files = dir.list(DirectoryFileFilter.INSTANCE);
            for(String str : files){
                if(!str.contains("-m")){
                    discretizationsList.add(str);
                }
            }
        }
        return discretizationsList;
    }
    
    public void prepareStream(String datasetName, int intervalDuration, int ngram, String stemLanguage, boolean lemmatization, AppVariables appVariables){
        try {
            Connection connection;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://"+appVariables.configuration.getHost(), appVariables.configuration.getUsername(), appVariables.configuration.getPassword());
            Statement statement = connection.createStatement();
            Statement statement2 = connection.createStatement();
            
            String lemStr = (lemmatization)?"_lem1":"_lem0";
            statement.executeUpdate("CREATE TABLE "+appVariables.configuration.getSchema()+"."+datasetName+"_"+intervalDuration+"min_"+stemLanguage+lemStr+"_"+ngram+"gram ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, msg_author VARCHAR(100), msg_post_time TIMESTAMP, msg_text VARCHAR(600), time_slice INT)ENGINE=myisam;" );
//            statement.executeUpdate("CREATE INDEX index_time ON "+appVariables.configuration.getSchema()+"."+datasetName+"_messages (msg_post_time)");
            
            ResultSet rsTMin = statement.executeQuery("select min(msg_post_time) from "+appVariables.configuration.getSchema()+"."+datasetName+"_messages;");
            rsTMin.next();
            Timestamp tMin = rsTMin.getTimestamp(1);
            ResultSet rsTMax = statement.executeQuery("select max(msg_post_time) from "+appVariables.configuration.getSchema()+"."+datasetName+"_messages;");
            rsTMax.next();
            Timestamp tMax = rsTMax.getTimestamp(1);
            Timestamp tRef = new Timestamp(0);
            long base = (tMin.getTime()-tRef.getTime())*1L;
            long streamDuration = (tMax.getTime()-tMin.getTime())*1L;
            long streamDurationMin = (streamDuration/1000)/60;
                        
            String path = appVariables.configuration.getWorkspace()+"/datasets/"+datasetName+"/"+intervalDuration+"min-"+stemLanguage;
            path += (lemmatization)?"-lem1":"-lem0";
            path += "-"+ngram+"gram";
            String pathMention = path+"-m";
            
            FSDirectory indexGlobal = FSDirectory.open(new File(path));
            FSDirectory indexMention = FSDirectory.open(new File(pathMention));
            Analyzer analyzer;
            Properties props = new Properties();
            props.put("annotators", "tokenize,ssplit,parse,lemma");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            Annotation annotation;
            if(stemLanguage.equalsIgnoreCase("Standard")){
                analyzer = new StandardAnalyzer(Version.LUCENE_36);
            }else{
                Class cl;
                if(stemLanguage.equals("Chinese")){
                    analyzer = new SmartChineseAnalyzer(Version.LUCENE_36);
                }else{
                    String packageName = stemLanguage.substring(0, 2).toLowerCase();
                    cl = Class.forName("org.apache.lucene.analysis."+packageName+"."+stemLanguage+"Analyzer");
                    Class[] types = new Class[]{Version.class, Set.class};
                    Constructor ct = cl.getConstructor(types);
                    analyzer = (Analyzer) ct.newInstance(Version.LUCENE_36,appVariables.currentStopWords.getSet());
                }
            }
            IndexWriterConfig configGlobal;
            IndexWriterConfig configMention;
            ShingleAnalyzerWrapper shingleAnalyzer = null;
            if(ngram > 1){
                shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, ngram, ngram," ",false,false);
                WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(Version.LUCENE_36);
                configGlobal = new IndexWriterConfig(Version.LUCENE_36, whitespaceAnalyzer);
                configMention = new IndexWriterConfig(Version.LUCENE_36, whitespaceAnalyzer);
            }
            else{
                configGlobal = new IndexWriterConfig(Version.LUCENE_36, analyzer);
                configMention = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            }
            IndexWriter wGlobal = new IndexWriter(indexGlobal, configGlobal);
            IndexWriter wMention = new IndexWriter(indexMention, configMention);

            int docId = 0;
            for(int i = 0; i < streamDurationMin; i += intervalDuration){
                statement = connection.createStatement();
                long infBound = base + i*60*1000L;
                long supBound = base + (i+intervalDuration)*60*1000L;
                Timestamp infTime = new Timestamp(infBound);
                Timestamp supTime = new Timestamp(supBound);
                ResultSet rs = statement.executeQuery("SELECT msg_text, msg_post_time, msg_author FROM "+appVariables.configuration.getSchema()+"."+datasetName+"_messages WHERE msg_post_time>'"+infTime+"' AND msg_post_time< '"+supTime+"'");
                String globalContent = new String();
                String mentionContent = new String();
                String timestamps = new String();
                NumberFormat formatter = new DecimalFormat("00000000");
                int bulk = 0;
                String bulkString = "";
                boolean mention;
                while(rs.next()){
                    String message = rs.getString(1).toLowerCase();
                    mention = message.contains("@");
                    if(lemmatization){
                        annotation = new Annotation(message);
                        message = "";
                        pipeline.annotate(annotation);
                        List<CoreMap> lem = annotation.get(SentencesAnnotation.class);
                        for(CoreMap l: lem) {
                            for (CoreLabel token: l.get(TokensAnnotation.class)) {
                                message += token.get(LemmaAnnotation.class)+" ";
                            }
                        }
                    }
                    if(ngram > 1){
                        String processedMessage = "";
                        TokenStream tokenStream = shingleAnalyzer.tokenStream("text", new StringReader(message));
                        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
                        while (tokenStream.incrementToken()) {
                            String termToken = charTermAttribute.toString();
                            if(!termToken.contains("_")){
                                processedMessage += termToken.replace(" ", "=")+" ";
                            }
                        }
                        message = processedMessage;
                    }
                    bulk++;
                    if(bulk < _BULK_SIZE_){
                            bulkString += " ("+docId+",'"+rs.getString(2)+"',\""+message+"\",\""+rs.getString(3)+"\"),";
                        }else{
                            bulk = 0;
                            bulkString += " ("+docId+",'"+rs.getString(2)+"',\""+message+"\",\""+rs.getString(3)+"\");";
                            statement2.executeUpdate("INSERT INTO "+appVariables.configuration.getSchema()+"."+datasetName+"_"+intervalDuration+"min_"+stemLanguage+lemStr+"_"+ngram+"gram (time_slice,msg_post_time,msg_text,msg_author) VALUES"+bulkString);
                            bulkString = "";
                        }
                    globalContent += message+"\n";
                    if(mention){
                        mentionContent += message+"\n";
                    }
                    timestamps += rs.getString(2)+"\n";
                }
                if(bulk > 0 && bulkString.length() > 0){
                    statement2.executeUpdate("INSERT INTO "+appVariables.configuration.getSchema()+"."+datasetName+"_"+intervalDuration+"min_"+stemLanguage+lemStr+"_"+ngram+"gram (time_slice,msg_post_time,msg_text,msg_author) VALUES"+bulkString.substring(0,bulkString.length()-1)+";");
                }
                Document docGlobal = new Document();
                docGlobal.add(new Field("content", globalContent, Field.Store.YES, Field.Index.ANALYZED,Field.TermVector.YES));
                docGlobal.add(new Field("id", Integer.toString(docId), Field.Store.YES, Field.Index.NOT_ANALYZED));
                wGlobal.addDocument(docGlobal);
                wGlobal.commit();
                Document docMention = new Document();
                docMention.add(new Field("content", mentionContent, Field.Store.YES, Field.Index.ANALYZED,Field.TermVector.YES));
                docMention.add(new Field("id", Integer.toString(docId), Field.Store.YES, Field.Index.NOT_ANALYZED));
                wMention.addDocument(docMention);
                wMention.commit();
                
                File textFile = new File(path+"/input/"+formatter.format(docId)+".text");
		FileUtils.writeStringToFile(textFile, globalContent);
                File timeFile = new File(path+"/input/"+formatter.format(docId)+".time");
		FileUtils.writeStringToFile(timeFile, timestamps);
                
                docId++;
                statement.close();
            }
            statement2.executeUpdate("CREATE INDEX index_time_slice ON "+appVariables.configuration.getSchema()+"."+datasetName+"_"+intervalDuration+"min_"+stemLanguage+lemStr+"_"+ngram+"gram (time_slice);");
            statement2.executeUpdate("CREATE FULLTEXT INDEX index_text ON "+appVariables.configuration.getSchema()+"."+datasetName+"_"+intervalDuration+"min_"+stemLanguage+lemStr+"_"+ngram+"gram (msg_text);");
            statement2.close();
            connection.close();
            wGlobal.close();
            wMention.close();
        } catch (IOException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void importDataset(String datasetName, String datasetDescription, String networkFilePath, String messagesFilePath, AppVariables appVariables){
        Connection connection;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://"+appVariables.configuration.getHost(), appVariables.configuration.getUsername(), appVariables.configuration.getPassword());
            Statement statement = connection.createStatement();
            // Creating tables and indexes
            statement.executeUpdate("CREATE TABLE "+appVariables.configuration.getSchema()+"."+datasetName+"_messages ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, msg_author VARCHAR(100), msg_post_time TIMESTAMP, msg_text VARCHAR(200))ENGINE=myisam;" );
            statement.executeUpdate("CREATE INDEX index_time ON "+appVariables.configuration.getSchema()+"."+datasetName+"_messages (msg_post_time)");
            statement.executeUpdate("CREATE TABLE "+appVariables.configuration.getSchema()+"."+datasetName+"_network ( user VARCHAR(100), connected_user VARCHAR(100))ENGINE=myisam;" );
            statement.executeUpdate("CREATE INDEX index_edges ON "+appVariables.configuration.getSchema()+"."+datasetName+"_network (user)");
            statement.executeUpdate("CREATE INDEX index_inverse_edges ON "+appVariables.configuration.getSchema()+"."+datasetName+"_network (connected_user)");
            statement.executeUpdate("CREATE TABLE "+appVariables.configuration.getSchema()+"."+datasetName+"_info ( description TEXT, nb_messages INT, nb_users INT, nb_edges INT) ENGINE=myisam;" );
            // Creating directory
            File datasetRoot = new File(appVariables.configuration.getWorkspace()+"/datasets/");
            if(!datasetRoot.exists()){
                datasetRoot.mkdir();
            }
            File datasetDirectory = new File(appVariables.configuration.getWorkspace()+"/datasets/"+datasetName);
            datasetDirectory.mkdir();
            // Loading data
            if(networkFilePath != null){
                networkFilePath = networkFilePath.replace("\\", "/");
                statement.executeUpdate("LOAD DATA LOCAL INFILE '"+networkFilePath+"' INTO TABLE "+appVariables.configuration.getSchema()+"."+datasetName+"_network CHARACTER SET UTF8 FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n' (user,connected_user);");
            }
            messagesFilePath = messagesFilePath.replace("\\", "/");
            statement.executeUpdate("LOAD DATA LOCAL INFILE '"+messagesFilePath+"' INTO TABLE "+appVariables.configuration.getSchema()+"."+datasetName+"_messages CHARACTER SET UTF8 FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n' (msg_author,msg_post_time,msg_text);");
            // Updating stats about the dataset
            statement.executeUpdate("INSERT INTO "+appVariables.configuration.getSchema()+"."+datasetName+"_info (description,nb_messages,nb_users,nb_edges) VALUES (\""+datasetDescription+"\", (SELECT count(*) FROM "+appVariables.configuration.getSchema()+"."+datasetName+"_messages) ,(SELECT count(distinct msg_author) FROM "+appVariables.configuration.getSchema()+"."+datasetName+"_messages), (SELECT count(*) FROM "+appVariables.configuration.getSchema()+"."+datasetName+"_network))");
//            int nbEdges = 0;
//            if(networkFilePath != null){
//                LineIterator itEdges = null;
//                try {
//                    itEdges = FileUtils.lineIterator(new File(networkFilePath), "UTF-8");
//                    while (itEdges.hasNext()) {
//                        String[] edge = itEdges.nextLine().split("\t");
//                        if(edge.length==2){
//                            statement.executeUpdate("INSERT INTO "+appVariables.configuration.getSchema()+"."+datasetName+"_network (user,connected_user) VALUES ("+edge[0]+","+edge[1]+")");
//                            nbEdges++;
//                        }
//                    }
//                } catch (IOException ex) {
//                    Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
//                } finally {
//                    LineIterator.closeQuietly(itEdges);
//                }
//            }
//            int nbMessages = 0;
//            LineIterator itMessages = null;
//            try {
//                itMessages = FileUtils.lineIterator(new File(messagesFilePath), "UTF-8");
//                while (itMessages.hasNext()) {
//                    String[] message = itMessages.nextLine().split("\t");
//                    if(message.length==3){
//                        statement.executeUpdate("INSERT INTO "+appVariables.configuration.getSchema()+"."+datasetName+"_messages (msg_author,msg_post_time,msg_text) VALUES (\""+message[0]+"\",'"+message[1]+"',\""+message[2]+"\")");
//                    }
//                    nbMessages++;
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
//            } finally {
//                LineIterator.closeQuietly(itMessages);
//            }
//            statement.executeUpdate("INSERT INTO "+appVariables.configuration.getSchema()+"."+datasetName+"_info (description,nb_messages,nb_users,nb_edges) VALUES (\""+datasetDescription+"\","+nbMessages+",(SELECT count(distinct user) from "+appVariables.configuration.getSchema()+"."+datasetName+"_network),"+nbEdges+")");
            connection.close();
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String[] getFrequentCoocurringTerms(String document, int numTerms, String baseTerm, AppVariables appVariables){
        String [] frequentTerms = new String[numTerms];
        try {
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            RAMDirectory index = new RAMDirectory();
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            IndexWriter w = new IndexWriter(index, config);
            Document doc = new Document();
            doc.add(new Field("content", document, Field.Store.NO, Field.Index.ANALYZED,Field.TermVector.YES));
            w.addDocument(doc);
            w.commit();
            IndexReader r = IndexReader.open(w, true);
            TermEnum allTerms = r.terms();
            int minFreq = 0;
            TermInfoList termList = new TermInfoList();
            StopWords stopWords = appVariables.currentStopWords;
            HashSet<String> stopWordsSet = stopWords.getSet();
            stopWords.add(baseTerm);
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>1 && !stopWordsSet.contains(term)){
                    float cf = getTermOccurenceCount(r, term);
                    if(cf>minFreq){
                        termList.addTermInfo(new TermInfo(term,(int)cf));
                        termList.sortList();
                        if(termList.size() > numTerms){
                            termList.removeLast();
                        }
                        minFreq = termList.get(termList.size()-1).occurence;
                    }
                }
            }
            for(int i = 0; i < termList.size(); i++){
                frequentTerms[i] = termList.get(i).text;
            }
            w.close();
            r.close();
            index.close();
        } catch (Exception ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return frequentTerms;
    }
    
    public String[] getFrequentCoocurringTermsFromIndex(IndexReader r, int numTerms, String baseTerm, AppVariables appVariables){
        String [] frequentTerms = new String[numTerms];
        try {
            TermEnum allTerms = r.terms();
            int minFreq = 0;
            TermInfoList termList = new TermInfoList();
            StopWords stopWords = appVariables.currentStopWords;
            HashSet<String> stopWordsSet = stopWords.getSet();
            stopWords.add(baseTerm);
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>1 && !stopWordsSet.contains(term)){
                    float cf = getTermOccurenceCount(r, term);
                    if(cf>minFreq){
                        termList.addTermInfo(new TermInfo(term,(int)cf));
                        termList.sortList();
                        if(termList.size() > numTerms){
                            termList.removeLast();
                        }
                        minFreq = termList.get(termList.size()-1).occurence;
                    }
                }
            }
            for(int i = 0; i < termList.size(); i++){
                frequentTerms[i] = termList.get(i).text;
            }
        } catch (Exception ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return frequentTerms;
    }
        
    public String[] getFrequentCoocurringTermsFromFile(int numTerms, String baseTerm, AppVariables appVariables){
        String [] frequentTerms = new String[numTerms];
        try {
            BufferedReader input = new BufferedReader(new FileReader("tmp.msg"));
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            RAMDirectory index = new RAMDirectory();
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            IndexWriter w = new IndexWriter(index, config);
            String line = "";
            String document = "";
            int count = 0;
            while((line = input.readLine())!=null){
                count++;
                document += line;
                if(count == 2000){
                    Document doc = new Document();
                    doc.add(new Field("content", document, Field.Store.NO, Field.Index.ANALYZED,Field.TermVector.YES));
                    w.addDocument(doc);
                    w.commit();
                    count = 0;
                    document = "";
                }
            }
            Document doc = new Document();
            doc.add(new Field("content", document, Field.Store.NO, Field.Index.ANALYZED,Field.TermVector.YES));
            w.addDocument(doc);
            w.commit();
            input.close();
            IndexReader r = IndexReader.open(w, true);
            TermEnum allTerms = r.terms();
            int minFreq = 0;
            TermInfoList termList = new TermInfoList();
            StopWords stopWords = appVariables.currentStopWords;
            HashSet<String> stopWordsSet = stopWords.getSet();
            stopWords.add(baseTerm);
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>1 && !stopWordsSet.contains(term)){
                    float cf = getTermOccurenceCount(r, term);
                    if(cf>minFreq){
                        termList.addTermInfo(new TermInfo(term,(int)cf));
                        termList.sortList();
                        if(termList.size() > numTerms){
                            termList.removeLast();
                        }
                        minFreq = termList.get(termList.size()-1).occurence;
                    }
                }
            }
            for(int i = 0; i < termList.size(); i++){
                frequentTerms[i] = termList.get(i).text;
            }
            w.close();
            r.close();
            index.close();
        } catch (Exception ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return frequentTerms;
    }
    
    public int[] getDistribution(AppVariables appVariables){
        try {
            Connection connection;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://"+appVariables.configuration.getHost(), appVariables.configuration.getUsername(), appVariables.configuration.getPassword());
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT time_slice, count(*) FROM "+appVariables.configuration.getSchema()+"."+appVariables.messageSet.getTableName()+" group by time_slice;");
            int[] distribution = new int[appVariables.messageSet.nbTimeSlice];
            while(rs.next()){
                distribution[rs.getInt(1)] = rs.getInt(2);
            }
            return distribution;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(DataManipulation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new int[0];
    }
    
    static public float sum(float tab[], int a, int b){
        float sum = 0;
        for(int i = a; i <= b; i++){
            sum += tab[i];
        }
        return sum;
    }
}
