package edu.umd.cs.mechdesign.simulator;

import java.util.Comparator;

import edu.cmu.cs.dickerson.kpd.helper.Pair;

public class PatientTimeComparator implements Comparator<Pair<Double,WaitlistedPatient>> {

	@Override
	public int compare(Pair<Double, WaitlistedPatient> o1, Pair<Double, WaitlistedPatient> o2) {
		
		// Primary: Sort in increasing order of doubles
		int first = o1.getLeft().compareTo(o2.getLeft());
		if(first == 0) {
			// Secondary: Sort in increasing order of vertex IDs
			return o1.getRight().compareTo(o2.getRight());
		} else {
			return first;
		}
	}

}
