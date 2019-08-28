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
 * StreamingPro文本文件信息表
 */
@Table(tableName = "sdm_sp_textfile")
public class Sdm_sp_textfile extends TableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_sp_textfile";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** StreamingPro文本文件信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("tsst_extfile_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	private Long tsst_extfile_id; //文本文件信息表id
	private String sst_file_type; //文件格式
	private String sst_file_path; //文件输入输出路径
	private String sst_is_header; //是否有表头
	private String sst_schema; //schema
	private Long sdm_info_id; //作业输入信息表id

	/** 取得：文本文件信息表id */
	public Long getTsst_extfile_id(){
		return tsst_extfile_id;
	}
	/** 设置：文本文件信息表id */
	public void setTsst_extfile_id(Long tsst_extfile_id){
		this.tsst_extfile_id=tsst_extfile_id;
	}
	/** 设置：文本文件信息表id */
	public void setTsst_extfile_id(String tsst_extfile_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(tsst_extfile_id)){
			this.tsst_extfile_id=new Long(tsst_extfile_id);
		}
	}
	/** 取得：文件格式 */
	public String getSst_file_type(){
		return sst_file_type;
	}
	/** 设置：文件格式 */
	public void setSst_file_type(String sst_file_type){
		this.sst_file_type=sst_file_type;
	}
	/** 取得：文件输入输出路径 */
	public String getSst_file_path(){
		return sst_file_path;
	}
	/** 设置：文件输入输出路径 */
	public void setSst_file_path(String sst_file_path){
		this.sst_file_path=sst_file_path;
	}
	/** 取得：是否有表头 */
	public String getSst_is_header(){
		return sst_is_header;
	}
	/** 设置：是否有表头 */
	public void setSst_is_header(String sst_is_header){
		this.sst_is_header=sst_is_header;
	}
	/** 取得：schema */
	public String getSst_schema(){
		return sst_schema;
	}
	/** 设置：schema */
	public void setSst_schema(String sst_schema){
		this.sst_schema=sst_schema;
	}
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
}
