/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.algo.eventdetection;

import cc.mallet.util.Util;
import fr.ericlab.sondy.algo.AlgorithmParameter;
import fr.ericlab.sondy.algo.eventdetection.onlinelda.OnlineLDATopic;
import fr.ericlab.sondy.algo.eventdetection.onlinelda.OnlineLDATopicList;
import fr.ericlab.sondy.core.structure.Collection;
import fr.ericlab.sondy.core.structure.DetectionResult;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

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

public class OnlineLDA {
    
//    int numTopic = 50;
//    int numTerms = 8;
//    
//    public String getName(){
//        return "Online LDA";
//    }
//    
//    public ObservableList<DetectionResult> apply() {        
//        try {
//            
//            if(parameters.get(0).getValue()!=null && !parameters.get(0).getValue().equals("")){
//                numTopic = Integer.parseInt(parameters.get(0).getValue());
//            }
//            if(parameters.get(1).getValue()!=null && !parameters.get(1).getValue().equals("")){
//                numTerms = Integer.parseInt(parameters.get(1).getValue());
//            }
//            long startNanoTime = System.nanoTime();           
//            HashMap<DetectionResult,Float> score = new HashMap<>();
//            
//            String baseInputPath = appVariables.configuration.getWorkspace()+"/datasets/"+appVariables.currentDatasetText.getText()+"/"+appVariables.getCurrentDatasetDiscretization();
//            String baseAlgoPath = appVariables.configuration.getWorkspace()+"/algorithms";
//            String launchScript = "#!/bin/bash\n" +
//                "\n" +
//                "#parameters\n" +
//                "num_topics="+numTopic+"\n" +
//                "\n" +
//                "#globals\n" +
//                "dates=`ls "+baseInputPath+"/input/ | cut -f 1-1 -d \\. | sort | uniq`\n" +
//                "first=1\n" +
//                "\n" +
//                "prev=\"\"\n" +
//                "for date in $dates\n" +
//                "do\n" +
//                "    echo \"processing datetime = $date (previous datetime = $prev)\"\n" +
//                "    if [ $first -eq 1 ]\n" +
//                "    then\n" +
//                "        #run the first one without online\n" +
//                "        out_dir=`echo $date | cut -f 2-2 -d-`\n" +
//                "        time python "+baseAlgoPath+"/lda.py -f "+baseInputPath+"/input/$date.text -t "+baseInputPath+"/input/$date.time -o "+baseAlgoPath+"/output-$out_dir -k $num_topics\n" +
//                "        prev=$out_dir\n" +
//                "    else\n" +
//                "        time python "+baseAlgoPath+"/lda.py -f "+baseInputPath+"/input/$date.text -t "+baseInputPath+"/input/$date.time \\\n" +
//                "            -m "+baseAlgoPath+"/output-$prev/model.dat -o "+baseAlgoPath+"/output-$date\n" +
//                "        prev=$date\n" +
//                "    fi\n" +
//                "    \n" +
//                "    first=0\n" +
//                "done";
//            File shellScript = new File(baseAlgoPath+"/launchOnlineLDA.sh");
//            FileUtils.writeStringToFile(shellScript,launchScript,Charset.forName("UTF-8"),false);
//            
//            ProcessBuilder pb = new ProcessBuilder("launchOnlineLDA.sh");
//            pb.directory(new File(baseAlgoPath));
//            Process p = pb.start();
//            
//            OnlineLDATopicList topics;
//            LinkedList<String> outputList = new LinkedList<>();
//            File dir = new File(baseInputPath+"/output/");
//            if(dir.exists()){
//                String[] files = dir.list(DirectoryFileFilter.INSTANCE);
//                outputList.addAll(Arrays.asList(files));
//            }
//            String topicsStr = "";
//            DecimalFormat df = new DecimalFormat("0.00");
//            float intervalDuration = ((float) appVariables.getCurrentDatasetInterval())/60;
//            topics = new OnlineLDATopicList();
//            for(int i = appVariables.startTimeSlice; i <= appVariables.endTimeSlice; i++){
//                OnlineLDATopicList topicList = new OnlineLDATopicList();
//                LineIterator it0 = null, it1 = null;
//                try {
//                    it0 = FileUtils.lineIterator(new File(baseInputPath+"/output/"+outputList.get(i)+"/topics.txt"), "UTF-8");
//                    it1 = FileUtils.lineIterator(new File(baseInputPath+"/output/"+outputList.get(i+1)+"/topics.txt"), "UTF-8");
//                    int topicId;
//                    while (it0.hasNext()) {
//                        it0.nextLine();
//                        it1.nextLine();
//                        if(it0.hasNext()){
//                            topicId = Integer.parseInt(it0.nextLine().split(": ")[1]);
//                            it1.nextLine();
//                            if(it0.hasNext()){
//                                HashSet<String> allWords = new HashSet<>();
//                                HashMap<String,Double> distTopic0Map = new HashMap<>(), distTopic1Map = new HashMap<>();
//                                String topic0[] = it0.nextLine().split(" "), topic1[] = it1.nextLine().split(" ");
//                                for(int j = 0; j < topic0.length; j++){
//                                    String split0[] = topic0[j].split(":0."), split1[] = topic1[j].split(":0.");
//                                    allWords.add(split0[0]);
//                                    distTopic0Map.put(split0[0],Double.parseDouble("0."+split0[1]));
//                                    allWords.add(split1[0]);
//                                    distTopic1Map.put(split1[0],Double.parseDouble("0."+split1[1]));
//                                }
//                                double distTopic0Array[] = new double[allWords.size()], distTopic1Array[] = new double[allWords.size()];
//                                int k =0;
//                                for(String word : allWords){
//                                    distTopic0Array[k] = (distTopic0Map.get(word) == null)?0:distTopic0Map.get(word);
//                                    distTopic1Array[k] = (distTopic1Map.get(word) == null)?0:distTopic1Map.get(word);
//                                    k++;
//                                }
//                                double e = Math.sqrt(Util.jensenShannonDivergence(distTopic0Array, distTopic1Array));
//                                if(e > 0.1){
//                                    String burstyTopic = "topic #"+topicId+"("+e+")";
//                                    OnlineLDATopic topic = new OnlineLDATopic();
//                                    for(int l = 0; l < numTerms; l++){
//                                        String split[] = topic1[l].split(":0.");
//                                        String word = split[0];
//                                        String proba = (split[1].length()>3)?split[1].substring(0,3):split[1];
//                                        burstyTopic += word + " (0." + proba + "), ";
//                                        topic.terms.add(word);
//                                        topic.probabilities.add(Double.parseDouble("0."+proba));
//                                        topic.timeSlice = i;
//                                        topic.score = e;
//                                        topic.id = topicId;
//                                    }
//                                    topicList.list.add(topic);
//                                    topics.list.add(topic);
//                                    float peakDay = (i*intervalDuration)/24;
//                                    float peakDay1 = ((i+1)*intervalDuration)/24;
//                                    score.put(new DetectionResult(topic.printTerms(numTerms),formatter.format(peakDay)+";"+formatter.format(peakDay1)),(float)e);
//                                }
//                            }
//                        }
//                        
//                    }
//                } catch (IOException ex) {                    
//                    Logger.getLogger(OnlineLDA.class.getName()).log(Level.SEVERE, null, ex);
//                } finally {
//                    LineIterator.closeQuietly(it0);
//                    LineIterator.closeQuietly(it1);
//                }
//                topicList.sort();
//                double sum = 0;
//                for(int j = 0; (j < 10) & (j <topicList.list.size()); j++){
////                    System.out.println(topicList.list.get(j).id+"("+topicList.list.get(j).score+"): "+dataset.toDate(i)+": "+topicList.list.get(j).printTerms(9));
//                    sum += topicList.list.get(j).score;
//                }
//                System.out.println(i+" "+sum/10);
//            }
//            topics.sort();
//            score = Collection.getSortedMapDesc(score);
//            Set<Entry<DetectionResult, Float>> entrySet = score.entrySet();
//            results = FXCollections.observableArrayList();  
//            for (Entry<DetectionResult, Float> entry : entrySet) {
//                results.add(0,entry.getKey());
//            }
//            long endNanoTime = System.nanoTime();
//            long elapsedNanoTime = endNanoTime - startNanoTime;
//            double elaspedSecondTime = (double)elapsedNanoTime/(double)1000000000;
//            appVariables.addLogEntry("[detection] computed online LDA, numTopics="+numTopic+", numTerms="+numTerms+", "+results.size()+" results in "+formatter.format(elaspedSecondTime)+"s");
//            return results;
//        } catch (IOException ex) {
//            Logger.getLogger(OnlineLDA.class.getName()).log(Level.SEVERE, null, ex);
//            return null;
//        }
//    }
//
//    public OnlineLDA() {
//        super();
//        parameters = FXCollections.observableArrayList(new AlgorithmParameter("numTopics",""),new AlgorithmParameter("numTerms",""));
//        algoDescription = "A topic modeling-based methodology to track emerging events in microblogs";
//    }
//
//    @Override
//    public String getReference() {
//        return "<li><b>On-line LDA:</b> J H Lau, N Collier, T Baldwin. On-line Trend Analysis with Topic Models: #twitter Trends Detection Topic Model Online, <i>In Proceedings of the 24th International Conference on Computational Linguistics</i>, pp. 1519-1534, 2012</li>";
//    }
}
