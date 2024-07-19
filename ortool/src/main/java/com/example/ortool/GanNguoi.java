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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/** Assignment problem. */
public class GanNguoi {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data
        int[][] costs = {
                { 120, 150, 80 },
                { 150, 225, 90 },
                { 156, 195, 104 },
        };

        int[][] product_price = {
                { 1, 1, 1 },
                { 1, 1, 1 },
                { 1, 1, 1 },
        };

        // int[][] skillRequire = { { 1, 2 }, { 1, 2, 3 }, { 2, 3 } };
        int[] skillRequire = { 1, 2, 3 };
        int[][] workerSkill = {
                { 1, 2, 3 }, // worker 1
                { 1, 2, 3 }, // worker 2
                { 1, 2, 3 }, // worker 3
        };

        int[] debt = { 1, 1, 3 };

        int[][] resource = {
                { 0, 1 },
                { 2 }
        };

        int[] load = { 2, 0 };

        int[][] times = { { 0, 10 }, { 11, 20 }, { 5, 25 } };
        int[][][] workerShift = {
                { { 0, 4 } },
                { { 10, 11 } },
                { { 10, 11 } }
        };

        int[] numWorkersRequired = { 1, 2, 1 };

        int[][] workerRequire = { null, { 1, 2 }, null };

        int[][] banWorker = { { 0, 1 }, null, null };

        final int numWorkers = costs.length;
        final int numTasks = costs[0].length;

        final int[] allWorkers = IntStream.range(0, numWorkers).toArray();
        final int[] allTasks = IntStream.range(0, numTasks).toArray();

        LinearExprBuilder obj = LinearExpr.newBuilder();
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
        // % load
        for (int j = 0; j < resource.length; j++) {
            LinearExprBuilder lst = LinearExpr.newBuilder();
            // List<Literal> lst = new ArrayList<>();
            IntVar l_percent = model.newIntVar(-numTasks, numTasks, "");
            IntVar optimize_val = model.newIntVar(0, numTasks, "");
            IntVar real_percent = model.newIntVar(0, numTasks, "");
            IntVar temp_val = model.newIntVar(-numTasks, numTasks, "");
            // IntVar l_percent = model.newIntVar(-100, 100, "");
            // IntVar optimize_val = model.newIntVar(0, 100, "");
            // IntVar real_percent = model.newIntVar(0, 100, "");
            // IntVar temp_val = model.newIntVar(-100, 0, "");

            model.addEquality(LinearExpr.sum(new IntVar[] { real_percent, temp_val }), 0);

            Literal d = model.newBoolVar("");

            model.addGreaterOrEqual(real_percent, l_percent).onlyEnforceIf(d);
            model.addEquality(l_percent, -load[j]).onlyEnforceIf(d);
            ;
            model.addEquality(optimize_val, LinearExpr.sum(new IntVar[] { real_percent, l_percent })).onlyEnforceIf(d);

            model.addLessOrEqual(real_percent, l_percent).onlyEnforceIf(d.not());
            model.addEquality(l_percent, load[j]).onlyEnforceIf(d.not());
            model.addEquality(optimize_val, LinearExpr.sum(new IntVar[] { temp_val, l_percent }))
                    .onlyEnforceIf(d.not());

            for (int i : resource[j]) {
                for (int task : allTasks) {
                    lst.add(x[i][task]);
                }
            }
            model.addGreaterOrEqual(lst, real_percent);
            // model.addEquality(optimize_val, 0);
            obj.add(optimize_val);
        }

        // DEBT
        for (int worker : allWorkers) {
            LinearExprBuilder debt_lst = LinearExpr.newBuilder();
            for (int task : allTasks) {
                debt_lst.addTerm(x[worker][task], product_price[worker][task]);
            }
            model.addLessOrEqual(debt_lst, debt[worker]);
        }

        // Skill constraint
        for (int task : allTasks) {
            for (int worker : allWorkers) {
                for (int skill : workerSkill[worker]) {
                    if (skillRequire[task] == skill) {
                        IntVar skillRequirementVar = model.newConstant(skillRequire[task]);
                        IntVar workerSkillVar = model.newConstant(skill);
                        model.addEquality(skillRequirementVar,
                                workerSkillVar).onlyEnforceIf(x[worker][task]);
                    }
                }
            }
        }
        // Ensure the number of people doing each job
        for (int task : allTasks) {
            List<Literal> assignedWorkers = new ArrayList<>();
            for (int worker : allWorkers) {
                assignedWorkers.add(x[worker][task]);
            }
            Literal[] assignedWorkersArray = assignedWorkers.toArray(new Literal[0]);
            LinearExpr sumExpr = LinearExpr.sum(assignedWorkersArray);
            model.addGreaterOrEqual(sumExpr, numWorkersRequired[task]);
        }
        // Task requirement for specific workers
        for (int task = 0; task < numTasks; task++) {
            if (workerRequire[task] != null) {
                List<Literal> taskRequirements = new ArrayList<>();
                for (int worker : workerRequire[task]) {
                    taskRequirements.add(x[worker][task]);
                }
                Literal[] taskRequirementsArray = taskRequirements.toArray(new Literal[0]);
                model.addBoolOr(taskRequirementsArray);
            }
        }
        // task not for workers
        for (int task = 0; task < numTasks; task++) {
            if (banWorker[task] != null) {
                for (int worker : banWorker[task]) {
                    model.addEquality(x[worker][task], 0);
                }
            }
        }
        // Objective
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
                                + ".  Cost: " + costs[worker][task] + "; start: " + times[task][0] + "; end: "
                                + times[task][1]);
                    }
                }
            }
        } else {
            System.err.println("No solution found.");
        }
    }

}