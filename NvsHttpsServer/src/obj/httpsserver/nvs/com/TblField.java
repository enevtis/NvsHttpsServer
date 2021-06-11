package obj.httpsserver.nvs.com;

public class TblField {

	public String fieldName = "";
	public String fieldType = "";
	public int fieldWidth = 10;
	public String refTable = "";
	public String refKeyField = "";
	public String refValueField = "";
	public String refKeyFieldType = "";
	public String defaultValue = "";
	public boolean isChangeable = false; 
	public String fieldLabel = "";
	
	public TblField() {
	}
	
	public TblField(String fieldName,String fieldType, int fieldWidth) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.fieldWidth = fieldWidth;
	}
	
	public TblField(String fieldName,String fieldType, int fieldWidth, boolean isChangeable) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.fieldWidth = fieldWidth;
		this.isChangeable = isChangeable;
	}

	public TblField(String fieldName,String fieldType, int fieldWidth, String refTable,String refKeyField,String refValueField) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.fieldWidth = fieldWidth;
		this.refTable = refTable;
		this.refKeyField = refKeyField;
		this.refValueField = refValueField;
	}
	
}
