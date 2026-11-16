package com.dsi.bolsin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsi.bolsin.model.Bolsin;

@Repository
public interface IBolsinRepository extends JpaRepository<Bolsin, Integer> {
}
