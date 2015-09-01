package main.java.fr.ericlab.sondy.core.structures;

import java.io.Serializable;

/**
 * Created by Farrokh on 8/27/2015.
 */
public class DocumentTermFrequencyItem implements Serializable {
    public int doc_id;
    public int term_id;
    public short frequency;

    private static final long serialVersionUID = 6112356138522871333L;

    public DocumentTermFrequencyItem() {
    }

    public DocumentTermFrequencyItem(int doc_id, int term_id, short frequency) {
        this.doc_id = doc_id;
        this.term_id = term_id;
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "DocumentTermFrequencyItem{" +
            "doc_id=" + doc_id +
            ", term_id=" + term_id +
            ", frequency=" + frequency +
            '}';
    }
}