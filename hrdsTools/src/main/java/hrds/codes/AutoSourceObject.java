package hrds.codes;
/**Created by automatic  */
/**代码类型名：可视化源对象  */
public enum AutoSourceObject {
	/**自主数据数据集<ZiZhuShuJuShuJuJi>  */
	ZiZhuShuJuShuJuJi("01","自主数据数据集","158","可视化源对象"),
	/**系统级数据集<XiTongJiShuJuJi>  */
	XiTongJiShuJuJi("02","系统级数据集","158","可视化源对象"),
	/**数据组件数据集<ShuJuZuJianShuJuJi>  */
	ShuJuZuJianShuJuJi("03","数据组件数据集","158","可视化源对象");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	AutoSourceObject(String code,String value,String catCode,String catValue){
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
		for (AutoSourceObject typeCode : AutoSourceObject.values()) {
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
	public static AutoSourceObject getCodeObj(String code) {
		for (AutoSourceObject typeCode : AutoSourceObject.values()) {
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
		return AutoSourceObject.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return AutoSourceObject.values()[0].getCatCode();
	}
}
