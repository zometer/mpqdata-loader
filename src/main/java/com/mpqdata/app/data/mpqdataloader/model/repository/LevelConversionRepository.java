package com.mpqdata.app.data.mpqdataloader.model.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mpqdata.app.data.mpqdataloader.model.domain.LevelConversion;

@Repository
public interface LevelConversionRepository extends JpaRepository<LevelConversion, Integer> {

	@Modifying
	@Transactional
	@Query("delete from LevelConversion l where l.rarity = ?1")
	public void deleteByRarity(int rarity);

}
