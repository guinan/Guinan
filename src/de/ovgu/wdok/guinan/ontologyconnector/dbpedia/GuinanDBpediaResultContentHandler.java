package de.ovgu.wdok.guinan.ontologyconnector.dbpedia;

/**
 * 
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.ovgu.wdok.guinan.GuinanOntologyResult;

/**
 * GuinanSlideshowResultContentHandler is a special content handler for the
 * SAXParser, responsible for mapping XML Nodes to GuinanResult fields.<br>
 * <br><br>
 * Only the methods {@code startElement} and {@code endElement} are implemented, all other
 * methods, that have to be overwritten, are not needed
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 * 
 */
public class GuinanDBpediaResultContentHandler extends DefaultHandler {

	private ArrayList<GuinanOntologyResult> resultlist;
	private GuinanOntologyResult gor;
	private StringBuilder currentValue;

	public ArrayList<GuinanOntologyResult> getGuinanOntologyResultList() {
		return resultlist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	/**
	 * when the SAX parser meets a starting element "Result", we will inspect
	 * it. The name of the element is saved in the qName variable, localname is
	 * almost always empty
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		currentValue = new StringBuilder();
		if (qName.equalsIgnoreCase("Result")) {
			// create new GuinanResult object for each slideshow
			gor = new GuinanOntologyResult();
			if (resultlist==null){
				resultlist = new ArrayList<GuinanOntologyResult>();
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("Label")) {
			gor.setLabel(currentValue.toString());
		} else if (qName.equals("Description")) {
			gor.setDescription(currentValue.toString());
		}
		else if (qName.equals("URI")){
			gor.setURI(currentValue.toString());
		}
		else if(qName.equals("Result")){
			resultlist.add(gor);
		}
		//other fields still to be implemented
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		currentValue.append(ch, start, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	@Override
	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub

	}

}

/**
 *  IntValueComparator can sort key,value pairs according to the value,
 *         which is an integer
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 *        
 */
class IntValueComparator implements Comparator<Map.Entry<String, Integer>> {
	@Override
	public int compare(Map.Entry<String, Integer> o1,
			Map.Entry<String, Integer> o2) {
		return (o2.getValue()).compareTo(o1.getValue());
	}

}
