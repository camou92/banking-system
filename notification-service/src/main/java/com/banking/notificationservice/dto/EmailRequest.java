package com.banking.notificationservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EmailRequest {

    private String to;

    private String title;

    private String subject;

    private String message;

}