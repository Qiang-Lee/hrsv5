package hrds.codes;
/**Created by automatic  */
/**代码类型名：自主取数数据来源  */
public enum AutoDataSource {
	/**内部<NeiBu>  */
	NeiBu("01","内部","163","自主取数数据来源"),
	/**外部<WaiBu>  */
	WaiBu("02","外部","163","自主取数数据来源");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	AutoDataSource(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String getValue(String code) {
		for (AutoDataSource typeCode : AutoDataSource.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode.value;
			}
		}
		throw new RuntimeException("根据"+code+"没有找到对应的代码项");
	}

	/**根据指定的代码值转换成对象
	* @param code   本代码的代码值
	* @return
	*/
	public static AutoDataSource getCodeObj(String code) {
		for (AutoDataSource typeCode : AutoDataSource.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode;
			}
		}
		throw new RuntimeException("根据code没有找到对应的代码项");
	}

	/**
	* 获取代码项的中文类名名称
	* @return
	*/
	public static String getObjCatValue(){
		return AutoDataSource.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return AutoDataSource.values()[0].getCatCode();
	}
}
