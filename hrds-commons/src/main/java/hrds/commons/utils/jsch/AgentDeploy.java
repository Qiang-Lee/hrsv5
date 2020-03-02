package hrds.commons.utils.jsch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Agent_down_info;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.PropertyParaValue;
import hrds.commons.utils.datastorage.QueryContrast;
import hrds.commons.utils.datastorage.httpserver.HttpServer;
import hrds.commons.utils.datastorage.scpconf.ScpHadoopConf;
import hrds.commons.utils.datastorage.syspara.SysPara;
import hrds.commons.utils.deployentity.HttpYaml;
import hrds.commons.utils.yaml.Yaml;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@DocClass(desc = "部署Agent", author = "Mr.Lee", createdate = "2020-01-15 14:20")
public class AgentDeploy {

  private static final Log logger = LogFactory.getLog(AgentDeploy.class);

  /** 系统路径的符号 */
  public static final String SEPARATOR = File.separator;

  /** 配置文件的临时存放路径 */
  private static final String CONFPATH =
      System.getProperty("user.dir")
          + SEPARATOR
          + "tempresources"
          + SEPARATOR
          + "fdconfig"
          + SEPARATOR;

  /** 写本地临时Yaml配置文件 */
  @Method(
      desc = "部署Agent配置文件",
      logicStep =
          "一 : 将配置文件信息写到本地.然后在 SFTP 到对应的Agent下面"
              + " 第一种文件( contrast.conf )内容 :"
              + "       1 : 名称( typecontrast ),存储数据类型转换"
              + "       2 : 名称( lengthcontrast ),存储类型的字段长度转换"
              + " 第二种文件( sysparam.conf )内容 : 全部的系统参数信息"
              + " 第三种文件( httpserver.conf )内容"
              + "       1 : 修改名称( name : default )的参数 : "
              + "               host : Agent启动IP"
              + "               port : Agent启动端口"
              + "               webContext : 服务的项目名称 "
              + "               actionPattern :  服务的地址"
              + "           如果未获取到,则使用默认的,不做修改"
              + "       2 : 修改名称( name : hyren_main )的参数 : "
              + "               host : 海云服务机器的IP"
              + "               port : 海云服务机器的端口")
  @Param(name = "down_info", desc = "部署Agent信息,这个里面的路径是最新的", range = "", isBean = true)
  @Param(name = "oldAgentPath", desc = "旧的,Agent部署目录地址", range = "可以为空,为空表示为第一次部署")
  @Param(name = "oldLogPath", desc = "旧的,Agent部署日志地址", range = "可以为空,为空表示为第一次部署")
  @Return(desc = "返回部署是否操作成功", range = "true-成功/false-失败")
  public static void agentConfDeploy(
      Agent_down_info down_info, String oldAgentPath, String oldLogPath) {
    try {

      // 一 : 将配置文件信息写到本地.然后在 SFTP 到对应的Agent下面
      /* 第一种文件( contrast.conf )内容 :" */
      Yaml.dump(QueryContrast.getDclContrast(), new File(CONFPATH + QueryContrast.CONF_FILE_NAME));

      /* 第二种文件( sysparam.conf )内容 */
      Yaml.dump(new SysPara().yamlDataFormat(), new File(CONFPATH + SysPara.CONF_FILE_NAME));

      /* 第三种文件( httpserver.conf )内容 */
      Map<String, List<HttpYaml>> httpServerMap =
          HttpServer.httpserverConfData(
              down_info.getAgent_context(),
              down_info.getAgent_pattern(),
              down_info.getAgent_ip(),
              down_info.getAgent_port());
      Yaml.dump(httpServerMap, new File(CONFPATH + HttpServer.HTTP_CONF_NAME));

      // 二 : resources/fdconfig/ 下的全部文件SCP 到agent目录下
      /* 开始将本地写好的文件SCP到Agent目下, */
      sftpAgentToTargetMachine(down_info, oldAgentPath, oldLogPath);

    } catch (FileNotFoundException e) {
      throw new BusinessException(e.getMessage());
    }
  }

