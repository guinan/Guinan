package de.ovgu.wdok.guinan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.spi.resource.Singleton;

import de.ovgu.wdok.guinan.connector.GuinanConnector;
import de.ovgu.wdok.guinan.connector.GuinanThread;
import de.ovgu.wdok.guinan.connector.delicious.GuinanDeliciousConnector;
import de.ovgu.wdok.guinan.connector.slideshare.GuinanSlideshareConnector;
import de.ovgu.wdok.guinan.connector.stackoverflow.GuinanStackoverflowConnector;
import de.ovgu.wdok.guinan.connector.vimeo.GuinanVimeoConnector;
import de.ovgu.wdok.guinan.graph.GuinanGraph;
import de.ovgu.wdok.guinan.graph.GuinanNode;
import de.ovgu.wdok.guinan.nlp.KeywordExtractor;
import de.ovgu.wdok.guinan.ontologyconnector.GuinanOntologyConnector;

/**
 * GuinanMaster is part of the Guinan Webservice.<br>
 * It accepts queries sent via HTTP GET to the query endpoint (something like
 * <code>http://host.com/GuinanMaster/query/&lt;querystring&gt;</code>). It then
 * forwards the query to each of the connectors in the registered_connectors
 * list and collects their results. The result set is returned as JSON to the
 * original caller.<br>
 * <br>
 * Using the @singleton annotation, since we want only one instance of the
 * service per request to be able to keep the list of connectors see <a href=
 * "http://jersey.java.net/nonav/apidocs/1.12/jersey/com/sun/jersey/spi/resource/Singleton.html"
 * >http://jersey.java.net/nonav/apidocs/1.12/jersey/com/sun/jersey/spi/resource
 * /Singleton.html</a>
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.6
 * **/
@Singleton
/* GuinanMaster's suffix of its location will be /GuinanMaster */
@Path("GuinanMaster")
public class GuinanMaster {
	
	private final String USER_AGENT = "Mozilla/5.0";

	/** list of registered ontology connectors **/
	private ArrayList<GuinanOntologyConnector> registered_ontology_connectors;

	/** list of registered connectors **/
	private ArrayList<GuinanConnector> registered_connectors;

	/** Guinan is acting also as a client (for the connectors) **/
	final Client client;

	final KeywordExtractor kwe;

	/**
	 * additional headers for the HTTP response in order to enable cross origin
	 * resource sharing (see <a
	 * href="http://de.wikipedia.org/wiki/Cross-Origin_Resource_Sharing"
	 * >http://de.wikipedia.org/wiki/Cross-Origin_Resource_Sharing</a> These
	 * headers are not used by now, but might be in the future.
	 */
	private String _corsHeaders;

	private Timestamp running_since;

	private String last_query;

	/** parameterless constructor, initialized the connector list and the client **/
	public GuinanMaster() {
		System.out.println("   ____       _                   ");
		System.out.println("  / ___|_   _(_)_ __   __ _ _ __  ");
		System.out.println(" | |  _| | | | | '_ \\ / _` | '_ \\ ");
		System.out.println(" | |_| | |_| | | | | | (_| | | | |");
		System.out.println("  \\____|\\__,_|_|_| |_|\\__,_|_| |_|");
		System.out.println("");
		this.registered_connectors = new ArrayList<GuinanConnector>();
		this.registered_ontology_connectors = new ArrayList<GuinanOntologyConnector>();
		this.client = new Client();
		this.running_since = getCurrentTimestamp();
		this.kwe = new KeywordExtractor();
		this.last_query = "";
		// if there are no connectors registered, try to register them now
		//this.registerConnectors();
	}

