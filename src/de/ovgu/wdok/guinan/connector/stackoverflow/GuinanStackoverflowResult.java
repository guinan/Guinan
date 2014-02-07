package de.ovgu.wdok.guinan.connector.stackoverflow;

import java.util.ArrayList;

import de.ovgu.wdok.guinan.GuinanResult;

public class GuinanStackoverflowResult extends GuinanResult {

	private int question_id;

	public GuinanStackoverflowResult() {
		super();
		this.question_id = 0;
		this.set_thumbnail_uri("http://upload.wikimedia.org/wikipedia/en/9/95/Stack_Overflow_website_logo.png");
		this.setDocumenttype(GuinanResult.RESOURCE_TYPE_TEXT);
	}

	public GuinanStackoverflowResult(String location,
			ArrayList<String> content_tags, double rating,
			ArrayList<String> contenttype_tags, String documenttype,
			String content, String language, ArrayList<String> comments,
			int question_id) {

		super();
		this.set_location(location);
		this.set_content_tags(content_tags);
		this.setRating(rating);
		this.setContent_tags(contenttype_tags);
		this.setDocumenttype(documenttype);
		this.setContent(content);
		this.set_language(language);
		this.setComments(comments);
		this.setQuestion_id(question_id);
		//Stackoverflow doesn not provide thumbnails for the resources themselves, so we use the logo
		this.set_thumbnail_uri("http://upload.wikimedia.org/wikipedia/en/9/95/Stack_Overflow_website_logo.png");
		this.setDocumenttype(GuinanResult.RESOURCE_TYPE_TEXT);

	}

	public int getQuestion_id() {
		return question_id;
	}

	public void setQuestion_id(int question_id) {
		this.question_id = question_id;
	}

}
