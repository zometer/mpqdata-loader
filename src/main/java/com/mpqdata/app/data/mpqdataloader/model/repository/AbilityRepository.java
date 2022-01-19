package com.mpqdata.app.data.mpqdataloader.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpqdata.app.data.mpqdataloader.model.domain.Ability;

@Repository
public interface AbilityRepository extends JpaRepository<Ability, String>{

	public void deleteByMpqCharacterId(String mpqCharacterId);

}
