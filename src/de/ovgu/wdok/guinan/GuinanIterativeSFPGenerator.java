package de.ovgu.wdok.guinan;

import filterheuristics.InterConceptConntecting;
import graph.GraphCleaner;
import graph.WTPGraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.sun.jersey.api.client.ClientResponse.Status;

import dbpedia.BreadthFirstSearch;
import dbpedia.BreadthFirstSearch.ResultSet;
import dbpedia.KeyWordSearch;
import dbpedia.KeyWordSearch.SearchResult;
//import graph.GraphCleaner.Path;

@Path("SFP")
public class GuinanIterativeSFPGenerator {

	private WTPGraph graph;
	public static final int maxSearchResults = 10;
	public static final int maxSearchDepth = 3;
	// Initial cleaning of the graph
	public static final int maxPathLength = maxSearchDepth;
	public static final int maxPathExtensionLength = 1;
	// Heuristics
	public static final int numRelevantNodesFilter = 10;
	public static final int minSupportNodesFilter = 5;

	@GET
	@Path("genSFP/{query_string}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response genSFP(@PathParam("query_string") final String query) {
		// convert string to linked list with strings
		LinkedList<String> keywords = new LinkedList<String>(
				Arrays.asList(query));
		// create initial nodes for the kw

		// map for the semantic concepts found in the ontology and their
		// corresponding keyword, used for searching them
		Map<String, String> correspondingKeywords = new HashMap<String, String>();

		KeyWordSearch s = new KeyWordSearch();
		List<SearchResult> res = s.search(keywords, maxSearchResults,
				correspondingKeywords);
		System.out.println(res);
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
		heuristic.filterInterconntection(graph, paths, correspondingKeywords);

		/**
		 * Filters the n Nodes which occur most frequently in the paths
		 */
		// heuristic.filterNMostFrequentlyOccuring(graph, paths,
		// numRelevantNodesFilter, correspondingKeywords);

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
	//	ArrayList<ArrayList<String>> graph = new ArrayList<ArrayString>();
		return makeCORS(Response.status(Status.OK).entity(graph), "");
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
}
