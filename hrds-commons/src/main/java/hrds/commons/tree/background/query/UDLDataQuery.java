package hrds.commons.tree.background.query;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.web.util.Dbo;
import hrds.commons.codes.StoreLayerDataSource;
import hrds.commons.entity.Data_store_layer;
import hrds.commons.entity.Dq_table_column;
import hrds.commons.entity.Dq_table_info;
import hrds.commons.entity.Dtab_relation_store;
import hrds.commons.exception.BusinessException;

import java.util.List;
import java.util.Map;

@DocClass(desc = "自定义层(UDL)数据信息查询类", author = "BY-HLL", createdate = "2020/7/7 0007 上午 11:14")
public class UDLDataQuery {

    @Method(desc = "获取自定义层(UDL)下表信息", logicStep = "获取自定义层(UDL)下表信息")
    @Param(name = "table_id", desc = "表id,唯一", range = "long类型")
    @Return(desc = "返回值说明", range = "返回值取值范围")
    public static Dq_table_info getUDLTableInfo(String table_id) {
        //设置自定义层数据表
        Dq_table_info dq_table_info = new Dq_table_info();
        dq_table_info.setTable_id(table_id);
        //查询自定义层数据表信息
        return Dbo.queryOneObject(Dq_table_info.class, "select * from " + Dq_table_info.TableName + " where table_id" +
                "=?", dq_table_info.getTable_id()).orElseThrow(() -> new BusinessException("获取UDL数据表信息的sql失败!"));
    }

    @Method(desc = "获取自定义层(UDL)下表字段信息", logicStep = "获取自定义层(UDL)下表字段信息")
    @Param(name = "table_id", desc = "表id,唯一", range = "long类型")
    @Return(desc = "返回值说明", range = "返回值取值范围")
    public static List<Map<String, Object>> getUDLTableColumns(String table_id) {
        //初始化sql
        SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
        asmSql.clean();
        //设置自定义层数据表
        Dq_table_info dq_table_info = new Dq_table_info();
        dq_table_info.setTable_id(table_id);
        //查询表字段信息
        asmSql.addSql("select field_id AS column_id,column_name as column_name," +
                " field_ch_name as column_ch_name,concat(column_type,'(',column_length,')') AS column_type," +
                " '0' AS is_primary_key FROM " + Dq_table_column.TableName + " WHERE table_id=?");
        asmSql.addParam(dq_table_info.getTable_id());
        return Dbo.queryList(asmSql.sql(), asmSql.params());
    }

    @Method(desc = "UDL数据层下,获取配置登记的所有数据存储层信息", logicStep = "UDL数据层下,获取配置登记的所有数据存储层信息")
    @Return(desc = "所有存储层登记信息列表", range = "所有存储层登记信息列表")
    public static List<Data_store_layer> getUDLExistTableDataStorageLayers() {
        return Dbo.queryList(Data_store_layer.class, "SELECT * from " + Data_store_layer.TableName + " dsl" +
                " JOIN " + Dtab_relation_store.TableName + " dtrs ON dtrs.dsl_id = dsl.dsl_id" +
                " WHERE dtrs.data_source=?", StoreLayerDataSource.UD.getCode());
    }


    @Method(desc = "数据管控-源数据列表获取UDL数据存储层下的表信息", logicStep = "数据管控-源数据列表获取数据存储层下的表信息")
    @Param(name = "data_store_layer", desc = "Data_store_layer实体对象", range = "Data_store_layer实体对象")
    @Return(desc = "返回值说明", range = "返回值取值范围")
    public static List<Map<String, Object>> getUDLStorageLayerTableInfos(Data_store_layer data_store_layer) {
        return Dbo.queryList("SELECT * FROM " + Dq_table_info.TableName + " dti" +
                " LEFT JOIN " + Dtab_relation_store.TableName + " dtrs ON dtrs.tab_id=dti.table_id" +
                " LEFT JOIN " + Data_store_layer.TableName + " dsl ON dsl.dsl_id=dtrs.dsl_id" +
                " WHERE dtrs.data_source=? AND dsl.dsl_id=?", StoreLayerDataSource.UD.getCode(), data_store_layer.getDsl_id());
    }
}
