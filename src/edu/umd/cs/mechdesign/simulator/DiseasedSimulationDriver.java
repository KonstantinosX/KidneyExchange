package edu.umd.cs.mechdesign.simulator;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Logger;

import edu.cmu.cs.dickerson.kpd.dynamic.arrivals.ExponentialArrivalDistribution;
import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.helper.VertexTimeComparator;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;


public class DiseasedSimulationDriver {
	private static final Logger logger = Logger
			.getLogger(SimulationDriver.class.getSimpleName());

	private static ExponentialArrivalDistribution arrivalTimeGen;
	private static ExponentialArrivalDistribution lifespanTimeGen_young;
	private static ExponentialArrivalDistribution lifespanTimeGen_old;
	private static ExponentialArrivalDistribution organArrivalTimeGen;

	
	public static void main(String[] args) {

		String csvFileTran = "././transplants_";
		String csvFileAlt = "././altruists_";
		String pathOutput = "././patientInfo_";

		double altruistLimit = 0.2;
		int timeLimit = 70*52+140;
		//+140 because it takes longer for the deceased to initialize

		for (double i = 0.01; i < altruistLimit; i+=0.02) {
			System.out.println("Running for altruist arrival lambda : "+ altruistLimit);
			for (int j = 0; j < 10; j += 1) {
				deceasedDonorSim(timeLimit, csvFileTran + Double.toString(i)
					+"_"+j+ ".csv", csvFileAlt + Double.toString(i) + "_"+j+".csv",
					pathOutput + Double.toString(i) +"_"+j+ ".csv");
			// k++;
			}
		}

	}
	
	public static void deceasedDonorSim(double timeLimit,
			String csvFileTran,
			String csvFileAlt, String pathOutput) {
		
		//String csvFileTran = "././transplants.csv";
		//String csvFileAlt = "././altruists.csv";
		//String pathOutput = "././patientInfo.csv";
		
		int total_organs=0;
		int total_new_patients=0;
		// time unit: week (52 weeks in year)
		
		// parameters for exponential
		//https://optn.transplant.hrsa.gov/data/view-data-reports/national-data/#
		//35,039 additions per year (2015), so 35039/52 per week = 673.8269231
		// but decrease the scale of the parameters by /50 ------> 13.47653846 per week
		double new_patient_additions_per_week = 13;

		
		//https://www.kidney.org/atoz/content/dialysisinfo
		// 11*52 years survival in dialysis (1/11*52)
		// 6*52 for 60+  (1/6*52)
		double lambda_young = 0.001748252;
		double lambda_old = 0.003205128;
		
		
		//https://optn.transplant.hrsa.gov/data/view-data-reports/national-data/#
		// in 2015: 8,250	deceased donors kidneys (8250/52)
		// but decrease the scale of the parameters by /50 ------> 3.173076924 per week
		double new_organ_per_week = 5;
		

		//in weeks
		double lifeExpectancy = 78*52; 
		
		
		Random r = new Random();
		
		// interarrival times for each type of event
		arrivalTimeGen = new ExponentialArrivalDistribution(new_patient_additions_per_week, r);
		lifespanTimeGen_young = new ExponentialArrivalDistribution(lambda_young, r);
		lifespanTimeGen_old = new ExponentialArrivalDistribution(lambda_old, r);
		organArrivalTimeGen = new ExponentialArrivalDistribution(new_organ_per_week, r);
		
		
		// probability that a patient will reject an organ and max number of patients asked
		// TODO different rejection based on characteristics of organ/patient
		double organ_rejection_prob = 0.3; 
		int max_patients_asked = 4;

		// create patient and organ generators
		PatientsForDeceasedDonorGenerator DKGen = new PatientsForDeceasedDonorGenerator(r);
		OrganGenerator OGen = new OrganGenerator(r);
		
		
		// https://optn.transplant.hrsa.gov/data/view-data-reports/national-data/#
		// current waiting list:99,327
		// but decrease the scale of the parameters by /50 ------> 1986
		int numPatients = 1986;
		
		// create initial Waiting List
		HashMap<BloodType, List<WaitlistedPatient>> DKWaitingList = DKGen.generateHashMap(numPatients);
		
		
		// create list that will contain information for all patients departing the simulation
		ArrayList <PatientInformationHolder> allinfo = new ArrayList <PatientInformationHolder>();
		
		// **************** SIMULATION ****************
		double currTime = 0.0;
		DeceasedEvent currDeceasedEvent = null;
		double lastExitTime = -1;
		//run time in weeks
		
		// read from main
		// double timeLimit = 5200; //100 years
		double deceasedSimulationZeroTime;
		
		
		/**
		 * A priority queue that maintains pairs (exitTime, vertex), prioritized
		 * by exitTime
		 */
		Queue<Pair<Double, Vertex>> patientsByExitTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());
		
