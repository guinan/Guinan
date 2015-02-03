
package de.ovgu.wdok.guinan.connector.WTPSemanticFingerprint;

import graph.WTPGraph;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import main.MainClass;

import org.graphstream.graph.Node;

import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.connector.GuinanConnector;

@Path("WTPSemanticFingerprintConnector")
public class WTPSemanticFingerPrintConnector extends GuinanConnector {


	public final static String SERVICE_NAME = "WTPSemanticFingerprintConnector";
	
	/** location of WTPSemanticFingerprint (URI) */
	private final static String LOCATION = "http://localhost:10080/Guinan/webapp/WTPSemanticFingerprintConnector";

	/**
	 * @return URI (endpoint) for GuinanWTPSemanticFingerprintConnector
	 * 
	 * */
	private static URI getBaseURIForWTPSemanticFingerprintConnector()
	{
		return UriBuilder.fromPath( LOCATION + "/query" ).build();
	}
	
	
	
	public WTPSemanticFingerPrintConnector() {
		super(SERVICE_NAME,  getBaseURIForWTPSemanticFingerprintConnector());
		// TODO Auto-generated constructor stub
//		this.client.addFilter(this.);
	}

	@GET
	@Path( "query/" )
	@Produces( MediaType.APPLICATION_JSON )
	@Override
	public ArrayList<GuinanResult> query( @QueryParam( "q" ) String query ){
		ArrayList<GuinanResult> res = new ArrayList<GuinanResult>();
		LinkedList<String> keywords = new LinkedList<String>();
		if(query == null)
			return res;
		String[] tmp = query.split(",");
		for(int i=0;i<tmp.length;i++){
			keywords.add(i,tmp[i]);
			}
		WTPGraph g = MainClass.processKeyWords(keywords);
		for(String keyword : keywords){
			for(Node n : g){
				if(n.getId().equals(keyword)){
					Iterator<Node> iter = n.getNeighborNodeIterator();
					while(iter.hasNext()){
						Node n2 = iter.next();
						String reString= n2.getId();
						WTPSemanticFingerprintGuinanResult result = new WTPSemanticFingerprintGuinanResult(keyword,reString);
						res.add(result);
					}
				}
			}
			
		}
		
		return res;
	}
	/*
	 * getting result graph and nodes = keywords 
	 * res are nearby nodes -> make GuinanResult Grpah 
	 * */
}