package com.centrify.scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;


@Entity
public class Cubicle {
	
	@Id private String cubeNum;
	
	private String email;
	
	@ElementCollection (fetch = FetchType.EAGER)
	private List<String> neighbors;
	//private List<Cubicle> neighbors;

	private Calendar bookedDate;
	
	/**
	 * @return the email
	 */
	String getEmail() {
		return email;
	}
	
	public int getNeighborsCount() {
		return this.neighbors.size();
	}
	
	
	//Checks to see if the neighbors list is empty or not
	public String getNeighbor(int index) {
		if(index < this.neighbors.size()) {
			return this.neighbors.get(index);
		}
		return null;
	}

	/**
	 * @param email the email to set
	 */
	@SuppressWarnings("unused")
	public void setEmail(String email) {
		this.email = email;
	}
	
	/*
	 * Constructer
	 */
	public Cubicle() {
		this.cubeNum = null;
		this.email = null;
		//this.neighbors = new ArrayList<Cubicle>();
		this.neighbors = new ArrayList<String>();
	}
	
	public List<String> getNeighbors(){
		return neighbors;
	}
	
	//Converts a date object to a calendar object
	public void setBookedDate(Date bookedDate) {
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(bookedDate);
		this.bookedDate = calDate;
	}
	
	
	//Constructor
	public Cubicle(String num, String email) {
		this.cubeNum = num;
		this.email = email;
		this.neighbors = new ArrayList<String>();
	}
	
	/**
	 * @return the bookedDate
	 */
	public Calendar getBookedDate() {
		return bookedDate;
	}
	
	public String getDate() {
		if(this.bookedDate == null) {
			return null;
		}
		else {
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, YYYY");
		    return(sdf.format(this.bookedDate.getTime()));
		}
	}
	
	//Clears book date in cancel form
	public void clearBookDate() {
		this.bookedDate.clear();
	}

	/**
	 * @param cubeNum the cubeNum to set
	 */
	@SuppressWarnings("unused")
	private void setCubeNum(String cubeNum) {
		this.cubeNum = cubeNum;
	}

	
	public String getCubeNum() {
		return this.cubeNum;
	}
	
	
	//Adds neighboring cube
	public void addNeighbor(String newNeighbor){
		System.out.println("  Adding "+newNeighbor +" to " + this.cubeNum+" as neighbor "+this.getNeighborsCount());
		this.neighbors.add(newNeighbor);		
	}
	
}
