package hrds.b.biz.agent.dbagentconf.cleanconf;

import com.alibaba.fastjson.JSONArray;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.resultset.Result;
import fd.ng.web.util.Dbo;
import hrds.b.biz.agent.bean.ColumnCleanParam;
import hrds.b.biz.agent.bean.TableCleanParam;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.CharSplitType;
import hrds.commons.codes.CleanType;
import hrds.commons.codes.FillingType;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.*;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.Constant;
import hrds.commons.utils.DboExecute;
import hrds.commons.utils.key.PrimayKeyGener;

import java.util.List;
import java.util.Map;

@DocClass(desc = "配置清洗规则", author = "WangZhengcheng")
public class CleanConfStepAction extends BaseAction{

	private static final int EMPTY_RESULT_COUNT = 0;

	/*
	 * 从上一个页面跳转过来，拿到在配置数据清洗页面显示信息(agentStep2&agentStep2SDO)
	 * */
	@Method(desc = "根据数据库设置ID获得清洗规则配置页面初始信息", logicStep = "" +
			"1、根据colSetId在table_info表中获取上一个页面配置好的采集表id" +
			"2、如果没有查询到结果，返回空的Result" +
			"3、否则根据采集表ID在table_info表和table_clean表中查出页面所需的信息")
	@Param(name = "colSetId", desc = "数据库设置ID，源系统数据库设置表主键，数据库对应表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空，" +
			"注意其中compflag/replaceflag/trimflag三个字段的值，" +
			"不为0表示该表做了相应的清洗设置，0表示没有做相应的设置")
	public Result getCleanConfInfo(long colSetId){
		//1、根据colSetId在table_info表中获取上一个页面配置好的采集表id
		List<Object> tableIds = Dbo.queryOneColumnList("SELECT table_id FROM " + Table_info.TableName +
				" WHERE database_id = ?", colSetId);
		//2、如果没有查询到结果，返回空的Result
		if(tableIds.isEmpty()){
			return new Result();
		}
		//3、否则根据采集表ID在table_info表和table_clean表中查出页面所需的信息
		StringBuilder strSB = new StringBuilder("SELECT ti.table_id, ti.table_name, ti.table_ch_name, " +
				" sum(CASE tc.clean_type WHEN ? THEN 1 ELSE 0 END) as compflag, " +
				" sum(CASE tc.clean_type WHEN ? THEN 1 ELSE 0 END) as replaceflag, " +
				" sum(CASE tc.clean_type WHEN ? THEN 1 ELSE 0 END) as trimflag " +
				" FROM "+ Table_info.TableName +" ti LEFT JOIN "+ Table_clean.TableName +" tc " +
				" ON ti.table_id = tc.table_id " +
				" where ti.table_id in ( ");
		for(int i = 0; i < tableIds.size(); i++){
			strSB.append(tableIds.get(i));
			if (i != tableIds.size() - 1)
				strSB.append( ",");
		}
		strSB.append(" ) GROUP BY ti.table_id ");

		return Dbo.queryResult(strSB.toString(), CleanType.ZiFuBuQi.getCode(), CleanType.ZiFuTiHuan.getCode(),
				CleanType.ZiFuTrim.getCode());
	}

	/*
	 * 配置数据清洗页面，字符补齐保存按钮，针对单个表(tableCleanSDO)
	 * */
	@Method(desc = "保存单表字符补齐规则", logicStep = "" +
			"1、校验入参合法性" +
			"2、在table_clean表中根据table_id删除该表原有的字符补齐设置，不关注删除数据的数目" +
			"3、设置主键" +
			"4、对补齐的特殊字符转为unicode码保存" +
			"5、执行保存")
	@Param(name = "charCompletion", desc = "待保存Table_clean实体类对象", range = "不为空,注意清洗方式的代码项" +
			"1：字符补齐" +
			"注意补齐方式：" +
			"1、前补齐" +
			"2、后补齐", isBean = true)
	public void saveSingleTbCompletionInfo(Table_clean charCompletion){
		//1、校验入参合法性，补齐字符应该不能为Null
		if(StringUtil.isEmpty(charCompletion.getCharacter_filling())){
			throw new BusinessException("保存整表字符补齐规则时，补齐字符不能为空");
		}
		if(charCompletion.getFilling_length() == null){
			throw new BusinessException("保存整表字符补齐规则时，补齐长度不能为空");
		}
		if(StringUtil.isBlank(charCompletion.getFilling_type())){
			throw new BusinessException("保存整表字符补齐规则时，必须选择补齐方式");
		}
		if(charCompletion.getTable_id() == null){
			throw new BusinessException("保存整表字符补齐规则是，必须关联表信息");
		}
		FillingType.ofEnumByCode(charCompletion.getFilling_type());
		//2、在table_clean表中根据table_id删除该表原有的字符补齐设置，不关注删除数据的数目
		Dbo.execute("DELETE FROM "+ Table_clean.TableName +" WHERE table_id = ? AND clean_type = ?",
				charCompletion.getTable_id(), CleanType.ZiFuBuQi.getCode());
		//3、设置主键
		charCompletion.setTable_clean_id(PrimayKeyGener.getNextId());
		charCompletion.setClean_type(CleanType.ZiFuBuQi.getCode());
		//4、对补齐的特殊字符转为unicode码保存
		charCompletion.setCharacter_filling(StringUtil.string2Unicode(charCompletion.getCharacter_filling()));
		//5、执行保存
		charCompletion.add(Dbo.db());
	}

	/*
	 * 在列清洗设置中保存字符补齐(columnLenSDO)
	 * */
	@Method(desc = "保存一列的字符补齐规则", logicStep = "" +
			"1、校验入参合法性，补齐字符应该不能为Null" +
			"2、在column_clean表中根据column_id删除该表原有的字符补齐设置，不关注删除数据的数目" +
			"3、设置主键" +
			"4、对补齐的特殊字符转为unicode码保存" +
			"5、执行保存")
	@Param(name = "charCompletion", desc = "待保存Column_clean实体类对象", range = "不为空,注意清洗方式的代码项" +
			"1：字符补齐" +
			"注意补齐方式：" +
			"1、前补齐" +
			"2、后补齐", isBean = true)
	public void saveColCompletionInfo(Column_clean charCompletion){
		//1、校验入参合法性，补齐字符应该不能为Null
		if(StringUtil.isEmpty(charCompletion.getCharacter_filling())){
			throw new BusinessException("保存列字符补齐规则时，补齐字符不能为空");
		}
		if(charCompletion.getFilling_length() == null){
			throw new BusinessException("保存列字符补齐规则时，补齐长度不能为空");
		}
		if(StringUtil.isBlank(charCompletion.getFilling_type())){
			throw new BusinessException("保存列字符补齐规则时，必须选择补齐方式");
		}
		if(charCompletion.getColumn_id() == null){
			throw new BusinessException("保存列字符补齐规则是，必须关联字段信息");
		}
		FillingType.ofEnumByCode(charCompletion.getFilling_type());
		//2、在column_clean表中根据column_id删除该表原有的字符补齐设置，不关注删除数据的数目
		Dbo.execute("DELETE FROM "+ Column_clean.TableName +" WHERE column_id = ? AND clean_type = ?",
				charCompletion.getColumn_id(), CleanType.ZiFuBuQi.getCode());
		//3、设置主键
		charCompletion.setCol_clean_id(PrimayKeyGener.getNextId());
		charCompletion.setClean_type(CleanType.ZiFuBuQi.getCode());
		//4、对补齐的特殊字符转为unicode码保存
		charCompletion.setCharacter_filling(StringUtil.string2Unicode(charCompletion.getCharacter_filling()));
		//5、执行保存
		charCompletion.add(Dbo.db());
	}

