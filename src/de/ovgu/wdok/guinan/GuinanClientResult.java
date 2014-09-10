package de.ovgu.wdok.guinan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.tartarus.snowball.EnglishSnowballStemmerFactory;
import org.tartarus.snowball.ext.porterStemmer;
import org.tartarus.snowball.util.StemmerException;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.graph.GuinanGraph;
import de.ovgu.wdok.guinan.nlp.GuinanInflector;

/**
 * GuinanResult is the result object that is sent back to the calling client. It
 * is basically an extended GuinanResult - a result object being returned from
 * the Connectors. Since GuinanMaster is doing some more computations, we need
 * an extended result object in order to store the additional data.
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * 
 */
public class GuinanClientResult extends GuinanResult {

	/** Array of tags that have been computed from the content **/
	private ArrayList<String> additional_tags;
	/** ArrayList containing content_tags and additional tags**/
	private ArrayList<String> aggregated_tags;
	
	/** ArrayList containing tags that are in both lists **/
	private ArrayList<String> common_tags;
	
	private GuinanGraph ontology_concepts;
	
	
	public GuinanClientResult() {
		super();
	}

	public GuinanClientResult(GuinanResult gr) {
		super(gr.getTitle(), gr.get_location(), gr.get_content_tags(), gr
				.getRating(), gr.getContenttype_tags(), gr.getDocumenttype(),
				gr.getContent(), gr.get_language(), gr.get_thumbnail_uri(), gr
						.getComments());
		

	}

	public GuinanClientResult(String title, String location,
			ArrayList<String> content_tags, double rating,
			ArrayList<String> contenttype_tags, String documenttype,
			String content, String language, String thumbnailuri,
			ArrayList<String> comments) {
		super(title, location, content_tags, rating, contenttype_tags,
				documenttype, content, language, thumbnailuri, comments);
	}

	public ArrayList<String> getAdditional_tags() {
		return additional_tags;
	}

	public void setAdditional_tags(ArrayList<String> additional_tags) {
		for(int i = 0; i<additional_tags.size(); i++){
			additional_tags.set(i, additional_tags.get(i).toLowerCase());
		}
		this.additional_tags = additional_tags;
	}

	public ArrayList<String> getAggregated_tags() {
		return aggregated_tags;
	}

	public void setAggregated_tags(ArrayList<String> aggregated_tags) {
		for(int i = 0; i<aggregated_tags.size(); i++){
			aggregated_tags.set(i, aggregated_tags.get(i).toLowerCase());
		}
		this.aggregated_tags = aggregated_tags;
	}

	public ArrayList<String> getCommon_tags() {
		return common_tags;
	}

	public void setCommon_tags(ArrayList<String> common_tags) {
		this.common_tags = common_tags;
	}
	
	

	

	/**
	 * computes the union of additional_tags and content_tags
	 * 
	 * @return an ArrayList containing the tags from content_tags as well as
	 *         additional tags, duplicates have been eliminated (due to
	 *         datastructure ;-))
	 */
	public ArrayList<String> mergeTags() {
		HashSet<String> tags = new HashSet<String>();
		tags.addAll(this.additional_tags);
		tags.addAll(this.get_content_tags());
		return new ArrayList<String>(tags);
	}

	/**
	 * computes the intersection between additional tags and content_tags
	 * 
	 * @return An ArrayList of strings containing the intersection of
	 *         content_tags and additional tags (so only those tags, which are
	 *         present in both arrays)
	 */
	public ArrayList<String> computeCommonTags() {
		HashSet<String> tags = new HashSet<String>();
		tags.addAll(this.getAdditional_tags());
		tags.retainAll(this.get_content_tags());
		return new ArrayList<String>(tags);
	}
	
	/**
	 * aggregates terms, that are inflections of the same word 
	 * @return HashMap containing only terms, that are no inflected forms of each other, and their number of occurence in the document
	 */
	

	public GuinanGraph getOntology_concepts() {
		return ontology_concepts;
	}

	public void setOntology_concepts(GuinanGraph ontology_concepts) {
		this.ontology_concepts = ontology_concepts;
	}

	/*public void computeTermFrequency() {
		
		for(String term : this.getWordStems()){
			//compute term frequency for content
			int term_in_content = this.countTermOccurrences(this.g@JsonAnySetter
	public void handleUnknown(String key, Object value) {
		// just don't do anything but ignore it
	}etContent(), term);
			int term_in_comments = 0;
			for(String comment: this.getComments()){
				term_in_comments += this.countTermOccurrences(comment, term);
			}
			this.getTerm_frequencies().put(term, term_in_comments+term_in_content);
		}
		
	}
	
	private int countTermOccurrences(String haystack, String needle){
		int lastIndex = 0;
		int count =0;

		//search for the term in the content part
		while(lastIndex != -1){

		       lastIndex = haystack.indexOf(" "+needle+" ",lastIndex);

		       if( lastIndex != -1){
		             count ++;
		             lastIndex+=needle.length();
		      }
		}
		return count;
	}
*/
	@JsonAnySetter
	public void handleUnknown(String key, Object value) {
		// just don't do anything but ignore it
	}
}
