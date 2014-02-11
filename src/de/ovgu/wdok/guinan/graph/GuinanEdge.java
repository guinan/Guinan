package de.ovgu.wdok.guinan.graph;

/**
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.1
 */

public class GuinanEdge {

	String label;
	GuinanNode startnode;
	GuinanNode endnode;

	public GuinanEdge(GuinanNode startnode, GuinanNode endnode, String label) {
		super();
		this.label = label;
		this.startnode = startnode;
		this.endnode = endnode;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public GuinanNode getStartnode() {
		return startnode;
	}

	public void setStartnode(GuinanNode startnode) {
		this.startnode = startnode;
	}

	public GuinanNode getEndnode() {
		return endnode;
	}

	public void setEndnode(GuinanNode endnode) {
		this.endnode = endnode;
	}

	public boolean equals(GuinanEdge edge) {
		return (edge.getStartnode().equals(this.getStartnode())
				&& edge.getEndnode().equals(this.getEndnode()) && edge
				.getLabel().equals(this.getLabel()));
	}
	
	public String toString(){
		return "["+this.getStartnode()+"]"+" ---["+this.getLabel()+"]---> ["+this.getEndnode()+"]\n";
	}

}
