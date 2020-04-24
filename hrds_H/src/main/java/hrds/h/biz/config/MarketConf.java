package hrds.h.biz.config;


import hrds.commons.entity.*;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: Mick Yuan
 * @Date: 20-3-31 下午5:34
 * @Since jdk1.8
 */
public class MarketConf implements Serializable {

    /**
     * 集市任务设置id
     */
    private final String datatableId;
    /**
     * 调度日期
     */
    private final String etlDate;
    /**
     * sql动态参数
     */
    private final String sqlParams;
    /**
     * 是否是属于重跑
     * 当属于重跑时，需要将当天已经跑过的数据清除掉
     */
    private boolean rerun;
    /**
     * 是否属于第一次运行
     * 第一次运行时，逻辑与其他不同
     */
    private boolean firstLoad;
    /**
     * 目的地表名
     */
    private String tableName;
    /**
     * 需要执行的sql
     */
    private String completeSql;
    /**
     * 数据表信息
     */
    private Dm_datatable dmDatatable = null;
    /**
     * 数据集市信息表
     */
    private Dm_info dmInfo = null;
    /**
     * 数据操作信息表
     */
    private Dm_operation_info dmOperationInfo = null;
    /**
     * 集市字段信息
     */
    private List<Datatable_field_info> datatableFields;
    /**
     * 集市表存储关系表
     */
    private Dm_relation_datatable dmRelationDatatable = null;
    /**
     * 集市存储层配置表
     */
    private Data_store_layer dataStoreLayer = null;
    /**
     * 数据存储层配置属性表
     */
    private List<Data_store_layer_attr> dataStoreLayerAttrs = null;


    private MarketConf(String datatableId, String etldate, String sqlParams) {
        this.datatableId = datatableId;
        this.etlDate = etldate;
        this.sqlParams = sqlParams;
    }

    public static MarketConf getConf(String datatableId, String etldate, String sqlParams) {

        //验证输入参数合法性
        MarketConfUtils.checkArguments(datatableId, etldate);
        final MarketConf conf = new MarketConf(datatableId, etldate, sqlParams);
        //初始化实体类
        MarketConfUtils.initBeans(conf);
        //验证是否是首次运行
        MarketConfUtils.checkFirstLoad(conf);
        //验证是否属于重跑
        MarketConfUtils.checkReRun(conf, etldate);

        return conf;
    }

    public boolean isRerun() {
        return rerun;
    }

    void setRerun(boolean rerun) {
        this.rerun = rerun;
    }

    public boolean isFirstLoad() {
        return firstLoad;
    }

    void setFirstLoad(boolean firstLoad) {
        this.firstLoad = firstLoad;
    }

    public String getDatatableId() {
        return datatableId;
    }

    public String getEtlDate() {
        return etlDate;
    }

    public String getSqlParams() {
        return sqlParams;
    }

    void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCompleteSql() {
        return completeSql;
    }

    void setCompleteSql(String completeSql) {
        this.completeSql = completeSql;
    }

    public Dm_datatable getDmDatatable() {
        return dmDatatable;
    }

    void setDmDatatable(Dm_datatable dmDatatable) {
        this.dmDatatable = dmDatatable;
    }

    public Dm_operation_info getDmOperationInfo() {
        return dmOperationInfo;
    }

    void setDmOperationInfo(Dm_operation_info dmOperationInfo) {
        this.dmOperationInfo = dmOperationInfo;
    }

    public List<Datatable_field_info> getDatatableFields() {
        return datatableFields;
    }

    void setDatatableFields(List<Datatable_field_info> datatableFields) {
        this.datatableFields = datatableFields;
    }

    public Dm_relation_datatable getDmRelationDatatable() {
        return dmRelationDatatable;
    }

    void setDmRelationDatatable(Dm_relation_datatable dmRelationDatatable) {
        this.dmRelationDatatable = dmRelationDatatable;
    }

    public Data_store_layer getDataStoreLayer() {
        return dataStoreLayer;
    }

    void setDataStoreLayer(Data_store_layer dataStoreLayer) {
        this.dataStoreLayer = dataStoreLayer;
    }

    public List<Data_store_layer_attr> getDataStoreLayerAttrs() {
        return dataStoreLayerAttrs;
    }

    void setDataStoreLayerAttrs(List<Data_store_layer_attr> dataStoreLayerAttrs) {
        this.dataStoreLayerAttrs = dataStoreLayerAttrs;
    }
}