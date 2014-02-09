package de.ovgu.wdok.guinan.graph;

import java.util.ArrayList;
import java.util.Hashtable;

public class GuinanEdge {

	String startnodelabel;
	ArrayList<GuinanNode> edgelist;

	public GuinanEdge() {
		super();
		this.startnodelabel = "";
		this.edgelist = new ArrayList<GuinanNode>();
	}

	public GuinanEdge(String label) {
		super();
		this.startnodelabel = label;
		this.edgelist = new ArrayList<GuinanNode>();
	}

	public GuinanEdge(String label, GuinanNode node) {
		super();
		this.edgelist = new ArrayList<GuinanNode>();
		this.edgelist.add(node);
		this.startnodelabel = label;
	}

	public GuinanEdge(String label, ArrayList<GuinanNode> edgelist) {
		super();
		this.startnodelabel = label;
		this.edgelist = edgelist;
	}

	
	public String getStartnodelabel() {
		return startnodelabel;
	}

	public void setStartnodelabel(String startnodelabel) {
		this.startnodelabel = startnodelabel;
	}

	public ArrayList<GuinanNode> getEdges() {
		return this.edgelist;
	}

}
