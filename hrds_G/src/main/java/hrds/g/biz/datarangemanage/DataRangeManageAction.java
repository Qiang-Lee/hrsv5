package hrds.g.biz.datarangemanage;

import com.alibaba.fastjson.TypeReference;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.web.util.Dbo;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Sysreg_parameter_info;
import hrds.commons.entity.Table_column;
import hrds.commons.entity.Table_use_info;
import hrds.commons.exception.BusinessException;
import hrds.commons.tree.background.TreeNodeInfo;
import hrds.commons.tree.background.bean.TreeConf;
import hrds.commons.tree.commons.TreePageSource;
import hrds.commons.utils.DataTableUtil;
import hrds.commons.utils.key.PrimayKeyGener;
import hrds.commons.utils.tree.Node;
import hrds.commons.utils.tree.NodeDataConvertedTreeList;
import hrds.g.biz.bean.TableDataInfo;
import hrds.g.biz.init.InterfaceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

@DocClass(desc = "接口数据范围管理接口", author = "dhw", createdate = "2020/3/25 17:53")
public class DataRangeManageAction extends BaseAction {

	private static final Logger logger = LogManager.getLogger();

	@Method(desc = "查询数据使用范围信息", logicStep = "1.数据可访问权限处理方式：该方法不需要进行访问权限限制" +
			"2.配置树不显示文件采集的数据" +
			"3.根据源菜单信息获取节点数据列表" +
			"4.转换节点数据列表为分叉树列表" +
			"5.定义返回的分叉树结果Map")
	@Return(desc = "返回的分叉树结果Map", range = "无限制")
	public List<Node> searchDataUsageRangeInfoToTreeData() {
		// 1.数据可访问权限处理方式：该方法不需要进行访问权限限制
		TreeConf treeConf = new TreeConf();
		// 2.配置树不显示文件采集的数据
		treeConf.setShowFileCollection(Boolean.FALSE);
		// 3.根据源菜单信息获取节点数据列表
		List<Map<String, Object>> dataList = TreeNodeInfo.getTreeNodeInfo(TreePageSource.INTERFACE, getUser(),
				treeConf);
		return NodeDataConvertedTreeList.dataConversionTreeInfo(dataList);
	}

	@Method(desc = "保存数据表数据", logicStep = "1.数据可访问权限处理方式：该方法不需要进行访问权限限制")
	@Param(name = "tableDataInfos", desc = "表数据信息对象数组", range = "无限制", isBean = true)
	@Param(name = "table_note", desc = "表数据信息对象数组", range = "无限制", nullable = true)
	@Param(name = "data_layer", desc = "数据层", range = "无限制")
	@Param(name = "user_id", desc = "用户ID", range = "无限制")
	public void saveTableData(TableDataInfo[] tableDataInfos, String table_note, String data_layer,
	                          long[] user_id) {
		// 1.数据可访问权限处理方式：该方法不需要进行访问权限限制
		for (TableDataInfo tableDataInfo : tableDataInfos) {
			// 2.根据存储层获取表与字段信息
			Map<String, Object> tableInfoAndColumnInfo =
					DataTableUtil.getTableInfoAndColumnInfo(data_layer, tableDataInfo.getFile_id());
			for (long userId : user_id) {
				Table_use_info table_use_info = new Table_use_info();
				// 3.遍历贴源层表数据信息保存表使用信息以及系统登记表参数信息
				// 4.获取系统内对应表名
				String hyren_name = tableInfoAndColumnInfo.get("hyren_name").toString();
				// 5.获取原始文件名称
				String original_name;
				if (tableInfoAndColumnInfo.get("original_name") == null) {
					original_name = tableInfoAndColumnInfo.get("table_name").toString();
				} else {
					original_name = tableInfoAndColumnInfo.get("original_name").toString();
				}
				// 6.根据用户ID、表名查询当前表是否已登记
				boolean flag = getUserTableInfo(userId, hyren_name);
				// 7.生成表使用ID
				String useId = String.valueOf(PrimayKeyGener.getNextId());
				// 8.判断当前用户对应表是否已登记做不同处理
				if (flag) {
					// 8.1已登记,根据用户ID、表名删除接口表数据
					deleteInterfaceTableInfo(userId, hyren_name);
				}
				// 9.新增表使用信息
				addTableUseInfo(table_note, data_layer, userId, useId, table_use_info,
						hyren_name, original_name);
				// 查询列信息
				String[] table_ch_column = tableDataInfo.getTable_ch_column();
				String[] table_en_column = tableDataInfo.getTable_en_column();
				if (table_ch_column == null && table_en_column == null) {
					// 获取所有列
					List<Table_column> tableColumnList = JsonUtil.toObject(
							JsonUtil.toJson(tableInfoAndColumnInfo.get("column_info_list")),
							new TypeReference<List<Table_column>>() {
							}.getType());
					table_ch_column = new String[tableColumnList.size()];
					table_en_column = new String[tableColumnList.size()];
					for (int i = 0; i < tableColumnList.size(); i++) {
						table_ch_column[i] = tableColumnList.get(i).getColumn_ch_name();
						table_en_column[i] = tableColumnList.get(i).getColumn_name();
					}
				}
				// 新增系统登记表参数信息
				addSysRegParameterInfo(useId, userId, table_ch_column, table_en_column,
						tableInfoAndColumnInfo.get("column_info_list"));
			}
			InterfaceManager.initTable(Dbo.db());
		}
	}