  @Method(
      desc = "开始是SFTP将文件传输到agent部署的目下",
      logicStep =
          "一 : 将需要的文件SCP 到目标agent目录下"
              + "1 : 根据旧的部署目录来判断是否为第一次部署,如果部署第一次部署则先将进程kill,然后再将目录删除.."
              + "2 : 检查当前的目录下的进程是否启动"
              + "二 : 将本地写的agent配置文件,sftp复制到agent部署的目标机器"
              + "三 : 将储存层上传的文件 SFTP 到agent目录下"
              + "四 : 将需要的jar包 SFTP 到agent下"
              + "五 : 判断是否启动agent")
  @Param(name = "down_info", desc = "部署的实体信息", range = "不能为空", isBean = true)
  @Param(name = "oldAgentPath", desc = "旧的,Agent部署目录地址", range = "可以为空,为空表示为第一次部署")
  @Param(name = "oldLogPath", desc = "旧的,Agent部署日志地址", range = "可以为空,为空表示为第一次部署")
  @Return(desc = "", range = "")
  private static void sftpAgentToTargetMachine(
      Agent_down_info down_info, String oldAgentPath, String oldLogPath) {

    // 这里先将配置的agent名称转换为拼音在和端口组合在一起,当做agent部署的目录
    String agentDirName =
        ChineseUtil.getPingYin(down_info.getAgent_name()) + "_" + down_info.getAgent_port();

    // 一 : 将需要的文件SCP 到目标agent目录下
    Session shellSession = null;
    ChannelSftp chSftp = null;
    SFTPChannel channel = null;
    try {
      shellSession = getSession(down_info);
      if (StringUtil.isNotBlank(oldAgentPath)) {

        // 1 : 根据旧的部署目录来判断是否为第一次部署,如果部署第一次部署则先将进程kill,然后再将目录删除.
        logger.info(
            "停止就Agent的命令 : "
                + "kill -9 $(netstat -anp|grep "
                + down_info.getAgent_port()
                + " |awk '{printf $7}'|cut -d/ -f1)");
        SFTPChannel.execCommandByJSch(
            shellSession,
            "kill -9 $(netstat -anp|grep "
                + down_info.getAgent_port()
                + " |awk '{printf $7}'|cut -d/ -f1)");

        // 这里的kill 还不知道怎么写
        logger.info("删除旧目录的命令是 : " + "rm -rf " + oldAgentPath + SEPARATOR + agentDirName);
        SFTPChannel.execCommandByJSchNoRs(
            shellSession, "rm -rf " + oldAgentPath + SEPARATOR + agentDirName);
      }

      // 检查当前的目录下的进程是否启动(这里直接使用kill命令,为防止后续启动出错)
      logger.info(
          "停止Agnet的命令是 : "
              + "kill -9 $(netstat -anp|grep "
              + down_info.getAgent_port()
              + " |awk '{printf $7}'|cut -d/ -f1)");
      SFTPChannel.execCommandByJSch(
          shellSession,
          "kill -9 $(netstat -anp|grep "
              + down_info.getAgent_port()
              + " |awk '{printf $7}'|cut -d/ -f1)");

      // 删除目标的机器的部署路径,防止存在
      logger.info("删除目标机器部署路径的命令是 : rm -rf " + down_info.getSave_dir() + SEPARATOR + agentDirName);
      SFTPChannel.execCommandByJSch(
          shellSession, "rm -rf " + down_info.getSave_dir() + SEPARATOR + agentDirName);

      // 创建远程目录
      mkdirToTarget(shellSession, down_info.getSave_dir() + SEPARATOR + agentDirName);

      // 开始传输的Agent包
      channel = new SFTPChannel();
      chSftp = channel.getChannel(shellSession, 60000);
      File file = new File(PropertyParaValue.getString("agentpath", ""));
      logger.info("本地Agent路径地址 : " + file.getAbsolutePath());
      logger.info("开始传输的Agent包,从本地路径 : " + file.getAbsolutePath());
      String targetDir = down_info.getSave_dir() + SEPARATOR + agentDirName + SEPARATOR + ".bin";
      chSftp.put(
          file.getAbsolutePath(),
          targetDir,
          new FileProgressMonitor(file.length()),
          ChannelSftp.OVERWRITE);

      //      // 解压上传的GZ包
      //      logger.info("解压上传的GZ包命令 : " + "tar -vxf " + targetDir + SEPARATOR + file.getName());
      //      String tarxvf = "tar -vxf " + targetDir + SEPARATOR + file.getName();
      //      SFTPChannel.execCommandByJSch(shellSession, tarxvf);
      //
      //      // 删除上传后的 GZ包
      //      logger.info("删除上传的GZ包命令 : " + "rm -rf " + targetDir + SEPARATOR + file.getName());
      //      SFTPChannel.execCommandByJSch(
      //          shellSession, "rm -rf " + targetDir + SEPARATOR + file.getName());

      // 本地当前工程下的配置文件信息,上传到目标机器
      String localConfPath = System.getProperty("user.dir") + SEPARATOR + "resources";
      logger.info("本地当前工程下的配置文件路径 : " + localConfPath);
      sftpFiles(localConfPath, chSftp, targetDir);

      // 二 : 将本地写的agent配置文件,sftp复制到agent部署的目标机器
      sftpFiles(CONFPATH, chSftp, targetDir + SEPARATOR + "resources");

      // 三 : 将储存层上传的文件 SFTP 到agent目录下
      ScpHadoopConf.scpConfToAgent(targetDir, chSftp, shellSession);

      // 四 : 将需要的jar包 SFTP 到agent下
      sftpFiles(
          new File(System.getProperty("user.dir")).getParent() + SEPARATOR + "lib",
          chSftp,
          down_info.getSave_dir() + SEPARATOR + agentDirName);

      // 五 : 判断是否启动agent
      if (IsFlag.Shi.getCode().equals(down_info.getDeploy())) {
        logger.info(
            "启动agent命令 : cd "
                + targetDir
                + ";nohup java -Dorg.eclipse.jetty.server.Request.maxFormContentSize=99900000 -jar hrds_Agent-5.0.jar &");
        SFTPChannel.execCommandByJSchNoRs(
            shellSession,
            "cd "
                + targetDir
                + ";nohup java -Dorg.eclipse.jetty.server.Request.maxFormContentSize=99900000 -jar hrds_Agent-5.0.jar &");
      }

    } catch (Exception e) {
      logger.error("", e);
      throw new BusinessException(e.getMessage());
    } finally {
      if (shellSession != null) {
        shellSession.disconnect();
      }
      if (chSftp != null) {
        chSftp.quit();
      }
      if (channel != null) {
        try {
          channel.closeChannel();
        } catch (Exception e) {
          throw new BusinessException("通道关闭失败!!!");
        }
      }
      // 删除本地临时的配置文件
      new File(CONFPATH).delete();
    }
  }

