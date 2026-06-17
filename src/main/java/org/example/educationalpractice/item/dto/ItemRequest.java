package org.example.educationalpractice.item.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record ItemRequest(

        @NotBlank
        @Size(max = 120)
        String title,

        @Size(max = 2000)
        String description,

        @NotNull
        @DecimalMin(value = "0.0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        Set<@NotBlank @Size(max = 40) String> tags
) {
}
