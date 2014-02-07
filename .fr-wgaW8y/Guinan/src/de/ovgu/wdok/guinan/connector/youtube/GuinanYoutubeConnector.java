package de.ovgu.wdok.guinan.connector.youtube;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.connector.GuinanConnector;

@Path("/youtubeconnector/")
public class GuinanYoutubeConnector extends GuinanConnector {

	private final static String SERVICE_NAME = "GuinanYoutubeConnector";
	private final static String LOCATION = "http://localhost:8080/Guinan/youtubeconnector";
	private ClientConfig config;
	private Client client;

	/** Global instance of youtube API key. */
	private final String YOUTUBE_API_KEY = "AIzaSyD5PF8zQNcbgLObvm2rKoajxh9HsHwU2tw";

	private final String YOUTUBE_RESOURCE_URI = "http://www.youtube.com/watch?v=";

	/** Global instance of the HTTP transport. */
	private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private final JsonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * Global instance of the max number of videos we want returned (50 = upper
	 * limit per page).
	 */
	private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

	/** Global instance of Youtube object to make all API requests. */
	private YouTube youtube;

	/** Global instance of GuinanYoutubeConnector WebResource */
	private WebResource youtubesearchloc;

	/** Constructor */
	public GuinanYoutubeConnector() {
		/** set service name and base URI for GuinanYoutubeConnector */
		super(SERVICE_NAME, getBaseURIForYoutubeConnector());

		/** set service's client config to default config */
		this.config = new DefaultClientConfig();

		/** create client with config */
		this.client = Client.create(config);

		/** set location of the master to provided URI */
		this.masterloc = client.resource(getBaseURIForMaster());

		/** set services location to provided URI */
		this.youtubesearchloc = client
				.resource(getBaseURIForYoutubeConnector());

		try {
			/**
			 * taken from
			 * https://developers.google.com/youtube/v3/code_samples/java
			 * 
			 * The YouTube object is used to make all API requests. The last
			 * argument is required, but because we don't need anything
			 * initialized when the HttpRequest is initialized, we override the
			 * interface and provide a no-op function.
			 */
			this.youtube = new YouTube.Builder(this.HTTP_TRANSPORT,
					this.JSON_FACTORY, new HttpRequestInitializer() {
						public void initialize(HttpRequest request)
								throws IOException {
						}
					}).setApplicationName("Guinan YouTubeConnector").build();

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	/**
	 * @return URI (endpoint) for GuinanYoutubeConnector
	 * 
	 * */
	private static URI getBaseURIForYoutubeConnector() {
		return UriBuilder.fromUri(LOCATION + "/query").build();
	}

//	@GET
//	@Path("register")
	/* TODO make it nice - can this be moved to the abstract superclass? */
	/**
	 * REST endpoint to call the register method at /register
	 * register YoutubeConnector to GuinanMaster ... basically calling the register URI,
	 * telling the master "hey look, it's me, i'm here"
	 */
/*	public String register() {

		String response;
		try {
			response = masterloc.path("registerService").path(SERVICE_NAME)
					.accept(MediaType.TEXT_PLAIN)
					.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
					.post(String.class, this.getLocation().toString());
			return "GuinanYouTubeConnector has been registered";
		}

		// System.out.println(response.toString());
		catch (UniformInterfaceException uie) {
			return "Service has already been registered. No changes";
		} catch (RuntimeException e) {

			System.out.println("Whoops! ");
			e.printStackTrace();
		}
		return "";
		// return response.getClientResponseStatus();
	}
*/
	@GET
	@Path("query/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ArrayList<GuinanResult> query(@QueryParam("q") String query) {
		// result list
		ArrayList<GuinanResult> resultlist = new ArrayList<GuinanResult>();
		ArrayList<GuinanYoutubeResult> tmplist = new ArrayList<GuinanYoutubeResult>();
		try{
		YouTube.Search.List search = youtube.search().list("id,snippet");
	      /*
	       * It is important to set your developer key from the Google Developer Console for
	       * non-authenticated requests (found under the API Access tab at this link:
	       * code.google.com/apis/). This is good practice and increased your quota.
	       */
	      String apiKey = this.YOUTUBE_API_KEY;
	      search.setKey(apiKey);
	      search.setQ(query);
	      /*
	       * We are only searching for videos (not playlists or channels). If we were searching for
	       * more, we would add them as a string like this: "video,playlist,channel".
	       */
	      search.setType("video");
	      /*
	       * This method reduces the info returned to only the fields we need and makes calls more
	       * efficient.
	       */
	      search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url,snippet/description)");
	      search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
	      SearchListResponse searchResponse = search.execute();

	      List<SearchResult> searchResultList = searchResponse.getItems();

	      if (searchResultList != null) {
	        //System.out.println((searchResultList.iterator());
	    	String string_of_video_ids ="";
	    	for(SearchResult sr: searchResultList){
	    		ResourceId rId = sr.getId();
	    		if (rId.getKind().equals("youtube#video")) {
	    			string_of_video_ids+=rId.getVideoId()+",";
	    			Thumbnail thumbnail = (Thumbnail) sr.getSnippet().getThumbnails().get("default");
	    	        GuinanYoutubeResult gr = new GuinanYoutubeResult();
	    	        /* set resource's location */
	    	        gr.setLocation(this.YOUTUBE_RESOURCE_URI+rId.getVideoId());
	    	        gr.setContent(sr.getSnippet().getDescription());
	    	        gr.setDocumenttype(GuinanResult.RESOURCE_TYPE_VIDEO);
	    	        gr.setTitle(sr.getSnippet().getTitle());
	    	        gr.setVideoId(rId.getVideoId());
	    	        gr.set_thumbnail_uri(sr.getSnippet().getThumbnails().getDefault().getUrl());
	    	        tmplist.add(gr);
	    	        //System.out.println(sr.toPrettyString());
	    		}
	    	}
	    	
	    	// collect statistics for every single video in order to compute rating
			YouTube.Videos.List single_search = youtube.videos().list("statistics");
			single_search.setFields("items(id,statistics)");
			single_search.setId(string_of_video_ids);
			single_search.setKey(apiKey);
			
			 VideoListResponse listResponse = single_search.execute();

		      List<Video> videoList = listResponse.getItems();
		      if (!videoList.isEmpty()) {
		        for(Video v: videoList){
		        	//find GuinanResult with correct video_id
		        	for(GuinanYoutubeResult gr: tmplist){
		        		if(gr.getVideoId().equals(v.getId())){
		        			//normalize and compute rating: viewcount is 100%
		        			double percentage=100;
		        			double rating = (v.getStatistics().getLikeCount().doubleValue()*percentage/v.getStatistics().getViewCount().doubleValue()) - (v.getStatistics().getDislikeCount().doubleValue()*100/v.getStatistics().getViewCount().doubleValue());
		        			gr.setRating(rating);
		        			//System.out.println("Rating: "+v.getStatistics().getViewCount()+" views / "+v.getStatistics().getLikeCount()+ " likes / "+v.getStatistics().getDislikeCount()+ " dislikes / "+rating+" rating");
		        		}
		        		resultlist.add(gr);
		        	}
		        }
		      }
		      
	      }
	    } catch (GoogleJsonResponseException e1) {
	      System.err.println("There was a service error: " + e1.getDetails().getCode() + " : "
	          + e1.getDetails().getMessage());
	      e1.printStackTrace();
	    } catch (IOException e2) {
	      System.err.println("There was an IO error: " + e2.getCause() + " : " + e2.getMessage());

	    }
		return resultlist;
	}
}
