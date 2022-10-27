package com.codecademy.goldmedal.controller;

import com.codecademy.goldmedal.model.*;
import com.codecademy.goldmedal.repositories.CountryRepository;
import com.codecademy.goldmedal.repositories.GoldMedalRepository;
import org.apache.commons.text.WordUtils;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/countries")
@SuppressWarnings("unused")
public class GoldMedalController {
    private final GoldMedalRepository goldMedalRepository;
    private final CountryRepository countryRepository;

    public GoldMedalController(final GoldMedalRepository goldMedalRepository, CountryRepository countryRepository) {
        this.goldMedalRepository = goldMedalRepository;
        this.countryRepository = countryRepository;
    }

    @GetMapping
    public CountriesResponse getCountries(@RequestParam(name = "sort_by") String sortBy, @RequestParam String ascending) {
        var ascendingOrder = ascending.equalsIgnoreCase("y");
        return new CountriesResponse(getCountrySummaries(sortBy.toLowerCase(), ascendingOrder));
    }

    @GetMapping("/{country}")
    public CountryDetailsResponse getCountryDetails(@PathVariable String country) {
        String countryName = WordUtils.capitalizeFully(country);
        return getCountryDetailsResponse(countryName);
    }

    @GetMapping("/{country}/medals")
    public CountryMedalsListResponse getCountryMedalsList(@PathVariable String country, @RequestParam(name = "sort_by") String sortBy, @RequestParam String ascending) {
        String countryName = WordUtils.capitalizeFully(country);
        var ascendingOrder = ascending.equalsIgnoreCase("y");
        return getCountryMedalsListResponse(countryName, sortBy.toLowerCase(), ascendingOrder);
    }

    private CountryMedalsListResponse getCountryMedalsListResponse(String countryName, String sortBy, boolean ascendingOrder) {
        try {
            if (ascendingOrder){
                return new CountryMedalsListResponse(this.goldMedalRepository.findByCountry(countryName, Sort.by(sortBy).ascending()));
            } else {
                return new CountryMedalsListResponse(this.goldMedalRepository.findByCountry(countryName, Sort.by(sortBy).descending()));
            }
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("The sort_by parameter is not supported.");
        }
    }

    private CountryDetailsResponse getCountryDetailsResponse(String countryName) {
        Optional<Country> countryOptional = this.countryRepository.findByName(countryName);

        if (countryOptional.isEmpty()) {
            return new CountryDetailsResponse(countryName);
        }

        Country country = countryOptional.get();
        Integer goldMedalCount = this.goldMedalRepository.countByCountry(countryName);

        List<GoldMedal> summerWins = this.goldMedalRepository.getGoldMedalsByCountryAndSeasonOrderByYearAsc(countryName, "Summer");
        Integer numberSummerWins = !summerWins.isEmpty() ? summerWins.size() : null;
        Integer totalSummerEvents = this.goldMedalRepository.countBySeason("Summer");
        Float percentageTotalSummerWins = totalSummerEvents != 0 && numberSummerWins != null ? (float) summerWins.size() / totalSummerEvents : null;
        Integer yearFirstSummerWin = !summerWins.isEmpty() ? summerWins.get(0).getYear() : null;

        List<GoldMedal> winterWins = this.goldMedalRepository.getGoldMedalsByCountryAndSeasonOrderByYearAsc(countryName, "Winter");
        Integer numberWinterWins = !winterWins.isEmpty() ? winterWins.size() : null;
        Integer totalWinterEvents = this.goldMedalRepository.countBySeason("Winter");
        Float percentageTotalWinterWins = totalWinterEvents != 0 && numberWinterWins != null ? (float) winterWins.size() / totalWinterEvents : null;
        Integer yearFirstWinterWin = !winterWins.isEmpty() ? winterWins.get(0).getYear() : null;

        Integer numberEventsWonByFemaleAthletes = this.goldMedalRepository.countByGender("female");
        Integer numberEventsWonByMaleAthletes = this.goldMedalRepository.countByGender("male");

        return new CountryDetailsResponse(
                countryName,
                country.getGdp(),
                country.getPopulation(),
                goldMedalCount,
                numberSummerWins,
                percentageTotalSummerWins,
                yearFirstSummerWin,
                numberWinterWins,
                percentageTotalWinterWins,
                yearFirstWinterWin,
                numberEventsWonByFemaleAthletes,
                numberEventsWonByMaleAthletes);
    }

    private List<CountrySummary> getCountrySummaries(String sortBy, boolean ascendingOrder) {
        List<Country> countries;
        try {
            if (ascendingOrder){
                countries = this.countryRepository.findAll(Sort.by(sortBy).ascending());
            } else {
                countries = this.countryRepository.findAll(Sort.by(sortBy).descending());
            }
        } catch (IllegalArgumentException iae) {
            countries = this.countryRepository.findAll();
        }

        List<CountrySummary> countrySummaries = getCountrySummariesWithMedalCount(countries);

        if (sortBy.equalsIgnoreCase("medals")) {
            countrySummaries = sortByMedalCount(countrySummaries, ascendingOrder);
        }

        return countrySummaries;
    }

    private List<CountrySummary> sortByMedalCount(List<CountrySummary> countrySummaries, boolean ascendingOrder) {
        return countrySummaries.stream()
                .sorted((t1, t2) -> ascendingOrder ?
                        t1.getMedals() - t2.getMedals() :
                        t2.getMedals() - t1.getMedals())
                .collect(Collectors.toList());
    }

    private List<CountrySummary> getCountrySummariesWithMedalCount(List<Country> countries) {
        List<CountrySummary> countrySummaries = new ArrayList<>();
        for (Country country : countries) {
            Integer goldMedalCount = this.goldMedalRepository.countByCountry(country.getName());
            countrySummaries.add(new CountrySummary(country, goldMedalCount));
        }
        return countrySummaries;
    }
}
