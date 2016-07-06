package de.ovgu.wdok.guinan.ontologyconnector.freebase;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
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

import de.ovgu.wdok.guinan.GuinanOntologyResult;
import de.ovgu.wdok.guinan.ontologyconnector.GuinanOntologyConnector;

@Path("freebaseconnector")
public class GuinanFreebaseConnector extends GuinanOntologyConnector {

	/** unique name */
	private final static String CONNECTOR_NAME = "GuinanFreebaseOConnector";

	/** location of GuinanFreebaseOConnector */
	private final static String LOCATION = "http://localhost:8080/Guinan/freebaseconnector";

	/** configuration for the client part (client for the API of Freebase) */
	private ClientConfig config;

	/** client part for API of Freebase */
	private Client client;
	
	private final static String API_KEY = "AIzaSyDFTFgbY0bWbNkWzCLw3E0q22JVE3CEs-U";
	
	/** Web resource representation of Freebase API query endpoint */
	private WebResource freebasesearchloc;
	
	public GuinanFreebaseConnector() {
		super(CONNECTOR_NAME, getBaseURIForFreebaseOConnector());
		this.config = new DefaultClientConfig();
		this.client = Client.create(config);
		// set location of GuinanMaster
		this.masterloc = client.resource(getBaseURIForMaster());
		// set location of Freebase query endoint
		this.freebasesearchloc = client
				.resource(getBaseURIForFreebaseSearch());
	}

	private URI getBaseURIForFreebaseSearch() {
		return UriBuilder.fromUri(
				"https://www.googleapis.com/freebase/v1/search").build();
	}

	private static URI getBaseURIForFreebaseOConnector() {
		return UriBuilder.fromUri(LOCATION + "/query").build();
	}
	
	@GET
	@Path("queryString/")
	@Produces(MediaType.APPLICATION_JSON)
	public String queryString(@QueryParam("q") String query) {
		String response = freebasesearchloc
				.queryParam("query", query)
				.queryParam("key", API_KEY)
				.queryParam("output", "(all)")	// get everything the search api can give us at this point
				.get(String.class);
		
		return response;
	}

	@GET
	@Path("query/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ArrayList<GuinanOntologyResult> query(@QueryParam("q") String query) {
		String response = freebasesearchloc
				.queryParam("query", query)
				.queryParam("key", API_KEY)
				.queryParam("output", "(all)")	// get everything the search api can give us at this point
				.get(String.class);
		System.out.println(response);
		HashMap<String, ArrayList<LinkedHashMap<String, String>>> response_tree = null;
		try {
			response_tree = new ObjectMapper().readValue(response, HashMap.class);
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
		
		if( response_tree == null )
			return null;
		return extractFreebaseResults(response_tree.get("result"));
	}
	
	private ArrayList<GuinanOntologyResult> extractFreebaseResults( ArrayList<LinkedHashMap<String, String>> results ) {
		ArrayList<GuinanOntologyResult> guinanOntologyResults = new ArrayList<GuinanOntologyResult>();
		
		for( LinkedHashMap<String, String> jsonObject: results ) {
			guinanOntologyResults.add( GuinanFreebaseResult.FromLinkedHashMap(jsonObject) );
		}
		
		return guinanOntologyResults;
	}
	
}
