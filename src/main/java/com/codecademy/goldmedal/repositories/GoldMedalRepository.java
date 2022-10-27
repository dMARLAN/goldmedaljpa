package com.codecademy.goldmedal.repositories;

import com.codecademy.goldmedal.model.GoldMedal;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoldMedalRepository extends CrudRepository<GoldMedal, Long> {

    List<GoldMedal> getGoldMedalsByCountryAndSeasonOrderByYearAsc(String countryName, String summer);

    List<GoldMedal> findByCountry(String country, Sort sort);

    Integer countBySeason(String summer);
    Integer countByCountry(String countryName);
    Integer countByGender(String female);
}
