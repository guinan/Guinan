package de.ovgu.wdok.guinan.connector.delicious;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.connector.GuinanConnector;

@Path("deliciousconnector")
public class GuinanDeliciousConnector extends GuinanConnector {

	/** unique name of connector */
	private final static String SERVICE_NAME = "GuinanDeliciousConnector";

	/** location of DeliciousConnector (URI) */
	private final static String LOCATION = "http://localhost:8080/Guinan/deliciousconnector";

	

	public GuinanDeliciousConnector() {
		// call constructor of super class, setting the name and endpoint
		super(SERVICE_NAME, getBaseURIForDeliciousConnector());
		
		// set location of the master to provided URI
		this.masterloc = client.resource(getBaseURIForMaster());
	}

	private static URI getQueryURIForDeliciousSearch(String query) {
		return UriBuilder
				.fromPath(getBaseURIForDeliciousSearch() + "/" + query).build();
	}

	/**
	 * @return URI representing the query endpoint of Delicious API
	 */
	private static URI getBaseURIForDeliciousSearch() {
		return UriBuilder.fromUri("http://feeds.delicious.com/v2/json/tag/")
				.build();
	}

	/**
	 * @return URI (endpoint) for GuinanDeliciousConnector
	 * 
	 * */
	private static URI getBaseURIForDeliciousConnector() {
		return UriBuilder.fromPath(LOCATION + "/query").build();
	}

	@GET
	@Path("query/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ArrayList<GuinanResult> query(@QueryParam("q") String query) {
		query = query.replaceAll("[^\\w]+", "+");
		System.out.println("query: "+query);
		// TODO maybe replace whitespaces between keywords in query string with
		// "+" for OR search
		// TODO don't query more often than about once every second. Also Filter
		// response 500 for spam blocking
		WebResource searchLoc = client
				.resource(getQueryURIForDeliciousSearch(query));
		String response = searchLoc.get(String.class);

		// build a hashmap where the json_string could be put in
		ArrayList<LinkedHashMap<String, String>> jsonList = null;
		try {
			// try to map the JSON response into a hashmap
			jsonList = new ObjectMapper().readValue(response, ArrayList.class);
		}

		catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (jsonList != null) {
			return extractGuinanResults(jsonList);
		}

		return new ArrayList<GuinanResult>();
	}
	
	private ArrayList<GuinanResult> extractGuinanResults(ArrayList<LinkedHashMap<String, String>> jsonList) {
		
		//initialize the result list
		ArrayList<GuinanResult> grlist = new ArrayList<GuinanResult>();

		//iterate of the linked hashmaps, each representing a web resource
		for (LinkedHashMap<String, ?> e : jsonList) {
			GuinanResult gr = new GuinanResult();
			if (e.containsKey("u"))
				gr.set_location((String) e.get("u"));
//			if (e.containsKey("score"))
//				gr.setRating((double) ((Integer) e.get("score")));
			if (e.containsKey("d"))
				gr.setTitle((String) e.get("d"));
//			if (e.containsKey("body"))
//				gr.setContent((String) e.get("body"));
			if (e.containsKey("t")) {
				gr.setContent_tags((ArrayList) (e.get("t")));
			}
			gr.setDocumenttype("text");
			gr.set_language("EN"); //TODO
			grlist.add(gr);
		}
		return grlist;
		// TODO Auto-generated method stub

	}
}
