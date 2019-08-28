package hrds.entity;
/**Auto Created by VBScript Do not modify!*/
import fd.ng.db.entity.TableEntity;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;
import hrds.exception.BusinessException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * StreamingPro作业输入信息表
 */
@Table(tableName = "sdm_job_input")
public class Sdm_job_input extends TableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_job_input";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** StreamingPro作业输入信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sdm_info_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	private Long sdm_info_id; //作业输入信息表id
	private String input_type; //输入类型
	private String input_en_name; //输入英文名称
	private String input_cn_name; //输入中文名称
	private String input_table_name; //输出表名
	private String input_source; //数据来源
	private String input_data_type; //数据模式
	private Long ssj_job_id; //作业id
	private Long input_number; //序号

	/** 取得：作业输入信息表id */
	public Long getSdm_info_id(){
		return sdm_info_id;
	}
	/** 设置：作业输入信息表id */
	public void setSdm_info_id(Long sdm_info_id){
		this.sdm_info_id=sdm_info_id;
	}
	/** 设置：作业输入信息表id */
	public void setSdm_info_id(String sdm_info_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_info_id)){
			this.sdm_info_id=new Long(sdm_info_id);
		}
	}
	/** 取得：输入类型 */
	public String getInput_type(){
		return input_type;
	}
	/** 设置：输入类型 */
	public void setInput_type(String input_type){
		this.input_type=input_type;
	}
	/** 取得：输入英文名称 */
	public String getInput_en_name(){
		return input_en_name;
	}
	/** 设置：输入英文名称 */
	public void setInput_en_name(String input_en_name){
		this.input_en_name=input_en_name;
	}
	/** 取得：输入中文名称 */
	public String getInput_cn_name(){
		return input_cn_name;
	}
	/** 设置：输入中文名称 */
	public void setInput_cn_name(String input_cn_name){
		this.input_cn_name=input_cn_name;
	}
	/** 取得：输出表名 */
	public String getInput_table_name(){
		return input_table_name;
	}
	/** 设置：输出表名 */
	public void setInput_table_name(String input_table_name){
		this.input_table_name=input_table_name;
	}
	/** 取得：数据来源 */
	public String getInput_source(){
		return input_source;
	}
	/** 设置：数据来源 */
	public void setInput_source(String input_source){
		this.input_source=input_source;
	}
	/** 取得：数据模式 */
	public String getInput_data_type(){
		return input_data_type;
	}
	/** 设置：数据模式 */
	public void setInput_data_type(String input_data_type){
		this.input_data_type=input_data_type;
	}
	/** 取得：作业id */
	public Long getSsj_job_id(){
		return ssj_job_id;
	}
	/** 设置：作业id */
	public void setSsj_job_id(Long ssj_job_id){
		this.ssj_job_id=ssj_job_id;
	}
	/** 设置：作业id */
	public void setSsj_job_id(String ssj_job_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(ssj_job_id)){
			this.ssj_job_id=new Long(ssj_job_id);
		}
	}
	/** 取得：序号 */
	public Long getInput_number(){
		return input_number;
	}
	/** 设置：序号 */
	public void setInput_number(Long input_number){
		this.input_number=input_number;
	}
	/** 设置：序号 */
	public void setInput_number(String input_number){
		if(!fd.ng.core.utils.StringUtil.isEmpty(input_number)){
			this.input_number=new Long(input_number);
		}
	}
}
