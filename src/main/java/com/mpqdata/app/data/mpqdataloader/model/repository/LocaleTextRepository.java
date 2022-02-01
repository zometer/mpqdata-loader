package com.mpqdata.app.data.mpqdataloader.model.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mpqdata.app.data.mpqdataloader.model.domain.LocaleText;

@Repository
public interface LocaleTextRepository extends JpaRepository<LocaleText, Long>{

	@Query(nativeQuery = true)
	public List<String> fetchAllTextKeys() ;

	@Modifying
	@Transactional
	@Query("delete from LocaleText lt where lt.localeLanguage = ?1")
	public void deleteByLocaleLanguage(String language);

}
