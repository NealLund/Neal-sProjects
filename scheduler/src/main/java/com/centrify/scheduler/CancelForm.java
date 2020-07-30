package com.centrify.scheduler;



import javax.validation.constraints.NotNull;



//Stores the variables for our postcontroller
public class CancelForm {
	

	@NotNull
	private String cubeNum;
	
	public String getCubeNum() {
		return this.cubeNum;
	}

	public void setCubeNum(String cubeNum) {
		this.cubeNum = cubeNum;
	}
}
