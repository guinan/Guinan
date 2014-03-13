package de.ovgu.wdok.guinan.graph;

import java.util.ArrayList;

/**
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.1
 * 
 *          GuinanGraph represents the semantic fingerprint of a (web) document.
 *          Nodes are concepts, edges are relations. Mathematically it's a
 *          directed, labeled multigraph since 2 nodes can be connected through
 *          multiple labeled edges, i.e. two semantical concepts can share
 *          multiple relations
 */
public class GuinanGraph {

	private String id;
	private ArrayList<GuinanNode> nodes;
	private ArrayList<GuinanEdge> edges;

	public GuinanGraph(String id) {
		this.id = id;
		this.nodes = new ArrayList<GuinanNode>();
		this.edges = new ArrayList<GuinanEdge>();
	}

	public GuinanGraph(String id, GuinanNode node) {
		this.id = id;
		this.nodes = new ArrayList<GuinanNode>();
		nodes.add(node);
		this.edges = new ArrayList<GuinanEdge>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<GuinanNode> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<GuinanNode> nodes) {
		this.nodes = nodes;
	}

	public ArrayList<GuinanEdge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<GuinanEdge> edges) {
		this.edges = edges;
	}

	public GuinanNode getNodeByLabel(String label) {
		for (GuinanNode n : this.getNodes()) {
			if (n.getLabel().equals(label))
				return n;
		}
		return null;
	}

	public int getNumberOfNodes() {
		return this.getNodes().size();
	}

	/**
	 * Adds a node to the graph, if it hasn't been present before
	 * 
	 * @param node
	 *            Node to be added
	 * @return true, if insertion of node was successful, false otherwise
	 */
	public boolean addNode(GuinanNode node) {
		if (!this.hasNode(node)) {
			this.getNodes().add(node);
			return true;
		}
		return false;
	}

	/**
	 * Adds an edge between two nodes to the graph, if that edge hasn't been
	 * present before
	 * 
	 * @param edge
	 *            edge to be inserted
	 * @return true, if edge has been added (and was not present before), false
	 *         otherwise
	 */
	public boolean addEdge(GuinanEdge edge) {
		if (edge.getStartnode() != null && edge.getEndnode() != null) {
			if (!this.hasEdge(edge)) {
				this.getEdges().add(edge);
				return true;
			}
		}
		return false;
	}

	/**
	 * searches for a node (or its label respectively) in the graph
	 * 
	 * @param node
	 *            node to be searched for
	 * @return true if a node with the same label is present in the graph,
	 *         otherwise false
	 */
	public boolean hasNode(GuinanNode node) {
		for (GuinanNode n : this.getNodes()) {
			if (n.equals(node))
				return true;
		}
		return false;
	}

	public boolean hasEdge(GuinanEdge edge) {
		// iterate over all edges for comparison
		for (GuinanEdge e : this.getEdges()) {
			if (e.equals(edge))
				return true;
		}
		return false;
	}

	public ArrayList<GuinanEdge> getEdgesForNode(GuinanNode node) {
		ArrayList<GuinanEdge> edges = new ArrayList<GuinanEdge>();
		for (GuinanEdge e : this.getEdges()) {
			if (e.getStartnode().equals(node))
				edges.add(e);
		}
		return edges;
	}

	/**
	 * Merges two graphs. The calling object is manipulated in that way, that
	 * nodes and edges from the graph passed as parameter, which have not been
	 * present in the graph from the calling object, are added
	 * 
	 * @param g
	 *            Graph whose nodes and edges are to be added to the graph from
	 *            the calling object
	 */
	public void mergeGraphs(GuinanGraph g) {
		// traverse node-wise through the graph passed as parameter
		for (GuinanNode node_candidate : g.getNodes()) {
			// if the node candidate was not present yet, add it to the graph
			if (!this.hasNode(node_candidate)) {
				this.addNode(node_candidate);
			}
		}
		// traverse edge-wise through the graph
		if (g.getEdges().size() > 0) {
			for (GuinanEdge edge_candidate : g.getEdges()) {
				if (!this.hasEdge(edge_candidate)) {
					this.addEdge(edge_candidate);
				}
			}
		}
	}

	/**
	 * get the neighbours of a node therefore, the method searches for all
	 * nodes, where node has edges with as well as nodes which have nodes
	 * pointing to node
	 * 
	 * @param node
	 *            node whose neighbours are searched for
	 * @return ArrayList with GuinanNodes who are neighbours of the node passed
	 *         as parameter
	 */
	public ArrayList<GuinanNode> getAllNeighbours(GuinanNode node) {
		ArrayList<GuinanNode> neighbour_list = new ArrayList<GuinanNode>();
		// list to store the nodes which are candidates for having edges
		// pointing to node

		for (GuinanEdge e : this.getEdges()) {
			if (e.getStartnode().equals(node))
				neighbour_list.add(e.getEndnode());
			if (e.getEndnode().equals(node)) {
				if (!neighbour_list.contains(e.getStartnode()))
					neighbour_list.add(e.getStartnode());
			}
		}

		return neighbour_list;
	}

	/**
	 * get all nodes the parameter node is pointing to
	 * 
	 * @param node
	 *            Find all GuinanNodes this node is pointing to
	 * @return ArrayList with direct neighbour nodes
	 */
	public ArrayList<GuinanNode> getDirectNeighbours(GuinanNode node) {
		ArrayList<GuinanNode> neighbour_list = new ArrayList<GuinanNode>();
		// list to store the nodes which are candidates for having edges
		// pointing to node

		for (GuinanEdge e : this.getEdgesForNode(node)) {
			neighbour_list.add(e.getEndnode());
		}

		return neighbour_list;
	}

	/**
	 * Computes the connected components of the graph
	 * 
	 * @return an ArrayList with GuinanGraphs each representing a connected
	 *         component
	 */
	public ArrayList<GuinanGraph> getConnectedComponents() {
		ArrayList<GuinanGraph> connected_components = new ArrayList<GuinanGraph>();
		// create a graph for each node
		int i = 0;
		if (this.getNumberOfNodes() == 1){
			System.out.println("No more nodes, exiting recursion");
			return connected_components;
		}
		else {
			for (GuinanNode node : this.getNodes()) {
				connected_components.add(new GuinanGraph("connected_component_"
						+ i, node));
				i++;
			}
			return getConnectedComponentsFromGraphs(connected_components);
		}

	}

	/**
	 * Computes connected components for a graph. In the first iteration, every
	 * node of the graph is a graph itself. The method looks for common edges
	 * between nodes or subgraphs, respectively. If two graphs share one or more
	 * common edges in the original graph, these graphs will be merged and the
	 * method is called recursively.
	 * 
	 * @param connected_components
	 *            ArrayList of graphs representing subgraphs (connected
	 *            components)
	 * @return
	 */
	private ArrayList<GuinanGraph> getConnectedComponentsFromGraphs(
			ArrayList<GuinanGraph> connected_components) {
		for (int i = 0; i < connected_components.size() - 1; i++) {
			for (int j = 1; j < connected_components.size(); j++) {
				GuinanGraph g1 = connected_components.get(i);
				GuinanGraph g2 = connected_components.get(j);
				// graphs are connected
				ArrayList<GuinanEdge> cedges = commonEdges(g1, g2);
				if (cedges.size() > 0) {
					// merge graphs
					g1.mergeGraphs(g2);
					// merge edges
					g1.getEdges().addAll(cedges);
					connected_components.remove(j);
					return getConnectedComponentsFromGraphs(connected_components);
				}
			}
		}
		return connected_components;
	}

	private ArrayList<GuinanEdge> getCommonEdgesForNodes(GuinanNode start,
			GuinanNode end) {
		ArrayList<GuinanEdge> edges = new ArrayList<GuinanEdge>();
		// iterate over all edges for comparison
		for (GuinanEdge e : this.getEdges()) {
			if (e.getStartnode().equals(start) && e.getEndnode().equals(end))
				edges.add(e);
		}
		return edges;
	}

	private ArrayList<GuinanEdge> commonEdges(GuinanGraph g1, GuinanGraph g2) {
		ArrayList<GuinanEdge> edges = new ArrayList<GuinanEdge>();
		for (GuinanNode node1 : g1.getNodes()) {
			for (GuinanNode node2 : g2.getNodes()) {

				edges.addAll(getCommonEdgesForNodes(node1, node2));
				edges.addAll(getCommonEdgesForNodes(node2, node1));
			}
		}
		return edges;
	}

	/**
	 * pretty print graph as adjacency list
	 */
	public String toString() {
		String pretty = "******" + this.getId() + "******\n\n";
		for (GuinanNode n : this.getNodes()) {
			pretty += n + "\n";
			for (GuinanEdge e : this.getEdgesForNode(n)) {
				pretty += "\t\t" + e;
			}
		}
		return pretty;
	}

	/**
	 * Creates a deep copy of the graph
	 * 
	 * @return GuinanGraph with the same nodes and edges as the calling graph
	 */
	public GuinanGraph clone() {

		GuinanGraph clonedgraph = new GuinanGraph(this.getId() + "_clone");

		for (GuinanNode n : this.getNodes()) {
			clonedgraph.addNode(new GuinanNode(n.getLabel()));
		}
		for (GuinanEdge e : this.getEdges()) {
			clonedgraph.addEdge(new GuinanEdge(new GuinanNode(e.getStartnode()
					.getLabel()), new GuinanNode(e.getEndnode().getLabel()), e
					.getLabel()));
		}
		return clonedgraph;

	}

}
