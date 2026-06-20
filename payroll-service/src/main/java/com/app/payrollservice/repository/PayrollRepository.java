package com.app.payrollservice.repository;

import com.app.payrollservice.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByEmployeeId(Long employeeId);
}