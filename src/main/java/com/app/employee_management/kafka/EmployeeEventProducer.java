package com.app.employee_management.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic.employee-events}")
    private String employeeEventsTopic;

    public void publishEmployeeCreatedEvent(Long employeeId) {
        String message = "EMPLOYEE_CREATED:" + employeeId;
        System.out.println("Publishing Kafka event: " + message);
        kafkaTemplate.send(employeeEventsTopic, String.valueOf(employeeId), message);
    }

    public void publishEmployeeUpdatedEvent(Long employeeId) {
        String message = "EMPLOYEE_UPDATED:" + employeeId;
        System.out.println("Publishing Kafka event: " + message);
        kafkaTemplate.send(employeeEventsTopic, String.valueOf(employeeId), message);
    }

    public void publishEmployeeDeletedEvent(Long employeeId) {
        String message = "EMPLOYEE_DELETED:" + employeeId;
        kafkaTemplate.send(employeeEventsTopic, String.valueOf(employeeId), message);
    }
}