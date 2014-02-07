package de.ovgu.wdok.guinan.connector.twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class TweetEntities {

	private HashMap<String, String> _media;
	private ArrayList<String> _urls;
	private ArrayList<String> _hashtags;

	public TweetEntities() {
		this._media = new HashMap<String,String>();
		this._urls = new ArrayList<String>();
		this._hashtags = new ArrayList<String>();
	}

	

	/* extract data fields from raw json string (wrapped in a hashmap) */
	public TweetEntities(HashMap<String, ?> raw_entities) {

		this._media = new HashMap<String,String>();
		this._urls = new ArrayList<String>();
		this._hashtags = new ArrayList<String>();
		
		for (Entry<String, ?> e : raw_entities.entrySet()) {
			if (e.getKey().equals("media"))
				this.extractMediaEntities((ArrayList<HashMap<String,String>>)e.getValue());
			else if (e.getKey().equals("urls")){
				this.extractURLEntities((ArrayList<HashMap<String,String>>)e.getValue());
			}
			else if (e.getKey().equals("hashtags")){
				this.extractHashTagEntities((ArrayList<HashMap<String,String>>)e.getValue());
			}
		}
	}

	private void extractHashTagEntities(ArrayList<HashMap<String, String>> raw_hashtag_entities) {
		
		if(raw_hashtag_entities.size()>0){
			for(HashMap<String,String> s:  raw_hashtag_entities){
				 //do something
				for(Entry<String,String> e : s.entrySet()){
					if(e.getKey().equals("text") && e.getValue() != ""){
						this.addHashtagToEntity(e.getValue());
					}
						
				}
			}
		}
		
	}



	private void addHashtagToEntity(String value) {
		this._hashtags.add(value);		
	}



	private void extractURLEntities(ArrayList<HashMap<String,String>> raw_url_entities) {
	
		if(raw_url_entities.size()>0){
			for(HashMap<String,String> s:  raw_url_entities){
				 //do something
				for(Entry<String,String> e : s.entrySet()){
					if(e.getKey().equals("url") && e.getValue() != ""){
						this.addURLtoEntity(e.getValue());
					}
						
				}
			}
		}

	}

	private void extractMediaEntities(ArrayList<HashMap<String,String>> raw_media_entities) {

		HashMap<String, String> tweet_media_entities = new HashMap<String, String>();

		for (HashMap<String, String> m : raw_media_entities) {
			// the result map

			for (Entry<String, String> e : m.entrySet()) {
				if (e.getKey().equals("media_url"))
					tweet_media_entities.put("media_url", e.getKey());
			}

		}
		
		this.set_media(tweet_media_entities);
	}

	public HashMap<String, String> get_media() {
		return _media;
	}

	public void set_media(HashMap<String, String> _media) {
		this._media = _media;
	}

	public ArrayList<String> get_urls() {
		return _urls;
	}

	public void set_urls(ArrayList<String> _urls) {
		this._urls = _urls;
	}

	public ArrayList<String> get_hashtags() {
		return _hashtags;
	}

	public void set_hashtags(ArrayList<String> _hashtags) {
		this._hashtags = _hashtags;
	}
	
	private void addURLtoEntity(String url){
		this._urls.add(url);
	}



	@Override
	public String toString() {
		return "TweetEntities [_media=" + _media + ", _urls=" + _urls
				+ ", _hashtags=" + _hashtags + "]";
	}
	
	

}
