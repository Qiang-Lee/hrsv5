package hrds.entity;

import fd.ng.db.entity.TableEntity;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;
import hrds.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 实体类中所有属性都应定义为对象，不要使用int等主类型，方便对null值的操作
 */
@Table(tableName = "edw_mapping")
public class EdwMapping extends TableEntity {
    private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "edw_mapping";

	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("mapping_job_id");
		__tmpPKS.add("tabname");
		__tmpPKS.add("colname");
		__tmpPKS.add("st_dt");
		__tmpPKS.add("st_time");
		__tmpPKS.add("jobname");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	/**
	 * 检查给定的名字，是否为主键中的字段
	 * @param name String 检验是否为主键的名字
	 * @return
	 */
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); }
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; }

	private String sourecolvalue;
	private String mapping;
	private String end_dt;
	private String st_dt;
	private String st_time;
	private String processtype;
	private String remark;
	private String tabname;
	private String tabalias;
	private String colname;
	private BigDecimal mapping_job_id;
	private String usercode;
	private String souretabname;
	private String jobname;

	public String getSourecolvalue() { return sourecolvalue; }
	public void setSourecolvalue(String sourecolvalue) {
		if(sourecolvalue==null) addNullValueField("sourecolvalue");
		this.sourecolvalue = sourecolvalue;
	}

	public String getMapping() { return mapping; }
	public void setMapping(String mapping) {
		if(mapping==null) addNullValueField("mapping");
		this.mapping = mapping;
	}

	public String getEnd_dt() { return end_dt; }
	public void setEnd_dt(String end_dt) {
		if(end_dt==null) addNullValueField("end_dt");
		this.end_dt = end_dt;
	}

	public String getSt_dt() { return st_dt; }
	public void setSt_dt(String st_dt) {
		if(st_dt==null) throw new BusinessException("Entity : EdwMapping.st_dt must not null!");
		this.st_dt = st_dt;
	}

	public String getSt_time() { return st_time; }
	public void setSt_time(String st_time) {
		if(st_time==null) throw new BusinessException("Entity : EdwMapping.st_time must not null!");
		this.st_time = st_time;
	}

	public String getProcesstype() { return processtype; }
	public void setProcesstype(String processtype) {
		if(processtype==null) addNullValueField("processtype");
		this.processtype = processtype;
	}

	public String getRemark() { return remark; }
	public void setRemark(String remark) {
		if(remark==null) addNullValueField("remark");
		this.remark = remark;
	}

	public String getTabname() { return tabname; }
	public void setTabname(String tabname) {
		if(tabname==null) throw new BusinessException("Entity : EdwMapping.tabname must not null!");
		this.tabname = tabname;
	}

	public String getTabalias() { return tabalias; }
	public void setTabalias(String tabalias) {
		if(tabalias==null) throw new BusinessException("Entity : EdwMapping.tabalias must not null!");
		this.tabalias = tabalias;
	}

	public String getColname() { return colname; }
	public void setColname(String colname) {
		if(colname==null) throw new BusinessException("Entity : EdwMapping.colname must not null!");
		this.colname = colname;
	}

	public BigDecimal getMapping_job_id() { return mapping_job_id; }
	public void setMapping_job_id(BigDecimal mapping_job_id) {
		if(mapping_job_id==null) throw new BusinessException("Entity : EdwMapping.mapping_job_id must not null!");
		this.mapping_job_id = mapping_job_id;
	}

	public String getUsercode() { return usercode; }
	public void setUsercode(String usercode) {
		if(usercode==null) addNullValueField("usercode");
		this.usercode = usercode;
	}

	public String getSouretabname() { return souretabname; }
	public void setSouretabname(String souretabname) {
		if(souretabname==null) throw new BusinessException("Entity : EdwMapping.souretabname must not null!");
		this.souretabname = souretabname;
	}

	public String getJobname() { return jobname; }
	public void setJobname(String jobname) {
		if(jobname==null) throw new BusinessException("Entity : EdwMapping.jobname must not null!");
		this.jobname = jobname;
	}

}