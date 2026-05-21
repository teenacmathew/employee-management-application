# Employee Management API Documentation
This document outlines the available REST API endpoints for the Employee Management application.
**Base URL**: `/api/employees`

---
## 1. Get All Employees
Retrieves a list of all employees in the system.
- **URL**: `/api/employees`
- **Method**: `GET`
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Array of `EmployeeResponse` objects.
---
## 2. Create an Employee
Adds a new employee to the system.
- **URL**: `/api/employees`
- **Method**: `POST`
- **Headers**: `Content-Type: application/json`
- **Request Body** (`EmployeeRequest`):
```json
{
  "firstName": "Teena",
  "lastName": "Mathew",
  "email": "teena@example.com",
  "phone": "9876543210",
  "role": "LEAD",
  "addressDTO": {
    "street": "King Street",
    "city": "Waterloo",
    "state": "Ontario",
    "country": "Canada",
    "zipcode": "N2L5Z5"
  },
  "workExperiences": [
    {
      "companyName": "TCS",
      "designation": "Java Developer",
      "yearsOfExperience": 15,
      "technology": "Spring Boot"
    },
    {
      "companyName": "Majorel",
      "designation": "Content Review Specialist",
      "yearsOfExperience": 16,
      "technology": "Content Moderation"
    }
  ]
}
```
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: `"Employee added Successfully"`
---
## 3. Get Employee by ID
Retrieves the details of a specific employee by their unique ID.
- **URL**: `/api/employees/{id}`
- **Method**: `GET`
- **URL Params**: 
  - `id`=[Long] (Required)
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: `EmployeeResponse` object
- **Error Response**:
  - **Code**: `404 Not Found` (If employee ID does not exist)
---
## 4. Update an Employee
Updates the details of an existing employee.
- **URL**: `/api/employees/{id}`
- **Method**: `PUT`
- **URL Params**: 
  - `id`=[Long] (Required)
- **Headers**: `Content-Type: application/json`
- **Request Body** (`EmployeeRequest`):
*(Same structure as the Create Employee request body)*
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: `"Employee updated Successfully"`
---
## 5. Get Employees Grouped By Skill
Returns a map of employees categorized by their technological skills.
- **URL**: `/api/employees/analytics/skills`
- **Method**: `GET`
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: A JSON object where the keys are technology names (e.g., `"Spring Boot"`) and the values are arrays of `EmployeeResponse` objects.
---
## 6. Get Top Experienced Employees
Retrieves the top 3 employees with the highest total years of experience.
- **URL**: `/api/employees/top-experienced`
- **Method**: `GET`
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Array of up to 3 `EmployeeResponse` objects sorted in descending order by experience.
---
## 7. Delete an Employee
Removes an employee from the system.
- **URL**: `/api/employees/{id}`
- **Method**: `DELETE`
- **URL Params**: 
  - `id`=[Long] (Required)
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: `"Employee deleted successfully"`
- **Error Response**:
  - **Code**: `404 Not Found` (If employee ID does not exist)
