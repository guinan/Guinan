package de.ovgu.wdok.guinan.connector;

import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.ovgu.wdok.guinan.GuinanResult;

/**
 * 
 * The class GuinanConnector defines all methods a connector for Guinan should
 * include
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.6
 */

public class GuinanConnector {

	/** unique name of a connector */
	protected String name;

	/** the connector's location (URI) */
	protected URI location;

	/** the location of GuinanMaster (URI) */
	protected WebResource masterloc;
	
	

	/** config for client part **/
	protected ClientConfig config;

	/** client part to specific API */
	protected Client client;

	public GuinanConnector(String name, URI location) {
		this.name = name;
		this.location = location;
		// set service's client config to default config
		this.config = new DefaultClientConfig();
		

		// create client with config
		this.client = Client.create(config);
		this.client.setReadTimeout(10000);
	}

	/**
	 * Returns a textual representation of a GuinanConnector
	 */
	@Override
	public String toString() {
		return "GuinanConnector [\n\t\t\t\t\tname=" + name
				+ ", \n\t\t\t\t\tlocation=" + location + "\n\t\t\t\t]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URI getLocation() {
		return location;
	}

	public void setLocation(URI location) {
		this.location = location;
	}



	/**
	 * This method will be overwritten with a specific implementation of the
	 * specialized connectors
	 * 
	 * @param query
	 *            the query string
	 * @return a list of GuinanResults, mapped from the Web API's response
	 *         string
	 */
	public ArrayList<GuinanResult> query(String query) {
		return null;

	}

	public static URI getBaseURIForMaster() {
		// TODO put this into central config file
		return UriBuilder.fromUri("http://localhost:10080/Guinan/webapp/GuinanMaster")
				.build();
	}

	/**
	 * register connector to GuinanMaster ... basically calling the register
	 * URI, telling the master
	 * "hey look, it's me, i'm here, this is my name and this is my query endpoint"
	 * 
	 * @return a status message telling whether the registration worked or not
	 */
	@GET
	@Path("register")
	public String register() {
		// TODO better return a Response object?
		WebResource masterloc = this.masterloc;
		String connectorname = this.getName();
		try {
			masterloc.path("registerConnector").path(connectorname)
					.accept(MediaType.TEXT_PLAIN)
					.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
					.post(String.class, this.getLocation().toString());
			return "Connector " + connectorname + " has been registered";
		}

		// System.out.println(response.toString());
		catch (UniformInterfaceException uie) {
			return "Connector " + connectorname
					+ " has already been registered. No changes";
		} catch (RuntimeException e) {

			System.out.println("Whoops! " + e.getMessage());
			e.printStackTrace();
		}
		return "";
	}
	
	@GET 
	@Path("unregister")
	public Response unregister() {
		// TODO better return a Response object?
		WebResource masterloc = this.masterloc;
		String connectorname = this.getName();
		try {
			masterloc.path("unregisterConnector").path(connectorname)
					.accept(MediaType.TEXT_PLAIN)
					.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
					.delete();
			return Response.ok().type("text/plain")
					.build();
		}

		 
		catch (UniformInterfaceException uie) {
			
			return Response.ok().type("text/plain")
					.entity("Service " + connectorname
					+ " was not registered. No changes. \n\n"+uie.getMessage()).build();
		} catch (RuntimeException e) {

			System.out.println("Whoops! " + e.getMessage());
			e.printStackTrace();
		}
		return Response.noContent().build();
	}

}
