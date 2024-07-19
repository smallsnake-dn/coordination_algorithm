package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;

public class EmployeeCoordinator {
    public void coordinator(int[][] costMatrix, int[] routePrice, int[] debt, int[][] routeTime,
            List<List<int[]>> employeeShift) {
        final int numWorkers = costMatrix.length;
        final int numTasks = costMatrix[0].length;

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

        for (int worker : allWorkers) {
            List<IntervalVar> lst = new ArrayList<>();
            // shift constraint
            if (employeeShift.get(worker) != null) {
                for (int[] shift : employeeShift.get(worker)) {
                    int shift_st = shift[0];
                    int shift_en = shift[1];
                    IntVar _shift_st = model.newConstant(shift_st);
                    IntVar _shift_en = model.newConstant(shift_en);
                    IntVar _shift_size = model.newConstant(shift_en - shift_st);
                    lst.add(model.newIntervalVar(_shift_st, _shift_size, _shift_en, "worker-task-shift" + worker));
                }
            }
            // time constraint
            for (int task : allTasks) {
                int st = routeTime[task][0];
                int en = routeTime[task][1];
                IntVar _st = model.newConstant(st);
                IntVar _en = model.newConstant(en);
                IntVar _size = model.newConstant(en - st);
                lst.add(model.newOptionalIntervalVar(_st, _size, _en, x[worker][task], "worker-task" + task + worker));
            }
            model.addNoOverlap(lst);
        }

        // // % load
        // for (int j = 0; j < resource.length; j++) {
        // LinearExprBuilder lst = LinearExpr.newBuilder();
        // // List<Literal> lst = new ArrayList<>();
        // for (int i : resource[j]) {
        // for (int task : allTasks) {
        // lst.add(x[i][task]);
        // }
        // }
        // model.addGreaterOrEqual(lst, load[j]);
        // }

        // DEBT công nợ
        for (int worker : allWorkers) {
            LinearExprBuilder debt_lst = LinearExpr.newBuilder();
            for (int task : allTasks) {
                debt_lst.addTerm(x[worker][task], routePrice[task]);
            }
            model.addLessOrEqual(debt_lst, debt[worker]);
            // model.addExactlyOne(workers);
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
                obj.addTerm(x[worker][task], costMatrix[worker][task]);
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
                                + ".  Cost: " + costMatrix[worker][task] + "; start: " + routeTime[task][0] + "; end: "
                                + routeTime[task][1]);
                    }
                }
            }
        } else {
            System.err.println("No solution found.");
        }
    }
}
