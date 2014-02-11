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
	 * @param startnode
	 *            Start node of the edge
	 * @param endnode
	 *            End node of the edge
	 * @param label
	 *            Label of the edge
	 * @return
	 */
	public boolean addEdge(GuinanEdge edge) {
		if (!this.hasEdge(edge)) {
			this.getEdges().add(edge);
			return true;
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
		for (GuinanEdge edge_candidate : g.getEdges()) {
			if (!this.hasEdge(edge_candidate)) {
				this.addEdge(edge_candidate);
			}
		}
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

	// deep copy
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
