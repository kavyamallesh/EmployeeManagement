package com.department.hr.employeeManagement.repository;

import com.department.hr.employeeManagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Page<Employee> findBySalaryGreaterThanEqualAndSalaryLessThan(Double minSalary, Double maxSalary, Pageable pageable);
}
