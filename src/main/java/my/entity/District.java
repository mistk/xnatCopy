package my.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * district entity
 * 
 * @author xnat
 *
 */
@Entity
@Table(name="tb_district")
public class District extends BaseEntity {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	@Id
	private Integer id;
	@Column(name = "pid")
	private Integer pId;
	private Integer level;
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getpId() {
		return pId;
	}

	public void setpId(Integer pId) {
		this.pId = pId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
