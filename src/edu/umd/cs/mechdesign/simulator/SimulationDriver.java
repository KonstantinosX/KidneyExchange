package edu.umd.cs.mechdesign.simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.cs.dickerson.kpd.dynamic.arrivals.ExponentialArrivalDistribution;
import edu.cmu.cs.dickerson.kpd.helper.CycleTimeComparator;
import edu.cmu.cs.dickerson.kpd.helper.IOUtil;
import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.helper.VertexTimeComparator;
import edu.cmu.cs.dickerson.kpd.solver.CycleFormulationCPLEXSolver;
import edu.cmu.cs.dickerson.kpd.solver.exception.SolverException;
import edu.cmu.cs.dickerson.kpd.solver.solution.Solution;
import edu.cmu.cs.dickerson.kpd.structure.Cycle;
import edu.cmu.cs.dickerson.kpd.structure.Edge;
import edu.cmu.cs.dickerson.kpd.structure.Pool;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.cmu.cs.dickerson.kpd.structure.VertexPair;
import edu.cmu.cs.dickerson.kpd.structure.alg.CycleGenerator;
import edu.cmu.cs.dickerson.kpd.structure.alg.CycleMembership;
import edu.cmu.cs.dickerson.kpd.structure.alg.FailureProbabilityUtil;
import edu.cmu.cs.dickerson.kpd.structure.generator.SaidmanPoolGenerator;

public class SimulationDriver {
	private static final Logger logger = Logger
			.getLogger(SimulationDriver.class.getSimpleName());

	private static ExponentialArrivalDistribution arrivalTimeGen;
	private static ExponentialArrivalDistribution lifespanTimeGen;
	private static ExponentialArrivalDistribution transplantTimeGen;
	private static ExponentialArrivalDistribution altArrivalTimeGen;

	public static void main(String[] args) {
		double m = 3.5;
		double lambda = 0.005;

		double arrivalLambda = 0.3;
		double timeLimit = 200;
		// lambda = 5 or 6
		// // List of m parameters (for every one time period, expect m vertices
		// to enter, Poisson process)
		// List<Double> mList = Arrays.asList(new Double[] {
		// //0.1, 0.2, 0.5, 1.0, 2.0,
		// 100.0,
		// });
		//
		// // List of lambda parameters (every vertex has lifespan of
		// exponential clock with parameter lambda)
		// List<Double> lambdaList = Arrays.asList(new Double[] {
		// //0.025, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9,
		// 1.0,
		// //1.5, 2.0,
		// });

		boolean doOptIPUB2 = true; // Do optimal IP solve on C(3,4) but using
									// infinite chain extension on 4-chains

		// Set up our random generators for pools (sample from UNOS data, sample
		// from Saidman distribution)
		Random r = new Random();

		arrivalTimeGen = new ExponentialArrivalDistribution(m, r);
		lifespanTimeGen = new ExponentialArrivalDistribution(lambda, r);
		transplantTimeGen = new ExponentialArrivalDistribution(m, r);
		altArrivalTimeGen = new ExponentialArrivalDistribution(arrivalLambda, r);

		double failure_param1 = 0.7; // e.g., constant failure rate of 70%

		// UNOSGenerator UNOSGen = UNOSGenerator.makeAndInitialize(
		// IOUtil.getBaseUNOSFilePath(), ',', r);
		SaidmanPoolGenerator SaidmanGen = new SaidmanPoolGenerator(r);

		// Optimize w.r.t. discounted or raw utility?
		boolean usingFailureProbabilities = false;
		FailureProbabilityUtil.ProbabilityDistribution failDist = FailureProbabilityUtil.ProbabilityDistribution.CONSTANT;

		if (!usingFailureProbabilities) {
			failDist = FailureProbabilityUtil.ProbabilityDistribution.NONE;
		}

		// Cycle and chain limits
		int cycleCap = 3;
		int chainCap = 4;
		int graphSize = 50;

		// IOUtil.dPrintln("\n*****\nGraph (|V|=" + graphSize + ", #" + graphRep
		// + "/" + numGraphReps + "), cap: " + chainCap + ", gen: "
		// + gen.getClass().getSimpleName() + "\n*****\n");

		// Generate pool (~5% altruists, UNOS might be different);
		int numPairs = (int) Math.round(graphSize * 0.95);
		int numAlts = graphSize - numPairs;

		Pool pool = SaidmanGen.generate(numPairs, numAlts);

		/*
		 * Conducts all possible matchings in the initial pool so that the pool
		 * we use to start the simulation is stable (no further matches can
		 * happen)
		 */
		CycleGenerator cg = new CycleGenerator(pool);

		conductAllRemainingTransplants(pool, cg, cycleCap, chainCap);

		logger.info("Pool: " + pool);
		logger.info("Altruists: " + pool.getAltruists());
		// GreedyPackingSolver s = new GreedyPackingSolver(pool);

		// If we're setting failure probabilities, do that here:
		if (usingFailureProbabilities) {
			FailureProbabilityUtil.setFailureProbability(pool, failDist, r,
					failure_param1);
		}

		// ====== SIMULATION ======

		double currTime = 0.0;
		Event currEvent = null;
		double lastExitTime = -1;

		/**
		 * vertexID -> (arrivalTime, maxDepartureTime) This will be updated
		 * after every transplant as well as every arrival of a new patient
		 */
		// Map<Integer, Pair<Double, Double>> patientTimes = new
		// HashMap<Integer, Pair<Double, Double>>();
		Set<Integer> patients = new HashSet<Integer>();

		/**
		 * A priority queue that maintains pairs (exitTime, vertex), prioritized
		 * by exitTime
		 */
		Queue<Pair<Double, Vertex>> verticesByExitTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());

