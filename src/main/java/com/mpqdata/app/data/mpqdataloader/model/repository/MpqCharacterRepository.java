package com.mpqdata.app.data.mpqdataloader.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpqdata.app.data.mpqdataloader.model.domain.MpqCharacter;

@Repository
public interface MpqCharacterRepository extends JpaRepository<MpqCharacter, String>{

}
