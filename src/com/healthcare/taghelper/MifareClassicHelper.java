package com.healthcare.taghelper;

import java.io.ByteArrayOutputStream;

import android.nfc.tech.MifareClassic;

/*
 * This is a MifareClassic helper which allows R access using keyA and RW access using keyB
 * Access bits = 0x78, 0x77, 0x88, 0x00
 */
public class MifareClassicHelper {
	
	private static final int MAD_OFFSET = 1;
	private static final int KEY_BLOCK = 1;
	private static final int FIRST_DATA_SECTOR = 1;
	private static final byte[] accessBits = new byte[] {(byte)0x78, (byte)0x77, (byte)0x88, (byte)0x00};
	
	public static void close(MifareClassic tag) throws Exception {
		tag.close();
	}
	
	public static byte[] readSector(MifareClassic tag, int sectorIndex, byte[] key) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		tag.connect();
		int blocks = tag.getBlockCountInSector(sectorIndex) - KEY_BLOCK;
		int index = tag.sectorToBlock(sectorIndex);
		
		if(tag.authenticateSectorWithKeyA(sectorIndex, key) || tag.authenticateSectorWithKeyB(sectorIndex, key)) {
			// Authentication successful
			while(blocks-- > 0) {
				// Read a block
				stream.write(tag.readBlock(index++));
			}
		}
		else {
			throw new Exception("Authentication unsuccessful for sector #"+sectorIndex+" using the provided key");
		}
		tag.close();
		
