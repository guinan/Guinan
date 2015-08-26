package de.ovgu.wdok.guinan;

import java.util.Date;

public class SFPGenJob {
	
	private String status;
	
	private Date timestamp;

	public SFPGenJob(String status, Date timestamp) {
		super();
		this.status = status;
		this.timestamp = timestamp;
	}

	public String getStatus() {
		return status;
	}

	public void updateStatus(String status) {
		this.status = status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void updateTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	

}
