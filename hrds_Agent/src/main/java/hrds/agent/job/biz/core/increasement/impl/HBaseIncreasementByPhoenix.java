package hrds.agent.job.biz.core.increasement.impl;

import fd.ng.db.jdbc.DatabaseWrapper;
import hrds.agent.job.biz.bean.TableBean;
import hrds.agent.job.biz.core.increasement.HBaseIncreasement;

/**
 * HBaseIncreasementByPhoenix
 * date: 2020/7/20 15:19
 * author: zxz
 */
public class HBaseIncreasementByPhoenix extends HBaseIncreasement {

	public HBaseIncreasementByPhoenix(TableBean tableBean, String hbase_name,
	                                  String sysDate, String dsl_name, DatabaseWrapper db) {
		super(tableBean, hbase_name, sysDate, dsl_name, db);
	}

	@Override
	public void calculateIncrement() throws Exception {

	}

	@Override
	public void mergeIncrement() throws Exception {

	}

	@Override
	public void append() {

	}

	@Override
	public void restore(String storageType) {

	}
}