	@GET
	@Path("rc")
	public void registerConnectors() {

		try {

			/* registering SlideshareConnector 
			System.out.print("Registering SlideshareConnector ... ");
			this.sendGet("http://localhost:10080/Guinan/slideshareconnector/register");
			System.out.println(" done.");

			/* registering StackoverflowConnector 
			System.out.print("Registering StackoverFlowConnector ... ");
			this.sendGet("http://localhost:10080/Guinan/stackoverflowconnector/register");
			System.out.println(" done.");

			/* registering DeliciousConnector 
			System.out.print("Registering DeliciousConnector ... ");
			this.sendGet("http://localhost:10080/Guinan/deliciousconnector/register");
			System.out.println(" done.");

			/* registering VimeoConnector 
			System.out.print("Registering Vimeoconnecotr ... ");
			this.sendGet("http://localhost:10080/Guinan/vimeoconnector/register");
			System.out.println(" done.");*/
			GuinanDeliciousConnector del = new GuinanDeliciousConnector();
			del.register();
			
			GuinanStackoverflowConnector so = new GuinanStackoverflowConnector();
			so.register();
			
			GuinanVimeoConnector vim = new GuinanVimeoConnector();
			vim.register();
			
			GuinanSlideshareConnector sl = new GuinanSlideshareConnector();
			sl.register();
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}
	
	

	private Timestamp getCurrentTimestamp() {
		return new Timestamp(Calendar.getInstance().getTime().getTime());
	}

	/**
	 * via HTTP GET .../info a client can request Guinan's status information,
	 * showing the currently registered services
	 * 
	 * @return String (plain text) containing information about registered
	 *         connectors
	 * **/

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("info")
	public String info() {

		return "   ____       _                   \n"
				+ "  / ___|_   _(_)_ __   __ _ _ __  \n"
				+ " | |  _| | | | | '_ \\ / _` | '_ \\ \n"
				+ " | |_| | |_| | | | | | (_| | | | |\n"
				+ "  \\____|\\__,_|_|_| |_|\\__,_|_| |_|\n\n"
				+ "Running since: " + this.getRunning_since()
				+ "\n\nCurrently registered services: "
				+ this.getRegisteredConnectors()
				+ "\n\nCurrently registered ontology connectors: "
				+ this.getRegistered_ontology_connectors() + "\n\nLast query: "
				+ this.getLast_query();
	}

	public Timestamp getRunning_since() {
		return running_since;
	}

	public void setRunning_since(Timestamp running_since) {
		this.running_since = running_since;
	}

	public ArrayList<GuinanOntologyConnector> getRegistered_ontology_connectors() {
		return registered_ontology_connectors;
	}

	public void setRegistered_ontology_connectors(
			ArrayList<GuinanOntologyConnector> registered_ontology_connectors) {
		this.registered_ontology_connectors = registered_ontology_connectors;
	}

	/**
	 * getter method for the registered_connectors ArrayList
	 * 
	 * @return list of registered connectors
	 * **/

	private ArrayList<GuinanConnector> getRegisteredConnectors() {
		return registered_connectors;
	}

	public String getLast_query() {
		return last_query;
	}

	public void setLast_query(String last_query) {
		this.last_query = last_query;
	}
	
	

	/**
	 * via HTTP POST .../register/<connectorname> a connector can register to
	 * the GuinanMaster The connector is then added to the list of currently
	 * registered connectors
	 * 
	 * @param new_connector
	 *            connector's unique name
	 * @param req
	 *            connector's URI endpoint
	 * @return Response containing status code of success or fail
	 **/
	@POST
	@Path("registerConnector/{new_connector}")
	public Response registerConnector(
			@PathParam("new_connector") String new_connector, String req) {

		URI location;
		// try to parse the URI location
		try {

			location = UriBuilder.fromUri(req).build();

		} catch (Exception e) { // if that fails, raise an error and return
								// status code 500
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e)
					.build();
		}

		// if service is not yet registered ..
		if (!this.existsConnectorByName(new_connector)) {
			// ...add it to the service list
			this.registered_connectors.add(new GuinanConnector(new_connector,
					location));
			// ... and return OK (200) to client
			return Response.ok().type("text/plain")
					.entity("Service has been registered.").build();
		} else
			// otherwise return an error code ("forbidden")
			// TODO is there a better return code for this use case?
			return Response.status(403).type("text/plain")
					.entity("Service already registered!").build();

	}

	/**
	 * remove connector from the list of currently registered connectors
	 * 
	 * @param service
	 *            servicename
	 * @return boolean indicating whether a certain connector could be removed
	 *         or not
	 * **/

	@DELETE
	@Path("unregisterConnector/{service}")
	public Response unregisterConnector(@PathParam("service") String service) {
		int i = findIndexOfRegisteredGuinanConnector(service);
		if (i > -1) {

			this.getRegisteredConnectors().remove(i);
			return Response
					.status(Status.OK)
					.entity("Connector " + service
							+ " has been removed successfully.").build();
		} else
			return Response.status(Status.BAD_REQUEST)
					.entity("Connector " + service + " could not be removed.")
					.build();
	}

