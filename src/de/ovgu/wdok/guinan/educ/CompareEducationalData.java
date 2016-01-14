package de.ovgu.wdok.guinan.educ;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CompareEducationalData {
	
	final static String basedir ="/home/kati/Documents/diss/LOM/sample/";
	
	public static void main(String args[]){
		//reading LOM files
		final File lomfolder = new File(basedir);
		for (final File lomfile : lomfolder.listFiles()){
			if (lomfile.isFile()){
				System.out.println("Reading file "+lomfile.getName());
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder=null;
				Document doc = null;
				try {
					dBuilder = dbFactory.newDocumentBuilder();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					doc = dBuilder.parse(lomfile);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(doc != null){
					doc.getDocumentElement().normalize();
					//get first identifier element
					XPath xpath = XPathFactory.newInstance().newXPath();
			        XPathExpression expr1=null;
					try {
						expr1 = xpath.compile("//identifier/entry");
					} catch (XPathExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        NodeList nodes=null;
					try {
						nodes = (NodeList)expr1.evaluate(doc, XPathConstants.NODESET);
					} catch (XPathExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String resourceloc = nodes.item(0).getTextContent();
					System.out.println("resource location: "+resourceloc);
					if (resourceloc != null)
					{
						//find resource and gather data automatically
					}
					NodeList nList = doc.getElementsByTagName("educational");
					for (int i = 0 ; i<nList.getLength(); i++){
						Node nNode = nList.item(i);
						
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {

							Element eElement = (Element) nNode;

							System.out.println("interactivity type: " + eElement.getElementsByTagName("interactivitytype").item(0).getTextContent());
							System.out.println("interactivity level : " + eElement.getElementsByTagName("interactivitylevel").item(0).getTextContent());
							System.out.println("semantic density : " + eElement.getElementsByTagName("semanticdensity").item(0).getTextContent());
							System.out.println("difficulty : " + eElement.getElementsByTagName("difficulty").item(0).getTextContent());
							System.out.println("learning resource type : " + eElement.getElementsByTagName("learningresourcetype").item(0).getTextContent());
							System.out.println("typical age range : " + eElement.getElementsByTagName("typicalagerange").item(0).getTextContent());

							System.out.println("\n-------------------------------------------------------------\n\n");
						}
					}
				}
			}
				
		}
		
	}

}
