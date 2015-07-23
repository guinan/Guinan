package de.ovgu.wdok.guinan;

import filterheuristics.InterConceptConntecting;
import graph.GraphCleaner;
import graph.WTPGraph;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.map.ObjectMapper;

import com.hp.hpl.jena.rdf.model.Model;

import dbpedia.BreadthFirstSearch;
import dbpedia.BreadthFirstSearch.ResultSet;
import dbpedia.KeyWordSearch;
import dbpedia.KeyWordSearch.SearchResult;

//import graph.GraphCleaner.Path;

@Path("SFP")
public class GuinanIterativeSFPGenerator {

	private WTPGraph graph;
	public static int maxSearchResults = 10;
	public static int maxSearchDepth = 3;
	// Initial cleaning of the graph
	public static int maxPathLength = maxSearchDepth;
	public static int maxPathExtensionLength = 2;
	// Heuristics
	public static int numRelevantNodesFilter = 10;
	public static int minSupportNodesFilter = 5;

	@GET
	@Path("/genSFP")
	@Produces(MediaType.TEXT_XML)
	public String genSFP(@Context UriInfo info) {
		// convert string to linked list with strings
		// LinkedList<String> keywords = new LinkedList<String>(
		// Arrays.asList(query));
		List<String> keywords = info.getQueryParameters().get("q");
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
		//heuristic.filterInterconntection(graph, paths, correspondingKeywords);

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
		return output.toString();
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

	class StringOutputStream extends OutputStream {

		StringBuilder mBuf;

		public void write(int bytes) throws IOException {
			mBuf.append((char) bytes);
		}

		public String getString() {
			return mBuf.toString();
		}
	}
}
