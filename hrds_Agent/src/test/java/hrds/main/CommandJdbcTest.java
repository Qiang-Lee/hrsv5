package hrds.main;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.JobStatusInfo;
import hrds.agent.job.biz.bean.SourceDataConfBean;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.agent.job.biz.core.DataBaseJobImpl;
import hrds.agent.job.biz.utils.FileUtil;
import hrds.agent.job.biz.utils.JobStatusInfoUtil;
import hrds.commons.codes.DataBaseCode;
import hrds.commons.codes.DataExtractType;
import hrds.commons.codes.FileFormat;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Data_extraction_def;
import hrds.commons.utils.Constant;
import hrds.testbase.WebBaseTestCase;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@DocClass(desc = "数据库采集测试用例", createdate = "2020/01/07 09:48", author = "zxz")
public class CommandJdbcTest extends WebBaseTestCase {

	/**
	 * 测试数据库抽取选择单表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test1() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test2() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test3() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Shi.getCode());
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test4() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Shi.getCode());
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、计算md5、指定并行数并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test5() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Shi.getCode());
			collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
			collectTableBean.setPageparallels(5);
			collectTableBean.setRec_num_date(DateUtil.getSysDate());
			collectTableBean.setDataincrement(10);
			collectTableBean.setTable_count("8");
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、计算md5、指定并行数并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test6() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Shi.getCode());
			collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
			collectTableBean.setPageparallels(5);
			collectTableBean.setRec_num_date(DateUtil.getSysDate());
			collectTableBean.setDataincrement(10);
			collectTableBean.setTable_count("100");
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、计算md5、指定并行数并行抽取、添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test7() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Shi.getCode());
			collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
			collectTableBean.setPageparallels(5);
			collectTableBean.setRec_num_date(DateUtil.getSysDate());
			collectTableBean.setDataincrement(10);
			collectTableBean.setTable_count("8");
			if ("call_center".equals(collectTableBean.getTable_name()))
				collectTableBean.setSql("cc_class = 'medium'");
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、计算md5、指定并行数并行抽取、添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test8() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Shi.getCode());
			collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
			collectTableBean.setPageparallels(5);
			collectTableBean.setRec_num_date(DateUtil.getSysDate());
			collectTableBean.setDataincrement(10);
			collectTableBean.setTable_count("100");
			if ("call_center".equals(collectTableBean.getTable_name()))
				collectTableBean.setSql("cc_class = 'medium'");
			else if ("item".equals(collectTableBean.getTable_name()))
				collectTableBean.setSql("i_rec_start_date = '2000-10-27'");
			else if ("reason".equals(collectTableBean.getTable_name()))
				collectTableBean.setSql("r_reason_desc like 'reason%'");
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、计算md5、自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test9() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Shi.getCode());
			if ("call_center".equals(collectTableBean.getTable_name())) {
				collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
				collectTableBean.setIs_customize_sql(IsFlag.Shi.getCode());
				collectTableBean.setPage_sql("select * from call_center where cc_class = 'medium'" +
						" `@^ select * from call_center where cc_class = 'large' " +
						" `@^ select * from call_center where cc_class = 'small' ");
			}
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、计算md5、自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test10() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Shi.getCode());
			if ("call_center".equals(collectTableBean.getTable_name())) {
				collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
				collectTableBean.setIs_customize_sql(IsFlag.Shi.getCode());
				collectTableBean.setPage_sql("select * from call_center where cc_class = 'medium'" +
						" `@^ select * from call_center where cc_class = 'large' " +
						" `@^ select * from call_center where cc_class = 'small' ");
			} else if ("item".equals(collectTableBean.getTable_name())) {
				collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
				collectTableBean.setIs_customize_sql(IsFlag.Shi.getCode());
				collectTableBean.setPage_sql("select * from item where i_rec_start_date = '2000-10-27'" +
						" `@^ select * from item where i_rec_start_date = '1997-10-27' " +
						" `@^ select * from item where i_rec_start_date = '1999-10-28' " +
						" `@^ select * from item where i_rec_start_date = '2001-10-27' ");
			} else if ("reason".equals(collectTableBean.getTable_name())) {
				collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
				collectTableBean.setIs_customize_sql(IsFlag.Shi.getCode());
				collectTableBean.setPage_sql("select * from reason where r_reason_desc like 'Did not like%'" +
						" `@^ select * from reason where r_reason_desc like 'Found a better%' " +
						" `@^ select * from reason where r_reason_desc like 'reason%' ");
			}
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、不计算md5、自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test11() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Fou.getCode());
			if ("call_center".equals(collectTableBean.getTable_name())) {
				collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
				collectTableBean.setIs_customize_sql(IsFlag.Shi.getCode());
				collectTableBean.setPage_sql("select * from call_center where cc_class = 'medium'" +
						" `@^ select * from call_center where cc_class = 'large' " +
						" `@^ select * from call_center where cc_class = 'small' ");
			}
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test12() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			collectTableBean.setIs_md5(IsFlag.Fou.getCode());
			if ("call_center".equals(collectTableBean.getTable_name())) {
				collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
				collectTableBean.setIs_customize_sql(IsFlag.Shi.getCode());
				collectTableBean.setPage_sql("select * from call_center where cc_class = 'medium'" +
						" `@^ select * from call_center where cc_class = 'large' " +
						" `@^ select * from call_center where cc_class = 'small' ");
			} else if ("item".equals(collectTableBean.getTable_name())) {
				collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
				collectTableBean.setIs_customize_sql(IsFlag.Shi.getCode());
				collectTableBean.setPage_sql("select * from item where i_rec_start_date = '2000-10-27'" +
						" `@^ select * from item where i_rec_start_date = '1997-10-27' " +
						" `@^ select * from item where i_rec_start_date = '1999-10-28' " +
						" `@^ select * from item where i_rec_start_date = '2001-10-27' ");
			} else if ("reason".equals(collectTableBean.getTable_name())) {
				collectTableBean.setIs_parallel(IsFlag.Shi.getCode());
				collectTableBean.setIs_customize_sql(IsFlag.Shi.getCode());
				collectTableBean.setPage_sql("select * from reason where r_reason_desc like 'Did not like%'" +
						" `@^ select * from reason where r_reason_desc like 'Found a better%' " +
						" `@^ select * from reason where r_reason_desc like 'reason%' ");
			}
		}
		sourceDataConfBean.setCollectTableBeanArray(collectTableBeanArray);
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test13() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.DingChang.getCode());
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成定长文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test14() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.DingChang.getCode());
				data_extraction_def.setDatabase_separatorr("");
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成Parquet文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test15() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.PARQUET.getCode());
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成Parquet文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test16() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.PARQUET.getCode());
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成Orc文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test17() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.ORC.getCode());
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成Orc文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test18() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.ORC.getCode());
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成SequenceFile文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test19() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.SEQUENCEFILE.getCode());
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成SequenceFile文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test20() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.SEQUENCEFILE.getCode());
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成Csv文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test21() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.CSV.getCode());
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成Csv文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test22() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setDbfile_format(FileFormat.CSV.getCode());
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、同时生成六种文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test23() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			String plane_url = collectTableBean.getData_extraction_def_list().get(0).getPlane_url();
			String row_separator = StringUtil.string2Unicode(collectTableBean.getData_extraction_def_list().
					get(0).getRow_separator());
			List<Data_extraction_def> data_extraction_def_list = new ArrayList<>();
			//定长
			Data_extraction_def data_extraction_def_DingChang = new Data_extraction_def();
			data_extraction_def_DingChang.setDbfile_format(FileFormat.DingChang.getCode());
			data_extraction_def_DingChang.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_DingChang.setRow_separator(row_separator);
			data_extraction_def_DingChang.setDatabase_separatorr(StringUtil.string2Unicode(Constant.DATADELIMITER));
			data_extraction_def_DingChang.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_DingChang.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_DingChang);
			//非定长
			Data_extraction_def data_extraction_def_FeiDingChang = new Data_extraction_def();
			data_extraction_def_FeiDingChang.setDbfile_format(FileFormat.FeiDingChang.getCode());
			data_extraction_def_FeiDingChang.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_FeiDingChang.setRow_separator(row_separator);
			data_extraction_def_FeiDingChang.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_FeiDingChang.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_FeiDingChang);
			//CSV
			Data_extraction_def data_extraction_def_csv = new Data_extraction_def();
			data_extraction_def_csv.setDbfile_format(FileFormat.CSV.getCode());
			data_extraction_def_csv.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_csv.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_csv.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_csv);
			//Parquet
			Data_extraction_def data_extraction_def_parquet = new Data_extraction_def();
			data_extraction_def_parquet.setDbfile_format(FileFormat.PARQUET.getCode());
			data_extraction_def_parquet.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_parquet.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_parquet.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_parquet);
			//Orc
			Data_extraction_def data_extraction_def_orc = new Data_extraction_def();
			data_extraction_def_orc.setDbfile_format(FileFormat.ORC.getCode());
			data_extraction_def_orc.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_orc.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_orc.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_orc);
			//SequenceFile
			Data_extraction_def data_extraction_def_sequenceFile = new Data_extraction_def();
			data_extraction_def_sequenceFile.setDbfile_format(FileFormat.SEQUENCEFILE.getCode());
			data_extraction_def_sequenceFile.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_sequenceFile.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_sequenceFile.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_sequenceFile);
			collectTableBean.setData_extraction_def_list(data_extraction_def_list);
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、同时生成六种文件、选择同一目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test24() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			String plane_url = collectTableBean.getData_extraction_def_list().get(0).getPlane_url();
			String row_separator = StringUtil.string2Unicode(collectTableBean.getData_extraction_def_list().
					get(0).getRow_separator());
			List<Data_extraction_def> data_extraction_def_list = new ArrayList<>();
			//定长
			Data_extraction_def data_extraction_def_DingChang = new Data_extraction_def();
			data_extraction_def_DingChang.setDbfile_format(FileFormat.DingChang.getCode());
			data_extraction_def_DingChang.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_DingChang.setRow_separator(row_separator);
			data_extraction_def_DingChang.setDatabase_separatorr(StringUtil.string2Unicode(Constant.DATADELIMITER));
			data_extraction_def_DingChang.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_DingChang.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_DingChang);
			//非定长
			Data_extraction_def data_extraction_def_FeiDingChang = new Data_extraction_def();
			data_extraction_def_FeiDingChang.setDbfile_format(FileFormat.FeiDingChang.getCode());
			data_extraction_def_FeiDingChang.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_FeiDingChang.setRow_separator(row_separator);
			data_extraction_def_FeiDingChang.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_FeiDingChang.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_FeiDingChang);
			//CSV
			Data_extraction_def data_extraction_def_csv = new Data_extraction_def();
			data_extraction_def_csv.setDbfile_format(FileFormat.CSV.getCode());
			data_extraction_def_csv.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_csv.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_csv.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_csv);
			//Parquet
			Data_extraction_def data_extraction_def_parquet = new Data_extraction_def();
			data_extraction_def_parquet.setDbfile_format(FileFormat.PARQUET.getCode());
			data_extraction_def_parquet.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_parquet.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_parquet.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_parquet);
			//Orc
			Data_extraction_def data_extraction_def_orc = new Data_extraction_def();
			data_extraction_def_orc.setDbfile_format(FileFormat.ORC.getCode());
			data_extraction_def_orc.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_orc.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_orc.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_orc);
			//SequenceFile
			Data_extraction_def data_extraction_def_sequenceFile = new Data_extraction_def();
			data_extraction_def_sequenceFile.setDbfile_format(FileFormat.SEQUENCEFILE.getCode());
			data_extraction_def_sequenceFile.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_sequenceFile.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_sequenceFile.setPlane_url(plane_url);
			data_extraction_def_list.add(data_extraction_def_sequenceFile);
			collectTableBean.setData_extraction_def_list(data_extraction_def_list);
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择不同目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test25() {
		//获取路径
		JSONArray multi_landing_directory = JSONArray.parseArray
				(agentInitConfig.getString("multi_landing_directory"));
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			String row_separator = StringUtil.string2Unicode(collectTableBean.getData_extraction_def_list().
					get(0).getRow_separator());
			List<Data_extraction_def> data_extraction_def_list = new ArrayList<>();
			//定长
			Data_extraction_def data_extraction_def_DingChang = new Data_extraction_def();
			data_extraction_def_DingChang.setDbfile_format(FileFormat.DingChang.getCode());
			data_extraction_def_DingChang.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_DingChang.setRow_separator(row_separator);
			data_extraction_def_DingChang.setDatabase_separatorr(StringUtil.string2Unicode(Constant.DATADELIMITER));
			data_extraction_def_DingChang.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_DingChang.setPlane_url(multi_landing_directory.getString(0
					% multi_landing_directory.size()));
			data_extraction_def_list.add(data_extraction_def_DingChang);
			//非定长
			Data_extraction_def data_extraction_def_FeiDingChang = new Data_extraction_def();
			data_extraction_def_FeiDingChang.setDbfile_format(FileFormat.FeiDingChang.getCode());
			data_extraction_def_FeiDingChang.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_FeiDingChang.setRow_separator(row_separator);
			data_extraction_def_FeiDingChang.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_FeiDingChang.setPlane_url(multi_landing_directory.getString(1
					% multi_landing_directory.size()));
			data_extraction_def_list.add(data_extraction_def_FeiDingChang);
			//CSV
			Data_extraction_def data_extraction_def_csv = new Data_extraction_def();
			data_extraction_def_csv.setDbfile_format(FileFormat.CSV.getCode());
			data_extraction_def_csv.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_csv.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_csv.setPlane_url(multi_landing_directory.getString(2
					% multi_landing_directory.size()));
			data_extraction_def_list.add(data_extraction_def_csv);
			//Parquet
			Data_extraction_def data_extraction_def_parquet = new Data_extraction_def();
			data_extraction_def_parquet.setDbfile_format(FileFormat.PARQUET.getCode());
			data_extraction_def_parquet.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_parquet.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_parquet.setPlane_url(multi_landing_directory.getString(3
					% multi_landing_directory.size()));
			data_extraction_def_list.add(data_extraction_def_parquet);
			//Orc
			Data_extraction_def data_extraction_def_orc = new Data_extraction_def();
			data_extraction_def_orc.setDbfile_format(FileFormat.ORC.getCode());
			data_extraction_def_orc.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_orc.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_orc.setPlane_url(multi_landing_directory.getString(4
					% multi_landing_directory.size()));
			data_extraction_def_list.add(data_extraction_def_orc);
			//SequenceFile
			Data_extraction_def data_extraction_def_sequenceFile = new Data_extraction_def();
			data_extraction_def_sequenceFile.setDbfile_format(FileFormat.SEQUENCEFILE.getCode());
			data_extraction_def_sequenceFile.setData_extract_type(DataExtractType.ShuJuKuChouQuLuoDi.getCode());
			data_extraction_def_sequenceFile.setDatabase_code(DataBaseCode.GBK.getCode());
			data_extraction_def_sequenceFile.setPlane_url(multi_landing_directory.getString(5
					% multi_landing_directory.size()));
			data_extraction_def_list.add(data_extraction_def_sequenceFile);
			collectTableBean.setData_extraction_def_list(data_extraction_def_list);
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择不同目的地
	 * 选择linux换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test26() {
		//获取路径
		JSONArray multi_landing_directory = JSONArray.parseArray
				(agentInitConfig.getString("multi_landing_directory"));
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		//获取单表的页面配置基本信息
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (int i = 0; i < collectTableBeanArray.size(); i++) {
			CollectTableBean collectTableBean = collectTableBeanArray.get(i);
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setPlane_url(multi_landing_directory.getString(
						i % multi_landing_directory.size()));
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择单表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择windows换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test27() {
		//获取单表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getSingleTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setRow_separator(StringUtil.string2Unicode("\\r\\n"));
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 测试数据库抽取选择多表、不计算md5、不并行抽取、不添加sql过滤、不是自定义写sql并行抽取、
	 * 列和表都不选择清洗、仅生成非定长文件、选择同一目的地
	 * 选择windows换行符、列分隔符使用`@^、字符集选择GBK、全量采集
	 */
	@Test
	public void test28() {
		//获取多表的页面配置基本信息
		SourceDataConfBean sourceDataConfBean = getMultiTableSourceDataConfBean();
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setRow_separator(StringUtil.string2Unicode("\\r\\n"));
			}
		}
		assertThat("执行成功", executeJdbcCollect(sourceDataConfBean), is(true));
	}

	/**
	 * 执行数据库采集的方法
	 *
	 * @param sourceDataConfBean 任务配置信息
	 * @return 成功失败值
	 */
	private boolean executeJdbcCollect(SourceDataConfBean sourceDataConfBean) {
		ExecutorService executor = null;
		try {
			//初始化当前任务需要保存的文件的根目录
			String[] paths = {Constant.JOBINFOPATH, Constant.DICTIONARY};
			FileUtil.initPath(sourceDataConfBean.getDatabase_id(), paths);
			//1.获取json数组转成File_source的集合
			List<CollectTableBean> collectTableBeanList = sourceDataConfBean.getCollectTableBeanArray();
			//此处不会有海量的任务需要执行，不会出现队列中等待的任务对象过多的OOM事件。
			executor = Executors.newFixedThreadPool(JobConstant.AVAILABLEPROCESSORS);
			List<Future<JobStatusInfo>> list = new ArrayList<>();
			//2.校验对象的值是否正确
			for (CollectTableBean collectTableBean : collectTableBeanList) {
				List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
				for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
					collectTableBean.setEtlDate(DateUtil.getSysDate());
					collectTableBean.setSelectFileFormat(data_extraction_def.getDbfile_format());
					//为了确保多个线程之间的值不互相干涉，复制对象的值。
					SourceDataConfBean sourceDataConfBean1 = JSONObject.parseObject(
							JSONObject.toJSONString(sourceDataConfBean), SourceDataConfBean.class);
					CollectTableBean collectTableBean1 = JSONObject.parseObject(
							JSONObject.toJSONString(collectTableBean), CollectTableBean.class);
					//多线程执行
					DataBaseJobImpl fileCollectJob = new DataBaseJobImpl(sourceDataConfBean1, collectTableBean1);
					Future<JobStatusInfo> submit = executor.submit(fileCollectJob);
					list.add(submit);
				}
			}
			//3.打印每个线程执行情况
			JobStatusInfoUtil.printJobStatusInfo(list);
		} catch (Exception e) {
			return false;
		} finally {
			if (executor != null)
				executor.shutdown();
		}
		return true;
	}