		return stream.toByteArray();
	}
	
	public static byte[] readSection(MifareClassic tag, int sectionIndex, byte[] key) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		int sectors = (tag.getSectorCount()-MAD_OFFSET)/TagHelper.TOTAL_SECTIONS;
		int index = MAD_OFFSET + (sectionIndex*sectors);
		
		while(sectors-- > 0) {
			stream.write(readSector(tag, index++, key));
		}
		
		return stream.toByteArray();
	}
	
	public static byte[] readTag(MifareClassic tag, byte[] key) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		int sectors = tag.getSectorCount() - MAD_OFFSET;
		int index = MAD_OFFSET;
		
		while(sectors-- > 0) {
			stream.write(readSector(tag, index++, key));
		}
		
		return stream.toByteArray();
	}
	
	public static void writeSector(MifareClassic tag, int sectorIndex, byte[] key, byte[] data) throws Exception {
		tag.connect();
		int blocks = tag.getBlockCountInSector(sectorIndex) - KEY_BLOCK;
		int index = tag.sectorToBlock(sectorIndex);
		
		// Clip the data to store on the tag
		int maxBytes = MifareClassic.BLOCK_SIZE * blocks;
		byte[] clippedData = new byte[maxBytes];
		if(data.length > maxBytes)
			System.arraycopy(data, 0, clippedData, 0, maxBytes);
		else
			System.arraycopy(data, 0, clippedData, 0, data.length);
		
		if(tag.authenticateSectorWithKeyB(sectorIndex, key)) {
			// Authentication successful
			int count = 0;
			while(blocks-- > 0) {
				// Prepare a chunk and write it
				byte[] chunk = new byte[MifareClassic.BLOCK_SIZE];
				System.arraycopy(clippedData, count++*MifareClassic.BLOCK_SIZE, chunk, 0, chunk.length);
				tag.writeBlock(index++, chunk);
			}
		}
		else {
			throw new Exception("Authentication unsuccessful for sector #"+sectorIndex+" using the provided key");
		}
		tag.close();
	}
	
	public static void writeSection(MifareClassic tag, int sectionIndex, byte[] key, byte[] data) throws Exception {
		int blocksInASector = tag.getBlockCountInSector(FIRST_DATA_SECTOR) - KEY_BLOCK;
		int sectors = (tag.getSectorCount()-MAD_OFFSET)/TagHelper.TOTAL_SECTIONS;
		int index = MAD_OFFSET + (sectionIndex*sectors);
		
		// Clip the data to store on the tag
		int maxBytes = MifareClassic.BLOCK_SIZE * blocksInASector * sectors;
		byte[] clippedData = new byte[maxBytes];
		if(data.length > maxBytes)
			System.arraycopy(data, 0, clippedData, 0, maxBytes);
		else
			System.arraycopy(data, 0, clippedData, 0, data.length);
		
		int count = 0;
		while(sectors-- > 0) {
			// Prepare a chunk and write it
			byte[] chunk = new byte[MifareClassic.BLOCK_SIZE * blocksInASector];
			System.arraycopy(clippedData, count++*(MifareClassic.BLOCK_SIZE * blocksInASector), chunk, 0, chunk.length);
			writeSector(tag, index++, key, chunk);
		}
	}
	
	public static void writeTag(MifareClassic tag, byte[] key, byte[] data) throws Exception {
		int blocksInASector = tag.getBlockCountInSector(FIRST_DATA_SECTOR) - KEY_BLOCK;
		int sectors = tag.getSectorCount() - MAD_OFFSET;
		int index = MAD_OFFSET;
		
		// Clip the data to store on the tag
		int maxBytes = MifareClassic.BLOCK_SIZE * blocksInASector * sectors;
		byte[] clippedData = new byte[maxBytes];
		if(data.length > maxBytes)
			System.arraycopy(data, 0, clippedData, 0, maxBytes);
		else
			System.arraycopy(data, 0, clippedData, 0, data.length);
		
		int count = 0;
		while(sectors-- > 0) {
			// Prepare a chunk and write it
			byte[] chunk = new byte[MifareClassic.BLOCK_SIZE * blocksInASector];
			System.arraycopy(clippedData, count++*(MifareClassic.BLOCK_SIZE * blocksInASector), chunk, 0, chunk.length);
			writeSector(tag, index++, key, chunk);
		}
	}
	
	public static void changeKeysOfSector(MifareClassic tag, int sectorIndex, byte[] oldKeyB, byte[] newKeyA, byte[] newKeyB) throws Exception {
		if(sectorIndex < FIRST_DATA_SECTOR) {
			throw new Exception("Cannot change keys for non-data or MAD sectors");
		}
		
		tag.connect();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(newKeyA);
		outputStream.write(accessBits);
		outputStream.write(newKeyB);
		byte[] chunk = outputStream.toByteArray();
		
		int index = tag.sectorToBlock(sectorIndex) + tag.getBlockCountInSector(sectorIndex) - KEY_BLOCK;
				
		System.out.println("OldKey: "+Tools.byteArrayToHexString(oldKeyB));
		System.out.println("Chunk: "+Tools.byteArrayToHexString(chunk));
		
		if(tag.authenticateSectorWithKeyB(sectorIndex, oldKeyB)) {
			tag.writeBlock(index, chunk);
		}
		else {
			throw new Exception("Authentication unsuccessful for sector #"+sectorIndex+" using the provided key");
		}
		tag.close();
	}
	
	public static void changeKeysOfSection(MifareClassic tag, int sectionIndex, byte[] oldKeyB, byte[] newKeyA, byte[] newKeyB) throws Exception {
		int sectors = (tag.getSectorCount()-MAD_OFFSET)/TagHelper.TOTAL_SECTIONS;
		int index = MAD_OFFSET + (sectionIndex*sectors);
		
		while(sectors-- > 0) {
			changeKeysOfSector(tag, index++, oldKeyB, newKeyA, newKeyB);
		}
	}
	
	public static void changeKeysOfTag(MifareClassic tag, byte[] oldKeyB, byte[] newKeyA, byte[] newKeyB) throws Exception {
		int sectors = tag.getSectorCount() - MAD_OFFSET;
		int index = MAD_OFFSET;
		
		while(sectors-- > 0) {
			changeKeysOfSector(tag, index++, oldKeyB, newKeyA, newKeyB);
		}
	}
}
