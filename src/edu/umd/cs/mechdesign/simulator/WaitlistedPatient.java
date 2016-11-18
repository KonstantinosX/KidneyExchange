package edu.umd.cs.mechdesign.simulator;

import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;

public class WaitlistedPatient {
	protected Integer ID;
	protected double CPRA;
	protected double Age;
	protected BloodType bloodTypePatient;
	
	
	public WaitlistedPatient(int ID, double CPRA,double Age,BloodType bloodTypePatient){
		this.ID = ID;
		this.CPRA = CPRA;
		this.Age = Age;
		this.bloodTypePatient = bloodTypePatient;
	}
	
	
	public Integer getID() {
		return ID;
	}


	public void setID(Integer iD) {
		ID = iD;
	}


	public double getCPRA() {
		return CPRA;
	}


	public void setCPRA(double cPRA) {
		CPRA = cPRA;
	}


	public double getAge() {
		return Age;
	}


	public void setAge(double age) {
		Age = age;
	}


	public BloodType getBloodTypePatient() {
		return bloodTypePatient;
	}


	public void setBloodTypePatient(BloodType bloodTypePatient) {
		this.bloodTypePatient = bloodTypePatient;
	}


	
}
