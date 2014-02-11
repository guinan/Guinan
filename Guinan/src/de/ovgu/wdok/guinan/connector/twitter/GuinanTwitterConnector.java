package de.ovgu.wdok.guinan.connector.twitter;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.connector.GuinanConnector;


//TODO:	extract hashtags
// convert tweet to worblehatresult
// 

@Path("/twitterservice/")
public class GuinanTwitterConnector extends GuinanConnector {

	private ClientConfig config;
	private Client client;
	private WebResource twittersearchloc;
	private WebResource twitterapiloc;
	private WebResource masterloc;
	private final static String SERVICE_NAME = "GuinanTwitterService";

	public GuinanTwitterConnector() {
		super(SERVICE_NAME, getBaseURIForTwitterService());
		this.config = new DefaultClientConfig();
		//since we want to serialize the result to JSON, we need to configure the resource appropriately
		this.config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		this.client = Client.create(config);
		this.twittersearchloc = client.resource(getBaseURIForTwitterSearch());
		this.twitterapiloc = client.resource(getBaseURIforTwitterAPI());
		this.masterloc = client.resource(getBaseURIForMaster());
	}

	// TODO finish this
	@GET
	@Path("query/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	
	public ArrayList<GuinanResult> query(@QueryParam("q") String query) {
		// result list
		ArrayList<GuinanResult> result = new ArrayList<GuinanResult>();
		// first fetch tweets for query
		ArrayList<Object> tweets_for_search = getTweetsForSearch(query);
		for (int i = 0; i < tweets_for_search.size(); i++) {
			HashMap<String, ?> tweet = (HashMap<String, String>) tweets_for_search
					.get(i);
			// get more details for the selected tweet
			Tweet t = getDetailsForTweet((String)tweet.get("id_str"));
			if(tweet.containsKey("entities")){
				TweetEntities te = new TweetEntities((HashMap<String,?>)tweet.get("entities"));
				t.set_tweet_entities(te);
			}
			
			result.add(mapResults(t));

		}

		return result;
		
	}
	
	private GuinanResult mapResults(Tweet t){
		
		GuinanResult gr = new GuinanResult();
		
		//set URL
		ArrayList<String> urls = t.get_tweet_entities().get_urls();
		
		if(urls.size()>0){
			
				gr.setLocation(urls.get(0));
			
		}
		else
			gr.setLocation(null);
		gr.setContent(t.get_text());
		gr.setContent_tags(t.get_tweet_entities().get_hashtags());
		gr.setDocumenttype("Tweet");
		gr.setRating(t.get_favourites_count());
		
		return gr;
	}

	private Tweet getDetailsForTweet(String id_str){
		
		Tweet tweet = new Tweet();
		
		
		String json_res = twitterapiloc.path(id_str+".json")
				.accept(MediaType.APPLICATION_JSON).get(String.class);
		HashMap<String, Object> json_tree = null;
		try {
				json_tree = new ObjectMapper().readValue(json_res, HashMap.class);
		}
		
		catch (JsonParseException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (JsonMappingException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(json_tree != null){
			
			for(Entry<String, ?> e: json_tree.entrySet()){
		        if(e.getKey().equals("text"))
		        	tweet.set_text((String)e.getValue());
		        else
		        	if(e.getKey().equals("retweeted"))
		        		tweet.set_retweeted((Boolean) e.getValue());
		        else
		        	if(e.getKey().equals("id_str"))
		        		tweet.set_tweet_id((String) e.getValue());
		        else
		        	if(e.getKey().equals("retweet_count"))
		        		tweet.set_retweet_count((Integer) e.getValue());
			}

		}
		

		return tweet;
	}

	public ArrayList<Object> getTweetsForSearch(String query) {

		// call search endpoint of twitter's REST API
		ArrayList<Object> tweets_for_search = new ArrayList<Object>();
		String json_res = twittersearchloc.path("search.json")
				.queryParam("q", query).queryParam("include_entities", "true")
				.queryParam("result_type", "mixed")
				.accept(MediaType.APPLICATION_JSON).get(String.class);

		HashMap<String, Object> json_tree;
		try {
			json_tree = new ObjectMapper().readValue(json_res, HashMap.class);
			// stuffing the result tweets into a hashmap
			tweets_for_search = (ArrayList<Object>) json_tree.get("results");
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tweets_for_search;
	}

	private static URI getBaseURIForTwitterSearch() {
		return UriBuilder.fromUri("http://search.twitter.com").build();
	}

	private static URI getBaseURIForTwitterService() {
		return UriBuilder.fromUri(
				"http://localhost:8080/Guinan/twitterservice/query").build();
	}

	/*private static URI getBaseURIForMaster() {
		return UriBuilder.fromUri(
				"http://localhost:8080/Worblehat/WorblehatMaster").build();
	}*/

	private static URI getBaseURIforTwitterAPI() {
		return UriBuilder.fromUri("http://api.twitter.com/1/statuses/show")
				.build();
	}

	@GET
	@Path("register")
	/* TODO make it nice */
	/**
	 * register TwitterService to WorblehatMaster ... basically calling the register URI,
	 * telling the master "hey look, it's me, i'm here"
	 */
	public String register() {

		
		String response;
		try {
			response = masterloc.path("registerService").path(SERVICE_NAME)
					.accept(MediaType.TEXT_PLAIN)
					.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
					.post(String.class, this.getLocation().toString());
			return "Service has been registered";
		}

		catch (UniformInterfaceException uie) {
			return "Service has already been registered. No changes";
		} catch (RuntimeException e) {

			System.out.println("Whoops! ");
			e.printStackTrace();
		}
		return "";
		// return response.getClientResponseStatus();
	}

	@GET
	@Path("ping/")
	public String ping() {
		return "pong!";
	}

	@Override
	public String getName() {
		return "GuinanTwitterService";
	}

	@Override
	public void setLocation(URI location) {
		this.location = location;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public URI getLocation() {
		return getBaseURIForTwitterService();
	}

}
