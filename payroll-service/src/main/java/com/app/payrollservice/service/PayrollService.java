package com.app.payrollservice.service;

import com.app.payrollservice.model.Payroll;
import com.app.payrollservice.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;

    public Payroll createPayroll(Payroll payroll) {
        BigDecimal netSalary = payroll.getBasicSalary()
                .add(payroll.getBonus())
                .subtract(payroll.getTax());

        payroll.setNetSalary(netSalary);

        return payrollRepository.save(payroll);
    }

    public Payroll getPayrollByEmployeeId(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Payroll not found for employee: " + employeeId));
    }

    public List<Payroll> getAllPayrolls() {
        return payrollRepository.findAll();
    }
}