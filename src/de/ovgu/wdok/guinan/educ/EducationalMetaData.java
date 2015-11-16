package de.ovgu.wdok.guinan.educ;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("EM")
public class EducationalMetaData {

	private Detector detector;
	String language;

	public EducationalMetaData() {
		try {
			this.init("WebContent/WEB-INF/lib/profiles/");
			this.detector = DetectorFactory.create();
		} catch (LangDetectException e) {
			System.err.println("Could not load language profiles");
		}
		this.language = "";
	}

	public void init(String profileDirectory) throws LangDetectException {
		DetectorFactory.loadProfile(profileDirectory);
	}

	@GET
	@Path("/genEM")
	public Response getEducationalMetadata(@Context UriInfo info) {
		System.out.println("Called genEM");
		String uri = info.getQueryParameters().getFirst("uri");

		Document doc = null;
		try {
			doc = Jsoup.connect(uri).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(uri);
			e.printStackTrace();
		}

		String plaintext = extractPlainText(doc);
		language = this.getLanguage(plaintext);

		if (language != "") {
			return Response.status(200).entity(language).build();
		}

		return Response.serverError().build();
	}

	private String getLanguage(String txt) {
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

	private String extractPlainText(Document doc) {

		return doc.text();
	}
}
