package com.bagri.xquery.saxon;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.instruct.UserFunctionParameter;
import net.sf.saxon.functions.ExecutableFunctionLibrary;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import com.bagri.xdm.system.XDMFunction;
import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xquery.api.XQCompiler;
import com.bagri.xquery.saxon.extension.StaticFunctionExtension;

public class XQCompilerImpl implements XQCompiler {
	
	private static final Logger logger = LoggerFactory.getLogger(XQCompilerImpl.class);
	
	private Properties props = new Properties();
	
    private Configuration config;
	private List<XDMLibrary> libraries = new ArrayList<>();
	
    public XQCompilerImpl() {
    	initializeConfig();
    }

	@Override
	public Properties getProperties() {
		return props;
	}

	@Override
	public void setProperty(String name, Object value) {
		props.setProperty(name, value.toString());
	}

	@Override
	public void compileQuery(String query) {
		long stamp = System.currentTimeMillis();
		logger.trace("compileQuery.enter; got query: {}", query);
		StaticQueryContext sqc = null;
		try {
		    sqc = prepareStaticContext(null);
			XQueryExpression exp = sqc.compileQuery(query);
		} catch (XPathException ex) {
			List<TransformerException> errors = ((LocalErrorListener) sqc.getErrorListener()).getErrors();
			StringBuffer buff = new StringBuffer();
			for (TransformerException tex: errors) {
				buff.append(tex.getMessageAndLocation()).append("\n");
			}
			String error = buff.toString();
			//logger.error("compileQuery.error", ex);
			logger.info("compileQuery.error; message: {}", error);
			throw new RuntimeException(error);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("compileQuery.exit; time taken: {}", stamp); 
	}

	@Override
	public void compileModule(XDMModule module) {
		long stamp = System.currentTimeMillis();
		logger.trace("compileModule.enter; got module: {}", module);
		XQueryExpression exp = getModuleExpression(module);
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("compileModule.exit; time taken: {}", stamp); 
	}
	
	@Override
	public List<String> getModuleFunctions(XDMModule module) {
		long stamp = System.currentTimeMillis();
		logger.trace("getModuleFunctions.enter; got module: {}", module);
		XQueryExpression exp = getModuleExpression(module);
		List<String> result = lookupFunctions(exp.getExecutable().getFunctionLibrary());
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getModuleFunctions.exit; time taken: {}; returning: {}", stamp, result);
		return result;
	}

	@Override
	public boolean getModuleState(XDMModule module) {
		try {
			String query = "import module namespace test=\"" + module.getNamespace() + 
					"\" at \"" + module.getName() + "\";\n\n";
			query += "1213";
		    StaticQueryContext sqc = prepareStaticContext(module.getBody());
			logger.trace("getModuleExpression; compiling query: {}", query);
			sqc.compileQuery(query);
			return true;
		} catch (XPathException ex) {
			return false;
		}
	}
	
	@Override
	public void setLibraries(Collection<XDMLibrary> libraries) {
		this.libraries.clear();
		this.libraries.addAll(libraries);
		//config.registerExtensionFunction(function);
		initializeConfig();
	}
	
	private void initializeConfig() {
		
		logger.trace("initializeConfig.enter; current config: {}", config);
        config = Configuration.newConfiguration();
        config.setHostLanguage(Configuration.XQUERY);
        config.setSchemaValidationMode(Validation.STRIP);
        //config.setConfigurationProperty(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, Boolean.TRUE);

        if (libraries != null) {
        	registerExtensions(config, libraries);
        }
		logger.trace("initializeConfig.exit; new config: {}", config);
	}
	
	static void registerExtensions(Configuration config, Collection<XDMLibrary> libraries) {
		for (XDMLibrary lib: libraries) {
			for (XDMFunction func: lib.getFunctions()) {
				try {
					ExtensionFunctionDefinition efd = new StaticFunctionExtension(func);
					logger.trace("registerExtensions; funtion {} registered as {}", func.toString(), efd.getFunctionQName()); 
					config.registerExtensionFunction(efd);
				} catch (Exception ex) {
					logger.warn("registerExtensions; error registering function {}: {}; skipped", func.toString(), ex.getMessage());
				}
			}
		}
	}
	
	private StaticQueryContext prepareStaticContext(String body) {
		StaticQueryContext sqc = config.newStaticQueryContext();
		sqc.setErrorListener(new LocalErrorListener());
        //sqc.setCompileWithTracing(true);
		if (body != null) {
			sqc.setModuleURIResolver(new LocalModuleURIResolver(body));
		}
		return sqc;
	}
	
	private XQueryExpression getModuleExpression(XDMModule module) {
		//logger.trace("getModuleExpression.enter; got namespace: {}, name: {}, body: {}", namespace, name, body);
		String query = "import module namespace test=\"" + module.getNamespace() + 
				"\" at \"" + module.getName() + "\";\n\n";
		query += "1213";
		StaticQueryContext sqc = null;
		try {
			//sqc.compileLibrary(query); - works in EE only
		    sqc = prepareStaticContext(module.getBody());
			logger.trace("getModuleExpression; compiling query: {}", query);
			//logger.trace("getModuleExpression.exit; time taken: {}", stamp);
			return sqc.compileQuery(query);
			//sqc.getCompiledLibrary("test")...
		} catch (XPathException ex) {
			List<TransformerException> errors = ((LocalErrorListener) sqc.getErrorListener()).getErrors();
			StringBuffer buff = new StringBuffer();
			for (TransformerException tex: errors) {
				buff.append(tex.getMessageAndLocation()).append("\n");
			}
			String error = buff.toString();
			//logger.error("compileQuery.error", ex);
			logger.info("getModuleExpression.error; message: {}", error);
			throw new RuntimeException(error);
		}
	}

	private List<String> lookupFunctions(FunctionLibraryList fll) {
		List<String> fl = new ArrayList<>();
		for (FunctionLibrary lib: fll.getLibraryList()) {
			logger.trace("lookupFunctions; function library: {}; class: {}", lib.toString(), lib.getClass().getName());
			if (lib instanceof FunctionLibraryList) {
				fl.addAll(lookupFunctions((FunctionLibraryList) lib));
			} else if (lib instanceof ExecutableFunctionLibrary) {
				ExecutableFunctionLibrary efl = (ExecutableFunctionLibrary) lib;
				Iterator<UserFunction> itr = efl.iterateFunctions();
				while (itr.hasNext()) {
					fl.add(getFunctionDeclaration(itr.next()));
				}
			}
		}
		return fl;
	}
	
	private String getFunctionDeclaration(UserFunction function) {
		//declare function hw:helloworld($name as xs:string)
		StringBuffer buff = new StringBuffer("function ");
		buff.append(function.getFunctionName());
		buff.append("(");
		int idx =0;
		for (UserFunctionParameter ufp: function.getParameterDefinitions()) {
			if (idx > 0) {
				buff.append(", ");
			}
			buff.append("$");
			buff.append(ufp.getVariableQName());
			buff.append(" as ");
			buff.append(ufp.getRequiredType().toString());
			idx++;
		}
		buff.append(") as ");
		// TODO: get rid of Q{} notation..
		buff.append(function.getDeclaredResultType().toString());
		return buff.toString();
	}
	
	private class LocalErrorListener implements ErrorListener {

	    private List<TransformerException> errors = new ArrayList<>();
	    
	    public List<TransformerException> getErrors() {
	    	return errors;
	    }
		
		@Override
		public void error(TransformerException txEx) throws TransformerException {
			errors.add(txEx);
		}
	
		@Override
		public void fatalError(TransformerException txEx) throws TransformerException {
			errors.add(txEx);
		}
	
		@Override
		public void warning(TransformerException txEx) throws TransformerException {
			errors.add(txEx);
		}
	}
	
	private class LocalModuleURIResolver implements ModuleURIResolver {
		
		private String body;
		
		LocalModuleURIResolver(String body) {
			this.body = body;
		}

		@Override
		public StreamSource[] resolve(String moduleURI, String baseURI,	String[] locations) throws XPathException {
			logger.trace("resolve.enter; got moduleURI: {}, baseURI: {}, locations: {}", moduleURI, baseURI, locations);
			Reader reader = new StringReader(body);
			return new StreamSource[] {new StreamSource(reader)};
		}
	}

}