	/*
	 * 列清洗页面，字符补齐列，点击设置，回显已经该字段已经设置好的字符补齐信息(colLengthSDO)
	 * */
	@Method(desc = "根据列ID获得列字符补齐信息", logicStep = "" +
			"1、根据columnId在column_clean中查询该表的字符补齐信息" +
			"2、如果查询到，则将补齐字符解码后返回前端" +
			"3、如果没有列字符补齐信息，则根据columnId查其所在表是否配置了整表字符补齐，如果查询到，" +
			"   则将补齐字符解码后返回前端" +
			"4、如果整表字符补齐信息也没有，返回空的Result")
	@Param(name = "columnId", desc = "列ID，表对应字段表主键，列清洗信息表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空,key为列名，value为列值")
	public Map<String, Object> getColCompletionInfo(long columnId){
		//1、根据columnId在column_clean中查询该表的字符补齐信息
		Map<String, Object> compMap = Dbo.queryOneObject("select col_clean_id, filling_type, character_filling, " +
				" filling_length, column_id from " + Column_clean.TableName + " where column_id = ? and clean_type = ?"
				, columnId, CleanType.ZiFuBuQi.getCode());
		//2、如果查询到，则将补齐字符解码后返回前端
		if(!compMap.isEmpty()){
			compMap.put("character_filling", StringUtil.unicode2String((String) compMap.get("character_filling")));
			return compMap;
		}
		//3、如果没有列字符补齐信息，则根据columnId查其所在表是否配置了整表字符补齐，如果查询到，则将补齐字符解码后返回前端
		Map<String, Object> tbCompMap = Dbo.queryOneObject("SELECT tc.table_clean_id, tc.filling_type, " +
				" tc.character_filling, tc.filling_length FROM " + Table_clean.TableName + " tc" +
				" WHERE tc.table_id = (SELECT table_id FROM "+ Table_column.TableName +" WHERE column_id = ?)" +
				" AND tc.clean_type = ?", columnId, CleanType.ZiFuBuQi.getCode());
		//4、如果整表字符补齐信息也没有，返回空的map
		if(tbCompMap.isEmpty()){
			return tbCompMap;
		}
		tbCompMap.put("character_filling", StringUtil.unicode2String((String) tbCompMap.get("character_filling")));
		return tbCompMap;
	}

	/*
	 * 配置数据清洗页面，字符补齐列，点击设置，回显已经该表已经设置好的字符补齐信息(searchCharacterSDO)
	 * */
	@Method(desc = "根据表ID获取该表的字符补齐信息", logicStep = "" +
			"1、根据tableId去table_clean表中查询字符补齐规则" +
			"2、如果没有查询到数据，返回空的Result" +
			"3、如果查到了数据，将补齐字符解码之后返回前端")
	@Param(name = "tableId", desc = "数据库对应表主键，表对应字段表外键，表清洗参数表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Map<String, Object> getTbCompletionInfo(long tableId){
		//1、根据tableId去table_clean表中查询字符补齐规则
		Map<String, Object> tbCompMap = Dbo.queryOneObject("SELECT table_clean_id, filling_type, character_filling, " +
						"filling_length  FROM " + Table_clean.TableName + " WHERE table_id = ? AND clean_type = ?"
				, tableId, CleanType.ZiFuBuQi.getCode());
		//2、如果没有查询到数据，返回空的Result
		if(tbCompMap.isEmpty()){
			return tbCompMap;
		}
		//3、如果查到了数据，将补齐字符解码之后返回前端
		tbCompMap.put("character_filling", StringUtil.unicode2String((String) tbCompMap.get("character_filling")));
		return tbCompMap;
	}

	/*
	 * 配置数据清洗页面，字符替换保存按钮，针对单个表(tableColCleanSDO)
	 * */
	@Method(desc = "保存单个表的字符替换规则", logicStep = "" +
			"1、使用table_id在table_clean表中删除之前对该表定义过的字符替换规则，不关心删除数目" +
			"2、遍历replaceList" +
			"2-1、为每一个Table_clean对象设置主键" +
			"2-2、原字符串和替换字符串转为Unicode码" +
			"2-3、保存")
	@Param(name = "replaceString", desc = "存放有待保存信息的JSON数组", range = "不为空")
	@Param(name = "tableId", desc = "数据库对应表主键，表清洗参数表外键", range = "不为空")
	public void saveSingleTbReplaceInfo(String replaceString, long tableId){
		List<Table_clean> replaceList = JSONArray.parseArray(replaceString, Table_clean.class);
		//1、使用tableId在table_clean表中删除之前对该表定义过的字符替换规则，不关心删除数目
		Dbo.execute("DELETE FROM "+ Table_clean.TableName +" WHERE table_id = ? AND clean_type = ?", tableId,
				CleanType.ZiFuTiHuan.getCode());
		//2、遍历replaceList
		if(replaceList != null && !replaceList.isEmpty()){
			for(int i = 0; i < replaceList.size(); i++){
				Table_clean tableClean = replaceList.get(i);
				//这里用isEmpty的目的是，有可能原字符串和替换字符串都是空格或者特殊字符
				if(StringUtil.isEmpty(tableClean.getField())){
					throw new BusinessException("保存表字符替换规则时，第"+ (i + 1) +"条数据缺少源字符串");
				}
				if(StringUtil.isEmpty(tableClean.getReplace_feild())){
					throw new BusinessException("保存表字符替换规则时，第"+ (i + 1) +"条数据缺少替换字符串");
				}
				//2-1、为每一个Table_clean对象设置主键
				tableClean.setTable_clean_id(PrimayKeyGener.getNextId());
				tableClean.setClean_type(CleanType.ZiFuTiHuan.getCode());
				tableClean.setTable_id(tableId);
				//2-2、原字符串和替换字符串转为Unicode码
				tableClean.setField(StringUtil.string2Unicode(tableClean.getField()));
				tableClean.setReplace_feild(StringUtil.string2Unicode(tableClean.getReplace_feild()));
				//2-3、保存
				tableClean.add(Dbo.db());
			}
		}
	}

	/*
	 * 列清洗页面，字符替换保存按钮(columnCleanSDO)
	 * */
	@Method(desc = "保存单个字段的字符替换规则", logicStep = "" +
			"1、使用columnId在column_clean表中删除之前对该字段定义过的字符替换规则，不关心删除数目" +
			"2、遍历replaceList" +
			"2-1、为每一个Column_clean对象设置主键" +
			"2-2、原字符串和替换字符串转为Unicode码" +
			"2-3、保存")
	@Param(name = "replaceString", desc = "存放有待保存信息的JSON数组", range = "不为空")
	@Param(name = "columnId", desc = "表对应字段表主键，列清洗参数表外键", range = "不为空")
	public void saveColReplaceInfo(String replaceString, long columnId){
		List<Column_clean> replaceList = JSONArray.parseArray(replaceString, Column_clean.class);
		//1、使用columnId在column_clean表中删除之前对该字段定义过的字符替换规则，不关心删除数目
		Dbo.execute("DELETE FROM "+ Column_clean.TableName +" WHERE column_id = ? AND clean_type = ?", columnId,
				CleanType.ZiFuTiHuan.getCode());
		//2、遍历replaceList
		if(replaceList != null && !replaceList.isEmpty()){
			for(int i = 0; i < replaceList.size(); i++){
				Column_clean columnClean = replaceList.get(i);
				//这里使用isEmpty的目的是，在保存字符替换规则的时候，源字符串和替换字符串可能都是空格
				if(StringUtil.isEmpty(columnClean.getField())){
					throw new BusinessException("保存列字符替换规则时，第"+ (i + 1) +"条数据缺少源字符串");
				}
				if(StringUtil.isEmpty(columnClean.getReplace_feild())){
					throw new BusinessException("保存列字符替换规则时，第"+ (i + 1) +"条数据缺少替换字符串");
				}
				//2-1、为每一个Column_clean对象设置主键
				columnClean.setCol_clean_id(PrimayKeyGener.getNextId());
				columnClean.setClean_type(CleanType.ZiFuTiHuan.getCode());
				columnClean.setColumn_id(columnId);
				//2-2、原字符串和替换字符串转为Unicode码
				columnClean.setField(StringUtil.string2Unicode(columnClean.getField()));
				columnClean.setReplace_feild(StringUtil.string2Unicode(columnClean.getReplace_feild()));
				//2-3、保存
				columnClean.add(Dbo.db());
			}
		}
	}

	/*
	 * 配置数据清洗页面，点击设置，弹框回显针对该表的字符替换规则(replaceSDO)
	 * */
	@Method(desc = "根据表ID获取针对该表定义的字符替换规则", logicStep = "" +
			"1、根据tableId去table_clean表中查询该表的字符替换规则" +
			"2、如果没有查询到,直接空的Result" +
			"3、如果查询到了，对原字符串和替换字符串进行解码，然后返回")
	@Param(name = "tableId", desc = "数据库对应表主键，表清洗参数表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Result getSingleTbReplaceInfo(long tableId){
		//1、根据tableId去table_clean表中查询该表的字符替换规则
		Result result = Dbo.queryResult("SELECT table_clean_id, field, replace_feild FROM "+
				Table_clean.TableName +" WHERE table_id = ? AND clean_type = ?", tableId,
				CleanType.ZiFuTiHuan.getCode());
		//2、如果没有查询到,直接空的Result
		if(result.isEmpty()){
			return result;
		}
		//3、如果查询到了，对原字符串和替换字符串进行解码，然后返回
		result.setObject(0, "field", StringUtil.unicode2String(result.getString(0,
				"field")));
		result.setObject(0, "replace_feild",
				StringUtil.unicode2String(result.getString(0, "replace_feild")));
		return result;
	}

