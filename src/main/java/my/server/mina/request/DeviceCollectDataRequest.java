package my.server.mina.request;

public class DeviceCollectDataRequest extends BaseRequest {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6858832925860209486L;
	/**
	 * device id.
	 */
	private Integer deviceId;

	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}

}
