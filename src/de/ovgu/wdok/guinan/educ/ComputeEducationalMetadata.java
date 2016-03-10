package de.ovgu.wdok.guinan.educ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.sf.classifier4J.summariser.SimpleSummariser;

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
	private String plaintext;

	/* constants */
	// elements that are considered to contain crucial textual content
	final private String[] elemsForPlainText = { "p", "article", "h1", "h2",
			"h3", "h4", "blockquote", "section" };

	// strings for FAQ
	final private String faq_pattern = "f\\.?(requently)?\\s?a\\.?(sked)?\\s?q\\.?(uestions)?\\s?";

	// elements to be searched for FAQ strings
	final private String[] header_strings = { "h1", "h2", "h3", "h4", "h5",
			"h6", "title" };

	final private String[] elemsForSlides = { "div", "section" };

	// word count threshold for narrative text
	final private int wc_threshold = 500;

	final HashMap<Integer, String> gradelevel_agerange = new HashMap<Integer, String>();

	public ComputeEducationalMetadata() {
		try {
			this.init("WebContent/WEB-INF/lib/profiles/");
			this.detector = DetectorFactory.create();
		} catch (LangDetectException e) {
			System.err.println("Could not load language profiles");
		}
		initGradeLevelMap();
		this.doc = null;

		this.plaintext = "";

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
			org.jsoup.Connection.Response response = Jsoup
					.connect(uri)
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.referrer("http://www.google.com").timeout(12000)
					.followRedirects(true).execute();
			doc = response.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(uri);
			e.printStackTrace();
		}

		EducationalMetaData em = new EducationalMetaData();

		this.plaintext = extractPlainText(uri);
		em.setLanguage(this.identifyLanguage(plaintext));
		em.setAge_range(this.computeReadabiltyScore(plaintext));
		em.setLearning_resource_type(this.computeLearningResourceType());
		em.setTitle(this.getTitleOfResource());
		em.setDescription(this.getDescriptionOfResource());
		return Response.status(200).entity(em).build();

		// return Response.serverError().build();
	}

	private String getTitleOfResource() {
		return doc.select("title").text();
	}

	private String getDescriptionOfResource() {
		String desc = "";
		SimpleSummariser sum = new SimpleSummariser();
		// is there a meta description field?
		// if(!doc.select(meta))

		// try to summarize with tools
		desc = sum.summarise(this.plaintext, 5);
		System.out.println(desc);
		return desc;
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

	/**
	 * TODO compute average of all readability scores?! Computing the
	 * readability score for a resource
	 * 
	 * @param resource_text
	 * @return
	 */
	private String computeReadabiltyScore(String resource_text) {
		this.r = new Readability(resource_text);
		Double fleschkincaid = r.getFleschKincaidGradeLevel();
		// calculating age
		int gradelevel = fleschkincaid.intValue();
		/*
		 * if (gradelevel < 0) return this.gradelevel_agerange.get(0); else
		 * if(gradelevel>13) return this.gradelevel_agerange.get(13); else
		 * return this.gradelevel_agerange.get(gradelevel);
		 */
		return fleschkincaid.toString();
	}

	/**
	 * 
	 * @return ArrayList with possible values for learning resource type
	 */
	private ArrayList<String> computeLearningResourceType() {

		ArrayList<String> rtype = new ArrayList<String>();
		// possible values from LOM: exercise, simulation, questionnaire,
		// diagram, figure, graph, index, slide, table, narrative text,
		// exam, experiment, problem statement, self assessment, lecture
		//
		// we try to work with: quiz, FAQ, code fragment, images, slides,
		// narrative text, table, video, audio

		// TODO compute order of importance
		// check for table
		Elements table = doc.select("table");
		if (!table.isEmpty()) {
			// resource contains a table
			rtype.add(EducationalMetaData.RESOURCETYPE_TABLE);
		}

		// check for FAQ
		// 1) check if there is the word "FAQ" or "frequently asked questions"
		// in a headline
		for (String headerstr : this.header_strings) {
			boolean done = false;
			if (elementContainsString(headerstr, faq_pattern)) {
				rtype.add(EducationalMetaData.RESOURCETYPE_FAQ);
				done = true;
			}
			if (done)
				break;
		}

		// TODO what abt pages like stackoverflow?

		// check for code fragments -- gonna be trickier
		// let's see if page contains <pre> or <code>
		// TODO: this was quick and dirty, what abt text that is not properly
		// formatted
		if ((!doc.select("pre").isEmpty()) || (!doc.select("code").isEmpty()))
			rtype.add(EducationalMetaData.RESOURCETYPE_CODE);

		// check for images
		if (!doc.select("img").isEmpty())
			rtype.add(EducationalMetaData.RESOURCETYPE_IMAGE);

		// check for slides

		// very often slide pages have elements with a class name that contains
		// the string "slide"
		// let's try this naive idea first
				
		//class name starts with slide
	    
	    if (!doc.select("[class~=(slide)s?.*]").isEmpty()){
	    	rtype.add(EducationalMetaData.RESOURCETYPE_SLIDES);
	    }
		// check for narrative text
		if (this.wordCount(this.plaintext) >= this.wc_threshold) {
			rtype.add(EducationalMetaData.RESOURCETYPE_NARRATIVE_TEXT);
		}

		//check for video
		  if (!(doc.select("[class~=(player)s?.*]")).isEmpty()){
		    	rtype.add(EducationalMetaData.RESOURCETYPE_VIDEO);
		    }
		//check for audio
		//try to find links to mp3 or wav files
		  if(!(doc.select("[href~=(?i)\\.(mp3|wav|ogg|mp4)]")).isEmpty())
			  rtype.add(EducationalMetaData.RESOURCETYPE_AUDIO);
		
		// return educational metadata result object
		return rtype;
	}

	/**
	 * extracting plain text from a web page trying to ignore elements such as
	 * navigation, menus, footer and others
	 * 
	 * @param uri
	 * @return
	 */
	private String extractPlainText(String uri) {

		// try to get a clean version of the content
		// boilerpipe removes things like navigation
		String plaintext = "";
		try {
			plaintext = ArticleExtractor.INSTANCE.getText(uri);
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!plaintext.equals(""))
			return plaintext;
		else {
			// extract such content that is in certain html elements
			System.out.println("Fallback to extracting certain HTML elements");
			// p article h
			for (String e : this.elemsForPlainText) {
				Elements tmp = doc.select(e);
				for (org.jsoup.nodes.Element el : tmp) {
					plaintext += el.text() + " ";
				}
			}
		}
		if (!plaintext.equals(""))
			return plaintext;
		else {
			// if the boilerpipe extraction doesnt work out for whatever reason
			// get the document and extract all textnodes (might include text
			// from navigation etc.)
			System.out.println("Fallback to extracting text nodes");

			return doc.text();
		}

	}

	/**
	 * helper method for analyzing whether a certain html element has textual
	 * content that matches the regex
	 * 
	 * @param element
	 *            HTML element(s) we want to analyze
	 * @param content
	 *            regex describing the textual content we are looking for
	 * @return true if element with text was found, false otherwise
	 */
	private boolean elementContainsString(String element, String content) {
		Pattern p = Pattern.compile(content, Pattern.CASE_INSENSITIVE
				| Pattern.UNICODE_CASE);
		Elements el = doc.select(element);
		if (!el.isEmpty()) {
			for (org.jsoup.nodes.Element e : el) {
				if (p.matcher(e.text()).matches()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * helper method couting words in a text
	 * 
	 * @param text
	 *            text with words to be counted
	 * @return number of words in the text
	 */
	private int wordCount(String text) {
		String trim = text.trim();
		if (trim.isEmpty())
			return 0;
		System.out.println(trim.split("\\W+").length);
		return trim.split("\\W+").length;
	}
}
