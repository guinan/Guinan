package de.ovgu.wdok.guinan;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonAnySetter;
/**
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * Oh my, all those hashmaps ...
 */
public class GuinanSemanticGraph extends HashMap<String,HashMap<String,HashMap<String,String>>>{
	
	private HashMap<String,HashMap<String,String>> nodelist;
	private HashMap<String,HashMap<String,String>> edgelist;

	public GuinanSemanticGraph(){
		this.nodelist = new HashMap<String,HashMap<String,String>>();
		this.edgelist = new HashMap<String,HashMap<String,String>>();
	}
	public void addNode(String name, String label, String color, String shape){
		//add only if node with given name is not existent
		HashMap<String,HashMap<String,String>> nodelist = this.getNodes();
		if(!nodelist.containsKey(name)){
			HashMap<String,String> nodedata = new HashMap<String,String>();
			nodedata.put("label", label);
			nodedata.put("color", color);
			nodedata.put("shape", shape);
			nodelist.put(name,nodedata);
		}
		
	}
	
	public HashMap<String,HashMap<String,String>> getNodes(){
		if(!this.containsKey("nodes")){
			HashMap<String,HashMap<String,String>> nodelist = new HashMap<String,HashMap<String,String>>();
			this.put("nodes", edgelist);
		}
		return this.get("nodes");
	}
	
	public HashMap<String,HashMap<String,String>> getEdges(){
		if(!this.containsKey("edges"))
		{
			HashMap<String,HashMap<String,String>> edgelist = new HashMap<String,HashMap<String,String>>();
			this.put("edges", edgelist);
		}
		return this.get("edges");
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
