
package de.ovgu.wdok.guinan.connector.SemanticFingerprint;

import graph.WTPGraph;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


import utils.MainClass;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.graphstream.graph.Node;

import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.ovgu.wdok.guinan.GuinanClientResult;
import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.connector.GuinanConnector;
import de.ovgu.wdok.guinan.connector.GuinanThread;

@Path("WTPSemanticFingerprintConnector")
public class SemanticFingerPrintConnector extends GuinanConnector {

	private static boolean thread = true;
	
	public final static String SERVICE_NAME = "SemanticFingerprintConnector";
	
	/** location of WTPSemanticFingerprint (URI) */
	private final static String LOCATION = "http://localhost:10080/Guinan/webapp/SemanticFingerprintConnector";

	/** configuration for the client part (client for the API of DBPedia) */
	private ClientConfig config;

	/** client part for API of dbpedia */
	private Client client;
	
	/**
	 * @return URI (endpoint) for GuinanWTPSemanticFingerprintConnector
	 * 
	 * */
	private static URI getBaseURIForWTPSemanticFingerprintConnector()
	{
		return UriBuilder.fromPath( LOCATION + "/query" ).build();
	}
	
	
	
	public SemanticFingerPrintConnector() {
		super(SERVICE_NAME,  getBaseURIForWTPSemanticFingerprintConnector());
		// TODO Auto-generated constructor stub
//		this.client.addFilter(this.);
		this.config = new DefaultClientConfig();
		this.client = Client.create( config );
	}

	@GET
	@Path( "query/" )
	@Produces( MediaType.APPLICATION_JSON )
	@Override
	public ArrayList<GuinanResult> query( @QueryParam( "q" ) String query ){
		System.out.println("[SFP] Got a query");
		double time = System.currentTimeMillis();
		ArrayList<GuinanResult> res = new ArrayList<GuinanResult>();
		LinkedList<String> keywords = new LinkedList<String>();
		LinkedList<LinkedList<String>> tmpList = new LinkedList<LinkedList<String>>();
		SemanticFingerprintGuinanResult result = new SemanticFingerprintGuinanResult(keywords,tmpList);
		LinkedList<String> relatedWords = new LinkedList<String>();
		ArrayList<GuinanClientResult> clientRes = new ArrayList<GuinanClientResult>();
		thread = false;
		if(thread){
			clientRes = makeThreads(query);
		}
		else{
			
		if(query == null)
			return res;
		String[] tmp = query.split(",");
		
		for(int i=0;i<tmp.length;i++){
			keywords.add(i,tmp[i]);
			}
		
		Model rdfgraph = WTPGraph.getRDFGraph(MainClass.processKeyWords(keywords));
		/*int i=0;
		for(String keyword : keywords){
			for(Node n : g.g){
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
		 * so for every keyword there should be a list of related words
		 * */
		time = System.currentTimeMillis() - time;
		time = time/1000;
		System.out.println("Needed time for query: "+time +"sec");
	
		System.out.println(result.toString());
		res.add(result);
//		g.display();
		System.out.println(res.toString());
		}
		return res;
	}
	/*
	 * getting result graph and nodes = keywords 
	 * res are nearby nodes -> make GuinanResult Graph 
	 * */
	private ArrayList<GuinanClientResult> makeThreads(String query){
		thread = false;
		String[] tmp = query.split(",");
		int length = tmp.length/2;
		String query1 = "";
		String query2 = "";
		for(int i = 0;i<length;i++)
		{
			query1 = query1.concat(tmp[i]+",");
			query2 = (query2).concat(tmp[i+length/2]+",");
		}
		GuinanThread threadone = new GuinanThread(this, client, query1);
		GuinanThread threadtwo = new GuinanThread(this, client, query2);
		ArrayList<GuinanClientResult> a = threadone.call();
//		threadone.run();
		ArrayList<GuinanClientResult> b = threadtwo.call();
		a.addAll(a.size(), b);

		return (ArrayList<GuinanClientResult>)a;
	}
	
	/*
	 * made 2 test runs dont know what to make out of the results
	 * 
	 * 	2 threads
	 *		Haskell: Needed time for query: 865.243sec
	 *		Miku,Vocaloid,NND,Japan: Needed time for query: 628.328sec
	 *	w/o thread
	 *		Haskell: Needed time for query: 893.501sec
	 *		Miku,Vocaloid,NND,Japan: 592.771sec
	 */
	
	
}