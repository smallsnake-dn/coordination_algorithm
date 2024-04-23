package com.example.ortool;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/** Assignment problem. */
public class OrtoolApplication2 {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data
        int[][] costs = {
                { 90, 76, 75, 20 },
                { 35, 85, 55, 20 },
                { 35, 85, 55, 20 },
        };

        int[][] times = { { 0, 10 }, { 11, 20 }, { 5, 25 }, { 5, 25 } };
        int[][][] workerShift = {
                { // Worker 0
                  // { 0, 10 },
                        { 0, 25 }
                },
                { // Worker 1
                  // { 0, 11 },
                        { 0, 10 }
                },
                { // Worker 2
                        { 0, 20 },
                // { 10, 20 }
                }
        };
        final int numWorkers = costs.length;
        final int numTasks = costs[0].length;

        final int[] allWorkers = IntStream.range(0, numWorkers).toArray();
        final int[] allTasks = IntStream.range(0, numTasks).toArray();

        // Model
        CpModel model = new CpModel();

        // Variables
        Literal[][] x = new Literal[numWorkers][numTasks];
        for (int worker : allWorkers) {
            for (int task : allTasks) {
                x[worker][task] = model.newBoolVar("x[" + worker + "," + task + "]");
            }
        }
        // workerShift in times
        for (int worker : allWorkers) {
            for (int i = 0; i < numTasks; i++) {
                LinearExprBuilder timeConstraint = LinearExpr.newBuilder();
                for (int j = 0; j < workerShift[worker].length; j++) {
                    int start = workerShift[worker][j][0];
                    int end = workerShift[worker][j][1];
                    if (times[i][0] >= start && times[i][1] <= end) {
                        timeConstraint.addTerm(x[worker][i], 1);
                    }
                }
                model.addGreaterOrEqual(timeConstraint.build(), 1);
                // System.out.println(
                // "Time constraint for worker " + worker + ", task " + i + ": " +
                // timeConstraint.build());
            }
        }
        // Each task is assigned to exactly one worker.
        for (int task : allTasks) {
            List<Literal> workers = new ArrayList<>();
            for (int worker : allWorkers) {
                workers.add(x[worker][task]);
            }
            model.addExactlyOne(workers);
        }
        // Objective
        LinearExprBuilder obj = LinearExpr.newBuilder();
        for (int worker : allWorkers) {
            for (int task : allTasks) {
                obj.addTerm(x[worker][task], costs[worker][task]);
            }
        }
        model.minimize(obj);

        // Solve
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        // Print solution.
        // Check that the problem has a feasible solution.
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Total cost: " + solver.objectiveValue() + "\n");
            for (int worker : allWorkers) {
                for (int task : allTasks) {
                    if (solver.booleanValue(x[worker][task])) {
                        System.out.println("Worker " + worker + " assigned to task " + task
                                + ".  Cost: " + costs[worker][task]);
                    }
                }
            }
        } else {
            System.err.println("No solution found.");
        }
    }
}
