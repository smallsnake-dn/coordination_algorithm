package com.example.demo.load_truck;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearArgument;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;
import com.google.ortools.sat.NoOverlap2dConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/** Assignment problem. */
public class Ovlap2 {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data

        // Model
        CpModel model = new CpModel();

        Literal b = model.newBoolVar("null");
        NoOverlap2dConstraint overlap2d = model.addNoOverlap2D();
        overlap2d.onlyEnforceIf(model.trueLiteral());
        overlap2d.addRectangle(
                model.newIntervalVar(model.newConstant(0), model.newConstant(2), model.newConstant(2), "null"),
                model.newIntervalVar(model.newConstant(0), model.newConstant(2), model.newConstant(2), "null"));
        NoOverlap2dConstraint overlap2dx = model.addNoOverlap2D();
        overlap2dx.addRectangle(
                model.newIntervalVar(model.newConstant(0), model.newConstant(2), model.newConstant(2), "null"),
                model.newIntervalVar(model.newConstant(0), model.newConstant(2), model.newConstant(2), "null"));
        // overlap2d.addRectangle(
        //         model.newIntervalVar(model.newConstant(3), model.newConstant(2), model.newConstant(5), "null"),
        //         model.newIntervalVar(model.newConstant(3), model.newConstant(2), model.newConstant(5), "null"));
        // overlap2d.
        // model.addNoOverlap2D()

        // Variables

        // Solve
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        // Print solution.
        // Check that the problem has a feasible solution.
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Total cost: " + solver.objectiveValue() + "\n");

        } else {
            System.err.println("No solution found.");
        }
    }

}