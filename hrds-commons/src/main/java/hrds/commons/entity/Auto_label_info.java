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
 * 仪表板标题表
 */
@Table(tableName = "auto_label_info")
public class Auto_label_info extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "auto_label_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 仪表板标题表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("label_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="serial_number",value="序号:",dataType = Integer.class,required = true)
	private Integer serial_number;
	@DocBean(name ="x_axis_coord",value="X轴坐标:",dataType = Integer.class,required = true)
	private Integer x_axis_coord;
	@DocBean(name ="y_axis_coord",value="Y轴坐标:",dataType = Integer.class,required = true)
	private Integer y_axis_coord;
	@DocBean(name ="length",value="标题长度:",dataType = Integer.class,required = true)
	private Integer length;
	@DocBean(name ="width",value="标题宽度:",dataType = Integer.class,required = true)
	private Integer width;
	@DocBean(name ="label_id",value="标题id:",dataType = Long.class,required = true)
	private Long label_id;
	@DocBean(name ="label_content",value="标题文字:",dataType = String.class,required = false)
	private String label_content;
	@DocBean(name ="label_color",value="标题背景颜色:",dataType = String.class,required = false)
	private String label_color;
	@DocBean(name ="label_size",value="标题大小:",dataType = String.class,required = false)
	private String label_size;
	@DocBean(name ="dashboard_id",value="仪表板id:",dataType = Long.class,required = false)
	private Long dashboard_id;

	/** 取得：序号 */
	public Integer getSerial_number(){
		return serial_number;
	}
	/** 设置：序号 */
	public void setSerial_number(Integer serial_number){
		this.serial_number=serial_number;
	}
	/** 设置：序号 */
	public void setSerial_number(String serial_number){
		if(!fd.ng.core.utils.StringUtil.isEmpty(serial_number)){
			this.serial_number=new Integer(serial_number);
		}
	}
	/** 取得：X轴坐标 */
	public Integer getX_axis_coord(){
		return x_axis_coord;
	}
	/** 设置：X轴坐标 */
	public void setX_axis_coord(Integer x_axis_coord){
		this.x_axis_coord=x_axis_coord;
	}
	/** 设置：X轴坐标 */
	public void setX_axis_coord(String x_axis_coord){
		if(!fd.ng.core.utils.StringUtil.isEmpty(x_axis_coord)){
			this.x_axis_coord=new Integer(x_axis_coord);
		}
	}
	/** 取得：Y轴坐标 */
	public Integer getY_axis_coord(){
		return y_axis_coord;
	}
	/** 设置：Y轴坐标 */
	public void setY_axis_coord(Integer y_axis_coord){
		this.y_axis_coord=y_axis_coord;
	}
	/** 设置：Y轴坐标 */
	public void setY_axis_coord(String y_axis_coord){
		if(!fd.ng.core.utils.StringUtil.isEmpty(y_axis_coord)){
			this.y_axis_coord=new Integer(y_axis_coord);
		}
	}
	/** 取得：标题长度 */
	public Integer getLength(){
		return length;
	}
	/** 设置：标题长度 */
	public void setLength(Integer length){
		this.length=length;
	}
	/** 设置：标题长度 */
	public void setLength(String length){
		if(!fd.ng.core.utils.StringUtil.isEmpty(length)){
			this.length=new Integer(length);
		}
	}
	/** 取得：标题宽度 */
	public Integer getWidth(){
		return width;
	}
	/** 设置：标题宽度 */
	public void setWidth(Integer width){
		this.width=width;
	}
	/** 设置：标题宽度 */
	public void setWidth(String width){
		if(!fd.ng.core.utils.StringUtil.isEmpty(width)){
			this.width=new Integer(width);
		}
	}
	/** 取得：标题id */
	public Long getLabel_id(){
		return label_id;
	}
	/** 设置：标题id */
	public void setLabel_id(Long label_id){
		this.label_id=label_id;
	}
	/** 设置：标题id */
	public void setLabel_id(String label_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(label_id)){
			this.label_id=new Long(label_id);
		}
	}
	/** 取得：标题文字 */
	public String getLabel_content(){
		return label_content;
	}
	/** 设置：标题文字 */
	public void setLabel_content(String label_content){
		this.label_content=label_content;
	}
	/** 取得：标题背景颜色 */
	public String getLabel_color(){
		return label_color;
	}
	/** 设置：标题背景颜色 */
	public void setLabel_color(String label_color){
		this.label_color=label_color;
	}
	/** 取得：标题大小 */
	public String getLabel_size(){
		return label_size;
	}
	/** 设置：标题大小 */
	public void setLabel_size(String label_size){
		this.label_size=label_size;
	}
	/** 取得：仪表板id */
	public Long getDashboard_id(){
		return dashboard_id;
	}
	/** 设置：仪表板id */
	public void setDashboard_id(Long dashboard_id){
		this.dashboard_id=dashboard_id;
	}
	/** 设置：仪表板id */
	public void setDashboard_id(String dashboard_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(dashboard_id)){
			this.dashboard_id=new Long(dashboard_id);
		}
	}
}
