package com.healthcare.beans;

import java.io.Serializable;

public class HealthRecordBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private String patientName;
	private String patientDOB;
	private SectionBean section1;
	private SectionBean section2;
	
	public HealthRecordBean(String patientName, String patientDOB) {
		super();
		this.patientName = patientName;
		this.patientDOB = patientDOB;
		this.section1 = new SectionBean();
		this.section2 = new SectionBean();
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getPatientDOB() {
		return patientDOB;
	}

	public void setPatientDOB(String patientDOB) {
		this.patientDOB = patientDOB;
	}
	
	public SectionBean getSection1() {
		return section1;
	}
	
	public SectionBean getSection2() {
		return section2;
	}
	
	public void addDataInSection1(SectionDataBean data) {
		section1.addData(data);
	}
	
	public void addDataInSection2(SectionDataBean data) {
		section2.addData(data);
	}
	
	@Override
	public String toString() {
		return new StringBuffer("\npatientName: ")
		.append(patientName+"\n")
		.append("patientDOB: ")
		.append(patientDOB+"\n\n")
		.append("Section 1:\n")
		.append(section1+"\n")
		.append("Section 2:\n")
		.append(section2+"\n")
		.toString();
	}
}
