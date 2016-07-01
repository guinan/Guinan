package de.ovgu.wdok.guinan.lli;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;
import com.viceversatech.rdfbeans.annotations.RDF;
import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFSubject;

/**data class for linked learning items (LLI)**/

@RDFBean("http://purl.org/lli/0.1/LLI")
public class LinkedLearningItem {
	
	
	private String uri;
	
	//general
	private String title;
	
	private String language;
	
	private String description;
	
	//technical
	private String technicalLocation;
	
	//educational
	private String educationalInteractivityLevel;
	private String educationalTypicalAgeRange;
	private ArrayList<String> educationalLearningResourceType;
	
	//classification - SFP
	private Model classificationSFP;
	
	

	//getters and setters
	
	public LinkedLearningItem() {
		super();
		this.uri="";
		this.title="";
		this.language="";
		this.description="";
		this.technicalLocation="";
		this.educationalInteractivityLevel="";
		this.educationalTypicalAgeRange="";
		this.educationalLearningResourceType=new ArrayList<>();
	}
	
	

	public LinkedLearningItem(String uri, String title, String language,
			String description, String technicalLocation,
			String educationalInteractivityLevel,
			String educationalTypicalAgeRange,
			ArrayList<String> educationalLearningResourceType, Model classificationSFP) {
		super();
		this.uri = uri;
		this.title = title;
		this.language = language;
		this.description = description;
		this.technicalLocation = technicalLocation;
		this.educationalInteractivityLevel = educationalInteractivityLevel;
		this.educationalTypicalAgeRange = educationalTypicalAgeRange;
		this.educationalLearningResourceType = educationalLearningResourceType;
		this.classificationSFP = classificationSFP;
	}



	@RDFSubject
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@RDF("http://purl.org/dc/terms/title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@RDF("http://purl.org/dc/terms/language")
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	@RDF("http://purl.org/dc/terms/description")
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@RDF("http://ltsc.ieee.org/rdf/lomv1p0/lom#technicalLocation")
	public String getTechnicalLocation() {
		return technicalLocation;
	}

	public void setTechnicalLocation(String technicalLocation) {
		this.technicalLocation = technicalLocation;
	}

	@RDF("http://ltsc.ieee.org/rdf/lomv1p0/lom#educationalTyicalAgeRange")
	public String getEducationalInteractivityLevel() {
		return educationalInteractivityLevel;
	}

	public void setEducationalInteractivityLevel(
			String educationalInteractivityLevel) {
		this.educationalInteractivityLevel = educationalInteractivityLevel;
	}

	@RDF("http://ltsc.ieee.org/rdf/lomv1p0/lom#educationalTypicalAgeRange")
	public String getEducationalTypicalAgeRange() {
		return educationalTypicalAgeRange;
	}

	public void setEducationalTypicalAgeRange(String educationalTypicalAgeRange) {
		this.educationalTypicalAgeRange = educationalTypicalAgeRange;
	}

	@RDF("http://purl.org/dc/terms/type")
	public ArrayList<String> getEducationalLearningResourceType() {
		return educationalLearningResourceType;
	}

	public void setEducationalLearningResourceType(
			ArrayList<String> educationalLearningResourceType) {
		this.educationalLearningResourceType = educationalLearningResourceType;
	}

	@RDF("http://purl.org/lli/sfp")
	public Model getClassificationSFP() {
		return classificationSFP;
	}

	public void setClassificationSFP(Model classificationSFP) {
		this.classificationSFP = classificationSFP;
	}

	
	
	
	
	
}
