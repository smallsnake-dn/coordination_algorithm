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

public class SkillLatest {

    
    

    
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data
        int[][] costs = {
                { 120, 150, 80 },
                { 180, 225, 120 },
                { 156, 195, 104 },
        };


        List<List<RouteSkill>> routeSkills = new ArrayList<>();
        List<RouteSkill> l1 = new ArrayList<>();
        l1.add(new RouteSkill(1, 2, 2));
        l1.add(new RouteSkill(1, 1, 1));
        l1.add(new RouteSkill(2, 2, 1));
        routeSkills.add(l1);
        // List<RouteSkill> l2 = new ArrayList<>();
        // l2.add(new RouteSkill(1, 2, 1));
        // l2.add(new RouteSkill(1, 1, 1));
        // l2.add(new RouteSkill(3, 2, 1));
        // routeSkills.add(l2);
        // List<RouteSkill> l3 = new ArrayList<>();
        // l3.add(new RouteSkill(1, 2, 1));
        // l3.add(new RouteSkill(2, 1, 1));
        // l3.add(new RouteSkill(2, 2, 1));
        // routeSkills.add(l3);

        List<List<EmployeeSkill>> employeeSkills = new ArrayList<>();
        List<EmployeeSkill> es = new ArrayList<>();
        es.add(new EmployeeSkill(1,2));
        es.add(new EmployeeSkill(2,1));
        es.add(new EmployeeSkill(3,0));
        List<EmployeeSkill> es1 = new ArrayList<>();
        // es1.add(new EmployeeSkill(1,1));
        es1.add(new EmployeeSkill(1,2));
        es1.add(new EmployeeSkill(2,2));
        es1.add(new EmployeeSkill(3,2));
        List<EmployeeSkill> es2 = new ArrayList<>();
        es2.add(new EmployeeSkill(1,2));
        es2.add(new EmployeeSkill(2,2));
        // es2.add(new EmployeeSkill(2,1));
        es2.add(new EmployeeSkill(3,0));
        // List<EmployeeSkill> es3 = new ArrayList<>();
        // es3.add(new EmployeeSkill(1,2));
        // es3.add(new EmployeeSkill(2,1));
        // es3.add(new EmployeeSkill(3,0));
        // List<EmployeeSkill> es4 = new ArrayList<>();
        // es4.add(new EmployeeSkill(1,2));
        // es4.add(new EmployeeSkill(2,1));
        // es4.add(new EmployeeSkill(3,0));
        // List<EmployeeSkill> es5 = new ArrayList<>();
        // es5.add(new EmployeeSkill(1,2));
        // es5.add(new EmployeeSkill(2,1));
        // es5.add(new EmployeeSkill(3,0));
        employeeSkills.add(es);
        employeeSkills.add(es1);
        employeeSkills.add(es2);
        // employeeSkills.add(es3);
        // employeeSkills.add(es4);
        // employeeSkills.add(es5);

        int[] limit = new int[]{2,2,2};




        final int numWorkers = 3;
        final int numTasks = 1;
        // final int numWorkers = costs.length;
        // final int numTasks = costs[0].length;

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


            for (int task : allTasks) {
                List<RouteSkill> lrs = routeSkills.get(task);
                for(var i : lrs) {
                    LinearExprBuilder lst = LinearExpr.newBuilder();
                    List<Integer> abc = new ArrayList<>();
                    for(int j = 0; j < numWorkers; j++) {
                        List<EmployeeSkill> lesk = employeeSkills.get(j);
                        for(var g : lesk) {
                            if(i.id == g.id && g.level >= i.level) {
                                lst.addTerm(x[j][task], 1);
                                abc.add(j);
                            }
                        }
                    }
                    System.out.print("Route: " + task + " ");
                    System.out.print(abc);
                    System.out.println(" size:" + abc.size() );
                    System.out.println("Condition: " + abc.size() + " quant=" + i.quantity);
                    System.out.println("============================");
                    model.addGreaterOrEqual(lst, i.quantity);
                }
                // model.addExactlyOne(workers);
            }
        
        
        for(int i = 0; i < numTasks; i++) {
            LinearExprBuilder lst = LinearExpr.newBuilder();
            for(int j = 0; j < numWorkers; j++) {
                lst.add(x[j][i]);
            } 
            model.addEquality(lst, limit[i]);
        }
        

        // // Each task is assigned to exactly one worker.
        // for (int task : allTasks) {
        //     List<Literal> workers = new ArrayList<>();
        //     for (int worker : allWorkers) {
        //         workers.add(x[worker][task]);
        //     }
        //     model.addExactlyOne(workers);
        // }

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
