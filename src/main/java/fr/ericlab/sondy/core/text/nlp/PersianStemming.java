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
package main.java.fr.ericlab.sondy.core.text.nlp;

import org.apache.lucene.analysis.fa.PersianNormalizer;

/**
 *
 *   @author Farrokh GHAMSARY, Techlimed
 */
public class PersianStemming implements Stemming {
	PersianNormalizer persianNormalizer;

	public PersianStemming() {
		persianNormalizer = new PersianNormalizer();
	}

	@Override
	public String stem(String word) {
		char[] wordArray = word.toCharArray();
		return new String(wordArray, 0, persianNormalizer.normalize(wordArray, wordArray.length));
	}
}