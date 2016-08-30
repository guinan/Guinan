package de.ovgu.wdok.guinan.educ;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
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
import org.jsoup.nodes.Element;
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
	private String res_uri;
	Document doc;
	private String plaintext;
	private String orig_uri;
	private HashMap<String, Double> resourcetypemap;

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
		this.orig_uri = "";
		this.resourcetypemap = new HashMap<String, Double>();
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
		// System.out.println("Called genEM");
		this.res_uri = info.getQueryParameters().getFirst("uri");
		boolean proxy=false;
		// dissecting the uri
		try {
			URL res_uri = new URL(this.res_uri);
			//System.out.println("resource uri: " + res_uri);
			if (res_uri.getQuery() != null) {
				this.orig_uri = res_uri.getQuery();
				//System.out.println("URI: " + this.orig_uri);
				// does uri have uri embedded?
				if (this.orig_uri.indexOf("=") != -1) {
					proxy=true;
					this.orig_uri = this.orig_uri.substring(this.orig_uri
							.indexOf("=") + 1);
				}
			}
			// System.out.println("Original uri: " + this.orig_uri);

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		this.doc = getDocumentFromUri(this.res_uri);
		if(this.doc==null){
			//if proxy failed try to fetch doc from original uri
			if (proxy){
				//System.out.println("Trying alternative URI");
				this.doc = getDocumentFromUri(this.orig_uri);
			}
		}
		if(this.doc==null){
			System.err.println("Could not load document");
			return Response.status(404).build();
		}
		
		EducationalMetaData em = new EducationalMetaData();

		/*
		 * if(this.orig_uri.endsWith(".pdf")) this.plaintext =
		 * extractPlainTextFromPdf(uri); else
		 */
		em.setUri(this.orig_uri);
		this.plaintext = extractPlainText(this.res_uri);
		// we can only compute these values if we have some text
		if (!plaintext.equals("")) {
			em.setLanguage(this.identifyLanguage(plaintext));
			if (em.getLanguage().equalsIgnoreCase("en"))
				em.setAge_range(this.computeReadabiltyScore(plaintext, 0));
			else
				em.setAge_range(this.computeReadabiltyScore(plaintext, 1));
			em.setDescription(this.getDescriptionOfResource());
		}
		em.setLearning_resource_type(this.computeLearningResourceType());
		em.setTitle(this.getTitleOfResource());

		return Response.status(200).entity(em).build();

		// return Response.serverError().build();
	}
	
	public Document getDocumentFromUri(String uri){
		try {
			org.jsoup.Connection.Response response = Jsoup
					.connect(uri)
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.referrer("http://www.google.com").timeout(12000)
					.followRedirects(true).execute();
			return response.parse();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("[ERR] Could not load document from "+uri);
			e.printStackTrace();
		}
		return null;
	}

	private String getTitleOfResource() {
		return doc.select("title").text();
	}

	private String getDescriptionOfResource() {
		String desc = "";
		SimpleSummariser sum = new SimpleSummariser();
		// is there a meta description field?
		Elements metadesc = doc.select("meta[name=description]");
		if (!metadesc.isEmpty()) {
			for (Element metadesccontent : metadesc) {
				return metadesccontent.attr("content");
			}

		}

		// try to summarize with tools
		desc = sum.summarise(this.plaintext, 5);
		//System.out.println(desc);
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
	private String computeReadabiltyScore(String resource_text, int mode) {

		this.r = new Readability(resource_text);
		int fleschkincaid = r.getFleschKincaidGradeLevel().intValue();
		int fck_readingEase = r.getFleschReadingEase().intValue();
		int colemanLiau = r.getColemanLiau().intValue();
		int gunningfog = (new Double(r.getGunningFog())).intValue();
		int smog = r.getSMOG().intValue();
		int smog_index = r.getSMOGIndex().intValue();
		int ari = r.getARI().intValue();
		int gradelevel = 0;

		// if language is english
		if (mode == 0) {
			// calculating age

			gradelevel = (fleschkincaid + fck_readingEase + colemanLiau
					+ gunningfog + smog + smog_index + ari) / 7;
		}
		// for other (european) languages
		else
			gradelevel = (colemanLiau + ari) / 2;

		if (gradelevel < 0)
			return this.gradelevel_agerange.get(0);
		else if (gradelevel > 13)
			return this.gradelevel_agerange.get(13);
		else
			return this.gradelevel_agerange.get(gradelevel);

		// return gradelevel+"";
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
			// TODO is this working as desired?
			String table_content = table.text();
			double ratio = table_content.length() / this.plaintext.length();
			this.resourcetypemap.put(EducationalMetaData.RESOURCETYPE_TABLE,
					ratio);
			// rtype.add(EducationalMetaData.RESOURCETYPE_TABLE);
		}

		// check for FAQ
		// 1) check if there is the word "FAQ" or "frequently asked questions"
		// in a headline
		for (String headerstr : this.header_strings) {
			boolean done = false;
			if (elementContainsString(headerstr, faq_pattern)) {
				this.resourcetypemap.put(EducationalMetaData.RESOURCETYPE_FAQ,
						1.0);
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
		if ((!doc.select("pre").isEmpty()) || (!doc.select("code").isEmpty())) {
			// compute amount of code in page

			rtype.add(EducationalMetaData.RESOURCETYPE_CODE);
		}
		// check for images
		Elements images = doc.select("img");
		if (!images.isEmpty()) {
			// check size of found images
			int width = 0;
			int height = 0;
			for (Element img : images) {
				// try actual dimensions of image file
				BufferedImage bimg;
				try {

					URI url = new URI(img.attr("src"));
					URI tmp;
					if (!url.isAbsolute()) {
						if (this.orig_uri != "") {
							tmp = new URI(this.orig_uri);
						} else
							tmp = new URI(this.res_uri);

						url = new URI(tmp.getScheme() + "://"
								+ tmp.getAuthority() + img.attr("src"));

					}
					//System.out.println("Image: " + url);
					bimg = ImageIO.read(url.toURL());
					if (bimg != null) {
						width = bimg.getWidth();
						height = bimg.getHeight();
					}

				} catch (MalformedURLException e) {
					System.err.print("Could not fetch image from URL: ");
					// e.printStackTrace();
				} catch (IOException e) {
					System.err.print("Could not read image: ");
					// e.printStackTrace();

				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//TODO width and height might be non integer --> "100%"
				// try width and height attributes of the image element itself
				if (!img.select("[width]").isEmpty())
					width = Integer.parseInt(img.attr("width"));
				if (!img.select("[height]").isEmpty())
					height = Integer.parseInt(img.attr("height"));
				//System.out.println("width, height: " + width + ", " + height);
				// try css attributes
				// [attr*=valContaining]
				if (!img.select("[style*=width]").isEmpty()) {
					// get attr value text and try to read values
					String cssvals = img.select("[style]").text();
					String[] vals = cssvals.split(";");
					for (String singleval : vals) {
						if (singleval.startsWith("width")) {
							if (singleval.endsWith("%")) {
								width = width
										/ 100
										* Integer.parseInt(singleval
												.substring(singleval
														.indexOf(":")));
							}
							width = Integer.parseInt(singleval
									.substring(singleval.indexOf(":")));
						}
						if (singleval.startsWith("height")) {
							if (singleval.endsWith("%")) {
								height = height
										/ 100
										* Integer.parseInt(singleval
												.substring(singleval
														.indexOf(":")));
							}
							height = Integer.parseInt(singleval
									.substring(singleval.indexOf(":")));
							System.out.println("width, height: " + width + ", "
									+ height);
						}
					}
				}

			}
			//System.out.println("width, height: " + width + ", " + height);
			if (width > 300 || height > 300) {
				rtype.add(EducationalMetaData.RESOURCETYPE_IMAGE);
			}
		}

		// check for slides

		// very often slide pages have elements with a class name that contains
		// the string "slide"
		// let's try this naive idea first

		// class name starts with slide

		for (String slidestr : this.elemsForSlides) {
			//System.out.println("Trying " + slidestr + "[class~=.*slide.*]");
			if (!doc.select(slidestr + "[class~=.*slide.*]").isEmpty()) {
				rtype.add(EducationalMetaData.RESOURCETYPE_SLIDES);
			}
		}

		// check for narrative text
		if (this.wordCount(this.plaintext) >= this.wc_threshold) {
			rtype.add(EducationalMetaData.RESOURCETYPE_NARRATIVE_TEXT);
		}

		// check for video
		if (!(doc.select("[class~=(player)s?.*]")).isEmpty()) {
			rtype.add(EducationalMetaData.RESOURCETYPE_VIDEO);
		}
		// check for audio
		// try to find links to mp3 or wav files
		if (!(doc.select("[href~=(?i)\\.(mp3|wav|ogg|mp4)]")).isEmpty())
			rtype.add(EducationalMetaData.RESOURCETYPE_AUDIO);

		// return educational metadata result object
		Map<String, Double> tmp = sortByValue(resourcetypemap);
		for (Entry<String, Double> e : tmp.entrySet()) {
			rtype.add(e.getKey());
		}
		return rtype;
	}

	/**
	 * extracting plain text from a web page trying to ignore elements such as
	 * navigation, menus, footer and others
	 * 
	 * @param uri
	 * @return
	 */
	public String extractPlainText(String uri) {

		// try to get a clean version of the content
		// boilerpipe removes things like navigation
		String plaintext = "";
		try {
			plaintext = DefaultExtractor.INSTANCE.getText(uri);
			//plaintext.trim();
			System.out.println(plaintext);
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
				// System.out.println("Looking for "+e);
				// System.out.println("doc: "+this.doc.html());
				Elements tmp = this.doc.select(e);
				for (org.jsoup.nodes.Element el : tmp) {
					plaintext += el.text() + " ";
				}
			}
			plaintext.trim();
		}
		if (!plaintext.equals(""))
			return plaintext.trim();
		else {
			// if the boilerpipe extraction doesnt work out for whatever reason
			// get the document and extract all textnodes (might include text
			// from navigation etc.)
			System.out.println("Fallback to extracting text nodes");

			return doc.text().trim();
		}

	}

	/*private String extractPlainTextFromPdf(String uri) {
		System.out.println("Trying to extract text from pdf");

		String plaintext = "";
		File pdffile;
		try {
			pdffile = new File(new URI(uri));
			PDDocument document = PDDocument.load(pdffile);
			document.getClass();

			// PDFTextStripperByArea stripper = new PDFTextStripperByArea();
			// stripper.setSortByPosition(true);
			PDFTextStripper stripper = new PDFTextStripper();
			// stripper.setStartPage(1);
			// stripper.setEndPage(2);
			stripper.setSortByPosition(true);
			plaintext += stripper.getText(document);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return plaintext;
	}*/

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
		//System.out.println(trim.split("\\W+").length);
		return trim.split("\\W+").length;
	}

	// taken from
	// http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java

	// sorting a hashmap by value
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}
}