		/**
		 * A priority queue that maintains pairs (entryTime, vertex),
		 * prioritized by entryTime
		 */
		Queue<Pair<Double, Vertex>> verticesByEntryTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());

		/**
		 * A priority queue that maintains the entry events for new altruists
		 * that are supposed to come into the system prioritized by entryTime
		 */
		Queue<Double> altEvents = new PriorityQueue<Double>((int) timeLimit);

		/**
		 * A priority queue that maintains the altruists that are supposed to
		 * come into the system prioritized by entryTime. We add new entries in
		 * this queue when we add the altruist vertices
		 */
		Queue<Pair<Double, Vertex>> altruistsByEntryTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());

		/**
		 * A priority queue that maintains the set of cycle transplants that
		 * need to happen and at what time.
		 */
		Queue<Pair<Double, Cycle>> cycleTransplantTimes = new PriorityQueue<Pair<Double, Cycle>>(
				1, new CycleTimeComparator());
		Queue<Double> matchingTimes = new LinkedList<Double>();

		/*
		 * create initial entry and exit times for the individuals already in
		 * the pool
		 */
		for (VertexPair v : pool.getPairs()) {

			/*
			 * get the times at which the patients/donor pairs enter the pool
			 * and exit the pool (by means of death :-/)
			 */
			if (!patients.contains(v.getID())) {

				/*
				 * Draw arrival and departure times from exponential clocks It
				 * is okay if entryTime == exitTime; the vertex enters and is
				 * critical simultaneously
				 */
				double entryTime = currTime + arrivalTimeGen.draw();
				double exitTime = entryTime + lifespanTimeGen.draw();
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

				patients.add(v.getID());

				// we don't care when these vertices entered, because they're
				// already in the pool
				verticesByExitTime.add(new Pair<Double, Vertex>(exitTime, v));

				System.out.println(v + " exit time :" + exitTime);
			}

		}

		// schedule the arrival of altruists
		double altrTime = currTime;
		while (true) {

			double entryTime = altrTime + altArrivalTimeGen.draw();
			altrTime = entryTime;

			if (altrTime > timeLimit) {
				/*
				 * If this vertex would arrive after the time limit, we're done
				 * generating
				 */
				break;
			}

			altEvents.add(entryTime);
		}

		/*
		 * 
		 * set the starting time of the simulation as the last entry time from
		 * the individuals in the initial pool
		 */
		double startingTime = currTime;
		logger.info("Last entry : " + startingTime);

		/*
		 * NOTE : Time needs to be relevant with the exit times of the vertex
		 * pairs that are already in the pool, therefore I guess we can attempt
		 * to start the simulation at the time the last vertex pair in the pool
		 * entered the pool, and run it for some time timeLimit after that.
		 * Also, we're assuming no matchings can happen after the last vertex
		 * pair has entered. This gives a disadvantage to everyone who's already
		 * in the pool...
		 * 
		 * The simulation could (and most likely will) start with the first
		 * death from the previous pool
		 */

		/* Do the matchings at a regular interval */
		double matchingsTime = currTime;

		// schedule the matchings
		// set the interval for when to do the matchings (every interval days);
		double interval = 5;
		while (true) {

			if (matchingsTime >= timeLimit) {
				break;
			} else {
				matchingsTime += interval;
			}

			matchingTimes.add(matchingsTime);
		}

		System.out.println("Entry time of the last patient: " + currTime);

		/*
		 * add new arrivals that will take place afterwards (only add pairs not
		 * altruists for now)
		 */
		logger.info("Scheduling new vertex pair arrivals...");
		while (true) {

			/*
			 * Draw arrival and departure times from exponential clocks It is
			 * okay if entryTime == exitTime; the vertex enters and is critical
			 * simultaneously
			 */
			double entryTime = currTime + arrivalTimeGen.draw();
			double exitTime = entryTime + lifespanTimeGen.draw();
			currTime = entryTime;

			if (currTime > timeLimit) {
				/*
				 * If this vertex would arrive after the time limit, we're done
				 * generating
				 */
				break;
			}
			if (exitTime > lastExitTime) {
				/*
				 * Store the exit time of the final vertex in the pool (after
				 * all other vertices have gone critical)
				 */
				lastExitTime = exitTime;
			}

			/*
			 * add times to the appropriate queues. Initially the vertex pair to
			 * be added is null, and will be generated at add time later
			 */
			verticesByEntryTime.add(new Pair<Double, Vertex>(entryTime, null));
			// System.out.println("currTime: " + currTime);
		}

		currTime = startingTime;

		// get starting event before the simulation starts running.
		// the next event can't be a transplant because we haven't had any
		// matchings yet
		currEvent = Event.getNextEvent(matchingTimes, cycleTransplantTimes,
				verticesByExitTime, verticesByEntryTime, altEvents);

		/*
		 * the first event is probably going to be a death event from the
		 * initial pool
		 */
		currTime = currEvent.getTime();

		logger.info("First event is: " + currEvent);
		// System.exit(0);
		logger.info("The following events will be executed");
		System.out.println("Matching Times: " + matchingTimes);
		System.out.println("CycleTransplant Times: " + cycleTransplantTimes);
		System.out.println("VerticesBy Exit Time: " + verticesByExitTime);
		System.out.println("VerticesBy Entry Time: " + verticesByEntryTime);

		System.out.println("Altruist Events " + altEvents);
		logger.info("State of the Pool: " + pool);
		// System.exit(0);

		while (currTime < timeLimit) {

			logger.info("AT TIME: " + currTime);
			if (currEvent.getType().equals(EventType.TERMINATE_SIMULATION)) {
				logger.info("No more events to process...Terminating Simulation...");
				break;
			}
			if (currEvent.getType().equals(EventType.PATIENT_DIES)) {

				/*
				 * remove vertex pair from the pool, remove the event from the
				 * queue
				 */
				Vertex toRemove = verticesByExitTime.poll().getRight();
				if (pool.removeVertex(toRemove)) {
					logger.info("We lost a patient... RIP");
				}

				/*
				 * break and remove the patient from any scheduled transplants,
				 * cycles and chains. We've lost them :/
				 */
				Iterator<Pair<Double, Cycle>> it = cycleTransplantTimes
						.iterator();
				Pair<Double, Cycle> newAddition = null;

				while (it.hasNext()) {
					Pair<Double, Cycle> cc = it.next();
					Cycle c = cc.getRight();
					if (Cycle.isAChain(c, pool)) {
						if (Cycle.getConstituentVertices(c, pool).contains(
								toRemove)) {
							logger.info("Breaking a chain! " + c
									+ " for removing " + toRemove);

							/*
							 * How do we handle chains? We probably need to
							 * break the chain at the patient tha we lost, and
							 * do the transplan on the chain up until this
							 * patient only
							 * 
							 * a chain is 2 -> A, 3 -> 2, 4 -> 3, 5 -> 4, A-> 5
							 * (begins and ends with the altruist) where A is
							 * the altruist
							 */
							List<Edge> chainEdges = new ArrayList<>();
							Iterator<Edge> chainIter = c.getEdges().iterator();

							/*
							 * Get the portion of the chain before the patient
							 * that we're removing
							 */
							while (chainIter.hasNext()) {
								Edge e = chainIter.next();

								if (e.getSrc().equals(toRemove.toString())) {
									break;
								} else {
									chainEdges.add(e);
								}
							}
							Cycle brokenChain = Cycle.makeCycle(chainEdges,
									c.getWeight());

							newAddition = new Pair<>(cc.getLeft(), brokenChain);
						}

						// reomve the old chain
						// it.remove();

					} else {

						/*
						 * the cycle breaks, so we need to remove it completely
						 * from the queue of cycles
						 */
						if (Cycle.getConstituentVertices(cc.getRight(), pool)
								.contains(toRemove)) {
							it.remove();
						}
					}

				}
				/*
				 * add the new broken chain to the queue with the same scheduled
				 * time
				 */
				if (newAddition != null) {
					cycleTransplantTimes.add(newAddition);
				}

			}

			// add new patient
			if (currEvent.getType().equals(EventType.PATIENT_ENTERS)) {

				int addPair = 1;
				int addAltruist = 0;
				Set<Vertex> l = SaidmanGen.addVerticesToPool(pool, addPair,
						addAltruist);

				// we're only generating one at a time
				Vertex toAdd = l.iterator().next();

				/* add new vertex to the pool */
				// VertexPair toAdd = (VertexPair) verticesByEntryTime.poll()
				// .getRight();
				// pool.addPair((VertexPair) toAdd);

				logger.info("Adding new patient");
				logger.info("State of the Pool: " + pool);
				/* setting exit time */
				double exitTime = currTime + lifespanTimeGen.draw();
				verticesByExitTime
						.add(new Pair<Double, Vertex>(exitTime, toAdd));

				System.out.println(toAdd + " exit time :" + exitTime);
			}

			// check if it's time to do matchings
			if (currEvent.getType().equals(EventType.CONDUCT_MATCHINGS)) {
				// run matching, update
				logger.info("conducting matchings..");

				/*
				 * add cycles and the time they should happen to the priority
				 * queue. Schedule them within the next x weeks at random
				 */

				/*
				 * add cycles and the times at which the transplants will happen
				 */
				Solution s = conductMatches(pool, cg, cycleCap, chainCap);

				/*
				 * Add the new matchings to the queue of matchings that should
				 * happen.
				 */
				for (Cycle c : s.getMatching()) {
					cycleTransplantTimes.add(new Pair<Double, Cycle>(
							(currTime + transplantTimeGen.draw()), c));
				}

				// TODO schedule chains seperately

				logger.info("State of pool: " + pool);
			}

			if (currEvent.getType().equals(EventType.CONDUCT_TRANSPLANT)) {
				logger.info("Transplant done...");

				/*
				 * TODO conducting a transplant means removing the patients from
				 * the pool properly. With some probability someone backs out.
				 * 
				 * We don't need to remove the exit event from the
				 * vertexExitevents queue, because we check if the vertex is
				 * still in the pool before doing anything in the PATIENT_DIES
				 * event
				 */
				Cycle toTransplant = cycleTransplantTimes.poll().getRight();
				for (Vertex v : Cycle
						.getConstituentVertices(toTransplant, pool)) {
					pool.removeVertex(v);
				}
			}

			if (currEvent.getType().equals(EventType.ALTRUIST_ENTERS)) {

				int addPair = 0;
				int addAltruist = 1;
				Set<Vertex> l = SaidmanGen.addVerticesToPool(pool, addPair,
						addAltruist);

				// we're only generating one at a time
				Vertex toAdd = l.iterator().next();

				logger.info("Adding new altruist");
				logger.info("State of the Pool: " + pool);

				/* adding the altruist by entry time exit time */
				altruistsByEntryTime.add(new Pair<Double, Vertex>(currTime,
						toAdd));

				altEvents.poll();
			}

			//
			currEvent = Event.getNextEvent(matchingTimes, cycleTransplantTimes,
					verticesByExitTime, verticesByEntryTime, altEvents);
			currTime = currEvent.getTime();
			// System.exit(0);
		}
		;

		logger.info("pool: " + pool);
	}

	/**
	 * Does matchings and transplants until there can be no more matchings done
	 * in the pool
	 * 
	 * @param pool
	 * @param cg
	 * @param cycleCap
	 * @param chainCap
	 */
	private static void conductAllRemainingTransplants(Pool pool,
			CycleGenerator cg, int cycleCap, int chainCap) {

		while (true) {
			Solution s = conductMatches(pool, cg, cycleCap, chainCap);

			/*
			 * Add the new matchings to the queue of matchings that should
			 * happen.
			 */
			if (s.getMatching().isEmpty())
				return;

			for (Cycle c : s.getMatching()) {
				for (Vertex v : Cycle.getConstituentVertices(c, pool)) {
					pool.removeVertex(v);
				}
			}
		}
	}

	public static Solution conductMatches(Pool pool, CycleGenerator cg,
			int cycleCap, int chainCap) {
		logger.info("State of the Pool: " + pool);
		boolean usingFailureProbabilities = false;
		double failure_param1 = 0.7; // e.g., constant failure rate of 70%
		try {

			// This should happen after a constant interval
			List<Cycle> cyclesUB2 = cg.generateCyclesAndChains(cycleCap,
					chainCap, usingFailureProbabilities, false, failure_param1);
			CycleMembership membershipUB2 = new CycleMembership(pool, cyclesUB2);

			// Get optimal match size for pool on C(3,some non-infinite
			// chain cap + infinite extension)
			CycleFormulationCPLEXSolver optIPUB2S = new CycleFormulationCPLEXSolver(
					pool, cyclesUB2, membershipUB2);
			Solution sol = optIPUB2S.solve();

			logger.info(sol.getMatching().toString());
			IOUtil.dPrintln("'Optimal IP' (UB extension) Value: "
					+ sol.getObjectiveValue());
			// Try to GC
			cyclesUB2 = null;
			membershipUB2 = null;
			System.gc();

			return sol;
		} catch (SolverException e) {
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

	// Simulation
	// t is the current time we're at and allocated_time is the maximum time we
	// should allocate to this simulation (for how long we should run it)

	// eventQueue = new Queue();
	// Do matches, add set of transplant events in the queue (these happen back
	// to back?)
	//
	// while(t < allocated_time)

	// if 1 week has passed re-run matches

	// while(true)
	//

	// t += nextEvent().getTime();

}
