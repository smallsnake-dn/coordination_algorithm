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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/** Assignment problem. */
public class Newest {
  public static void main(String[] args) {
    Loader.loadNativeLibraries();
    // Data
    int[][] costs = {
        { 120, 150, 80 },
        { 180, 225, 120 },
        { 156, 195, 104 },
    };

    int[][] resource = {
        { 0, 1 },
        { 2 }
    };

    int[] load = { 2, 0 };

    // {price, [tuyen ap dung]}
    HashMap<Integer, int[]> day_price = new HashMap<>();
    day_price.put(200, new int[] { 1 });

    int[][] times = { { 0, 10 }, { 11, 20 }, { 5, 25 } };
    int[][][] vehicle_shift = {
        // { { 9, 10 } },
        null,
        null,
        null,
        // { { 24, 25 } }
        // {{10, 20}}
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

    for (int worker : allWorkers) {
      List<IntervalVar> lst = new ArrayList<>();
      // shift constraint
      if (vehicle_shift[worker] != null) {
        for (int[] shift : vehicle_shift[worker]) {
          int shift_st = shift[0];
          int shift_en = shift[1];
          IntVar _shift_st = model.newConstant(shift_st);
          IntVar _shift_en = model.newConstant(shift_en);
          IntVar _shift_sten = model.newConstant(shift_en - shift_st);
          lst.add(model.newIntervalVar(_shift_st, _shift_sten, _shift_en, "worker-task-shift" + worker));
        }
      }
      // time constraint
      for (int task : allTasks) {
        int st = times[task][0];
        int en = times[task][1];
        IntVar _st = model.newConstant(st);
        IntVar _en = model.newConstant(en);
        IntVar _sten = model.newConstant(en - st);
        lst.add(model.newOptionalIntervalVar(_st, _sten, _en, x[worker][task], "worker-task" + task + worker));
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

    // thue xe theo ngay
    // chi ap dung cho các nha van chuyen la doi tac
    Set<Integer> keyDayPrice = day_price.keySet();
    if (!keyDayPrice.isEmpty()) {
      for (int price : keyDayPrice) {
        LinearExprBuilder lst = LinearExpr.newBuilder();
        for (int worker : day_price.get(price)) {
          for (int task : allTasks) {
            lst.addTerm(x[worker][task], costs[worker][task]);
          }
          model.addGreaterOrEqual(lst, price);
        }
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
                + ".  Cost: " + costs[worker][task] + "; start: " + times[task][0] + "; end: " + times[task][1]);
          }
        }
      }
    } else {
      System.err.println("No solution found.");
    }
  }

}