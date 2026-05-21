package com.app.employee_management.service;

import com.app.employee_management.dto.AddressDTO;
import com.app.employee_management.dto.EmployeeRequest;
import com.app.employee_management.dto.EmployeeResponse;
import com.app.employee_management.model.Address;
import com.app.employee_management.model.Employee;
import com.app.employee_management.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

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
        return response;
    }
}
