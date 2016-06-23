package org.restcameracontrol.beans;

public class CameraWidget {
	private String name;
	private String label;
	private String value;
	private int typeId;
	private String typeName;
	private String[] choices;
	private Float rangeMin;
	private Float rangeMax;
	private Float rangeIncrement;
	private boolean readOnly;
	private String info;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String[] getChoices() {
		return choices;
	}
	public void setChoices(String[] choices) {
		this.choices = choices;
	}
	public Float getRangeMin() {
		return rangeMin;
	}
	public void setRangeMin(Float rangeMin) {
		this.rangeMin = rangeMin;
	}
	public Float getRangeMax() {
		return rangeMax;
	}
	public void setRangeMax(Float rangeMax) {
		this.rangeMax = rangeMax;
	}
	public Float getRangeIncrement() {
		return rangeIncrement;
	}
	public void setRangeIncrement(Float rangeIncrement) {
		this.rangeIncrement = rangeIncrement;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	
}