		/**
		 * A priority queue that maintains pairs (entryTime, vertex),
		 * prioritized by entryTime
		 */
		Queue<Pair<Double, Vertex>> patientsByEntryTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());

		/**
		 * A priority queue that maintains the entry events for new altruists
		 * that are supposed to come into the system prioritized by entryTime
		 */
		Queue<Double> organEvents = new PriorityQueue<Double>((int) timeLimit);
		
		/*
		 * A priority queue that keeps the times that the object vertex (altruist) will enter the waiting list
		 * 
		 */
		Queue<Pair<Double, Vertex>> altruistsByEntryTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());
		
		/*
		 * A priority queue that keeps the times that the object vertex (altruist) will leave the waiting list
		 * because they received a kidney from a living donor (pseudo-patients)
		 * 
		 */
		Queue<Pair<Double, Vertex>> patientsByLeavingTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());
		
		
		
		List<WaitlistedPatient> AllInitialPatients = new ArrayList<WaitlistedPatient>();
		
		// insert all blood type patients in the list
		for (WaitlistedPatient w : DKWaitingList.get(BloodType.O)){
			AllInitialPatients.add(w);
		}
		for (WaitlistedPatient w : DKWaitingList.get(BloodType.A)){
			AllInitialPatients.add(w);
		}
		for (WaitlistedPatient w : DKWaitingList.get(BloodType.B)){
			AllInitialPatients.add(w);
		}
		for (WaitlistedPatient w : DKWaitingList.get(BloodType.AB)){
			AllInitialPatients.add(w);
		}
		
		// shuffle list contents
		long seed = System.nanoTime();
		Collections.shuffle(AllInitialPatients, new Random(seed));
		
		
		
		// create entry and exit times for patients initially in the queue
		
		for (WaitlistedPatient w : AllInitialPatients){
			double entryTime = currTime + arrivalTimeGen.draw();
			double exitTime;
			if(w.getAge()<=60){
				exitTime = entryTime + lifespanTimeGen_young.draw();
			}
			else{
				exitTime = entryTime + lifespanTimeGen_old.draw();
			}
			currTime = entryTime;
			
			if (currTime > timeLimit) {
				/*
				 * If this vertex would arrive after the time limit, we're
				 * done generating
				 */
				break;
			}
			if (exitTime > lastExitTime) {
				/*
				 * Store the exit time of the final vertex in the pool
				 * (after all other vertices have gone critical)
				 */
				lastExitTime = exitTime;
			}
			// enter entry time in patient object
			w.setEntryTime(entryTime);
			
			patientsByExitTime.add(new Pair<Double, Vertex>(exitTime, w));
			patientsByEntryTime.add(new Pair<Double, Vertex>(entryTime, w));
		}
		deceasedSimulationZeroTime = currTime;
		
		
		
		// schedule arrival of altruistic donors
		
		altruistsByEntryTime = Utils.readAltruists(timeLimit, deceasedSimulationZeroTime, DKGen,csvFileAlt);
		System.out.print("Time that first altruist gets sick "+altruistsByEntryTime.peek().getLeft()+"\n");
		

		
		// schedule departure of patients getting a living donor kidney
		
		patientsByLeavingTime = Utils.readPatientGivenLDK( timeLimit,  deceasedSimulationZeroTime,  DKGen, csvFileTran);
		System.out.print("Time that first patient gets LDK "+patientsByLeavingTime.peek().getLeft()+"\n");
		



		// schedule arrival of organs 
		double organTime = currTime;
		while (true) {

			double entryTime = organTime + organArrivalTimeGen.draw();
			organTime = entryTime;

			if (organTime > timeLimit) {
				/*
				 * If this vertex would arrive after the time limit, we're done
				 * generating
				 */
				break;
			}

			organEvents.add(entryTime);
		}
		System.out.print("Number of organs expected to arrive "+organEvents.size()+"\n");
		total_organs=organEvents.size();
		
		 /* 
		 * set the starting time of the simulation as the last entry time from
		 * the individuals in the initial pool
		 */
		
		double startingTime = currTime;
		System.out.print("Last entry : " + startingTime+"\n");		
		
		
		System.out.print("Scheduling new patient arrivals in the waiting list...\n");
		while (true) {

			/*
			 * Draw arrival and departure times from exponential clocks It is
			 * okay if entryTime == exitTime; the vertex enters and is critical
			 * simultaneously
			 */
			double entryTime = currTime + arrivalTimeGen.draw();
			currTime = entryTime;

			if (currTime > timeLimit) {
				/*
				 * If this vertex would arrive after the time limit, we're done
				 * generating
				 */
				break;
			}
			

			/*
			 * add times to the appropriate queues. Initially the vertex pair to
			 * be added is null, and will be generated at add time later
			 */
			patientsByEntryTime.add(new Pair<Double, Vertex>(entryTime, null));
			// System.out.println("currTime: " + currTime);
		}
		System.out.print("Additional patients expected to arrive "+patientsByEntryTime.size()+"\n");
		total_new_patients=patientsByEntryTime.size();
		
		currTime = startingTime;

		// get starting event before the simulation starts running.
		// the next event can't be a transplant because we haven't had any
		// matchings yet
		currDeceasedEvent = DeceasedEvent.getNextDeceasedEvent(patientsByExitTime, patientsByEntryTime, altruistsByEntryTime, patientsByLeavingTime, organEvents);

		/*
		 * the first event is probably going to be a death event from the
		 * initial pool
		 */
		currTime = currDeceasedEvent.getTime();
		
		System.out.print("First event is: " + currDeceasedEvent);
		
		
		System.out.println("Total number of patient in waiting list " + (DKWaitingList.get(BloodType.O).size()+
				DKWaitingList.get(BloodType.A).size()+DKWaitingList.get(BloodType.B).size()+DKWaitingList.get(BloodType.AB).size()));

		while (currTime < timeLimit) {

			logger.info("AT TIME: " + currTime);
			
			// end simulation
			if (currDeceasedEvent.getType().equals(DeceasedEventType.TERMINATE_SIMULATION)) {
				logger.info("No more events to process...Terminating Simulation...");
				break;
			}
			
			
			// a patient dies
			if (currDeceasedEvent.getType().equals(DeceasedEventType.PATIENT_DIES)) {


				/*
				 * remove patient from the waiting list, remove the event from the
				 * queue
				 */
				Vertex toRemove = patientsByExitTime.poll().getRight();
				
				//if the patient was given already an organ in the past, he is no longer in the waiting list
				// patient is still in the waiting list
				if(DKGen.IsInWaitingList(DKWaitingList,toRemove.getID())){
					System.out.println("Patient ID "+toRemove.getID()+" has died");
					
					// keep information
					PatientInformationHolder newInfo = new PatientInformationHolder(currTime  ,(WaitlistedPatient)toRemove, null, "died");
					allinfo.add(newInfo);
					
					
					DKWaitingList = DKGen.removePatient(DKWaitingList,toRemove.getID());
				
				}
				else{
					System.out.println("The patient we are trying to kill was given a kindey \n");
				}
				
			}
			
			// a patient got a living donor transplant
			
			if (currDeceasedEvent.getType().equals(DeceasedEventType.PATIENT_GETS_LDK)) {
				// a patient with this blood type will randomly be selected to leave the queue
				// in this is an altruistic donor, another patient will be removed
				
				WaitlistedPatient toRemove = (WaitlistedPatient)patientsByLeavingTime.poll().getRight();
				boolean flag = true;
				
				int numTries=0;
				while(flag){
					double IndexToRemoveDouble = r.nextDouble()*DKWaitingList.get(toRemove.getBloodTypePatient()).size() ;
					int IndexToRemove = (int) IndexToRemoveDouble;
					if(!DKWaitingList.get(toRemove.getBloodTypePatient()).isEmpty()){
						WaitlistedPatient w = DKWaitingList.get(toRemove.getBloodTypePatient()).get(IndexToRemove);
						if((!w.isIsAnAltruist())||(numTries>=100)){
							System.out.println("Patient with ID " +DKWaitingList.get(toRemove.getBloodTypePatient()).get(IndexToRemove).getID()+ "got an organ from a living donor!");
							
							// keep information
							PatientInformationHolder newInfo = new PatientInformationHolder(currTime  ,DKWaitingList.get(toRemove.getBloodTypePatient()).get(IndexToRemove), null, "receivedLDK");
							allinfo.add(newInfo);
							
							
							DKWaitingList.get(toRemove.getBloodTypePatient()).remove(IndexToRemove);
							flag = false;
						}
						else{
							numTries++;
						}
					}
					else{
						flag = false;
					}

					
				}
					
			}
			
			
			// the altruist got sick, so he will be added to the waiting list
			if (currDeceasedEvent.getType().equals(DeceasedEventType.ALTRUIST_GETS_SICK)) {
				
				// put patient in waiting list
				WaitlistedPatient newAltruist = (WaitlistedPatient)altruistsByEntryTime.poll().getRight();
				newAltruist.setEntryTime(currTime);

				DKWaitingList = DKGen.addThisPatient(newAltruist,DKWaitingList);
				
				// generate life span based on age
				double exitTime;
				if(newAltruist.getAge()<=60){
					exitTime = currTime + lifespanTimeGen_young.draw();
				}
				else{
					exitTime = currTime + lifespanTimeGen_old.draw();
				}

				// add the exit time of the patient to the queue
	
				patientsByExitTime.add(new Pair<Double, Vertex>(exitTime, newAltruist));
				
				System.out.println("New Altruist has arrived, whith ID "+newAltruist + " ,and with exit time: " + exitTime);
				
			}
			
			
			// a new patient arrives
			if (currDeceasedEvent.getType().equals(DeceasedEventType.PATIENT_ENTERS)) {

				// add 1 patient to waiting list
				WaitlistedPatient newPatient;
				newPatient = DKGen.addPatient(DKWaitingList);
				newPatient.setEntryTime(currTime);
				
				// generate life span based on age
				double exitTime;
				if(newPatient.getAge()<=60){
					exitTime = currTime + lifespanTimeGen_young.draw();
				}
				else{
					exitTime = currTime + lifespanTimeGen_old.draw();
				}
				
				patientsByExitTime.add(new Pair<Double, Vertex>(exitTime, newPatient));

				System.out.println("New Patient has arrived, with ID "+newPatient + " ,and with exit time :" + exitTime);
				
			}
			
			// a new organ arrives
			if (currDeceasedEvent.getType().equals(DeceasedEventType.ORGAN_ARRIVES)) {
				
				// generate organ
				DeceasedOrgan newOrgan = OGen.generateOrgan();
				System.out.print("A new organ has arrived, of blood type " +newOrgan.getBloodTypeDonor()+" !!!");
				
				List<Pair<Double, Vertex>> ListPrioritized = new ArrayList<Pair<Double, Vertex>>();
				double initial =0;
				for(int i=0;i<max_patients_asked;i++){
					Pair<Double, Vertex> InitialPair = new Pair<Double, Vertex>(initial,null);
					ListPrioritized.add(InitialPair);
				}
				
				
				//check who gets the organ
				if(newOrgan.getBloodTypeDonor() == BloodType.O){
					// he can give to all patients in Waiting list
					for (WaitlistedPatient w : DKWaitingList.get(BloodType.O)){
						//calculate KAS and enter (KAS,patient) into queue
						double KAS = CalculateKAS(w,newOrgan,currTime,lifeExpectancy);
						
						for(int i=0; i<max_patients_asked; i++){
							if(KAS>=ListPrioritized.get(i).getLeft()){
								for(int j=(max_patients_asked-1);j>i;j--){
									ListPrioritized.set(j, ListPrioritized.get(j-1));
								}
								Pair<Double, Vertex> newPair = new Pair<Double, Vertex>(KAS,w);
								ListPrioritized.set(i,newPair);
								break;
							}
						}	
					}
					for (WaitlistedPatient w : DKWaitingList.get(BloodType.A)){
						//calculate KAS and enter (KAS,patient) into queue
						double KAS = CalculateKAS(w,newOrgan,currTime,lifeExpectancy);
						for(int i=0; i<max_patients_asked; i++){
							if(KAS>=ListPrioritized.get(i).getLeft()){
								for(int j=(max_patients_asked-1);j>i;j--){
									ListPrioritized.set(j, ListPrioritized.get(j-1));
								}
								Pair<Double, Vertex> newPair = new Pair<Double, Vertex>(KAS,w);
								ListPrioritized.set(i,newPair);
								break;
							}
						}
					}
					for (WaitlistedPatient w : DKWaitingList.get(BloodType.B)){
						//calculate KAS and enter (KAS,patient) into queue
						double KAS = CalculateKAS(w,newOrgan,currTime,lifeExpectancy);
						for(int i=0; i<max_patients_asked; i++){
							if(KAS>=ListPrioritized.get(i).getLeft()){
								for(int j=(max_patients_asked-1);j>i;j--){
									ListPrioritized.set(j, ListPrioritized.get(j-1));
								}
								Pair<Double, Vertex> newPair = new Pair<Double, Vertex>(KAS,w);
								ListPrioritized.set(i,newPair);
								break;
							}
						}
					}
					for (WaitlistedPatient w : DKWaitingList.get(BloodType.AB)){
						//calculate KAS and enter (KAS,patient) into queue
						double KAS = CalculateKAS(w,newOrgan,currTime,lifeExpectancy);
						for(int i=0; i<max_patients_asked; i++){
							if(KAS>=ListPrioritized.get(i).getLeft()){
								for(int j=(max_patients_asked-1);j>i;j--){
									ListPrioritized.set(j, ListPrioritized.get(j-1));
								}
								Pair<Double, Vertex> newPair = new Pair<Double, Vertex>(KAS,w);
								ListPrioritized.set(i,newPair);
								break;
							}
						}
					}
				}
				if(newOrgan.getBloodTypeDonor() == BloodType.A){
					// he can give to A or AB
					
					for (WaitlistedPatient w : DKWaitingList.get(BloodType.A)){
						//calculate KAS and enter (KAS,patient) into queue
						double KAS = CalculateKAS(w,newOrgan,currTime,lifeExpectancy);
						for(int i=0; i<max_patients_asked; i++){
							if(KAS>=ListPrioritized.get(i).getLeft()){
								for(int j=(max_patients_asked-1);j>i;j--){
									ListPrioritized.set(j, ListPrioritized.get(j-1));
								}
								Pair<Double, Vertex> newPair = new Pair<Double, Vertex>(KAS,w);
								ListPrioritized.set(i,newPair);
								break;
							}
						}
					}
					for (WaitlistedPatient w : DKWaitingList.get(BloodType.AB)){
						//calculate KAS and enter (KAS,patient) into queue
						double KAS = CalculateKAS(w,newOrgan,currTime,lifeExpectancy);
						for(int i=0; i<max_patients_asked; i++){
							if(KAS>=ListPrioritized.get(i).getLeft()){
								for(int j=(max_patients_asked-1);j>i;j--){
									ListPrioritized.set(j, ListPrioritized.get(j-1));
								}
								Pair<Double, Vertex> newPair = new Pair<Double, Vertex>(KAS,w);
								ListPrioritized.set(i,newPair);
								break;
							}
						}
					}
				}
				if(newOrgan.getBloodTypeDonor() == BloodType.B){
					// he can give to B or AB
					for (WaitlistedPatient w : DKWaitingList.get(BloodType.B)){
						//calculate KAS and enter (KAS,patient) into queue
						double KAS = CalculateKAS(w,newOrgan,currTime,lifeExpectancy);
						for(int i=0; i<max_patients_asked; i++){
							if(KAS>=ListPrioritized.get(i).getLeft()){
								for(int j=(max_patients_asked-1);j>i;j--){
									ListPrioritized.set(j, ListPrioritized.get(j-1));
								}
								Pair<Double, Vertex> newPair = new Pair<Double, Vertex>(KAS,w);
								ListPrioritized.set(i,newPair);
								break;
							}
						}
					}
					for (WaitlistedPatient w : DKWaitingList.get(BloodType.AB)){
						//calculate KAS and enter (KAS,patient) into queue
						double KAS = CalculateKAS(w,newOrgan,currTime,lifeExpectancy);
						for(int i=0; i<max_patients_asked; i++){
							if(KAS>=ListPrioritized.get(i).getLeft()){
								for(int j=(max_patients_asked-1);j>i;j--){
									ListPrioritized.set(j, ListPrioritized.get(j-1));
								}
								Pair<Double, Vertex> newPair = new Pair<Double, Vertex>(KAS,w);
								ListPrioritized.set(i,newPair);
								break;
							}
						}
					}
				}
				else if(newOrgan.getBloodTypeDonor() == BloodType.AB){
					// he can give to AB
					for (WaitlistedPatient w : DKWaitingList.get(BloodType.AB)){
						//calculate KAS and enter (KAS,patient) into queue
						double KAS = CalculateKAS(w,newOrgan,currTime,lifeExpectancy);
						for(int i=0; i<max_patients_asked; i++){
							if(KAS>=ListPrioritized.get(i).getLeft()){
								for(int j=(max_patients_asked-1);j>i;j--){
									ListPrioritized.set(j, ListPrioritized.get(j-1));
								}
								Pair<Double, Vertex> newPair = new Pair<Double, Vertex>(KAS,w);
								ListPrioritized.set(i,newPair);
								break;
							}
						}
					}	
				}
				// ask patient based on priority 
				System.out.print("Size of waiting list for O "+DKWaitingList.get(BloodType.O).size()+"\n");
				System.out.print("Size of waiting list for A "+DKWaitingList.get(BloodType.A).size()+"\n");
				System.out.print("Size of waiting list for B "+DKWaitingList.get(BloodType.B).size()+"\n");
				System.out.print("Size of waiting list for AB "+DKWaitingList.get(BloodType.AB).size()+"\n");
				boolean rejected=false;
				for(int i=0;i<max_patients_asked;i++){
					if(ListPrioritized.get(i).getRight()==null){
						//we have reached the end of the prioritized list (rest is null)
						rejected=true;
						break;
					}
					else{
						if(Math.random()>=organ_rejection_prob){
							//the patient accepts the organ
							System.out.println("Patient ID "+ListPrioritized.get(i).getRight().getID()+" was given a kidney");
							
							// keep information
							PatientInformationHolder newInfo = new PatientInformationHolder(currTime  ,(WaitlistedPatient)ListPrioritized.get(i).getRight(), newOrgan, "receivedDDK");
							allinfo.add(newInfo);
							
							// remove from Waiting list
							DKWaitingList = DKGen.removePatient(DKWaitingList,ListPrioritized.get(i).getRight().getID());
							
							break;
						}
						else{
							System.out.println("The " + (i+1)+"th patient asked did not accept the organ");
							
						}
					}
					
				}
				if (rejected){
					System.out.println("All compatible patients were asked and declined, so the organ will be discarted\n");
				}
				

			}
			currDeceasedEvent = DeceasedEvent.getNextDeceasedEvent(patientsByExitTime, patientsByEntryTime, altruistsByEntryTime, patientsByLeavingTime, organEvents);

			currTime = currDeceasedEvent.getTime();
			
			

		}
		
		System.out.print("Size of waiting list for O "+DKWaitingList.get(BloodType.O).size()+"\n");
		System.out.print("Size of waiting list for A "+DKWaitingList.get(BloodType.A).size()+"\n");
		System.out.print("Size of waiting list for B "+DKWaitingList.get(BloodType.B).size()+"\n");
		System.out.print("Size of waiting list for AB "+DKWaitingList.get(BloodType.AB).size()+"\n");

		System.out.print("Total organs arrived: "+total_organs+"\n");
		System.out.print("Total new patients arrived: "+total_new_patients+"\n");
		
	
		
		System.out.print("Done! :)");

		lifespanTimeGen_young = new ExponentialArrivalDistribution(lambda_young, r);
		lifespanTimeGen_old = new ExponentialArrivalDistribution(lambda_old, r);
		
		//write information in CSV
		
		Utils.printPatientInformation(pathOutput, allinfo);

		return;
	}
	
	//method to calculate KAS
	public static double CalculateKAS(WaitlistedPatient patient, DeceasedOrgan organ, double currTime, double lifeExpectancy){
		double KAS = 0.8*Math.min(lifeExpectancy-patient.getAge()+(currTime-patient.getEntryTime()),0)*(1-organ.getDPI())+
				0.8*(currTime-patient.getEntryTime())*organ.getDPI()
				+0.2*(currTime-patient.getEntryTime())
				+0.04*patient.getCPRA();
		if (patient.isIsAnAltruist()){
			KAS = KAS + 100000;
		}
		return KAS;
	}

	
}