	@DELETE
	@Path("unregisterOConnector/{service}")
	public Response unregisterOConnector(@PathParam("service") String service) {
		int i = findIndexOfRegisteredGuinanOntologyConnector(service);
		if (i > -1) {

			this.getRegistered_ontology_connectors().remove(i);
			return Response
					.status(Status.OK)
					.entity("OntologyConnector " + service
							+ " has been removed successfully.").build();
		} else
			return Response
					.status(Status.BAD_REQUEST)
					.entity("OntologyConnector " + service
							+ " could not be removed.").build();
	}

	private int findIndexOfRegisteredGuinanConnector(String service) {
		int counter = 0;
		for (GuinanConnector c : this.getRegisteredConnectors()) {
			if (c.getName().equals(service))
				return counter;
			else
				counter++;
		}
		return -1;
	}

	private int findIndexOfRegisteredGuinanOntologyConnector(String service) {
		int counter = 0;
		for (GuinanOntologyConnector c : this
				.getRegistered_ontology_connectors()) {
			if (c.getName().equals(service))
				return counter;
			else
				counter++;
		}
		return -1;
	}

	@POST
	@Path("registerOntologyConnector/{new_ontology_connector}")
	public Response registerOntologyConnector(
			@PathParam("new_ontology_connector") String new_connector,
			String req) {

		URI location;
		// try to parse the URI location
		try {

			location = UriBuilder.fromUri(req).build();

		} catch (Exception e) { // if that fails, raise an error and return
								// status code 500
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e)
					.build();
		}