	/*
	 * 列清洗页面，点击设置，弹框回显针对该列的字符替换规则(colChartSDO)
	 * */
	@Method(desc = "根据列ID获得列字符替换信息", logicStep = "" +
			"1、根据columnId在column_clean中查询该表的字符替换信息" +
			"2、如果查询到，则将替换信息解码后返回前端" +
			"3、如果没有列字符替换信息，则根据columnId查其所在表是否配置了整表字符替换，如果查询到，" +
			"   则将替换字符解码后返回前端" +
			"4、如果整表字符替换信息也没有，返回空的Result")
	@Param(name = "columnId", desc = "列ID，表对应字段表主键，列清洗信息表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Result getColReplaceInfo(long columnId){
		//1、根据columnId在column_clean中查询该表的字符替换信息
		Result columnResult = Dbo.queryResult("select col_clean_id, field, replace_feild, column_id" +
				" from "+ Column_clean.TableName +" where column_id = ? and clean_type = ?", columnId,
				CleanType.ZiFuTiHuan.getCode());
		//2、如果查询到，则将源字符和替换字符解码后返回前端
		if(!columnResult.isEmpty()){
			columnResult.setObject(0, "field",
					StringUtil.unicode2String(columnResult.getString(0, "field")));
			columnResult.setObject(0, "replace_feild",
					StringUtil.unicode2String(columnResult.getString(0, "replace_feild")));
			return columnResult;
		}
		//3、如果没有列字符补齐信息，则根据columnId查其所在表是否配置了整表字符替换，如果查询到，则将补齐字符解码后返回前端
		Result tableResult = Dbo.queryResult("SELECT tc.table_clean_id, tc.field, tc.replace_feild " +
				" FROM "+ Table_clean.TableName +" tc" +
				" WHERE tc.table_id = (SELECT table_id FROM "+ Table_column.TableName +" WHERE column_id = ?" +
				" AND tc.clean_type = ?)", columnId, CleanType.ZiFuTiHuan.getCode());
		//4、如果整表字符替换信息也没有，返回空的Result
		if(tableResult.isEmpty()){
			return tableResult;
		}
		tableResult.setObject(0, "field", StringUtil.unicode2String(tableResult.getString(0,
				"field")));
		tableResult.setObject(0, "replace_feild",
				StringUtil.unicode2String(tableResult.getString(0, "replace_feild")));
		return tableResult;
	}

	/*
	 * 点击选择列按钮，查询列信息(columnSDO)
	 * */
	@Method(desc = "根据表ID获取该表所有的列清洗信息", logicStep = "" +
			"1、根据tableId去到table_column表中查询采集的列的列ID" +
			"2、如果没有找到采集列，直接返回一个空的结果集" +
			"3、如果找到了，再进行关联查询，查询出页面需要显示的信息" +
			"4、返回")
	@Param(name = "tableId", desc = "数据库对应表主键，表清洗参数表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空，数据的条数视实际情况而定" +
			"注意compflag/replaceflag/formatflag/splitflag/codevalueflag/trimflag这六个字段的值" +
			"不为0表示该列做了相应的清洗设置，0表示没有列相应的设置")
	public Result getColumnInfo(long tableId){
		//1、根据tableId去到table_column表中查询采集的,并且不是变化而生成的列ID
		List<Object> columnIds = Dbo.queryOneColumnList("select column_id from " + Table_column.TableName +
				" where table_id = ? and is_get = ? and is_new = ?", tableId, IsFlag.Shi.getCode(),
				IsFlag.Fou.getCode());
		//2、如果没有找到采集列，直接返回一个空结果集
		if(columnIds.isEmpty()){
			return new Result();
		}
		//3、如果找到了，再进行关联查询，查询出页面需要显示的信息
		StringBuilder sqlSB = new StringBuilder("SELECT t1.column_id,t1.colume_name,t1.colume_ch_name," +
				" t2.table_name," +
				" sum(case t3.clean_type when ? then 1 else 0 end) as compflag, " +
				" sum(case t3.clean_type when ? then 1 else 0 end) as replaceflag, " +
				" sum(case t3.clean_type when ? then 1 else 0 end ) as formatflag, " +
				" sum(case t3.clean_type when ? then 1 else 0 end) as splitflag, " +
				" sum(case t3.clean_type when ? then 1 else 0 end) as codevalueflag, " +
				" sum(case t3.clean_type when ? then 1 else 0 end) as trimflag " +
				" FROM "+ Table_column.TableName +" t1 JOIN "+ Table_info.TableName +
				" t2 ON t1.table_id = t2.table_id " +
				" left join "+ Column_clean.TableName +" t3 on t1.column_id = t3.column_id " +
				" WHERE t1.column_id in ( ");
		for(int i = 0; i < columnIds.size(); i++){
			sqlSB.append(columnIds.get(i));
			if (i != columnIds.size() - 1)
				sqlSB.append(",");
		}
		sqlSB.append(" ) GROUP BY t1.column_id, t2.table_name order by cast(t1.remark as integer) asc ");
		//4、返回
		return Dbo.queryResult(sqlSB.toString(), CleanType.ZiFuBuQi.getCode(), CleanType.ZiFuTiHuan.getCode(),
				CleanType.ShiJianZhuanHuan.getCode(), CleanType.ZiFuChaiFen.getCode(),
				CleanType.MaZhiZhuanHuan.getCode(), CleanType.ZiFuTrim.getCode());
	}

	/*
	 * 保存所有表清洗设置字符补齐和字符替换(saveJobCleanSDO)
	 * */
	@Method(desc = "保存所有表清洗设置字符补齐和字符替换", logicStep = "" +
			"1、根据colSetId在清洗参数属性表中删除记录，不关心是否删除到相应的数据" +
			"2、如果配置了字符补齐" +
			"2-2、保存字符补齐信息" +
			"3、如果配置了字符替换" +
			"3-1、构建Clean_parameter对象，设置原字段，替换后字段" +
			"3-2、保存字符替换信息")
	@Param(name = "colSetId", desc = "数据库设置ID，源系统数据库设置表主键，清洗参数属性表外键", range = "不为空")
	@Param(name = "compFlag", desc = "是否设置字符补齐标识位", range = "1：是，0：否")
	@Param(name = "replaceFlag", desc = "是否设置字符替换标识位", range = "1：是，0：否")
	@Param(name = "compType", desc = "字符补齐类型", range = "1：前补齐，2：后补齐", nullable = true, valueIfNull = "0")
	@Param(name = "compChar", desc = "补齐字符", range = "如果要进行字符补齐，该参数不为空", nullable = true,
			valueIfNull = "")
	@Param(name = "compLen", desc = "补齐长度", range = "如果要进行字符补齐，该参数不为空", nullable = true,
			valueIfNull = "")
	@Param(name = "oriFieldArr", desc = "原字符", range = "如果要进行字符替换，该参数不为空", nullable = true,
			valueIfNull = "")
	@Param(name = "replaceFeildArr", desc = "替换后字符", range = "如果要进行字符替换，该参数不为空", nullable = true,
			valueIfNull = "")
	public void saveAllTbCleanConfigInfo(long colSetId, String compFlag, String replaceFlag, String compType,
	                                     String compChar, String compLen, String[] oriFieldArr,
	                                     String[] replaceFeildArr){
		//1、根据colSetId在清洗参数属性表中删除记录，不关心是否删除到相应的数据
		Dbo.execute("DELETE FROM clean_parameter WHERE database_id = ?", colSetId);
		//2、如果配置了字符补齐
		if(IsFlag.ofEnumByCode(compFlag) == IsFlag.Shi){
			//这里表示校验补齐方式，1代表前补齐，2代表后补齐，如果传入的值不对，代码项校验就会报错
			FillingType fillingType = FillingType.ofEnumByCode(compType);
			//2-1、构建Clean_parameter对象，设置主键，存储字符补齐信息，将补齐字符转为unicode编码
			Clean_parameter allTbClean = new Clean_parameter();
			allTbClean.setC_id(PrimayKeyGener.getNextId());
			allTbClean.setDatabase_id(colSetId);
			allTbClean.setClean_type(CleanType.ZiFuBuQi.getCode());
			allTbClean.setFilling_type(fillingType.getCode());
			allTbClean.setCharacter_filling(StringUtil.string2Unicode(compChar));
			allTbClean.setFilling_length(compLen);
			//2-2、保存字符补齐信息
			allTbClean.add(Dbo.db());
		}

		//3、如果配置了字符替换
		if(IsFlag.ofEnumByCode(replaceFlag) == IsFlag.Shi){
			if(!(oriFieldArr.length > 0)){
				throw new BusinessException("保存所有表字符替换清洗设置时，缺失原字符");
			}
			if(!(replaceFeildArr.length > 0)){
				throw new BusinessException("保存所有表字符替换清洗设置时，缺失替换字符");
			}
			for(int i = 0; i < oriFieldArr.length; i++){
				String oriField = oriFieldArr[i];
				String replaceFeild = replaceFeildArr[i];
				//这里使用isEmpty的原因是，在保存字符替换的时候，原字符和替换字符都可能是空格
				if(StringUtil.isEmpty(oriField)){
					throw new BusinessException("保存所有表字符替换清洗时，请填写第"+ (i + 1) +"条数据的原字符");
				}
				if(StringUtil.isEmpty(replaceFeild)){
					throw new BusinessException("保存所有表字符替换清洗时，请填写第"+ (i + 1) +"条数据的替换字符");
				}
				//3-1、构建Clean_parameter对象，设置原字段，替换后字段
				Clean_parameter allTbClean = new Clean_parameter();
				allTbClean.setC_id(PrimayKeyGener.getNextId());
				allTbClean.setDatabase_id(colSetId);
				allTbClean.setClean_type(CleanType.ZiFuTiHuan.getCode());
				allTbClean.setField(StringUtil.string2Unicode(oriField));
				allTbClean.setReplace_feild(StringUtil.string2Unicode(replaceFeild));
				//3-2、保存字符替换信息
				allTbClean.add(Dbo.db());
			}
		}
	}

