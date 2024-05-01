package org.accenture.DeveloperAssessment.domain.country;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Country(
    Name name, Long population, String region, Optional<String[]> borders, String cca3) {}
