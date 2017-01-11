package my.dao.mysql.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import my.dao.mysql.entity.DeviceCollectDataEntity;

public interface DeviceCollectDataRepository extends JpaRepository<DeviceCollectDataEntity, Integer> {

	
}
