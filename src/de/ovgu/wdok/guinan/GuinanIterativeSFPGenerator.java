package de.ovgu.wdok.guinan;

import filterheuristics.InterConceptConntecting;
import graph.GraphCleaner;
import graph.WTPGraph;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.jena.rdf.model.Model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

import dbpedia.BreadthFirstSearch;
import dbpedia.BreadthFirstSearch.ResultSet;
import dbpedia.KeyWordSearch;
import dbpedia.KeyWordSearch.SearchResult;
//import javax.persistence.Entity;


@Path("SFP")
public class GuinanIterativeSFPGenerator {

	private static volatile ConcurrentHashMap<UUID, SFPGenJob> jobqueue = new ConcurrentHashMap<UUID, SFPGenJob>();
	private static volatile ConcurrentHashMap<UUID, String> sfplist = new ConcurrentHashMap<UUID, String>();
	private WTPGraph graph;
	public static int maxSearchResults = 10;
	public static int maxSearchDepth = 3;
	// Initial cleaning of the graph
	public static int maxPathLength = maxSearchDepth;
	public static int maxPathExtensionLength = 1;
	// Heuristics
	public static int numRelevantNodesFilter = 10;
	public static int minSupportNodesFilter = 5;
	private ExecutorService executor = Executors.newCachedThreadPool();