	@Method(desc = "新增系统登记参数表数据", logicStep = "1.数据可访问权限处理方式：该方法不需要进行访问权限限制" +
			" 2.封装系统登记参数表信息" +
			"3.循环保存系统登记表参数信息")
	@Param(name = "tableDataInfo", desc = "表信息实体对象", range = "无限制")
	@Param(name = "useId", desc = "表使用ID", range = "新增表使用信息时生成")
	@Param(name = "userId", desc = "用户ID", range = "新增用户时生成")
	@Param(name = "table_ch_column", desc = "列中文名称", range = "无限制")
	@Param(name = "table_en_column", desc = "列英文名称", range = "无限制")
	@Param(name = "column_info_list", desc = "字段的mate信息", range = "无限制")
	private void addSysRegParameterInfo(String useId, long userId, String[] table_ch_column,
	                                    String[] table_en_column, Object column_info_list) {
		// 1.数据可访问权限处理方式：该方法不需要进行访问权限限制
		// 2.封装系统登记参数表信息
		Sysreg_parameter_info sysreg_parameter_info = new Sysreg_parameter_info();
		sysreg_parameter_info.setUse_id(useId);
		sysreg_parameter_info.setIs_flag(IsFlag.Fou.getCode());
		sysreg_parameter_info.setUser_id(userId);
		System.out.println(JsonUtil.toJson(column_info_list));
		List<Map<String, String>> columnInfoList = JsonUtil.toObject(JsonUtil.toJson(column_info_list),
				new TypeReference<List<Map<String, String>>>() {
				}.getType());
		// 3.循环保存系统登记表参数信息
		for (int i = 0; i < table_en_column.length; i++) {
			sysreg_parameter_info.setParameter_id(PrimayKeyGener.getNextId());
			sysreg_parameter_info.setTable_ch_column(table_ch_column[i]);
			sysreg_parameter_info.setTable_en_column(table_en_column[i]);
			for (Map<String, String> map : columnInfoList) {
				if (map.get("column_name").equals(table_en_column[i])) {
					sysreg_parameter_info.setRemark(JsonUtil.toJson(map));
				}
			}
			sysreg_parameter_info.add(Dbo.db());
		}
	}

