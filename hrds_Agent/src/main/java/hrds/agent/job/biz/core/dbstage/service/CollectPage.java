package hrds.agent.job.biz.core.dbstage.service;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.SourceDataConfBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.commons.codes.DatabaseType;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.ConnUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@DocClass(desc = "多线程采集线程类，子线程向主线程返回的有生成的文件路径，当前线程采集到的ResultSet，" +
		"当前线程采集到的数据量", author = "WangZhengcheng")
public class CollectPage implements Callable<Map<String, Object>> {
	private final static Logger LOGGER = LoggerFactory.getLogger(CollectPage.class);
	private SourceDataConfBean sourceDataConfBean;
	private CollectTableBean collectTableBean;
	private TableBean tableBean;
	private String sql;
	private long start;
	private long end;
	private long pageNum;
	private long pageRow;

	public CollectPage(SourceDataConfBean sourceDataConfBean, CollectTableBean collectTableBean,
	                   TableBean tableBean, long start, long end, long pageNum, long pageRow) {
		this.sourceDataConfBean = sourceDataConfBean;
		this.collectTableBean = collectTableBean;
		this.tableBean = tableBean;
		this.start = start;
		this.end = end;
		this.pageNum = pageNum;
		this.pageRow = pageRow;
		this.sql = tableBean.getCollectSQL();
	}

	@Method(desc = "多线程采集执行方法", logicStep = "" +
			"1、执行查询，获取ResultSet" +
			"2、解析ResultSet，并写数据文件" +
			"3、数据落地文件后，线程执行完毕后的返回内容，用于写作业meta文件和验证本次采集任务的结果")
	@Return(desc = "当前线程完成任务(查询数据，落地数据文件)后的结果", range = "三对Entry，key分别为：" +
			"1、filePath，代表生成的数据文件路径" +
			"2、pageData，代表当前线程采集到的ResultSet" +
			"3、pageCount，代表当前线程采集到的数据量")
	@Override
	public Map<String, Object> call() {
		Connection conn = null;
		try {
			//获取jdbc连接
			conn = ConnUtil.getConnection(sourceDataConfBean.getDatabase_drive(), sourceDataConfBean.getJdbc_url(),
					sourceDataConfBean.getUser_name(), sourceDataConfBean.getDatabase_pad());
			//1、执行查询，获取ResultSet
			Map<String, Object> map = new HashMap<>();
			ResultSet resultSet = getPageData(conn);
			if (resultSet != null) {
				//2、解析ResultSet，并写数据文件
				ResultSetParser parser = new ResultSetParser();
				//文件路径
				String unLoadInfo = parser.parseResultSet(resultSet, collectTableBean, pageNum, pageRow, tableBean);
				if (!StringUtil.isEmpty(unLoadInfo) && unLoadInfo.contains(CollectTableHandleParse.STRSPLIT)) {
					List<String> unLoadInfoList = StringUtil.split(unLoadInfo, CollectTableHandleParse.STRSPLIT);
					map.put("pageCount", unLoadInfoList.get(unLoadInfoList.size() - 2));
					map.put("fileSize", unLoadInfoList.get(unLoadInfoList.size() - 1));
					unLoadInfoList.remove(unLoadInfoList.size() - 2);
					unLoadInfoList.remove(unLoadInfoList.size() - 1);
					map.put("filePathList", unLoadInfoList);
				}
			}
			return map;
		} catch (Exception e) {
			throw new AppSystemException("执行分页卸数程序失败", e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}

	}

	@Method(desc = "根据分页SQL获取ResultSet", logicStep = "" +
			"1、将DBConfigBean对象传入工具类ConnectionTool，得到DatabaseWrapper" +
			"2、将采集SQL，当前页的start，end转换通过strategy转为分页SQL" +
			"3、调用方法获得当前线程的分页数据并返回")
	@Param(name = "strategy", desc = "数据库方言策略实例", range = "不为空，DataBaseDialectStrategy接口实例")
	@Param(name = "strSql", desc = "数据库直连采集作业SQL语句", range = "不为空")
	@Param(name = "start", desc = "当前分页开始条数", range = "不限")
	@Param(name = "end", desc = "当前分页结束条数", range = "不限")
	@Return(desc = "当前线程执行分页SQL查询得到的结果集", range = "不会为null")
	private ResultSet getPageData(Connection conn) throws Exception {
		// TODO 默认使用主键做分页，没有主键默认使用第一个字段
		DatabaseMetaData databaseMetaData = conn.getMetaData();
		ResultSet rs = databaseMetaData.getPrimaryKeys(null, null,
				collectTableBean.getTable_name().toUpperCase());
		String primaryKey = null;
		if (rs.next()) {
			primaryKey = rs.getString("COLUMN_NAME");
		}
		rs.close();
		if (StringUtil.isEmpty(primaryKey)) {
			primaryKey = collectTableBean.getCollectTableColumnBeanList().get(0).getColumn_name();
		}
		String database_type = sourceDataConfBean.getDatabase_type();
		//拼分页的sql
		sql = pageForSql(database_type, primaryKey);
		Statement statement = conn.createStatement();
		//TODO 不同数据库的set fetchSize 实现方式不同，暂时设置Oracle 的参数 其他的目前不予处理
		if (DatabaseType.Oracle10g.getCode().equals(database_type) ||
				DatabaseType.Oracle9i.getCode().equals(database_type)) {
			statement.setFetchSize(400);
		}
		if (DatabaseType.MYSQL.getCode().equals(database_type))
			((com.mysql.jdbc.Statement) statement).enableStreamingResults();
		return statement.executeQuery(sql);
	}

	private String pageForSql(String dataType, String primaryKey) {
		LOGGER.info("start-->" + start + "  limit --> " + pageRow + "  end--> " + end);
		if (DatabaseType.MYSQL.getCode().equals(dataType)) {
			sql = sql + " limit " + start + "," + pageRow;
		} else if (DatabaseType.TeraData.getCode().equals(dataType)) {
			sql = sql + " qualify row_number() over(order by " + primaryKey + ") >= " + start
					+ " and row_number() over(order by " + primaryKey + ") <=" + end;
		} else if (DatabaseType.Oracle9i.getCode().equals(dataType) ||
				DatabaseType.Oracle10g.getCode().equals(dataType)) {
			sql = "select * from (select t.*,rownum hyren_rn from (" + sql + ") t where rownum <= "
					+ Math.abs(end) + ") t1 where t1.hyren_rn>" + start + "";
		} else if (DatabaseType.Postgresql.getCode().equals(dataType)) {
			sql = sql + " limit " + pageRow + " offset " + start;
		} else {
			//TODO 这里欢迎补全，最后else抛异常
			sql = sql + " limit " + start + "," + pageRow;
		}
		LOGGER.info("分页这里执行的sql是：" + sql);
		return sql;
	}

}