//	/**
//	 * 执行数据库采集的主程序
//	 *
//	 * @param job_id      任务id
//	 * @param collectType 采集类型
//	 */
//	private boolean execute(SourceDataConfBean sourceDataConfBean) {
//		if (AgentType.WenJianXiTong.getCode().equals(collectType)) {
//			return executeFileCollect(taskInfo);
//		} else if (AgentType.ShuJuKu.getCode().equals(collectType)) {
//			return executeJdbcCollect(taskInfo);
//		} else if (AgentType.FTP.getCode().equals(collectType)) {
//			return executeFtpCollect(taskInfo);
//		} else if (AgentType.DBWenJian.getCode().equals(collectType)) {
//			return executeDbFileCollect(taskInfo);
//		} else if (AgentType.DuiXiang.getCode().equals(collectType)) {
//			return executeObjectFileCollect(taskInfo);
//		} else {
//			throw new AppSystemException("采集类型不正确");
//		}
//
//	}

//	/**
//	 * 执行ftp采集的方法
//	 *
//	 * @param taskInfo 任务配置信息
//	 * @return 成功失败值
//	 */
//	private boolean executeFtpCollect(String taskInfo) {
//		try {
//			//对配置信息解压缩并反序列化为Ftp_collect对象
//			Ftp_collect ftp_collect = JSONObject.parseObject(taskInfo, Ftp_collect.class);
//			//1.获取参数，校验对象的值是否正确
//			//此处不会有海量的任务需要执行，不会出现队列中等待的任务对象过多的OOM事件。
//			ExecutorService pool = Executors.newFixedThreadPool(1);
//			JobInterface job = new FtpCollectJobImpl(ftp_collect);
//			Future<JobStatusInfo> statusInfoFuture = pool.submit(job);
//			JobStatusInfo jobStatusInfo = statusInfoFuture.get();
//			System.out.println("作业执行情况" + jobStatusInfo.toString());
//		} catch (Exception e) {
//			return false;
//		}
//		return true;
//	}
//
//	/**
//	 * 执行非结构化文件采集的方法
//	 *
//	 * @param taskInfo 任务配置信息
//	 * @return 成功失败值
//	 */
//	private boolean executeFileCollect(String taskInfo) {
//		FileCollectParamBean fileCollectParamBean = JSONObject.parseObject(
//				taskInfo, FileCollectParamBean.class);
//		ThreadPoolExecutor executor = null;
//		try {
//			//初始化当前任务需要保存的文件的根目录
//			String[] paths = {Constant.MAPDBPATH, Constant.JOBINFOPATH, Constant.FILEUNLOADFOLDER};
//			FileUtil.initPath(fileCollectParamBean.getFcs_id(), paths);
//			//1.获取json数组转成File_source的集合
//			List<File_source> fileSourceList = fileCollectParamBean.getFile_sourceList();
//			//使用多线程按照文件夹采集，核心线程5个，最大线程10个，队列里面50个，超出会报错
//			executor = new ThreadPoolExecutor(5, 10,
//					5L, TimeUnit.MINUTES, new LinkedBlockingQueue<>(50));
//			List<Future<JobStatusInfo>> list = new ArrayList<>();
//			//2.校验对象的值是否正确
//			for (File_source file_source : fileSourceList) {
//				//为了确保两个线程之间的值不互相干涉，复制对象的值。
//				FileCollectParamBean fileCollectParamBean1 = JSONObject.parseObject(
//						JSONObject.toJSONString(fileCollectParamBean), FileCollectParamBean.class);
//				FileCollectJobImpl fileCollectJob = new FileCollectJobImpl(fileCollectParamBean1, file_source);
//				Future<JobStatusInfo> submit = executor.submit(fileCollectJob);
//				list.add(submit);
//			}
//			//3.打印每个线程执行情况
//			JobStatusInfoUtil.printJobStatusInfo(list);
//		} catch (Exception e) {
//			return false;
//		} finally {
//			if (executor != null)
//				executor.shutdown();
//		}
//		return true;
//	}
//
//	/**
//	 * 执行Db文件采集的方法
//	 *
//	 * @param taskInfo 任务配置信息
//	 * @return 成功失败值
//	 */
//	private boolean executeDbFileCollect(String taskInfo) {
//		return true;
//	}
//
//	/**
//	 * 执行对象采集的方法
//	 *
//	 * @param taskInfo 任务配置信息
//	 * @return 成功失败值
//	 */
//	private boolean executeObjectFileCollect(String taskInfo) {
//		return true;
//	}

	/**
	 * 获取数据库抽取只选择单表的页面配置文件
	 *
	 * @return 数据库抽取源数据读取配置信息
	 */
	private SourceDataConfBean getSingleTableSourceDataConfBean() {
		String taskInfo = FileUtil.readFile2String(new File(agentInitConfig.
				getString("singleTableSourceDataConfPath")));
		//对配置信息解压缩并反序列化为SourceDataConfBean对象
		SourceDataConfBean sourceDataConfBean = JSONObject.parseObject(taskInfo, SourceDataConfBean.class);
		return replaceTestInfoConf(sourceDataConfBean);
	}

	/**
	 * 获取数据库抽取选择多表的配置文件
	 *
	 * @return 数据库抽取源数据读取配置信息
	 */
	private SourceDataConfBean getMultiTableSourceDataConfBean() {
		String taskInfo = FileUtil.readFile2String(new File(agentInitConfig.
				getString("multiTableSourceDataConfPath")));
		//对配置信息解压缩并反序列化为SourceDataConfBean对象
		SourceDataConfBean sourceDataConfBean = JSONObject.parseObject(taskInfo, SourceDataConfBean.class);
		return replaceTestInfoConf(sourceDataConfBean);
	}

	/**
	 * 替换掉要采集的源数据库的信息
	 *
	 * @param sourceDataConfBean 数据库抽取源数据读取配置信息
	 * @return 数据库抽取源数据读取配置信息
	 */
	private SourceDataConfBean replaceTestInfoConf(SourceDataConfBean sourceDataConfBean) {
		JSONObject object = JSONObject.parseObject(agentInitConfig.getString("source_database_info"));
		sourceDataConfBean.setDatabase_drive(object.getString("database_drive"));
		sourceDataConfBean.setDatabase_type(object.getString("database_type"));
		sourceDataConfBean.setJdbc_url(object.getString("jdbc_url"));
		sourceDataConfBean.setDatabase_port(object.getString("database_port"));
		sourceDataConfBean.setDatabase_pad(object.getString("database_pad"));
		sourceDataConfBean.setUser_name(object.getString("user_name"));
		sourceDataConfBean.setDatabase_name(object.getString("database_name"));
		sourceDataConfBean.setDatabase_ip(object.getString("database_ip"));
		List<CollectTableBean> collectTableBeanArray = sourceDataConfBean.getCollectTableBeanArray();
		for (CollectTableBean collectTableBean : collectTableBeanArray) {
			List<Data_extraction_def> data_extraction_def_list = collectTableBean.getData_extraction_def_list();
			for (Data_extraction_def data_extraction_def : data_extraction_def_list) {
				data_extraction_def.setPlane_url(agentInitConfig.getString("landing_directory"));
			}
		}
		return sourceDataConfBean;
	}
}