	/*
	 * 点击所有表清洗设置，回显所有表清洗设置字符补齐和字符替换规则(jobCleanSDO)
	 * */
	@Method(desc = "根据数据库设置ID查询所有表清洗设置字符替换规则", logicStep = "" +
			"1、根据colSetId在清洗参数属性表中获取字符替换规则" +
			"2、将原字符和替换后字符解码" +
			"3、返回")
	@Param(name = "colSetId", desc = "数据库设置ID，源系统数据库设置表主键，清洗参数属性表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Result getAllTbCleanReplaceInfo(long colSetId){
		//1、根据colSetId在清洗参数属性表中获取字符替换规则
		Result replaceResult = Dbo.queryResult("SELECT c_id, field, replace_feild FROM "+
				Clean_parameter.TableName + " WHERE database_id = ? AND clean_type = ?", colSetId,
				CleanType.ZiFuTiHuan.getCode());

		if(!replaceResult.isEmpty()){
			//2、将原字符和替换后字符解码
			for(int i = 0; i < replaceResult.getRowCount(); i++){
				replaceResult.setObject(i, "field", StringUtil.unicode2String(
						replaceResult.getString(i, "field")));
				replaceResult.setObject(i, "replace_feild", StringUtil.unicode2String(
						replaceResult.getString(i, "replace_feild")));
			}
		}
		//3、返回
		return replaceResult;
	}

	@Method(desc = "根据数据库设置ID查询所有表清洗设置字符补齐规则", logicStep = "" +
			"1、根据colSetId在清洗参数属性表中获取字符补齐规则" +
			"2、将补齐字符解码" +
			"3、返回")
	@Param(name = "colSetId", desc = "数据库设置ID，源系统数据库设置表主键，清洗参数属性表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Result getAllTbCleanCompInfo(long colSetId){
		//3、根据colSetId在清洗参数属性表中获取字符补齐规则
		Result compResult = Dbo.queryResult("SELECT c_id, filling_type, character_filling, filling_length " +
						" FROM "+ Clean_parameter.TableName +" WHERE database_id = ? AND clean_type = ?"
				, colSetId, CleanType.ZiFuBuQi.getCode());
		if(compResult.isEmpty()){
			return compResult;
		}
		if(compResult.getRowCount() > 1){
			throw new BusinessException("对所有表设置的字符补齐规则不唯一");
		}
		//4、将补齐字符解码
		compResult.setObject(0, "character_filling",
				StringUtil.unicode2String(compResult.getString(0, "character_filling")));
		return compResult;
	}

	/*
	 * 列清洗页面，点击日期格式化列，设置按钮，回显针对该列设置的日期格式化规则(lookColDateSDO)
	 * */
	@Method(desc = "根据列ID获取针对该列设置的日期格式化规则", logicStep = "" +
			"1、根据columnId在column_clean表中查询日期格式化规则并返回")
	@Param(name = "columnId", desc = "列ID，表对应字段表主键，列清洗信息表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Result getDateFormatInfo(long columnId){
		//1、根据columnId在column_clean表中查询日期格式化规则并返回
		return Dbo.queryResult("select col_clean_id, old_format, convert_format FROM "+ Column_clean.TableName
				+ " WHERE column_id = ? AND clean_type = ?", columnId, CleanType.ShiJianZhuanHuan.getCode());
	}

	/*
	 * 列清洗页面，点击日期格式化列设置按钮，对该列配置日期格式化规则，保存按钮(saveColDateSDO)
	 * */
	@Method(desc = "保存列清洗日期格式化", logicStep = "" +
			"1、如果之前针对该列设置过日期格式化，要删除之前的设置" +
			"2、设置主键" +
			"3、保存")
	@Param(name = "dateFormat", desc = "待保存的Column_clean类对象", range = "不为空，注意清洗方式代码项：" +
			"3：时间转换", isBean = true)
	public void saveDateFormatInfo(Column_clean dateFormat){
		if(StringUtil.isBlank(dateFormat.getOld_format())){
			throw new BusinessException("请填写原日期格式");
		}
		if(StringUtil.isBlank(dateFormat.getConvert_format())){
			throw new BusinessException("请填写转换后日期格式");
		}
		if(dateFormat.getColumn_id() == null){
			throw new BusinessException("保存日期转换信息必须关联字段");
		}
		//1、如果之前针对该列设置过日期格式化，要删除之前的设置
		Dbo.execute("DELETE FROM "+ Column_clean.TableName +" WHERE column_id = ? AND clean_type = ?"
				, dateFormat.getColumn_id(), CleanType.ShiJianZhuanHuan.getCode());
		//2、设置主键
		dateFormat.setCol_clean_id(PrimayKeyGener.getNextId());
		dateFormat.setClean_type(CleanType.ShiJianZhuanHuan.getCode());
		//3、保存
		dateFormat.add(Dbo.db());
	}

	/*
	 * 列清洗页面，点击列拆分列设置按钮，回显针对该列设置的列拆分信息(codeSplitLookSDO)
	 * */
	@Method(desc = "根据columnId查询列拆分信息", logicStep = "" +
			"1.使用columnId在column_split表中查询数据" +
			"2、如果没有查到，直接返回空的List" +
			"3、如果查到了，需要把拆分分隔符解码")
	@Param(name = "columnId", desc = "列ID，表对应字段表主键，列拆分表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Result getColSplitInfo(long columnId){
		//1.使用columnId在column_split表中查询数据
		Result result = Dbo.queryResult("select * from " +
				Column_split.TableName + " WHERE column_id = ?", columnId);
		//2、如果没有查到，直接返回空的List
		if(result.isEmpty()){
			return result;
		}
		//3、如果查到了，需要把拆分分隔符解码
		for(int i = 0; i < result.getRowCount(); i++){
			result.setObject(i, "split_sep", StringUtil.unicode2String(result.
					getString(i, "split_sep")));
		}
		return result;
	}

