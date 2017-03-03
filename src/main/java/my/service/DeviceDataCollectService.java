package my.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import my.dao.mysql.entity.DeviceCollectDataEntity;
import my.spring.BaseBeanService;

@Service
public class DeviceDataCollectService extends BaseBeanService {
//	@Autowired
//	private DeviceCollectDataRepository repository;
	
	@Transactional
	public boolean collect(List<DeviceCollectDataEntity> deviceCollectDatas) {
		if (CollectionUtils.isEmpty(deviceCollectDatas)) {
			logWarn("input parameter deviceCollectDatas is empty.");
			return false;
		}
//		repository.save(deviceCollectDatas);
		return true;
	}

	@Transactional
	public boolean collect(DeviceCollectDataEntity ...deviceCollectDatas) {
		if (ArrayUtils.isEmpty(deviceCollectDatas)) {
		    logWarn("input parameter deviceCollectDatas is empty.");
			return false;
		}
//		repository.save(Arrays.asList(deviceCollectDatas));
		return true;
	}
}
