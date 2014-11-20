/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection;

import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.algo.eventdetection.mabed.MABEDTimeInterval;
import fr.ericlab.sondy.algo.eventdetection.mabed.MABEDTopic;
import fr.ericlab.sondy.algo.eventdetection.mabed.MABEDTopicGraph;
import fr.ericlab.sondy.algo.eventdetection.mabed.MABEDTopicList;
import fr.ericlab.sondy.algo.eventdetection.mabed.MABEDWeightedTerm;
import fr.ericlab.sondy.core.DataManipulation;
import fr.ericlab.sondy.core.access.DBAccess;
import fr.ericlab.sondy.core.access.IndexAccess;
import fr.ericlab.sondy.core.access.MentionIndexAccess;
import fr.ericlab.sondy.core.structure.DetectionResult;
import fr.ericlab.sondy.core.structure.TermInfo;
import fr.ericlab.sondy.core.structure.list.TermInfoList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
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
final public class MABED extends EventDetectionAlgorithm{

    double minTermSupport = 0.0;
    double maxTermSupport = 1.0;
    int nbEvents = 10;
    int nbRelatedTerms = 10;
    
    int[] distribution;
    DataManipulation dataManipulation;
    
    // algo
    double maximumScore;
    static int _SMOOTH_ = 6;
    static Double _SIGMA_ = 0.7;
    static Double _THETA_ = 0.7;
    static int _MIN_RELATED_WORDS_ = 2;
    public String info;
    
    // results
    public MABEDTopicList topics;
    public MABEDTopicGraph topicGraph;
    
    IndexAccess indexAccess;
    DBAccess dbAccess;
    
    @Override
    public ObservableList<DetectionResult> apply(){
        if(parameters.get(0).getValue()!=null && !parameters.get(0).getValue().equals("")){
            minTermSupport = Double.parseDouble(parameters.get(0).getValue());
        }
        if(parameters.get(1).getValue()!=null && !parameters.get(1).getValue().equals("")){
            maxTermSupport = Double.parseDouble(parameters.get(1).getValue());
        }
        if(parameters.get(4).getValue()!=null && !parameters.get(4).getValue().equals("")){
            nbEvents = Integer.parseInt(parameters.get(4).getValue());
        }
        long startNanoTime = System.nanoTime();
        dataManipulation = new DataManipulation();
        distribution = dataManipulation.getDistribution(appVariables);
        indexAccess = new IndexAccess(appVariables);

        // Get basic topics
        MABEDTopicList basicTopics = getSimpleTopics((int)(minTermSupport*appVariables.nbMessages), (int)(maxTermSupport*appVariables.nbMessages));
        basicTopics.sort();
        
        // Get final topics
        int nbFinalTopics = 0;
        int i = 0;
        if(basicTopics.size() > 0){
            topicGraph = new MABEDTopicGraph(basicTopics.get(0).score, _SIGMA_);
            dbAccess = new DBAccess();
            dbAccess.initialize(appVariables, false);
            while(nbFinalTopics < nbEvents && i < basicTopics.size()){
                MABEDTopic topic = getRefinedTopic(basicTopics.get(i), nbRelatedTerms);
                if(topic.relatedTerms.size() >= _MIN_RELATED_WORDS_){
                    nbFinalTopics += topicGraph.addTopic(topic);
                }
                i++;
            }
            mergeRedundantTopics(topicGraph);
            topics = topicGraph.toTopicList();
            results = FXCollections.observableArrayList();
            float intervalDuration = ((float) appVariables.getCurrentDatasetInterval())/60;
            for(MABEDTopic t : topics.list){
                float startDay = (t.I.timeSliceA*intervalDuration)/24;
                float endDay = (t.I.timeSliceB*intervalDuration)/24;
                results.add(new DetectionResult(t.mainTerm+" "+t.relatedTermAsList(),formatter.format(startDay)+";"+formatter.format(endDay)));
            }
            indexAccess.close();
        }
        long endNanoTime = System.nanoTime();
        long elapsedNanoTime = endNanoTime - startNanoTime;
        double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
        appVariables.addLogEntry("[event detection] computed MABED, minTermSupport="+minTermSupport+", maxTermSupport="+maxTermSupport+". "+results.size()+" results in "+formatter.format(elaspedSecondTime)+"s");
        return results;
    }
    
