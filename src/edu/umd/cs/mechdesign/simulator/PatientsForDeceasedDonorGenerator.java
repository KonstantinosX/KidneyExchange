package edu.umd.cs.mechdesign.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;

public class PatientsForDeceasedDonorGenerator {

	protected Random random;

	// probability of the patient added to the waiting list being of each blood
	// type
	// 2015 source:
	// https://optn.transplant.hrsa.gov/data/view-data-reports/national-data/#
	protected double Pr_PATIENT_TYPE_O = 0.484817352;
	protected double Pr_PATIENT_TYPE_A = 0.327597032;
	protected double Pr_PATIENT_TYPE_B = 0.148373288;
	protected double Pr_PATIENT_TYPE_AB = 0.039212329;

	// probability of an addition to waiting list being in each age group
	// 2015 source:
	// https://optn.transplant.hrsa.gov/data/view-data-reports/national-data/#
	protected double Pr_PATIENT_0_1 = 0.000656093;
	protected double Pr_PATIENT_1_5 = 0.006075993;
	protected double Pr_PATIENT_6_10 = 0.00487791;
	protected double Pr_PATIENT_11_17 = 0.017828617;
	protected double Pr_PATIENT_18_34 = 0.116157006;
	protected double Pr_PATIENT_35_49 = 0.265175719;
	protected double Pr_PATIENT_50_64 = 0.411912369;
	protected double Pr_PATIENT_65 = 0.177316294;

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
	 * 
	 * we assume that in each age range, each age in uniformally distributed
	 * 
	 * @return Age
	 */
	protected double drawPatientAge() {
		double r = random.nextDouble();
		double age;
		if (r <= Pr_PATIENT_0_1) {
			double r1 = random.nextDouble();
			age = 1 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5) {
			double r1 = random.nextDouble();
			age = 1 + 4 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10) {
			double r1 = random.nextDouble();
			age = 5 + 5 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10
				+ Pr_PATIENT_11_17) {
			double r1 = random.nextDouble();
			age = 10 + 7 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10
				+ Pr_PATIENT_11_17 + Pr_PATIENT_18_34) {
			double r1 = random.nextDouble();
			age = 17 + 17 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10
				+ Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49) {
			double r1 = random.nextDouble();
			age = 34 + 15 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10
				+ Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49
				+ Pr_PATIENT_50_64) {
			double r1 = random.nextDouble();
			age = 49 + 15 * r1;
			return age;
		} else {
			double r1 = random.nextDouble();
			age = 65 + 15 * r1;
		}

		return age;
	}

	/**
	 * Draws a random patient's sensitization level
	 * 
	 * @return CPRA
	 */
	// assume uniform distribution
	private double drawCPRA() {
		double CPRA = random.nextDouble();
		return CPRA;

	}

	/**
	 * Draws a random patient's blood type from the US distribution
	 * 
	 * @return BloodType.{O,A,B,AB}
	 */
	private BloodType drawPatientBloodType() {
		double r = random.nextDouble();

		if (r <= Pr_PATIENT_TYPE_O) {
			return BloodType.O;
		}
		if (r <= Pr_PATIENT_TYPE_O + Pr_PATIENT_TYPE_A) {
			return BloodType.A;
		}
		if (r <= Pr_PATIENT_TYPE_O + Pr_PATIENT_TYPE_A + Pr_PATIENT_TYPE_B) {
			return BloodType.B;
		}
		return BloodType.AB;
	}

	/*
	 * Generates a patient (always assume that the patient is not an altruist)
	 */
	public WaitlistedPatient generatePatient(int ID) {
		BloodType bloodTypePatient = drawPatientBloodType();
		double Age = drawPatientAge();
		double CPRA = drawCPRA();
		boolean IsAnAltruist = false;
		return new WaitlistedPatient  (ID,CPRA,Age,bloodTypePatient, IsAnAltruist);

	}

	/*
	 * Generates a hashmap with 4 lists containing all Patients Waitlisted for a
	 * deceased donor transplant by blood type
	 */

	HashMap<BloodType, List<WaitlistedPatient>> generateHashMap(int NumPatients) {
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
		int i = 0;
		while (i < NumPatients) {
			WaitlistedPatient newPatient = generatePatient(++currentID);
			hmap.get(newPatient.getBloodTypePatient()).add(newPatient);
			i++;
		}
		return hmap;
	}

	/*
	 * Adds new patient in Waiting list
	 */
	public WaitlistedPatient addPatient(
			HashMap<BloodType, List<WaitlistedPatient>> myHashMap) {

		WaitlistedPatient newPatient = generatePatient(++currentID);
		myHashMap.get(newPatient.getBloodTypePatient()).add(newPatient);

		return newPatient;
	}

	/*
	 * Removes patient with particular ID from the list
	 */

	public HashMap<BloodType, List<WaitlistedPatient>> removePatient(
			HashMap<BloodType, List<WaitlistedPatient>> myHashMap, int ID) {
		WaitlistedPatient foundPatient = null;

		for (WaitlistedPatient w : myHashMap.get(BloodType.O)) {
			if (w.getID() == ID) {
				foundPatient = w;
				break;
			}
		}
		if (foundPatient != null) {
			myHashMap.get(BloodType.O).remove(foundPatient);
			return myHashMap;
		} else {
			for (WaitlistedPatient w : myHashMap.get(BloodType.A)) {
				if (w.getID() == ID) {
					foundPatient = w;
					break;
				}
			}
			if (foundPatient != null) {
				myHashMap.get(BloodType.A).remove(foundPatient);
				return myHashMap;
			} else {
				for (WaitlistedPatient w : myHashMap.get(BloodType.B)) {
					if (w.getID() == ID) {
						foundPatient = w;
						break;
					}
				}
				if (foundPatient != null) {
					myHashMap.get(BloodType.B).remove(foundPatient);
					return myHashMap;
				} else {
					for (WaitlistedPatient w : myHashMap.get(BloodType.AB)) {
						if (w.getID() == ID) {
							foundPatient = w;
							break;
						}
					}
					if (foundPatient != null) {
						myHashMap.get(BloodType.AB).remove(foundPatient);
						return myHashMap;
					} else {
						System.out
								.printf("Patient with ID %d not found in list, so could not be removed",
										ID);
						return myHashMap;
					}

				}
			}
		}

	}

	/*
	 * Checks if patient with particular ID is in the list
	 */

	public boolean IsInWaitingList(
			HashMap<BloodType, List<WaitlistedPatient>> myHashMap, int ID) {

		for (WaitlistedPatient w : myHashMap.get(BloodType.O)) {
			if (w.getID() == ID) {
				return true;
			}
		}
		for (WaitlistedPatient w : myHashMap.get(BloodType.A)) {
			if (w.getID() == ID) {
				return true;
			}
		}
		for (WaitlistedPatient w : myHashMap.get(BloodType.B)) {
			if (w.getID() == ID) {
				return true;
			}
		}
		for (WaitlistedPatient w : myHashMap.get(BloodType.AB)) {
			if (w.getID() == ID) {
				return true;
			}
		}
		return false;

	}
}
