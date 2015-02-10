
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
		LinkedList<LinkedList<String>> tmpList = new LinkedList<LinkedList<String>>();
		WTPSemanticFingerprintGuinanResult result = new WTPSemanticFingerprintGuinanResult(keywords,tmpList);
		LinkedList<String> relatedWords = new LinkedList<String>();
		if(query == null)
			return res;
		String[] tmp = query.split(",");
		for(int i=0;i<tmp.length;i++){
			keywords.add(i,tmp[i]);
			}
		WTPGraph g = MainClass.processKeyWords(keywords);
		int i=0;
		for(String keyword : keywords){
			for(Node n : g){
				if(n.getId().equals(keyword)){
					System.out.println("			"+n.getId().toString());
					Iterator<Node> iter = n.getNeighborNodeIterator();
					while(iter.hasNext()){
						Node n2 = iter.next();
						String reString= n2.getId();
						relatedWords.add(reString);
//						WTPSemanticFingerprintGuinanResult result = new WTPSemanticFingerprintGuinanResult(keywords,reString);
//						res.add(result);
					}
					result.addRelatedWords(i, relatedWords);
					i++;
				}
			}
			
		}
		/*
		 * Only one GuinanResult
		 * one keyword has X many related words 
		 * related words can be other keywords
		 * so for every keyword there is a list of related words
		 * */
		System.out.println(result.toString());
		res.add(result);
//		g.display();
		System.out.println(res.toString());
		return res;
	}
	/*
	 * getting result graph and nodes = keywords 
	 * res are nearby nodes -> make GuinanResult Grpah 
	 * */
}