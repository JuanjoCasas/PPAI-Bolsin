package com.dsi.bolsin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsi.bolsin.model.Sesion;

@Repository
public interface ISesionRepository extends JpaRepository<Sesion, Long> {
}
