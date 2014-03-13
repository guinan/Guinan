package misc;

import de.ovgu.wdok.guinan.graph.GuinanGraph;
import de.ovgu.wdok.guinan.graph.GuinanNode;

public class GraphTester {

	public GraphTester() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[]){
		GuinanGraph g = new GuinanGraph("Testgraph");
		g.addNode(new GuinanNode("1"));
		g.addNode(new GuinanNode("2"));
		g.addNode(new GuinanNode("3"));
		g.addNode(new GuinanNode("4"));
		g.addNode(new GuinanNode("5"));
		
		System.out.println(g);
		System.out.println(g.getConnectedComponents());
	}
}
