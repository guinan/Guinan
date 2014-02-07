/**
 * 
 */
package de.ovgu.wdok.guinan.connector.slideshare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.ovgu.wdok.guinan.GuinanResult;

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
public class GuinanSlideshowResultContentHandler extends DefaultHandler {

	private GuinanResult gr;
	private String currentValue;
	private SortedMap<String, Integer> tag_counter_list;
	private int currentTagCounter = 0;

	public GuinanResult getGuinanResult() {
		return gr;
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
	 * when the SAX parser meets a starting element "slideshow", we will inspect
	 * it. The name of the element is saved in the qName variable, localname is
	 * almost always empty
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (qName.equalsIgnoreCase("Slideshow")) {
			// create new GuinanResult object for each slideshow
			gr = new GuinanResult();
			// saves the tag and their occurences
			tag_counter_list = new TreeMap<String, Integer>();
		}
		// if we find a "Tag" element, we will extract its "count" attribute
		if (qName.equals("Tag")) {
			currentTagCounter = Integer.parseInt(atts.getValue("Count"));
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
		if (qName.equals("Title")) {
			gr.setTitle(currentValue);
		} else if (qName.equals("Description")) {
			gr.setContent(currentValue);
		} else if (qName.equals("ThumbnailURL")) {
			gr.set_thumbnail_uri("http:" + currentValue);
		} else if (qName.equals("Language")) {
			gr.set_language(currentValue);
		} else if (qName.equals("URL"))
			gr.set_location(currentValue);
		else if (qName.equals("Tag")) {
			tag_counter_list.put(currentValue, currentTagCounter);
		}
		// slideshow element is closed
		else if (qName.equals("Slideshow")) {
			gr.setDocumenttype(GuinanResult.RESOURCE_TYPE_SLIDESHOW);
			// create a sorted list, that can contains the tags sorted by
			// occurrence in descending order
			ArrayList<Entry<String, Integer>> sorted_tag_count_list = new ArrayList<Entry<String, Integer>>();
			sorted_tag_count_list.addAll(tag_counter_list.entrySet());
			// use the intvaluecomparator to sort the entries
			Collections.sort(sorted_tag_count_list, new IntValueComparator());
			// extract only the tags and put them in sorted order into the tag
			// field of GuinanResult
			gr.setContent_tags(extractPureTags(sorted_tag_count_list));
		}
	}

	// extract the tags (key) from the <tags,occurence> pairs
	private ArrayList<String> extractPureTags(
			ArrayList<Entry<String, Integer>> sorted_tag_count_list) {
		ArrayList<String> result = new ArrayList<String>();
		for (Entry<String, Integer> e : sorted_tag_count_list) {
			result.add(e.getKey());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		currentValue = new String(ch, start, length);
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
 * 
 * @author <a href="mailto:kkrieger@ovgu.de">Katrin Krieger</a>
 *         IntValueComparator can sort key,value pairs according to the value,
 *         which is an integer
 */
class IntValueComparator implements Comparator<Map.Entry<String, Integer>> {
	@Override
	public int compare(Map.Entry<String, Integer> o1,
			Map.Entry<String, Integer> o2) {
		return (o2.getValue()).compareTo(o1.getValue());
	}

}
