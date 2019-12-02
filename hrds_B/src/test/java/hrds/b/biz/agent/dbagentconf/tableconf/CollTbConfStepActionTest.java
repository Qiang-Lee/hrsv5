package hrds.b.biz.agent.dbagentconf.tableconf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.exception.BusinessSystemException;
import fd.ng.core.utils.JsonUtil;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.db.resultset.Result;
import fd.ng.netclient.http.HttpClient;
import fd.ng.web.action.ActionResult;
import hrds.b.biz.agent.bean.CollTbConfParam;
import hrds.b.biz.agent.dbagentconf.BaseInitData;
import hrds.commons.codes.*;
import hrds.commons.entity.*;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.Constant;
import hrds.testbase.WebBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@DocClass(desc = "CollTbConfStepAction单元测试类", author = "WangZhengcheng")
public class CollTbConfStepActionTest extends WebBaseTestCase{

	private static final long CODE_INFO_TABLE_ID = 7002L;
	private static final long SYS_USER_TABLE_ID = 7001L;
	private static final long AGENT_INFO_TABLE_ID = 7003L;
	private static final long DATA_SOURCE_TABLE_ID = 7004L;
	private static final long FIRST_DATABASESET_ID = 1001L;
	private static final long SECOND_DATABASESET_ID = 1002L;
	private static final long UNEXPECTED_ID = 999999;
	private static final JSONObject tableCleanOrder = BaseInitData.initTableCleanOrder();
	private static final JSONObject columnCleanOrder = BaseInitData.initColumnCleanOrder();

	/**
	 * 为每个方法的单元测试初始化测试数据
	 *
	 * 1、构造默认表清洗优先级
	 * 2、构造默认列清洗优先级
	 * 3、构造data_source表测试数据
	 * 4、构造agent_info表测试数据
	 * 5、构造database_set表测试数据
	 * 6、构造Collect_job_classify表测试数据
	 * 7、构建table_info测试数据
	 * 8、构建table_column表测试数据
	 * 9、构造table_storage_info表测试数据
	 * 10、构造table_clean表测试数据
	 * 11、构造column_merge表测试数据
	 * 12、插入数据
	 *
	 * 测试数据：
	 *      1、默认表清洗优先级为(字符补齐，字符替换，列合并，首尾去空)
	 *      2、默认列清洗优先级为(字符补齐，字符替换，日期格式转换，码值转换，列拆分，首尾去空)
	 *      3、data_source表：有1条数据，source_id为1
	 *      4、agent_info表：有2条数据,全部是数据库采集Agent，agent_id分别为7001，7002,source_id为1
	 *      5、database_set表：有2条数据,database_id为1001,1002, agent_id分别为7001,7002，1001的classifyId是10086，1002的classifyId是10010
	 *      1001设置完成并发送成功(is_sendok)
	 *      6、collect_job_classify表：有2条数据，classify_id为10086L、10010L，agent_id分别为7001L、7002L,user_id为9997L
	 *      7、table_info表测试数据共4条，databaseset_id为1001
	 *          7-1、table_id:7001,table_name:sys_user,按照画面配置信息进行采集，并且配置了单表过滤SQL,select * from sys_user where user_id = 2001，不进行并行抽取
	 *          7-2、table_id:7002,table_name:code_info,按照画面配置信息进行采集,进行并行抽取，分页SQL为select * from code_info limit 10
	 *          7-3、table_id:7003,table_name:agent_info,按照自定义SQL进行采集，不进行并行抽取
	 *          7-4、table_id:7004,table_name:data_source,按照自定义SQL进行采集，不进行并行抽取
	 *      8、table_column表测试数据：只有在画面上进行配置的采集表才会向table_column表中保存数据
	 *          8-1、column_id为2001-2010，模拟采集了sys_user表的前10个列，列名为user_id，create_id，dep_id，role_id，
	 *               user_name，user_password，user_email，user_mobile，useris_admin，user_type
     *          8-2、column_id为3001-3005，模拟采集了code_info表的所有列，列名为ci_sp_code，ci_sp_class，ci_sp_classname，
	 *               ci_sp_name，ci_sp_remark
	 *          8-3、模拟自定义采集agent_info表的agent_id，agent_name，agent_type三个字段
	 *          8-4、模拟自定义采集data_source表的source_id，datasource_number，datasource_name三个字段
	 *      9、table_storage_info表测试数据：
	 *          9-1、storage_id为1234，文件格式为CSV，存储方式为替换，table_id为sys_user表的ID
	 *          9-2、storage_id为5678，文件格式为定长文件，存储方式为追加，table_id为code_info表的ID
	 *      10、table_clean表测试数据：
	 *          10-1、模拟数据对sys_user表进行整表清洗，分别做了列合并和首尾去空
	 *          10-2、模拟数据对code_info表进行了整表清洗，分别做了字符替换字符补齐，将所有列值的abc全部替换为def，将所有列值的前面补上beyond字符串
	 *      11、column_merge表测试数据：对sys_user设置了两个列合并，对code_info表设置了一个列合并
	 *          11-1、模拟数据将sys_user表的user_id和create_id两列合并为user_create_id
	 *          11-2、模拟数据将sys_user表的user_name和user_password两列合并为user_name_password
	 *          11-3、模拟数据将code_info表的ci_sp_classname和ci_sp_name两列合并为ci_sp_classname_name
	 *      12、data_extraction_def表测试数据：
	 *          12-1、sys_user:抽取并入库，需要表头，落地编码为UTF-8，数据落地格式为ORC，数据落地目录为/root
	 *          12-2、code_info:抽取并入库，不需要表头，落地编码为UTF-8，数据落地格式为PARQUET，数据落地目录为/home/hyshf
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Before
	public void before() {
		InitAndDestDataForCollTb.before();
		//模拟登陆
		ActionResult actionResult = BaseInitData.simulatedLogin();
		assertThat("模拟登陆", actionResult.isSuccess(), is(true));
	}

	/**
	 * 测试根据colSetId加载页面初始化数据
	 *
	 * 正确数据访问1：构造正确的colSetId(FIRST_DATABASESET_ID)
	 * 错误的数据访问1：构造错误的colSetId
	 * 错误的测试用例未达到三组:getInitInfo方法只有一个参数
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void getInitInfo(){
		//正确数据访问1：构造正确的colSetId(FIRST_DATABASESET_ID)
		String rightString = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.post(getActionUrl("getInitInfo")).getBodyString();
		ActionResult rightResult = JsonUtil.toObjectSafety(rightString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResult.isSuccess(), is(true));
		Result rightData = rightResult.getDataForResult();
		assertThat("根据测试数据，输入正确的colSetId查询到的非自定义采集表应该有" + rightData.getRowCount() + "条", rightData.getRowCount(), is(2));

		//错误的数据访问1：构造错误的colSetId
		long wrongColSetId = 99999L;
		String wrongString = new HttpClient()
				.addData("colSetId", wrongColSetId)
				.post(getActionUrl("getInitInfo")).getBodyString();
		ActionResult wrongResult = JsonUtil.toObjectSafety(wrongString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResult.isSuccess(), is(true));
		Result wrongData = wrongResult.getDataForResult();
		assertThat("根据测试数据，输入错误的colSetId查询到的非自定义采集表信息应该有" + wrongData.getRowCount() + "条", wrongData.getRowCount(), is(0));
	}

	/**
	 * 测试根据数据库设置id得到所有表相关信息功能
	 * TODO 目前测试用例只判断和agent能够调通，并且获取到的list有值，因为目前我们自己的测试用数据库是处于变化当中的
	 * 正确数据访问1：构造colSetId为1002，inputString为code的测试数据
	 * 正确数据访问2：构造colSetId为1002，inputString为sys的测试数据
	 * 正确数据访问3：构造colSetId为1001，inputString为sys|code的测试数据
	 * 正确数据访问4：构造colSetId为1001，inputString为wzc的测试数据
	 * 错误的数据访问1：构造colSetId为1003的测试数据
	 * 错误的测试用例未达到三组:已经测试用例已经可以覆盖程序中所有的分支
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void getTableInfo(){
		//正确数据访问1：构造colSetId为1002，inputString为code的测试数据
		String rightStringOne = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("inputString", "code")
				.post(getActionUrl("getTableInfo")).getBodyString();
		ActionResult rightResultOne = JsonUtil.toObjectSafety(rightStringOne, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultOne.isSuccess(), is(true));

		Result rightDataOne = rightResultOne.getDataForResult();
		assertThat("使用code做模糊查询得到的表信息", rightDataOne.isEmpty(), is(false));

		/*
		List<Result> rightDataOne = rightResultOne.getDataForEntityList(Result.class);

		assertThat("使用code做模糊查询得到的表信息有1条", rightDataOne.size(), is(1));
		assertThat("使用code做模糊查询得到的表名为code_info", rightDataOne.get(0).getString(0, "table_name"), is("code_info"));
		*/

		//正确数据访问2：构造colSetId为1002，inputString为sys的测试数据
		String rightStringTwo = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("inputString", "sys")
				.post(getActionUrl("getTableInfo")).getBodyString();
		ActionResult rightResultTwo = JsonUtil.toObjectSafety(rightStringTwo, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultTwo.isSuccess(), is(true));
		Result rightDataTwo = rightResultTwo.getDataForResult();
		assertThat("使用sys做模糊查询得到的表信息", rightDataTwo.isEmpty(), is(false));

		/*
		List<Result> rightDataTwo = rightResultTwo.getDataForEntityList(Result.class);
		assertThat("使用sys做模糊查询得到的表信息有6条", rightDataTwo.size(), is(6));
		for(Result result : rightDataTwo){
			String tableName = result.getString(0, "table_name");
			if(tableName.equalsIgnoreCase("sys_dump")){
				assertThat("使用sys做模糊查询得到的表名有sys_dump", tableName, is("sys_dump"));
			}else if(tableName.equalsIgnoreCase("sys_exeinfo")){
				assertThat("使用sys做模糊查询得到的表名有sys_exeinfo", tableName, is("sys_exeinfo"));
			}else if(tableName.equalsIgnoreCase("sys_para")){
				assertThat("使用sys做模糊查询得到的表名有sys_para", tableName, is("sys_para"));
			}else if(tableName.equalsIgnoreCase("sys_recover")){
				assertThat("使用sys做模糊查询得到的表名有sys_recover", tableName, is("sys_recover"));
			}else if(tableName.equalsIgnoreCase("sys_role")){
				assertThat("使用sys做模糊查询得到的表名有sys_role", tableName, is("sys_role"));
			}else{
				assertThat("使用sys做模糊查询得到的表名有不符合期望的情况，表名为" + tableName, true, is(false));
			}
		}
		*/

