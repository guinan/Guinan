package de.ovgu.wdok.guinan.educ;

import java.util.ArrayList;

/**
 * 
 * @author kkrieger
 *	Data object for educational metadata
 */

public class EducationalMetaData {

	String language;
	String interactivity_type;
	String interactivity_level;
	ArrayList <String> learning_resource_type;
	String title;
	String description;
	String age_range;
	final static String RESOURCETYPE_TABLE="table";
	final static String RESOURCETYPE_FAQ="FAQ";
	final static String RESOURCETYPE_CODE="code";
	

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getInteractivity_type() {
		return interactivity_type;
	}

	public void setInteractivity_type(String interactivity_type) {
		this.interactivity_type = interactivity_type;
	}

	/*public String getSemantic_density() {
		return semantic_density;
	}

	public void setSemantic_density(String semantic_density) {
		this.semantic_density = semantic_density;
	}*/

	public String getInteractivity_level() {
		return interactivity_level;
	}

	public void setInteractivity_level(String interactivity_level) {
		this.interactivity_level = interactivity_level;
	}

	

	public ArrayList <String> getLearning_resource_type() {
		return learning_resource_type;
	}

	public void setLearning_resource_type(ArrayList<String> learning_resource_type) {
		this.learning_resource_type = learning_resource_type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAge_range() {
		return age_range;
	}

	public void setAge_range(String age_range) {
		this.age_range = age_range;
	}

	
}
