package de.ovgu.wdok.guinan;

import java.io.StringWriter;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.jena.rdf.model.Model;

import de.ovgu.wdok.guinan.graph.GuinanNode;

/**
 * GuinanOntologyResult is a datastructure for storing/mapping results from
 * Guinan's ontology connectors. An ontology result is basically a
 * semantical concept which will be stored as graph node later. In that way, it extends GuinanNode
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.1
 */

public class GuinanOntologyResult extends GuinanNode{

	private ArrayList<String> sameAsLabels; // what's this?
	private String URI;
	private String description;
	private ArrayList<String> categories;
	
	private Model model;
	
	public GuinanOntologyResult(){
		super("");
	}
	
	public String getURI() {
		return URI;
	}


	public void setURI(String URI) {
		this.URI = URI;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public ArrayList<String> getCategories() {
		return categories;
	}


	public void setCategories(ArrayList<String> categories) {
		this.categories = categories;
	}


	/**
	 * 
	 * @param label primary identifier
	 */
	@JsonCreator
	public GuinanOntologyResult(@JsonProperty("label") String label) {
		super(label);
		this.sameAsLabels = new ArrayList<String>();
	}
		
	
	public ArrayList<String> getSameAsLabels() {
		return sameAsLabels;
	}



	public void setSameAsLabels(ArrayList<String> sameAsLabels) {
		this.sameAsLabels = sameAsLabels;
	}

	public void setRDFModel( Model model )
	{
	  this.model = model;
	}
	
	public Model getRDFModel()
	{
	  return model;
	}
	
	@Override
	public String toString()
	{
	  if( model == null )
		return super.toString();
	  
	  StringWriter writer = new StringWriter();
	  model.write( writer, "JSON-LD" );
	  return writer.toString();
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
