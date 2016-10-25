SONDY
=====

An open source social media data mining software (event detection + influence analysis)

Contributors: Adrien Guille, Farrokh Ghamsary

An early version of this software is described in the following paper:

	Adrien Guille, Cécile Favre, Hakim Hacid, Djamel A. Zighed (2013) 
	SONDY: an open source platform for social dynamics mining and analysis.
	In proceedings of the ACM International Conference on Management of Data (SIGMOD 2013),
	pp. 1005-1008, DOI: 10.1145/2463676.2463694

Please cite this paper when using the application.

Getting the binary release
--------------------------

Binary releases of SONDY are available <a href="https://github.com/AdrienGuille/SONDY/releases">on this page</a>. 
SONDY requires Java 8u40+.

Wiki
----

For help about how to use SONDY, take a look at the <a href="https://github.com/AdrienGuille/SONDY/wiki">wiki on GitHub</a>

Features
--------

Data manipulation 

- Data import: Importation of CSV files representing the message stream and the social network of authors 
- Data preparation: Discretization and indexation of the message stream; Tokenization of the messages by unigram, bigram and trigram; Stemming for English, French and Chinese; Lemmatization for English 
- Data filtering: Removing stop-words; Focusing analysis on a sub-period of time 

Event detection 

- Implemented algorithms: MABED (Guille and Favre ASONAM 2014), ET (Parikh and Karlapalem WWW 2013), Trending Scores (Benhardus and Kalita IJWBC vol.9, n.1, 2013), Peaky Topics (Shamma et al. CSCW 2011), Persistent Conversations (Shamma et al. CSCW 2011), EDCoW (Weng and Lee ICWSM 2011)
- Visualization: Automatic generation of timelines; Visualization of trends with MACD (Rong and Qing IJMLC vol2. n.3, 2012); Exploration of the messages related to the detected topics/events; 

Influence analysis 

- Implemented algorithms: Social Capitalists (Dugué and Perez 2014), Log K-Shell Decomposition (Brown and Feng ICWSM 2011), K-Shell Decomposition (Batagelj and Zaversnik ULPPS vol. 40, n.1, 2003), Page Rank (Page et al. WWW 1998), Betweenness Centrality (Freeman 1965)
- Visualization: Interactive visualization of colored graphs; Plotting of rank distribution; Exploration of authors' messages; Visualization of events's activation sequences through the network of authors

Required library
----------------

In order to be able to run SONDY from the sources, you should download the Stanford CoreNLP library at: http://nlp.stanford.edu/software/corenlp.shtml#Download, and add stanford-corenlp-1.3.x-models.jar
into the lib/ directory of SONDY. This file is already included in the binary releases of SONDY.

