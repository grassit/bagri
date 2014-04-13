package com.bagri.common.query;

public class Expression {

	protected int docType;
	protected PathBuilder path; 
	protected Comparison compType;
	
	public Expression(int docType, Comparison compType, PathBuilder path) {
		this.docType = docType;
		this.compType = compType;
		this.path = path;
	}
	
	public int getDocType() {
		return this.docType;
	}
	
	public Comparison getCompType() {
		return this.compType;
	}

	public PathBuilder getPath() {
		return this.path;
	}
	
	public String getFullPath() {
		return path.getFullPath();
	}
	
	//public void addPath(String add) {
	//	this.path += add;
	//}
}