    float expectation(int timeSlice, float tmf){
        return distribution[timeSlice]*(tmf/(float)appVariables.nbMessages);
    }
    
    float anomaly(float expectation, float beta){
        return beta - expectation;
    }
    
    float getErdemCoefficient(float[] ref, float[] comp, int a, int b){
        float scores1[] = new float[b-a+1], scores2[] = new float[b-a+1]; 
        for(int i = a; i <= b; i++){
            scores1[i-a] = ref[i];
            scores2[i-a] = comp[i];
        }
        float result = 0;
        float A12 = 0, A1 = 0, A2 = 0;
        for(int i=2;i<scores1.length;i++){
            A12 += (scores1[i]-scores1[i-1])*(scores2[i]-scores2[i-1]);
            A1 += (scores1[i]-scores1[i-1])*(scores1[i]-scores1[i-1]);
            A2 += (scores2[i]-scores2[i-1])*(scores2[i]-scores2[i-1]);
        }
        A1 = (float) Math.sqrt(A1/(scores1.length-1));
        A2 = (float) Math.sqrt(A2/(scores1.length-1));
        result = A12/((scores1.length-1)*A1*A2);
        return (result+1)/2;
    }
                        
    MABEDTopic getRefinedTopic(MABEDTopic simpleTopic, int nbrelatedTerms){
        MABEDTopic refinedTopic = new MABEDTopic();
        String [] frequentTerms = new String[nbrelatedTerms];
        try {
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            RAMDirectory temporaryIndex = new RAMDirectory();
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            IndexWriter temporaryWriter = new IndexWriter(temporaryIndex, config);
            Document doc = new Document();
            doc.add(new Field("content", dbAccess.getMessagesAsString(appVariables, simpleTopic.mainTerm, simpleTopic.I.timeSliceA, simpleTopic.I.timeSliceB), Field.Store.YES, Field.Index.ANALYZED,Field.TermVector.YES));
            temporaryWriter.addDocument(doc);
            temporaryWriter.commit();
            IndexReader temporaryReader = IndexReader.open(temporaryWriter, true);
            TermEnum allTerms = temporaryReader.terms();
            int minFreq = 0;
            TermInfoList termList = new TermInfoList();
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(!term.equals(simpleTopic.mainTerm) && term.length()>1 && !appVariables.isStopWord(term)){
                    int cf = IndexAccess.getTermOccurenceCount(temporaryReader, term);
                    if(cf>minFreq){
                        termList.addTermInfo(new TermInfo(term,(int)cf));
                        termList.sortList();
                        if(termList.size() > nbrelatedTerms){
                            termList.removeLast();
                        }
                        minFreq = termList.get(termList.size()-1).occurence;
                    }
                }
            }
            for(int i = 0; i < termList.size() && i < nbrelatedTerms; i++){
                frequentTerms[i] = termList.get(i).text;
            }
            temporaryWriter.close();
            temporaryReader.close();
            temporaryIndex.close();
            
            float ref[] = indexAccess.getTermFrequency(appVariables, simpleTopic.mainTerm);
            float comp[];
            refinedTopic = new MABEDTopic(simpleTopic.mainTerm, simpleTopic.I, simpleTopic.score, simpleTopic.anomaly);
            for(int j = 0; j < nbrelatedTerms && frequentTerms[j] != null; j++){
                comp = indexAccess.getTermFrequency(appVariables, frequentTerms[j]);
                double w = getErdemCoefficient(ref, comp, simpleTopic.I.timeSliceA, simpleTopic.I.timeSliceB);
                if(w >= _THETA_){
                    refinedTopic.relatedTerms.add(new MABEDWeightedTerm(frequentTerms[j],w));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MABED.class.getName()).log(Level.SEVERE, null, ex);
        }
        return refinedTopic;
    }
        
