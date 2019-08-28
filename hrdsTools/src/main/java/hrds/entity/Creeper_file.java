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
 * 爬虫文件源设置
 */
@Table(tableName = "creeper_file")
public class Creeper_file extends TableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "creeper_file";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 爬虫文件源设置 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("file_source_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	private Long file_source_id; //文件源ID
	private String file_source_path; //文件源路径
	private String is_pdf; //PDF文件
	private String is_office; //office文件
	private String is_text; //文本文件
	private String is_video; //视频文件
	private String is_audio; //音频文件
	private String is_image; //图片文件
	private String is_other; //其他
	private String file_remark; //备注
	private Long cs_id; //爬虫数据源id

	/** 取得：文件源ID */
	public Long getFile_source_id(){
		return file_source_id;
	}
	/** 设置：文件源ID */
	public void setFile_source_id(Long file_source_id){
		this.file_source_id=file_source_id;
	}
	/** 设置：文件源ID */
	public void setFile_source_id(String file_source_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(file_source_id)){
			this.file_source_id=new Long(file_source_id);
		}
	}
	/** 取得：文件源路径 */
	public String getFile_source_path(){
		return file_source_path;
	}
	/** 设置：文件源路径 */
	public void setFile_source_path(String file_source_path){
		this.file_source_path=file_source_path;
	}
	/** 取得：PDF文件 */
	public String getIs_pdf(){
		return is_pdf;
	}
	/** 设置：PDF文件 */
	public void setIs_pdf(String is_pdf){
		this.is_pdf=is_pdf;
	}
	/** 取得：office文件 */
	public String getIs_office(){
		return is_office;
	}
	/** 设置：office文件 */
	public void setIs_office(String is_office){
		this.is_office=is_office;
	}
	/** 取得：文本文件 */
	public String getIs_text(){
		return is_text;
	}
	/** 设置：文本文件 */
	public void setIs_text(String is_text){
		this.is_text=is_text;
	}
	/** 取得：视频文件 */
	public String getIs_video(){
		return is_video;
	}
	/** 设置：视频文件 */
	public void setIs_video(String is_video){
		this.is_video=is_video;
	}
	/** 取得：音频文件 */
	public String getIs_audio(){
		return is_audio;
	}
	/** 设置：音频文件 */
	public void setIs_audio(String is_audio){
		this.is_audio=is_audio;
	}
	/** 取得：图片文件 */
	public String getIs_image(){
		return is_image;
	}
	/** 设置：图片文件 */
	public void setIs_image(String is_image){
		this.is_image=is_image;
	}
	/** 取得：其他 */
	public String getIs_other(){
		return is_other;
	}
	/** 设置：其他 */
	public void setIs_other(String is_other){
		this.is_other=is_other;
	}
	/** 取得：备注 */
	public String getFile_remark(){
		return file_remark;
	}
	/** 设置：备注 */
	public void setFile_remark(String file_remark){
		this.file_remark=file_remark;
	}
	/** 取得：爬虫数据源id */
	public Long getCs_id(){
		return cs_id;
	}
	/** 设置：爬虫数据源id */
	public void setCs_id(Long cs_id){
		this.cs_id=cs_id;
	}
	/** 设置：爬虫数据源id */
	public void setCs_id(String cs_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(cs_id)){
			this.cs_id=new Long(cs_id);
		}
	}
}
