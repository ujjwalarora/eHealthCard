package com.healthcare.beans;

import java.io.Serializable;

public class EncryptedMessageBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 10L;
	private String senderID;
	private byte[] encryptedHealthRecord;
	private byte[] encryptedKey;
	
	public EncryptedMessageBean(String senderID, byte[] encryptedHealthRecord, byte[] encryptedKey) {
		super();
		this.senderID = senderID;
		this.encryptedHealthRecord = encryptedHealthRecord;
		this.encryptedKey = encryptedKey;
	}
	
	public String getSenderID() {
		return senderID;
	}
	public void setSenderID(String senderID) {
		this.senderID = senderID;
	}
	public byte[] getEncryptedHealthRecord() {
		return encryptedHealthRecord;
	}
	public void setEncryptedHealthRecord(byte[] encryptedHealthRecord) {
		this.encryptedHealthRecord = encryptedHealthRecord;
	}
	public byte[] getEncryptedKey() {
		return encryptedKey;
	}
	public void setEncryptedKey(byte[] encryptedKey) {
		this.encryptedKey = encryptedKey;
	}
	
	@Override
	public String toString() {
		return new StringBuffer("senderID: ")
		.append(senderID+"\n")
		.append("encryptedHealthRecord: ")
		.append(encryptedHealthRecord+"\n")
		.append("encryptedKey: ")
		.append(encryptedKey).toString();
	}
}
