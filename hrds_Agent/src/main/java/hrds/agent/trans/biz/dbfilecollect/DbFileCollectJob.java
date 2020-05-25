package hrds.agent.trans.biz.dbfilecollect;

import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import hrds.agent.job.biz.bean.SourceDataConfBean;
import hrds.agent.job.biz.utils.FileUtil;
import hrds.commons.base.AgentBaseAction;
import hrds.commons.utils.Constant;
import hrds.commons.utils.PackUtil;

@DocClass(desc = "db文件采集agent接受发送任务的接口", author = "zxz", createdate = "2020/3/26 9:56")
public class DbFileCollectJob extends AgentBaseAction {
	@Method(desc = "文件采集和前端交互的接口",
			logicStep = "1.获取json数组转成File_source的集合" +
					"2.校验对象的值是否正确" +
					"3.使用JobFactory工厂类调用后台方法")
	@Param(name = "etlDate", desc = "跑批日期", range = "不能为空")
	@Param(name = "taskInfo", desc = "数据库采集需要的参数实体bean的json对象字符串",
			range = "所有这张表不能为空的字段的值必须有，为空则会抛异常，" +
					"collectTableBeanArray对应的表CollectTableBean这个实体不能为空的字段的值必须有，为空则会抛异常")
	public void execute(String etlDate, String taskInfo) {
		//对配置信息解压缩并反序列化为SourceDataConfBean对象
		SourceDataConfBean sourceDataConfBean =
				JSONObject.parseObject(PackUtil.unpackMsg(taskInfo).get("msg"), SourceDataConfBean.class);
		//将页面传递过来的压缩信息解压写文件
		FileUtil.createFile(Constant.MESSAGEFILE + sourceDataConfBean.getDatabase_id(),
				PackUtil.unpackMsg(taskInfo).get("msg"));
//		ExecutorService executor = null;
//		try {
//			//初始化当前任务需要保存的文件的根目录
//			String[] paths = {Constant.JOBINFOPATH, Constant.DBFILEUNLOADFOLDER, Constant.XMLPATH};
//			FileUtil.initPath(sourceDataConfBean.getDatabase_id(), paths);
//			//将json数据字典转为xml
//			String plane_url = sourceDataConfBean.getPlane_url();
//			//获取数据字典所在目录文件，根据数据字典计算xml文件名称
//			String xmlName = Math.abs(plane_url.hashCode()) + ".xml";
//			//DB文件采集将数据字典dd_data.xls转为xml
//			DFCollectTableHandleParse.toXml(plane_url, Constant.XMLPATH
//					+ sourceDataConfBean.getDatabase_id() + File.separator + xmlName);
//			//1.获取json数组转成File_source的集合
//			List<CollectTableBean> collectTableBeanList = sourceDataConfBean.getCollectTableBeanArray();
//			//此处不会有海量的任务需要执行，不会出现队列中等待的任务对象过多的OOM事件。
//			//TODO Runtime.getRuntime().availableProcessors()此处不能用这个,因为可能同时又多个数据库采集同时进行
//			executor = Executors.newFixedThreadPool(5);
//			List<Future<JobStatusInfo>> list = new ArrayList<>();
//			//2.校验对象的值是否正确
//			for (CollectTableBean collectTableBean : collectTableBeanList) {
//				//设置跑批日期
//				collectTableBean.setEtlDate(etlDate);
//				//为了确保多个线程之间的值不互相干涉，复制对象的值。
//				SourceDataConfBean sourceDataConfBean1 = JSONObject.parseObject(
//						JSONObject.toJSONString(sourceDataConfBean), SourceDataConfBean.class);
//				DataFileJobImpl fileCollectJob = new DataFileJobImpl(sourceDataConfBean1, collectTableBean);
//				Future<JobStatusInfo> submit = executor.submit(fileCollectJob);
//				list.add(submit);
//			}
//			//3.打印每个线程执行情况
//			JobStatusInfoUtil.printJobStatusInfo(list);
//		} catch (Exception e) {
//			throw new AppSystemException("DB文件采集失败!", e);
//		} finally {
//			if (executor != null)
//				executor.shutdown();
//		}
	}
}