	/*
	 * 列清洗页面，点击列拆分列设置按钮，列拆分弹框操作栏，删除按钮(deletesplitSDO)
	 * */
	@Method(desc = "删除一条列拆分规则", logicStep = "" +
			"1、在table_column表中找到拆分生成的新列，并删除,应该删除一条数据" +
			"2、column_split表中根据colSplitId找到数据并删除，应该只有一条数据被删除" +
			"3、如果该列在列拆分表中已经没有数据，则在column_clean表中根据colCleanId删除类型为列拆分的数据，" +
			"如果删除，应该删除一条数据")
	@Param(name = "colSplitId", desc = "列拆分信息表主键", range = "不为空")
	@Param(name = "colCleanId", desc = "列清洗参数信息表主键", range = "不为空")
	public void deleteColSplitInfo(long colSplitId, long colCleanId){
		//1、在table_column表中找到拆分生成的新列，并删除,应该删除一条数据
		DboExecute.deletesOrThrow("列拆分规则删除失败", "delete from "+ Table_column.TableName  +
				" where colume_name = (select t1.colume_name from "+ Table_column.TableName +" t1 " +
				" JOIN "+ Column_split.TableName +" t2 ON t1.colume_name = t2.col_name " +
				" JOIN "+ Column_clean.TableName +" t3 ON t2.col_clean_id = t3.col_clean_id " +
				" WHERE t2.col_clean_id = ? and  t2.col_split_id = ? and t1.is_new = ?)",
				colCleanId, colSplitId, IsFlag.Shi.getCode());
		//2、column_split表中根据colSplitId找到数据并删除，应该只有一条数据被删除
		DboExecute.deletesOrThrow("列拆分规则删除失败",
				"delete from "+ Column_split.TableName +" where col_split_id = ?", colSplitId);
		//3、如果该列在列拆分表中已经没有数据，则在column_clean表中根据colCleanId删除类型为列拆分的数据，如果删除，应该删除一条数据
		long splitCount = Dbo.queryNumber("select count(1) from column_split where col_clean_id = ?",
				colCleanId).orElseThrow(() -> new BusinessException("SQL查询错误"));
		if(splitCount == 0){
			DboExecute.deletesOrThrow("列拆分规则删除失败", "delete from "+ Column_clean.TableName +
					" where col_clean_id = ? and clean_type = ?", colCleanId, CleanType.ZiFuChaiFen.getCode());
		}
	}

	/*
	 * 列清洗页面，点击列拆分列设置按钮，列拆分弹框操作栏，保存按钮(codeSplitCleanSDO)
	 * */
	@Method(desc = "保存列拆分规则", logicStep = "" +
			"1、首先，在column_clean表中，保存该列的列清洗信息" +
			"2、如果之前这个字段做过列拆分，需要在table_column表中找到拆分生成的新列，并删除,不关心删除的数目" +
			"3、如果这个字段之前做过列拆分，需要在column_split表中根据column_id找到数据并删除，不关心数目" +
			"4、为Column_split实体类对象中必须有值的属性设置值" +
			"5、保存Column_split实体类对象" +
			"6、将本次拆分生成的新列保存到table_column表中")
	@Param(name = "columnClean", desc = "待保存的列清洗信息", range = "Column_clean实体类对象，不为空" +
			"注意清洗方式：" +
			"字符拆分(6)", isBean = true)
	@Param(name = "columnSplitString", desc = "待保存的列拆分信息", range = "json字符串，不为空，注意拆分方式：" +
			"偏移量(1)" +
			"自定符号(2)")
	@Param(name = "tableId", desc = "数据库对应表主键，表清洗参数表外键", range = "不为空")
	public void saveColSplitInfo(Column_clean columnClean, String columnSplitString, long tableId){
		if(columnClean.getColumn_id() == null){
			throw new BusinessException("保存列拆分时必须关联字段");
		}
		//1、首先，在column_clean表中，保存该列的列清洗信息
		if(columnClean.getCol_clean_id() != null){
			//id有值，表示修改对该列设置的列拆分
			columnClean.setClean_type(CleanType.ZiFuChaiFen.getCode());
			columnClean.update(Dbo.db());

			//2、如果之前这个字段做过列拆分，需要在table_column表中找到拆分生成的新列，并删除,不关心删除的数目
			Dbo.execute("delete from "+ Table_column.TableName +" where colume_name in " +
					" (select t1.colume_name from "+ Table_column.TableName +" t1 " +
					" JOIN "+ Column_split.TableName +" t2 ON t1.colume_name = t2.col_name " +
					" JOIN "+ Column_clean.TableName +" t3 ON t2.col_clean_id = t3.col_clean_id " +
					" WHERE t2.col_clean_id = ? and t2.column_id = ? and t1.table_id = ? and t1.is_new = ?)",
					columnClean.getCol_clean_id(), columnClean.getColumn_id(), tableId, IsFlag.Shi.getCode());

			//3、如果这个字段之前做过列拆分，需要在column_split表中根据column_id找到该列并删除，不关心数目
			Dbo.execute("delete from "+ Column_split.TableName +" where column_id = ?",
					columnClean.getColumn_id());
		}else{
			//id没有值，表示新增
			columnClean.setCol_clean_id(PrimayKeyGener.getNextId());
			columnClean.setClean_type(CleanType.ZiFuChaiFen.getCode());
			columnClean.add(Dbo.db());
		}
		List<Column_split> columnSplits = JSONArray.parseArray(columnSplitString, Column_split.class);
		if(columnSplits != null && !columnSplits.isEmpty()){
			for(int i = 0; i < columnSplits.size(); i++){
				Column_split columnSplit = columnSplits.get(i);
				if(StringUtil.isBlank(columnSplit.getSplit_type())){
					throw new BusinessException("保存字符拆分信息时，第"+ (i + 1) +"条数据拆分方式不能为空");
				}
				CharSplitType charSplitType = CharSplitType.ofEnumByCode(columnSplit.getSplit_type());
				if(charSplitType == CharSplitType.ZhiDingFuHao){
					if(StringUtil.isBlank(columnSplit.getSplit_sep())){
						throw new BusinessException("按照自定符号进行拆分，第"+ (i + 1) +"条数据必须填写自定义符号");
					}
					if(columnSplit.getSeq() == null){
						throw new BusinessException("按照自定符号进行拆分，第"+ (i + 1) +"条数据必须填写值位置");
					}
				}else if(charSplitType == CharSplitType.PianYiLiang){
					if(StringUtil.isBlank(columnSplit.getCol_offset())){
						throw new BusinessException("按照偏移量进行拆分，第"+ (i + 1) +"条数据必须填写字段偏移量");
					}
				}else{
					throw new BusinessException("第"+ (i + 1) +"条数据拆分方式错误");
				}
				//4、为Column_split实体类对象中必须有值的属性设置值
				columnSplit.setCol_split_id(PrimayKeyGener.getNextId());
				columnSplit.setColumn_id(columnClean.getColumn_id());
				columnSplit.setCol_clean_id(columnClean.getCol_clean_id());
				columnSplit.setValid_s_date(DateUtil.getSysDate());
				columnSplit.setValid_e_date(Constant.MAXDATE);

				if(charSplitType == CharSplitType.ZhiDingFuHao){
					columnSplit.setSplit_sep(StringUtil.string2Unicode(columnSplit.getSplit_sep()));
				}
				//5、保存Column_split实体类对象
				columnSplit.add(Dbo.db());
				//6、将本次拆分生成的新列保存到table_column表中
				Table_column tableColumn = new Table_column();
				tableColumn.setTable_id(tableId);
				//是否为变化生成，设置为是
				tableColumn.setIs_new(IsFlag.Shi.getCode());
				//保存原字段
				tableColumn.setIs_alive(IsFlag.Shi.getCode());
				tableColumn.setColumn_id(PrimayKeyGener.getNextId());
				tableColumn.setIs_primary_key(IsFlag.Fou.getCode());
				tableColumn.setColume_name(columnSplit.getCol_name());
				tableColumn.setColumn_type(columnSplit.getCol_type());
				tableColumn.setColume_ch_name(columnSplit.getCol_zhname());
				tableColumn.setValid_s_date(DateUtil.getSysDate());
				tableColumn.setValid_e_date(Constant.MAXDATE);

				tableColumn.add(Dbo.db());
			}
		}
	}

