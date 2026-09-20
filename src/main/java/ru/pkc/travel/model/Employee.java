package ru.pkc.travel.model;

import java.util.Objects;

public class Employee {
    private Long id;
    private String fullName;
    private String email;
    private String department;

    public Employee(Long id, String fullName, String email, String department) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
    }

    public Employee(String fullName, String email, String department) {
        this(null, fullName, email, department);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return String.format("%d | %s | %s | %s", id, fullName, email, department);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return id != null && Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() { return id == null ? 0 : id.hashCode(); }
}
