package hrds.commons.entity;
/**Auto Created by VBScript Do not modify!*/
import hrds.commons.entity.fdentity.ProjectTableEntity;
import fd.ng.db.entity.anno.Table;
import fd.ng.core.annotation.DocBean;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * 数据抽取定义
 */
@Table(tableName = "data_extraction_def")
public class Data_extraction_def extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "data_extraction_def";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据抽取定义 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("ded_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="ded_id",value="数据抽取定义主键",dataType = Long.class,required = true)
	private Long ded_id;
	@DocBean(name ="table_id",value="表名ID",dataType = Long.class,required = true)
	private Long table_id;
	@DocBean(name ="is_header",value="是否需要表头",dataType = String.class,required = true)
	private String is_header;
	@DocBean(name ="data_extract_type",value="数据抽取方式",dataType = String.class,required = true)
	private String data_extract_type;
	@DocBean(name ="database_code",value="数据抽取落地编码",dataType = String.class,required = true)
	private String database_code;
	@DocBean(name ="row_separator",value="行分隔符",dataType = String.class,required = false)
	private String row_separator;
	@DocBean(name ="database_separatorr",value="列分割符",dataType = String.class,required = false)
	private String database_separatorr;
	@DocBean(name ="is_hidden",value="分隔符是否为ASCII隐藏字符",dataType = String.class,required = true)
	private String is_hidden;
	@DocBean(name ="ded_remark",value="备注",dataType = String.class,required = false)
	private String ded_remark;
	@DocBean(name ="dbfile_format",value="数据落地格式",dataType = String.class,required = true)
	private String dbfile_format;
	@DocBean(name ="plane_url",value="数据落地目录",dataType = String.class,required = false)
	private String plane_url;
	@DocBean(name ="file_suffix",value="落地文件后缀名",dataType = String.class,required = false)
	private String file_suffix;

	/** 取得：数据抽取定义主键 */
	public Long getDed_id(){
		return ded_id;
	}
	/** 设置：数据抽取定义主键 */
	public void setDed_id(Long ded_id){
		this.ded_id=ded_id;
	}
	/** 设置：数据抽取定义主键 */
	public void setDed_id(String ded_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(ded_id)){
			this.ded_id=new Long(ded_id);
		}
	}
	/** 取得：表名ID */
	public Long getTable_id(){
		return table_id;
	}
	/** 设置：表名ID */
	public void setTable_id(Long table_id){
		this.table_id=table_id;
	}
	/** 设置：表名ID */
	public void setTable_id(String table_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(table_id)){
			this.table_id=new Long(table_id);
		}
	}
	/** 取得：是否需要表头 */
	public String getIs_header(){
		return is_header;
	}
	/** 设置：是否需要表头 */
	public void setIs_header(String is_header){
		this.is_header=is_header;
	}
	/** 取得：数据抽取方式 */
	public String getData_extract_type(){
		return data_extract_type;
	}
	/** 设置：数据抽取方式 */
	public void setData_extract_type(String data_extract_type){
		this.data_extract_type=data_extract_type;
	}
	/** 取得：数据抽取落地编码 */
	public String getDatabase_code(){
		return database_code;
	}
	/** 设置：数据抽取落地编码 */
	public void setDatabase_code(String database_code){
		this.database_code=database_code;
	}
	/** 取得：行分隔符 */
	public String getRow_separator(){
		return row_separator;
	}
	/** 设置：行分隔符 */
	public void setRow_separator(String row_separator){
		this.row_separator=row_separator;
	}
	/** 取得：列分割符 */
	public String getDatabase_separatorr(){
		return database_separatorr;
	}
	/** 设置：列分割符 */
	public void setDatabase_separatorr(String database_separatorr){
		this.database_separatorr=database_separatorr;
	}
	/** 取得：分隔符是否为ASCII隐藏字符 */
	public String getIs_hidden(){
		return is_hidden;
	}
	/** 设置：分隔符是否为ASCII隐藏字符 */
	public void setIs_hidden(String is_hidden){
		this.is_hidden=is_hidden;
	}
	/** 取得：备注 */
	public String getDed_remark(){
		return ded_remark;
	}
	/** 设置：备注 */
	public void setDed_remark(String ded_remark){
		this.ded_remark=ded_remark;
	}
	/** 取得：数据落地格式 */
	public String getDbfile_format(){
		return dbfile_format;
	}
	/** 设置：数据落地格式 */
	public void setDbfile_format(String dbfile_format){
		this.dbfile_format=dbfile_format;
	}
	/** 取得：数据落地目录 */
	public String getPlane_url(){
		return plane_url;
	}
	/** 设置：数据落地目录 */
	public void setPlane_url(String plane_url){
		this.plane_url=plane_url;
	}
	/** 取得：落地文件后缀名 */
	public String getFile_suffix(){
		return file_suffix;
	}
	/** 设置：落地文件后缀名 */
	public void setFile_suffix(String file_suffix){
		this.file_suffix=file_suffix;
	}
}