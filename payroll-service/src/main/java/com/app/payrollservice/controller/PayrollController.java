package com.app.payrollservice.controller;

import com.app.payrollservice.model.Payroll;
import com.app.payrollservice.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    public Payroll createPayroll(@RequestBody Payroll payroll) {
        return payrollService.createPayroll(payroll);
    }

    @GetMapping("/{employeeId}")
    public Payroll getPayrollByEmployeeId(@PathVariable Long employeeId) {
        return payrollService.getPayrollByEmployeeId(employeeId);
    }

    @GetMapping
    public List<Payroll> getAllPayrolls() {
        return payrollService.getAllPayrolls();
    }
}