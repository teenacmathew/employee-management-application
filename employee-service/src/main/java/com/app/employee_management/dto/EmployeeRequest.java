package com.app.employee_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private AddressDTO addressDTO;

    private List<WorkExperienceDTO> workExperiences;
}
