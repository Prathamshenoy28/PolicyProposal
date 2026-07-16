package com.example.demo.service;

import com.example.demo.dto.CustomerRequest;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Customer;
import com.example.demo.repository.AuditRepository;
import com.example.demo.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {
    private CustomerService customerService;
    private CustomerRepository customerRepository;
    private AuditRepository auditRepository;

    @BeforeEach
    void setUp() {
        customerRepository = new CustomerRepository();
        auditRepository = new AuditRepository();
        customerService = new CustomerService(customerRepository, auditRepository);
    }

    @Test
    void testCreateCustomerSuccess() {
        // Arrange
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("9876543210")
                .age(35)
                .pan("ABCDE1234F")
                .build();

        // Act
        Customer response = customerService.createCustomer(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getCustomerId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(35, response.getAge());
    }

    @Test
    void testCustomerAgeValidation_BelowMinimum() {
        // Arrange
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phone("9876543210")
                .age(15) // Below minimum
                .pan("ABCDE1234F")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> customerService.createCustomer(request));
    }

    @Test
    void testCustomerAgeValidation_AboveMaximum() {
        // Arrange
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Bob")
                .lastName("Smith")
                .email("bob@example.com")
                .phone("9876543210")
                .age(70) // Above maximum
                .pan("ABCDE1234F")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> customerService.createCustomer(request));
    }

    @Test
    void testCreateCustomerWithDuplicateEmail() {
        // Arrange
        CustomerRequest request1 = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("9876543210")
                .age(30)
                .pan("ABCDE1234F")
                .build();

        CustomerRequest request2 = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("john@example.com") // Duplicate email
                .phone("9123456789")
                .age(28)
                .pan("XYZAB5678G")
                .build();

        // Act
        customerService.createCustomer(request1);

        // Assert
        assertThrows(BusinessException.class, () -> customerService.createCustomer(request2));
    }

    @Test
    void testUpdateCustomerSuccess() {
        // Arrange
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("9876543210")
                .age(35)
                .pan("ABCDE1234F")
                .build();

        Customer created = customerService.createCustomer(request);
        String customerId = created.getCustomerId();

        CustomerRequest updateRequest = CustomerRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("9123456789")
                .age(36)
                .pan("ABCDE1234F")
                .build();

        // Act
        Customer updated = customerService.updateCustomer(customerId, updateRequest);

        // Assert
        assertEquals("Smith", updated.getLastName());
        assertEquals("john.smith@example.com", updated.getEmail());
        assertEquals(36, updated.getAge());
    }

    @Test
    void testGetNonExistentCustomer() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomer("NONEXISTENT"));
    }

    @Test
    void testGetAllCustomers() {
        // Arrange
        CustomerRequest request1 = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("9876543210")
                .age(30)
                .pan("ABCDE1234F")
                .build();

        CustomerRequest request2 = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("9123456789")
                .age(28)
                .pan("XYZAB5678G")
                .build();

        // Act
        customerService.createCustomer(request1);
        customerService.createCustomer(request2);
        java.util.List<Customer> customers = customerService.getAllCustomers();

        // Assert
        assertEquals(2, customers.size());
    }
}
