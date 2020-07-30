package com.centrify.scheduler;


import java.util.Date;

import javax.validation.constraints.NotNull;


import org.springframework.format.annotation.DateTimeFormat;

//Stores the variables for our postcontroller
public class PersonForm {
	

	@NotNull
	private String cubeNum;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date scheduleDate;
	
	public String getCubeNum() {
		return this.cubeNum;
	}

	public void setCubeNum(String cubeNum) {
		this.cubeNum = cubeNum;
	}


	public String toString() {
		return "Person(Name: " + this.cubeNum + ", Scheduled on: " + this.scheduleDate + ")";
	}
	
	public Date getScheduleDate() {
		return scheduleDate;
	}
	public void setScheduleDate(Date date) {
		this.scheduleDate = date;
	}
}