	@GET
	@Path("/genSFP")
	@Produces(MediaType.TEXT_HTML)
	public Response genSFP(@Context UriInfo info) {
		// convert string to linked list with strings
		// LinkedList<String> keywords = new LinkedList<String>(
		// Arrays.asList(query));
		final List<String> keywords = info.getQueryParameters().get("q");
		// get other params
		if (info.getQueryParameters().containsKey("maxSearchResults")) {
			String maxSearchR = info.getQueryParameters()
					.get("maxSearchResults").get(0);
			if (maxSearchR != null)
				maxSearchResults = Integer.parseInt(maxSearchR);
		}
		if (info.getQueryParameters().containsKey("maxSearchDepth")) {
			String maxSearchD = info.getQueryParameters().get("maxSearchDepth")
					.get(0);
			if (maxSearchD != null)
				maxSearchDepth = Integer.parseInt(maxSearchD);
		}
		if (info.getQueryParameters().containsKey("maxPathLength")) {
			String maxPathL = info.getQueryParameters().get("maxPathLength")
					.get(0);
			if (maxPathL != null)
				maxPathLength = Integer.parseInt(maxPathL);
		}
		if (info.getQueryParameters().containsKey("numRelevantNodes")) {
			String numRelNodes = info.getQueryParameters()
					.get("numRelevantNodes").get(0);
			if (numRelNodes != null)
				numRelevantNodesFilter = Integer.parseInt(numRelNodes);
		}

		/*
		 * LinkedList <String> keywords = new LinkedList<String>();
		 * keywords.add("haskell"); keywords.add("higher order function");
		 * keywords.add("map"); keywords.add("functional");
		 */
		// TODO split keywords
		System.out.println("keywords from uri params: " + keywords);
		// create initial nodes for the kw

		final UUID jobID = UUID.randomUUID();
		// trigger SFP generation in new thread
		executor.execute(new Runnable() {
			public void run() {
				_genSFPinQueue(keywords, jobID);
			}
		});
		URI jobQueueUri;
		try {
			jobQueueUri = new URI("SFP/jobQueue/" + jobID.toString());
			return Response
					.status(202)
					.location(jobQueueUri)
					.entity("<html>Request received. Processing .... check back later @ <a href=\""+info.getBaseUri()+
							 jobQueueUri.toASCIIString()+"\">"+jobQueueUri.toASCIIString()+"</a></html>").build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		// if something went wrong send an error return code
		return Response.serverError().build();
	}

	private Response makeCORS(ResponseBuilder req, String returnMethod) {
		ResponseBuilder rb = req.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

		// if the CORS headers are set, use these or overwrite the settings from
		// above respectively
		if (!"".equals(returnMethod)) {
			rb.header("Access-Control-Allow-Headers", returnMethod);
		}

		return rb.build();

	}

	private void _genSFPinQueue(List<String> keywords, UUID jobID) {
		// create timestamp
		Date now = Calendar.getInstance().getTime();
		// create jobqueue entry
		jobqueue.put(jobID, new SFPGenJob(SFPGenJob.PROCESSING, now));
		System.out.println("[_genSFPinQueue] size of job queue: "+jobqueue.size());
		System.out.println("Job accessible @ "+this.jobqueue.toString());
		// map for the semantic concepts found in the ontology and their
		// corresponding keyword, used for searching them
		Map<String, String> correspondingKeywords = new HashMap<String, String>();

		KeyWordSearch s = new KeyWordSearch();
		List<SearchResult> res = s.search(keywords, maxSearchResults,
				correspondingKeywords);
		System.out.println("Resultlist from KW search: " + res);
		List<String> request = KeyWordSearch.toUriList(res);
		System.out.println("Starting BFS...");
		BreadthFirstSearch lc = new BreadthFirstSearch();
		ResultSet result = lc.getConnections(request, maxSearchDepth);
		System.out.println("...Done");

		// -- 2) create the graph
		System.out.println("Creating the initial graph...");
		WTPGraph graph = WTPGraph.createFromResultSet(result,
				"Semantic Fingerprint");
		System.out.println("...Done");

		// -- 3) remove specific edges
		// graph.removeEdgesByName("ject");
		// graph.removeEdgesByName("paradigm");
		// graph.removeEdgesByName("influencedBy");
		// graph.removeEdgesByName("influenced");
		// graph.removeEdgesByName("typing");
		// graph.removeEdgesByName("license");

		// -- 4) tidy graph
		System.out.print("Tidying graph (" + graph.getNodeCount() + " Nodes, "
				+ graph.getEdgeCount() + " Edges) ...");
		GraphCleaner c = new GraphCleaner(graph.getGraph(), result.requestNodes);
		LinkedList<graph.GraphCleaner.Path> paths = c.clean(maxPathLength,
				maxPathExtensionLength);
		System.out.println(" Done (" + graph.getNodeCount() + " Nodes, "
				+ graph.getEdgeCount() + " Edges, " + paths.size() + " Paths)");

		// --4.2) heuristics finger print selection
		InterConceptConntecting heuristic = new InterConceptConntecting();

		/**
		 * Filters all Nodes that have paths to other Nodes which correspond to
		 * a different keyword
		 */
		//heuristic.filterInterconntection(graph, paths,
		// correspondingKeywords);

		/**
		 * Filters the n Nodes which occur most frequently in the paths
		 */
		heuristic.filterNMostFrequentlyOccuring(graph, paths,
				numRelevantNodesFilter, correspondingKeywords);

		/**
		 * Selects the cluster which corresponds to the most different keywords
		 */
		heuristic.filterClusterByInterconnectionLevel(graph,
				correspondingKeywords);

		/**
		 * Selects the biggest cluster
		 */
		heuristic.filterClusterBySize(graph);

		/**
		 * Selects the cluster whose nodes occur most frequently in the paths
		 */
		// ArrayList<ArrayList<String>> graph = new ArrayList<ArrayString>();
		// convert WTP graph to RDF
		Model rdfgraph = WTPGraph.getRDFGraph(graph);
		rdfgraph.write(System.out);
		/*
		 * ObjectMapper mapper = new ObjectMapper();
		 * 
		 * 
		 * try { return
		 * makeCORS(Response.status(Status.OK).entity(mapper.writeValueAsString
		 * (rdfgraph.write(System.out))), ""); } catch (JsonGenerationException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); } catch
		 * (JsonMappingException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace();
		 * 
		 * } return makeCORS(Response.status(Status.OK), "");
		 */

		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			public String toString() {
				return this.string.toString();
			}
		};
		rdfgraph.write((OutputStream) output);
		// put result in sfplist
		this.sfplist.put(jobID, output.toString());

		// get the job object of current jobid and update it
		SFPGenJob currJob = this.jobqueue.get(jobID);
		currJob.updateStatus(SFPGenJob.FINISHED);
		// update timestamp
		now = Calendar.getInstance().getTime();
		currJob.updateTimestamp(now);
		this.jobqueue.put(jobID, currJob);
	}

