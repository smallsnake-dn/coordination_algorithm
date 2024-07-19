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
public class LoadTruck2 {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data

        // lenght, width, heigh, weigh
        int[][] items = {
                { 10, 5, 10, 1 },
                { 10, 5, 10, 1 }
        };

        int MAX_LENGTH = 100;
        int MAX_WIDTH = 100;
        int MAX_HEIGHT = 100;

        // Model
        CpModel model = new CpModel();

        List<IntVar[]> intVarItems = new LinkedList<>();
        List<IntVar[]> oIntVarItems = new LinkedList<>();
        List<IntervalVar[]> xyz = new LinkedList<>();
        List<Literal[]> b = new ArrayList<>();
        List<Literal[]> bGLM = new ArrayList<>();
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

            Literal bX = model.newBoolVar("null");
            Literal bY = model.newBoolVar("null");
            Literal bZ = model.newBoolVar("null");

            IntervalVar x = model.newOptionalIntervalVar(oLength, model.newConstant(item[0]), length, bX,
                    "null");
            IntervalVar y = model.newOptionalIntervalVar(oWidth, model.newConstant(item[1]), width, bY,
                    "null");
            IntervalVar z = model.newOptionalIntervalVar(oHeight, model.newConstant(item[2]), height, bZ,
                    "null");
            b.add(new Literal[] {bX, bY, bZ});
            xyz.add(new IntervalVar[] {x, y, z});
        }

        // List<Literal> b = new ArrayList<>();

        for (int i = 0; i < items.length; i++) {
            for (int j = i + 1; j < items.length; j++) {
                if (i == j)
                    continue;

                IntervalVar x1 = xyz.get(i)[0];
                IntervalVar x2 = xyz.get(j)[0];
                IntervalVar y1 = xyz.get(i)[1];
                IntervalVar y2 =xyz.get(j)[1];
                IntervalVar z1 = xyz.get(i)[2];
                IntervalVar z2 = xyz.get(j)[2];
                
                Literal bX = b.get(i)[0];
                Literal _bX = b.get(j)[0];
                // Literal bX = model.newBoolVar("null");
                // Literal _bX = model.newBoolVar("null");
                // model.addEquality(_bX, model.trueLiteral()).onlyEnforceIf(bX.not());
                // IntervalVar x1 = model.newOptionalIntervalVar(oIntVarItems.get(i)[0], model.newConstant(items[i][0]), intVarItems.get(i)[0], bX, "null");
                // IntervalVar x2 = model.newOptionalIntervalVar(oIntVarItems.get(j)[0], model.newConstant(items[j][0]), intVarItems.get(j)[0], bX, "null");
                
                Literal bY = b.get(i)[1];
                Literal _bY = b.get(j)[1];
                // model.addEquality(_bY, model.trueLiteral()).onlyEnforceIf(bY.not());
                // IntervalVar y1 = model.newOptionalIntervalVar(oIntVarItems.get(i)[1], model.newConstant(items[i][1]), intVarItems.get(i)[1], bX, "null");
                // IntervalVar y2 = model.newOptionalIntervalVar(oIntVarItems.get(j)[1], model.newConstant(items[j][1]), intVarItems.get(j)[1], bX, "null");
                
                Literal bZ = b.get(i)[2];
                Literal _bZ = b.get(j)[2];
                // model.addEquality(_bZ, model.trueLiteral()).onlyEnforceIf(bZ.not());
                // IntervalVar z1 = model.newOptionalIntervalVar(oIntVarItems.get(i)[2], model.newConstant(items[i][2]), intVarItems.get(i)[2], bX, "null");
                // IntervalVar z2 = model.newOptionalIntervalVar(oIntVarItems.get(j)[2], model.newConstant(items[j][2]), intVarItems.get(j)[2], bX, "null");

                
                NoOverlap2dConstraint constraintX = model.addNoOverlap2D();
                List<IntervalVar> lstX = new ArrayList<>();
                lstX.add(x1);
                lstX.add(x2);
                model.addNoOverlap(lstX);
                constraintX.addRectangle(y1, z1);
                constraintX.addRectangle(y2, z2);
                
                
                NoOverlap2dConstraint constraintY = model.addNoOverlap2D();
                List<IntervalVar> lstY = new ArrayList<>();
                lstY.add(y1);
                lstY.add(y2);
                model.addNoOverlap(lstY);
                constraintY.addRectangle(x1, z1);
                constraintY.addRectangle(x2, z2);
                
                
                NoOverlap2dConstraint constraintZ = model.addNoOverlap2D();
                List<IntervalVar> lstZ = new ArrayList<>();
                lstZ.add(z1);
                lstZ.add(z2);
                model.addNoOverlap(lstZ);
                constraintZ.addRectangle(x1, y1);
                constraintZ.addRectangle(x2, y2);

                model.addEquality(bX, model.trueLiteral()).onlyEnforceIf(new Literal[] {bY.not(), bZ.not()});
                model.addEquality(bY, model.trueLiteral()).onlyEnforceIf(new Literal[] {bZ.not(), _bX.not()});
                model.addEquality(bZ, model.trueLiteral()).onlyEnforceIf(new Literal[] {bX.not(), _bY.not()});

                Literal xf = model.newBoolVar("null");
                Literal bxG = model.newBoolVar("null");
                model.addGreaterThan(oIntVarItems.get(j)[0], intVarItems.get(i)[0]).onlyEnforceIf(bxG);
                model.addGreaterThan(intVarItems.get(j)[0], intVarItems.get(i)[0]).onlyEnforceIf(bxG);
                Literal bxL = model.newBoolVar("null");
                model.addLessThan(oIntVarItems.get(j)[0], oIntVarItems.get(i)[0]).onlyEnforceIf(bxL);
                model.addLessThan(intVarItems.get(j)[0], oIntVarItems.get(i)[0]).onlyEnforceIf(bxL);
                Literal bxM = model.newBoolVar("null");
                model.addEquality(bxM, model.trueLiteral()).onlyEnforceIf(new Literal[] {bxG.not(), bxL.not(), xf});
                // model.addBoolOr(new Literal[] {bxG, bxL, bxM});
                bGLM.add(new Literal[] {bxG, bxL, bxM});

                
                
                Literal yf = model.newBoolVar("null");
                Literal byG = model.newBoolVar("null");
                model.addGreaterThan(oIntVarItems.get(j)[1], intVarItems.get(i)[1]).onlyEnforceIf(byG);
                model.addGreaterThan(intVarItems.get(j)[1], intVarItems.get(i)[1]).onlyEnforceIf(byG);
                Literal byL = model.newBoolVar("null");
                model.addLessThan(oIntVarItems.get(j)[1], oIntVarItems.get(i)[1]).onlyEnforceIf(byL);
                model.addLessThan(intVarItems.get(j)[1], oIntVarItems.get(i)[1]).onlyEnforceIf(byL);
                Literal byM = model.newBoolVar("null");
                model.addEquality(byM, model.trueLiteral()).onlyEnforceIf(new Literal[] {byG.not(), byL.not(), yf});
                model.addBoolOr(new Literal[] {byG, byL, byM});
                bGLM.add(new Literal[] {byG, byL, byM});

                
                
                Literal zf = model.newBoolVar("null");
                Literal bzG = model.newBoolVar("null");
                model.addGreaterThan(oIntVarItems.get(j)[2], intVarItems.get(i)[2]).onlyEnforceIf(bzG);
                model.addGreaterThan(intVarItems.get(j)[2], intVarItems.get(i)[2]).onlyEnforceIf(bzG);
                Literal bzL = model.newBoolVar("null");
                model.addLessThan(oIntVarItems.get(j)[2], oIntVarItems.get(i)[2]).onlyEnforceIf(bzL);
                model.addLessThan(intVarItems.get(j)[2], oIntVarItems.get(i)[2]).onlyEnforceIf(bzL);
                Literal bzM = model.newBoolVar("null");
                model.addEquality(bzM, model.trueLiteral()).onlyEnforceIf(new Literal[] {bzG.not(), bzL.not(), zf});
                model.addBoolOr(new Literal[] {bzG, bzL, bzM});
                bGLM.add(new Literal[] {bzG, bzL, bzM});

                // model.addEquality(bX, model.trueLiteral()).onlyEnforceIf(new Literal[] {bxG.not(), bxL.not(), bxM, bY.not(), _bZ.not()});
                // model.addEquality(_bX, model.trueLiteral()).onlyEnforceIf(new Literal[] {bxG.not(), bxL.not(), bxM, bY.not(), _bZ.not()});

                // model.addEquality(bY, model.trueLiteral()).onlyEnforceIf(new Literal[] {byG.not(), byL.not(), byM, bX.not(), _bZ.not()});
                // model.addEquality(_bY, model.trueLiteral()).onlyEnforceIf(new Literal[] {byG.not(), byL.not(), byM, bX.not(), _bZ.not()});

                // model.addEquality(bZ, model.trueLiteral()).onlyEnforceIf(new Literal[] {bzG.not(), bzL.not(), bzM, bX.not(), _bY.not()});
                // model.addEquality(_bZ, model.trueLiteral()).onlyEnforceIf(new Literal[] {bzG.not(), bzL.not(), bzM, bX.not(), _bY.not()});
                
                
                
                model.addEquality(bX, model.trueLiteral()).onlyEnforceIf(new Literal[] {bxG.not(), bxL.not(), bxM});
                model.addEquality(_bX, model.trueLiteral()).onlyEnforceIf(new Literal[] {bxG.not(), bxL.not(), bxM});

                model.addEquality(bY, model.trueLiteral()).onlyEnforceIf(new Literal[] {byG.not(), byL.not(), byM});
                model.addEquality(_bY, model.trueLiteral()).onlyEnforceIf(new Literal[] {byG.not(), byL.not(), byM});

                model.addEquality(bZ, model.trueLiteral()).onlyEnforceIf(new Literal[] {bzG.not(), bzL.not(), bzM});
                model.addEquality(_bZ, model.trueLiteral()).onlyEnforceIf(new Literal[] {bzG.not(), bzL.not(), bzM});

                model.addEquality(bxG, model.falseLiteral()).onlyEnforceIf(bY);
                model.addEquality(bxL, model.falseLiteral()).onlyEnforceIf(bY);
                model.addEquality(bxM, model.falseLiteral()).onlyEnforceIf(bY);
                model.addEquality(bxG, model.falseLiteral()).onlyEnforceIf(bZ);
                model.addEquality(bxL, model.falseLiteral()).onlyEnforceIf(bZ);
                model.addEquality(bxM, model.falseLiteral()).onlyEnforceIf(bZ);
                
                model.addEquality(byG, model.falseLiteral()).onlyEnforceIf(bX);
                model.addEquality(byL, model.falseLiteral()).onlyEnforceIf(bX);
                model.addEquality(byM, model.falseLiteral()).onlyEnforceIf(bX);
                model.addEquality(byG, model.falseLiteral()).onlyEnforceIf(bZ);
                model.addEquality(byL, model.falseLiteral()).onlyEnforceIf(bZ);
                model.addEquality(byM, model.falseLiteral()).onlyEnforceIf(bZ);
                
                // model.addEquality(bzG, model.falseLiteral()).onlyEnforceIf(bX);
                // model.addEquality(bzL, model.falseLiteral()).onlyEnforceIf(bX);
                // model.addEquality(bzM, model.falseLiteral()).onlyEnforceIf(bX);
                // model.addEquality(bzG, model.falseLiteral()).onlyEnforceIf(bY);
                // model.addEquality(bzL, model.falseLiteral()).onlyEnforceIf(bY);
                // model.addEquality(bzM, model.falseLiteral()).onlyEnforceIf(bY);
                
                
                // model.addEquality(bX, model.trueLiteral()).onlyEnforceIf(new Literal[] {bxG.not(), bxL.not(), bxM});
                // model.addEquality(_bX, model.trueLiteral()).onlyEnforceIf(new Literal[] {bxG.not(), bxL.not(), bxM});

                // model.addEquality(bY, model.trueLiteral()).onlyEnforceIf(new Literal[] {byG.not(), byL.not(), byM});
                // model.addEquality(_bY, model.trueLiteral()).onlyEnforceIf(new Literal[] {byG.not(), byL.not(), byM});

                // model.addEquality(bZ, model.trueLiteral()).onlyEnforceIf(new Literal[] {bzG.not(), bzL.not(), bzM});
                // model.addEquality(_bZ, model.trueLiteral()).onlyEnforceIf(new Literal[] {bzG.not(), bzL.not(), bzM});





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
            System.out.println(" x2::" + solver.value(intVarItems.get(i)[0]));
            System.out.print("y1::" + solver.value(oIntVarItems.get(i)[1]));
            System.out.println(" y2::" + solver.value(intVarItems.get(i)[1]));
            System.out.print("z1::" + solver.value(oIntVarItems.get(i)[2]));
            System.out.println(" z2::" + solver.value(intVarItems.get(i)[2]));
            System.out.println("=====================================");
            // System.out.println("bX" + solver.booleanValue(bX));
        }

        for(var _b : b) {
            System.out.print("X:" + solver.booleanValue(_b[0]));
            System.out.print("Y:" + solver.booleanValue(_b[1]));
            System.out.println("Z:" + solver.booleanValue(_b[2]));
            System.out.println("========================");
        }

        System.out.println("================GLM=====================");

        for(var glm : bGLM) {
            System.out.print("G:" + solver.booleanValue(glm[0]));
            System.out.print("L:" + solver.booleanValue(glm[1]));
            System.out.println("M:" + solver.booleanValue(glm[2]));
            System.out.println("=========================");
            System.out.println();
        }
    }

}