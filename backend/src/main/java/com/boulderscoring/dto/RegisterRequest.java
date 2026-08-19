package com.boulderscoring.dto;

import com.boulderscoring.model.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank @Size(max = 100) String name,
		@NotNull Gender gender,
		@NotBlank @Size(min = 6, max = 100) String password) {
}
