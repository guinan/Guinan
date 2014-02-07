package de.ovgu.wdok.guinan.graph;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = PropertyValueSerializer.class)
public class GuinanNode {

	private String label; //key
	private HashMap<String,String> data; //k-v pairs
	

	

	public GuinanNode(String label, HashMap<String, String> data) {
		
		this.label = label;
		this.data = data;
		System.out.println(data.toString());
	}
	
	public GuinanNode(String label) {
		
		this.label = label;
	}


	public void putData(HashMap<String,String> data){
		this.data=data;
	}




	public void setData(HashMap<String, String> data) {
		this.data = data;
	}


	

	public String getLabel() {
		return this.label;
	}


	
	public HashMap<String, String> getData() {
		return this.data;
	}

	@JsonValue
	public String toString() {
	    return getLabel() + ":" + datatoJSONString();
	}
	
	public String datatoJSONString(){
		String jsonstring = "{";
		for (Map.Entry<String, String> entry : this.getData().entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    jsonstring+=" \'"+key + "\' : \'"+value+"\' ,";
		}
		//remove last comma and replace with } TODO
		
		return jsonstring+"}";
	}
	
}