  private static Session getSession(Agent_down_info down_info) {
    // 开始JSCH Session连接
    Session jSchSession = null;
    try {
      // 创建准备SFTP所需要的参数bean
      SFTPDetails sftpDetails = new SFTPDetails();
      sftpDetails.setHost(down_info.getAgent_ip());
      logger.info("IP : " + down_info.getAgent_ip());
      sftpDetails.setPort(22);
      sftpDetails.setUser_name(down_info.getUser_name());
      logger.info("user_name : " + down_info.getUser_name());
      sftpDetails.setPwd(down_info.getPasswd());
      logger.info("password : " + down_info.getPasswd());
      jSchSession = SFTPChannel.getJSchSession(sftpDetails, 0);
    } catch (JSchException e) {
      throw new BusinessException("建立 Session失败!!!");
    }

    return jSchSession;
  }

  private static void sftpFiles(String sftpDir, ChannelSftp chSftp, String targetDir)
      throws SftpException {
    File file = new File(sftpDir);
    File[] confFiles = file.listFiles();
    for (int i = 0; i < confFiles.length; i++) {
      if (confFiles[i].isDirectory()) {
        sftpFiles(
            confFiles[i].getAbsolutePath(),
            chSftp,
            targetDir + SEPARATOR + new File(confFiles[i].getParent()).getName());
      } else {
        chSftp.put(
            confFiles[i].getAbsolutePath(),
            targetDir + SEPARATOR + new File(confFiles[i].getParent()).getName(),
            new FileProgressMonitor(confFiles[i].length()),
            ChannelSftp.OVERWRITE);
      }
    }
  }

  private static void mkdirToTarget(Session shellSession, String targetDir)
      throws IOException, JSchException {
    /**
     * .bin : 部署Agent的隐藏目录 storeConfigPath : 上传的配置文件根目录 lib : 需要的依赖jar包目录 resources : 配置文件根目录
     * fdconfig : 配置信息文件 i18n : 翻译的配置文件
     */
    String[] targetch_machine = {".bin", "storeConfigPath", "lib", "resources", "fdconfig", "i18n"};
    String rootDir = targetDir + SEPARATOR + targetch_machine[0];
    // 建立  .bin 隐藏目录
    logger.info("创建远程目录 .bin  : " + rootDir);
    SFTPChannel.execCommandByJSch(shellSession, "mkdir -p " + rootDir);

    // 建立  lib 目录
    logger.info("创建远程目录 lib  : " + targetDir + SEPARATOR + targetch_machine[2]);
    SFTPChannel.execCommandByJSch(
        shellSession, "mkdir -p " + targetDir + SEPARATOR + targetch_machine[2]);

    // 建立storeConfigPath目录
    logger.info("创建远程目录 建立storeConfigPath目录  : " + rootDir + SEPARATOR + targetch_machine[1]);
    SFTPChannel.execCommandByJSch(
        shellSession, "mkdir -p " + rootDir + SEPARATOR + targetch_machine[1]);

    // 建立 resource/fdconfig 目录
    logger.info(
        "创建远程目录storeConfigPath目录  : "
            + rootDir
            + SEPARATOR
            + targetch_machine[3]
            + SEPARATOR
            + targetch_machine[4]);
    SFTPChannel.execCommandByJSch(
        shellSession,
        "mkdir -p " + rootDir + SEPARATOR + targetch_machine[3] + SEPARATOR + targetch_machine[4]);

    // 建立 resource/i18n 目录
    SFTPChannel.execCommandByJSch(
        shellSession,
        "mkdir -p " + rootDir + SEPARATOR + targetch_machine[3] + SEPARATOR + targetch_machine[5]);
  }
}