	/*
	 * 列清洗页面，点击码值转换列设置按钮，获取该列在列清洗表中定义的码值系统编码和码值系统名称
	 * */
	@Method(desc = "根据列ID获取该列在列清洗参数表中定义码值系统编码(codesys)的和编码分类(codename)", logicStep = "" +
			"1、直接拼接SQL语句去数据中进行查询并返回")
	@Param(name = "columnId", desc = "列ID，采集列信息表主键，列清洗参数表外键", range = "不为空")
	@Return(desc = "查询结果", range = "不为空，条数根据实际情况决定")
	public Result getCVConversionInfo(long columnId){
		//1、直接拼接SQL语句去数据中进行查询并返回
		return Dbo.queryResult("select osi.orig_sys_code, osi.orig_sys_name ||'('||osi.orig_sys_code||')' " +
				" as orig_sys_name, cc.codename as code_classify " +
				" from " + Column_clean.TableName + " cc left join " + Orig_syso_info.TableName + " osi" +
				" on cc.codesys = osi.orig_sys_code where cc.column_id = ? and clean_type = ?"
				, columnId, CleanType.MaZhiZhuanHuan.getCode());
	}

	/*
	 * 获取当前系统中的所有码值系统信息
	 * */
	@Method(desc = "获取当前系统中的所有码值系统信息", logicStep = "" +
			"1、直接使用SQL语句去数据中进行查询并返回")
	@Return(desc = "系统中所有码值信息", range = "数据条数根据实际情况决定")
	public List<Orig_syso_info> getSysCVInfo(){
		//1、直接使用SQL语句去数据中进行查询并返回
		return Dbo.queryList(Orig_syso_info.class, "select * from " + Orig_syso_info.TableName);
	}

