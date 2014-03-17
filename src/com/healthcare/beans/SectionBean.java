package com.healthcare.beans;

import java.io.Serializable;
import java.util.ArrayList;

public class SectionBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;
	ArrayList<SectionDataBean> data;
	
	public SectionBean() {
		data = new ArrayList<SectionDataBean>();
	}
	
	public void addData(SectionDataBean d) {
		data.add(d);
	}
	
	public SectionDataBean[] getData() {
		return (SectionDataBean[]) data.toArray();
	}
	
	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		for(SectionDataBean sdb : data)
			res.append(sdb + "\n");
		return res.toString();
	}
}
