package com.banking.notificationservice.client;

import com.banking.notificationservice.dto.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "account-service", url = "${account.service.url}")
public interface AccountServiceClient {

    @GetMapping("/api/v1/accounts/{accountNumber}")
    AccountResponse getAccountNumber(@PathVariable String accountNumber);

}
