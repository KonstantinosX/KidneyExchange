package edu.umd.cs.mechdesign.simulator;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import edu.cmu.cs.dickerson.kpd.dynamic.arrivals.ExponentialArrivalDistribution;
import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;


public class DiseasedSimulationDriver {
	private static final Logger logger = Logger
			.getLogger(SimulationDriver.class.getSimpleName());

	private static ExponentialArrivalDistribution arrivalTimeGen;
	private static ExponentialArrivalDistribution lifespanTimeGen;
	private static ExponentialArrivalDistribution organArrivalTimeGen;

	public static void main(String[] args) {
		
		// parameters for exponential
		int m = 6;
		double lambda1 = 1.0;
		double lambda2 = 1.0;
		
		Random r = new Random();
		
		// interarrival times for each type of event
		arrivalTimeGen = new ExponentialArrivalDistribution(m, r);
		lifespanTimeGen = new ExponentialArrivalDistribution(lambda1, r);
		organArrivalTimeGen = new ExponentialArrivalDistribution(lambda2, r);
		
		// probability that a patient will reject an organ and max number of patients asked
		// TODO different rejection based on characteristics of organ/patient
		double organ_rejection_prob = 0.3; 
		int max_patients_asked = 4;

		// create patient and organ generators
		PatientsForDeceasedDonorGenerator DKGen = new PatientsForDeceasedDonorGenerator(r);
		OrganGenerator OGen = new OrganGenerator(r);
		
		// create initial Waiting List
		int numPatients = 100;
		HashMap<BloodType, List<WaitlistedPatient>> DKWaitingList = DKGen.generateHashMap(numPatients);
		
		
		// **************** SIMULATION ****************
		
		double currTime = 0.0;
		Event currEvent = null;
		double lastExitTime = -1;
		double timeLimit = 30;
		
		
		DeceasedOrgan newOrgan = OGen.generateOrgan();
		
		//assume that a new organ arrives
		
		
		
		
		
		/*
		System.out.printf("The new organ has ID %d, DPI %f\n ", newOrgan.getID(), newOrgan.getDPI());
		System.out.println("And Blood Type: "+newOrgan.getBloodTypeDonor());
		
		for (int i=1; i<=DKWaitingList.get(BloodType.AB).size(); i++){
			System.out.printf("\n type AB ID: %d \n",DKWaitingList.get(BloodType.AB).get(i-1).ID);
		}
		for (int i=1; i<=DKWaitingList.get(BloodType.A).size(); i++){
			System.out.printf("type A ID: %d \n",DKWaitingList.get(BloodType.A).get(i-1).ID);
		}
		for (int i=1; i<=DKWaitingList.get(BloodType.B).size(); i++){
			System.out.printf("type B ID: %d \n",DKWaitingList.get(BloodType.B).get(i-1).ID);
		}
		for (int i=1; i<=DKWaitingList.get(BloodType.O).size(); i++){
			System.out.printf("type O ID: %d \n",DKWaitingList.get(BloodType.O).get(i-1).ID);
		}
		*/
		
		
		
		System.out.print("Done! :)");
		return;
	}
	
	
}
