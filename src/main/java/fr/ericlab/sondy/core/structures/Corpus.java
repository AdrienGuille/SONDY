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
package main.java.fr.ericlab.sondy.core.structures;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import main.java.fr.ericlab.sondy.core.app.Configuration;
import main.java.fr.ericlab.sondy.core.text.index.GlobalIndexer;
import main.java.fr.ericlab.sondy.core.text.nlp.ArabicStemming;
import main.java.fr.ericlab.sondy.core.text.nlp.PersianStemming;
import main.java.fr.ericlab.sondy.core.utils.PropertiesFileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.fr.ericlab.sondy.core.text.index.Tokenizer;
import main.java.fr.ericlab.sondy.core.text.nlp.EnglishStemming;
import main.java.fr.ericlab.sondy.core.text.nlp.FrenchStemming;
import main.java.fr.ericlab.sondy.core.utils.ArrayUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 *   @author Farrokh GHAMSARY
 */
public class Corpus {
    // Properties
    public int messageCount;
    public int authorCount;
    public Date start;
    public Date end;
    public Path path;
    
    // Preprocessed corpus
    public String preprocessing = "";
    public int timeSliceLength;
    public int[] messageDistribution;
    public short[][] termFrequencies;
    public ArrayList<String> vocabulary;
    
    public short[][] termMentionFrequencies;
    public ArrayList<String> mentionVocabulary;

    public String[] splitString(String str) {
        return str.split("\t");
    }

