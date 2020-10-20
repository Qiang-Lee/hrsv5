||模块|分类|功能|特性|
|:--|:--|:--|:--|:--|
|||生产测试态|导入导出|系统对采集、集市、作业等功能需要有导出加密文件，在导入到生产环境中，需要有审核功能，如：新增什么内容、删除什么内容、修改什么内容等，同时作业直接的影响，表之间的影响|
|||输入sql||sql需要有提示，可以格式化等，提供一个易用的sql编辑器，目前使用的是ace|
|||agent打包|agent打包jre|Agent等需要在本机部署的东西，都把JRE（不是JDK）打包进去|
|||支持hadoop模块|数据存储层|存储层类型，添加hbase、solr、hbase+solr、hudi、carbondata等hadoop的存储|
|||码值维护功能-统一码值-系统对应码值||维护码值，提供数据清洗中使用|
||数据采<br/>集管理|数据库agent(任务管理)||发送和生成作业，一键生成作业|
|||数据库agent(日志查看)||通过画面查看agent的日志功能|
|||貼源登记|贴源登记|将已有的数据以登记的方式记录到海云平台，后续所有的功能都可以使用登记后的表数据进行完成各种操作，包括集市、加工、接口等|
||||定义表抽取属性||
||||选择表，主键、分区||
|||数据库采集|采集周期，设置秒数<br/>，不停的采集|使用jdbc的方式连接数据库，不落地文件直接将数据转存到目的地（新的agent类型）|
||||||
|||数据抽数|数据字典生成|在数据库抽数配置完成后，最后一步可以直接生成数据字典，并不需要等数据卸数完成后才生成，且可以进行数据字典下载功能|
||||ok文件生成|是否生成ok文件，如果生成ok文件，在数据文件卸完成后，在同级目录下生成一个与表名一样的后缀为.ok的文件，文件内容为空|
||||定义卸数文件|csv、定长、非定长需要定义是否有表头的选择|
||||立即启动|缺少立即执行，也就是说不通过作业调度，只执行一次|
||||不依赖agent部署|agent独立运行，不依赖服务的状态，可以离线部署，可以将生成好的json下载到本地进行部署|
||||excel导入功能|通过定义excel将采集的一步到第五步定义处理，使用导入功能，将数据直接导入到系统中，不用一步一步的通过画面点击|
||||什么是大字段|需要有一个配置，配置什么类型的是大字段，如longvarche、text，同时需要有是否需要单独卸成文件，或直接以字符串的方式存在数据文件中|
||||增量数据文件|增量抽取文件只实现了定长文件的卸数,应该有非定长如固定分隔符等|
|||非结构化采集|数据存储层|数据存储层限制，没有对存储层进行开发，并且启动方式需要重新开发（这里需要重新讨论）|
||||启动方式|定义作业的部署方式，同时需要支持一键启动，不通过作业调度|
||||支持hadoop模块|支持本地合并文件或不合并、支持合并文件上传hdfs，支持非机构化数据进solr+hbase等方式，使用存储层方式|
|||DB文件采集|立即启动|缺少立即执行，也就是说不通过作业调度，只执行一次|
||||不依赖agent部署|agent独立运行，不依赖服务的状态，可以离线部署，可以将生成好的json下载到本地进行部署|
||||excel导入功能|通过定义excel将采集的一步到第五步定义处理，使用导入功能，将数据直接导入到系统中，不用一步一步的通过画面点击|
||||大字段是否加载|需要有一个配置，配置如果是单个文件存在的大字段，是否直接加载到库里面啊，如果是应该读数据加载，否就是一个占位符（记录文件在哪里）|
||||增量数据文件|增量文件，定长、非定长（固定分隔符）的加载|
||||支持hadoop模块|数据采集模块，支持数据进hbase、solr、hbase+solr等hadoop的存储|
|||半结构化采集|数据存储层|数据存储层限制，没有对存储层进行开发，并且启动方式需要重新开发（这里需要重新讨论）|
||||启动方式|定义作业的部署方式，同时需要支持一键启动，不通过作业调度|
||||支持hadoop模块|数据采集模块，支持数据进hbase、solr、hbase+solr等hadoop的存储|
|||FTP采集|启动方式|定义作业的部署方式，同时需要支持一键启动，不通过作业调度|
||SQL控制台|表查询||支持多存储层的联合查询，跨数据库查询，使用spark完成，目前支持只不过是修改了spark的源码，找到不修改spark源码的方式，确认spark的启动方式，使用spark自身的集群模式|
|||SQL查询|||
|||数据对标|数据对标||
||||数据管控新增表|在数据管控中，可以在每个存储层下面新增空表、回收站删除所有表，恢复所有表的|
|||数据管控|错误结果导出|数据质量检测对于有问题的数据出问题数据列表清单，并且可以导出excel|
||||删除、新增|删除新增数据表，支持hive、hbase|
||||规则分类配置|数据质量检测可以对规则增加分类，问题数据可以按分类统计|
||数据加工<br/>集市|数据集市|添加后处理功能|针对可以update、delete的存储成，可以对集市进行前处理，提供sql处理的方式|
||||集市分类|支持类似加工的分类方式，可以使用树进行展现每个加工数据的层级关系|
||||映射关系|支持类似加工的映射方式（映射并修改、分组映射），并可以支持写函数操作|
||||进数方式|目前是增量、追加、替换，需要支持直接更新的方式|
||||操作|支持选择表，选择关联关系自动生成sql|
||||工程生成作业|集市根据工程一键生成所有工程下的作业|
||||前后作业|支持前后作业，前作业只支持关系型数据库|
||||支持预聚合操作|数据选择cb 支持预聚合的创建|
||||支持hadoop模块|数据集市支持直接更新，支持数据进hbase、solr、hbase+solr、hudi、carbondata等hadoop的存储，同时支持外部文件的导出|
||版本管理|数据结构对比||所有数据表结构对历史修改情况进行保存，并进行对比展现|
|||数据mapping对比||对集市数据sql的mapping进行保存并展现|
||服务<br/>接口用户|服务接口用户|接口功能开发<br/>（36个接口）|1、表结构查询-获取json信息接口<br/>2、表分页查询（针对关系型数据库）<br/>3、单表数据操作（修改、删除数据）接口<br/>4、创建表接口（有溯源无溯源）<br/>5、数据新增接口<br/>6、表批量操作接口（根据数据文件，新增、删除、修改）接口-包括是否拉链存储<br/>7、文件属性搜索接口|

