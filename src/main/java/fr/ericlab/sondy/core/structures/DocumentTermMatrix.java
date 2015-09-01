package main.java.fr.ericlab.sondy.core.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Farrokh on 8/26/2015.
 */
public class DocumentTermMatrix implements Serializable {

    private static final long serialVersionUID = -1707325685152959198L;

    private List<String> terms;
    private Map<String, Integer> termsDicitonary;
    private Integer[] numberOfDocuments;

    private List<DocumentTermFrequencyItem> term_doc_freq_id;

    public DocumentTermMatrix() {
        terms = new ArrayList<>();
        numberOfDocuments = new Integer[0];
        term_doc_freq_id = new ArrayList<>();
        ensureDictionary();
    }

    private void ensureDictionary() {
        termsDicitonary = new HashMap<>();
        for(int i=0;i<terms.size();i++)
            termsDicitonary.put(terms.get(i), i);
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
        ensureDictionary();
    }

    public List<String> getTerms() {
        return terms;
    }

    public void setNumberOfDocuments(Integer[] numberOfDocuments) {
        this.numberOfDocuments = numberOfDocuments;
    }

    public Integer[] getNumberOfDocuments() {
        return this.numberOfDocuments;
    }

    public void prepareDocumentTermSize(int size) {
        term_doc_freq_id = new ArrayList<>(size);
    }

    private int getIndex(int term, int doc) {
        try {
            List<Integer> indices = IntStream.range(0, term_doc_freq_id.size())
                .filter(i -> term_doc_freq_id.get(i).doc_id == doc && term_doc_freq_id.get(i).term_id == term)
                .boxed()
                .collect(Collectors.toList());
            if (indices.size() > 1)
                throw new RuntimeException("Problem in the term & document, exists more than once!!!");
            else if (indices.size() == 1)
                return indices.get(0);
            return -1;
        }
        catch (Exception exp) {
            exp.printStackTrace();
            return  -1;
        }
    }

    public int getTermIndex(String term) {
        return termsDicitonary.getOrDefault(term, -1);
    }

    public int setTermDocumentFrequency(String term, int doc, short frequency) {
        return setTermDocumentFrequency(term, doc, frequency, true);
    }

    public int setTermDocumentFrequency(String term, int doc, short frequency, boolean checkExistence) {
        return setTermDocumentFrequency(term, doc, frequency, true, checkExistence);
    }

    public int setTermDocumentFrequency(String term, int doc, short frequency, boolean addNewTerm, boolean checkExistence) {
        int i_term = getTermIndex(term);
        if (i_term == -1) {
            if (addNewTerm) {
                synchronized (terms) {
                    terms.add(term);
                    i_term = terms.size() - 1;
                }
            }
            else
                return -1;
        }
        return setTermDocumentFrequency(i_term, doc, frequency, checkExistence);
    }

    public int setTermDocumentFrequency(int term, int doc, short frequency) {
        return setTermDocumentFrequency(term, doc, frequency, false);
    }

    public int setTermDocumentFrequency(int term, int doc, short frequency, boolean checkExistence) {
        int index = -1;
        if (checkExistence) {
            index = getIndex(term, doc);
            if (frequency > 0) {
                if (index != -1)
                    term_doc_freq_id.set(index, new DocumentTermFrequencyItem(doc, term, frequency));
                else {
                    synchronized (term_doc_freq_id) {
                        term_doc_freq_id.add(new DocumentTermFrequencyItem(doc, term, frequency));
                        index = term_doc_freq_id.size() - 1;
                    }
                }
            } else { // should remove the term from document
                if (index != -1) {
                    synchronized (term_doc_freq_id) {
                        term_doc_freq_id.remove(index);
                        index = -1;
                    }
                }
            }
        }
        else {
            if (frequency > 0) {
                synchronized (term_doc_freq_id) {
                    term_doc_freq_id.add(new DocumentTermFrequencyItem(doc, term, frequency));
                    index = term_doc_freq_id.size() - 1;
                }
            }
        }
        return index;
    }

    public void setTermDocumentFrequencyWithoutCheck(String term, int doc, short frequency) {
        setTermDocumentFrequencyWithoutCheck(getTermIndex(term), doc, frequency);
    }

    public void setTermDocumentFrequencyWithoutCheck(int term, int doc, short frequency) {
        term_doc_freq_id.add(new DocumentTermFrequencyItem(doc, term, frequency));
    }

    public int getTermDocumentFrequency(String term, int doc) {
        int i_term = getTermIndex(term);
        if (i_term == -1)
            return 0;
        return getTermDocumentFrequency(i_term, doc);
    }

    public int getTermDocumentFrequency(int term, int doc) {
        int index = getIndex(term, doc);
        if (index == -1)
            return 0;
        return term_doc_freq_id.get(index).frequency;
    }

    public int getTotalTermFrequency(String term) {
        int index = getTermIndex(term);
        if (index == -1)
            return 0;
        return getTotalTermFrequency(index);
    }

    public int getTotalTermFrequency(int term) {
        return term_doc_freq_id.stream()
            .filter(dti -> dti.term_id == term)
            .mapToInt(dti -> dti.frequency)
            .sum();
    }

    public int getNumberOfDocumentsContainingTerm(String term) {
        int index = getTermIndex(term);
        if (index == -1)
            return 0;
        return getNumberOfDocumentsContainingTerm(index);
    }

    public int getNumberOfDocumentsContainingTerm(int term) {
        return (int)term_doc_freq_id.stream()
            .filter(dti -> dti.term_id == term)
            .count();
    }

    public List<DocumentTermFrequencyItem> getDocumentsContainingTerm(int term) {
        return term_doc_freq_id.stream()
            .filter(dti -> dti.term_id == term)
            .collect(Collectors.toList());
    }

    public Short[] getDocumentsTermFrequency(String term) {
        return getDocumentsTermFrequency(getTermIndex(term));
    }

    public Short[] getDocumentsTermFrequency(int term) {
        Short[] frqs = new Short[numberOfDocuments.length];
        IntStream.range(0, numberOfDocuments.length).forEach(i -> frqs[i] = 0);
        if (term != -1)
            term_doc_freq_id.stream()
                .filter(dti -> dti.term_id == term)
                .forEach(dti -> frqs[dti.doc_id] = dti.frequency);
        return frqs;
    }
}