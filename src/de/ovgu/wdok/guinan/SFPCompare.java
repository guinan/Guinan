package de.ovgu.wdok.guinan;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import graph.WTPGraph;

@Path("similarity")
public class SFPCompare {

	
	public SFPCompare()
	{
		
	}
	
	@POST
	@Path("/getSim")
	public static Double getGraphEditSimilarity(final SFPPair sfp_pair){
		//unmarshalling the sfps
		WTPGraph graph1 =  WTPGraph.fromXML(sfp_pair.sfp1);
		WTPGraph graph2 =  WTPGraph.fromXML(sfp_pair.sfp2);
		
		//calculating similarity
		double distance = 0;
		Collection<Node> g1_nodes = graph1.getGraph().getNodeSet();
		Collection<Node> g2_nodes = graph2.getGraph().getNodeSet();
		
		Collection<Edge> g1_edges = graph1.getGraph().getEdgeSet();
		Collection<Edge> g2_edges = graph2.getGraph().getEdgeSet();
		//check for common nodes
		
		for(Node g1Node: g1_nodes){
			if(!g2_nodes.contains(g1Node))
				distance++;
		}
		//check for common nodes (the other way around)
		for(Node g2Node: g2_nodes){
			if(!g1_nodes.contains(g2Node))
				distance++;
		}
		
		//check for common edges
		for(Edge g1Edge: g1_edges){
			if(!g2_edges.contains(g1Edge))
				distance++;
		}
		//check for common edges (the other way around)
		for(Edge g2Edge: g2_edges){
			if(!g1_edges.contains(g2Edge))
				distance++;
		}
		double maxDist = g1_edges.size() + g2_edges.size() + g1_nodes.size() + g2_nodes.size();
		double similarity = (maxDist - distance) / maxDist;
		return similarity;
	}
}