	/*
	 * 选择好码值系统后，关联出编码分类(sysTypeSDO)
	 * */
	@Method(desc = "根据码值系统编码获取编码分类", logicStep = "" +
			"1、执行SQL语句去数据库中查询编码分类")
	@Param(name = "origSysCode", desc = "码值系统编码", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空，具体数据条数根据实际情况决定")
	public Result getCVClassifyBySysCode(String origSysCode){
		//1、执行SQL语句去数据库中查询编码分类
		return Dbo.queryResult("select code_classify,orig_sys_code from " + Orig_code_info.TableName +
				" where orig_sys_code = ? group by code_classify,orig_sys_code", origSysCode);
	}

	/*
	 * 根据码值系统编码和编码分类获得原码值(orig_value)和新码值(code_value)(sysCodeValSDO)
	 * */
	@Method(desc = "根据码值系统编码和编码分类获得原码值(orig_value)和新码值(code_value)", logicStep = "" +
			"1、执行SQL语句去数据库中查询数据")
	@Param(name = "codeClassify", desc = "编码分类", range = "不为空")
	@Param(name = "origSysCode", desc = "码值系统编码", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空，具体数据条数根据实际情况决定")
	public Result getCVInfo(String codeClassify, String origSysCode){
		return Dbo.queryResult("select code_value, orig_value from " + Orig_code_info.TableName + " " +
				"where code_classify = ? and orig_sys_code = ? group by code_value, orig_value", codeClassify, origSysCode);
	}


	/*
	 * 列清洗页面，点击码值转换列设置按钮，码值转换弹框确定按钮(codeValueChangeCleanSDO)
	 * String codeClassify, String origSysCode
	 * */
	@Method(desc = "保存码值转换信息", logicStep = "" +
			"1、校验码值系统编码、编码分类、表ID" +
			"2、根据columnId在列清洗参数表中删除对该列定义的码值相关信息，不关注删除的条目" +
			"3、保存")
	@Param(name = "columnClean", desc = "待保存的码值转换信息", range = "Column_clean类型对象", isBean = true)
	public void saveCVConversionInfo(Column_clean columnClean){
		//1、校验码值系统编码、编码分类、表ID
		if(StringUtil.isBlank(columnClean.getCodename())){
			throw new BusinessException("请选择码值系统类型");
		}
		if(StringUtil.isBlank(columnClean.getCodesys())){
			throw new BusinessException("请选择码值系统名称");
		}
		if(columnClean.getColumn_id() == null){
			throw new BusinessException("保存码值转换，必须关联字段");
		}
		//2、根据columnId在列清洗参数表中删除对该列定义的码值相关信息，不关注删除的条目
		Dbo.execute("DELETE FROM " + Column_clean.TableName + " WHERE column_id = ? AND clean_type = ?",
				columnClean.getColumn_id(), CleanType.MaZhiZhuanHuan.getCode());
		//3、保存
		columnClean.setCol_clean_id(PrimayKeyGener.getNextId());
		columnClean.setClean_type(CleanType.MaZhiZhuanHuan.getCode());
		columnClean.add(Dbo.db());
	}

	/*
	 * 列清洗页面，点击列合并按钮，回显之前对该表设置的列合并信息(codeMergeLookSDO)
	 * */
	@Method(desc = "根据表ID查询针对该表设置的列合并信息", logicStep = "" +
			"1、去column_merge表中按照table_id查询出数据直接返回")
	@Param(name = "tableId", desc = "数据库对应表主键，列合并表外键", range = "不为空")
	@Return(desc = "查询结果集", range = "Column_merge实体类对象，不为空")
	public Result getColMergeInfo(long tableId){
		//1、去column_merge表中按照table_id查询出数据直接返回
		return Dbo.queryResult("select * from "+ Column_merge.TableName +
				" where table_id = ?", tableId);
	}

	/*
	 * 列清洗页面，点击列合并按钮，弹出列合并弹框，保存列合并信息(codeMergeCleanSDO)
	 * */
	@Method(desc = "保存列合并信息", logicStep = "" +
			"1、在table_column表中找到因配置过列合并而生成的列并删除，不关注删除的数目" +
			"2、在column_merge表中，按照table_id删除该表配置的所有列合并信息" +
			"3、为Column_merge实体类对象属性中设置必填的值" +
			"4、保存Column_merge实体类对象" +
			"5、将合并出来的列保存到table_column表中")
	@Param(name = "columnMergeString", desc = "待保存的列合并信息", range = "不为空，json格式字符串")
	@Param(name = "tableId", desc = "数据库对应表主键，列合并表外键", range = "不为空")
	public void saveColMergeInfo(String columnMergeString, long tableId){
		//1、在table_column表中找到因配置过列合并而生成的列并删除，不关注删除的数目
		Dbo.execute("delete from "+ Table_column.TableName +" where colume_name in " +
				" (select t1.colume_name from "+ Table_column.TableName +" t1 " +
				" JOIN "+ Column_merge.TableName +" t2 ON t1.table_id=t2.table_id " +
				" and t1.colume_name = t2.col_name " +
				" where t2.table_id = ? and t1.is_new = ? )", tableId, IsFlag.Shi.getCode());
		//2、在column_merge表中，按照table_id删除该表配置的所有列合并信息
		Dbo.execute("delete from "+ Column_merge.TableName +" where table_id = ?", tableId);
		//3、为Column_merge实体类对象属性中设置必填的值
		List<Column_merge> columnMerges = JSONArray.parseArray(columnMergeString, Column_merge.class);
		if(columnMerges != null && !columnMerges.isEmpty()){
			for(int i = 0; i < columnMerges.size(); i++){
				Column_merge columnMerge = columnMerges.get(i);
				if(StringUtil.isBlank(columnMerge.getOld_name())){
					throw new BusinessException("保存列合并时，第" + (i + 1) + "条数据必须选择要合并的字段");
				}
				if(StringUtil.isBlank(columnMerge.getCol_name())){
					throw new BusinessException("保存列合并时，第" + (i + 1) + "条数据必须填写合并后字段名称");
				}
				if(StringUtil.isBlank(columnMerge.getCol_type())){
					throw new BusinessException("保存列合并时，第" + (i + 1) + "条数据必须填写字段类型");
				}
				//4、保存Column_merge实体类对象
				columnMerge.setTable_id(tableId);
				columnMerge.setCol_merge_id(PrimayKeyGener.getNextId());
				columnMerge.setValid_s_date(DateUtil.getSysDate());
				columnMerge.setValid_e_date(Constant.MAXDATE);

				columnMerge.add(Dbo.db());

				//5、将合并出来的列保存到table_column表中
				Table_column tableColumn = new Table_column();
				tableColumn.setTable_id(tableId);
				tableColumn.setIs_new(IsFlag.Shi.getCode());
				tableColumn.setIs_alive(IsFlag.Shi.getCode());
				tableColumn.setColumn_id(PrimayKeyGener.getNextId());
				tableColumn.setIs_primary_key(IsFlag.Fou.getCode());
				tableColumn.setColume_name(columnMerge.getCol_name());
				tableColumn.setColumn_type(columnMerge.getCol_type());
				tableColumn.setColume_ch_name(columnMerge.getCol_zhname());
				tableColumn.setValid_s_date(DateUtil.getSysDate());
				tableColumn.setValid_e_date(Constant.MAXDATE);

				tableColumn.add(Dbo.db());
			}
		}
	}

	/*
	 * 列清洗页面，点击列合并按钮，弹出列合并弹框，操作栏删除按钮，删除列合并信息(deletemergeSDO)
	 * */
	@Method(desc = "删除一条列合并信息", logicStep = "" +
			"1、在table_column表中删除因合并生成的新列，删除的应该有且只有一条" +
			"2、在column_merge表中按ID删除一条列合并信息")
	@Param(name = "colMergeId", desc = "列合并信息表主键", range = "不为空")
	public void deleteColMergeInfo(long colMergeId){
		//1、在table_column表中删除因合并生成的新列，删除的应该有且只有一条
		DboExecute.deletesOrThrow("删除列合并失败", "delete from "+ Table_column.TableName +
				" where colume_name = " +
				" (select t1.colume_name " +
				" from "+ Table_column.TableName +" t1 " +
				" JOIN "+ Column_merge.TableName +" t2 ON t1.table_id = t2.table_id " +
				" and t1.colume_name = t2.col_name " +
				" where t2.col_merge_id = ?)", colMergeId);
		//2、在column_merge表中按ID删除一条列合并信息
		DboExecute.deletesOrThrow("删除列合并失败", "delete from "+ Column_merge.TableName +
						" where col_merge_id = ?", colMergeId);
	}

	/*
	 * 全表清洗优先级保存按钮(saveClearSortSDO)，针对本次采集任务的所有表保存清洗优先级
	 * */
	@Method(desc = "保存所有表清洗优先级", logicStep = "" +
			"1、使用colSetId在database_set表中查找，看是否能找到对应的记录" +
			"2、如果没有找到，直接抛异常" +
			"3、如果找到了，根据colSetId,在database_set表中找到对应的记录，将sort更新进去")
	@Param(name = "colSetId", desc = "数据库采集设置ID", range = "不为空")
	@Param(name = "sort", desc = "所有表清洗优先级，JSON格式", range = "不为空，" +
			"如：{\"1\":1,\"2\":2,\"3\":3,\"4\":4,\"5\":5,\"6\":6,\"7\":7}" +
			"注意：json的key请务必按照示例中给出的写")
	public void saveAllTbCleanOrder(long colSetId, String sort){
		//1、使用colSetId在database_set表中查找，看是否能找到对应的记录
		long count = Dbo.queryNumber("select count(1) from " + Database_set.TableName + " where database_id = ?"
				, colSetId).orElseThrow(() -> new BusinessException("SQL查询错误"));
		//2、如果没有找到，直接抛异常
		if(count != 1){
			throw new BusinessException("未能找到数据库采集任务");
		}
		//3、如果找到了，根据table_id,在table_info表中找到对应的记录，将sort更新进去
		DboExecute.updatesOrThrow("保存全表清洗优先级失败", "update " +
				Database_set.TableName + " set cp_or = ? where database_id = ?", sort, colSetId);
	}

	/*
	 * 回显全表清洗优先级
	 * */
	@Method(desc = "根据数据库设置ID回显全表清洗优先级", logicStep = "" +
			"1、使用colSetId在database_set表中查找，看是否能找到对应的记录" +
			"2、如果没有找到，直接抛异常" +
			"3、如果找到了，根据colSetId,在database_set表中找到对应的记录，并返回")
	@Param(name = "colSetId", desc = "数据库采集设置ID", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Result getAllTbCleanOrder(long colSetId){
		//1、使用colSetId在database_set表中查找，看是否能找到对应的记录
		long count = Dbo.queryNumber("select count(1) from " + Database_set.TableName + " where database_id = ?"
				, colSetId).orElseThrow(() -> new BusinessException("SQL查询错误"));
		//2、如果没有找到，直接抛异常
		if(count != 1){
			throw new BusinessException("未能找到数据库采集任务");
		}
		//3、如果找到了，根据colSetId,在database_set表中找到对应的记录，并返回
		return Dbo.queryResult("select cp_or from " + Database_set.TableName + " where database_id = ?", colSetId);
	}

	/*
	 * 列清洗页面，整表优先级设置，对单个表的所有字段设置清洗优先级
	 * */
	@Method(desc = "保存整表清洗优先级", logicStep = "" +
			"1、根据table_id,在table_info表中找到对应的表，将sort更新进去")
	@Param(name = "tableId", desc = "数据库对应表主键", range = "不为空")
	@Param(name = "sort", desc = "所有表清洗优先级，JSON格式", range = "不为空，" +
			"如：{\"1\":1,\"2\":2,\"3\":3,\"4\":4,\"5\":5,\"6\":6,\"7\":7}" +
			"注意：json的key请务必按照示例中给出的写")
	public void saveSingleTbCleanOrder(long tableId, String sort){
		//1、根据table_id,在table_info表中找到对应的表，将sort更新进去
		DboExecute.updatesOrThrow("保存整表清洗优先级失败",
				"update "+ Table_info.TableName +" set ti_or = ? where table_id = ?", sort, tableId);
	}

	/*
	 * 列清洗页面，回显整表优先级
	 * */
	@Method(desc = "根据表ID回显整表清洗优先级", logicStep = "" +
			"1、在数据库设置表中，根据tableId和colSetId查找该采集任务中是否存在该表" +
			"2、如果不存在，直接抛异常" +
			"3、如果存在，查询正表清洗优先级并返回")
	@Param(name = "tableId", desc = "表ID", range = "不为空")
	@Param(name = "colSetId", desc = "数据库设置ID", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Result getSingleTbCleanOrder(long tableId, long colSetId){
		//1、在数据库设置表中，根据tableId和colSetId查找该采集任务中是否存在该表
		long count = Dbo.queryNumber("select count(1) from " + Table_info.TableName + " where table_id = ? and " +
				"database_id = ?", tableId, colSetId).orElseThrow(() -> new BusinessException("SQL查询错误"));
		//2、如果不存在，直接抛异常
		if(count != 1){
			throw new BusinessException("在当前数据库采集任务中未找到该采集表");
		}
		//3、如果存在，查询整表清洗优先级并返回
		return Dbo.queryResult("select ti_or from " + Table_info.TableName + " where table_id = ?", tableId);
	}

	/*
	 * 列清洗页面，优先级调整设置，对单个字段设置清洗优先级
	 * */
	@Method(desc = "保存单个字段清洗优先级", logicStep = "" +
			"1、根据columnId,在table_column表中找到对应的字段，将清洗顺序设置进去")
	@Param(name = "columnId", desc = "表对应字段表主键", range = "不为空")
	@Param(name = "sort", desc = "字段清洗优先级，JSON格式", range = "不为空，" +
			"如：{\"1\":1,\"2\":2,\"3\":3,\"4\":4,\"5\":5,\"6\":6,\"7\":7}" +
			"注意：json的key请务必按照示例中给出的来命名")
	public void saveColCleanOrder(long columnId, String sort){
		//1、根据columnId,在table_column表中找到对应的字段，将清洗顺序设置进去
		DboExecute.updatesOrThrow("保存列清洗优先级失败",
				"update "+ Table_column.TableName +" set tc_or = ? where column_id = ?", sort, columnId);
	}

	/*
	 * 列清洗页面，回显列清洗优先级
	 * */
	@Method(desc = "根据列ID回显列清洗优先级", logicStep = "" +
			"1、在table_column表中，判断列是否存在" +
			"2、不存在，直接抛异常" +
			"3、若存在，查询出该列的清洗优先级返回给前端")
	@Param(name = "columnId", desc = "列ID", range = "不为空")
	@Param(name = "tableId", desc = "表ID", range = "不为空")
	@Return(desc = "查询结果集", range = "不为空")
	public Result getColCleanOrder(long columnId, long tableId){
		//1、在table_column表中，判断列是否存在
		long count = Dbo.queryNumber("select count(1) from " + Table_column.TableName + " where column_id = ? " +
				"and table_id = ?", columnId, tableId).orElseThrow(() -> new BusinessException("SQL查询错误"));
		//2、不存在，直接抛异常
		if(count != 1){
			throw new BusinessException("未找到字段");
		}
		//3、若存在，查询出该列的清洗优先级返回给前端
		return Dbo.queryResult("select tc_or from " + Table_column.TableName + " where column_id = ?", columnId);
	}

	/*
	 * 列清洗页面，点击保存，由于字符补齐、字符替换、日期格式化、列拆分、码值转换都已经保存入库了，所以这里处理的逻辑只保存列首尾去空
	 * 但是必须将页面上每个字段是否补齐，是否替换，是否码值，是否日期也都传过来，如果用户配置了，但是有取消了勾选，要在这个方法里面做处理
	 * */
	@Method(desc = "保存列清洗信息", logicStep = "" +
			"1、将colCleanString反序列化为List<ColumnCleanParam>" +
			"2、遍历List集合" +
			"2-1、判断最终保存时，是否选择了字符补齐，否，则根据columnId去column_clean表中删除一条记录" +
			"2-2、判断最终保存时，是否选择了字符替换，否，则根据columnId去column_clean表中删除一条记录" +
			"2-3、判断最终保存时，是否选择了日期格式化，否，则根据columnId去column_clean表中删除一条记录" +
			"2-4、判断最终保存时，是否选择了码值转换，否，则进行删除当前列码值转换的处理，目前没搞清楚码值转换的保存逻辑，所以这个处理暂时没有" +
			"2-5、判断最终保存时，是否选择了列拆分，否，则进行删除列拆分的操作" +
			"2-6、判断最终保存时，是否选择了列首尾去空，进行首尾去空的保存处理")
	@Param(name = "colCleanString", desc = "所有列的列清洗参数信息,JSON格式", range = "不为空，" +
			"如：[{\"columnId\":1001,\"complementFlag\":true,\"replaceFlag\":true,\"formatFlag\":true,\"conversionFlag\":4,\"spiltFlag\":false,\"trimFlag\":true}," +
			"{\"columnId\":1002,\"complementFlag\":true,\"replaceFlag\":true,\"formatFlag\":true,\"conversionFlag\":4,\"spiltFlag\":false,\"trimFlag\":true}]" +
			"注意：请务必按照示例来命名")
	public void saveColCleanConfig(String colCleanString){
		//1、将colCleanString反序列化为List<ColumnCleanParam>
		List<ColumnCleanParam> columnCleanParams = JSONArray.parseArray(colCleanString, ColumnCleanParam.class);

		if(columnCleanParams == null || columnCleanParams.isEmpty()){
			throw new BusinessException("未获取到列清洗信息");
		}

		//2、遍历List集合
		for(ColumnCleanParam param : columnCleanParams){
			//2-1、判断最终保存时，是否选择了字符补齐，否，则根据columnId去column_clean表中尝试删除记录，不关心具体的数目
			if(!param.isComplementFlag()){
				Dbo.execute("DELETE FROM "+ Column_clean.TableName +" WHERE column_id = ? AND clean_type = ?"
						, param.getColumnId(), CleanType.ZiFuBuQi.getCode());
			}
			//2-2、判断最终保存时，是否选择了字符替换，否，则根据columnId去column_clean表中尝试删除记录，不关心具体的数目
			if(!param.isReplaceFlag()){
				Dbo.execute("DELETE FROM "+ Column_clean.TableName +" WHERE column_id = ? AND clean_type = ?"
						, param.getColumnId(), CleanType.ZiFuTiHuan.getCode());
			}
			//2-3、判断最终保存时，是否选择了日期格式化，否，则根据columnId去column_clean表中尝试删除记录，不关心具体的数目
			if(!param.isFormatFlag()){
				Dbo.execute( "DELETE FROM " + Column_clean.TableName +" WHERE column_id = ? AND clean_type = ?"
						, param.getColumnId(), CleanType.ShiJianZhuanHuan.getCode());
			}
			//2-4、判断最终保存时，是否选择了码值转换，否，则根据columnId去column_clean表中尝试删除记录，不关心具体的数目
			if(!param.isConversionFlag()){
				Dbo.execute( "DELETE FROM " + Column_clean.TableName +" WHERE column_id = ? AND clean_type = ?"
						, param.getColumnId(), CleanType.MaZhiZhuanHuan.getCode());
			}

			//2-5、判断最终保存时，是否选择了列拆分，否，则进行删除列拆分的操作
			if(!param.isSpiltFlag()){
				Result colSplitInfo = getColSplitInfo(param.getColumnId());
				if(!colSplitInfo.isEmpty()){
					for(int i = 0; i < colSplitInfo.getRowCount(); i++){
						deleteColSplitInfo(colSplitInfo.getLong(i, "col_split_id"),
								colSplitInfo.getLong(i, "col_clean_id"));
					}
				}
			}
			//2-6、判断最终保存时，是否选择了列首尾去空，进行首尾去空的保存处理
			if(param.isTrimFlag()){
				Dbo.execute("delete from "+ Column_clean.TableName +" where column_id = ? and clean_type = ?",
						param.getColumnId(), CleanType.ZiFuTrim.getCode());
				Column_clean trim = new Column_clean();
				trim.setCol_clean_id(PrimayKeyGener.getNextId());
				trim.setClean_type(CleanType.ZiFuTrim.getCode());
				trim.setColumn_id(param.getColumnId());

				trim.add(Dbo.db());
			}
		}
	}

	/*
	 * 点击下一步按钮，保存该页面所有信息(其实经过上面所有方法的处理后，配置数据清洗保存的只有首尾去空这一项信息了)，
	 * 但是必须将页面上是否整表补齐，是否整表替换信息也传过来，如果用户配置了，但是又取消了勾选，要在这个方法里面做处理
	 * */
	@Method(desc = "保存配置数据清洗页面信息", logicStep = "" +
			"1、将tbCleanString反序列化为List<TableCleanParam>" +
			"2、遍历List集合" +
			"2-1、判断最终保存时，是否选择了字符补齐，否，则根据tableId去table_clean表中删除一条记录" +
			"2-2、判断最终保存时，是否选择了字符替换，否，则根据tableId去table_clean表中删除一条记录" +
			"2-3、判断最终保存时，是否选择了列首尾去空，进行首尾去空的保存处理")
	@Param(name = "colSetId", desc = "数据库设置ID，源系统数据库设置表主键，数据库对应表外键", range = "不为空")
	@Param(name = "tbCleanString", desc = "所有表的清洗参数信息,JSON格式", range = "不为空，" +
			"如：[{\"tableId\":1001,\"tableName\":\"table_info\",\"complementFlag\":true,\"replaceFlag\":true,trimFlag:true}," +
			"{\"tableId\":1002,\"tableName\":\"table_column\",\"complementFlag\":true,\"replaceFlag\":true,trimFlag:true}]" +
			"注意：请务必按照示例中给出的方式命名")
	@Return(desc = "数据库设置ID", range = "便于下一个页面通过传递这个值，查询到之前设置的信息")
	public long saveDataCleanConfig(long colSetId, String tbCleanString){
		//1、将tbCleanString反序列化为List<TableCleanParam>
		List<TableCleanParam> tableCleanParams = JSONArray.parseArray(tbCleanString, TableCleanParam.class);

		if(tableCleanParams == null || tableCleanParams.isEmpty()){
			throw new BusinessException("未获取到表清洗信息");
		}

		//2、遍历List集合
		for(TableCleanParam param : tableCleanParams){
			//2-1、判断最终保存时，是否选择了字符补齐，否，则根据tableId去table_clean表中尝试删除记录，不关心删除的数目
			if(!param.isComplementFlag()){
				Dbo.execute("DELETE FROM "+ Table_clean.TableName +" WHERE table_id = ? AND clean_type = ?"
						, param.getTableId(), CleanType.ZiFuBuQi.getCode());
			}
			//2-2、判断最终保存时，是否选择了字符替换，否，则根据tableId去table_clean表中尝试删除记录，不关心删除的数目
			if(!param.isReplaceFlag()){
				Dbo.execute("DELETE FROM "+ Table_clean.TableName +" WHERE table_id = ? AND clean_type = ?"
						, param.getTableId(), CleanType.ZiFuTiHuan.getCode());
			}
			//2-3、判断最终保存时，是否选择了列首尾去空，进行首尾去空的保存处理
			if(param.isTrimFlag()){
				Dbo.execute("delete from "+ Table_clean.TableName +" where table_id = ? and clean_type = ?",
						param.getTableId(), CleanType.ZiFuTrim.getCode());
				Table_clean trim = new Table_clean();
				trim.setTable_clean_id(PrimayKeyGener.getNextId());
				trim.setClean_type(CleanType.ZiFuTrim.getCode());
				trim.setTable_id(param.getTableId());

				trim.add(Dbo.db());
			}
		}
		return colSetId;
	}
}
