package my.dao.h2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import my.dao.h2.entity.District;

/**
 * District Repository
 * @author xnat
 *
 */
public interface DistrictRepository extends Repository<District, Integer>{
	@Query("from District where level = 1")
	List<District> getAllLevelOne();
}
