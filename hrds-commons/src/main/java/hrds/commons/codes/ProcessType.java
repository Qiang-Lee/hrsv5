package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：数据处理方式  */
public enum ProcessType {
	/**定值<DingZhi>  */
	DingZhi("1","定值","91","数据处理方式"),
	/**自增<ZiZeng>  */
	ZiZeng("2","自增","91","数据处理方式"),
	/**映射赋值<YingShe>  */
	YingShe("3","映射赋值","91","数据处理方式"),
	/**函数映射<HanShuYingShe>  */
	HanShuYingShe("4","函数映射","91","数据处理方式"),
	/**分组映射<FenZhuYingShe>  */
	FenZhuYingShe("5","分组映射","91","数据处理方式");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	ProcessType(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}
	public static final String CodeName = "ProcessType";

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (ProcessType typeCode : ProcessType.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode.value;
			}
		}
		throw new AppSystemException("根据"+code+"没有找到对应的代码项");
	}

	/**根据指定的代码值转换成对象
	* @param code   本代码的代码值
	* @return
	*/
	public static ProcessType ofEnumByCode(String code) {
		for (ProcessType typeCode : ProcessType.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode;
			}
		}
		throw new AppSystemException("根据"+code+"没有找到对应的代码项");
	}

	/**
	* 获取代码项的中文类名名称
	* @return
	*/
	public static String ofCatValue(){
		return ProcessType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return ProcessType.values()[0].getCatCode();
	}

	/**
	* 禁止使用类的tostring()方法
	* @return
	*/
	@Override
	public String toString() {
		throw new AppSystemException("There's no need for you to !");
	}
}
