package com.app.employee_management.controller;

import com.app.employee_management.dto.EmployeeRequest;
import com.app.employee_management.dto.EmployeeResponse;
import com.app.employee_management.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")

public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(){
        return new ResponseEntity<>(employeeService.fetchAllEmployees(), HttpStatus.OK);
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

    @GetMapping("/analytics/skills")
    public ResponseEntity<Map<String, List<EmployeeResponse>>> getEmployeesGroupedBySkill() {

        List<EmployeeResponse> employees = employeeService.fetchAllEmployees();

        Map<String, List<EmployeeResponse>> grouped =
                employees.stream()
                        .filter(emp -> emp.getTechnologies() != null)
                        .flatMap(emp ->
                                emp.getTechnologies().stream()
                                        .map(skill -> Map.entry(skill, emp))
                        )
                        .collect(Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                        ));

        return ResponseEntity.ok(grouped);
    }

    @GetMapping("/top-experienced")
    public ResponseEntity<List<EmployeeResponse>> getTopExperienced() {

        return ResponseEntity.ok(
                employeeService.fetchAllEmployees()
                        .stream()
                        .sorted(Comparator.comparing(EmployeeResponse::getTotalYearsOfExperience)
                                .reversed())
                        .limit(3)
                        .toList()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        boolean deleted = employeeService.deleteEmployee(id);

        if (deleted) {
            return ResponseEntity.ok("Employee deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    api/employees/download-report
    @GetMapping("/download-report")
    public ResponseEntity<byte[]> downloadEmployeeReport() {

        byte[] excelData = employeeService.generateEmployeeReportExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee-report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(excelData);
    }

//    GET /api/employees/upload-json-to-s3
@GetMapping("/upload-json-to-s3")
public ResponseEntity<String> uploadEmployeesJsonToS3() {

//    String s3Key = employeeService.uploadEmployeesJsonToS3();

//    return ResponseEntity.ok("Employee JSON uploaded successfully to S3. File key: " + s3Key);
    String presignedUrl = employeeService.uploadEmployeesJsonToS3();
    return ResponseEntity.ok(presignedUrl);

}


}
