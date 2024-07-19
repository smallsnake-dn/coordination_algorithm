package com.example.demo;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.Literal;

public class Main {
    static {
        System.loadLibrary("jniortools");
    }

    public static void main(String[] args) {
        // Tạo mô hình
        CpModel model = new CpModel();

        // Tạo biến
        IntVar x = model.newIntVar(0, 10, "x");
        IntVar y = model.newIntVar(0, 10, "y");

        // Thêm constraint
        model.addEquality(LinearExpr.sum(new IntVar[]{x, y}), 10);

        // Áp dụng constraint `x + y == 10` chỉ khi `x` là số chẵn
        Literal isEven = model.newBoolVar("isEven");
        // model.addModuloEquality(isEven, x, 2, 0);

        // Tạo solver
        CpSolver solver = new CpSolver();


        // System.out.println(1/0);
        // Giải quyết mô hình
        CpSolverStatus status = solver.solve(model);

        // Kiểm tra trạng thái giải
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Giải pháp tối ưu hoặc khả thi:");
            System.out.println("x = " + solver.value(x));
            System.out.println("y = " + solver.value(y));
            System.out.println("isEven = " + solver.value(isEven));
        } else {
            System.out.println("Không tìm thấy giải pháp.");
        }
    }
}

