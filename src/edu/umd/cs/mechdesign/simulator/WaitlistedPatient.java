package edu.umd.cs.mechdesign.simulator;

import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;

public class WaitlistedPatient extends Vertex{
	protected double CPRA;
	protected double Age;
	protected BloodType bloodTypePatient;
	double entryTime;
	boolean IsAnAltruist;
	
	
	


	public WaitlistedPatient(int ID, double CPRA,double Age,BloodType bloodTypePatient, boolean IsAnAltruist){
		super(ID);
		this.CPRA = CPRA;
		this.Age = Age;
		this.bloodTypePatient = bloodTypePatient;
		this.IsAnAltruist = IsAnAltruist;
	}
	
	public double getEntryTime() {
		return entryTime;
	}


	public void setEntryTime(double entrytime) {
		entryTime = entrytime;
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



	public boolean isIsAnAltruist() {
		return IsAnAltruist;
	}

	public void setIsAnAltruist(boolean isAnAltruist) {
		IsAnAltruist = isAnAltruist;
	}

	@Override
	public boolean isAltruist() {
		// TODO Auto-generated method stub
		return false;
	}





	
}
