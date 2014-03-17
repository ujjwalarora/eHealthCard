package com.healthcare.beans;

import java.io.Serializable;

public class MessageBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String senderID;
	private HealthRecordBean healthRecord;
	private String key;
	
	public MessageBean(String senderID, HealthRecordBean healthRecord, String key) {
		super();
		this.senderID = senderID;
		this.healthRecord = healthRecord;
		this.key = key;
	}
	
	public String getSenderID() {
		return senderID;
	}
	public void setSenderID(String senderID) {
		this.senderID = senderID;
	}
	public HealthRecordBean getHealthRecord() {
		return healthRecord;
	}
	public void setHealthRecord(HealthRecordBean healthRecord) {
		this.healthRecord = healthRecord;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	public String toString() {
		return new StringBuffer("senderID: ")
		.append(senderID+"\n")
		.append("healthRecord: ")
		.append(healthRecord+"\n").toString();
	}
}
