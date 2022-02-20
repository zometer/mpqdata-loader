package com.mpqdata.app.data.mpqdataloader.model.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mpqdata.app.data.mpqdataloader.model.domain.CharacterCover;

@Repository
public interface CharacterCoverRepository extends JpaRepository<CharacterCover, String>{

	public List<CharacterCover> findByCustomCoverIsNull();

	public List<CharacterCover> findByCompleteFalse();

	@Modifying
	@Transactional
	@Query(nativeQuery = true)
	public void insertForNewCharacters();

}
