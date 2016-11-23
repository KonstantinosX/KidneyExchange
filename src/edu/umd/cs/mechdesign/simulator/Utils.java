package edu.umd.cs.mechdesign.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;

import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.helper.VertexTimeComparator;
import edu.cmu.cs.dickerson.kpd.io.AltruistOutput;
import edu.cmu.cs.dickerson.kpd.io.PatientListOutput;
import edu.cmu.cs.dickerson.kpd.io.PatientListOutput.Col;
import edu.cmu.cs.dickerson.kpd.io.PatientTransplantOutput.TCol;
import edu.cmu.cs.dickerson.kpd.io.AltruistOutput.ACol;
import edu.cmu.cs.dickerson.kpd.io.PatientTransplantOutput;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.cmu.cs.dickerson.kpd.structure.VertexAltruist;
import edu.cmu.cs.dickerson.kpd.structure.VertexPair;
import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;

public class Utils {

	/**
	 * Serialize the set of patients in the pool as well as the future patients
	 * to a csv file for sharing them with the deceased donor simulator
	 * 
	 * @param entryList
	 *            The list of patients that enter the pool in the entirety of
	 *            the simulation's runtime as well as each patient's entry time
	 * @param exitTimes
	 *            The set of all patients including each patient's exit time.
	 * @param path
	 */
	public static void serializePatients(List<Pair<Double, Vertex>> entryList,
			Map<String, Double> exitTimes, String path) {
		PatientListOutput out;
		try {
			out = new PatientListOutput(path);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (Pair<Double, Vertex> p : entryList) {
			/* set entry and exit times */
			VertexPair patient = (VertexPair) p.getRight();
			out.set(Col.VERTEX_ID, patient.toString());
			out.set(Col.TIME_ENTERED, p.getLeft());
			out.set(Col.TIME_LEAVING, exitTimes.get(patient.toString()));
			out.set(Col.BLOOD_TYPE, patient.getBloodTypePatient());

			/* record tuple */
			try {
				out.record();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Serialize the set of transplants that happened in the simulation
	 * including the time the transplants happened to a csv file for sharing
	 * them with the deceased donor simulator
	 */
	public static void serializeTransplants(String path,
			Map<Vertex, Double> transplantTimes, double startingTime) {
		PatientTransplantOutput out;
		try {
			out = new PatientTransplantOutput(path, startingTime);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (Entry<Vertex, Double> patientTransplant : transplantTimes
				.entrySet()) {
			Vertex v = patientTransplant.getKey();
			if (!(v instanceof VertexAltruist)) {

				out.set(TCol.BLOOD_TYPE, ((VertexPair) patientTransplant
						.getKey()).getBloodTypeDonor());
				out.set(TCol.TRANSPLANT_TIME, patientTransplant.getValue());

				/* record tuple */
				try {
					out.record();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * Writes information about the altruists that entered the simulation to a
	 * csv file
	 * 
	 * @param altruistsPath
	 * @param patientAges
	 * @param altruistsByEntryTime
	 * @param startingTime
	 */
	public static void serializeAltruists(String altruistsPath,
			Map<String, Double> patientAges,
			Queue<Pair<Double, Vertex>> altruistsByEntryTime,
			double startingTime) {

		AltruistOutput out;
		try {
			out = new AltruistOutput(altruistsPath, startingTime);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (Pair<Double, Vertex> altruist : altruistsByEntryTime) {
			Vertex alt = altruist.getRight();
			out.set(ACol.BLOOD_TYPE, ((VertexAltruist) alt).getBloodTypeDonor());
			out.set(ACol.ILLNESS_TIME, altruist.getLeft());
			out.set(ACol.AGE, patientAges.get(alt.toString()));

			/* record tuple */
			try {
				out.record();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static double calculateTimeOfEntry(double Age, double CurrTime){
		Random random = new Random();
		// 2015 source:
		// https://optn.transplant.hrsa.gov/data/view-data-reports/national-data/#
		double Pr_PATIENT_0_1 = 0.000656093;
		double Pr_PATIENT_1_5 = 0.006075993;
		double Pr_PATIENT_6_10 = 0.00487791;
		double Pr_PATIENT_11_17 = 0.017828617;
		double Pr_PATIENT_18_34 = 0.116157006;
		double Pr_PATIENT_35_49 = 0.265175719;
		double Pr_PATIENT_50_64 = 0.411912369;
		double Pr_PATIENT_65 = 0.177316294;
		
		
		double AgeOfEntry = 0;
		// assume that can only get sick in next age period
		if(Age <=1 ){
			double r = random.nextDouble();
			if (r <= (Pr_PATIENT_1_5/(Pr_PATIENT_1_5 + Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 1 + 4 * r1;
			}
			else if (r <= (Pr_PATIENT_6_10/(Pr_PATIENT_1_5 + Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 5 + 5 * r1;
			}
			else if (r <= (Pr_PATIENT_11_17/(Pr_PATIENT_1_5 + Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 10 + 7 * r1;
			}
			else if (r <= (Pr_PATIENT_18_34/(Pr_PATIENT_1_5 + Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 17 + 17 * r1;
			}
			else if (r <= (Pr_PATIENT_35_49/(Pr_PATIENT_1_5 + Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 34 + 15 * r1;
			}
			else if (r <= (Pr_PATIENT_50_64/(Pr_PATIENT_1_5 + Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 49 + 15 * r1;
			}
			else if (r <= (Pr_PATIENT_65/(Pr_PATIENT_1_5 + Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 65 + 15 * r1;
			}

			
		}
		else if (Age <=5 ){
			double r = random.nextDouble();
			if (r <= (Pr_PATIENT_6_10/( Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 5 + 5 * r1;
			}
			else if (r <= ((Pr_PATIENT_6_10 + Pr_PATIENT_11_17)/(Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 10 + 7 * r1;
			}
			else if (r <= ((Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34)/(Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 17 + 17 * r1;
			}
			else if (r <= ((Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49)/( Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 34 + 15 * r1;
			}
			else if (r <= ((Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64)/(Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 49 + 15 * r1;
			}
			else if (r <= ((Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65)/(Pr_PATIENT_6_10 + Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 65 + 15 * r1;
			}
		}
		else if (Age <=10 ){
			double r = random.nextDouble();
			if (r <= (Pr_PATIENT_11_17/(Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 10 + 7 * r1;
			}
			else if (r <= ((Pr_PATIENT_11_17 + Pr_PATIENT_18_34)/(Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 17 + 17 * r1;
			}
			else if (r <= ((Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49)/( Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 34 + 15 * r1;
			}
			else if (r <= ((Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64)/(Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 49 + 15 * r1;
			}
			else if (r <= ((Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65)/(Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 65 + 15 * r1;
			}
		}
		else if (Age <=17 ){
			double r = random.nextDouble();
			if (r <= (Pr_PATIENT_18_34/(Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 17 + 17 * r1;
			}
			else if (r <= ((Pr_PATIENT_18_34 + Pr_PATIENT_35_49)/(Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 34 + 15 * r1;
			}
			else if (r <= ((Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64)/(Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 49 + 15 * r1;
			}
			else if (r <= ((Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65)/(Pr_PATIENT_18_34 + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 65 + 15 * r1;
			}
		}
		else if (Age <=34 ){
			double r = random.nextDouble();
			if (r <= (Pr_PATIENT_35_49/(Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 34 + 15 * r1;
			}
			else if (r <= ((Pr_PATIENT_35_49 + Pr_PATIENT_50_64)/(Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 49 + 15 * r1;
			}
			else if (r <= ((Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65)/(Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 65 + 15 * r1;
			}
		}
		else if (Age <=49 ){
			double r = random.nextDouble();
			if (r <= ((Pr_PATIENT_50_64)/(Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 49 + 15 * r1;
			}
			else if (r <= ((Pr_PATIENT_50_64 + Pr_PATIENT_65)/( Pr_PATIENT_50_64 + Pr_PATIENT_65))   ) {
				double r1 = random.nextDouble();
				AgeOfEntry = 65 + 15 * r1;
			}
		}
		else if (Age <=64 ){
			double r1 = random.nextDouble();
			AgeOfEntry = 65 + 15 * r1;
		}
		double timeOfEntry = CurrTime + AgeOfEntry - Age;
		
		return timeOfEntry;
	}
	
	
	public static Queue<Pair<Double, WaitlistedPatient>> readAltruists(double timeLimit, double deceasedSimulationZeroTime, PatientsForDeceasedDonorGenerator DKGen){
		
		// create queue than includes entry time and altruists
		Queue<Pair<Double, WaitlistedPatient>> altruistsByEntryTime = new PriorityQueue<Pair<Double, WaitlistedPatient>>(
				(int) timeLimit, new PatientTimeComparator());
		
		
		String csvFile = "././altruists.csv";
        String line = "";
        String cvsSplitBy = ",";
        int myline=1;
        double livingSimulationZeroTime = 0;
        double entryTimeInLivingSimulation = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] info = line.split(cvsSplitBy);
                
                if(myline==1){
                	livingSimulationZeroTime = Double.parseDouble(info[3]);
                	System.out.println("Simulation Time: "+info[3]);
                }
                else{
                	System.out.println("Age " + info[0] + " , Blood Type " + info[1]);
                	// create a new patient for each altruistic
                	BloodType bloodTypePatient = null;
                	if(info[1].equals("O")){
                		bloodTypePatient = BloodType.O;
                	}
                	else if(info[1].equals("A")){
                		bloodTypePatient = BloodType.A;
                	}
                	else if(info[1].equals("B")){
                		bloodTypePatient = BloodType.B;
                	}
                	else if(info[1].equals("AB")){
                		bloodTypePatient = BloodType.AB;
                	}
                	entryTimeInLivingSimulation = Double.parseDouble(info[2]);
                	
                	double TimeOfTransplantInDeceased = deceasedSimulationZeroTime + (entryTimeInLivingSimulation-livingSimulationZeroTime);
                	
                	double EntryTime = calculateTimeOfEntry(Double.parseDouble(info[0]), TimeOfTransplantInDeceased);
                	
                	double ageWhenEntersWaitingList = Double.parseDouble(info[0]) + (EntryTime - TimeOfTransplantInDeceased);
                	// age defined as the age that the patient will have when he enters the waiting list
                	WaitlistedPatient w = new WaitlistedPatient(DKGen.getNewID(), DKGen.drawCPRA(),ageWhenEntersWaitingList,bloodTypePatient, true);
                	
                	System.out.println("\n Time that altruist will arrive " + EntryTime + " \n");
                	
                	
                	altruistsByEntryTime.add(new Pair<Double, WaitlistedPatient>(EntryTime, w));
                }
                myline++;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        System.out.printf("The fist altruist blood type: "+altruistsByEntryTime.peek().getRight().getBloodTypePatient());
        return altruistsByEntryTime;
	}
	
	
	
	// read the csv file and return a list of patient objects
public static Queue<Pair<Double, WaitlistedPatient>> readPatientGivenLDK(double timeLimit, double deceasedSimulationZeroTime, PatientsForDeceasedDonorGenerator DKGen){
		
		// create queue than includes entry time and altruists
		Queue<Pair<Double, WaitlistedPatient>> patientsByLeavingTime = new PriorityQueue<Pair<Double, WaitlistedPatient>>(
				(int) timeLimit, new PatientTimeComparator());
		
		
		String csvFile = "././transplants.csv";
        String line = "";
        String cvsSplitBy = ",";
        int myline=1;
        double livingSimulationZeroTime = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] info = line.split(cvsSplitBy);
                
                if(myline==1){
                	livingSimulationZeroTime = Double.parseDouble(info[2]);
                	System.out.println("Simulation Time: "+info[2]);
                }
                else{
                	System.out.println("Tranplant time " + info[0] + " , Blood Type " + info[1]);
                	// create a new patient for each altruistic
                	BloodType bloodTypePatient = null;
                	if(info[1].equals("O")){
                		bloodTypePatient = BloodType.O;
                	}
                	else if(info[1].equals("A")){
                		bloodTypePatient = BloodType.A;
                	}
                	else if(info[1].equals("B")){
                		bloodTypePatient = BloodType.B;
                	}
                	else if(info[1].equals("AB")){
                		bloodTypePatient = BloodType.AB;
                	}
                	
                	WaitlistedPatient w = new WaitlistedPatient(-1, 0,0,bloodTypePatient, false);
                	
                	double patientTransplantTimeInLiving = Double.parseDouble(info[0]);
                	double patientLeavesDeceasedSimulationTime = deceasedSimulationZeroTime + (patientTransplantTimeInLiving - livingSimulationZeroTime);
                	System.out.println("\n Time that he leaves the simulation " + patientLeavesDeceasedSimulationTime + " \n");
                	
                	
                	patientsByLeavingTime.add(new Pair<Double, WaitlistedPatient>(patientLeavesDeceasedSimulationTime, w));
                }
                myline++;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return patientsByLeavingTime;
	}

}
