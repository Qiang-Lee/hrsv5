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
 * 编码信息表
 */
@Table(tableName = "hyren_code_info")
public class Hyren_code_info extends TableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "hyren_code_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 编码信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("code_classify");
		__tmpPKS.add("code_value");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	private String code_classify; //编码分类
	private String code_value; //编码类型值
	private String code_classify_name; //编码分类名称
	private String code_type_name; //编码名称
	private String code_remark; //编码描述

	/** 取得：编码分类 */
	public String getCode_classify(){
		return code_classify;
	}
	/** 设置：编码分类 */
	public void setCode_classify(String code_classify){
		this.code_classify=code_classify;
	}
	/** 取得：编码类型值 */
	public String getCode_value(){
		return code_value;
	}
	/** 设置：编码类型值 */
	public void setCode_value(String code_value){
		this.code_value=code_value;
	}
	/** 取得：编码分类名称 */
	public String getCode_classify_name(){
		return code_classify_name;
	}
	/** 设置：编码分类名称 */
	public void setCode_classify_name(String code_classify_name){
		this.code_classify_name=code_classify_name;
	}
	/** 取得：编码名称 */
	public String getCode_type_name(){
		return code_type_name;
	}
	/** 设置：编码名称 */
	public void setCode_type_name(String code_type_name){
		this.code_type_name=code_type_name;
	}
	/** 取得：编码描述 */
	public String getCode_remark(){
		return code_remark;
	}
	/** 设置：编码描述 */
	public void setCode_remark(String code_remark){
		this.code_remark=code_remark;
	}
}
