package com.healthcare.taghelper;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

public class TagHelper {

	private Object tagObject;
	private byte[] tagID;
	private String tagTech = null;
	private StringBuilder status = null;
	
	private static final String TAG_NOT_RECOGNIZED = "Tag wasn't recognized";
	
	public static final int TOTAL_SECTIONS = 3;
	private static final String UNSUPPORTED = "unsupported";
	private static final String MIFARE_CLASSIC = "android.nfc.tech.MifareClassic";
	
	public TagHelper(Tag tag) {
		tagID = tag.getId();
		tagTech = UNSUPPORTED;
		status = new StringBuilder();
		for(String tech : tag.getTechList()) {
			if(tech.equals(MIFARE_CLASSIC)) {
				tagTech = MIFARE_CLASSIC;
				tagObject = MifareClassic.get(tag);
			}
		}
	}
	
	public byte[] getTagID() {
		return tagID;
	}

	public String getStatus() {
		String result = status.toString();
		status = null;
		status = new StringBuilder();
		return result;
	}
	
	public void close() {
		if(tagTech.equals(MIFARE_CLASSIC)) {
			try {
				MifareClassicHelper.close((MifareClassic)tagObject);
			} catch (Exception e) {
				e.printStackTrace();
				status.append("\n" + e.getMessage());
			}
		}
		else {
			status.append("\n" + TAG_NOT_RECOGNIZED);
		}
	}

	public void readSector(int sectorIndex, byte[] key) {
		// No wrappers specified
	}
	
	public byte[] readSection(int sectionIndex, byte[] key) {
		status.append("Attempting to read section " + (sectionIndex+1));
		if(tagTech.equals(MIFARE_CLASSIC)) {
			try {
				status.append("\nReading section " + (sectionIndex+1));
				return MifareClassicHelper.readSection((MifareClassic)tagObject, sectionIndex, key);
			} catch (Exception e) {
				e.printStackTrace();
				status.append("\n" + e.getMessage());
			}
		}
		else {
			status.append("\n" + TAG_NOT_RECOGNIZED);
		}
		return null;
	}
	
	public byte[] readTag(byte[] key) {
		status.append("Attempting to read the tag");
		if(tagTech.equals(MIFARE_CLASSIC)) {
			try {
				status.append("\nReading the tag");
				return MifareClassicHelper.readTag((MifareClassic)tagObject, key);
			} catch (Exception e) {
				e.printStackTrace();
				status.append("\n" + e.getMessage());
			}
		}
		else {
			status.append("\n" + TAG_NOT_RECOGNIZED);
		}
		return null;
	}
	
	public void writeSector(int sectorIndex, byte[] key, byte[] data) {
		// No wrappers specified
	}
	
	public void writeSection(int sectionIndex, byte[] key, byte[] data) {
		status.append("Attempting to write to section " + (sectionIndex+1));
		if(tagTech.equals(MIFARE_CLASSIC)) {
			try {
				status.append("\nWriting to section " + (sectionIndex+1));
				MifareClassicHelper.writeSection((MifareClassic)tagObject, sectionIndex, key, data);
			} catch (Exception e) {
				e.printStackTrace();
				status.append("\n" + e.getMessage());
			}
		}
		else {
			status.append("\n" + TAG_NOT_RECOGNIZED);
		}
	}
	
	public void writeTag(byte[] key, byte[] data) {
		status.append("Attempting to write to the tag");
		if(tagTech.equals(MIFARE_CLASSIC)) {
			try {
				status.append("\nWriting to the tag");
				MifareClassicHelper.writeTag((MifareClassic)tagObject, key, data);
			} catch (Exception e) {
				e.printStackTrace();
				status.append("\n" + e.getMessage());
			}
		}
		else {
			status.append("\n" + TAG_NOT_RECOGNIZED);
		}
	}
	
	public void changeKeysOfSector(int sectorIndex, byte[] oldKeyB, byte[] newKeyA, byte[] newKeyB) {
		// No wrappers specified
	}
	
	public void changeKeysOfSection(int sectionIndex, byte[] oldKeyB, byte[] newKeyA, byte[] newKeyB) {
		status.append("Attempting to change keys of section " + (sectionIndex+1));
		if(tagTech.equals(MIFARE_CLASSIC)) {
			try {
				status.append("\nChanging keys of section " + (sectionIndex+1));
				status.append("\nOld RW Key: " + Tools.byteArrayToString(oldKeyB));
				status.append("\nNew R Key: " + Tools.byteArrayToString(newKeyA));
				status.append("\nNew RW Key: " + Tools.byteArrayToString(newKeyB));
				MifareClassicHelper.changeKeysOfSection((MifareClassic)tagObject, sectionIndex, oldKeyB, newKeyA, newKeyB);
			} catch (Exception e) {
				e.printStackTrace();
				status.append("\n" + e.getMessage());
			}
		}
		else {
			status.append("\n" + TAG_NOT_RECOGNIZED);
		}
	}
	
	public void changeKeysOfTag(byte[] oldKeyB, byte[] newKeyA, byte[] newKeyB) {
		status.append("Attempting to change keys of the tag");
		if(tagTech.equals(MIFARE_CLASSIC)) {
			try {
				status.append("\nChanging keys of the tag");
				status.append("\nOld RW Key: " + Tools.byteArrayToString(oldKeyB));
				status.append("\nNew R Key: " + Tools.byteArrayToString(newKeyA));
				status.append("\nNew RW Key: " + Tools.byteArrayToString(newKeyB));
				MifareClassicHelper.changeKeysOfTag((MifareClassic)tagObject, oldKeyB, newKeyA, newKeyB);
			} catch (Exception e) {
				e.printStackTrace();
				status.append("\n" + e.getMessage());
			}
		}
		else {
			status.append("\n" + TAG_NOT_RECOGNIZED);
		}
	}
}
