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
 * 字体属性信息表
 */
@Table(tableName = "auto_font_info")
public class Auto_font_info extends TableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "auto_font_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 字体属性信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("font_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	private Long font_id; //字体信息id
	private String color; //字体颜色
	private String fontfamily; //字体系列
	private Integer fontsize; //字体大小
	private String font_corr_tname; //字体属性对应的表名
	private Long font_corr_id; //字体属性对应的编号
	private String fontstyle; //字体风格
	private String fontweight; //字体粗细
	private String align; //字体对齐方式
	private String verticalalign; //文字垂直对齐方式
	private Integer lineheight; //行高
	private String backgroundcolor; //文字块背景色
	private String bordercolor; //文字块边框颜色
	private Integer borderwidth; //文字块边框宽度
	private Integer borderradius; //文字块圆角

	/** 取得：字体信息id */
	public Long getFont_id(){
		return font_id;
	}
	/** 设置：字体信息id */
	public void setFont_id(Long font_id){
		this.font_id=font_id;
	}
	/** 设置：字体信息id */
	public void setFont_id(String font_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(font_id)){
			this.font_id=new Long(font_id);
		}
	}
	/** 取得：字体颜色 */
	public String getColor(){
		return color;
	}
	/** 设置：字体颜色 */
	public void setColor(String color){
		this.color=color;
	}
	/** 取得：字体系列 */
	public String getFontfamily(){
		return fontfamily;
	}
	/** 设置：字体系列 */
	public void setFontfamily(String fontfamily){
		this.fontfamily=fontfamily;
	}
	/** 取得：字体大小 */
	public Integer getFontsize(){
		return fontsize;
	}
	/** 设置：字体大小 */
	public void setFontsize(Integer fontsize){
		this.fontsize=fontsize;
	}
	/** 设置：字体大小 */
	public void setFontsize(String fontsize){
		if(!fd.ng.core.utils.StringUtil.isEmpty(fontsize)){
			this.fontsize=new Integer(fontsize);
		}
	}
	/** 取得：字体属性对应的表名 */
	public String getFont_corr_tname(){
		return font_corr_tname;
	}
	/** 设置：字体属性对应的表名 */
	public void setFont_corr_tname(String font_corr_tname){
		this.font_corr_tname=font_corr_tname;
	}
	/** 取得：字体属性对应的编号 */
	public Long getFont_corr_id(){
		return font_corr_id;
	}
	/** 设置：字体属性对应的编号 */
	public void setFont_corr_id(Long font_corr_id){
		this.font_corr_id=font_corr_id;
	}
	/** 设置：字体属性对应的编号 */
	public void setFont_corr_id(String font_corr_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(font_corr_id)){
			this.font_corr_id=new Long(font_corr_id);
		}
	}
	/** 取得：字体风格 */
	public String getFontstyle(){
		return fontstyle;
	}
	/** 设置：字体风格 */
	public void setFontstyle(String fontstyle){
		this.fontstyle=fontstyle;
	}
	/** 取得：字体粗细 */
	public String getFontweight(){
		return fontweight;
	}
	/** 设置：字体粗细 */
	public void setFontweight(String fontweight){
		this.fontweight=fontweight;
	}
	/** 取得：字体对齐方式 */
	public String getAlign(){
		return align;
	}
	/** 设置：字体对齐方式 */
	public void setAlign(String align){
		this.align=align;
	}
	/** 取得：文字垂直对齐方式 */
	public String getVerticalalign(){
		return verticalalign;
	}
	/** 设置：文字垂直对齐方式 */
	public void setVerticalalign(String verticalalign){
		this.verticalalign=verticalalign;
	}
	/** 取得：行高 */
	public Integer getLineheight(){
		return lineheight;
	}
	/** 设置：行高 */
	public void setLineheight(Integer lineheight){
		this.lineheight=lineheight;
	}
	/** 设置：行高 */
	public void setLineheight(String lineheight){
		if(!fd.ng.core.utils.StringUtil.isEmpty(lineheight)){
			this.lineheight=new Integer(lineheight);
		}
	}
	/** 取得：文字块背景色 */
	public String getBackgroundcolor(){
		return backgroundcolor;
	}
	/** 设置：文字块背景色 */
	public void setBackgroundcolor(String backgroundcolor){
		this.backgroundcolor=backgroundcolor;
	}
	/** 取得：文字块边框颜色 */
	public String getBordercolor(){
		return bordercolor;
	}
	/** 设置：文字块边框颜色 */
	public void setBordercolor(String bordercolor){
		this.bordercolor=bordercolor;
	}
	/** 取得：文字块边框宽度 */
	public Integer getBorderwidth(){
		return borderwidth;
	}
	/** 设置：文字块边框宽度 */
	public void setBorderwidth(Integer borderwidth){
		this.borderwidth=borderwidth;
	}
	/** 设置：文字块边框宽度 */
	public void setBorderwidth(String borderwidth){
		if(!fd.ng.core.utils.StringUtil.isEmpty(borderwidth)){
			this.borderwidth=new Integer(borderwidth);
		}
	}
	/** 取得：文字块圆角 */
	public Integer getBorderradius(){
		return borderradius;
	}
	/** 设置：文字块圆角 */
	public void setBorderradius(Integer borderradius){
		this.borderradius=borderradius;
	}
	/** 设置：文字块圆角 */
	public void setBorderradius(String borderradius){
		if(!fd.ng.core.utils.StringUtil.isEmpty(borderradius)){
			this.borderradius=new Integer(borderradius);
		}
	}
}
