package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Integer age;
    private String pan;
    private Long createdAt;
    private Long updatedAt;
}
