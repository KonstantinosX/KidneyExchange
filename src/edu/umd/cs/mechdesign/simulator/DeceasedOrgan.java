package edu.umd.cs.mechdesign.simulator;


import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;

public class DeceasedOrgan {
	protected Integer ID;
	protected double DPI;
	protected BloodType bloodTypeDonor;
	
	public DeceasedOrgan(int ID, double DPI,BloodType bloodTypeDonor){
		this.ID = ID;
		this.DPI = DPI;
		this.bloodTypeDonor = bloodTypeDonor;
	}

	public Integer getID() {
		return ID;
	}
	public void setID(Integer iD) {
		ID = iD;
	}
	public double getDPI() {
		return DPI;
	}
	public void setDPI(double dPI) {
		DPI = dPI;
	}
	public BloodType getBloodTypeDonor() {
		return bloodTypeDonor;
	}
	public void setBloodTypeDonor(BloodType bloodTypeDonor) {
		this.bloodTypeDonor = bloodTypeDonor;
	}

}
