/**
 * 
 */
package de.ovgu.wdok.guinan.ontologyconnector.freebase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ovgu.wdok.guinan.GuinanOntologyResult;

/**
 * Stores the results that have been extracted by a GuinanFreebaseConnector
 * 
 * @author Peter Herold
 */
public class GuinanFreebaseResult extends GuinanOntologyResult {

	private String mid;
	private String id;
	private String name;
	private String notableName;
	private String notableId;
	private String lang;
	private Double score;
	
	private HashMap<String, ArrayList<String>> extraData;
	
	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNotableName() {
		return notableName;
	}

	public void setNotableName(String notableName) {
		this.notableName = notableName;
	}

	public String getNotableId() {
		return notableId;
	}

	public void setNotableId(String notableId) {
		this.notableId = notableId;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	/**
	 * @param label primary identifier
	 */
	public GuinanFreebaseResult(String label) {
		super(label);

		extraData = new HashMap<String, ArrayList<String>>();
	}

	public void putExtraData( String key, ArrayList<String> value ) {
		extraData.put(key, value);
	}
	
	public ArrayList<String> getExtraData( String key ) {
		return extraData.get(key);
	}
	
	public ArrayList<String> removeExtraData( String key ) {
		return extraData.remove(key);
	}
	
	@Override
	public String toString() {
		String extraDataString = "[";
		for(String key: extraData.keySet()) {
			extraDataString += "{\""+key+"\":"+extraData.get(key).toString()+"},";
		}
		extraDataString += "]";
		
		return 	"{\"label\":\""+getLabel()+
				"\",\"mid\":\""+getMid()+
				"\",\"id\":\""+getId()+
				"\",\"name\":\""+getName()+
				"\",\"notableName\":\""+getNotableName()+
				"\",\"notableId\":\""+getNotableId()+
				"\",\"lang\":\""+getLang()+
				"\",\"score\":\""+getScore().toString()+
				"\",\"extraData\":"+extraDataString+"}";
	}
	
	public static GuinanFreebaseResult FromLinkedHashMap( LinkedHashMap<String, ?> source ) {
		String label = (String) source.get("id");
		GuinanFreebaseResult gfr = new GuinanFreebaseResult(label);
		
		gfr.setMid( (String) source.get("mid") );
		gfr.setId( (String) source.get("id") );
		gfr.setName( (String) source.get("name") );
		gfr.setLang( (String) source.get("lang") );
		gfr.setScore( (Double) source.get("score") );
				
		LinkedHashMap<String, String> notables = (LinkedHashMap<String, String>) source.get("notable");
		if( notables != null ) {
			gfr.setNotableId(notables.get("id"));
			gfr.setNotableName(notables.get("name"));
		}
		
		// now process the output list, that is all the extra we could get. Since this is different for each object we'll stuff it all into a hashmap for now
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<?>>> output_tree = (LinkedHashMap<String, LinkedHashMap<String, ArrayList<?>>>) source.get("output");
		
		if( output_tree != null ) {
			// traverse all entries in /output/all
			for( String key: output_tree.get("all").keySet() ) {
				gfr.putExtraData(key, (ArrayList<String>) output_tree.get("all").get(key));
			}
		}
		
		return gfr;
	}
}
