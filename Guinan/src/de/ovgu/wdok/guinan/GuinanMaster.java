package de.ovgu.wdok.guinan;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
/* GuinanMaster's suffix of it's location will be /GuinanMaster */
@Path("/GuinanMaster")
public class GuinanMaster {

	/** list of registered connectors **/
	private ArrayList<GuinanConnector> registered_connectors;

	/** Guinan is acting also as a client (for the connectors) **/
	final Client client;
	
	/**
	 * additional headers for the HTTP response in order to enable cross origin
	 * resource sharing (see <a
	 * href="http://de.wikipedia.org/wiki/Cross-Origin_Resource_Sharing"
	 * >http://de.wikipedia.org/wiki/Cross-Origin_Resource_Sharing</a>
	 * These headers are not used by now, but might be in the future.
	 */
	private String _corsHeaders;

	/** parameterless constructor, initialized the connector list and the client **/
	public GuinanMaster() {
		this.registered_connectors = new ArrayList<GuinanConnector>();

		this.client = new Client();
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

		return "Currently registered services: " + this.getRegisteredServices();
	}

	/**
	 * getter method for the registered_connectors ArrayList
	 * 
	 * @return list of registered connectors
	 * **/

	private ArrayList<GuinanConnector> getRegisteredServices() {
		return registered_connectors;
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
		if (!this.existsServiceByName(new_connector)) {
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
	public boolean unregisterService(@PathParam("service") String service) {

		// TODO implement this
		return true;
	}

	/**
	 * test, if service with given name is already registered
	 * 
	 * @param name
	 *            unique name of connector
	 * @return true if there is already a connector with the given name
	 *         registered, false if there is no such connector registered
	 **/
	private boolean existsServiceByName(String name) {
		// iterate through list of registered connectors
		for (GuinanConnector a : this.getRegisteredServices()) {
			// if there is a connector with a name that is equal to that passed
			// as parameter
			if (a.getName().equalsIgnoreCase(name))
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
		// prepare list containing the results of all connectors
		final ArrayList<GuinanResult> results = new ArrayList<GuinanResult>();
		// if no connector is registered, return "Service unavailable" to the
		// user
		if (this.getRegisteredServices().size() < 1)
			return makeCORS(Response.status(Status.SERVICE_UNAVAILABLE));
		// prepare a thread pool, one thread for each connector
		ExecutorService threadExecutor = Executors.newCachedThreadPool();
		for (final GuinanConnector aws : this.getRegisteredServices()) {
			// execute query method for each connector
			threadExecutor
					.execute(new GuinanThread(aws, client, query, results));

		}

		// if done, stop the threads
		threadExecutor.shutdown(); // shutdown worker threads

		// TODO
		// this is really ugly, please make it better!
		// better using callable??

		while (!threadExecutor.isTerminated()) {
			// idle
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Everything went fine, report OK and return the result as JSON
		return makeCORS(Response.status(Status.OK).entity(results));

	}

	/**
	 * mapping json response to GuinanResult objects (plain old java objects) in
	 * order to deserialize the response from the connectors
	 * 
	 * @param json_response
	 *            the response as it came back from the connectors
	 * @return an ArrayList containing GuinanResult objects
	 */
	static protected ArrayList<GuinanResult> convertJSONtoPOJO(
			String json_response) {

		// prepare result list
		ArrayList<GuinanResult> result = null;
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
	 * @param req the original request containing the response to the client
	 * @param returnMethod the CORS headers
	 * @return Response with CORS headers
	 */
	private Response makeCORS(ResponseBuilder req, String returnMethod) {
		ResponseBuilder rb = req.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

		//if the CORS headers are set, use these or overwrite the settings from above respectively
		if (!"".equals(returnMethod)) {
			rb.header("Access-Control-Allow-Headers", returnMethod);
		}

		return rb.build();
	}

	/**
	 * add additional headers to the HTTP request in order to make request CORS compliant
	 * @param req original request to be send to the client
	 * @return Response containing original request and CORS headers
	 */
	private Response makeCORS(ResponseBuilder req) {
		return makeCORS(req, _corsHeaders);
	}

}

/**
 * a special thread class for GuinanMaster<br>
 * each registered connector will be queried in a different thread (for performance reasons)
 * @author <a href="mailto:katrin.krieger@ovgu.de">Katrin Krieger</a>
 *
 */
class GuinanThread extends Thread implements Runnable {

	/** the GuinanConnector to be run in the thread */
	private GuinanConnector ags;
	/** the client making requests to the connector	 */
	private Client client;
	/** the query string*/
	private String query;
	/** list with GuinanResults returned from the connector*/
	private ArrayList<GuinanResult> results;

	/**
	 * Constructor, taking 4 parameters
	 * @param ags the GuinanConnector running in the thread
	 * @param client the client part sending requests to the connector
	 * @param q the query string
	 * @param results the ArrayList containing results returned from the connector (empty upon call of constructor)
	 */
	GuinanThread(GuinanConnector ags, Client client, String q,
			ArrayList<GuinanResult> results) {
		this.ags = ags;
		this.client = client;
		this.query = q;
		this.results = results;
	}

	/**
	 * Method actually doing stuff (sending query to the connector and fetching results)
	 */
	public void run() {
		System.out
				.println("Started Thread " + Thread.currentThread().getName());
		//try to send query to the connector and tell them, that the GuinanMaster accepts JSON as return format
		try {
			String json_res = this.client.resource(ags.getLocation())
					.queryParam("q", query).accept(MediaType.APPLICATION_JSON)
					.get(String.class);
			//after this was successfull, convert the json response (basically a string) into plain old java objects (deserializing)
			synchronized (this) {
				results.addAll(GuinanMaster.convertJSONtoPOJO(json_res));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
