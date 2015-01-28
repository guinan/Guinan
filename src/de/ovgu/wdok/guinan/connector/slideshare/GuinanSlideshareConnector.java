package de.ovgu.wdok.guinan.connector.slideshare;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

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

import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.WebResource;

import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.connector.GuinanConnector;

/**
 * GuinanSlideshareConnector is a special connector that forwards queries from
 * the GuinanMaster to the <a
 * href="http://www.slideshare.net/developers">Slideshare API</a>, transforms
 * the XML result from Slideshare into GuinanResults and sends a list of those
 * results back the master
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.6
 */
@Path("/slideshareconnector/")
public class GuinanSlideshareConnector extends GuinanConnector {

	// TODO put config into seperate file
	/** unique name */
	private final static String CONNECTOR_NAME = "GuinanSlideshareConnector";

	/** location of GuinanSlideshareConnector */
	private final static String LOCATION = "http://localhost:10080/Guinan/webapp/slideshareconnector";

	
	/** API key for Slideshare API */
	private final String API_KEY = "sjLic05V";

	/** shared secret as part of authentification at slideshare */
	private final String SHARED_SECRET = "Rxunnqyw";

	/** Web resource representation of slideshare API query endpoint */
	private WebResource slidesharesearchloc;

	public GuinanSlideshareConnector() {

		// call constructor of super class and set connector name and endpoint
		super(CONNECTOR_NAME, getBaseURIForSlideshareConnector());
		
		this.masterloc = client.resource(getBaseURIForMaster());
		// set location of slideshare query endoint
		this.slidesharesearchloc = client
				.resource(getBaseURIForSlideshareSearch());
	}

	/**
	 * sets query endpoint of GuinanSlideshareConnector
	 * 
	 * @return URI of query endpoint
	 */
	private static URI getBaseURIForSlideshareConnector() {
		return UriBuilder.fromUri(LOCATION + "/query").build();
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
	 *            "http://localhost:8080/Guinan/slideshareconnector/query?q=foo"
	 * @return an ArrayList with GuinanResults containing the response from
	 *         Slideshare
	 */
	@GET
	@Path("query/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ArrayList<GuinanResult> query(@QueryParam("q") String query) {
		// result list
		// compute timestamp and a hash for authentication with slideshare

		Date now = new Date();
		String ts = Long.toString(now.getTime() / 1000);
		String hash = DigestUtils.sha1Hex(this.SHARED_SECRET + ts)
				.toLowerCase();

		// query the slideshare endpoint
		// the string contains the XML response with the results
		String xml_response = slidesharesearchloc
				.queryParam("api_key", this.API_KEY).queryParam("ts", ts)
				.queryParam("hash", hash).queryParam("q", query)
				.queryParam("include_entities", "true")
				.queryParam("result_type", "mixed")
				.accept(MediaType.APPLICATION_XML).get(String.class);
		// convert XML response into list of GuinanResults
		return this.extractGuinanResultsFromXMLResponse(xml_response);

	}

	/**
	 * @return the URI slideshare API endpoint
	 * 
	 */
	private static URI getBaseURIForSlideshareSearch() {
		return UriBuilder.fromUri(
				"https://www.slideshare.net/api/2/search_slideshows").build();
	}

	/**
	 * converts the XML response string into an ArrayList of GuinanResults
	 * 
	 * @param xml_response
	 *            XML string containing the response from SlideShare
	 * @return an ArrayList of GuinanResults
	 */
	private ArrayList<GuinanResult> extractGuinanResultsFromXMLResponse(
			String xml_response) {

		// initialize result list
		ArrayList<GuinanResult> resultlist = new ArrayList<GuinanResult>();

		// from
		// http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(
					xml_response.getBytes("utf-8"))));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		// optional, but recommended
		// read this -
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();
		// fetch all nodes with name "Slideshow"
		NodeList nList = doc.getElementsByTagName("Slideshow");
		// iterate through slideshow nodes
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			// if "slideshow" node has child elements (AKA content)
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;
				// grep the ID of the slideshow
				String slideshow_id = eElement.getElementsByTagName("ID")
						.item(0).getTextContent();
				// finally fetch some more details for the slideshow and add the
				// gr to the result
				resultlist.add(this.getDetailsForSlideshow(slideshow_id));
			}

			temp++;

		}

		return resultlist;
	}

	/**
	 * Queries slideshow API for more details on a slideshow and wraps this
	 * information into a GuinanResult object
	 * 
	 * @param slideshow_id
	 *            the id of a particular slideshow
	 * @return a GuinanResult object for a particular slideshow
	 */
	private GuinanResult getDetailsForSlideshow(String slideshow_id) {
		GuinanResult guinan_result = new GuinanResult();
		// compute date and hash for a new query to slideshow API
		Date now = new Date();
		String ts = Long.toString(now.getTime() / 1000);
		String hash = DigestUtils.sha1Hex(this.SHARED_SECRET + ts)
				.toLowerCase();
		URI loc = UriBuilder.fromUri(
				"https://de.slideshare.net/api/2/get_slideshow").build();
		WebResource detail_location = client.resource(loc);

		String xml_response = detail_location
				.queryParam("api_key", this.API_KEY).queryParam("ts", ts)
				.queryParam("hash", hash)
				.queryParam("slideshow_id", slideshow_id)
				.queryParam("detailed", "1").accept(MediaType.APPLICATION_XML)
				.get(String.class);

		// we will convert the XML response to a GuinanResult, so first create a
		// SAXParserFactory
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = saxParserFactory.newSAXParser();

			// read XML input from the passed string
			InputSource inputSource = new InputSource(new StringReader(
					xml_response));
			// create a new special content handler for GuinanSlideshow, that
			// will take care of the mapping from XML elements to oject
			// attributes
			GuinanSlideshowResultContentHandler ch = new GuinanSlideshowResultContentHandler();
			// do actual parsing
			saxParser.parse(inputSource, ch);
			// fetch GuinanResultObject from the content parser
			guinan_result = ch.getGuinanResult();
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
		return guinan_result;
	}
}
