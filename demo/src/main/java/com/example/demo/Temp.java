package com.example.demo;


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
public class Temp {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data
        int[][] costs = {
                { 90, 76, 75, 20 },
                { 35, 85, 55, 20 },
                { 35, 85, 55, 20 },
        };

        int[] totalSizeMax = { 1, 2, 1 };
        int[][] times = { { 0, 10 }, { 11, 20 }, { 5, 25 }, { 5, 25 } };
        int[][][] workerShift = {
                { // Worker 0
                        { 0, 4 },
                // { 13, 17 }
                },
                // { // Worker 1
                // { 0, 11 },
                // { 18, 31 }
                // },
                // { // Worker 2
                // { 18, 31 },
                // // { 15, 19 }
                // }
                null, null
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
        // Additional variables and constraints for maximum tasks per worker
        IntVar[] numTasksPerWorker = new IntVar[numWorkers];
        for (int worker : allWorkers) {
            numTasksPerWorker[worker] = model.newIntVar(0, totalSizeMax[worker], "numTasksWorker" + worker);
            List<Literal> tasksForWorker = new ArrayList<>();
            for (int task : allTasks) {
                tasksForWorker.add(x[worker][task]);
            }
            model.addEquality(LinearExpr.sum(tasksForWorker.toArray(new Literal[0])), totalSizeMax[worker]);
        }
        // Additional variables and constraints for task scheduling
        for (int worker : allWorkers) {
            List<IntervalVar> lst = new ArrayList<>();
            // shift constraint
            if (workerShift[worker] != null) {
                for (int[] shift : workerShift[worker]) {
                    int shift_st = shift[0];
                    int shift_en = shift[1];
                    IntVar _shift_st = model.newConstant(shift_st);
                    IntVar _shift_en = model.newConstant(shift_en);
                    IntVar _shift_size = model.newConstant(shift_en - shift_st);
                    lst.add(model.newIntervalVar(_shift_st, _shift_size, _shift_en,
                            "worker-task-shift" + worker));
                }
            }
            // time constraint
            for (int task : allTasks) {
                int st = times[task][0];
                int en = times[task][1];
                IntVar _st = model.newConstant(st);
                IntVar _en = model.newConstant(en);
                IntVar _size = model.newConstant(en - st);
                lst.add(model.newOptionalIntervalVar(_st, _size, _en, x[worker][task],
                        "worker-task" + task + worker));
            }
            model.addNoOverlap(lst);
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