		//正确数据访问3：构造colSetId为1002，inputString为sys|code的测试数据
		String rightStringThree = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("inputString", "sys|code")
				.post(getActionUrl("getTableInfo")).getBodyString();
		ActionResult rightResultThree = JsonUtil.toObjectSafety(rightStringThree, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultThree.isSuccess(), is(true));
		Result rightDataThree = rightResultThree.getDataForResult();
		assertThat("使用sys|code做模糊查询得到的表信息", rightDataThree.isEmpty(), is(false));
		/*
		List<Result> rightDataThree = rightResultThree.getDataForEntityList(Result.class);
		assertThat("使用sys|code做模糊查询得到的表信息有7条", rightDataThree.size(), is(7));
		for(Result result : rightDataThree){
			String tableName = result.getString(0, "table_name");
			if(tableName.equalsIgnoreCase("sys_dump")){
				assertThat("使用sys|code做模糊查询得到的表名有sys_dump", tableName, is("sys_dump"));
			}else if(tableName.equalsIgnoreCase("sys_exeinfo")){
				assertThat("使用sys|code做模糊查询得到的表名有sys_exeinfo", tableName, is("sys_exeinfo"));
			}else if(tableName.equalsIgnoreCase("sys_para")){
				assertThat("使用sys|code做模糊查询得到的表名有sys_para", tableName, is("sys_para"));
			}else if(tableName.equalsIgnoreCase("sys_recover")){
				assertThat("使用sys|code做模糊查询得到的表名有sys_recover", tableName, is("sys_recover"));
			}else if(tableName.equalsIgnoreCase("sys_role")){
				assertThat("使用sys|code做模糊查询得到的表名有sys_role", tableName, is("sys_role"));
			}else if(tableName.equalsIgnoreCase("code_info")){
				assertThat("使用sys|code做模糊查询得到的表名有code_info", tableName, is("code_info"));
			}else{
				assertThat("使用sys|code做模糊查询得到的表名有不符合期望的情况，表名为" + tableName, true, is(false));
			}
		}
		*/
		//正确数据访问4：构造colSetId为1002，inputString为wzc的测试数据
		String rightStringFour = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("inputString", "wzc")
				.post(getActionUrl("getTableInfo")).getBodyString();
		ActionResult rightResultFour = JsonUtil.toObjectSafety(rightStringFour, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultFour.isSuccess(), is(true));
		Result rightDataFour = rightResultFour.getDataForResult();
		assertThat("使用wzc做模糊查询得到的表信息有0条", rightDataFour.isEmpty(), is(true));
		/*
		List<Result> rightDataFour = rightResultFour.getDataForEntityList(Result.class);
		assertThat("使用wzc做模糊查询得到的表信息有0条", rightDataFour.isEmpty(), is(true));
		*/
		//错误的数据访问1：构造colSetId为1003的测试数据
		long wrongColSetId = 1003L;
		String wrongColSetIdString = new HttpClient()
				.addData("colSetId", wrongColSetId)
				.addData("inputString", "code")
				.post(getActionUrl("getTableInfo")).getBodyString();
		ActionResult wrongColSetIdResult = JsonUtil.toObjectSafety(wrongColSetIdString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongColSetIdResult.isSuccess(), is(false));
	}

	/**
	 * 测试根据数据库设置id得到所有表相关信息
	 * 正确数据访问1：构造正确的colSetId进行测试
	 * 错误的数据访问1：构造错误的colSetId进行测试
	 * 错误的测试用例未达到三组:getAllTableInfo方法只有一个参数
	 * @Param: 无
	 * @return: 无
	 * TODO 由于目前测试用的数据库是我们的测试库，所以表的数量不固定
	 * */
	@Test
	public void getAllTableInfo(){
		//正确数据访问1：构造正确的colSetId进行测试
		String rightString = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.post(getActionUrl("getAllTableInfo")).getBodyString();
		ActionResult rightResult = JsonUtil.toObjectSafety(rightString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResult.isSuccess(), is(true));
		Result rightData = rightResult.getDataForResult();
		assertThat("截止2019.11.27，IP为47.103.83.1的测试库上有74张表",rightData.getRowCount(), is(74));

		//错误的数据访问1：构造错误的colSetId进行测试
		long wrongColSetId = 1003L;
		String wrongString = new HttpClient()
				.addData("colSetId", wrongColSetId)
				.post(getActionUrl("getAllTableInfo")).getBodyString();
		ActionResult wrongResult = JsonUtil.toObjectSafety(wrongString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResult.isSuccess(), is(false));
	}

	/**
	 * 测试根据table_id获取对该表定义的分页SQL
	 * 正确数据访问1：使用设置了分页SQL的tableId进行查询(code_info)
	 * 错误的数据访问1：使用未设置分页SQL的tableId进行查询(sys_user)
	 * 错误的数据访问2：使用不存在的tableId进行查询
	 * 错误的测试用例未达到三组:getPageSQL方法只有一个参数,上述测试用例已经可以覆盖所有可能出现的情况
	 * @Param: 无
	 * @return: 无
	 * */
	@Test
	public void getPageSQL(){
		//正确数据访问1：使用设置了分页SQL的tableId进行查询
		String rightString = new HttpClient()
				.addData("tableId", CODE_INFO_TABLE_ID)
				.post(getActionUrl("getPageSQL")).getBodyString();
		ActionResult rightResult = JsonUtil.toObjectSafety(rightString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResult.isSuccess(), is(true));
		Result rightData = rightResult.getDataForResult();
		assertThat("获得了一条数据", rightData.getRowCount(), is(1));
		assertThat("获得的分页抽取SQL是select * from code_info limit 10", rightData.getString(0, "page_sql"), is("select * from code_info limit 10"));

		//错误的数据访问1：使用未设置分页SQL的tableId进行查询
		String wrongStringOne = new HttpClient()
				.addData("tableId", SYS_USER_TABLE_ID)
				.post(getActionUrl("getPageSQL")).getBodyString();
		ActionResult wrongResultOne = JsonUtil.toObjectSafety(wrongStringOne, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResultOne.isSuccess(), is(true));
		Result wrongDataOne = wrongResultOne.getDataForResult();
		assertThat("获得了一条数据", wrongDataOne.getRowCount(), is(1));
		assertThat("获得的分页抽取SQL是空字符串", wrongDataOne.getString(0, "page_sql"), is(""));

		//错误的数据访问2：使用不存在的tableId进行查询
		long wrongTableId = 12138L;
		String wrongStringTwo = new HttpClient()
				.addData("tableId", wrongTableId)
				.post(getActionUrl("getPageSQL")).getBodyString();
		ActionResult wrongResultTwo = JsonUtil.toObjectSafety(wrongStringTwo, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResultTwo.isSuccess(), is(false));
	}

	/**
	 * 测试并行采集SQL测试功能
	 * 正确数据访问1：构建正确的colSetId和SQL语句
	 * 错误的数据访问1：构建错误的colSetId和正确的SQL语句
	 * 错误的数据访问2：构建正确的colSetId和错误SQL语句
	 * 错误的数据访问3：构建正确的colSetId和正确的SQL语句，但是SQL语句查不到数据
	 *
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void testParallelExtraction(){
		//正确数据访问1：构建正确的colSetId和SQL语句
		String pageSQL = "select * from sys_user limit 2 offset 0";
		String rightString = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("pageSql", pageSQL)
				.post(getActionUrl("testParallelExtraction")).getBodyString();
		ActionResult rightResult = JsonUtil.toObjectSafety(rightString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResult.isSuccess(), is(true));

		//错误的数据访问1：构建错误的colSetId和正确的SQL语句
		long wrongColSetId = 1003L;
		String wrongColSetIdString = new HttpClient()
				.addData("colSetId", wrongColSetId)
				.addData("pageSql", pageSQL)
				.post(getActionUrl("testParallelExtraction")).getBodyString();
		ActionResult wrongColSetIdResult = JsonUtil.toObjectSafety(wrongColSetIdString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongColSetIdResult.isSuccess(), is(false));

		//错误的数据访问2：构建正确的colSetId和错误SQL语句
		String wrongSQL = "select * from sys_user limit 10,20";
		String wrongSQLString = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("pageSql", wrongSQL)
				.post(getActionUrl("testParallelExtraction")).getBodyString();
		ActionResult wrongSQLResult = JsonUtil.toObjectSafety(wrongSQLString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongSQLResult.isSuccess(), is(false));

		//错误的数据访问3：构建正确的colSetId和正确的SQL语句，但是SQL语句查不到数据
		String wrongSQLTwo = "select * from collect_case";
		String wrongSQLStringTwo = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("pageSql", wrongSQLTwo)
				.post(getActionUrl("testParallelExtraction")).getBodyString();
		ActionResult wrongSQLResultTwo = JsonUtil.toObjectSafety(wrongSQLStringTwo, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongSQLResultTwo.isSuccess(), is(false));
	}

	/**
	 * 测试SQL查询设置页面，保存按钮后台方法功能
	 *
	 * 正确数据访问1：构造两条自定义SQL查询设置数据，测试保存功能
	 * 错误的数据访问1：构造两条自定义SQL查询设置数据，第一条数据的表名为空
	 * 错误的数据访问2：构造两条自定义SQL查询设置数据，第二条数据的中文名为空
	 * 错误的数据访问3：构造两条自定义SQL查询设置数据，第一条数据的sql为空
	 * 错误的数据访问4：构造不存在与测试用例模拟数据中的databaseId
	 *
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void saveAllSQL(){
		//正确数据访问1：构造两条自定义SQL查询设置数据，测试保存功能
		List<Table_info> tableInfos = new ArrayList<>();
		for(int i = 1; i <= 2; i++){
			String tableName;
			String tableChName;
			String customizeSQL;
			switch (i) {
				case 1 :
					tableName = "getHalfStructTask";
					tableChName = "获得半结构化采集任务";
					customizeSQL = "SELECT odc_id, object_collect_type, obj_number FROM "+ Object_collect.TableName;
					break;
				case 2 :
					tableName = "getFTPTask";
					tableChName = "获得FTP采集任务";
					customizeSQL = "SELECT ftp_id, ftp_number, ftp_name FROM "+ Ftp_collect.TableName;
					break;
				default:
					tableName = "unexpected_tableName";
					tableChName = "unexpected_tableChName";
					customizeSQL = "unexpected_customizeSQL";
			}
			Table_info tableInfo = new Table_info();
			tableInfo.setTable_name(tableName);
			tableInfo.setTable_ch_name(tableChName);
			tableInfo.setSql(customizeSQL);

			tableInfos.add(tableInfo);
		}
		JSONArray array= JSONArray.parseArray(JSON.toJSONString(tableInfos));
		String rightString = new HttpClient()
				.addData("tableInfoArray", array.toJSONString())
				.addData("databaseId", FIRST_DATABASESET_ID)
				.post(getActionUrl("saveAllSQL")).getBodyString();
		ActionResult rightResult = JsonUtil.toObjectSafety(rightString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResult.isSuccess(), is(true));

		List<Table_info> expectedList;

		//保存成功，验证数据库中的记录是否符合预期
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			expectedList = SqlOperator.queryList(db, Table_info.class, "select * from " + Table_info.TableName + " where database_id = ? AND is_user_defined = ?", FIRST_DATABASESET_ID, IsFlag.Shi.getCode());
			assertThat("保存成功后，table_info表中的用户自定义SQL查询数目应该有2条", expectedList.size(), is(2));
			for(Table_info tableInfo : expectedList){
				if(tableInfo.getTable_name().equalsIgnoreCase("getHalfStructTask")){
					assertThat("保存成功后，自定义SQL查询getHalfStructTask的中文名应该是<获得半结构化采集任务>", tableInfo.getTable_ch_name(), is("获得半结构化采集任务"));
					Result resultOne = SqlOperator.queryResult(db, "select is_get, is_primary_key, colume_name, column_type, colume_ch_name, is_alive, is_new, tc_or from " + Table_column.TableName + " where table_id = ?", tableInfo.getTable_id());
					assertThat("保存成功后，自定义SQL采集的三列数据被保存到了table_column表中", resultOne.getRowCount(), is(3));
					for(int i = 0; i < resultOne.getRowCount(); i++){
						if(resultOne.getString(i, "colume_name").equalsIgnoreCase("odc_id")){
							assertThat("采集列名为odc_id，is_get字段符合预期", resultOne.getString(i, "is_get"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为odc_id，is_primary_key字段符合预期", resultOne.getString(i, "is_primary_key"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为odc_id，column_type字段符合预期", resultOne.getString(i, "column_type").equalsIgnoreCase("int8"), is(true));
							assertThat("采集列名为odc_id，colume_ch_name字段符合预期", resultOne.getString(i, "colume_ch_name"), is("odc_id"));
							assertThat("采集列名为odc_id，is_alive字段符合预期", resultOne.getString(i, "is_alive"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为odc_id，is_new字段符合预期", resultOne.getString(i, "is_new"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为odc_id，tc_or字段符合预期", resultOne.getString(i, "tc_or"), is(columnCleanOrder.toJSONString()));
						}else if(resultOne.getString(i, "colume_name").equalsIgnoreCase("object_collect_type")){
							assertThat("采集列名为object_collect_type，is_get字段符合预期", resultOne.getString(i, "is_get"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为object_collect_type，is_primary_key字段符合预期", resultOne.getString(i, "is_primary_key"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为object_collect_type，column_type字段符合预期", resultOne.getString(i, "column_type").equalsIgnoreCase("bpchar(1)"), is(true));
							assertThat("采集列名为object_collect_type，colume_ch_name字段符合预期", resultOne.getString(i, "colume_ch_name"), is("object_collect_type"));
							assertThat("采集列名为object_collect_type，is_alive字段符合预期", resultOne.getString(i, "is_alive"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为object_collect_type，is_new字段符合预期", resultOne.getString(i, "is_new"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为object_collect_type，tc_or字段符合预期", resultOne.getString(i, "tc_or"), is(columnCleanOrder.toJSONString()));
						}else if(resultOne.getString(i, "colume_name").equalsIgnoreCase("obj_number")){
							assertThat("采集列名为obj_number，is_get字段符合预期", resultOne.getString(i, "is_get"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为obj_number，is_primary_key字段符合预期", resultOne.getString(i, "is_primary_key"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为obj_number，column_type字段符合预期", resultOne.getString(i, "column_type").equalsIgnoreCase("varchar(200)"), is(true));
							assertThat("采集列名为obj_number，colume_ch_name字段符合预期", resultOne.getString(i, "colume_ch_name"), is("obj_number"));
							assertThat("采集列名为obj_number，is_alive字段符合预期", resultOne.getString(i, "is_alive"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为obj_number，is_new字段符合预期", resultOne.getString(i, "is_new"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为obj_number，tc_or字段符合预期", resultOne.getString(i, "tc_or"), is(columnCleanOrder.toJSONString()));
						}else{
							assertThat("出现了不符合预期的情况，采集列名为：" + resultOne.getString(i, "colume_name"), true, is(false));
						}
					}
				}else if(tableInfo.getTable_name().equalsIgnoreCase("getFTPTask")){
					assertThat("保存成功后，自定义SQL查询getFTPTask的中文名应该是<获得FTP采集任务>", tableInfo.getTable_ch_name(), is("获得FTP采集任务"));
					Result resultTwo = SqlOperator.queryResult(db, "select is_get, is_primary_key, colume_name, column_type, colume_ch_name, is_alive, is_new, tc_or from " + Table_column.TableName + " where table_id = ?", tableInfo.getTable_id());
					assertThat("保存成功后，自定义SQL采集的三列数据被保存到了table_column表中", resultTwo.getRowCount(), is(3));
					for(int i = 0; i < resultTwo.getRowCount(); i++){
						if(resultTwo.getString(i, "colume_name").equalsIgnoreCase("ftp_id")){
							assertThat("采集列名为ftp_id，is_get字段符合预期", resultTwo.getString(i, "is_get"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为ftp_id，is_primary_key字段符合预期", resultTwo.getString(i, "is_primary_key"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为ftp_id，column_type字段符合预期", resultTwo.getString(i, "column_type").equalsIgnoreCase("int8"), is(true));
							assertThat("采集列名为ftp_id，colume_ch_name字段符合预期", resultTwo.getString(i, "colume_ch_name"), is("ftp_id"));
							assertThat("采集列名为ftp_id，is_alive字段符合预期", resultTwo.getString(i, "is_alive"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为ftp_id，is_new字段符合预期", resultTwo.getString(i, "is_new"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为ftp_id，tc_or字段符合预期", resultTwo.getString(i, "tc_or"), is(columnCleanOrder.toJSONString()));
						}else if(resultTwo.getString(i, "colume_name").equalsIgnoreCase("ftp_number")){
							assertThat("采集列名为ftp_number，is_get字段符合预期", resultTwo.getString(i, "is_get"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为ftp_number，is_primary_key字段符合预期", resultTwo.getString(i, "is_primary_key"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为ftp_number，column_type字段符合预期", resultTwo.getString(i, "column_type").equalsIgnoreCase("varchar(200)"), is(true));
							assertThat("采集列名为ftp_number，colume_ch_name字段符合预期", resultTwo.getString(i, "colume_ch_name"), is("ftp_number"));
							assertThat("采集列名为ftp_number，is_alive字段符合预期", resultTwo.getString(i, "is_alive"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为ftp_number，is_new字段符合预期", resultTwo.getString(i, "is_new"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为ftp_number，tc_or字段符合预期", resultTwo.getString(i, "tc_or"), is(columnCleanOrder.toJSONString()));
						}else if(resultTwo.getString(i, "colume_name").equalsIgnoreCase("ftp_name")){
							assertThat("采集列名为ftp_name，is_get字段符合预期", resultTwo.getString(i, "is_get"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为ftp_name，is_primary_key字段符合预期", resultTwo.getString(i, "is_primary_key"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为ftp_name，column_type字段符合预期", resultTwo.getString(i, "column_type").equalsIgnoreCase("varchar(512)"), is(true));
							assertThat("采集列名为ftp_name，colume_ch_name字段符合预期", resultTwo.getString(i, "colume_ch_name"), is("ftp_name"));
							assertThat("采集列名为ftp_name，is_alive字段符合预期", resultTwo.getString(i, "is_alive"), is(IsFlag.Shi.getCode()));
							assertThat("采集列名为ftp_name，is_new字段符合预期", resultTwo.getString(i, "is_new"), is(IsFlag.Fou.getCode()));
							assertThat("采集列名为ftp_name，tc_or字段符合预期", resultTwo.getString(i, "tc_or"), is(columnCleanOrder.toJSONString()));
						}else{
							assertThat("出现了不符合预期的情况，采集列名为：" + resultTwo.getString(i, "colume_name"), true, is(false));
						}
					}
				}else{
					assertThat("保存出错，出现了不希望出现的数据，表id为" + tableInfo.getTable_id(), true, is(false));
				}
			}
			//验证完毕后，将自己在本方法中构造的数据删除掉(table_info表)
			int firCount = SqlOperator.execute(db, "delete from " + Table_info.TableName + " WHERE table_name = ?", "getHalfStructTask");
			assertThat("测试完成后，table_name为getHalfStructTask的测试数据被删除了", firCount, is(1));
			int secCount = SqlOperator.execute(db, "delete from " + Table_info.TableName + " WHERE table_name = ?", "getFTPTask");
			assertThat("测试完成后，table_name为getFTPTask的测试数据被删除了", secCount, is(1));

			//验证完毕后，将自己在本方法中构造的数据删除掉(table_column表)
			for(Table_info tableInfo : expectedList){
				SqlOperator.execute(db, "delete from " + Table_column.TableName + " where table_id = ?", tableInfo.getTable_id());
			}
			SqlOperator.commitTransaction(db);
		}

		//错误的数据访问1：构造两条自定义SQL查询设置数据，第一条数据的表名为空
		List<Table_info> errorTableInfosOne = new ArrayList<>();
		for(int i = 1; i <= 2; i++){
			String tableName;
			String tableChName;
			String customizeSQL;
			switch (i) {
				case 1 :
					tableName = null;
					tableChName = "通过数据源ID获得半结构化采集任务";
					customizeSQL = "SELECT fcs.odc_id " +
							"FROM "+ Data_source.TableName +" ds " +
							"JOIN "+ Agent_info.TableName +" ai ON ds.source_id = ai.source_id " +
							"JOIN "+ Object_collect.TableName +" fcs ON ai.agent_id = fcs.agent_id " +
							"WHERE ds.source_id = ? AND fcs.is_sendok = ? AND ds.create_user_id = ?";
					break;
				case 2 :
					tableName = "getFTPTaskBySourceId";
					tableChName = "通过数据源ID获得FTP采集任务";
					customizeSQL = "SELECT fcs.ftp_id " +
							"FROM "+ Data_source.TableName +" ds " +
							"JOIN "+ Agent_info.TableName +" ai ON ds.source_id = ai.source_id " +
							"JOIN "+ Ftp_collect.TableName +" fcs ON ai.agent_id = fcs.agent_id " +
							"WHERE ds.source_id = ? AND fcs.is_sendok = ? AND ds.create_user_id = ? ";
					break;
				default:
					tableName = "unexpected_tableName";
					tableChName = "unexpected_tableChName";
					customizeSQL = "unexpected_customizeSQL";
			}
			Table_info tableInfo = new Table_info();
			tableInfo.setTable_name(tableName);
			tableInfo.setTable_ch_name(tableChName);
			tableInfo.setSql(customizeSQL);

			errorTableInfosOne.add(tableInfo);
		}
		JSONArray errorArrayOne= JSONArray.parseArray(JSON.toJSONString(errorTableInfosOne));
		String errorStringOne = new HttpClient()
				.addData("tableInfoArray", errorArrayOne.toJSONString())
				.addData("databaseId", FIRST_DATABASESET_ID)
				.post(getActionUrl("saveAllSQL")).getBodyString();
		ActionResult errorResultOne = JsonUtil.toObjectSafety(errorStringOne, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(errorResultOne.isSuccess(), is(false));

		//错误的数据访问2：构造两条自定义SQL查询设置数据，第二条数据的中文名为空
		List<Table_info> errorTableInfosTwo = new ArrayList<>();
		for(int i = 1; i <= 2; i++){
			String tableName;
			String tableChName;
			String customizeSQL;
			switch (i) {
				case 1 :
					tableName = "getHalfStructTaskBySourceId";
					tableChName = "通过数据源ID获得半结构化采集任务";
					customizeSQL = "SELECT fcs.odc_id " +
							"FROM "+ Data_source.TableName +" ds " +
							"JOIN "+ Agent_info.TableName +" ai ON ds.source_id = ai.source_id " +
							"JOIN "+ Object_collect.TableName +" fcs ON ai.agent_id = fcs.agent_id " +
							"WHERE ds.source_id = ? AND fcs.is_sendok = ? AND ds.create_user_id = ?";
					break;
				case 2 :
					tableName = "getFTPTaskBySourceId";
					tableChName = null;
					customizeSQL = "SELECT fcs.ftp_id " +
							"FROM "+ Data_source.TableName +" ds " +
							"JOIN "+ Agent_info.TableName +" ai ON ds.source_id = ai.source_id " +
							"JOIN "+ Ftp_collect.TableName +" fcs ON ai.agent_id = fcs.agent_id " +
							"WHERE ds.source_id = ? AND fcs.is_sendok = ? AND ds.create_user_id = ? ";
					break;
				default:
					tableName = "unexpected_tableName";
					tableChName = "unexpected_tableChName";
					customizeSQL = "unexpected_customizeSQL";
			}
			Table_info tableInfo = new Table_info();
			tableInfo.setTable_name(tableName);
			tableInfo.setTable_ch_name(tableChName);
			tableInfo.setSql(customizeSQL);

			errorTableInfosTwo.add(tableInfo);
		}
		JSONArray errorArrayTwo= JSONArray.parseArray(JSON.toJSONString(errorTableInfosTwo));
		String errorStringTwo = new HttpClient()
				.addData("tableInfoArray", errorArrayTwo.toJSONString())
				.addData("databaseId", FIRST_DATABASESET_ID)
				.post(getActionUrl("saveAllSQL")).getBodyString();
		ActionResult errorResultTwo = JsonUtil.toObjectSafety(errorStringTwo, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(errorResultTwo.isSuccess(), is(false));

		//错误的数据访问3：构造两条自定义SQL查询设置数据，第一条数据的sql为空
		List<Table_info> errortableInfoThree = new ArrayList<>();
		for(int i = 1; i <= 2; i++){
			String tableName;
			String tableChName;
			String customizeSQL;
			switch (i) {
				case 1 :
					tableName = "getHalfStructTaskBySourceId";
					tableChName = "通过数据源ID获得半结构化采集任务";
					customizeSQL = null;
					break;
				case 2 :
					tableName = "getFTPTaskBySourceId";
					tableChName = "通过数据源ID获得FTP采集任务";
					customizeSQL = "SELECT fcs.ftp_id " +
							"FROM "+ Data_source.TableName +" ds " +
							"JOIN "+ Agent_info.TableName +" ai ON ds.source_id = ai.source_id " +
							"JOIN "+ Ftp_collect.TableName +" fcs ON ai.agent_id = fcs.agent_id " +
							"WHERE ds.source_id = ? AND fcs.is_sendok = ? AND ds.create_user_id = ? ";
					break;
				default:
					tableName = "unexpected_tableName";
					tableChName = "unexpected_tableChName";
					customizeSQL = "unexpected_customizeSQL";
			}
			Table_info tableInfo = new Table_info();
			tableInfo.setTable_name(tableName);
			tableInfo.setTable_ch_name(tableChName);
			tableInfo.setSql(customizeSQL);

			errortableInfoThree.add(tableInfo);
		}
		JSONArray errorArrayThree= JSONArray.parseArray(JSON.toJSONString(errortableInfoThree));
		String errorStringThree = new HttpClient()
				.addData("tableInfoArray", errorArrayThree.toJSONString())
				.addData("databaseId", FIRST_DATABASESET_ID)
				.post(getActionUrl("saveAllSQL")).getBodyString();
		ActionResult errorResultThree = JsonUtil.toObjectSafety(errorStringThree, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(errorResultThree.isSuccess(), is(false));

		//错误的数据访问4：构造不存在于测试用例模拟数据中的databaseId
		long wrongDatabaseId = 8888888L;
		String errorDatabaseId = new HttpClient()
				.addData("tableInfoArray", array.toJSONString())
				.addData("databaseId", wrongDatabaseId)
				.post(getActionUrl("saveAllSQL")).getBodyString();
		ActionResult errorDatabaseIdResult = JsonUtil.toObjectSafety(errorDatabaseId, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(errorDatabaseIdResult.isSuccess(), is(false));
	}

	/**
	 * 测试SQL查询设置页面操作栏，删除按钮后台方法功能
	 *
	 * 正确数据访问1：模拟删除table_id为7003的自定义SQL采集数据
	 * 错误的数据访问1：模拟删除一个不存在的table_id的自定义SQL采集数据
	 * 错误的测试用例未达到三组: deleteSQLConf()方法只有一个参数
	 *
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void deleteSQLConf(){
		//正确数据访问1：模拟删除table_id为7003的自定义SQL采集数据
		//删除前，确认待删除数据是否存在
		try(DatabaseWrapper db = new DatabaseWrapper()){
			long beforeCount = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName + " where table_id = ?", AGENT_INFO_TABLE_ID).orElseThrow(() -> new BusinessException("必须有且只有一条数据"));
			assertThat("删除前，table_id为" + AGENT_INFO_TABLE_ID + "的数据在table_info表中确实存在", beforeCount, is(1L));
			long beforeAgentInfoCount = SqlOperator.queryNumber(db, "select count(1) from " + Table_column.TableName + " where table_id = ?", AGENT_INFO_TABLE_ID).orElseThrow(() -> new BusinessException("必须有且只有一条数据"));
			assertThat("删除前，table_id为" + AGENT_INFO_TABLE_ID + "的数据在table_column表中确实存在", beforeAgentInfoCount, is(3L));
		}
		//构造正确的数据进行删除
		String rightString = new HttpClient()
				.addData("tableId", AGENT_INFO_TABLE_ID)
				.post(getActionUrl("deleteSQLConf")).getBodyString();
		ActionResult rightResult = JsonUtil.toObjectSafety(rightString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResult.isSuccess(), is(true));
		//删除后，确认数据是否真的被删除了
		try(DatabaseWrapper db = new DatabaseWrapper()){
			long afterCount = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName + " where table_id = ?", AGENT_INFO_TABLE_ID).orElseThrow(() -> new BusinessException("必须有且只有一条数据"));
			assertThat("删除后，table_id为" + AGENT_INFO_TABLE_ID + "的数据不存在了", afterCount, is(0L));
			long afterAgentInfoCount = SqlOperator.queryNumber(db, "select count(1) from " + Table_column.TableName + " where table_id = ?", AGENT_INFO_TABLE_ID).orElseThrow(() -> new BusinessException("必须有且只有一条数据"));
			assertThat("删除前，table_id为" + AGENT_INFO_TABLE_ID + "的数据在table_column表中确实存在", afterAgentInfoCount, is(0L));
		}

		//错误的数据访问1：模拟删除一个不存在的table_id的自定义SQL采集数据
		long errorTableId = 88888L;
		String wrongString = new HttpClient()
				.addData("tableId", errorTableId)
				.post(getActionUrl("deleteSQLConf")).getBodyString();
		ActionResult wrongResult = JsonUtil.toObjectSafety(wrongString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResult.isSuccess(), is(false));
	}

	/**
	 * 测试配置采集表页面，SQL设置按钮后台方法功能，用于回显已经设置的SQL
	 *
	 * 正确数据访问1：构造正确的，且有数据的colSetId(FIRST_DATABASESET_ID)
	 * 正确的数据访问2：构造正确的，但没有数据的colSetId(SECOND_DATABASESET_ID)
	 *
	 * 错误的测试用例未达到三组:getAllSQL()只有一个参数，且只要用户登录，能查到数据就是能查到，查不到就是查不到
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void getAllSQLs(){
		//正确数据访问1：构造正确的，且有数据的colSetId(FIRST_DATABASESET_ID)
		String rightStringOne = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.post(getActionUrl("getAllSQLs")).getBodyString();
		ActionResult rightResultOne = JsonUtil.toObjectSafety(rightStringOne, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultOne.isSuccess(), is(true));
		List<Table_info> rightDataOne = rightResultOne.getDataForEntityList(Table_info.class);
		assertThat("在ID为" + FIRST_DATABASESET_ID + "的数据库采集任务下，有2条自定义采集SQL", rightDataOne.size(), is(2));
		for(Table_info tableInfo : rightDataOne){
			if(tableInfo.getTable_id() == AGENT_INFO_TABLE_ID){
				assertThat("在table_id为" + AGENT_INFO_TABLE_ID + "的自定义采集SQL中，自定义SQL为", tableInfo.getSql(), is("select agent_id, agent_name, agent_type from agent_info where source_id = 1"));
			}else if(tableInfo.getTable_id() == DATA_SOURCE_TABLE_ID){
				assertThat("在table_id为" + DATA_SOURCE_TABLE_ID + "的自定义采集SQL中，自定义SQL为", tableInfo.getSql(), is("select source_id, datasource_number, datasource_name from data_source where source_id = 1"));
			}else{
				assertThat("获取到了不期望获取的数据，该条数据的table_name为" + tableInfo.getTable_name(), true, is(false));
			}
		}

		//正确的数据访问2：构造正确的，但没有数据的colSetId(SECOND_DATABASESET_ID)
		String rightStringTwo = new HttpClient()
				.addData("colSetId", SECOND_DATABASESET_ID)
				.post(getActionUrl("getAllSQLs")).getBodyString();
		ActionResult rightResultTwo = JsonUtil.toObjectSafety(rightStringTwo, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultTwo.isSuccess(), is(true));
		List<Table_info> rightDataTwo = rightResultTwo.getDataForEntityList(Table_info.class);
		assertThat("在ID为" + SECOND_DATABASESET_ID + "的数据库采集任务下，有0条自定义采集SQL", rightDataTwo.size(), is(0));
	}

	/**
	 * 测试配置采集表页面,定义过滤按钮后台方法，用于回显已经对单表定义好的SQL功能
	 *
	 * 正确的数据访问1：模拟回显table_name为sys_user的表定义的对sys_user表的过滤SQL，可以拿到设置的SQL语句select * from sys_user where user_id = 2001
	 * 正确的数据访问2：模拟回显table_name为code_info的过滤SQL，因为测试数据没有设置，所以得到的结果是空字符串
	 * 错误的数据访问1：查询database_id为1002的数据，应该查不到结果，因为在这个数据库采集任务中，没有配置采集表
	 * 错误的测试用例未达到三组:getSingleTableSQL()只有一个参数，且只要用户登录，能查到数据就是能查到，查不到就是查不到
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void getSingleTableSQL(){
		//正确的数据访问1：模拟回显table_name为sys_user的表定义的对sys_user表的过滤SQL，可以拿到设置的SQL语句select * from sys_user where user_id = 2001
		String rightTableNameOne = "sys_user";
		String rightStringOne = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("tableName", rightTableNameOne)
				.post(getActionUrl("getSingleTableSQL")).getBodyString();
		ActionResult rightResultOne = JsonUtil.toObjectSafety(rightStringOne, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultOne.isSuccess(), is(true));
		Result rightDataOne = rightResultOne.getDataForResult();
		assertThat("使用database_id为" + FIRST_DATABASESET_ID + "和table_name为" + rightTableNameOne + "得到1条数据", rightDataOne.getRowCount(), is(1));
		assertThat("回显table_name为sys_user表定义的过滤SQL", rightDataOne.getString(0, "sql"), is("select * from sys_user where user_id = 9997"));

		//正确的数据访问2：模拟回显table_name为code_info的过滤SQL，因为测试数据没有设置，所以拿不到
		String rightTableNameTwo = "code_info";
		String rightStringTwo = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("tableName", rightTableNameTwo)
				.post(getActionUrl("getSingleTableSQL")).getBodyString();
		ActionResult rightResultTwo = JsonUtil.toObjectSafety(rightStringTwo, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultTwo.isSuccess(), is(true));
		Result rightDataTwo = rightResultTwo.getDataForResult();
		assertThat("使用database_id为" + FIRST_DATABASESET_ID + "和table_name为" + rightTableNameTwo + "得到1条数据", rightDataTwo.getRowCount(), is(1));
		assertThat("code_info表没有定义过滤SQL",  rightDataTwo.getString(0, "sql"), is(""));

		//错误的数据访问1：查询database_id为1002的数据，应该查不到结果，因为在这个数据库采集任务中，没有配置采集表
		String wrongString = new HttpClient()
				.addData("colSetId", SECOND_DATABASESET_ID)
				.addData("tableName", rightTableNameTwo)
				.post(getActionUrl("getSingleTableSQL")).getBodyString();
		ActionResult wrongResult = JsonUtil.toObjectSafety(wrongString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResult.isSuccess(), is(true));
		Result wrongData = wrongResult.getDataForResult();
		assertThat("使用database_id为" + SECOND_DATABASESET_ID + "和table_name为" + rightTableNameTwo + "得到0条数据", wrongData.getRowCount(), is(0));
	}

	/**
	 * 测试配置采集表页面,选择列按钮后台功能
	 * 正确数据访问1：构造tableName为code_info，tableId为7002，colSetId为1001的测试数据
	 * 正确数据访问2：构造tableName为ftp_collect，tableId为999999，colSetId为1001的测试数据
	 * 错误的数据访问1：构造tableName为ftp_collect，tableId为999999，colSetId为1003的测试数据
	 * 错误的数据访问2：构造tableName为wzc_collect，tableId为999999，colSetId为1001的测试数据
	 * 错误的测试用例未达到三组:以上所有测试用例已经可以覆盖处理逻辑中所有的分支和错误处理了
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void getColumnInfo(){
		//正确数据访问1：构造tableName为code_info，tableId为7002，colSetId为1001的测试数据
		String tableNameOne = "code_info";
		String rightStringOne = new HttpClient()
				.addData("tableName", tableNameOne)
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("tableId", CODE_INFO_TABLE_ID)
				.post(getActionUrl("getColumnInfo")).getBodyString();
		ActionResult rightResultOne = JsonUtil.toObjectSafety(rightStringOne, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultOne.isSuccess(), is(true));
		Map<Object, Object> rightDataOne = rightResultOne.getDataForMap();
		for(Map.Entry<Object, Object> entry : rightDataOne.entrySet()){
			String key = (String) entry.getKey();
			if(key.equalsIgnoreCase("tableName")){
				assertThat("返回的结果中，有一对Entry的key为tableName", key, is("tableName"));
				assertThat("返回的结果中，key为tableName的Entry，value为code_info", entry.getValue(), is(tableNameOne));
			}else if(key.equalsIgnoreCase("columnInfo")){
				assertThat("返回的结果中，有一对Entry的key为columnInfo", key, is("columnInfo"));
				List<Table_column> tableColumns = (List<Table_column>) entry.getValue();
				assertThat("返回的结果中，key为columnInfo的Entry，value为List<Table_column>,code_info表中有5列", tableColumns.size(), is(5));
			}else{
				assertThat("返回的结果中，出现了不期望出现的内容", true, is(false));
			}
		}

		//正确数据访问2：构造tableName为ftp_collect，tableId为999999，colSetId为1001的测试数据
		String tableNameTwo = "ftp_collect";
		String rightStringTwo = new HttpClient()
				.addData("tableName", tableNameTwo)
				.addData("colSetId", FIRST_DATABASESET_ID)
				.post(getActionUrl("getColumnInfo")).getBodyString();
		ActionResult rightResultTwo = JsonUtil.toObjectSafety(rightStringTwo, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultTwo.isSuccess(), is(true));
		Map<Object, Object> rightDataTwo = rightResultTwo.getDataForMap();
		for(Map.Entry<Object, Object> entry : rightDataTwo.entrySet()){
			String key = (String) entry.getKey();
			if(key.equalsIgnoreCase("tableName")){
				assertThat("返回的结果中，有一对Entry的key为tableName", key, is("tableName"));
				assertThat("返回的结果中，key为tableName的Entry，value为ftp_collect", entry.getValue(), is(tableNameTwo));
			}else if(key.equalsIgnoreCase("columnInfo")){
				assertThat("返回的结果中，有一对Entry的key为columnInfo", key, is("columnInfo"));
				List<Table_column> tableColumns = (List<Table_column>) entry.getValue();
				assertThat("返回的结果中，key为columnInfo的Entry，value为List<Table_column>,ftp_collect表中有24列", tableColumns.size(), is(24));
			}else{
				assertThat("返回的结果中，出现了不期望出现的内容", true, is(false));
			}
		}

		//错误的数据访问1：构造tableName为ftp_collect，tableId为999999，colSetId为1003的测试数据
		long wrongColSetId = 1003L;
		String wrongColSetIdString = new HttpClient()
				.addData("tableName", tableNameTwo)
				.addData("colSetId", wrongColSetId)
				.addData("collColumnArray", "")
				.addData("columnSortArray", "")
				.post(getActionUrl("getColumnInfo")).getBodyString();
		ActionResult wrongColSetIdResult = JsonUtil.toObjectSafety(wrongColSetIdString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongColSetIdResult.isSuccess(), is(false));

		//错误的数据访问2：构造tableName为wzc_collect，tableId为999999，colSetId为1001的测试数据
		String notExistTableName = "wzc_collect";
		String wrongTableNameString = new HttpClient()
				.addData("tableName", notExistTableName)
				.addData("colSetId", FIRST_DATABASESET_ID)
				.post(getActionUrl("getColumnInfo")).getBodyString();
		ActionResult wrongTableNameResult = JsonUtil.toObjectSafety(wrongTableNameString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongTableNameResult.isSuccess(), is(true));
		Map<Object, Object> wrongTableNameData = wrongTableNameResult.getDataForMap();
		for(Map.Entry<Object, Object> entry : wrongTableNameData.entrySet()){
			String key = (String) entry.getKey();
			if(key.equalsIgnoreCase("tableName")){
				assertThat("返回的结果中，有一对Entry的key为tableName", key, is("tableName"));
				assertThat("返回的结果中，key为tableName的Entry，value为wzc_collect", entry.getValue(), is(notExistTableName));
			}else if(key.equalsIgnoreCase("columnInfo")){
				assertThat("返回的结果中，有一对Entry的key为columnInfo", key, is("columnInfo"));
				List<Table_column> tableColumns = (List<Table_column>) entry.getValue();
				assertThat("返回的结果中，key为columnInfo的Entry，value为List<Table_column>,没有wzc_collect这张表", tableColumns.isEmpty(), is(true));
			}else{
				assertThat("返回的结果中，出现了不期望出现的内容", true, is(false));
			}
		}
	}

	/**
	 * 测试保存单个表的采集信息功能
	 * 正确数据访问1：在database_id为1001的数据库采集任务下构造新增采集ftp_collect表的数据，不选择采集列和列排序，设置并行抽取SQL为select * from ftp_collect limit 10;(需要和agent进行交互获取该表的字段)
	 * 正确数据访问2：在database_id为1001的数据库采集任务下构造新增采集object_collect表的数据，选择采集列和列排序,不设置并行抽取(不需要和agent进行交互)
	 * 正确数据访问3：在database_id为1001的数据库采集任务下构造修改采集code_info表的数据，选择采集列和列排序，不设置并行抽取，
	 * 注意，由于给code_info表构造了data_extraction_def表、table_clean表、table_srorage_info表、column_merge表、table_column表信息
	 * 因此对这个表的采集数据进行修改，要断言修改成功后这5张表的数据是否都被修改了。(不需要和agent进行交互)
	 * 正确数据访问4：在database_id为1001的数据库采集任务下构造新增采集ftp_collect表和object_collect表的数据，不选择采集列和列排序，不设置并行抽取(需要和agent交互)
	 * 错误的数据访问1：构造缺少表名的采集数据
	 * 错误的数据访问2：构造缺少表中文名的采集数据
	 * 错误的数据访问3：构造设置了并行抽取，但没有设置并行抽取SQL的访问方式
	 * 错误的数据访问4：构造在不存在的数据库采集任务中保存采集ftp_collect表数据
	 * 错误的数据访问5：构造tableInfoString参数是空字符串的情况
	 * 错误的数据访问6：构造collTbConfParamString参数是空字符串的情况
	 * 错误的数据访问7：构造tableInfoString和collTbConfParamString解析成的list集合大小不同的情况
	 *
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void saveCollTbInfoOne(){
		List<Table_info> tableInfos = new ArrayList<>();
		List<CollTbConfParam> tbConfParams = new ArrayList<>();

		//正确数据访问1：在database_id为1001的数据库采集任务下构造新增采集ftp_collect表的数据，不选择采集列和列排序，设置并行抽取SQL为select * from ftp_collect limit 10;(需要和agent进行交互获取该表的字段)
		try(DatabaseWrapper db = new DatabaseWrapper()){
			//在新增前，查询数据库，table_info表中应该没有采集ftp_collect表的信息
			long count = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName + " where table_name = ?", "ftp_collect").orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			assertThat("在新增前，查询数据库，table_info表中应该没有采集ftp_collect表的信息", count, is(0L));
		}

		Table_info FTPInfo = new Table_info();
		FTPInfo.setTable_name("ftp_collect");
		FTPInfo.setTable_ch_name("ftp采集设置表");
		FTPInfo.setDatabase_id(FIRST_DATABASESET_ID);
		FTPInfo.setIs_parallel(IsFlag.Shi.getCode());
		FTPInfo.setPage_sql("select * from ftp_collect limit 10;");

		tableInfos.add(FTPInfo);

		CollTbConfParam FTPParam = new CollTbConfParam();
		FTPParam.setCollColumnString("");
		FTPParam.setColumnSortString("");

		tbConfParams.add(FTPParam);

		String rightStringOne = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult rightResultOne = JsonUtil.toObjectSafety(rightStringOne, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultOne.isSuccess(), is(true));
		Integer returnValue = (Integer) rightResultOne.getData();
		assertThat(returnValue == FIRST_DATABASESET_ID, is(true));

		try(DatabaseWrapper db = new DatabaseWrapper()){
			//新增成功后，断言数据库中的数据是否符合期望，如果符合期望，则删除本次新增带来的数据
			Result afterTableInfo = SqlOperator.queryResult(db, "select * from " + Table_info.TableName + " where table_name = ?", "ftp_collect");
			assertThat("<正确的测试用例1>执行成功后，table_info表中出现了采集ftp_collect表的配置", afterTableInfo.getRowCount(), is(1));
			assertThat("<正确的测试用例1>执行成功后，采集ftp_collect表配置，<清洗顺序>符合期望", afterTableInfo.getString(0, "ti_or"), is(tableCleanOrder.toJSONString()));
			assertThat("<正确的测试用例1>执行成功后，采集ftp_collect表配置，<是否使用MD5>符合期望", afterTableInfo.getString(0, "is_md5"), is(IsFlag.Shi.getCode()));
			assertThat("<正确的测试用例1>执行成功后，采集ftp_collect表配置，<是否仅登记>符合期望", afterTableInfo.getString(0, "is_register"), is(IsFlag.Fou.getCode()));
			assertThat("<正确的测试用例1>执行成功后，采集ftp_collect表配置，<是否并行抽取>符合期望", afterTableInfo.getString(0, "is_parallel"), is(IsFlag.Shi.getCode()));
			assertThat("<正确的测试用例1>执行成功后，采集ftp_collect表配置，<分页SQL>符合期望", afterTableInfo.getString(0, "page_sql"), is("select * from ftp_collect limit 10;"));

			Result afterTableColumn = SqlOperator.queryResult(db, "select * from " + Table_column.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			assertThat("<正确的测试用例1>执行成功后，table_column表中有关ftp_collect表的列应该有<24>列", afterTableColumn.getRowCount(), is(24));
			for(int i = 0; i < afterTableColumn.getRowCount(); i++){
				if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_id")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_id>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("int8"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_number")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_number>字段的类型为<varchar(200)>", afterTableColumn.getString(i, "column_type"), is("varchar(200)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_name")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_name>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("start_date")){
					assertThat("<正确的测试用例1>执行成功后, <start_date>字段的类型为<bpchar(8)>", afterTableColumn.getString(i, "column_type"), is("bpchar(8)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("end_date")){
					assertThat("<正确的测试用例1>执行成功后, <end_date>字段的类型为<bpchar(8)>", afterTableColumn.getString(i, "column_type"), is("bpchar(8)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_ip")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_ip>字段的类型为<varchar(50)>", afterTableColumn.getString(i, "column_type"), is("varchar(50)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_port")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_port>字段的类型为<varchar(10)>", afterTableColumn.getString(i, "column_type"), is("varchar(10)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_username")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_username>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_password")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_password>字段的类型为<varchar(100)>", afterTableColumn.getString(i, "column_type"), is("varchar(100)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_dir")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_dir>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("local_path")){
					assertThat("<正确的测试用例1>执行成功后, <local_path>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_rule_path")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_rule_path>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("child_file_path")){
					assertThat("<正确的测试用例1>执行成功后, <child_file_path>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("child_time")){
					assertThat("<正确的测试用例1>执行成功后, <child_time>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("file_suffix")){
					assertThat("<正确的测试用例1>执行成功后, <file_suffix>字段的类型为<varchar(200)>", afterTableColumn.getString(i, "column_type"), is("varchar(200)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_model")){
					assertThat("<正确的测试用例1>执行成功后, <ftp_model>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("run_way")){
					assertThat("<正确的测试用例1>执行成功后, <run_way>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("remark")){
					assertThat("<正确的测试用例1>执行成功后, <remark>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("is_sendok")){
					assertThat("<正确的测试用例1>执行成功后, <is_sendok>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("is_unzip")){
					assertThat("<正确的测试用例1>执行成功后, <is_unzip>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("reduce_type")){
					assertThat("<正确的测试用例1>执行成功后, <reduce_type>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("is_read_realtime")){
					assertThat("<正确的测试用例1>执行成功后, <is_read_realtime>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("realtime_interval")){
					assertThat("<正确的测试用例1>执行成功后, <realtime_interval>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("int8"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("agent_id")){
					assertThat("<正确的测试用例1>执行成功后, <agent_id>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("int8"));
				}else{
					assertThat("<正确的测试用例1>执行完成之后，采集ftp_collect表的所有字段，出现了不符合期望的字段，字段名为 : " + afterTableColumn.getString(i, "colume_name"), true, is(false));
				}
			}

			//以上断言都测试成功后，删除<正确数据访问1>生成的数据
			int tableCount = SqlOperator.execute(db, "delete from " + Table_info.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			int columnCount = SqlOperator.execute(db, "delete from " + Table_column.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			assertThat("删除<正确数据访问1>生成的数据成功<table_info>", tableCount, is(1));
			assertThat("删除<正确数据访问1>生成的数据成功<table_column>", columnCount, is(24));

			SqlOperator.commitTransaction(db);
		}
	}

	/*
	 * 正确数据访问2：
	 * 在database_id为1001的数据库采集任务下构造新增采集object_collect表的数据，选择采集列和列排序,不设置并行抽取(不需要和agent进行交互)
	 * 模拟采集object_collect表的odc_id、object_collect_type、obj_number三列
	 * */
	@Test
	public void saveCollTbInfoTwo(){
		List<Table_info> tableInfos = new ArrayList<>();
		List<CollTbConfParam> tbConfParams = new ArrayList<>();

		try(DatabaseWrapper db = new DatabaseWrapper()){
			//在新增前，查询数据库，table_info表中应该没有采集object_collect表的信息
			long count = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName + " where table_name = ?", "object_collect").orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			assertThat("在新增前，查询数据库，table_info表中应该没有采集object_collect表的信息", count, is(0L));
		}
		Table_info objInfo = new Table_info();
		objInfo.setTable_name("object_collect");
		objInfo.setTable_ch_name("半结构化文件采集设置表");
		objInfo.setDatabase_id(FIRST_DATABASESET_ID);
		objInfo.setIs_parallel(IsFlag.Fou.getCode());

		tableInfos.add(objInfo);

		List<Table_column> objColumn = new ArrayList<>();
		for(int i = 0; i < 3; i++){
			String columnName;
			String columnChName;
			String columnType;
			switch (i) {
				case 0 :
					columnName = "odc_id";
					columnChName = "对象采集id";
					columnType = "int8";
					break;
				case 1 :
					columnName = "object_collect_type";
					columnChName = "对象采集方式";
					columnType = "bpchar(1)";
					break;
				case 2 :
					columnName = "obj_number";
					columnChName = "对象采集设置编号";
					columnType = "varchar(200)";
					break;
				default:
					columnName = "unexpected_columnName";
					columnChName = "unexpected_columnChName";
					columnType = "unexpected_columnType";
			}
			Table_column tableColumn = new Table_column();
			tableColumn.setColume_name(columnName);
			tableColumn.setColume_ch_name(columnChName);
			tableColumn.setColumn_type(columnType);

			objColumn.add(tableColumn);
		}

		JSONArray objSort = new JSONArray();
		for(int i = 0; i < 3; i++){
			String columnName;
			int sort;
			switch (i) {
				case 0 :
					columnName = "odc_id";
					sort = 1;
					break;
				case 1 :
					columnName = "object_collect_type";
					sort = 2;
					break;
				case 2 :
					columnName = "obj_number";
					sort = 3;
					break;
				default:
					columnName = "unexpected_columnName";
					sort = (int)UNEXPECTED_ID;
			}
			JSONObject object = new JSONObject();
			object.put("columnName", columnName);
			object.put("sort", sort);
			objSort.add(object);
		}

		CollTbConfParam objParam = new CollTbConfParam();
		objParam.setCollColumnString(JSON.toJSONString(objColumn));
		objParam.setColumnSortString(objSort.toJSONString());

		tbConfParams.add(objParam);

		String rightStringTwo = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult rightResultTwo = JsonUtil.toObjectSafety(rightStringTwo, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultTwo.isSuccess(), is(true));
		Integer returnValueTwo = (Integer) rightResultTwo.getData();
		assertThat(returnValueTwo == FIRST_DATABASESET_ID, is(true));

		try(DatabaseWrapper db = new DatabaseWrapper()){
			//新增成功后，断言数据库中的数据是否符合期望，如果符合期望，则删除本次新增带来的数据
			Result afterTableInfo = SqlOperator.queryResult(db, "select * from " + Table_info.TableName + " where table_name = ?", "object_collect");
			assertThat("<正确的测试用例2>执行成功后，table_info表中出现了采集object_collect表的配置", afterTableInfo.getRowCount(), is(1));
			assertThat("<正确的测试用例2>执行成功后，采集object_collect表配置，<清洗顺序>符合期望", afterTableInfo.getString(0, "ti_or"), is(tableCleanOrder.toJSONString()));
			assertThat("<正确的测试用例2>执行成功后，采集object_collect表配置，<是否使用MD5>符合期望", afterTableInfo.getString(0, "is_md5"), is(IsFlag.Shi.getCode()));
			assertThat("<正确的测试用例2>执行成功后，采集object_collect表配置，<是否仅登记>符合期望", afterTableInfo.getString(0, "is_register"), is(IsFlag.Fou.getCode()));
			assertThat("<正确的测试用例2>执行成功后，采集object_collect表配置，<是否并行抽取>符合期望", afterTableInfo.getString(0, "is_parallel"), is(IsFlag.Fou.getCode()));

			Result afterTableColumn = SqlOperator.queryResult(db, "select * from " + Table_column.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			assertThat("<正确的测试用例2>执行成功后，table_column表中有关object_collect表的列应该有<24>列", afterTableColumn.getRowCount(), is(3));
			for(int i = 0; i < afterTableColumn.getRowCount(); i++){
				if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("odc_id")){
					assertThat("<正确的测试用例2>执行成功后, <odc_id>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("int8"));
					assertThat("<正确的测试用例2>执行成功后, <odc_id>字段的采集顺序为<1>", afterTableColumn.getString(i, "remark"), is("1"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("object_collect_type")){
					assertThat("<正确的测试用例2>执行成功后, <object_collect_type>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
					assertThat("<正确的测试用例2>执行成功后, <object_collect_type>字段的采集顺序为<2>", afterTableColumn.getString(i, "remark"), is("2"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("obj_number")){
					assertThat("<正确的测试用例2>执行成功后, <obj_number>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("varchar(200)"));
					assertThat("<正确的测试用例2>执行成功后, <obj_number>字段的采集顺序为<3>", afterTableColumn.getString(i, "remark"), is("3"));
				}else{
					assertThat("<正确的测试用例2>执行成功后, 再次查询table_column表，出现了不符合期望的情况，表名为：" + afterTableColumn.getString(i, "colume_name"), true, is(false));
				}
			}

			//以上断言都测试成功后，删除<正确数据访问1>生成的数据
			int tableCount = SqlOperator.execute(db, "delete from " + Table_info.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			int columnCount = SqlOperator.execute(db, "delete from " + Table_column.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			assertThat("删除<正确数据访问2>生成的数据成功<table_info>", tableCount, is(1));
			assertThat("删除<正确数据访问2>生成的数据成功<table_column>", columnCount, is(3));

			SqlOperator.commitTransaction(db);
		}
	}

	/*
	 * 正确数据访问3：在database_id为1001的数据库采集任务下构造修改采集code_info表的数据，选择采集列和列排序，不设置并行抽取，
	 * 注意，由于给code_info表构造了data_extraction_def表、table_clean表、table_srorage_info表、column_merge表、table_column表信息
	 * 因此对这个表的采集数据进行修改，要断言修改成功后这5张表的数据是否都被修改了。(不需要和agent进行交互)
	 * */
	@Test
	public void saveCollTbInfoThree(){
		List<Table_info> tableInfos = new ArrayList<>();
		List<CollTbConfParam> tbConfParams = new ArrayList<>();

		//注意：由于这里是对code_info表的采集字段进行修改，所以必须要传table_id
		Table_info codeInfo = new Table_info();
		codeInfo.setTable_id(CODE_INFO_TABLE_ID);
		codeInfo.setTable_name("code_info");
		codeInfo.setTable_ch_name("代码信息表");
		codeInfo.setDatabase_id(FIRST_DATABASESET_ID);
		codeInfo.setIs_parallel(IsFlag.Fou.getCode());

		tableInfos.add(codeInfo);

		List<Table_column> codeColumn = new ArrayList<>();
		for(int i = 0; i < 3; i++){
			String columnName;
			String columnChName;
			String columnType;
			switch (i) {
				case 0 :
					columnName = "ci_sp_code";
					columnType = "varchar(200)";
					columnChName = "ci_sp_code";
					break;
				case 1 :
					columnName = "ci_sp_class";
					columnType = "varchar(200)";
					columnChName = "ci_sp_class";
					break;
				case 2 :
					columnName = "ci_sp_classname";
					columnType = "varchar(100)";
					columnChName = "ci_sp_classname";
					break;
				default:
					columnName = "unexpected_columnName";
					columnChName = "unexpected_columnChName";
					columnType = "unexpected_columnType";
			}
			Table_column tableColumn = new Table_column();
			tableColumn.setColume_name(columnName);
			tableColumn.setColume_ch_name(columnChName);
			tableColumn.setColumn_type(columnType);

			codeColumn.add(tableColumn);
		}

		JSONArray codeSort = new JSONArray();
		for(int i = 0; i < 3; i++){
			String columnName;
			int sort;
			switch (i) {
				case 0 :
					columnName = "ci_sp_code";
					sort = 1;
					break;
				case 1 :
					columnName = "ci_sp_class";
					sort = 2;
					break;
				case 2 :
					columnName = "ci_sp_classname";
					sort = 3;
					break;
				default:
					columnName = "unexpected_columnName";
					sort = (int)UNEXPECTED_ID;
			}
			JSONObject object = new JSONObject();
			object.put("columnName", columnName);
			object.put("sort", sort);

			codeSort.add(object);
		}

		CollTbConfParam codeParam = new CollTbConfParam();
		codeParam.setCollColumnString(JSON.toJSONString(codeColumn));
		codeParam.setColumnSortString(codeSort.toJSONString());

		tbConfParams.add(codeParam);

		String rightStringThree = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult rightResultThree = JsonUtil.toObjectSafety(rightStringThree, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultThree.isSuccess(), is(true));
		Integer returnValueThree = (Integer) rightResultThree.getData();
		assertThat(returnValueThree == FIRST_DATABASESET_ID, is(true));

		try(DatabaseWrapper db = new DatabaseWrapper()){
			//断言table_info表中的内容是否符合期望
			long count = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName + " where table_id = ?", CODE_INFO_TABLE_ID).orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			Result tableInfo = SqlOperator.queryResult(db, "select * from " + Table_info.TableName + " where table_name = ?", "code_info");
			assertThat("code_info表在table_info表中有且只有一条数据，但是该条数据的id和构造初始化数据时不一致，导致这个事情的原因是保存操作全部都是按照先删除后新增的逻辑执行的", count, is(0L));
			assertThat("code_info表在table_info表中有且只有一条数据，但是该条数据的id和构造初始化数据时不一致，导致这个事情的原因是保存操作全部都是按照先删除后新增的逻辑执行的", tableInfo.getRowCount(), is(1));

			long tableId = tableInfo.getLong(0, "table_id");

			//断言table_column表中的内容是否符合期望
			long tbColCount = SqlOperator.queryNumber(db, "select count(1) from " + Table_column.TableName + " where table_id = ?", CODE_INFO_TABLE_ID).orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			Result tableColumn = SqlOperator.queryResult(db, "select * from " + Table_column.TableName + " where table_id = ?", tableId);
			assertThat("code_info表的采集列在table_column表中有数据，数据有三条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在table_column表中已经查不到数据了", tbColCount, is(0L));
			assertThat("code_info表的采集列在table_column表中有数据，数据有三条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在table_column表中已经查不到数据了", tableColumn.getRowCount(), is(3));
			for(int i = 0; i < tableColumn.getRowCount(); i++){
				if(tableColumn.getString(i, "colume_name").equalsIgnoreCase("ci_sp_code")){
					assertThat("采集列名为<ci_sp_code>,该列的数据类型为<varchar(200)>", tableColumn.getString(i, "column_type"), is("varchar(200)"));
				}else if(tableColumn.getString(i, "colume_name").equalsIgnoreCase("ci_sp_class")){
					assertThat("采集列名为<ci_sp_class>,该列的数据类型为<varchar(200)>", tableColumn.getString(i, "column_type"), is("varchar(200)"));
				}else if(tableColumn.getString(i, "colume_name").equalsIgnoreCase("ci_sp_classname")){
					assertThat("采集列名为<ci_sp_classname>,该列的数据类型为<varchar(100)>", tableColumn.getString(i, "column_type"), is("varchar(100)"));
				}else {
					assertThat("设置采集code_info表的前三列，但是出现了不符合期望的列，列名为 : " + tableColumn.getString(i, "colume_name"), true, is(false));
				}
			}
			//断言data_extraction_def表中的内容是否符合期望
			long defCount = SqlOperator.queryNumber(db, "select count(1) from " + Data_extraction_def.TableName + " where table_id = ?", CODE_INFO_TABLE_ID).orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			Result dataExtractionDef = SqlOperator.queryResult(db, "select * from " + Data_extraction_def.TableName + " where table_id = ?", tableId);
			assertThat("code_info表的数据抽取定义信息在Data_extraction_def表中有数据，数据有1条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在data_extraction_def表中已经查不到数据了", defCount, is(0L));
			assertThat("code_info表的数据抽取定义信息在Data_extraction_def表中有数据，数据有1条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在data_extraction_def表中已经查不到数据了", dataExtractionDef.getRowCount(), is(1));
			assertThat("code_info表的数据抽取定义信息在Data_extraction_def表中有数据，<数据抽取方式>符合预期", dataExtractionDef.getString(0, "data_extract_type"), is(DataExtractType.ShuJuChouQuJiRuKu.getCode()));
			assertThat("code_info表的数据抽取定义信息在Data_extraction_def表中有数据，<是否表头>符合预期", dataExtractionDef.getString(0, "is_header"), is(IsFlag.Fou.getCode()));
			assertThat("code_info表的数据抽取定义信息在Data_extraction_def表中有数据，<落地文件编码>符合预期", dataExtractionDef.getString(0, "database_code"), is(DataBaseCode.UTF_8.getCode()));
			assertThat("code_info表的数据抽取定义信息在Data_extraction_def表中有数据，<落地文件格式>符合预期", dataExtractionDef.getString(0, "dbfile_format"), is(FileFormat.PARQUET.getCode()));
			assertThat("code_info表的数据抽取定义信息在Data_extraction_def表中有数据，<落地存储目录>符合预期", dataExtractionDef.getString(0, "plane_url"), is("/home/hyshf"));

			//断言table_clean表中的内容是否符合期望
			long cleanCount = SqlOperator.queryNumber(db, "select count(1) from " + Table_clean.TableName + " where table_id = ?", CODE_INFO_TABLE_ID).orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			Result tableClean = SqlOperator.queryResult(db, "select * from " + Table_clean.TableName + " where table_id = ?", tableId);
			assertThat("code_info表的表清洗信息在Table_clean表中有数据，数据有2条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在Table_clean表中已经查不到数据了", cleanCount, is(0L));
			assertThat("code_info表的表清洗信息在Table_clean表中有数据，数据有2条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在Table_clean表中已经查不到数据了", tableClean.getRowCount(), is(2));
			for(int i = 0; i < tableClean.getRowCount(); i++){
				if(tableClean.getString(i, "clean_type").equalsIgnoreCase(CleanType.ZiFuTiHuan.getCode())){
					assertThat("字符替换，原字符串符合预期", tableClean.getString(i, "field"), is("abc"));
					assertThat("字符替换，目标字符串符合预期", tableClean.getString(i, "replace_feild"), is("def"));
				}else if(tableClean.getString(i, "clean_type").equalsIgnoreCase(CleanType.ZiFuBuQi.getCode())){
					assertThat("字符补齐，补齐长度符合预期", tableClean.getInt(i, "filling_length"), is(6));
					assertThat("字符补齐，补齐字符串符合预期", tableClean.getString(i, "character_filling"), is("beyond"));
				}else{
					assertThat("修改成功后，code_info表在table_clean表中定义的表清洗方式出现了不符合预期的情况,清洗方式为 : " + tableClean.getString(i, "clean_type"), true, is(false));
				}
			}

			//断言table_srorage_info表中的内容是否符合期望
			long storageCount = SqlOperator.queryNumber(db, "select count(1) from " + Table_storage_info.TableName + " where table_id = ?", CODE_INFO_TABLE_ID).orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			Result tableStorage = SqlOperator.queryResult(db, "select * from " + Table_storage_info.TableName + " where table_id = ?", tableId);
			assertThat("code_info表的表存储信息在Table_storage_info表中有数据，数据有1条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在Table_storage_info表中已经查不到数据了", storageCount, is(0L));
			assertThat("code_info表的表存储信息在Table_storage_info表中有数据，数据有1条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在Table_storage_info表中已经查不到数据了", tableStorage.getRowCount(), is(1));
			assertThat("code_info表的表存储信息在Table_storage_info表中有数据，数据有1条，存储格式为<定长>", tableStorage.getString(0, "file_format"), is(FileFormat.DingChang.getCode()));
			assertThat("code_info表的表存储信息在Table_storage_info表中有数据，数据有1条，进数方式为<追加>", tableStorage.getString(0, "storage_type"), is(StorageType.ZhuiJia.getCode()));


			//断言column_merge表中的内容是否符合期望
			long mergeCount = SqlOperator.queryNumber(db, "select count(1) from " + Column_merge.TableName + " where table_id = ?", CODE_INFO_TABLE_ID).orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			Result columnMerge = SqlOperator.queryResult(db, "select * from " + Column_merge.TableName + " where table_id = ?", tableId);
			assertThat("code_info表的列合并信息在column_merge表中有数据，数据有1条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在column_merge表中已经查不到数据了", mergeCount, is(0L));
			assertThat("code_info表的列合并信息在column_merge表中有数据，数据有1条，并且用构造初始化数据是使用的CODE_INFO_TABLE_ID在column_merge表中已经查不到数据了", columnMerge.getRowCount(), is(1));
			assertThat("ode_info表的列合并信息在column_merge表中有数据，数据有1条,<要合并的字段>符合预期", columnMerge.getString(0, "col_name"), is("ci_sp_classname_name"));
			assertThat("ode_info表的列合并信息在column_merge表中有数据，数据有1条,<要合并的字段>符合预期", columnMerge.getString(0, "old_name"), is("ci_sp_classname|ci_sp_name"));

			//删除测试数据
			SqlOperator.execute(db, "delete from " + Table_column.TableName + " where table_id = ? ", tableId);
			SqlOperator.execute(db, "delete from " + Table_storage_info.TableName + " where table_id = ? ", tableId);
			SqlOperator.execute(db, "delete from " + Table_clean.TableName + " where table_id = ? ", tableId);
			SqlOperator.execute(db, "delete from " + Column_merge.TableName + " where table_id = ? ", tableId);
			SqlOperator.execute(db, "delete from " + Data_extraction_def.TableName + " where table_id = ? ", tableId);

			SqlOperator.commitTransaction(db);
		}
	}

	/*
	* 正确数据访问4：在database_id为1001的数据库采集任务下构造新增采集ftp_collect表和object_collect表的数据，
	* ftp_collect表不选择采集列和列排序，object_collect表选择采集列和列排序,都不设置并行抽取(需要和agent交互)
	* */
	@Test
	public void saveCollTbInfoFour(){
		List<Table_info> tableInfos = new ArrayList<>();
		List<CollTbConfParam> tbConfParams = new ArrayList<>();

		try(DatabaseWrapper db = new DatabaseWrapper()){
			//在新增前，查询数据库，table_info表中应该没有采集ftp_collect表的信息
			long count = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName + " where table_name = ?", "ftp_collect").orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			assertThat("在新增前，查询数据库，table_info表中应该没有采集ftp_collect表的信息", count, is(0L));
			//在新增前，查询数据库，table_info表中应该没有采集object_collect表的信息
			long countTwo = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName + " where table_name = ?", "object_collect").orElseThrow(() -> new BusinessSystemException("查询结果必须有且只有一条"));
			assertThat("在新增前，查询数据库，table_info表中应该没有采集object_collect表的信息", countTwo, is(0L));
		}

		Table_info FTPInfo = new Table_info();
		FTPInfo.setTable_name("ftp_collect");
		FTPInfo.setTable_ch_name("ftp采集设置表");
		FTPInfo.setDatabase_id(FIRST_DATABASESET_ID);
		FTPInfo.setIs_parallel(IsFlag.Fou.getCode());

		Table_info objInfo = new Table_info();
		objInfo.setTable_name("object_collect");
		objInfo.setTable_ch_name("半结构化文件采集设置表");
		objInfo.setDatabase_id(FIRST_DATABASESET_ID);
		objInfo.setIs_parallel(IsFlag.Fou.getCode());

		tableInfos.add(FTPInfo);
		tableInfos.add(objInfo);

		CollTbConfParam FTPParam = new CollTbConfParam();
		FTPParam.setColumnSortString("");
		FTPParam.setColumnSortString("");

		CollTbConfParam objParam = new CollTbConfParam();
		objParam.setColumnSortString("");
		objParam.setColumnSortString("");

		tbConfParams.add(FTPParam);
		tbConfParams.add(objParam);

		String rightStringFour = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult rightResultFour = JsonUtil.toObjectSafety(rightStringFour, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResultFour.isSuccess(), is(true));
		Integer returnValue = (Integer) rightResultFour.getData();
		assertThat(returnValue == FIRST_DATABASESET_ID, is(true));

		try(DatabaseWrapper db = new DatabaseWrapper()){
			//新增成功后，断言数据库中的数据是否符合期望，如果符合期望，则删除本次新增带来的数据
			Result afterTableInfo = SqlOperator.queryResult(db, "select * from " + Table_info.TableName + " where table_name = ?", "ftp_collect");
			assertThat("<正确的测试用例4>执行成功后，table_info表中出现了采集ftp_collect表的配置", afterTableInfo.getRowCount(), is(1));
			assertThat("<正确的测试用例4>执行成功后，采集ftp_collect表配置，<清洗顺序>符合期望", afterTableInfo.getString(0, "ti_or"), is(tableCleanOrder.toJSONString()));
			assertThat("<正确的测试用例4>执行成功后，采集ftp_collect表配置，<是否使用MD5>符合期望", afterTableInfo.getString(0, "is_md5"), is(IsFlag.Shi.getCode()));
			assertThat("<正确的测试用例4>执行成功后，采集ftp_collect表配置，<是否仅登记>符合期望", afterTableInfo.getString(0, "is_register"), is(IsFlag.Fou.getCode()));
			assertThat("<正确的测试用例4>执行成功后，采集ftp_collect表配置，<是否并行抽取>符合期望", afterTableInfo.getString(0, "is_parallel"), is(IsFlag.Fou.getCode()));

			Result afterTableColumn = SqlOperator.queryResult(db, "select * from " + Table_column.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			assertThat("<正确的测试用例4>执行成功后，table_column表中有关ftp_collect表的列应该有<24>列", afterTableColumn.getRowCount(), is(24));
			for(int i = 0; i < afterTableColumn.getRowCount(); i++){
				if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_id")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_id>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("int8"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_number")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_number>字段的类型为<varchar(200)>", afterTableColumn.getString(i, "column_type"), is("varchar(200)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_name")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_name>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("start_date")){
					assertThat("<正确的测试用例4>执行成功后, <start_date>字段的类型为<bpchar(8)>", afterTableColumn.getString(i, "column_type"), is("bpchar(8)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("end_date")){
					assertThat("<正确的测试用例4>执行成功后, <end_date>字段的类型为<bpchar(8)>", afterTableColumn.getString(i, "column_type"), is("bpchar(8)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_ip")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_ip>字段的类型为<varchar(50)>", afterTableColumn.getString(i, "column_type"), is("varchar(50)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_port")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_port>字段的类型为<varchar(10)>", afterTableColumn.getString(i, "column_type"), is("varchar(10)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_username")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_username>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_password")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_password>字段的类型为<varchar(100)>", afterTableColumn.getString(i, "column_type"), is("varchar(100)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_dir")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_dir>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("local_path")){
					assertThat("<正确的测试用例4>执行成功后, <local_path>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_rule_path")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_rule_path>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("child_file_path")){
					assertThat("<正确的测试用例4>执行成功后, <child_file_path>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("child_time")){
					assertThat("<正确的测试用例4>执行成功后, <child_time>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("file_suffix")){
					assertThat("<正确的测试用例4>执行成功后, <file_suffix>字段的类型为<varchar(200)>", afterTableColumn.getString(i, "column_type"), is("varchar(200)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("ftp_model")){
					assertThat("<正确的测试用例4>执行成功后, <ftp_model>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("run_way")){
					assertThat("<正确的测试用例4>执行成功后, <run_way>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("remark")){
					assertThat("<正确的测试用例4>执行成功后, <remark>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("is_sendok")){
					assertThat("<正确的测试用例4>执行成功后, <is_sendok>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("is_unzip")){
					assertThat("<正确的测试用例4>执行成功后, <is_unzip>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("reduce_type")){
					assertThat("<正确的测试用例4>执行成功后, <reduce_type>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("is_read_realtime")){
					assertThat("<正确的测试用例4>执行成功后, <is_read_realtime>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("realtime_interval")){
					assertThat("<正确的测试用例4>执行成功后, <realtime_interval>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("int8"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("agent_id")){
					assertThat("<正确的测试用例4>执行成功后, <agent_id>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("int8"));
				}else{
					assertThat("<正确的测试用例4>执行完成之后，采集ftp_collect表的所有字段，出现了不符合期望的字段，字段名为 : " + afterTableColumn.getString(i, "colume_name"), true, is(false));
				}
			}

			//以上断言都测试成功后，删除<正确数据访问4>生成的数据
			int tableCount = SqlOperator.execute(db, "delete from " + Table_info.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			int columnCount = SqlOperator.execute(db, "delete from " + Table_column.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			assertThat("删除<正确数据访问4>生成的数据成功<table_info>", tableCount, is(1));
			assertThat("删除<正确数据访问4>生成的数据成功<table_column>", columnCount, is(24));

			SqlOperator.commitTransaction(db);
		}

		try(DatabaseWrapper db = new DatabaseWrapper()){
			//新增成功后，断言数据库中的数据是否符合期望，如果符合期望，则删除本次新增带来的数据
			Result afterTableInfo = SqlOperator.queryResult(db, "select * from " + Table_info.TableName + " where table_name = ?", "object_collect");
			assertThat("<正确的测试用例4>执行成功后，table_info表中出现了采集object_collect表的配置", afterTableInfo.getRowCount(), is(1));
			assertThat("<正确的测试用例4>执行成功后，采集object_collect表配置，<清洗顺序>符合期望", afterTableInfo.getString(0, "ti_or"), is(tableCleanOrder.toJSONString()));
			assertThat("<正确的测试用例4>执行成功后，采集object_collect表配置，<是否使用MD5>符合期望", afterTableInfo.getString(0, "is_md5"), is(IsFlag.Shi.getCode()));
			assertThat("<正确的测试用例4>执行成功后，采集object_collect表配置，<是否仅登记>符合期望", afterTableInfo.getString(0, "is_register"), is(IsFlag.Fou.getCode()));
			assertThat("<正确的测试用例4>执行成功后，采集object_collect表配置，<是否并行抽取>符合期望", afterTableInfo.getString(0, "is_parallel"), is(IsFlag.Fou.getCode()));

			Result afterTableColumn = SqlOperator.queryResult(db, "select * from " + Table_column.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			assertThat("<正确的测试用例4>执行成功后，table_column表中有关object_collect表的列应该有<16>列", afterTableColumn.getRowCount(), is(16));
			for(int i = 0; i < afterTableColumn.getRowCount(); i++){
				if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("odc_id")){
					assertThat("<正确的测试用例4>执行成功后, <odc_id>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("int8"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("object_collect_type")){
					assertThat("<正确的测试用例4>执行成功后, <object_collect_type>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("obj_number")){
					assertThat("<正确的测试用例4>执行成功后, <obj_number>字段的类型为<varchar(200)>", afterTableColumn.getString(i, "column_type"), is("varchar(200)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("obj_collect_name")){
					assertThat("<正确的测试用例4>执行成功后, <obj_collect_name>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("system_name")){
					assertThat("<正确的测试用例4>执行成功后, <system_name>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("host_name")){
					assertThat("<正确的测试用例4>执行成功后, <host_name>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("local_time")){
					assertThat("<正确的测试用例4>执行成功后, <local_time>字段的类型为<bpchar(20)>", afterTableColumn.getString(i, "column_type"), is("bpchar(20)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("server_date")){
					assertThat("<正确的测试用例4>执行成功后, <server_date>字段的类型为<bpchar(20)>", afterTableColumn.getString(i, "column_type"), is("bpchar(20)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("s_date")){
					assertThat("<正确的测试用例4>执行成功后, <s_date>字段的类型为<bpchar(8)>", afterTableColumn.getString(i, "column_type"), is("bpchar(8)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("e_date")){
					assertThat("<正确的测试用例4>执行成功后, <e_date>字段的类型为<bpchar(8)>", afterTableColumn.getString(i, "column_type"), is("bpchar(8)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("database_code")){
					assertThat("<正确的测试用例4>执行成功后, <database_code>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("run_way")){
					assertThat("<正确的测试用例4>执行成功后, <run_way>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("file_path")){
					assertThat("<正确的测试用例4>执行成功后, <file_path>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("is_sendok")){
					assertThat("<正确的测试用例4>执行成功后, <is_sendok>字段的类型为<bpchar(1)>", afterTableColumn.getString(i, "column_type"), is("bpchar(1)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("remark")){
					assertThat("<正确的测试用例4>执行成功后, <remark>字段的类型为<varchar(512)>", afterTableColumn.getString(i, "column_type"), is("varchar(512)"));
				}else if(afterTableColumn.getString(i, "colume_name").equalsIgnoreCase("agent_id")){
					assertThat("<正确的测试用例4>执行成功后, <agent_id>字段的类型为<int8>", afterTableColumn.getString(i, "column_type"), is("int8"));
				}else{
					assertThat("<正确的测试用例4>执行成功后, 再次查询table_column表，出现了不符合期望的情况，表名为：" + afterTableColumn.getString(i, "colume_name"), true, is(false));
				}
			}

			//以上断言都测试成功后，删除<正确数据访问4>生成的数据
			int tableCount = SqlOperator.execute(db, "delete from " + Table_info.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			int columnCount = SqlOperator.execute(db, "delete from " + Table_column.TableName + " where table_id = ?", afterTableInfo.getLong(0, "table_id"));
			assertThat("删除<正确数据访问4>生成的数据成功<table_info>", tableCount, is(1));
			assertThat("删除<正确数据访问4>生成的数据成功<table_column>", columnCount, is(16));

			SqlOperator.commitTransaction(db);
		}
	}

	@Test
	public void saveCollTbInfoFive(){
		List<Table_info> tableInfos = new ArrayList<>();
		List<CollTbConfParam> tbConfParams = new ArrayList<>();
		//错误的数据访问1：构造缺少表名的采集数据
		Table_info FTPInfo = new Table_info();
		FTPInfo.setTable_ch_name("ftp采集设置表");
		FTPInfo.setDatabase_id(FIRST_DATABASESET_ID);
		FTPInfo.setIs_parallel(IsFlag.Fou.getCode());

		tableInfos.add(FTPInfo);

		CollTbConfParam FTPParam = new CollTbConfParam();
		FTPParam.setColumnSortString("");
		FTPParam.setColumnSortString("");

		tbConfParams.add(FTPParam);

		String wrongStringOne = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult wrongResultOne = JsonUtil.toObjectSafety(wrongStringOne, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResultOne.isSuccess(), is(false));

		tableInfos.clear();
		tbConfParams.clear();

		//错误的数据访问2：构造缺少表中文名的采集数据
		Table_info FTPInfoTwo = new Table_info();
		FTPInfoTwo.setTable_name("ftp_collect");
		FTPInfoTwo.setDatabase_id(FIRST_DATABASESET_ID);
		FTPInfoTwo.setIs_parallel(IsFlag.Fou.getCode());

		tableInfos.add(FTPInfoTwo);

		CollTbConfParam FTPParamTwo = new CollTbConfParam();
		FTPParamTwo.setColumnSortString("");
		FTPParamTwo.setColumnSortString("");

		tbConfParams.add(FTPParamTwo);

		String wrongStringTwo = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult wrongResultTwo = JsonUtil.toObjectSafety(wrongStringTwo, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResultTwo.isSuccess(), is(false));

		tableInfos.clear();
		tbConfParams.clear();

		//错误的数据访问3：构造设置了并行抽取，但没有设置并行抽取SQL的访问方式
		Table_info FTPInfoThree = new Table_info();
		FTPInfoThree.setTable_name("ftp_collect");
		FTPInfoThree.setTable_ch_name("ftp采集设置表");
		FTPInfoThree.setDatabase_id(FIRST_DATABASESET_ID);
		FTPInfoThree.setIs_parallel(IsFlag.Shi.getCode());

		tableInfos.add(FTPInfoThree);

		CollTbConfParam FTPParamThree = new CollTbConfParam();
		FTPParamThree.setColumnSortString("");
		FTPParamThree.setColumnSortString("");

		tbConfParams.add(FTPParamThree);

		String wrongStringThree = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult wrongResultThree = JsonUtil.toObjectSafety(wrongStringThree, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResultThree.isSuccess(), is(false));

		tableInfos.clear();
		tbConfParams.clear();

		//错误的数据访问4：构造在不存在的数据库采集任务中保存采集ftp_collect表数据
		Table_info FTPInfoFour = new Table_info();
		FTPInfoFour.setTable_name("ftp_collect");
		FTPInfoFour.setTable_ch_name("ftp采集设置表");
		FTPInfoFour.setDatabase_id(UNEXPECTED_ID);
		FTPInfoFour.setIs_parallel(IsFlag.Fou.getCode());

		tableInfos.add(FTPInfoFour);

		CollTbConfParam FTPParamFour = new CollTbConfParam();
		FTPParamFour.setColumnSortString("");
		FTPParamFour.setColumnSortString("");

		tbConfParams.add(FTPParamFour);

		String wrongStringFour = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", UNEXPECTED_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult wrongResultFour = JsonUtil.toObjectSafety(wrongStringFour, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResultFour.isSuccess(), is(false));

		tableInfos.clear();
		tbConfParams.clear();

		//错误的数据访问5：构造tableInfoString参数是空字符串的情况
		String wrongStringFive = new HttpClient()
				.addData("tableInfoString", "")
				.addData("colSetId", UNEXPECTED_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult wrongResultFive = JsonUtil.toObjectSafety(wrongStringFive, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResultFive.isSuccess(), is(false));

		//错误的数据访问6：构造collTbConfParamString参数是空字符串的情况
		String wrongStringSix = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", UNEXPECTED_ID)
				.addData("collTbConfParamString", "")
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult wrongResultSix = JsonUtil.toObjectSafety(wrongStringSix, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResultSix.isSuccess(), is(false));

		//错误的数据访问7：构造tableInfoString和collTbConfParamString解析成的list集合大小不同的情况
		Table_info FTPInfoSeven = new Table_info();
		FTPInfoSeven.setTable_name("ftp_collect");
		FTPInfoSeven.setTable_ch_name("ftp采集设置表");
		FTPInfoSeven.setDatabase_id(FIRST_DATABASESET_ID);
		FTPInfoSeven.setIs_parallel(IsFlag.Fou.getCode());

		Table_info objInfoSeven = new Table_info();
		objInfoSeven.setTable_name("object_collect");
		objInfoSeven.setTable_ch_name("半结构化文件采集设置表");
		objInfoSeven.setDatabase_id(FIRST_DATABASESET_ID);
		objInfoSeven.setIs_parallel(IsFlag.Fou.getCode());

		tableInfos.add(FTPInfoSeven);
		tableInfos.add(objInfoSeven);

		CollTbConfParam FTPParamSeven = new CollTbConfParam();
		FTPParamSeven.setColumnSortString("");
		FTPParamSeven.setColumnSortString("");

		tbConfParams.add(FTPParamSeven);

		String wrongStringSeven = new HttpClient()
				.addData("tableInfoString", JSON.toJSONString(tableInfos))
				.addData("colSetId", FIRST_DATABASESET_ID)
				.addData("collTbConfParamString", JSON.toJSONString(tbConfParams))
				.post(getActionUrl("saveCollTbInfo")).getBodyString();
		ActionResult wrongResultSeven = JsonUtil.toObjectSafety(wrongStringSeven, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(wrongResultSeven.isSuccess(), is(false));

	}

	/**
	 * 测试如果页面只有自定义SQL查询采集，保存该界面配置的所有信息功能
	 *
	 * 正确的数据访问1：模拟这样一种情况，在构造的测试数据的基础上，不采集页面上配置的两张表，然后点击下一步，也就是说现在datavase_id为FIRST_DATABASESET_ID的数据库采集任务全部是采集自定义SQL
	 * 错误的测试用例未达到三组:因为自定义表已经入库了，所以要在table_info表中删除不是自定义SQL的表信息，删除的条数可能为0-N，不关注是否删除了数据和删除的数目
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@Test
	public void saveCustomizeCollTbInfo(){
		//正确的数据访问1：模拟这样一种情况，在构造的测试数据的基础上，不采集页面上配置的两张表，然后点击下一步，也就是说现在datavase_id为FIRST_DATABASESET_ID的数据库采集任务全部是采集自定义SQL
		//删除前，确认待删除数据是否存在
		try(DatabaseWrapper db = new DatabaseWrapper()){
			long beforeCount = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName + " WHERE database_id = ? AND valid_e_date = ? AND is_user_defined = ?", FIRST_DATABASESET_ID, Constant.MAXDATE, IsFlag.Fou.getCode()).orElseThrow(() -> new BusinessException("必须有且只有一条数据"));
			assertThat("方法调用前，table_info表中的非用户自定义采集有2条", beforeCount, is(2L));
		}

		//构造正确的数据访问
		String rightString = new HttpClient()
				.addData("colSetId", FIRST_DATABASESET_ID)
				.post(getActionUrl("saveCustomizeCollTbInfo")).getBodyString();
		ActionResult rightResult = JsonUtil.toObjectSafety(rightString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败!"));
		assertThat(rightResult.isSuccess(), is(true));
		Integer returnValue = (Integer) rightResult.getData();
		assertThat(returnValue == FIRST_DATABASESET_ID, is(true));

		//删除后，确认数据是否真的被删除了
		try(DatabaseWrapper db = new DatabaseWrapper()){
			long afterCount = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName + " WHERE database_id = ? AND valid_e_date = ? AND is_user_defined = ?", FIRST_DATABASESET_ID, Constant.MAXDATE, IsFlag.Fou.getCode()).orElseThrow(() -> new BusinessException("必须有且只有一条数据"));
			assertThat("方法调用后，table_info表中的非用户自定义采集有0条", afterCount, is(0L));
		}
	}

	/**
	 * 在测试用例执行完之后，删除测试数据
	 *
	 * @Param: 无
	 * @return: 无
	 *
	 * */
	@After
	public void after(){
		InitAndDestDataForCollTb.after();
	}
}
