package de.ovgu.wdok.guinan;

import java.util.ArrayList;

import de.ovgu.wdok.guinan.graph.GuinanNode;

/**
 * GuinanOntologyResult is a datastructure for storing/mapping results from
 * Guinan's ontology connectors. An ontology result is basically a
 * semantical concept which will be stored as graph node later. In that way, it extends GuinanNode
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * @version 0.1
 */

public class GuinanOntologyResult extends GuinanNode{

	private ArrayList<String> sameAsLabels;
	/**
	 * 
	 * @param label primary identifier
	 */
	public GuinanOntologyResult(String label) {
		super(label);
		this.sameAsLabels = new ArrayList<String>();
	}

}
