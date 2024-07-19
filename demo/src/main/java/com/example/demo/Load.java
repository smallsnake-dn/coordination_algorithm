package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.example.demo.model.EmployeeSkill;
import com.example.demo.model.RouteSkill;
import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;

public class Load {

    
    

    
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data

        int skill = 3;
        int emp = 2;
        // int[][] empSkill = {
        //     {0,1,1,1},
        //     {1,0,0,1},
        //     {0,0,0,0},
        //     {0,0,0,1},
        //     // {1,1,0,1},
        //     // {1,0,0,1},
        //     // {0,1,1,0}
        // };
        int[][] empSkill = {
            {1,1,1},
            {0,0,1},
            // {1,0,0},
            // {0,0,1},
            // {1,1,0},
            // {0,1,0},
            // {1,1,1},
            // {0,0,0},
            // {1,1,1},
            // {1,0,1}
        };

        int[] skillRequire = {1,0,0};
        int empRequire = 2;

        
        

        // Model
        CpModel model = new CpModel();


        // Variables
        Literal[][] x = new Literal[emp][skill + 1];
        for (int e = 0; e < emp; e++) {
            for (int sk = 0; sk < skill + 1; sk++) {
                x[e][sk] = model.newBoolVar("x[" + sk + "," + e + "]");
            }
        }
        
        for (int e = 0; e < emp; e++) {
            for (int sk = 0; sk < skill; sk++) {
                if(empSkill[e][sk] == 0) {
                    model.addEquality(x[e][sk], model.falseLiteral());
                }
            }
        }

        

        IntVar coeff = model.newIntVar(0, 5000, "");
        IntVar[] _skillRequire = new IntVar[skill];

        for(int sk = 0; sk < skill; sk++) {
            _skillRequire[sk] = model.newConstant(skillRequire[sk]);
        }

        IntVar[] coefRequire = new IntVar[skill];
        for(int sk = 0; sk < skill; sk++) {
            coefRequire[sk] = model.newIntVar(0, 5000, "");
        }

        for(int sk = 0; sk < skill; sk++) {
            LinearExprBuilder lst = LinearExpr.newBuilder();
            for(int e = 0; e < emp; e++) {
                lst.add(x[e][sk]);
            }
            model.addMultiplicationEquality(coefRequire[sk], coeff, _skillRequire[sk]);
            model.addGreaterOrEqual(lst, coefRequire[sk]);
        }

        for(int e = 0; e < emp; e++) {
            Literal c = model.newBoolVar("");
            model.addEquality(c, model.trueLiteral()).onlyEnforceIf(x[e][skill].not());
            for(int sk = 0; sk < skill; sk++) {
                // if(empSkill[e][sk] == 1) {
                    model.addEquality(x[e][sk], model.falseLiteral()).onlyEnforceIf(c);
                // }
            }
        }

            
        for (int e = 0; e < emp; e++) {
            Literal[] temp = new Literal[skill];
            for (int sk = 0; sk < skill; sk++) {
                temp[sk] = x[e][sk];
            }
            model.addBoolOr(temp).onlyEnforceIf(x[e][skill]);
        }

        LinearExprBuilder numOfEmp = LinearExpr.newBuilder();
        for(int e = 0; e < emp; e++) {
            numOfEmp.add(x[e][skill]);
        }
        IntVar numEmpRequire = model.newIntVar(0, 5000, "");
        IntVar _empRequire = model.newConstant(empRequire);
        model.addMultiplicationEquality(numEmpRequire, coeff, _empRequire);
        model.addEquality(numOfEmp, numEmpRequire);
        
        LinearExprBuilder optimizeLst = LinearExpr.newBuilder();
        for (int e = 0; e < emp; e++) {
            optimizeLst.addTerm(x[e][skill], 50);
        }
        optimizeLst.add(coeff);   

        
        
        model.maximize(optimizeLst);
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("LOAD::" + solver.value(coeff));
            for(int sk = 0; sk < skill; sk++) {
                System.out.println("SKILL:: " + (sk + 1) + " :: " + solver.value(coefRequire[sk]));
            }
            for(int e = 0; e < emp; e++) {
                for(int sk = 0; sk < skill + 1; sk++) {
                    if(sk < skill){
                        System.out.print(solver.value(x[e][sk]) + " ");}
                    else
                        System.out.print("[" + solver.value(x[e][sk]) + "]");
                }
                System.out.println();
            }

        } else {
            System.err.println("No solution found.");
        }
    }

}
