package com.app.employee_management.kafka;

import com.app.employee_management.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeEventConsumer {

    private final EmployeeService employeeService;

    @KafkaListener(
            topics = "${app.kafka.topic.employee-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeEmployeeEvent(String message) {

        System.out.println("Kafka event received: " + message);

        // Whenever DB changes, regenerate latest employee JSON and upload to S3
        String s3Url = employeeService.uploadEmployeesJsonToS3();

        System.out.println("Updated employee JSON uploaded to S3: " + s3Url);
    }
}