package edu.umd.cs.mechdesign.simulator;


import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;

public class DeceasedOrgan extends Vertex {
	protected double DPI;
	protected BloodType bloodTypeDonor;
	
	public DeceasedOrgan(int ID, double DPI,BloodType bloodTypeDonor){
		super(ID);
		this.DPI = DPI;
		this.bloodTypeDonor = bloodTypeDonor;
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


	@Override
	public boolean isAltruist() {
		// TODO Auto-generated method stub
		return false;
	}

}
