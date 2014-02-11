package de.ovgu.wdok.guinan.graph;


public class GuinanNode {

	private String label; // label

	public GuinanNode(String label) {

		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
	
	public boolean equals(GuinanNode node){
		return this.getLabel().equals(node.getLabel());
	}

	public String toString(){
		return this.getLabel();
	}
}
