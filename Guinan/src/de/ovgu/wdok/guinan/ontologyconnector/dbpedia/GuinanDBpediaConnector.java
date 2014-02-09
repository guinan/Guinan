package de.ovgu.wdok.guinan.ontologyconnector.dbpedia;

import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.ontologyconnector.GuinanOntologyConnector;

/**
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.1
 */

@Path("/dbpediaconnector/")
public class GuinanDBpediaConnector extends GuinanOntologyConnector {

	/** unique name */
	private final static String CONNECTOR_NAME = "GuinanDBPediaOConnector";

	/** location of GuinanDBPediaOConnector */
	private final static String LOCATION = "http://localhost:8080/Guinan/dbpediaconnector";

	/** configuration for the client part (client for the API of DBPedia) */
	private ClientConfig config;

	/** client part for API of dbpedia */
	private Client client;
	
	/** Web resource representation of dbpedia API query endpoint */
	private WebResource dbpediasearchloc;

	public GuinanDBpediaConnector() {
		super(CONNECTOR_NAME, getBaseURIForDBpediaOConnector());
		this.config = new DefaultClientConfig();
		this.client = Client.create(config);
		// set location of GuinanMaster
		this.masterloc = client.resource(getBaseURIForMaster());
		// set location of slideshare query endoint
		this.dbpediasearchloc = client
				.resource(getBaseURIForDBPediaSearch());
	}

	private URI getBaseURIForDBPediaSearch() {
		return UriBuilder.fromUri(
				"http://lookup.dbpedia.org/api/search.asmx/KeywordSearch").build();
	}

	private static URI getBaseURIForDBpediaOConnector() {
		return UriBuilder.fromUri(LOCATION + "/query").build();
	}

	@GET
	@Path("query/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String query(String query) {
		String response = this.dbpediasearchloc
				.queryParam("QueryString", query)
				.get(String.class);
		
		
		return response;
	}
	
	
}