    public void loadProperties(Path p) {
        try {
            path = p;
            String propertiesFilePath = Paths.get(path+File.separator+"messages.properties").toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            messageCount = Integer.parseInt(PropertiesFileUtils.readProperty(propertiesFilePath, "messageCount"));
            authorCount = Integer.parseInt(PropertiesFileUtils.readProperty(propertiesFilePath, "authorCount"));
            start = dateFormat.parse(PropertiesFileUtils.readProperty(propertiesFilePath, "start"));
            end = dateFormat.parse(PropertiesFileUtils.readProperty(propertiesFilePath, "end"));
            preprocessing = "";
            messageDistribution = null;
            termFrequencies = null;
            vocabulary = null;
        } catch (ParseException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String create(String id, String csvFilePath) {
        File dir = Paths.get(Configuration.datasets.toString() + File.separator + id).toFile();
        int skippedLineCount = 0;
        try {
            Properties properties = new Properties();
            HashSet<String> authors = new HashSet<>();
            Date minDate = null, maxDate = null;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(csvFilePath)));
            messageCount = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String line;
            String firstLine = bufferedReader.readLine();
            String[] components = splitString(firstLine);
            while (components.length != 3 && (firstLine = bufferedReader.readLine()) != null) {
                components = splitString(firstLine);
                skippedLineCount++;
            }
            if (components.length == 3) {
                authors.add(components[0]);
                Date parsedDate = dateFormat.parse(components[1]);
                minDate = maxDate = parsedDate;
                File messages = new File(dir.getAbsolutePath() + File.separator + "messages.csv");
                FileUtils.write(messages, "");
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(messages));
                bufferedWriter.write(firstLine);
                bufferedWriter.newLine();
                while ((line = bufferedReader.readLine()) != null) {
                    components = splitString(line);
                    if (components.length == 3) {
                            authors.add(components[0]);
                            parsedDate = dateFormat.parse(components[1]);
                            if (parsedDate.before(minDate)) {
                                    minDate = parsedDate;
                            } else {
                                    if (parsedDate.after(maxDate)) {
                                            maxDate = parsedDate;
                                    }
                            }
                            messageCount++;
                            bufferedWriter.write(line);
                            bufferedWriter.newLine();
                    }
                    else {
                            skippedLineCount++;
                    }
                }
                bufferedWriter.close();
            }
            bufferedReader.close();
            properties.setProperty("messageCount", messageCount+"");
            properties.setProperty("authorCount", authors.size()+"");
            properties.setProperty("start", dateFormat.format(minDate));
            properties.setProperty("end", dateFormat.format(maxDate));
            PropertiesFileUtils.saveProperties(Paths.get(Configuration.datasets + File.separator + id + File.separator + "messages.properties").toString(),properties);
        } catch (IOException | ParseException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Messages: done (imported "+messageCount+" messages, skipped "+skippedLineCount+" misformatted lines).";
    }

    
    public void lemmatize(Path path) {
        try {
            LineIterator lineIterator = FileUtils.lineIterator(new File(path.toString()+File.separator+"messages.csv"));
            Properties props = new Properties();
            props.put("annotators", "tokenize,ssplit,parse,lemma");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            Annotation annotation;
            File lemmatizedFile = new File(path.toString()+File.separator+"lemmatized_messages.csv");
            while(lineIterator.hasNext()){
                String[] components = splitString(lineIterator.nextLine());
                String text = components[2];
                annotation = new Annotation(text);
                String lemmatizedText = "";
                pipeline.annotate(annotation);
                List<CoreMap> lem = annotation.get(CoreAnnotations.SentencesAnnotation.class);
                for(CoreMap l: lem) {
                    for (CoreLabel token: l.get(CoreAnnotations.TokensAnnotation.class)) {
                        lemmatizedText += token.get(CoreAnnotations.LemmaAnnotation.class)+" ";
                    }
                }
                if(text.contains("@")){
                    lemmatizedText += " @";
                }
                FileUtils.writeStringToFile(lemmatizedFile, components[0]+"\t"+components[1]+"\t"+lemmatizedText+"\n", true);
            }
        } catch (IOException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String preprocess(Path path,String stemming, String lemmatization, int ngram, int timeSliceLength){      
        Path preprocessPath = Paths.get(path+File.separator+stemming+"-"+lemmatization+"-"+ngram+"-"+timeSliceLength);
        preprocessPath.normalize();
        File dir = preprocessPath.toFile();
        File sourceFile = new File(path.toString()+File.separator+"messages.csv");
        if(lemmatization.equals("English")){
            File lemmatizedFile = new File(path.toString()+File.separator+"lemmatized_messages.csv");
            if(!lemmatizedFile.exists()){
                lemmatize(path);
            }
            sourceFile = lemmatizedFile;
        }
        if(!dir.exists()){
            try {
                dir.mkdir();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(sourceFile));
                BufferedWriter bwText = null, bwTime = null, bwAuthor = null;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long startTime = start.getTime();
                NumberFormat formatter = new DecimalFormat("00000000");
                Analyzer analyzer = new StandardAnalyzer();
                String line;
                int timeSlice = -1;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] components = splitString(line);
                    Date parsedDate = dateFormat.parse(components[1]);
                    double diff = (parsedDate.getTime() - startTime) / (60 * 1000);
                    if (timeSlice != (int) (diff / timeSliceLength)) {
                        timeSlice = (int) (diff / timeSliceLength);
                        if (bwText != null) {
                            bwText.close();
                        }
                        File fileText = new File(preprocessPath + File.separator + formatter.format(timeSlice) + ".text");
                        if (!fileText.exists()) {
                            FileUtils.write(fileText, "");
                        }
                        bwText = new BufferedWriter(new FileWriter(fileText, true));
                        if (bwTime != null) {
                            bwTime.close();
                        }
                        File fileTime = new File(preprocessPath + File.separator + formatter.format(timeSlice) + ".time");
                        if (!fileTime.exists()) {
                            FileUtils.write(fileText, "");
                        }
                        bwTime = new BufferedWriter(new FileWriter(fileTime, true));
                        if (bwAuthor != null) {
                            bwAuthor.close();
                        }
                        File fileAuthor = new File(preprocessPath + File.separator + formatter.format(timeSlice) + ".author");
                        if (!fileAuthor.exists()) {
                            FileUtils.write(fileAuthor, "");
                        }
                        bwAuthor = new BufferedWriter(new FileWriter(fileAuthor, true));
                    }
                    String text = components[2];
                    if (!stemming.equals("disabled")) {
                        String newText = "";
                        List<String> tokenList = Tokenizer.tokenizeString(analyzer, text);
                        switch (stemming) {
                            case "French":
                                FrenchStemming frenchStemming = new FrenchStemming();
                                for (String token : tokenList)
                                    newText += frenchStemming.stem(token) + " ";
                                break;
                            case "Arabic":
                                ArabicStemming arabicStemming = new ArabicStemming();
                                for (String token : tokenList)
                                    newText += arabicStemming.stem(token) + " ";
                                break;
                            case "Persian":
                                PersianStemming persianStemming = new PersianStemming();
                                for (String token : tokenList)
                                    newText += persianStemming.stem(token) + " ";
                                break;
                            case "English":
                                EnglishStemming englishStemming = new EnglishStemming();
                                for (String token : tokenList)
                                    newText += englishStemming.stem(token) + " ";
                            default:
                                break;
                        }
                        text = newText;
                    }
                    if (ngram > 1) {
                        String newText = "";
                        List<String> tokenList = Tokenizer.tokenizeString(analyzer, text);
                        for (int token = 0; token < tokenList.size() - 1 - ngram; token++) {
                            for (int n = 0; n < ngram; n++) {
                                newText += tokenList.get(token + n);
                                if (n == ngram - 1) {
                                    newText += " ";
                                } else {
                                    newText += "_";
                                }
                            }

                        }
                        text = newText;
                    }
                    bwText.write(text);
                    bwText.newLine();
                    bwTime.write(components[1]);
                    bwTime.newLine();
                    bwAuthor.write(components[0]);
                    bwAuthor.newLine();
                }
                bwText.close();
                bwTime.close();
                bwAuthor.close();
                bufferedReader.close();
                GlobalIndexer indexer = new GlobalIndexer(Configuration.numberOfCores, false);
                indexer.index(preprocessPath.toString());
                indexer = new GlobalIndexer(Configuration.numberOfCores, true);
                indexer.index(preprocessPath.toString());
                return "Done.";
            } catch (IOException | ParseException | InterruptedException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "Error:";
    }
    
    public void loadFrequencies(String preprocessedCorpus){
        preprocessing = preprocessedCorpus;
        FileInputStream fisMatrix = null;
        try {
            setTimeSliceLength();
            fisMatrix = new FileInputStream(path+File.separator+preprocessing+File.separator+"indexes/frequencyMatrix.dat");
            ObjectInputStream oisMatrix = new ObjectInputStream(fisMatrix);
            termFrequencies = (short[][]) oisMatrix.readObject();
            FileInputStream fisVocabulary = new FileInputStream(path+File.separator+preprocessing+File.separator+"indexes/vocabulary.dat");
            ObjectInputStream oisVocabulary = new ObjectInputStream(fisVocabulary);
            vocabulary = (ArrayList<String>) oisVocabulary.readObject();
            FileInputStream fisDistribution = new FileInputStream(path+File.separator+preprocessing+File.separator+"indexes/messageCountDistribution.dat");
            ObjectInputStream oisDistribution = new ObjectInputStream(fisDistribution);
            messageDistribution = (int[]) oisDistribution.readObject();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fisMatrix.close();
            } catch (IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void loadMentionFrequencies(){
        FileInputStream fisMatrix = null;
        try {
            setTimeSliceLength();
            fisMatrix = new FileInputStream(path+File.separator+preprocessing+File.separator+"indexes/mentionFrequencyMatrix.dat");
            ObjectInputStream oisMatrix = new ObjectInputStream(fisMatrix);
            termMentionFrequencies = (short[][]) oisMatrix.readObject();
            FileInputStream fisVocabulary = new FileInputStream(path+File.separator+preprocessing+File.separator+"indexes/mentionVocabulary.dat");
            ObjectInputStream oisVocabulary = new ObjectInputStream(fisVocabulary);
            mentionVocabulary = (ArrayList<String>) oisVocabulary.readObject();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fisMatrix.close();
            } catch (IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public short[] getTermFrequency(String term){
        int i = vocabulary.indexOf(term);
        return termFrequencies[i];
    }
    
    public short[] getTermMentionFrequency(String term){
        int i = mentionVocabulary.indexOf(term);
        return termMentionFrequencies[i];
    }
    
    public String getMessages(String term, int timeSliceA, int timeSliceB){
        String messages = "";
        NumberFormat formatter = new DecimalFormat("00000000");
        for(int i = timeSliceA; i <= timeSliceB; i++){
            try {
                File textFile = new File(path+File.separator+preprocessing+File.separator+formatter.format(i)+".text");
                List<String> lines = FileUtils.readLines(textFile);
                for(String line : lines){
                    if(StringUtils.containsIgnoreCase(line,term)){
                        messages += line+"\n";
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return messages;
    }
    
    public ObservableList<Message> getMessages(Event event){
        ObservableList<Message> messages = FXCollections.observableArrayList();
        String[] interval = event.getTemporalDescription().split(",");
        int timeSliceA = convertDayToTimeSlice(Double.parseDouble(interval[0]));
        int timeSliceB = convertDayToTimeSlice(Double.parseDouble(interval[1]));
        String term = event.getTextualDescription().split(" ")[0];
        NumberFormat formatter = new DecimalFormat("00000000");
        for(int i = timeSliceA; i <= timeSliceB; i++){
            try {
                File textFile = new File(path+File.separator+preprocessing+File.separator+formatter.format(i)+".text");
                File timeFile = new File(path+File.separator+preprocessing+File.separator+formatter.format(i)+".time");
                File authorFile = new File(path+File.separator+preprocessing+File.separator+formatter.format(i)+".author");
                LineIterator textIter = FileUtils.lineIterator(textFile);
                LineIterator timeIter = FileUtils.lineIterator(timeFile);
                LineIterator authorIter = FileUtils.lineIterator(authorFile);
                while(textIter.hasNext()){
                    String text = textIter.nextLine();
                    String author = authorIter.nextLine();
                    String time = timeIter.nextLine();
                    if(StringUtils.containsIgnoreCase(text,term)){
                        messages.add(new Message(author,time,text));
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return messages;
    }
    
    public ObservableList<Message> getFilteredMessages(Event event, String[] words, int operator){
        ObservableList<Message> messages = FXCollections.observableArrayList();
        String[] interval = event.getTemporalDescription().split(",");
        int timeSliceA = convertDayToTimeSlice(Double.parseDouble(interval[0]));
        int timeSliceB = convertDayToTimeSlice(Double.parseDouble(interval[1]));
        String term = event.getTextualDescription().split(" ")[0];
        NumberFormat formatter = new DecimalFormat("00000000");
        for(int i = timeSliceA; i <= timeSliceB; i++){
            try {
                File textFile = new File(path+File.separator+preprocessing+File.separator+formatter.format(i)+".text");
                File timeFile = new File(path+File.separator+preprocessing+File.separator+formatter.format(i)+".time");
                File authorFile = new File(path+File.separator+preprocessing+File.separator+formatter.format(i)+".author");
                LineIterator textIter = FileUtils.lineIterator(textFile);
                LineIterator timeIter = FileUtils.lineIterator(timeFile);
                LineIterator authorIter = FileUtils.lineIterator(authorFile);
                while(textIter.hasNext()){
                    String text = textIter.nextLine();
                    short[] test = new short[words.length];
                    for(int j = 0; j < words.length; j++){
                        if(StringUtils.containsIgnoreCase(text,words[j])){
                            test[j] = 1;
                        }else{
                            test[j] = 0;
                        }
                    }
                    if(StringUtils.containsIgnoreCase(text,term)){
                        int testSum = ArrayUtils.sum(test, 0, test.length-1);
                        String author = authorIter.nextLine();
                        String time = timeIter.nextLine();
                        if(operator==0 && testSum == test.length){
                            messages.add(new Message(author,time,text));
                        }
                        if(operator==1 && testSum > 0){
                            messages.add(new Message(author,time,text));
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return messages;
    }
    
    public ObservableList<Message> getMessages(String user){
        ObservableList<Message> messages = FXCollections.observableArrayList();
        try {
            File messagesFile = new File(path.toString() + File.separator + "messages.csv");
            LineIterator lineIterator = FileUtils.lineIterator(messagesFile);
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();
                String[] components = splitString(line);
                if(components[0].equals(user)){
                    messages.add(new Message(components[0],components[1],components[2]));
                }
            }
            return messages;
        } catch (IOException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return messages;
    }
    
    public HashSet<String> getAuthors(Event event){
        HashSet<String> authors = new HashSet<>();
        String[] interval = event.getTemporalDescription().split(",");
        int timeSliceA = convertDayToTimeSlice(Double.parseDouble(interval[0]));
        int timeSliceB = convertDayToTimeSlice(Double.parseDouble(interval[1]));
        String term = event.getTextualDescription().split(" ")[0];
        NumberFormat formatter = new DecimalFormat("00000000");
        for(int i = timeSliceA; i <= timeSliceB; i++){
            try {
                File textFile = new File(path+File.separator+preprocessing+File.separator+formatter.format(i)+".text");
                File authorFile = new File(path+File.separator+preprocessing+File.separator+formatter.format(i)+".author");
                LineIterator textIter = FileUtils.lineIterator(textFile);
                LineIterator authorIter = FileUtils.lineIterator(authorFile);
                while(textIter.hasNext()){
                    String text = textIter.nextLine();
                    String author = authorIter.nextLine();
                    if(text.contains(term)){
                        authors.add(author);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return authors;
    }
    
    public int getNumberOfTermsInTimeSlice(int timeSlice){
        int count = 0;
        try {
            NumberFormat formatter = new DecimalFormat("00000000");
            List<String> lines = FileUtils.readLines(new File(path+File.separator+preprocessing+File.separator+formatter.format(timeSlice)+".text"));
            for(String line : lines){
                for(int i = 0; i < line.length(); i++)
                    if(Character.isWhitespace(line.charAt(i))) count++;
                count++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }
    
    public void setTimeSliceLength(){
        String[] components = preprocessing.split("-");
        timeSliceLength = Integer.parseInt(components[3]);
    }
    
    public double convertTimeSliceToDay(int timeSlice){
        double norm = (((double)timeSliceLength)/60.0)/24.0;
        return ((double)timeSlice) * norm;
    }
    
    public int convertDayToTimeSlice(double day){
        double norm = 24*60/(double)timeSliceLength;
        int timeSlice = (int) Math.round(day * norm);
        return timeSlice;
    }
    
    public double getLength(){
        return (end.getTime() - start.getTime())/(1000*60*60*24L)+1;
    }
}
