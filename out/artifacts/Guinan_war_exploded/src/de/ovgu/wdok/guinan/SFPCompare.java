package de.ovgu.wdok.guinan;

import graph.WTPGraph;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

@Path("similarity")
public class SFPCompare {

	
	public SFPCompare()
	{
		
	}
	
	@POST
	@Path("/getSim")
	@Consumes(MediaType.APPLICATION_JSON)
	public static String getGraphEditSimilarity(final SFPPair sfp_pair){
		//unmarshalling the sfps
		System.out.println("Trying to unmarshall SFPs ... ");
	
		WTPGraph graph1 =  WTPGraph.fromXML(sfp_pair.sfp1);
		WTPGraph graph2 =  WTPGraph.fromXML(sfp_pair.sfp2);
		
		//calculating similarity
		double distance = 0;
		Collection<Node> g1_nodes = graph1.getGraph().getNodeSet();
		Collection<Node> g2_nodes = graph2.getGraph().getNodeSet();
		
		Collection<Edge> g1_edges = graph1.getGraph().getEdgeSet();
		Collection<Edge> g2_edges = graph2.getGraph().getEdgeSet();
		//check for common nodes
		
		boolean found = false;
		
		for(Node g1Node: g1_nodes){
			String label1 = g1Node.getId();
			//look if you can find the node
			found=false;
			for(Node g2Node : g2_nodes){
				if (label1.equals(g2Node.getId()))
					found=true;
			}
			if (!found)
				distance++;
		}
		

		//check for common nodes (the other way around)
		for(Node g2Node: g2_nodes){
			String label2 = g2Node.getId();
			found=false;
			for(Node g1Node : g1_nodes){
				
				if(label2.equals(g1Node.getId()))
					found=true;
			}
			if (!found)
				distance++;
		}
		
		//check for common edges
		for(Edge g1Edge: g1_edges){
			//compare only the labels, since ids are unique
			String e1label = g1Edge.getAttribute("ui.label");
			Node src = g1Edge.getNode0();
			Node dest = g1Edge.getNode1();
			found=false;
			for(Edge g2edge : g2_edges){
				if(g2edge.getAttribute("ui.label").equals(e1label) && src.getAttribute("ui.label").equals(e1label) && dest.getAttribute("ui.label").equals(e1label) )
					found=true;
			}
			if (!found)
				distance++;
		}
		//check for common edges (the other way around)
		for(Edge g2Edge: g2_edges){
			//compare only the labels, since ids are unique
			String e2label = g2Edge.getAttribute("ui.label");
			Node src = g2Edge.getNode0();
			Node dest = g2Edge.getNode1();
			found=false;
			for(Edge g1edge : g1_edges){
				if(g1edge.getAttribute("ui.label").equals(e2label) && src.getAttribute("ui.label").equals(e2label) && dest.getAttribute("ui.label").equals(e2label) )
					found=true;
			}
			if (!found)
				distance++;
		}
		double maxDist = g1_edges.size() + g2_edges.size() + g1_nodes.size() + g2_nodes.size();
		double similarity = (maxDist - distance) / maxDist;
		System.out.println("nodes graph 1: "+ graph1.getNodeCount()+", \t nodes graph 2: "+graph2.getNodeCount());
		System.out.println("edges graph 1: "+ graph1.getEdgeCount()+", \t nodes graph 2: "+graph2.getEdgeCount());
		System.out.println("calculated distance: "+distance);
		return Double.toString(similarity);
	}
}
