package de.ovgu.wdok.guinan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.tartarus.snowball.ext.englishStemmer;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class ExtendedGuinanResult extends GuinanClientResult {

	private String query;

	/** name of file where the Web document has been saved to */
	private String filename;

	/** found terms and their count */
	private HashMap<Integer, String> wordcount;

	private int totalWordCount;

	private HashMap<String, Integer> stems_numbers;

	private int numberOfuniqueTerms;

	private HashMap<String, Integer> top_terms;

	public ExtendedGuinanResult() {
		super();
		stems_numbers = new HashMap<String, Integer>();
		top_terms = new HashMap<String, Integer>();
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public HashMap<Integer, String> getWordcount() {
		return wordcount;
	}

	public void setWordcount(HashMap<Integer, String> wordcount) {
		this.wordcount = wordcount;
	}

	/*
	 * public ExtendedGuinanResult(GuinanResult gr){
	 * this.set_content_tags(gr.get_content_tags());
	 * this.set_language(gr.get_language());
	 * this.set_location(gr.get_location());
	 * this.set_thumbnail_uri(gr.get_thumbnail_uri()); this.setComments(gr.ge);
	 * }
	 */

	public void setContent(String content) {
		// calling original method of superclass
		super.setContent(content);
		// compute wordcount
		this.computeWordCount();
	}

	public int getNumberOfUniqueTerms() {
		return numberOfuniqueTerms;
	}

	public void setNumberOfUniqueTerms(int uniqueTerms) {
		this.numberOfuniqueTerms = uniqueTerms;
	}

	public int getTotalWordCount() {
		return totalWordCount;
	}

	public void setTotalWordCount(int totalWordCount) {
		this.totalWordCount = totalWordCount;
	}

	public HashMap<String, Integer> getTop_terms() {
		return top_terms;
	}

	public void setTop_terms(HashMap<String, Integer> top_terms) {
		this.top_terms = top_terms;
	}

	/**
	 * for now compute word count for content field. not really the whole
	 * document
	 */
	private void computeWordCount() {
		// tokenize string
		StringTokenizer st = new StringTokenizer(this.getContent(), " ");
		int wc = st.countTokens();

		// count tokens in comments
		for (String c : this.getComments()) {
			st = new StringTokenizer(c, " ");
			wc += st.countTokens();
		}
		this.setTotalWordCount(wc);
	}

	public void computeWordStemsAndOccurrences(ArrayList<String> tags) {

		for (String tag : tags) {

			englishStemmer stemmer = new englishStemmer();
			stemmer.setCurrent(tag);
			if (stemmer.stem()) {
				String base = stemmer.getCurrent();
				// word stem already in list?
				if (stems_numbers.containsKey(base)) {
					int curr_val = stems_numbers.get(base);
					stems_numbers.put(base, curr_val + 1);
				} else {
					stems_numbers.put(base, 1);
				}
			}
		}
		this.setNumberOfUniqueTerms(this.getStems_numbers().size());
		this.computeTopTerms(3);
	}

	public HashMap<String, Integer> getStems_numbers() {
		return stems_numbers;
	}

	public void setStems_numbers(HashMap<String, Integer> stems_numbers) {
		this.stems_numbers = stems_numbers;
	}

	public void computeTopTerms(int threshold) {
		for (Map.Entry<String, Integer> entry : this.getStems_numbers().entrySet()) {
			if (entry.getValue().intValue() >= threshold)
				this.getTop_terms().put(entry.getKey(), entry.getValue());
		}
	}

	@JsonAnySetter
	public void handleUnknown(String key, Object value) {
		// just don't do anything but ignore it
	}

}
