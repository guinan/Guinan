package de.ovgu.wdok.guinan.ontologyconnector.dbpedia;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.ovgu.wdok.guinan.GuinanOntologyResult;
import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.connector.slideshare.GuinanSlideshowResultContentHandler;
import de.ovgu.wdok.guinan.ontologyconnector.GuinanOntologyConnector;

/**
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.1
 */

@Path( "/dbpediaconnector/" )
public class GuinanDBpediaConnector extends GuinanOntologyConnector
{

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

  public GuinanDBpediaConnector()
  {
	super( CONNECTOR_NAME, getBaseURIForDBpediaOConnector() );
	this.config = new DefaultClientConfig();
	this.client = Client.create( config );
	
	// set location of GuinanMaster
	this.masterloc = client.resource( getBaseURIForMaster() );
	// set location of slideshare query endoint
	this.dbpediasearchloc = client.resource( getBaseURIForDBPediaSearch() );
  }

  private URI getBaseURIForDBPediaSearch()
  {
	return UriBuilder.fromUri( "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch" ).build();
  }

  private static URI getBaseURIForDBpediaOConnector()
  {
	return UriBuilder.fromUri( LOCATION + "/query" ).build();
  }

	@GET
	@Path("query/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ArrayList<GuinanOntologyResult> query(@QueryParam("q") String query) {
		String response = this.dbpediasearchloc
			.queryParam( "QueryString", query )
			//.queryParam( "QueryClass", "Resource" )
			//.header( "Accept", "application/json" )
			.get( String.class );
		
		//return response;
		return this.extractGuinanOntologyResultsFromXMLResponse(response);
	}

	private ArrayList<GuinanOntologyResult> extractGuinanOntologyResultsFromXMLResponse(
			String response) {
		// initialize result list
				ArrayList<GuinanOntologyResult> resultlist = new ArrayList<GuinanOntologyResult>();

								SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
				try {
					SAXParser saxParser = saxParserFactory.newSAXParser();

					// read XML input from the passed string
					InputSource inputSource = new InputSource(new StringReader(
							response));
					// create a new special content handler for GuinanDBpediaConnector, that
					// will take care of the mapping from XML elements to object
					// attributes
					GuinanDBpediaResultContentHandler ch = new GuinanDBpediaResultContentHandler();
					// do actual parsing
					saxParser.parse(inputSource, ch);
					// fetch GuinanResultObject from the content parser
					resultlist=ch.getGuinanOntologyResultList();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return resultlist;
	}
	
	
}
