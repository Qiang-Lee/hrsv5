package hrds.g.biz.bean;

import fd.ng.core.annotation.DocBean;
import fd.ng.core.annotation.DocClass;
import fd.ng.db.entity.anno.Table;
import hrds.commons.entity.fdentity.ProjectTableEntity;

@DocClass(desc = "rowkey查询参数实体", author = "dhw", createdate = "2020/4/1 15:36")
@Table(tableName = "row_key_search")
public class RowKeySearch extends ProjectTableEntity {

	private static final long serialVersionUID = 321566870187324L;

	public static final String TableName = "row_key_search";

	@DocBean(name = "rowkey", value = "rowkey:", dataType = String.class, required = true)
	private String rowkey;
	@DocBean(name = "en_table", value = "表英文名:", dataType = String.class, required = true)
	private String en_table;
	@DocBean(name = "en_column", value = "列英文名:", dataType = String.class, required = false)
	private String en_column;
	@DocBean(name = "get_version", value = "数据版本号:", dataType = String.class, required = false)
	private String get_version;
	@DocBean(name = "dataType", value = "数据类型:(json/csv)只能选择一种", dataType = String.class,
			required = true)
	private String dataType;
	@DocBean(name = "outType", value = "数据输出形式:( stream / file)只能选择一种", dataType = String.class,
			required = true)
	private String outType;
	@DocBean(name = "asynType", value = "异步标识:outType为file时使用", dataType = String.class, required = false)
	private String asynType;
	@DocBean(name = "backurl", value = "回调路径:与参数asynType一起使用(如果asynType为1,则必填回调URL)",
			dataType = String.class, required = false)
	private String backurl;
	@DocBean(name = "filename", value = "文件名:与参数asynType一起使用(如果asynType为2,则必填轮询返回文件名称)",
			dataType = String.class, required = false)
	private String filename;
	@DocBean(name = "filepath", value = "轮询ok文件路径:与参数asynType一起使用(如果asynType为2,则必填轮询返回文件路径)",
			dataType = String.class, required = false)
	private String filepath;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getRowkey() {
		return rowkey;
	}

	public void setRowkey(String rowkey) {
		this.rowkey = rowkey;
	}

	public String getEn_table() {
		return en_table;
	}

	public void setEn_table(String en_table) {
		this.en_table = en_table;
	}

	public String getEn_column() {
		return en_column;
	}

	public void setEn_column(String en_column) {
		this.en_column = en_column;
	}

	public String getGet_version() {
		return get_version;
	}

	public void setGet_version(String get_version) {
		this.get_version = get_version;
	}

	public String getOutType() {
		return outType;
	}

	public void setOutType(String outType) {
		this.outType = outType;
	}

	public String getAsynType() {
		return asynType;
	}

	public void setAsynType(String asynType) {
		this.asynType = asynType;
	}

	public String getBackurl() {
		return backurl;
	}

	public void setBackurl(String backurl) {
		this.backurl = backurl;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
}
