package de.ovgu.wdok.guinan.connector.vimeo;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;

import de.ovgu.wdok.guinan.GuinanResult;
import de.ovgu.wdok.guinan.connector.GuinanConnector;

@Path( "vimeoconnector" )
public class GuinanVimeoConnector extends GuinanConnector
{

	/** unique name of connector */
	private final static String SERVICE_NAME = "GuinanVimeoConnector";

	/** location of VimeoConnector (URI) */
	private final static String LOCATION = "http://localhost:8080/Guinan/vimeoconnector";

	// OAuth Parameters
	private final static String CLIENT_ID = "1eb1df1747bb9bc1fc1451e27e40949d1bf9d6c2";
	private final static String CLIENT_SECRET = "d9fe632594bc94330f1324a17094d318764ddecb";
	private final static String ACCESS_TOKEN = "22afaafb1048720b7932be24232cf70d";
	private final static String ACCESS_TOKEN_SECRET = "7e95b6fcb1783bd663bed011f8a29c82d7624d2a";

	private static URI getVimeoVideoURI( String videoID )
	{
		return UriBuilder.fromUri( "http://vimeo.com/" + videoID ).build();
	}

	/**
	 * @return URI (endpoint) for GuinanVimeoConnector
	 * 
	 * */
	private static URI getBaseURIForVimeoConnector()
	{
		return UriBuilder.fromPath( LOCATION + "/query" ).build();
	}

	/**
	 * @return URI representing the query endpoint of Vimeo API
	 */
	private static URI getBaseURIForVimeoSearch()
	{
		return UriBuilder.fromUri( "http://vimeo.com/api/rest/v2" ).queryParam( "format", "json" ).queryParam( "method", "vimeo.videos.getByTag" ).build();
	}

	private static URI getQueryURIForVimeoSearch( String query )
	{
		return UriBuilder.fromUri( getBaseURIForVimeoSearch() ).queryParam( "sort", "relevant" ).queryParam( "full_response", true ).queryParam( "tag", query ).build();
	}

	private static OAuthClientFilter getVimeoOAuthFilter( Client client )
	{
		// baseline OAuth parameters for access to resource
		OAuthParameters params = new OAuthParameters().signatureMethod( "HMAC-SHA1" ).consumerKey( CLIENT_ID );
		params.setToken( ACCESS_TOKEN );

		// OAuth secrets to access resource
		OAuthSecrets secrets = new OAuthSecrets().consumerSecret( CLIENT_SECRET );
		secrets.setTokenSecret( ACCESS_TOKEN_SECRET );

		// if parameters and secrets remain static, filter can be added to each web
		// resource
		OAuthClientFilter filter = new OAuthClientFilter( client.getProviders(), params, secrets );

		return filter;
	}

	/** config for client part **/
	private ClientConfig config;

	/** client part to Vimeo API */
	private Client client;

	public GuinanVimeoConnector()
	{
		// call constructor of super class, setting the name and endpoint
		super( SERVICE_NAME, getBaseURIForVimeoConnector() );
		// set service's client config to default config
		this.config = new DefaultClientConfig();

		// create client with config
		this.client = Client.create( this.config );
		// add filter for automatic OAuth authorization header
		this.client.addFilter( getVimeoOAuthFilter( this.client ) );

		// set location of the master to provided URI
		this.masterloc = this.client.resource( getBaseURIForMaster() );
	}

	private ArrayList<GuinanResult> extractGuinanResults( ArrayList<LinkedHashMap<String, ?>> jsonList )
	{
		// initialize the result list
		ArrayList<GuinanResult> grlist = new ArrayList<GuinanResult>();

		// iterate of the linked hashmaps, each representing a web resource
		for ( LinkedHashMap<String, ?> e: jsonList )
		{
			GuinanResult gr = new GuinanResult();

			if ( e.containsKey( "id" ) ) gr.set_location( getVimeoVideoURI( (String)e.get( "id" ) ).toString() );
			if ( e.containsKey( "title" ) ) gr.setTitle( (String)e.get( "title" ) );
			if ( e.containsKey( "description" ) ) gr.setContent( (String)e.get( "description" ) );

			if ( e.containsKey( "thumbnails" ) )
			{
				// thumbnails = { thumbnail = [ {height=#Number#, width=#Number#, _content=#URI#}, {...}, ... ] }
				LinkedHashMap<String, ?> thumbnails = (LinkedHashMap<String, ?>)e.get( "thumbnails" );
				ArrayList<LinkedHashMap<String, ?>> thumbnail = (ArrayList<LinkedHashMap<String, ?>>)( thumbnails.get( "thumbnail" ) );
				
				if ( thumbnail.size() > 0 )
				{
					String thumbnailURI = (String)thumbnail.get( thumbnail.size() - 1 ).get( "_content" );
					gr.set_thumbnail_uri( thumbnailURI );
				}
			}

			if ( e.containsKey( "tags" ) )
			{
				// tags = { tag = [ {author=#ID#, id=#ID#, normalized=#STRING#, url=#URI#, _content=#STRING#}, {...}, ... ] }
				LinkedHashMap<String, ?> tags = (LinkedHashMap<String, ?>)e.get( "tags" );
				ArrayList<LinkedHashMap<String, ?>> tag = (ArrayList<LinkedHashMap<String, ?>>)tags.get( "tag" );
				ArrayList<String> grTags = new ArrayList<>();

				for ( LinkedHashMap<String, ?> t: tag )
					grTags.add( (String)t.get( "normalized" ) );

				gr.setContent_tags( grTags );
			}

			gr.setDocumenttype( GuinanResult.RESOURCE_TYPE_VIDEO );
			gr.set_language( "EN" ); // TODO
			grlist.add( gr );
		}
		return grlist;
	}

	@GET
	@Path( "query/" )
	@Produces( MediaType.APPLICATION_JSON )
	@Override
	public ArrayList<GuinanResult> query( @QueryParam( "q" ) String query )
	{
		WebResource searchLoc = this.client.resource( getQueryURIForVimeoSearch( query ) );
		String response = searchLoc.get( String.class );

		// build a hashmap where the json_string could be put in

		LinkedHashMap<String, ?> resultMap = null;
		try
		{
			resultMap = new ObjectMapper().readValue( response, LinkedHashMap.class );
		}
		catch( JsonParseException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch( JsonMappingException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch( IOException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if ( resultMap != null && "ok".equals( resultMap.get( "stat" ) ) )
		{
			LinkedHashMap<String, ?> videos = (LinkedHashMap<String, ?>)resultMap.get( "videos" );
			if ( videos != null )
			{
				ArrayList<LinkedHashMap<String, ?>> video = (ArrayList<LinkedHashMap<String, ?>>)videos.get( "video" );
				return this.extractGuinanResults( video );
			}
		}

		return new ArrayList<GuinanResult>();
	}

}
