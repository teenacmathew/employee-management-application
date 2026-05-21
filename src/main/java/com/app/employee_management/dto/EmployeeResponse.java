package com.app.employee_management.dto;

import com.app.employee_management.model.EmployeeRole;
import lombok.Data;

import java.util.List;

@Data
public class EmployeeResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private EmployeeRole role;

    private AddressDTO addressDTO;

    private List<WorkExperienceDTO> workExperiences;
}
