package de.ovgu.wdok.guinan.connector.SemanticFingerprint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.client.Client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.EnglishSnowballStemmerFactory;
import org.tartarus.snowball.util.StemmerException;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import de.ovgu.wdok.guinan.nlp.KeywordExtractor;

 
@Path("SemFP")
public class SFPGeneratorforDoc {
	
	//number of terms from the doc the SFP will be generated for
	private static final int maxTermCount = 10;
	//sfp gen location
	private static final String sfpgenloc = "http://localhost:10080/Guinan/webapp/SFP/genSFP/";
	
	private HashMap<Integer,Boolean> jobqueue;
	
	//ugly hack, using a real triple store would be nicer, but that's sufficient for now
	private HashMap<Integer,String> sfp_store;
	
	private Client client;
	
	
	public SFPGeneratorforDoc() {
		this.client = new Client();
		this.jobqueue = new HashMap<Integer,Boolean>();
		this.sfp_store = new HashMap<Integer,String>();
	}

	@GET
	@Path("/getSFPforDoc")
	@Produces(MediaType.TEXT_HTML)
	public String getSFPForDoc(@Context UriInfo info){
		
		String uri = info.getQueryParameters().getFirst("uri");
		
		Document doc=null;
		try {
			doc = Jsoup.connect(uri).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(uri);
			e.printStackTrace();
		}

		String plaintext = extractPlainText(doc);
		
		ArrayList <String> topterms = computeTopTerms(plaintext);
		//build query string for generating SFP
		MultivaluedMap<String,String> query=new MultivaluedMapImpl();
		for(String term : topterms){
			query.add("q", term);
		}
		String xml_res="";
		
		try{
			xml_res = this.client.resource(sfpgenloc).queryParams(query).get(String.class);
		}
		catch(Error e) {
			e.printStackTrace();
		}
		
		System.out.println("*****************Answer from service: "+xml_res);
		return xml_res;
	}

	private ArrayList<String> computeTopTerms(String plaintext) {
		ArrayList<String> result = new ArrayList<String>();
		HashMap<String,Integer> stems_occ = new HashMap<String,Integer>();
		KeywordExtractor kw = new KeywordExtractor();
		ArrayList<String> origterms = kw.extractKeywords(plaintext);
		for (String term: origterms){
			
			String stem = this.getStem(term.toLowerCase());
			//word stem already in list?
			if(stems_occ.containsKey(stem)){
				int curr_val = stems_occ.get(stem);
				stems_occ.put(stem, curr_val+1);
			}
			else{
				stems_occ.put(stem, 1);
			}
		}
		//order map by descending number of occurrences of terms
		Map<String, Integer> sortedMapDesc = sortByComparator(stems_occ, false);
       // printMap(sortedMapDesc);
        //we want only the keys which are the terms itself
        for(Entry <String, Integer> entry : sortedMapDesc.entrySet()){
        	result.add(entry.getKey());
        }
        //System.out.println(result.toString());
        ArrayList<String> retlist = new ArrayList<String>(result.subList(0, maxTermCount));
		return  retlist;
	}

	private String extractPlainText(Document doc) {

		return doc.text();
	}
	
	public String getStem(String term){
		try {
				return EnglishSnowballStemmerFactory.getInstance().process(term);
				
			} catch (StemmerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return "";
	}
	
    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order)
    {

        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>()
        {
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
    
    public static void printMap(Map<String, Integer> map)
    {
        for (Entry<String, Integer> entry : map.entrySet())
        {
            System.out.println("Key : " + entry.getKey() + " Value : "+ entry.getValue());
        }
    }
	
}
