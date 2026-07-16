package com.example.demo.repository;

import com.example.demo.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CustomerRepository {
    private final Map<String, Customer> customers = new HashMap<>();

    public Customer save(Customer customer) {
        customers.put(customer.getCustomerId(), customer);
        return customer;
    }

    public Optional<Customer> findById(String customerId) {
        return Optional.ofNullable(customers.get(customerId));
    }

    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    public boolean existsById(String customerId) {
        return customers.containsKey(customerId);
    }

    public Optional<Customer> findByEmail(String email) {
        return customers.values().stream()
                .filter(c -> c.getEmail().equals(email))
                .findFirst();
    }
}
