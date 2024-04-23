package com.example.ortool;

import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.IntVar;
import com.google.ortools.constraintsolver.RoutingDimension;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

@SpringBootApplication
public class OrtoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrtoolApplication.class, args);
	}

	static class Time {
		int startTime;
		int endTime;

		public Time() {
		}

		public Time(int startTime, int endTime) {
			this.startTime = startTime;
			this.endTime = endTime;
		}

		@Override
		public String toString() {
			return "Time [startTime=" + startTime + ", endTime=" + endTime + "]";
		}
	}

	@Bean
	public static String getOr() {
		Loader.loadNativeLibraries();
		// Data
		double[][] costs = {
				{ 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
						100, 100, 100, 100, 100,
						100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
						100, 100, 100, 100,
						100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
						100, 100, 100, 100,
						100, 100, },
				{ 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
						200, 200, 200, 200, 200,
						200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
						200, 200, 200, 200,
						200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
						200, 200, 200, 200,
						200, 200, },
				{ 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300,
						300, 300, 300, 300, 300,
						300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300,
						300, 300, 300, 300,
						300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300,
						300, 300, 300, 300,
						300, 300, },
				{ 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400,
						400, 400, 400, 400, 400,
						400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400,
						400, 400, 400, 400,
						400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400,
						400, 400, 400, 400,
						400, 400, },
				{ 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
						500, 500, 500, 500, 500,
						500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
						500, 500, 500, 500,
						500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
						500, 500, 500, 500,
						500, 500, },
				{ 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600,
						600, 600, 600, 600, 600,
						600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600,
						600, 600, 600, 600,
						600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600, 600,
						600, 600, 600, 600,
						600, 600, },
				{ 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700,
						700, 700, 700, 700, 700,
						700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700,
						700, 700, 700, 700,
						700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700, 700,
						700, 700, 700, 700,
						700, 700, },
				{ 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800,
						800, 800, 800, 800, 800,
						800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800,
						800, 800, 800, 800,
						800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800, 800,
						800, 800, 800, 800,
						800, 800, },
		};
		int numWorkers = costs.length;
		int numTasks = costs[0].length;

		int[] totalSizeMax = { 4, 2, 3, 4, 5, 6, 7, 3000 };
		int priority_Carrier = 4;
		int position = 0;

		MPSolver solver = new MPSolver("JobAssignment",
				MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);

		MPVariable[][] x = new MPVariable[numWorkers][numTasks];
		for (int worker = 0; worker < numWorkers; worker++) {
			for (int task = 0; task < numTasks; task++) {
				x[worker][task] = solver.makeBoolVar("x[" + worker + "," + task + "]");
			}
		}

		// Constraints
		for (int worker = 0; worker < numWorkers; worker++) {
			MPConstraint constraint = solver.makeConstraint(0, totalSizeMax[worker], "");
			for (int task = 0; task < numTasks; task++) {
				constraint.setCoefficient(x[worker][task], 1);
			}
		}

		MPConstraint constraintForThreeTasks = solver.makeConstraint(priority_Carrier, priority_Carrier, "");

		for (int task = 0; task < numTasks; task++) {
			constraintForThreeTasks.setCoefficient(x[position][task], 1);
		}

		for (int task = 0; task < numTasks; task++) {
			MPConstraint constraint = solver.makeConstraint(1, 1, "");
			for (int worker = 0; worker < numWorkers; worker++) {
				constraint.setCoefficient(x[worker][task], 1);
			}
		}

		// Objective
		MPObjective objective = solver.objective();
		for (int worker = 0; worker < numWorkers; worker++) {
			for (int task = 0; task < numTasks; task++) {
				objective.setCoefficient(x[worker][task], costs[worker][task]);
			}
		}
		objective.setMinimization();

		// Solve
		MPSolver.ResultStatus resultStatus = solver.solve();
		double costTB = 0.0;
		// Print solution
		if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
			System.out.println("Total cost: " + objective.value() + "\n");
			for (int worker = 0; worker < numWorkers; worker++) {
				for (int task = 0; task < numTasks; task++) {
					if (x[worker][task].solutionValue() > 0.5) {
						if (worker == 0) {
							costTB += costs[worker][task];
						}
						System.out.println("Worker " + worker + " assigned to task " + task
								+ ". Cost: " + costs[worker][task]);
					}
				}
			}
			System.out.println(costTB);
		} else {
			System.err.println("No solution found.");
		}
		return "";
	}

	private static final Logger logger = Logger.getLogger(OrtoolApplication.class.getName());

	static class DataModel {
		public final long[][] distanceMatrix = {
				{ 0, 6, 9, 8, 1, 1 },
				{ 6, 10, 8, 3, 1, 1 },
				{ 9, 8, 0, 11, 1, 1 },
				{ 8, 3, 11, 0, 1, 1 },
				{ 1, 1, 1, 1, 0, 1 },
				{ 1, 1, 1, 1, 1, 0 }
		};
		public final int[][] pickupsDeliveries = {
				// {1, 2},
				{ 3, 1 },
				{ 1, 4 }
				// {2, 10},
		};
		public final long[][] timeWindows = {
				{ 0, 5 }, // depot
				{ 7, 12 }, // 1
				{ 1, 9 }, // 2
				// {1,2},
				// {1,2},
		};
		public final int vehicleNumber = 2;
		public final int depot = 0;
	}

	static void printSolution(
			DataModel data, RoutingModel routing, RoutingIndexManager manager, Assignment solution) {
		// Solution cost.
		logger.info("Objective : " + solution.objectiveValue());
		// Inspect solution.
		RoutingDimension timeDimension = routing.getMutableDimension("Time");
		long totalTime = 0;
		for (int i = 0; i < data.vehicleNumber; ++i) {
			long index = routing.start(i);
			logger.info("Route for Vehicle " + i + ":");
			String route = "";
			while (!routing.isEnd(index)) {
				IntVar timeVar = timeDimension.cumulVar(index);
				route += manager.indexToNode(index) + " Time(" + solution.min(timeVar) + ","
						+ solution.max(timeVar) + ") -> ";
				index = solution.value(routing.nextVar(index));
			}
			IntVar timeVar = timeDimension.cumulVar(index);
			route += manager.indexToNode(index) + " Time(" + solution.min(timeVar) + ","
					+ solution.max(timeVar) + ")";
			logger.info(route);
		}
		logger.info("Total time of all routes: " + totalTime + "min");
	}

}
