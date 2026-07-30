package com.bank.account.dto.request;

import com.bank.account.entity.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAccountRequestDTO {


    @NotBlank(message = "Account holder name is required")
    private String accountholdername;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phone;

    @NotNull(message = "Account type is required")
    private AccountType acctype;

    @NotNull(message = "Balance is required")
    @Min(value = 0, message = "Balance cannot be negative")
    private BigDecimal balance;
}
