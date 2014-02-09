package de.ovgu.wdok.guinan.ontologyconnector.freebase;

import java.net.URI;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.ovgu.wdok.guinan.ontologyconnector.GuinanOntologyConnector;

@Path("freebaseconnector")
public class GuinanFreebaseConnector extends GuinanOntologyConnector {

	/** unique name */
	private final static String CONNECTOR_NAME = "GuinanFreebaseOConnector";

	/** location of GuinanSlideshareConnector */
	private final static String LOCATION = "http://localhost:8080/Guinan/freebaseconnector";

	/** configuration for the client part (client for the API of DBPedia) */
	private ClientConfig config;

	/** client part for API of dbpedia */
	private Client client;
	
	/** Web resource representation of dbpedia API query endpoint */
	private WebResource freebasesearchloc;
	
	public GuinanFreebaseConnector() {
		super(CONNECTOR_NAME, getBaseURIForFreebaseOConnector());
		this.config = new DefaultClientConfig();
		this.client = Client.create(config);
		// set location of GuinanMaster
		this.masterloc = client.resource(getBaseURIForMaster());
		// set location of slideshare query endoint
		this.freebasesearchloc = client
				.resource(getBaseURIForFreebaseSearch());
	}

	private URI getBaseURIForFreebaseSearch() {
		return UriBuilder.fromUri(
				"http://lookup.dbpedia.org/api/search.asmx/KeywordSearch").build();
	}

	private static URI getBaseURIForFreebaseOConnector() {
		return UriBuilder.fromUri(LOCATION + "/query").build();
	}

	

}
