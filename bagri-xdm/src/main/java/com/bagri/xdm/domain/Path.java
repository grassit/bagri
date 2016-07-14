package com.bagri.xdm.domain;


/**
 * Meta-data for XDM elements.
 *  
 * @author Denis Sukhoroslov
 * @since 05.2013 
 */
public class Path implements Comparable<Path> { 
	
	private String path;
	private int typeId;
	private NodeKind kind;
	private int pathId;
	private int parentId;
	private int postId;
	// the type constant from javax.xml.xquery.XQItemType.*
	// change it to QName?
	private int dataType;
	private Occurrence occurrence = Occurrence.zeroOrOne;
	
	// cache it!
	private String name = null; 
	
	/**
	 * default constructor
	 */
	public Path() {
		//..
	}
	
	/**
	 * 
	 * @param path the node path
	 * @param typeId the node type id
	 * @param kind the node kind
	 * @param pathId the node path id
	 * @param parentId the node parent id
	 * @param postId the node post id
	 * @param dataType the node data type
	 * @param occurrence the node occurrence
	 */
	public Path(String path, int typeId, NodeKind kind, int pathId, int parentId, int postId, 
			int dataType, Occurrence occurrence) {
		super();
		this.path = path;
		this.typeId = typeId;
		this.kind = kind;
		this.pathId = pathId;
		this.parentId = parentId;
		this.postId = postId;
		this.dataType = dataType;
		if (occurrence != null) {
			this.occurrence = occurrence;
		}
	}
	
	/**
	 * 
	 * @return node occurence
	 */
	public Occurrence getOccurrence() {
		return occurrence;
	}
	
	/**
	 * @return XQJ data type
	 */
	public int getDataType() {
		return dataType;
	}
	
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * @return the last path portion
	 */
	public String getName() {
		if (kind == NodeKind.document || kind == NodeKind.comment) {
			return null;
		}
		
		if (name == null) {
			String last;
			String[] segments = path.split("/");
	
			switch (kind) {
				case attribute: //@
				case namespace: //#
				case pi: 		//?
					last = segments[segments.length-1];
					name = last.substring(1);
					break;
				case text: 
					name = segments[segments.length-2];
					break;
				case element:
					if (segments.length > 0) {
						name = segments[segments.length-1];
					} else {
						name = path;
					}
					break;
				//case document:
				//case comment:
				default:
					return null;
			}
		}
		return name;
	}
	
	/**
	 * @return the node kind
	 */
	public NodeKind getNodeKind() {
		return kind;
	}
	
	/**
	 * @return the type id
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * @return the path id
	 */
	public int getPathId() {
		return pathId;
	}
	
	/**
	 * @return the parent path id
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @return the post path id
	 */
	public int getPostId() {
		return postId;
	}
	
	/**
	 * @param parentId the parent id to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	/**
	 * @param postId the post id to set
	 */
	public void setPostId(int postId) {
		this.postId = postId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 + path.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Path other = (Path) obj;
		return path.equals(other.path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Path [path=" + path + ", pathId=" + pathId + ", typeId="
				+ typeId + ", kind=" + kind + ", parentId=" + parentId
				+ ", postId=" + postId + ", dataType=" + dataType
				+ ", occurrence=" + occurrence.toString() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Path other) {
		return this.pathId - other.pathId;
	}
	
}