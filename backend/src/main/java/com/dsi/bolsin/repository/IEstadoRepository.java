package com.dsi.bolsin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsi.bolsin.model.Estado;

@Repository
public interface IEstadoRepository extends JpaRepository<Estado, Integer> {
}
