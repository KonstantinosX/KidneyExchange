package edu.umd.cs.mechdesign.simulator;

import java.util.Random;

import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;

public class OrganGenerator {

	protected Random random;
	
	// probability of the patient being of each blood type (obtained from Saidman Generator - assumed same as living)
	protected double Pr_DONOR_TYPE_O = 0.4814;
	protected double Pr_DONOR_TYPE_A = 0.3373;
	protected double Pr_DONOR_TYPE_B = 0.1428;
	
	private int currentID;
	
	
	public OrganGenerator(Random random) {
		this.random = random;
		this.currentID = 0;
	}
	
	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}
	
	/**
	 * Draws a random donor's blood type from the US distribution 
	 * @return BloodType.{O,A,B,AB}
	 */
	private BloodType drawDonorBloodType() {
		double r = random.nextDouble();

		if (r <= Pr_DONOR_TYPE_O) { return BloodType.O; }
		if (r <= Pr_DONOR_TYPE_O + Pr_DONOR_TYPE_A) { return BloodType.A; }
		if (r <= Pr_DONOR_TYPE_O + Pr_DONOR_TYPE_A + Pr_DONOR_TYPE_B) { return BloodType.B; }
		return BloodType.AB;
	}
	
	/**
	 * Draws a random quality of the organ from the deceased donor
	 * @return DPI
	 */
	// assume uniform distribution
	private double drawDPI() {
		double DPI = random.nextDouble();
		return DPI;
		
	}
	
	
	/*
	 * Generates an organ
	 */
	public DeceasedOrgan generateOrgan(){
		int ID = ++currentID;
		BloodType bloodTypeDonor = drawDonorBloodType();
		double DPI = drawDPI();
		return new DeceasedOrgan  (ID,DPI,bloodTypeDonor);
	}
	
	
	
}
