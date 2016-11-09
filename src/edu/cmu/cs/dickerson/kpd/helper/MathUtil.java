package edu.cmu.cs.dickerson.kpd.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MathUtil {

	private MathUtil() {
	}

	public static boolean isInteger(double d) {
		return (d == Math.rint(d) && !Double.isInfinite(d) && !Double.isNaN(d));
	}

	public static int minIndex(List<Double> list) {
		return list.indexOf(Collections.min(list));
	}
}
