package de.ovgu.wdok.guinan.educ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.ipeirotis.readability.Readability;
import com.sun.jersey.spi.resource.Singleton;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.DefaultExtractor;

@Singleton
@Path("EM")
public class ComputeEducationalMetadata {

	private Detector detector;
	private Readability r;
	private String uri;
	Document doc;
	
	final  HashMap<Integer, String> gradelevel_agerange = new HashMap<Integer,String>();

	public ComputeEducationalMetadata() {
		try {
			this.init("WebContent/WEB-INF/lib/profiles/");
			this.detector = DetectorFactory.create();
		} catch (LangDetectException e) {
			System.err.println("Could not load language profiles");
		}
		initGradeLevelMap();
		this.doc = null;
		
		
	}

	private void initGradeLevelMap() {
		this.gradelevel_agerange.put(0, "0-6");
		this.gradelevel_agerange.put(1, "6-7");
		this.gradelevel_agerange.put(2, "7-8");
		this.gradelevel_agerange.put(3, "8-9");
		this.gradelevel_agerange.put(4, "9-10");
		this.gradelevel_agerange.put(5, "10-11");
		this.gradelevel_agerange.put(6, "11-12");
		this.gradelevel_agerange.put(7, "12-13");
		this.gradelevel_agerange.put(8, "13-14");
		this.gradelevel_agerange.put(9, "14-15");
		this.gradelevel_agerange.put(10, "15-16");
		this.gradelevel_agerange.put(11, "16-17");
		this.gradelevel_agerange.put(12, "17-18");
		this.gradelevel_agerange.put(13, ">18");
		
	}

	public void init(String profileDirectory) throws LangDetectException {
		DetectorFactory.loadProfile(profileDirectory);
	}

	@GET
	@Path("/genEM")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEducationalMetadata(@Context UriInfo info) {
		System.out.println("Called genEM");
		String uri = info.getQueryParameters().getFirst("uri");
		
		try {
			org.jsoup.Connection.Response response= Jsoup.connect(uri)
			.ignoreContentType(true)
	           .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")  
	           .referrer("http://www.google.com")   
	           .timeout(12000) 
	           .followRedirects(true)
	           .execute();
			doc = response.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(uri);
			e.printStackTrace();
		}
	
		EducationalMetaData em = new EducationalMetaData();

		String plaintext = extractPlainText(uri);
		em.setLanguage(this.identifyLanguage(plaintext));
		em.setAge_range(this.computeReadabiltyScore(plaintext));
		em.setLearning_resource_type(this.computeLearningResourceType());
		
		return Response.status(200).entity(em).build();
		

		//return Response.serverError().build();
	}

	private String identifyLanguage(String txt) {
		try {
			this.detector = DetectorFactory.create();
		}

		catch (LangDetectException e) {
			System.err.println("Could not create new detector instance");
		}
		this.detector.append(txt);
		try {
			return this.detector.detect();
		} catch (LangDetectException e) {
			System.err.println("Could not get language of text");
		}
		return "";
	}
	
	private String computeReadabiltyScore(String resource_text){
		this.r=new Readability(resource_text);
		Double fleschkincaid =  r.getFleschKincaidGradeLevel();
		//calculating age
		int gradelevel = fleschkincaid.intValue();
		/*if (gradelevel < 0)
			return this.gradelevel_agerange.get(0);
		else if(gradelevel>13)
			return this.gradelevel_agerange.get(13);
		else
			return this.gradelevel_agerange.get(gradelevel);*/
		return fleschkincaid.toString();
	}
	
	/**
	 * 
	 * @return ArrayList with possible values for learning resource type
	 */
	private ArrayList<String> computeLearningResourceType(){
		
		ArrayList<String> rtype= new ArrayList<String>();
		//possible values from LOM: exercise, simulation, questionnaire, 
		//diagram, figure, graph, index, slide, table, narrative text, 
		//exam, experiment, problem statement, self assessment, lecture 
		//
		//we try to work with: quiz, FAQ, code fragment, images, slides, narrative text, table
		
		//
		//check for table
		Elements table = doc.select("table");
		if(!table.isEmpty()){
			//resource contains a table
			rtype.add(EducationalMetaData.RESOURCETYPE_TABLE);
		}
		
		//check for FAQ 
		//1) check if there is the word "FAQ" or "frequently asked questions" in a headline
		if(elementContainsString("h1", "FAQ") || 
				elementContainsString("h2", "FAQ") || 
				elementContainsString("h3", "FAQ") || 
				elementContainsString("h4", "FAQ") || 
				elementContainsString("h5", "FAQ") || 
				elementContainsString("title", "FAQ") ||
				elementContainsString("h1", "frequently asked questions") || 
				elementContainsString("h2", "frequently asked questions") || 
				elementContainsString("h3", "frequently asked questions") || 
				elementContainsString("h4", "frequently asked questions") || 
				elementContainsString("h5", "frequently asked questions") || 
				elementContainsString("title", "frequently asked questions")  
				)
		
			//TODO what abt pages like stackoverflow?
		rtype.add(EducationalMetaData.RESOURCETYPE_FAQ);
		
		// check for code fragments -- gonna be trickier
		//let's see if page contains <pre> or <code>
		//TODO: this was quick and dirty, what abt text that is not properly formatted
		if((!doc.select("pre").isEmpty()) || (!doc.select("code").isEmpty()))
			rtype.add(EducationalMetaData.RESOURCETYPE_CODE);
		
		//return educational metadata result object
		return rtype;
	}

	private String extractPlainText(String uri) {

		//try to get  a clean version of the content
		//boilerpipe removes things like navigation
		String plaintext="";
		try {
			plaintext = ArticleExtractor.INSTANCE.getText(uri);
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!plaintext.equals(""))
			return plaintext;
		else{
			//if the boilerpipe extraction doesnt work out for whatever reason
			//get the document and extract all textnodes (might include text from navigation etc.)
			
			
			 return doc.text();
		}
		
	}
	
	private boolean elementContainsString(String element, String content){
		Elements el = doc.select(element);
		if(!el.isEmpty() && el.text().contains(content)){
			return true;
		}
		return false;
	}
}
