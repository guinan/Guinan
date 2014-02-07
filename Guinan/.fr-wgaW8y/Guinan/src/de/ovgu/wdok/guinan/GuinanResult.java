package de.ovgu.wdok.guinan;

import java.net.URL;
import java.util.ArrayList;

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
	private ArrayList<String> _content_tags;

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

	/** constant for the video content type */
	public static final String RESOURCE_TYPE_VIDEO = "video";

	/** constant for the slideshwo content type */
	public static final String RESOURCE_TYPE_SLIDESHOW = "slideshow";

	public GuinanResult() {
		this._location = null;
		this._content_tags = null;
		this._rating = 0;
		this._contenttype_tags = null;
		this._documenttype = null;
		this._content = null;
		this._language = null;
	}

	public GuinanResult(String location, ArrayList<String> content_tags,
			double rating, ArrayList<String> contenttype_tags,
			String documenttype, String content, String language) {
		super();
		this._location = location;
		this._content_tags = content_tags;
		this._rating = rating;
		this._contenttype_tags = contenttype_tags;
		this._documenttype = documenttype;
		this._content = content;
		this._language = language;
	}

	public String getLocation() {
		return _location;
	}

	public void setLocation(String string) {
		this._location = string;
	}

	public ArrayList<String> getContent_tags() {
		return _content_tags;
	}

	public void setContent_tags(ArrayList<String> content_tags) {
		this._content_tags = content_tags;
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
