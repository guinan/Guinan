package de.ovgu.wdok.guinan.connector.twitter;

import java.util.ArrayList;



public class Tweet{

	private String _tweet_id;
	private boolean _favorited;
	private int _favourites_count;
	private TweetEntities _tweet_entities;
	private boolean _retweeted;
	private int _retweet_count;
	private String _text;
	private ArrayList<String> _urls;
	
	
	
	
	public Tweet() {
		super();
		this._tweet_id = null;
		this._favorited = false;
		this._favourites_count = 0;
		this._tweet_entities = null;
		this._retweeted = false;
		this._retweet_count = 0;
		this._text =null;
	}
	
	

	
	public Tweet(String _tweet_id, boolean _favorited, int _favourites_count,
			TweetEntities _tweet_entities, boolean _retweeted,
			int _retweet_count, String _text) {
		super();
		this._tweet_id = _tweet_id;
		this._favorited = _favorited;
		this._favourites_count = _favourites_count;
		this._tweet_entities = _tweet_entities;
		this._retweeted = _retweeted;
		this._retweet_count = _retweet_count;
		this._text = _text;
	}
	public String get_tweet_id() {
		return _tweet_id;
	}
	public void set_tweet_id(String _tweet_id) {
		this._tweet_id = _tweet_id;
	}
	public boolean is_favorited() {
		return _favorited;
	}
	public void set_favorited(boolean _favorited) {
		this._favorited = _favorited;
	}
	public int get_favourites_count() {
		return _favourites_count;
	}
	public void set_favourites_count(int _favourites_count) {
		this._favourites_count = _favourites_count;
	}
	public TweetEntities get_tweet_entities() {
		return _tweet_entities;
	}
	public void set_tweet_entities(TweetEntities _tweet_entities) {
		this._tweet_entities = _tweet_entities;
	}
	public boolean is_retweeted() {
		return _retweeted;
	}
	public void set_retweeted(boolean _retweeted) {
		this._retweeted = _retweeted;
	}
	public int get_retweet_count() {
		return _retweet_count;
	}
	public void set_retweet_count(int _retweet_count) {
		this._retweet_count = _retweet_count;
	}
	public String get_text() {
		return _text;
	}
	public void set_text(String _text) {
		this._text = _text;
	}

	//for testing purposes only
	@Override
	public String toString() {
		return "Tweet [_tweet_id=" + _tweet_id + ", _favorited=" + _favorited
				+ ", _favourites_count=" + _favourites_count
				+ ", _tweet_entities=" + _tweet_entities + ", _retweeted="
				+ _retweeted + ", _retweet_count=" + _retweet_count
				+ ", _text=" + _text + "]";
	}
	
	
}
