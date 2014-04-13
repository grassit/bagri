package com.bagri.common.query;

public class BinaryExpression extends Expression {

	private Expression left;
	private Expression right;

	public BinaryExpression(int docType, Comparison compType, PathBuilder path) {
		super(docType, compType, path);
	}
	
	public Expression getLeft() {
		return this.left;
	}
	
	public void setLeft(Expression left) {
		this.left = left;
	}
	
	public Expression getRight() {
		return this.right;
	}
	
	public void setRight(Expression right) {
		this.right = right;
	}

	@Override
	public String toString() {
		return "BinaryExpression [docType=" + docType + ", compType=" + compType + 
				", path=" + path + ", left=" + left + ", right=" + right + "]";
	}

	
}
