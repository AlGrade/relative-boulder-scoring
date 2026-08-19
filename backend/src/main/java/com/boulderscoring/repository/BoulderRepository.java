package com.boulderscoring.repository;

import java.util.List;
import java.util.Optional;

import com.boulderscoring.model.Boulder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoulderRepository extends JpaRepository<Boulder, Long> {

	List<Boulder> findAllByOrderByNumberAsc();

	Optional<Boulder> findByNumber(int number);
}
