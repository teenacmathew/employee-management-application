package com.app.employee_management.dto;

import lombok.Data;

@Data
public class WorkExperienceDTO {

    private String companyName;

    private String designation;

    private Integer yearsOfExperience;

    private String technology;
}