package de.ovgu.wdok.guinan;

import java.util.Date;

public class SFPGenJob {
	
	public static final int PROCESSING=1;
	public static final int FINISHED=2;
	public static final int GONE=2;
	
	private int status;
	
	private Date timestamp;

	public SFPGenJob(int status, Date timestamp) {
		super();
		this.status = status;
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void updateStatus(int status) {
		this.status = status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void updateTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	

}
