package com.app.employee_management.controller;

import com.app.employee_management.dto.EmployeeRequest;
import com.app.employee_management.dto.EmployeeResponse;
import com.app.employee_management.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")

public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(){
        return new ResponseEntity<>(employeeService.fetchAllEmployees(), HttpStatus.OK);
//        return ResponseEntity.ok(employeeService.fetchAllEmployees());
    }

    @PostMapping
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeRequest employeeRequest){
        employeeService.addEmployee(employeeRequest);
        return ResponseEntity.ok("Employee added Successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id){
        return employeeService.fetchEmployee(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequest updatedEmployeeRequest){
        boolean updated = employeeService.updateEmployee(id, updatedEmployeeRequest);
        return ResponseEntity.ok("Employee updated Successfully");
    }

}
