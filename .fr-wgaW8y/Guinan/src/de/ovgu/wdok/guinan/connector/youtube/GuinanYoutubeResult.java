package de.ovgu.wdok.guinan.connector.youtube;

import de.ovgu.wdok.guinan.GuinanResult;

public class GuinanYoutubeResult extends GuinanResult {
	
	private String videoId;
	
	public GuinanYoutubeResult(){
		super();
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	
	

}
