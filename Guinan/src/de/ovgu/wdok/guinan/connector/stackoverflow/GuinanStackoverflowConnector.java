/**
 * 
 */
package de.ovgu.wdok.guinan.connector.stackoverflow;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;

import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.connector.GuinanConnector;

/**
 * GuinanStackoverflowConnector is a special connector that forwards queries
 * from the GuinanMaster to the <a
 * href="https://api.stackexchange.com/">StackOverflow API</a>, transforms the
 * JSON result from Stackoverflow into GuinanResults and sends a list of those
 * results back the master
 * 
 * @author <a href="mailto:katrin.krieger@ovgu.de">Katrin Krieger</a>
 * @version 0.6
 * 
 */
@Path("/stackoverflowconnector/")
public class GuinanStackoverflowConnector extends GuinanConnector {

	/** unique name of connector */
	private final static String SERVICE_NAME = "GuinanStackoverflowConnector";

	/** location of StackoverflowConnector (URI) */
	private final static String LOCATION = "http://localhost:10080/Guinan/stackoverflowconnector";

	/** WebResource representing the Stackoverflow API endpoint */
	private WebResource stackoverflowsearchloc;

	/** API key for Stackoverflow API */
	private String API_KEY = "oe0Kq8xA)4p4QTMb3k*jww((";

	/**
	 * ArrayList with GuinanResults storing the result list of the last request
	 */
	private ArrayList<GuinanStackoverflowResult> resultlist;

	public GuinanStackoverflowConnector() {

		// call constructor of super class, setting the name and endpoint
		super(SERVICE_NAME, getBaseURIForStackoverflowConnector());
		
		client.addFilter(new GZIPContentEncodingFilter(false));

		// set location of the master to provided URI
		this.masterloc = client.resource(getBaseURIForMaster());

		// set endpoint of Stackoverflow API
		this.stackoverflowsearchloc = client
				.resource(getBaseURIForStackoverflowSearch());

		// initialize ArrayList for GuinanResults
		this.resultlist = new ArrayList<GuinanStackoverflowResult>();
	}

	/**
	 * @return URI representing the query endpoint of Stackoverflow API
	 */
	private static URI getBaseURIForStackoverflowSearch() {
		return UriBuilder.fromUri("https://api.stackexchange.com/2.1").build();
	}

	/**
	 * Implementation of query method (overrides (empty) implementation in the
	 * superclass).<br>
	 * The endpoint is set by the @Path annotation. It produces JSON, hence the
	 * produced list of GuinanResults will be serialized to JSON.
	 * 
	 * @param query
	 *            the query string, extracted from the URL containing the
	 *            parameter "q", e.g.
	 *            "http://localhost:10080/Guinan/stackoverflowconnector/query?q=foo"
	 * @return ArrayList containing GuinanResults with the response from the Web
	 *         API
	 * 
	 */
	@GET
	@Path("query/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	// initialize question_ids array with size of response list
	public ArrayList<GuinanResult> query(@QueryParam("q") String query) {
		// query the stackoverflow API endpoint, passing all needed parameters
		// like the API key and the actual query
		// the response will contain the JSON response from the API
		String response = this.stackoverflowsearchloc.path("search")
				.path("advanced").queryParam("filter", "!)S00Zqd)qUFd3vNab3248qIN")
				.queryParam("key", this.API_KEY).queryParam("sort", "votes")
				.queryParam("q", query).queryParam("site", "stackoverflow")
				.get(String.class);

		// build a hashmap where the json_string could be put in
		HashMap<String, ArrayList<LinkedHashMap<String, String>>> json_tree = null;
		try {
			// try to map the JSON response into a hashmap
			json_tree = new ObjectMapper().readValue(response, HashMap.class);
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
		// if the JSON response could be put into the hashmap successfully
		if (json_tree != null) {

			for (Entry<String, ArrayList<LinkedHashMap<String, String>>> e : json_tree
					.entrySet()) {

				// grep items from the JSON response
				// an item represents list of web resources
				if (e.getKey().equals("items")) {
					// make a GuinanResult from the "item"
					return extractGuinanResults(e.getValue());
				}
			}

		}
		// if all else failed, return an empty list
		return new ArrayList<GuinanResult>();
	}

	/**
	 * greps the Web resources from the JSON response and puts it into a list of
	 * GuinanResults
	 * 
	 * @param json_response
	 *            an ArrayList containing linked Hashmaps with key-value-pairs
	 *            (both strings)
	 * @return an ArrayList with GuinanResults
	 */
	private ArrayList<GuinanResult> extractGuinanResults(
			ArrayList<LinkedHashMap<String, String>> json_response) {

		// initialize the result list
		// ArrayList<GuinanResult> grlist = new ArrayList<GuinanResult>();

		// iterate of the linked hashmaps, each representing a web resource
		for (LinkedHashMap<String, ?> e : json_response) {
			GuinanStackoverflowResult gr = new GuinanStackoverflowResult();
			if (e.containsKey("link"))
				gr.set_location((String) e.get("link"));
			if (e.containsKey("score"))
				gr.setRating((double) ((Integer) e.get("score")));
			if (e.containsKey("title"))
				gr.setTitle((String) e.get("title"));
			if (e.containsKey("body"))
				gr.setContent((String) e.get("body"));
			if (e.containsKey("tags")) {
				gr.setContent_tags((ArrayList<String>) (e.get("tags")));
			}
			if (e.containsKey("question_id"))
				gr.setQuestion_id((Integer) e.get("question_id"));
			gr.setDocumenttype("text");
			gr.set_language("EN"); // TODO
			
			if (e.containsKey("answers")) {
				this.extractComments(
						(ArrayList<LinkedHashMap<String, String>>) e
								.get("answers"), gr);
			}
			this.resultlist.add(gr);
		}
		

		// we need to return only GuinanResults, hence we have to create another
		// list to return
		ArrayList<GuinanResult> returnlist = new ArrayList<GuinanResult>();
		returnlist.addAll(this.getResultlist());
		return returnlist;
		// TODO Auto-generated method stub

	}

	/**
	 * Extract the "body" of an "answer" to a Stackoverflow question
	 * 
	 * @param commentsmap
	 *            An ArrayList containing a linked Hashmap with <String,
	 *            String>- key-value pairs
	 */
	private void extractComments(
			ArrayList<LinkedHashMap<String, String>> commentsmap,
			GuinanStackoverflowResult gr) {
		for (LinkedHashMap<String, ?> e : commentsmap) {

			if (e.containsKey("body")){
				
						gr.addComment(Jsoup.parse((String) e.get("body")).text());
			}
		}
	}

	public ArrayList<GuinanStackoverflowResult> getResultlist() {
		return resultlist;
	}

	public void setResultlist(ArrayList<GuinanStackoverflowResult> resultlist) {
		this.resultlist = resultlist;
	}

	/**
	 * @return URI (endpoint) for GuinanStackoverflowConnector
	 * 
	 * */
	private static URI getBaseURIForStackoverflowConnector() {
		return UriBuilder.fromUri(LOCATION + "/query").build();

	}

}
