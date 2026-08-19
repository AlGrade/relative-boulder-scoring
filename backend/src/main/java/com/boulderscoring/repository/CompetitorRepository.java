package com.boulderscoring.repository;

import java.util.List;
import java.util.Optional;

import com.boulderscoring.model.Competitor;
import com.boulderscoring.model.Gender;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitorRepository extends JpaRepository<Competitor, Long> {

	Optional<Competitor> findByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCase(String name);

	List<Competitor> findAllByGender(Gender gender);
}
