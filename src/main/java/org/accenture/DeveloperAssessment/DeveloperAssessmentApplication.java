package org.accenture.DeveloperAssessment;

import java.util.Arrays;
import java.util.HashMap;
import org.accenture.DeveloperAssessment.domain.country.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DeveloperAssessmentApplication {
  private static final String URL = "https://restcountries.com/v3.1/all";
  private static final Logger log = LoggerFactory.getLogger(DeveloperAssessmentApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(DeveloperAssessmentApplication.class, args);
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  /*
   * Count how many given countries are not in Asia
   *
   * @param countryCodes CCA3 country codes
   * @param indexedCountries map of countries, indexed by CCA3 country code
   * @return how many countries that are not in Asia
   */
  private long countCountriesNotInAsia(
      String[] countryCodes, HashMap<String, Country> indexedCountries) {
    return Arrays.stream(countryCodes)
        .filter(countryCode -> !indexedCountries.get(countryCode).region().equals("Asia"))
        .count();
  }

  @Bean
  public CommandLineRunner run(RestTemplate restTemplate) {
    return args -> {
      Country[] countries = restTemplate.getForObject(URL, Country[].class);

      if (countries == null) throw new IllegalStateException();

      // country borders are saved as CCA3, so index by that
      HashMap<String, Country> indexed =
          new HashMap<>() {
            {
              for (Country country : countries) {
                put(country.cca3(), country);
              }
            }
          };

      // get country in asia with most bordering countries not in asia
      Country bordering =
          Arrays.stream(countries)
              .filter(country -> country.region().equals("Asia"))
              .max(
                  (o1, o2) -> {
                    // some countries don't have any borders specified,
                    // so use empty array as default
                    long firstCount =
                        countCountriesNotInAsia(o1.borders().orElse(new String[] {}), indexed);
                    long secondCount =
                        countCountriesNotInAsia(o2.borders().orElse(new String[] {}), indexed);

                    return Math.toIntExact(firstCount - secondCount);
                  })
              .orElseThrow();

      log.info("%s has most bordering countries".formatted(bordering.name().common()));

      // print population densities in descending order
     Arrays.stream(countries)
          .sorted((o1, o2) -> Math.toIntExact(o2.population() - o1.population()))
          .forEach(
              country ->
                  log.info(
                      "%s has a population of %s"
                          .formatted(country.name().common(), country.population())));
    };
  }
}
