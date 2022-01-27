package com.mpqdata.app.data.mpqdataloader.model.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.mpqdata.app.data.mpqdataloader.model.domain.Ability;

@Repository
public interface AbilityRepository extends JpaRepository<Ability, String>{

	@Modifying
	@Transactional
	public void deleteByMpqCharacterId(String mpqCharacterId);

}
