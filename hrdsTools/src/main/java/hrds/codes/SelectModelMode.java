package hrds.codes;
/**Created by automatic  */
/**代码类型名：模型选择方式  */
public enum SelectModelMode {
	/**专家建模<ZhuanJiaJianMo>  */
	ZhuanJiaJianMo("1","专家建模","65"),
	/**ARIMA<ARIMA>  */
	ARIMA("2","ARIMA","65");

	private final String code;
	private final String value;
	private final String catCode;

	SelectModelMode(String code,String value,String catCode){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
}
