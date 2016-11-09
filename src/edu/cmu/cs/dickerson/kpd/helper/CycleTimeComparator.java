package edu.cmu.cs.dickerson.kpd.helper;

import java.util.Comparator;

import edu.cmu.cs.dickerson.kpd.structure.Cycle;

public class CycleTimeComparator implements Comparator<Pair<Double, Cycle>> {

	@Override
	public int compare(Pair<Double, Cycle> o1, Pair<Double, Cycle> o2) {

		/*
		 * Primary: Sort in increasing order of doubles (the time the cycles //
		 * were found) TODO this is too simple
		 */
		return o1.getLeft().compareTo(o2.getLeft());

	}

}
