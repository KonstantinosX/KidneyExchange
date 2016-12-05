package edu.umd.cs.mechdesign.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Experiments {

	public static void main(String[] args) {

		/*
		 * to01 has 10 runs, starting from lambda = 1% to 10% of the incoming
		 * patients being altruists
		 */

		/*
		 * to05 has 5 run starting from lambda = 10% to 50% of the incoming
		 * patients being altruists
		 */
		String csvFile = "patients";
		String line = "";
		String cvsSplitBy = ",";

		double drSum = 0;
		double waitSum = 0;

		for (int i = 0; i < 10; i++) {
			double sum = 0;
			int numLines = 0;
			int countDeaths = 0;
			String fl = csvFile + "_" + i + ".csv";
			// System.out.println("file: " + fl);
			try (BufferedReader br = new BufferedReader(new FileReader(fl))) {

				int size = 0;
				while ((line = br.readLine()) != null) {

					size++;
					// skip header
					if (numLines == 0) {
						numLines++;
						continue;
					}

					// use comma as separator
					String[] patients = line.split(cvsSplitBy);

					if (!patients[6].equals("DEATH")) {

						double waitingTime = Double.parseDouble(patients[2])
								- Double.parseDouble(patients[1]);
						sum += waitingTime;
						numLines++;
					} else {
						countDeaths++;
					}

				}
				// System.out.println("sum: " + sum);
				// System.out.println("numLines: " + numLines);
				// System.out.println("size " + size);
				double avg = (sum / numLines);
				System.out.println(i + " AVG waiting time: " + avg);
				System.out.println("Deaths: " + countDeaths);
				System.out.println("total patients: " + size);

				waitSum += avg;

				double deathRate = ((double) countDeaths / size) * 100;
				drSum += deathRate;

			} catch (IOException e) {
				e.printStackTrace();
				// return;
			}
		}

		System.out.println("AVG Death rate: " + (drSum / 3));
		System.out.println("AVG Waiting rate: " + (waitSum / 3));

	}
}
