package com.example.demo.service;

import com.example.demo.dto.CustomerRequest;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Audit;
import com.example.demo.model.Customer;
import com.example.demo.repository.AuditRepository;
import com.example.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AuditRepository auditRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, AuditRepository auditRepository) {
        this.customerRepository = customerRepository;
        this.auditRepository = auditRepository;
    }

    public Customer createCustomer(CustomerRequest request) {
        validateCustomerInput(request);

        // Check if email already exists
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Customer with email " + request.getEmail() + " already exists");
        }

        String customerId = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();

        Customer customer = Customer.builder()
                .customerId(customerId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .age(request.getAge())
                .pan(request.getPan())
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();

        customerRepository.save(customer);

        // Create audit record
        Audit audit = Audit.builder()
                .auditId(UUID.randomUUID().toString())
                .entityType("CUSTOMER")
                .entityId(customerId)
                .action("CREATE")
                .details("Customer created: " + customer.getFirstName() + " " + customer.getLastName())
                .timestamp(currentTime)
                .build();
        auditRepository.save(audit);

        return customer;
    }

    public Customer updateCustomer(String customerId, CustomerRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        validateCustomerInput(request);

        // Check if new email is already taken by another customer
        if (!customer.getEmail().equals(request.getEmail()) &&
                customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Customer with email " + request.getEmail() + " already exists");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAge(request.getAge());
        customer.setPan(request.getPan());
        customer.setUpdatedAt(System.currentTimeMillis());

        customerRepository.save(customer);

        // Create audit record
        Audit audit = Audit.builder()
                .auditId(UUID.randomUUID().toString())
                .entityType("CUSTOMER")
                .entityId(customerId)
                .action("UPDATE")
                .details("Customer updated")
                .timestamp(System.currentTimeMillis())
                .build();
        auditRepository.save(audit);

        return customer;
    }

    public Customer getCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    private void validateCustomerInput(CustomerRequest request) {
        if (request.getAge() < 18 || request.getAge() > 65) {
            throw new BusinessException("Customer age must be between 18 and 65 years");
        }
    }

}
