package de.ovgu.wdok.guinan.ontologyconnector;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;

import de.ovgu.wdok.guinan.GuinanClientResult;
import de.ovgu.wdok.guinan.GuinanMaster;
import de.ovgu.wdok.guinan.GuinanOntologyResult;

/**
 * a special thread class for GuinanMaster<br>
 * each registered connector will be queried in a different thread (for
 * performance reasons)
 * 
 * @author <a href="mailto:katrin.krieger@ovgu.de">Katrin Krieger</a>
 * 
 */
public class GuinanOConnectorThread extends Thread implements
		Callable<ArrayList<GuinanOntologyResult>> {

	/** the GuinanConnector to be run in the thread */
	private GuinanOntologyConnector gos;
	/** the client making requests to the connector */
	private Client client;
	/** the query string */
	private String query;

	/**
	 * Constructor, taking 4 parameters
	 * 
	 * @param ags
	 *            the GuinanConnector running in the thread
	 * @param client
	 *            the client part sending requests to the connector
	 * @param q
	 *            the query string
	 * @param results
	 *            the ArrayList containing results returned from the connector
	 *            (empty upon call of constructor)
	 */
	public GuinanOConnectorThread(GuinanOntologyConnector gos, Client client, String q) {
		this.gos = gos;
		this.client = client;
		this.query = q;
	}
	

	/**
	 * Method actually doing stuff (sending query to the connector and fetching
	 * results)
	 */
	public ArrayList<GuinanOntologyResult> call() {
		System.out
				.println("Started oConnector Thread " + Thread.currentThread().getName());
		// try to send query to the connector and tell them, that the
		// GuinanMaster accepts JSON as return format
		try {
			String json_res = this.client.resource(gos.getLocation())
					.queryParam("q", query).accept(MediaType.APPLICATION_JSON)
					.get(String.class);
			// after this was successfull, convert the json response (basically
			// a string) into plain old java objects (deserializing)
			return GuinanMaster.convertOntologyRJSONtoPOJO(json_res);
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
}