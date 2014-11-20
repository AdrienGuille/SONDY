/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ericlab.sondy.ui.misc;

import fr.ericlab.sondy.algo.eventdetection.EventDetectionAlgorithm;
import fr.ericlab.sondy.algo.networkanalysis.NetworkAnalysisAlgorithm;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;

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

public class Credits {
    public String htmlContent;
    
    public String getHtml(){
        return htmlContent;
    }
    
    public Credits(){
        htmlContent = 
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "    <head>\n" +
            "        <title></title>\n" +
            "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "    </head>\n" +
            "    <body style='font-family:\"Verdana\"; font-size: 9pt;'>\n" +
            "        <div><b>Main developer</b><br> Adrien GUILLE<br>ERIC Lab, Lumière University Lyon 2<br>adrien.guille@univ-lyon2.fr</b></div>\n" +
            "        <br>\n" +
            "        <div><b>Additional developers</b><br> Yue HE and Falitokiniaina RABEARISON<br>DIS Department, Lumière University Lyon 2</b></div>\n" +
            "        <br>\n" +    
            "        <div><b>References</b></div>\n" +
            "        <ul>\n"+
            "           <li><b>MACD:</b> L. Rong and Y. Qing. Trends analysis of news topics on twitter, <i>In International Journal of Machine Learning and Computing, 2(3)</i> pp. 327–332, 2012</li>";
        ComponentScanner scanner = new ComponentScanner();
        Set<Class<?>> topicClasses = scanner.getClasses(new ComponentQuery() {
            @Override
            protected void query() {
                select().from("fr.ericlab.sondy.algo.detection").returning(allExtending(EventDetectionAlgorithm.class));
            }
        });
        for(Class<?> c : topicClasses){
            try {
                EventDetectionAlgorithm a = (EventDetectionAlgorithm) Class.forName(c.getName()).newInstance();
                htmlContent += a.getReference()+"\n";
            } catch (    InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                Logger.getLogger(Credits.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Set<Class<?>> graphClasses = scanner.getClasses(new ComponentQuery() {
            @Override
            protected void query() {
                select().from("fr.ericlab.sondy.algo.graph").returning(allExtending(NetworkAnalysisAlgorithm.class));
            }
        });
        for(Class<?> c : graphClasses){
            try {
                NetworkAnalysisAlgorithm a = (NetworkAnalysisAlgorithm) Class.forName(c.getName()).newInstance();
                htmlContent += a.getReference()+"\n";
            } catch (    InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                Logger.getLogger(Credits.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        htmlContent += 
            "        </ul>\n" +
            "        <br>\n" +
            "        <div><b>API</b></div>\n" +
            "        <ul>\n" +
            "           <li><b>Stanford CoreNLP:</b> http://nlp.stanford.edu</li>       "+
            "           <li><b>Graphstream:</b> http://graphstream-project.org</li>       "+
            "           <li><b>Lucene:</b> http://lucene.apache.org</li>       "+
            "           <li><b>MysQL/JDBC:</b> http://www.mysql.com/products/connector/</li>       "+
            "        </ul>\n" +
            "    </body>\n" +
            "</html>";
    }
}
