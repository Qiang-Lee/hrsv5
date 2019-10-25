package hrds.commons.hadoop.readconfig;


import fd.ng.core.utils.StringUtil;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.PropertyParaValue;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;


public class HDFSFileSystem {

	private static final Log log = LogFactory.getLog(HDFSFileSystem.class);

	private Configuration conf;

	private FileSystem fileSystem;

	public HDFSFileSystem() {
		this(null);
	}

	public HDFSFileSystem(String configPath) {

		try {
			if (PropertyParaValue.getString("platform", "normal").equals(ConfigReader.PlatformType.normal.toString())) {

				conf = ConfigReader.getConfiguration();
				fileSystem = FileSystem.get(conf);
				log.info("normal FileSystem inited ");
			} else if (PropertyParaValue.getString("platform", "normal").equals(ConfigReader.PlatformType.cdh5_13.toString())) {
				LoginUtil lg;
				if (StringUtil.isEmpty(configPath)) {
					lg = new LoginUtil();
				} else {
					lg = new LoginUtil(configPath);
				}
				conf = lg.confLoad(conf);
				conf = lg.authentication(conf);
				fileSystem = FileSystem.get(conf);
				log.info("cdh5_13 FileSystem inited ");
			} else if (PropertyParaValue.getString("platform", "normal").equals(ConfigReader.PlatformType.fic50.toString())) {
				conf = SecurityUtils.confLoad(conf);
				conf = SecurityUtils.authentication(conf);
				fileSystem = FileSystem.get(conf);
				log.info("fi FileSystem inited ");
			} else if (PropertyParaValue.getString("platform", "normal").equals(ConfigReader.PlatformType.fic80.toString())) {
				LoginUtil lg;
				if (StringUtil.isEmpty(configPath)) {
					lg = new LoginUtil();
				} else {
					lg = new LoginUtil(configPath);
				}
				conf = lg.confLoad(conf);
				conf = lg.authentication(conf);
				fileSystem = FileSystem.get(conf);
				log.info("fic60 FileSystem inited ");
			} else if (PropertyParaValue.getString("platform", "normal").equals(ConfigReader.PlatformType.fic60.toString())) {
				LoginUtil lg;
				if (StringUtil.isEmpty(configPath)) {
					lg = new LoginUtil();
				} else {
					lg = new LoginUtil(configPath);
				}
				conf = lg.confLoad(conf);
				conf = C80LoginUtil.login(conf);
				fileSystem = FileSystem.get(conf);
				log.info("fic80 FileSystem inited ");
			} else {
				throw new BusinessException("The platform is a wrong type ,please check the syspara table for the argument <platform>...");
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public FileSystem getFileSystem() {

		return fileSystem;
	}

	public Configuration getConfig() {

		return conf;
	}

	public Path getWorkingDirectory(String directory) {

		if (StringUtils.isBlank(directory))
			return fileSystem.getWorkingDirectory();
		fileSystem.setWorkingDirectory(new Path(fileSystem.getConf().get("fs.default.name") + Path.SEPARATOR + directory + Path.SEPARATOR));
		return fileSystem.getWorkingDirectory();
	}

	public void close() {

		try {
			if (null != fileSystem)
				fileSystem.close();
			log.debug("FileSystem closed ");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

}
