package com.app.employee_management.service;

import com.app.employee_management.dto.AddressDTO;
import com.app.employee_management.dto.EmployeeRequest;
import com.app.employee_management.dto.EmployeeResponse;
import com.app.employee_management.dto.WorkExperienceDTO;
import com.app.employee_management.model.Address;
import com.app.employee_management.model.Employee;
import com.app.employee_management.model.WorkExperience;
import com.app.employee_management.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public List<EmployeeResponse> fetchAllEmployees(){
        return employeeRepository.findAll().stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    public void addEmployee(EmployeeRequest employeeRequest){
        Employee employee = new Employee();
        updateEmployeeFromRequest(employee, employeeRequest);
        employeeRepository.save(employee);

    }

    private void updateEmployeeFromRequest(Employee employee, EmployeeRequest employeeRequest) {
        employee.setFirstName(employeeRequest.getFirstName());
        employee.setLastName(employeeRequest.getLastName());
        employee.setEmail(employeeRequest.getEmail());
        employee.setPhone(employeeRequest.getPhone());
        if( employeeRequest.getAddressDTO() != null) {
            Address address = new Address();
            address.setCity(employeeRequest.getAddressDTO().getCity());
            address.setZipcode(employeeRequest.getAddressDTO().getZipcode());
            address.setStreet(employeeRequest.getAddressDTO().getStreet());
            address.setState(employeeRequest.getAddressDTO().getState());
            address.setCountry(employeeRequest.getAddressDTO().getCountry());
            employee.setAddress(address);

            if(employeeRequest.getWorkExperiences() != null){

                List<WorkExperience> workExperienceList =
                        employeeRequest.getWorkExperiences()
                                .stream()
                                .map(workDTO -> {

                                    WorkExperience workExperience = new WorkExperience();

                                    workExperience.setCompanyName(workDTO.getCompanyName());
                                    workExperience.setDesignation(workDTO.getDesignation());
                                    workExperience.setYearsOfExperience(workDTO.getYearsOfExperience());
                                    workExperience.setTechnology(workDTO.getTechnology());

                                    workExperience.setEmployee(employee);

                                    return workExperience;

                                }).toList();

                employee.setWorkExperiences(workExperienceList);
            }
        }
    }

    public Optional<EmployeeResponse> fetchEmployee(Long id){
        return employeeRepository.findById(id)
                .map(this::mapToEmployeeResponse);
    }

    public boolean updateEmployee(Long id, EmployeeRequest updatedEmployeeRequest){
        return employeeRepository.findById(id)
                .map(existingEmployee -> {
                    updateEmployeeFromRequest(existingEmployee, updatedEmployeeRequest);
                    employeeRepository.save(existingEmployee);
                    return true;
                }).orElse(false);
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();

        response.setId(String.valueOf(employee.getId()));
        response.setFirstName(employee.getFirstName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setLastName(employee.getLastName());
        response.setRole(employee.getRole());

        if (employee.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setCity(employee.getAddress().getCity());
            addressDTO.setState(employee.getAddress().getState());
            addressDTO.setCountry(employee.getAddress().getCountry());
            addressDTO.setStreet(employee.getAddress().getStreet());
            addressDTO.setZipcode(employee.getAddress().getZipcode());
            response.setAddressDTO(addressDTO);
        }

        List<WorkExperience> experiences =
                Optional.ofNullable(employee.getWorkExperiences())
                        .orElse(List.of());

        List<WorkExperienceDTO> workDTOList =
                experiences.stream()
                        .map(work -> {
                            WorkExperienceDTO dto = new WorkExperienceDTO();
                            dto.setCompanyName(work.getCompanyName());
                            dto.setDesignation(work.getDesignation());
                            dto.setYearsOfExperience(work.getYearsOfExperience());
                            dto.setTechnology(work.getTechnology());
                            return dto;
                        })
                        .toList();

        response.setWorkExperiences(workDTOList);

        int totalExp =
                experiences.stream()
                        .mapToInt(w -> Optional.ofNullable(w.getYearsOfExperience()).orElse(0))
                        .sum();

        response.setTotalYearsOfExperience(totalExp);

        Set<String> techSet =
                experiences.stream()
                        .map(WorkExperience::getTechnology)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        response.setTechnologies(techSet);

        return response;
    }

    public boolean deleteEmployee(Long id) {
        return employeeRepository.findById(id)
                .map(emp -> {
                    employeeRepository.delete(emp);
                    return true;
                })
                .orElse(false);
    }

    public List<EmployeeResponse> getTopExperiencedEmployees() {
        return fetchAllEmployees()
                .stream()
                .sorted(Comparator.comparing(EmployeeResponse::getTotalYearsOfExperience).reversed())
                .limit(3)
                .toList();
    }

    public Map<String, List<EmployeeResponse>> getEmployeesGroupedBySkill() {
        return fetchAllEmployees()
                .stream()
                .filter(emp -> emp.getTechnologies() != null)
                .flatMap(emp ->
                        emp.getTechnologies()
                                .stream()
                                .map(skill -> Map.entry(skill, emp))
                )
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    public byte[] generateEmployeeReportExcel() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            createAllEmployeesSheet(workbook);
            createTopExperiencedSheet(workbook);
            createSkillsAnalyticsSheet(workbook);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate employee Excel report", e);
        }
    }

    private void createAllEmployeesSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("All Employees");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("First Name");
        header.createCell(2).setCellValue("Last Name");
        header.createCell(3).setCellValue("Email");
        header.createCell(4).setCellValue("Phone");
        header.createCell(5).setCellValue("Role");
        header.createCell(6).setCellValue("Total Experience");
        header.createCell(7).setCellValue("Technologies");

        List<EmployeeResponse> employees = fetchAllEmployees();

        int rowIndex = 1;

        for (EmployeeResponse emp : employees) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(emp.getId());
            row.createCell(1).setCellValue(emp.getFirstName());
            row.createCell(2).setCellValue(emp.getLastName());
            row.createCell(3).setCellValue(emp.getEmail());
            row.createCell(4).setCellValue(emp.getPhone());
            row.createCell(5).setCellValue(emp.getRole() != null ? emp.getRole().name() : "");
            row.createCell(6).setCellValue(emp.getTotalYearsOfExperience() != null ? emp.getTotalYearsOfExperience() : 0);
            row.createCell(7).setCellValue(emp.getTechnologies() != null ? String.join(", ", emp.getTechnologies()) : "");
        }

        autoSizeColumns(sheet, 8);
    }

    private void createTopExperiencedSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Top Experienced");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Email");
        header.createCell(3).setCellValue("Total Experience");
        header.createCell(4).setCellValue("Technologies");

        List<EmployeeResponse> employees = getTopExperiencedEmployees();

        int rowIndex = 1;

        for (EmployeeResponse emp : employees) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(emp.getId());
            row.createCell(1).setCellValue(emp.getFirstName() + " " + emp.getLastName());
            row.createCell(2).setCellValue(emp.getEmail());
            row.createCell(3).setCellValue(emp.getTotalYearsOfExperience() != null ? emp.getTotalYearsOfExperience() : 0);
            row.createCell(4).setCellValue(emp.getTechnologies() != null ? String.join(", ", emp.getTechnologies()) : "");
        }

        autoSizeColumns(sheet, 5);
    }

    private void createSkillsAnalyticsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Skills Analytics");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Technology");
        header.createCell(1).setCellValue("Employee Name");
        header.createCell(2).setCellValue("Email");
        header.createCell(3).setCellValue("Total Experience");

        Map<String, List<EmployeeResponse>> skillMap = getEmployeesGroupedBySkill();

        int rowIndex = 1;

        for (Map.Entry<String, List<EmployeeResponse>> entry : skillMap.entrySet()) {
            String technology = entry.getKey();

            for (EmployeeResponse emp : entry.getValue()) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(technology);
                row.createCell(1).setCellValue(emp.getFirstName() + " " + emp.getLastName());
                row.createCell(2).setCellValue(emp.getEmail());
                row.createCell(3).setCellValue(emp.getTotalYearsOfExperience() != null ? emp.getTotalYearsOfExperience() : 0);
            }
        }

        autoSizeColumns(sheet, 4);
    }

    private void autoSizeColumns(Sheet sheet, int numberOfColumns) {
        for (int i = 0; i < numberOfColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public String uploadEmployeesJsonToS3() {
        try {
            List<EmployeeResponse> employees = fetchAllEmployees();

            String jsonData = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(employees);

            String fileName = "employee-reports/employees-"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                    + ".json";

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromString(jsonData, StandardCharsets.UTF_8)
            );

            return fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload employee JSON to S3", e);
        }
    }
}
