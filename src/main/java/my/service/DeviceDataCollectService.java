package my.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import my.dao.mysql.entity.DeviceCollectDataEntity;
import my.dao.mysql.repository.DeviceCollectDataRepository;

@Service
public class DeviceDataCollectService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
//	@Autowired
//	private DeviceCollectDataRepository repository;
	
	@Transactional
	public boolean collect(List<DeviceCollectDataEntity> deviceCollectDatas) {
		if (CollectionUtils.isEmpty(deviceCollectDatas)) {
			logger.warn("input parameter deviceCollectDatas is empty.");
			return false;
		}
//		repository.save(deviceCollectDatas);
		return true;
	}

	@Transactional
	public boolean collect(DeviceCollectDataEntity ...deviceCollectDatas) {
		if (ArrayUtils.isEmpty(deviceCollectDatas)) {
			logger.warn("input parameter deviceCollectDatas is empty.");
			return false;
		}
//		repository.save(Arrays.asList(deviceCollectDatas));
		return true;
	}
}