		// if service is not yet registered ..
		if (!this.existsOntologyConnectorByName(new_connector)) {
			// ...add it to the service list
			this.registered_ontology_connectors
					.add(new GuinanOntologyConnector(new_connector, location));
			// ... and return OK (200) to client
			return Response.ok().type("text/plain")
					.entity("Ontology connector has been registered.").build();
		} else
			// otherwise return an error code ("forbidden")
			// TODO is there a better return code for this use case?
			return Response.status(403).type("text/plain")
					.entity("Ontology connector already registered!").build();

	}

	private boolean existsOntologyConnectorByName(String new_connector) {
		// iterate through list of registered connectors
		for (GuinanOntologyConnector a : this
				.getRegistered_ontology_connectors()) {
			// if there is a connector with a name that is equal to that passed
			// as parameter
			if (a.getName().equals(new_connector))
				return true;
		}
		return false;

	}

	/**
	 * test, if service with given name is already registered
	 * 
	 * @param name
	 *            unique name of connector
	 * @return true if there is already a connector with the given name
	 *         registered, false if there is no such connector registered
	 **/
	private boolean existsConnectorByName(String name) {
		// iterate through list of registered connectors
		for (GuinanConnector a : this.getRegisteredConnectors()) {
			// if there is a connector with a name that is equal to that passed
			// as parameter
			if (a.getName().equals(name))
				return true;
		}
		return false;
	}

	/****************************************************************************/
	/* CLIENT PART */
	/****************************************************************************/
	/**
	 * The query method is called, when the client makes a HTTP GET request to
	 * the query endpoint (something like
	 * <code>http://host.com/GuinanMaster/query/&lt;querystring&gt;</code>). It
	 * then forwards the query to the registered endpoints and collects their
	 * responses in order to send it back to the calling client.
	 * 
	 * @param query
	 *            query string to be passed to connectors
	 * @return Response to client containing set of result objects (as JSON)
	 */
	@GET
	@Path("query/{query_string}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response query(@PathParam("query_string") final String query) {

		// save last query
		this.setLast_query(query);
		// prepare list containing the results of all connectors
		final ArrayList<GuinanResult> resultsfromconnectors = new ArrayList<GuinanResult>();
		final ArrayList<GuinanClientResult> resultsforclient = new ArrayList<GuinanClientResult>();
		// if no connector is registered, return "Service unavailable" to the
		// user
		if (this.getRegisteredConnectors().size() < 1)
			return makeCORS(Response.status(Status.SERVICE_UNAVAILABLE));
		// prepare a thread pool, one thread for each connector
		ExecutorService threadExecutor = Executors.newFixedThreadPool(this
				.getRegisteredConnectors().size());
		List<Future<ArrayList<GuinanClientResult>>> futurelist = new ArrayList<Future<ArrayList<GuinanClientResult>>>();
		for (final GuinanConnector aws : this.getRegisteredConnectors()) {
			// execute query method for each connector
			Callable<ArrayList<GuinanClientResult>> worker = new GuinanThread(
					aws, client, query);
			Future<ArrayList<GuinanClientResult>> future = threadExecutor
					.submit(worker);
			futurelist.add(future);
		}

		// if done, stop the threads
		threadExecutor.shutdown(); // shutdown worker threads
		// Wait until all threads are finished
		try {
			threadExecutor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("Could not await termination of all threads");
			return makeCORS(Response.status(Status.SERVICE_UNAVAILABLE));
		}

		// new fetch the results from the worker threads' future objects
		for (Future<ArrayList<GuinanClientResult>> f : futurelist) {
			try {
				resultsfromconnectors.addAll(f.get());
			} catch (InterruptedException | ExecutionException e) {
				System.err
						.println("Could not fetch the result from worker thread");
				return makeCORS(Response.status(Status.SERVICE_UNAVAILABLE));
			}
		}
		for (GuinanResult gr : resultsfromconnectors) {
			GuinanClientResult gcr = new GuinanClientResult(gr);
			ArrayList<String> comments = gcr.getComments();
			ArrayList<String> unhandled_keywords = new ArrayList<String>();
			// get further concepts from the content description
			if (gcr.getContent() != null)
				unhandled_keywords
						.addAll(kwe.extractKeywords(gcr.getContent()));
			// get further concepts from the comments
			for (String s : comments) {

				if (s != null)
					unhandled_keywords.addAll((kwe.extractKeywords(s)));
			}
			gcr.setAdditional_tags(new ArrayList<String>(kwe
					.getSetofKeywords(unhandled_keywords)));
			// compute common tagset
			gcr.setAggregated_tags(gcr.mergeTags());
			gcr.setCommon_tags(gcr.computeCommonTags());
			gcr.computeTermFrequency();
			// gcr.setOntology_concepts(this.buildConceptGraph(
			// gcr.getCommon_tags(), gcr.get_location()));
			// System.out.println("ontology_concepts: "+gcr.getOntology_concepts());
			resultsforclient.add(gcr);
		}

		// Everything went fine, report OK and return the result as JSON
		return makeCORS(Response.status(Status.OK).entity(resultsforclient));

	}

	/*
	 * private GuinanGraph buildConceptGraph(ArrayList<String> common_tags,
	 * String id) { GuinanGraph conceptGraph = new GuinanGraph(id); for (String
	 * tag : common_tags) {
	 * 
	 * ExecutorService threadExecutor = Executors.newFixedThreadPool(this
	 * .getRegisteredConnectors().size());
	 * List<Future<ArrayList<GuinanOntologyResult>>> futurelist = new
	 * ArrayList<Future<ArrayList<GuinanOntologyResult>>>(); for (final
	 * GuinanOntologyConnector goc : this.getRegistered_ontology_connectors()) {
	 * // execute query method for each connector
	 * Callable<ArrayList<GuinanOntologyResult>> worker = new
	 * GuinanOConnectorThread( goc, client, tag);
	 * Future<ArrayList<GuinanOntologyResult>> future = threadExecutor
	 * .submit(worker); futurelist.add(future); }
	 * 
	 * // if done, stop the threads threadExecutor.shutdown(); // shutdown
	 * worker threads // Wait until all threads are finished try {
	 * threadExecutor.awaitTermination(30, TimeUnit.SECONDS); } catch
	 * (InterruptedException e) {
	 * System.err.println("Could not await termination of all threads");
	 * //return makeCORS(Response.status(Status.SERVICE_UNAVAILABLE)); }
	 * 
	 * // new fetch the results from the worker threads' future objects for
	 * (Future<ArrayList<GuinanOntologyResult>> f : futurelist) { try {
	 * 
	 * for(GuinanOntologyResult gor : f.get()){
	 * System.out.println("Trying to unpack label from ontology result");
	 * System.
	 * out.println("Trying to add GuinanNode with label "+gor.getLabel());
	 * conceptGraph.addNode(new GuinanNode(gor.getLabel())); } } catch
	 * (InterruptedException | ExecutionException e) { System.err
	 * .println("Could not fetch the result from worker thread"); //return
	 * makeCORS(Response.status(Status.SERVICE_UNAVAILABLE)); } }
	 * 
	 * }
	 * 
	 * return conceptGraph; }
	 */

	private GuinanGraph buildConceptGraph(ArrayList<String> common_tags,
			String id) {
		System.out.println("Trying to build concept graph");
		GuinanGraph conceptGraph = new GuinanGraph(id);
		for (String tag : common_tags) {
			for (GuinanOntologyConnector goc : this
					.getRegistered_ontology_connectors()) {
				try {
					String json_res = this.client.resource(goc.getLocation())
							.queryParam("q", tag)
							.accept(MediaType.APPLICATION_JSON)
							.get(String.class);

					// after this was successfull, convert the json response
					// (basically
					// a string) into plain old java objects (deserializing)
					ArrayList<GuinanOntologyResult> ores = GuinanMaster
							.convertJSONtoGraph(json_res);
					if (ores != null) {
						// System.out.println("**************# of onto results: "
						// + ores.size() + "*************");
						for (GuinanOntologyResult or : ores) {

							if (or.getLabel() != null) {

								conceptGraph.addNode(new GuinanNode(or
										.getLabel()));
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return conceptGraph;
	}

	public static ArrayList<GuinanOntologyResult> convertJSONtoGraph(
			String json_response) {

		// prepare result list
		ArrayList<GuinanOntologyResult> result = null;
		try {
			// using jacksons mapping capabilities, we try to map the json
			// response directly to an arraylist of GuinanResults
			// this should work always, since the json string is only a
			// serialized version of an arraylist of GuinanResults
			result = new ObjectMapper().readValue(json_response,
					new TypeReference<ArrayList<GuinanOntologyResult>>() {
					});
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			System.err.println("ERROR: JsonParseException");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block

			System.err
					.println("\n\n*************************************************\n\nERROR: JsonMappingException");
			e.printStackTrace();

		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(result);
		return result;
	}

	/*
	 * @GET
	 * 
	 * @Path("getSemantics/{query_string}")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * getSemantics(@PathParam("query_string") final String query) { String[]
	 * keywords =query.split(","); GuinanSemanticGraph graph = new
	 * GuinanSemanticGraph(); for(String kw: keywords)
	 * graph.addNode(kw,kw,"#ff950e","dot");
	 * 
	 * ObjectMapper objMapper = new ObjectMapper(); try { String jsonString =
	 * objMapper.writeValueAsString(graph); return
	 * makeCORS(Response.status(Status.OK).entity(jsonString));
	 * 
	 * } catch (JsonProcessingException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * return makeCORS(Response.status(Status.BAD_REQUEST)); }
	 */

	/**
	 * mapping json response to GuinanResult objects (plain old java objects) in
	 * order to deserialize the response from the connectors
	 * 
	 * @param json_response
	 *            the response as it came back from the connectors
	 * @return an ArrayList containing GuinanResult objects
	 */
	public static ArrayList<GuinanClientResult> convertJSONtoPOJO(
			String json_response) {

		// prepare result list
		ArrayList<GuinanClientResult> result = null;
		try {
			// using jacksons mapping capabilities, we try to map the json
			// response directly to an arraylist of GuinanResults
			// this should work always, since the json string is only a
			// serialized version of an arraylist of GuinanResults
			result = new ObjectMapper().readValue(json_response,
					new TypeReference<ArrayList<GuinanResult>>() {
					});
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * add additional headers to enable CORS<br>
	 * This enables e.g. clients, that try to include the response to a query
	 * via AJAX, to actually be able to use the response (otherwise denied
	 * through Same-origin-policy (SOP))
	 * 
	 * @param req
	 *            ResponseBuilder with the original request containing the
	 *            response to the client
	 * @param returnMethod
	 *            String containing the CORS headers
	 * @return Response with CORS headers
	 */
	private Response makeCORS(ResponseBuilder req, String returnMethod) {
		ResponseBuilder rb = req.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

		// if the CORS headers are set, use these or overwrite the settings from
		// above respectively
		if (!"".equals(returnMethod)) {
			rb.header("Access-Control-Allow-Headers", returnMethod);
		}

		return rb.build();
	}

	/**
	 * add additional headers to the HTTP request in order to make request CORS
	 * compliant
	 * 
	 * @param req
	 *            ResponseBuilder with original request to be send to the client
	 * @return Response containing original request and CORS headers
	 */
	private Response makeCORS(ResponseBuilder req) {
		return makeCORS(req, _corsHeaders);
	}

	public static ArrayList<GuinanOntologyResult> convertOntologyRJSONtoPOJO(
			String json_response) {
		// prepare result list
		ArrayList<GuinanOntologyResult> result = null;
		try {
			// using jacksons mapping capabilities, we try to map the json
			// response directly to an arraylist of GuinanResults
			// this should work always, since the json string is only a
			// serialized version of an arraylist of GuinanResults
			result = new ObjectMapper().readValue(json_response,
					new TypeReference<ArrayList<GuinanOntologyResult>>() {
					});
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

}
