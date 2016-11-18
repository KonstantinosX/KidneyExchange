package edu.umd.cs.mechdesign.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;

public class PatientsForDeceasedDonorGenerator{


	protected Random random;
	
	// probability of the patient being of each blood type (obtained from Saidman Generator)
	protected double Pr_PATIENT_TYPE_O = 0.4814;
	protected double Pr_PATIENT_TYPE_A = 0.3373;
	protected double Pr_PATIENT_TYPE_B = 0.1428;
	
	// probability of kidney-disease incidence base on age
	// source: https://ndt.oxfordjournals.org/content/11/8/1542.full.pdf
	protected double Pr_PATIENT_19_39 = 0.4471;
	protected double Pr_PATIENT_40_59 = 0.3010;
	protected double Pr_PATIENT_60_74 = 0.1699;
	protected double Pr_PATIENT_75 = 0.0820;
	
	private int currentID;
	
	
	
	public PatientsForDeceasedDonorGenerator(Random random) {
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
	 * Draws a random patient's age 
	 * @return Age
	 */
	private double drawPatientAge() {
		double r = random.nextDouble();
		double age;
		if (r <= Pr_PATIENT_19_39) {
			double r1 = random.nextDouble();
			age = 19 + 21*r1;
			return age; }
		if (r <= Pr_PATIENT_19_39 + Pr_PATIENT_40_59) {
			double r1 = random.nextDouble();
			age =  40 + 20*r1;
			return age; }
		if (r <= Pr_PATIENT_19_39 + Pr_PATIENT_40_59 + Pr_PATIENT_60_74) {
			double r1 = random.nextDouble();
			age =  60 + 15*r1;
			return age; }
		double r1 = random.nextDouble();
		age =  60 + 15*r1;
		return age;
	}

	/**
	 * Draws a random patient's sensitization level 
	 * @return CPRA
	 */
	// assume uniform distribution
	private double drawCPRA() {
		double CPRA = random.nextDouble();
		return CPRA;
		
	}
	
	/**
	 * Draws a random patient's blood type from the US distribution 
	 * @return BloodType.{O,A,B,AB}
	 */
	private BloodType drawPatientBloodType() {
		double r = random.nextDouble();

		if (r <= Pr_PATIENT_TYPE_O) { return BloodType.O; }
		if (r <= Pr_PATIENT_TYPE_O + Pr_PATIENT_TYPE_A) { return BloodType.A; }
		if (r <= Pr_PATIENT_TYPE_O + Pr_PATIENT_TYPE_A + Pr_PATIENT_TYPE_B) { return BloodType.B; }
		return BloodType.AB;
	}
	
	
	
	/*
	 * Generates a patient
	 */
	public WaitlistedPatient generatePatient(int ID){
		BloodType bloodTypePatient = drawPatientBloodType();
		double Age = drawPatientAge();
		double CPRA = drawCPRA();
		return new WaitlistedPatient  (ID,CPRA,Age,bloodTypePatient);
	}
	
	
	/*
	 * Generates a hashmap with 4 lists containing all Patients Waitlisted for a deceased donor transplant by blood type
	 */
	
	
	HashMap<BloodType, List<WaitlistedPatient>> generateHashMap(int NumPatients){
		HashMap<BloodType, List<WaitlistedPatient>> hmap = new HashMap<BloodType, List<WaitlistedPatient>>();
		List<WaitlistedPatient> DDWaitingList0 = new ArrayList<WaitlistedPatient>();
		List<WaitlistedPatient> DDWaitingListA = new ArrayList<WaitlistedPatient>();
		List<WaitlistedPatient> DDWaitingListB = new ArrayList<WaitlistedPatient>();
		List<WaitlistedPatient> DDWaitingListAB = new ArrayList<WaitlistedPatient>();
		
		// add key and lists to hashmap
		 hmap.put(BloodType.O, DDWaitingList0);
		 hmap.put(BloodType.A, DDWaitingListA);
		 hmap.put(BloodType.B, DDWaitingListB);
		 hmap.put(BloodType.AB, DDWaitingListAB);
		 
		 // insert patients to corresponding lists
		 int i=0;
	     while(i < NumPatients){
	    	 WaitlistedPatient newPatient = generatePatient(++currentID);
	    	 hmap.get(newPatient.getBloodTypePatient()).add(newPatient);
	    	 i++;
	     }
	     return hmap;
	}

	
	
	/*
	 * Adds new patients in Waiting list
	 */
	public HashMap<BloodType, List<WaitlistedPatient>> addPatient(HashMap<BloodType, List<WaitlistedPatient>> myHashMap,int NumPatients){
		for(int i=1; i<=NumPatients; i++){
			WaitlistedPatient newPatient = generatePatient(++currentID);
			myHashMap.get(newPatient.getBloodTypePatient()).add(newPatient);
		}
		return myHashMap;
	}
	
	
	
	/*
	 * Removes patient with particular ID from the list
	 */
	
	public HashMap<BloodType, List<WaitlistedPatient>> removePatient(HashMap<BloodType, List<WaitlistedPatient>> myHashMap, int ID){
		WaitlistedPatient foundPatient = null;
		
		for (WaitlistedPatient w : myHashMap.get(BloodType.O)){
			if(w.ID == ID){
				foundPatient = w;
				break;
			}
		}
		if (foundPatient != null){
			myHashMap.get(BloodType.O).remove(foundPatient);
			return myHashMap;
		}
		else{
			for (WaitlistedPatient w : myHashMap.get(BloodType.A)){
				if(w.ID == ID){
					foundPatient = w;
					break;
				}
			}
			if (foundPatient != null){
				myHashMap.get(BloodType.A).remove(foundPatient);
				return myHashMap;
			}
			else{
				for (WaitlistedPatient w : myHashMap.get(BloodType.B)){
					if(w.ID == ID){
						foundPatient = w;
						break;
					}
				}
				if (foundPatient != null){
					myHashMap.get(BloodType.B).remove(foundPatient);
					return myHashMap;
				}
				else{
					for (WaitlistedPatient w : myHashMap.get(BloodType.AB)){
						if(w.ID == ID){
							foundPatient = w;
							break;
						}
					}
					if (foundPatient != null){
						myHashMap.get(BloodType.AB).remove(foundPatient);
						return myHashMap;
					}
					else{
						System.out.printf("Patient with ID %d not found in list, so could not be removed", ID);
						return myHashMap;
					}
					
				}
			}
		}

	}
	
}