	// TODO
	@GET
	@Path("/jobQueue/{jobID}")
	// @Produces(MediaType.TEXT_XML)
	public Response checkJobQueue(@PathParam("jobID") String jobID) {
		// does job exist?
		System.out.println("[checkJobQueue] queue length = "+jobqueue.size());
		System.out.println("[checkJobQueue] jobID = "+jobID);
		if (jobqueue.containsKey(UUID.fromString(jobID))) {
			System.out.println("Found job ID");
			// if job is not finished yet return 200
			if (jobqueue.get(UUID.fromString(jobID)).getStatus() == SFPGenJob.PROCESSING)
				return Response.status(Status.OK)
						.entity("Still processing - please check back later.")
						.build();
			// if job is finished and SFP has been created
			else {
				// return path to SFP resource
				URI sfp_uri;
				try {
					sfp_uri = new URI("SFP/sfplist/" + jobID);
					// update jobQueue
					SFPGenJob currjob = jobqueue.get(UUID.fromString(jobID));
					currjob.updateStatus(SFPGenJob.GONE);
					jobqueue.put(UUID.fromString(jobID), currjob);
					return Response.status(Status.SEE_OTHER).location(sfp_uri)
							.build();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return Response.serverError().build();

			}
		} else
			return Response.serverError().entity("No such job ID").build();

	}
	@GET
	@Path("/sfplist/{jobID}")
	@Produces(MediaType.TEXT_XML)
	public Response fetchSFP(@PathParam("jobID") String jobID){
		if(sfplist.containsKey(UUID.fromString(jobID))){
			return Response.status(Status.OK).entity(sfplist.get(UUID.fromString(jobID))).build();
		}
		else
			return Response.status(Status.NOT_FOUND).entity("No such SFP ID").build();
	}
	/***********************************************************************************************/

	@GET
	@Path("/testSim")
	public Response testSim(){
		
		//try to get the last two SFPs from the sfplist
		java.util.Enumeration<String> enu = sfplist.elements();
		ArrayList<String> entries =  Collections.list(enu);
		System.out.println("No of entries in FP list: "+entries.size());
		if (entries.size() < 2)
			return Response.status(Status.NOT_FOUND).entity("Less than two fingerprints in sfplist ... nothing to compare").build();
		String sfp1="";
		String sfp2="";
		try{
			sfp1 = entries.get(0);
			sfp2 = entries.get(1);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		Client client = Client.create();

		WebResource webResource = client
		   .resource("http://localhost:10080/Guinan/webapp/similarity/getSim");
		
		HashMap<String,String> params = new HashMap<String, String>();
		params.put("sfp1", sfp1);
		params.put("sfp2", sfp2);
		
		//String input = "{\"sfp1\":\""+sfp1+"\",\"sfp2\":\""+sfp2+"\"}";
		//System.out.println("Input for similarity: "+input);
		ObjectMapper mapper = new ObjectMapper();
		String input="";
		try {
			input = mapper.writeValueAsString(params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		System.out.println("JSON input: ");
		ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, input);
		
			System.out.println("Response from Service: "+response.getStatus() + "\n\n ");
	
		System.out.println("Output from Server .... \n");
		String output = response.getEntity(String.class);
		
		return Response.status(Status.OK).entity(output).build();
	}
}
