package de.ovgu.wdok.guinan.connector.SemanticFingerprint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.ovgu.wdok.guinan.nlp.KeywordExtractor;

@Path("SemFP")
public class SFPGeneratorforDoc {
	
	@GET
	@Path("/getSFPforDoc")
	@Produces(MediaType.TEXT_PLAIN)
	public String getSFPForDoc(@Context UriInfo info){
		
		String uri = info.getQueryParameters().getFirst("uri");
		
		Document doc=null;
		try {
			doc = Jsoup.connect("http://jsoup.org").get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String plaintext = extractPlainText(doc);
		
		ArrayList <String> topterms = computeTopTerms(plaintext);
		
		return topterms.toString();
	}

	private ArrayList<String> computeTopTerms(String plaintext) {
		KeywordExtractor kw = new KeywordExtractor();
		return kw.extractKeywords(plaintext);
		
	}

	private String extractPlainText(Document doc) {

		return doc.text();
	}
}
