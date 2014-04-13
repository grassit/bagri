package com.bagri.xdm.access.api;

public class XDMCacheConstants {

    /**
     * XDM Document cache
     * Key: Long; Value: com.bagri.xdm.XDMDocument
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_DOCUMENT = "xdm-document";

    /**
      * XDM Data cache
      * Key: DataDocumentKey; Value: com.bagri.xdm.XDMData
      * CacheStore: no
      * Mapped as: no
      */
	public static final String CN_XDM_ELEMENT = "xdm-element";

    /**
     * XDM Path cache
     * Key: PathDocumentKey; Value: com.bagri.xdm.XDMIndex<null>
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_PATH_INDEX = "xdm-path-index";

    /**
     * XDM Path-Value cache
     * Key: PathValueDocumentKey; Value: com.bagri.xdm.XDMIndex<String>
     * CacheStore: no
     * Mapped as: no
     */
	//public static final String CN_XDM_PATH_VALUE_INDEX = "xdm-path-value-index";
	
    /**
     * XDM Namespace cache
     * Key: String (NS URI); Value: com.bagri.xdm.XDMNamespace
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_NAMESPACE_DICT = "dict-namespace";

    /**
     * XDM Path cache
     * Key: String (QName); Value: com.bagri.xdm.XDMPath
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_PATH_DICT = "dict-path";

    /**
     * XDM Document cache
     * Key: Long; Value: com.bagri.xdm.XDMDocument
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_DOCTYPE_DICT = "dict-document-type";

	// Sequence/IdGen names
    public static final String SQN_DOCUMENT = "xdm.document";
    public static final String SQN_NAMESPACE = "xdm.namespace";
    public static final String SQN_PATH = "xdm.path";
    public static final String SQN_DOCTYPE = "xdm.document.type";
    public static final String SQN_ELEMENT = "xdm.element";
    
}
