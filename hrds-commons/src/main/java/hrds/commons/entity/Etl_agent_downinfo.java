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
 * 作业Agent下载信息
 */
@Table(tableName = "etl_agent_downinfo")
public class Etl_agent_downinfo extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "etl_agent_downinfo";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 作业Agent下载信息 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("down_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="down_id",value="下载编号(primary):",dataType = Long.class,required = true)
	private Long down_id;
	@DocBean(name ="agent_name",value="Agent名称:",dataType = String.class,required = true)
	private String agent_name;
	@DocBean(name ="agent_ip",value="Agent IP:",dataType = String.class,required = true)
	private String agent_ip;
	@DocBean(name ="save_dir",value="存放目录:",dataType = String.class,required = true)
	private String save_dir;
	@DocBean(name ="ai_desc",value="描述:",dataType = String.class,required = false)
	private String ai_desc;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="user_id",value="用户id:",dataType = Long.class,required = true)
	private Long user_id;
	@DocBean(name ="user_name",value="用户名:",dataType = String.class,required = false)
	private String user_name;
	@DocBean(name ="passwd",value="密码:",dataType = String.class,required = false)
	private String passwd;
	@DocBean(name ="agent_type",value="agent类别:",dataType = String.class,required = true)
	private String agent_type;

	/** 取得：下载编号(primary) */
	public Long getDown_id(){
		return down_id;
	}
	/** 设置：下载编号(primary) */
	public void setDown_id(Long down_id){
		this.down_id=down_id;
	}
	/** 设置：下载编号(primary) */
	public void setDown_id(String down_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(down_id)){
			this.down_id=new Long(down_id);
		}
	}
	/** 取得：Agent名称 */
	public String getAgent_name(){
		return agent_name;
	}
	/** 设置：Agent名称 */
	public void setAgent_name(String agent_name){
		this.agent_name=agent_name;
	}
	/** 取得：Agent IP */
	public String getAgent_ip(){
		return agent_ip;
	}
	/** 设置：Agent IP */
	public void setAgent_ip(String agent_ip){
		this.agent_ip=agent_ip;
	}
	/** 取得：存放目录 */
	public String getSave_dir(){
		return save_dir;
	}
	/** 设置：存放目录 */
	public void setSave_dir(String save_dir){
		this.save_dir=save_dir;
	}
	/** 取得：描述 */
	public String getAi_desc(){
		return ai_desc;
	}
	/** 设置：描述 */
	public void setAi_desc(String ai_desc){
		this.ai_desc=ai_desc;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：用户id */
	public Long getUser_id(){
		return user_id;
	}
	/** 设置：用户id */
	public void setUser_id(Long user_id){
		this.user_id=user_id;
	}
	/** 设置：用户id */
	public void setUser_id(String user_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(user_id)){
			this.user_id=new Long(user_id);
		}
	}
	/** 取得：用户名 */
	public String getUser_name(){
		return user_name;
	}
	/** 设置：用户名 */
	public void setUser_name(String user_name){
		this.user_name=user_name;
	}
	/** 取得：密码 */
	public String getPasswd(){
		return passwd;
	}
	/** 设置：密码 */
	public void setPasswd(String passwd){
		this.passwd=passwd;
	}
	/** 取得：agent类别 */
	public String getAgent_type(){
		return agent_type;
	}
	/** 设置：agent类别 */
	public void setAgent_type(String agent_type){
		this.agent_type=agent_type;
	}
}