    MABEDTopicList getSimpleTopics(int minTermOccur, int maxTermOccur){
        MABEDTopicList simpleTopics = new MABEDTopicList();
        int m = appVariables.messageSet.nbTimeSlice;
        try {
            IndexReader r = indexAccess.reader;
            TermEnum allTerms = r.terms();
            MentionIndexAccess mentionIndexAccess = new MentionIndexAccess(appVariables);
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>2 && !appVariables.isStopWord(term)){
                    TermDocs termDocs = r.termDocs(allTerms.term());
                    float alpha[], beta[];
                    alpha = indexAccess.getTermFrequency(appVariables, termDocs);
                    beta = mentionIndexAccess.getTermFrequency(appVariables, term);
                    float tmf = beta[m];
                    float tgf = alpha[m];
                    if(tgf > minTermOccur && tgf < maxTermOccur){
                        float expectation;
                        if(_SMOOTH_>0){
                            beta = dataManipulation.getSmoothedTermFrequency(beta, _SMOOTH_);
                        }
                        float scoreSequence[] = new float[m];
                        for(int i = 0; i < m; i++){
                            expectation = expectation(i,tmf);
                            scoreSequence[i] = anomaly(expectation, beta[i]);
                        }
                        LinkedList<MABEDTimeInterval> I = new LinkedList<>();
                        LinkedList<Float> L = new LinkedList<>();
                        LinkedList<Float> R = new LinkedList<>();
                        ArrayList<Float> anomaly = new ArrayList<>();
                        for(int i = 0; i < m; i++){
                            anomaly.add(scoreSequence[i]>0.0?scoreSequence[i]:0);
                            if(scoreSequence[i]>0){
                                int k = I.size();
                                float Lk = 0, Rk = DataManipulation.sum(scoreSequence,0,i);
                                if(i>0){
                                    Lk = DataManipulation.sum(scoreSequence,0,i-1);
                                }
                                int j = 0;
                                boolean foundJ = false;
                                for(int l=k-1; l>=0 && !foundJ; l--){
                                    if(L.get(l)<Lk){
                                        foundJ = true;
                                        j = l;
                                    }
                                }
                                if(foundJ && R.get(j)<Rk){
                                     MABEDTimeInterval Ik = new MABEDTimeInterval(I.get(j).timeSliceA,i);
                                     for(int p = j; p<k; p++){
                                         I.removeLast();
                                         L.removeLast();
                                         R.removeLast();
                                     }
                                     k = j;
                                     I.add(Ik);
                                     L.add(DataManipulation.sum(scoreSequence,0,Ik.timeSliceA-1));
                                     R.add(DataManipulation.sum(scoreSequence,0,Ik.timeSliceB));
                                }else{
                                    I.add(new MABEDTimeInterval(i,i));
                                    L.add(Lk);
                                    R.add(Rk);
                                }
                            }
                        }
                        if(I.size()>0){
                            MABEDTimeInterval maxI = I.get(0);
                            for(MABEDTimeInterval Ii : I){
                                if(DataManipulation.sum(scoreSequence,Ii.timeSliceA,Ii.timeSliceB)>DataManipulation.sum(scoreSequence,maxI.timeSliceA,maxI.timeSliceB)){
                                    maxI.timeSliceA = Ii.timeSliceA;
                                    maxI.timeSliceB = Ii.timeSliceB;
                                }
                            }
                            double score = DataManipulation.sum(scoreSequence,I.get(0).timeSliceA,I.get(0).timeSliceB);
                            simpleTopics.add(new MABEDTopic(term,maxI,score,anomaly));
                        }
                    }
                }
            }
            mentionIndexAccess.close();
        } catch (IOException ex) {
            Logger.getLogger(MABED.class.getName()).log(Level.SEVERE, null, ex);
        }
        simpleTopics.sort();
        return simpleTopics;
    }
    
    void mergeRedundantTopics(MABEDTopicGraph topicGraph){
        topicGraph.identifyConnectedComponents();
    }

    @Override
    public String getName() {
        return "MABED";
    }
    
    public MABED() {
        super();
        parameters = FXCollections.observableArrayList(new AlgorithmParameter("minTermSupport",""),new AlgorithmParameter("maxTermSupport",""), new AlgorithmParameter("theta",""), new AlgorithmParameter("sigma",""), new AlgorithmParameter("events",""));
        algoDescription = "Mention-anomaly-based event detection";
    }

    @Override
    public String getReference() {
        return "<li><b>MABED:</b> Guille A. and Favre C. Mention-anomaly-based Event Detection in Twitter, <i>In Proceedings of the 2014 IEEE/ACM International Conference on Advances in Social Network Analysis and Mining</i>, 2014</li>";
    }
}
