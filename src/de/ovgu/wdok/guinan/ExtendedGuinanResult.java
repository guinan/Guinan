package de.ovgu.wdok.guinan;

import java.util.HashMap;
import java.util.StringTokenizer;

public class ExtendedGuinanResult extends GuinanResult {

	/** name of file where the Web document has been saved to*/
	private String filename; 
	
	/** found terms and their count */
	private HashMap<Integer,String> wordcount;
	
	private int totalWordCount;

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
	
	
	/*public ExtendedGuinanResult(GuinanResult gr){
		this.set_content_tags(gr.get_content_tags());
		this.set_language(gr.get_language());
		this.set_location(gr.get_location());
		this.set_thumbnail_uri(gr.get_thumbnail_uri());
		this.setComments(gr.ge);
	}*/
	
	
	public void setContent(String content){
		//calling original method of superclass
		this.setContent(content);
		//compute wordcount
		this.computeWordCount();
	}
	public int getTotalWordCount() {
		return totalWordCount;
	}

	public void setTotalWordCount(int totalWordCount) {
		this.totalWordCount = totalWordCount;
	}

	/**
	 * for now compute word count for content field. not really the whole document
	 */
	private void computeWordCount(){
		//tokenize string
		StringTokenizer st = new StringTokenizer(this.getContent(), " ");
		this.setTotalWordCount(st.countTokens());
	}
}
