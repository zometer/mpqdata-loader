package com.mpqdata.app.data.mpqdataloader.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mpqdata.app.data.mpqdataloader.model.domain.LocaleText;

@Repository
public interface LocaleTextRepository extends JpaRepository<LocaleText, Long>{

	@Query(nativeQuery = true)
	public List<String> fetchAllTextKeys() ;

}
