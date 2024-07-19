package com.example.demo;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;

import java.util.HashMap;
import java.util.Map;

public class Skills {

    static {
        // System.loadLibrary("jniortools"); // Load the OR-Tools library
            Loader.loadNativeLibraries();

    }

    public static void main(String[] args) {
        // Danh sách nhân viên với kỹ năng và level
        Map<String, Map<String, Integer>> employees = new HashMap<>();
        employees.put("Alice", Map.of("skill1", 3, "skill2", 2));
        employees.put("Bob", Map.of("skill1", 2, "skill2", 3));
        employees.put("Charlie", Map.of("skill1", 1, "skill2", 4));
        // Thêm các nhân viên khác

        // Danh sách các tuyến với yêu cầu về kỹ năng, level và số lượng nhân viên cần thiết
        Map<String, Map<String, int[]>> routes = new HashMap<>();
        routes.put("Route1", Map.of(
                "skill1_level1", new int[]{2, 1}, // skill1: level 2, cần 1 nhân viên
                "skill1_level2", new int[]{3, 1}, // skill1: level 3, cần 1 nhân viên
                "skill2", new int[]{3, 1})); // skill2: level 3, cần 1 nhân viên
        routes.put("Route2", Map.of(
                "skill1_level1", new int[]{1, 1}, // skill1: level 1, cần 1 nhân viên
                "skill1_level2", new int[]{2, 1})); // skill1: level 2, cần 1 nhân viên
        // Thêm các tuyến khác

        // Chi phí của mỗi nhân viên khi được gán cho mỗi tuyến
        Map<String, Map<String, Integer>> costs = new HashMap<>();
        costs.put("Alice", Map.of("Route1", 100, "Route2", 200));
        costs.put("Bob", Map.of("Route1", 150, "Route2", 100));
        costs.put("Charlie", Map.of("Route1", 200, "Route2", 300));
        // Thêm các chi phí khác

        // Thiết lập mô hình
        CpModel model = new CpModel();

        // Tạo biến quyết định
        Map<String, Map<String, IntVar>> assignments = new HashMap<>();
        for (String employee : employees.keySet()) {
            assignments.put(employee, new HashMap<>());
            for (String route : routes.keySet()) {
                assignments.get(employee).put(route, model.newBoolVar(employee + "_assigned_to_" + route));
            }
        }

        // Ràng buộc kỹ năng và số lượng nhân viên
        for (String route : routes.keySet()) {
            Map<String, int[]> requirements = routes.get(route);
            for (String skill_level : requirements.keySet()) {
                String[] parts = skill_level.split("_");
                String skill = parts[0];
                int level = Integer.parseInt(parts[1].replace("level", ""));
                int num_needed = requirements.get(skill_level)[1];

                // Danh sách các nhân viên có kỹ năng này và đủ level
                IntVar[] qualifiedAssignments = employees.keySet().stream()
                        .filter(employee -> employees.get(employee).getOrDefault(skill, 0) >= level)
                        .map(employee -> assignments.get(employee).get(route))
                        .toArray(IntVar[]::new);

                // Ràng buộc số lượng nhân viên đủ kỹ năng và level cho mỗi tuyến
                model.addGreaterOrEqual(LinearExpr.sum(qualifiedAssignments), num_needed);
            }
        }

        // Ràng buộc mỗi nhân viên chỉ được gán cho một tuyến
        for (String employee : employees.keySet()) {
            IntVar[] routeAssignments = routes.keySet().stream()
                    .map(route -> assignments.get(employee).get(route))
                    .toArray(IntVar[]::new);
            model.addLessOrEqual(LinearExpr.sum(routeAssignments), 1);
        }

        // Hàm mục tiêu: giảm thiểu tổng chi phí
        LinearExpr totalCost = LinearExpr.sum(
                employees.keySet().stream()
                        .flatMap(employee -> routes.keySet().stream()
                                .filter(route -> costs.containsKey(employee) && costs.get(employee).containsKey(route))
                                .map(route -> LinearExpr.term(assignments.get(employee).get(route), costs.get(employee).get(route))))
                        .toArray(LinearExpr[]::new)
        );
        model.minimize(totalCost);

        // Tạo solver và giải mô hình
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        // In kết quả
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Total cost = " + solver.objectiveValue());
            for (String employee : employees.keySet()) {
                for (String route : routes.keySet()) {
                    // if (solver.booleanValue(assignments.get(employee).get(route))) {
                    //     System.out.println("Employee " + employee + " is assigned to " + route + " with cost " + costs.get(employee).get(route));
                    // }
                }
            }
        } else {
            System.out.println("No solution found.");
        }
    }
}
