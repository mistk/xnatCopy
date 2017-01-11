package my.dao.mysql.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import my.dao.BaseEntity;

@Entity
@Table(name = "tb_device_data")
public class DeviceCollectDataEntity extends BaseEntity {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8892329234462900701L;
	@Id
	@GeneratedValue
	private Integer id;
	@Column(name = "device_id")
	private Integer deviceId;

	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
