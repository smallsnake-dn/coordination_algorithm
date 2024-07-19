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
public class LoadTruck {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data

        // lenght, width, heigh, weigh
        int[][] items = {
                { 10, 5, 10, 1 },
                { 10, 5, 10, 1 }
        };

        int MAX_LENGTH = 40;
        int MAX_WIDTH = 40;
        int MAX_HEIGHT = 40;

        // Model
        CpModel model = new CpModel();

        List<IntVar[]> intVarItems = new LinkedList<>();
        List<IntVar[]> oIntVarItems = new LinkedList<>();
        List<IntervalVar[]> xyz = new LinkedList<>();
        LinearExprBuilder obj = LinearExpr.newBuilder();
        for (var item : items) {
            IntVar length = model.newIntVar(0, MAX_LENGTH, "null");
            IntVar width = model.newIntVar(0, MAX_WIDTH, "null");
            IntVar height = model.newIntVar(0, MAX_HEIGHT, "null");
            intVarItems.add(new IntVar[] { length, width, height });
            obj.add(length);
            obj.add(width);
            obj.add(height);

            IntVar oLength = model.newIntVar(0, MAX_LENGTH, "null");
            IntVar oWidth = model.newIntVar(0, MAX_WIDTH, "null");
            IntVar oHeight = model.newIntVar(0, MAX_HEIGHT, "null");
            model.addEquality(LinearExpr.sum(new LinearArgument[] { oLength, model.newConstant(item[0]) }), length);
            model.addEquality(LinearExpr.sum(new LinearArgument[] { oWidth, model.newConstant(item[1]) }), width);
            model.addEquality(LinearExpr.sum(new LinearArgument[] { oHeight, model.newConstant(item[2]) }), height);
            oIntVarItems.add(new IntVar[] { oLength, oWidth, oHeight });

            // IntervalVar x = model.newIntervalVar(oLength, model.newConstant(item[0]), length,
            //         "null");
            // IntervalVar y = model.newIntervalVar(oWidth, model.newConstant(item[1]), width,
            //         "null");
            // IntervalVar z = model.newIntervalVar(oHeight, model.newConstant(item[2]), height,
            //         "null");
            // xyz.add(new IntervalVar[] {x, y, z});
        }

        for (int i = 0; i < items.length; i++) {
            for (int j = 0; j < items.length; j++) {
                if (i == j)
                    continue;

                // IntervalVar x1 = xyz.get(i)[0];
                // IntervalVar x2 = xyz.get(j)[0];
                // IntervalVar y1 = xyz.get(i)[1];
                // IntervalVar y2 =xyz.get(j)[1];
                // IntervalVar z1 = xyz.get(i)[2];
                // IntervalVar z2 = xyz.get(j)[2];
                
                Literal bX = model.newBoolVar("null");
                Literal _bX = model.newBoolVar("null");
                model.addEquality(_bX, model.trueLiteral()).onlyEnforceIf(bX.not());
                IntervalVar x1 = model.newOptionalIntervalVar(oIntVarItems.get(i)[0], model.newConstant(items[i][0]), intVarItems.get(i)[0], bX, "null");
                IntervalVar x2 = model.newOptionalIntervalVar(oIntVarItems.get(j)[0], model.newConstant(items[j][0]), intVarItems.get(j)[0], bX, "null");
                
                Literal bY = model.newBoolVar("null");
                Literal _bY = model.newBoolVar("null");
                model.addEquality(_bY, model.trueLiteral()).onlyEnforceIf(bY.not());
                IntervalVar y1 = model.newOptionalIntervalVar(oIntVarItems.get(i)[1], model.newConstant(items[i][1]), intVarItems.get(i)[1], bX, "null");
                IntervalVar y2 = model.newOptionalIntervalVar(oIntVarItems.get(j)[1], model.newConstant(items[j][1]), intVarItems.get(j)[1], bX, "null");
                
                Literal bZ = model.newBoolVar("null");
                Literal _bZ = model.newBoolVar("null");
                model.addEquality(_bZ, model.trueLiteral()).onlyEnforceIf(bZ.not());
                IntervalVar z1 = model.newOptionalIntervalVar(oIntVarItems.get(i)[2], model.newConstant(items[i][2]), intVarItems.get(i)[2], bX, "null");
                IntervalVar z2 = model.newOptionalIntervalVar(oIntVarItems.get(j)[2], model.newConstant(items[j][2]), intVarItems.get(j)[2], bX, "null");

                
                NoOverlap2dConstraint constraintx2d = model.addNoOverlap2D();
                List<IntervalVar> lstX = new ArrayList<>();
                // lstX.add(model.newOptionalIntervalVar(oIntVarItems.get(i)[0], model.newConstant(items[i][0]), intVarItems.get(i)[0], bLength, "null"));
                // lstX.add(model.newOptionalIntervalVar(oIntVarItems.get(j)[0], model.newConstant(items[j][0]), intVarItems.get(j)[0], bLength, "null"));
                lstX.add(x1);
                lstX.add(x2);
                model.addNoOverlap(lstX);
                model.addEquality(bX, model.trueLiteral());
                // model.ad
                // model.addGreaterThan(oIntVarItems.get(j)[0], intVarItems.get(i)[0]);
                constraintx2d.addRectangle(y1, z1);
                constraintx2d.addRectangle(y2, z2);
                // constraintx2d.onlyEnforceIf(_bX);

                // Literal bWidth = model.newBoolVar("null");
                // Literal _bWidth = model.newBoolVar("null");
                // NoOverlap2dConstraint constrainty2d = model.addNoOverlap2D();
                // model.addEquality(_bWidth, model.falseLiteral()).onlyEnforceIf(bWidth);
                // List<IntervalVar> lstY = new ArrayList<>();
                // lstY.add(x1);
                // lstY.add(x2);
                // model.addNoOverlap(lstY).onlyEnforceIf(bLength);
                // constrainty2d.addRectangle(x1, z1).onlyEnforceIf(_bWidth);
                // constrainty2d.addRectangle(x2, z2).onlyEnforceIf(_bWidth);

                // Literal bHeight = model.newBoolVar("null");
                // Literal _bHeight = model.newBoolVar("null");
                // NoOverlap2dConstraint constraintz2d = model.addNoOverlap2D();
                // model.addEquality(_bHeight, model.falseLiteral()).onlyEnforceIf(bHeight);
                // List<IntervalVar> lstZ = new ArrayList<>();
                // lstZ.add(x1);
                // lstZ.add(x2);
                // model.addNoOverlap(lstZ).onlyEnforceIf(bLength);
                // constraintz2d.addRectangle(x1, y1).onlyEnforceIf(_bHeight);
                // constraintz2d.addRectangle(x2, y2).onlyEnforceIf(_bHeight);

            }
        }

        // Variables
        model.minimize(obj);

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
        for(int i = 0; i < items.length; i++) {
            System.out.print("x1::" + solver.value(oIntVarItems.get(i)[0]));
            System.out.println("x2::" + solver.value(intVarItems.get(i)[0]));
            System.out.print("y1::" + solver.value(oIntVarItems.get(i)[1]));
            System.out.println("y2::" + solver.value(intVarItems.get(i)[1]));
            System.out.print("z1::" + solver.value(oIntVarItems.get(i)[2]));
            System.out.println("z2::" + solver.value(intVarItems.get(i)[2]));
            System.out.println("=====================================");
        }
    }

}