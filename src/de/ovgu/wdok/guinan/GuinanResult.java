package de.ovgu.wdok.guinan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * Result object for the Guinan connectors, wrapping results from
 * connector's Web API into a common result object. <br>
 * The results from a connector are Web resources, described through
 * their URI, title, tags, etc.<br>
 * <br>
 * This class represents basically just data objects with getters and
 * setters, but it doesn't contain any logic of its own.
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.6
 */

public class GuinanResult {

	/** URI of the Web resource */
	private String _location;

	/** Array of tags describing the content of the Web resource */
	private ArrayList<String> original_content_tags;	
	
	

	/** A double value representing the user rating */
	private double _rating;

	/**
	 * Array of tags describing the (educational) content type, e.g. "example",
	 * "introduction", ...
	 */
	private ArrayList<String> _contenttype_tags;

	/** The type of the Web resource, e.g. "video", "slide", "text", ... */
	private String _documenttype;

	/** String describing the content of the Web resource textually */
	private String _content;

	/** the title of the Web resource */
	private String _title;

	/** URI of the Web resource's thumbnail **/
	private String _thumbnail_uri;

	/**
	 * String with ISO country code representing the language of the Web
	 * resource
	 */
	private String _language;
	
	/** ArrayList with comments for a Web Resource, represented as Strings
	 * 
	 */
	private ArrayList<String> comments;

	/** constant for the video content type */
	public static final String RESOURCE_TYPE_VIDEO = "video";

	/** constant for the slideshow content type */
	public static final String RESOURCE_TYPE_SLIDESHOW = "slideshow";
	
	/** constant for the text content type */
	public static final String RESOURCE_TYPE_TEXT = "text";
	
	/** map storing terms from content and comments and their frequencies **/
	private HashMap<String, Integer> term_frequencies;

	public GuinanResult() {
		this._location = null;
		this.original_content_tags = null;
		this._rating = 0;
		this._contenttype_tags = null;
		this._documenttype = null;
		this._content = null;
		this._language = null;
		this.comments = new ArrayList<String>();
		this.term_frequencies = new HashMap<String, Integer>();
	}

	public GuinanResult(String title, String location, ArrayList<String> content_tags,
			double rating, ArrayList<String> contenttype_tags,
			String documenttype, String content, String language, String thumbailuri, ArrayList<String> comments) {
		super();
		this._title = title;
		this._location = location;
		this.original_content_tags = content_tags;
		this._rating = rating;
		this._contenttype_tags = contenttype_tags;
		this._documenttype = documenttype;
		this._content = content;
		this._language = language;
		this._thumbnail_uri=thumbailuri;
		this.comments = comments;
		this.term_frequencies = new HashMap<String, Integer>();
	}

	public String getLocation() {
		return _location;
	}

	public void setLocation(String string) {
		this._location = string;
	}

	public ArrayList<String> getContent_tags() {
		return original_content_tags;
	}

	public void setContent_tags(ArrayList<String> content_tags) {
		this.original_content_tags = content_tags;
	}

	public double getRating() {
		return _rating;
	}

	public void setRating(double rating) {
		this._rating = rating;
	}

	public ArrayList<String> getContenttype_tags() {
		return _contenttype_tags;
	}

	public void setContenttype_tags(ArrayList<String> contenttype_tags) {
		this._contenttype_tags = contenttype_tags;
	}

	public String getDocumenttype() {
		return _documenttype;
	}

	public void setDocumenttype(String documenttype) {
		this._documenttype = documenttype;
	}

	public String getContent() {
		return _content;
	}

	public void setContent(String content) {
		this._content = content;
	}

	public String getTitle() {
		return _title;
	}

	public void setTitle(String _title) {
		this._title = _title;
	}

	public String get_thumbnail_uri() {
		return _thumbnail_uri;
	}

	public void set_thumbnail_uri(String _thumbnail_uri) {
		this._thumbnail_uri = _thumbnail_uri;
	}

	public String get_location() {
		return _location;
	}

	public void set_location(String _location) {
		this._location = _location;
	}

	public String get_language() {
		return _language;
	}

	public void set_language(String _language) {
		this._language = _language;
	}

	public ArrayList<String> getComments() {
		return comments;
	}

	public void setComments(ArrayList<String> comments) {
		this.comments = comments;
	}
	
	public void addComment(String newcomment){
		this.comments.add(newcomment);
	}

	public ArrayList<String> get_content_tags() {
		return original_content_tags;
	}

	public void set_content_tags(ArrayList<String> _content_tags) {
		this.original_content_tags = _content_tags;
	}
	
	public HashMap<String,Integer> getTerm_frequencies() {
		return term_frequencies;
	}

	public void setTerm_frequencies(HashMap<String, Integer> term_frequencies) {
		this.term_frequencies = term_frequencies;
	}

	

	/**
	 * // tell Guinan what to do when it comes across unknown properties during
	 * // JSON POJO mapping // AKA ignore the additional fields of special
	 * Guinan*Result objects
	 * 
	 * @param key
	 *            data field
	 * @param value
	 *            value field
	 */
	@JsonAnySetter
	public void handleUnknown(String key, Object value) {
		// just don't do anything but ignore it
	}
}
