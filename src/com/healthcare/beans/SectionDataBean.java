package com.healthcare.beans;

import java.io.Serializable;

public class SectionDataBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4L;
	private String doctor;
	private String problem;
	private String test;
	private String medicine;
	private int sectionNum;
	
	public SectionDataBean(String doctor, String problem, String test, String medicine) {
		super();
		this.doctor = doctor;
		this.problem = problem;
		this.test = test;
		this.medicine = medicine;
	}
	
	public SectionDataBean(String doctor, String problem, String test, String medicine, int sectionNum) {
		super();
		this.doctor = doctor;
		this.problem = problem;
		this.test = test;
		this.medicine = medicine;
		this.sectionNum = sectionNum;
	}
	
	public String getDoctor() {
		return doctor;
	}
	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}
	public String getProblem() {
		return problem;
	}
	public void setProblem(String problem) {
		this.problem = problem;
	}
	public String getTest() {
		return test;
	}
	public void setTest(String test) {
		this.test = test;
	}
	public String getMedicine() {
		return medicine;
	}
	public void setMedicine(String medicine) {
		this.medicine = medicine;
	}
	public int getSectionNum() {
		return sectionNum;
	}
	public void setSectionNum(int sectionNum) {
		this.sectionNum = sectionNum;
	}

	@Override
	public String toString() {
		return new StringBuffer("doctor: ")
		.append(doctor+"\n")
		.append("problem: ")
		.append(problem+"\n")
		.append("test: ")
		.append(test+"\n")
		.append("medicine: ")
		.append(medicine+"\n")
		.toString();
	}
}