	@Method(desc = "新增表使用信息", logicStep = "1.数据可访问权限处理方式：该方法不需要进行访问权限限制" +
			"2.封装表使用信息参数" +
			"3.新增保存表使用信息")
	@Param(name = "table_note", desc = "备注", range = "无限制")
	@Param(name = "data_layer", desc = "数据层，树根节点", range = "无限制")
	@Param(name = "userId", desc = "用户ID", range = "新增用户时生成")
	@Param(name = "useId", desc = "表使用ID", range = "新增表使用信息时生成")
	@Param(name = "table_use_info", desc = "表使用信息实体", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "hyren_name", desc = "原始登记表名", range = "无限制")
	@Param(name = "original_name", desc = "原始文件名", range = "无限制")
	private void addTableUseInfo(String table_note, String data_layer, long userId, String useId,
	                             Table_use_info table_use_info, String hyren_name, String original_name) {
		// 1.数据可访问权限处理方式：该方法不需要进行访问权限限制
		// 2.封装表使用信息参数
		table_use_info.setSysreg_name(hyren_name);
		table_use_info.setUser_id(userId);
		table_use_info.setUse_id(useId);
		table_use_info.setTable_blsystem(data_layer);
		if (StringUtil.isBlank(original_name)) {
			table_use_info.setOriginal_name(hyren_name);
		} else {
			table_use_info.setOriginal_name(original_name);
		}
		if (StringUtil.isBlank(table_note)) {
			table_use_info.setTable_note("");
		} else {
			table_use_info.setTable_note(table_note);
		}
		// 3.新增保存表使用信息
		table_use_info.add(Dbo.db());
	}

	@Method(desc = "根据用户ID、表名查询当前表是否已登记",
			logicStep = "1.数据可访问权限处理方式：该方法不需要进行访问权限限制" +
					"2.根据用户ID、表名查询表使用信息是否存在")
	@Param(name = "user_id", desc = "用户ID", range = "新增用户时生成")
	@Param(name = "sysreg_name", desc = "系统登记表名", range = "无限制")
	@Return(desc = "返回当前用户对应的表是否已登记标志", range = "false代表未登记，true代表已登记")
	public boolean getUserTableInfo(long user_id, String sysreg_name) {
		// 1.数据可访问权限处理方式：该方法不需要进行访问权限限制
		// 2.根据用户ID、表名查询当前表是否已登记
		if (Dbo.queryNumber("select count(1) from " + Table_use_info.TableName
				+ " where user_id = ? and sysreg_name = ?", user_id, sysreg_name)
				.orElseThrow(() -> new BusinessException("sql查询错误")) > 0) {
			// 已登记
			logger.info("此表已登记");
			return true;
		} else {
			// 未登记
			logger.info("此表未登记");
			return false;
		}
	}

	@Method(desc = "根据用户ID、表名删除接口表数据",
			logicStep = "1.数据可访问权限处理方式：该方法不需要进行访问权限限制" +
					"2.先删除Sysreg_parameter_info" +
					"3.再删除table_use_info")
	@Param(name = "user_id", desc = "用户ID", range = "新增用户时生成")
	@Param(name = "sysreg_name", desc = "表名", range = "无限制")
	private void deleteInterfaceTableInfo(long user_id, String sysreg_name) {
		// 1.数据可访问权限处理方式：该方法不需要进行访问权限限制
		// 2.先删除Sysreg_parameter_info
		List<Long> useIdList = Dbo.queryOneColumnList("select use_id from " + Table_use_info.TableName +
				" where lower(sysreg_name)=lower(?) and user_id=?", sysreg_name, user_id);
		for (Long use_id : useIdList) {
			Dbo.execute("delete from " + Sysreg_parameter_info.TableName + " where use_id =? ", use_id);
		}
		// 3.再删除table_use_info
		Dbo.execute("delete from " + Table_use_info.TableName + " where lower(sysreg_name) = lower(?)" +
				" and user_id = ?", sysreg_name, user_id);
	}

	@Method(desc = "根据ID查询列信息", logicStep = "1.数据可访问权限处理方式：该方法不需要进行访问权限限制" +
			"2.根据不同数据源类型查询表的列信息并返回" +
			"2.1贴源层" +
			"2.2集市层")
	@Param(name = "file_id", desc = "表ID", range = "无限制", nullable = true)
	@Param(name = "data_layer", desc = "数据层，树根节点", range = "无限制")
	@Return(desc = "根据不同数据源类型查询表的列信息并返回", range = "无限制")
	public Map<String, Object> searchFieldById(String file_id, String data_layer) {
		// 1.数据可访问权限处理方式：该方法不需要进行访问权限限制
		return DataTableUtil.getTableInfoAndColumnInfo(data_layer, file_id);
	}
}
