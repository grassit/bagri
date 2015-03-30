package com.bagri.xdm.api.test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.bagri.common.query.AxisType;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathBuilder;

public class XDMQueryManagementTest extends XDMManagementTest {

	public Collection<String> getPrice(String symbol) {
		String prefix = getModelManagement().getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		Map<String, String> params = new HashMap<String, String>();
		params.put(":name", "/" + prefix + ":Security/" + prefix + ":Name/text()");
		params.put(":price", "/" + prefix + ":Security/" + prefix + ":Price/" + prefix + ":PriceToday/" + prefix + ":Open/text()");
		return getQueryManagement().getXML(ec, "<print>The open price of the security \":name\" is :price dollars</print>", params);
	}
	
	public Collection<String> getOrder(String id) {
		String prefix = getModelManagement().getNamespacePrefix("http://www.fixprotocol.org/FIXML-4-4"); 
		int docType = getModelManagement().getDocumentType("/" + prefix + ":FIXML"); // /" + prefix + ":Order");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "FIXML").
				addPathSegment(AxisType.CHILD, prefix, "Order").
				addPathSegment(AxisType.ATTRIBUTE, null, "ID");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);
		Map<String, String> params = new HashMap<String, String>();
		params.put(":order", "/" + prefix + ":FIXML/" + prefix + ":Order");
		return getQueryManagement().getXML(ec, ":order", params);
	}
	
	public Collection<String> getCustomerProfile(String id) {
		String prefix = getModelManagement().getNamespacePrefix("http://tpox-benchmark.com/custacc"); 
		int docType = getModelManagement().getDocumentType("/" + prefix + ":Customer");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Customer").
				addPathSegment(AxisType.ATTRIBUTE, null, "id");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);

		String template = "<Customer_Profile CUSTOMERID=\":id\">\n" +
				"\t:name" + 
				"\t:dob" + 
				"\t:gender" + 
				"\t:langs" + 
				"\t:addrs" + 
				"\t:email" + 
			"</Customer_Profile>";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(":id", "/" + prefix + ":Customer/@id");
		params.put(":name", "/" + prefix + ":Customer/" + prefix + ":Name");
		params.put(":dob", "/" + prefix + ":Customer/" + prefix + ":DateOfBirth");
		params.put(":gender", "/" + prefix + ":Customer/" + prefix + ":Gender");
		params.put(":langs", "/" + prefix + ":Customer/" + prefix + ":Languages");
		params.put(":addrs", "/" + prefix + ":Customer/" + prefix + ":Addresses");
		params.put(":email", "/" + prefix + ":Customer/" + prefix + ":EmailAddresses");
		return getQueryManagement().getXML(ec, template, params);
	}
	
	public Collection<String> getCustomerAccounts(String id) {
		String prefix = getModelManagement().getNamespacePrefix("http://tpox-benchmark.com/custacc"); 
		int docType = getModelManagement().getDocumentType("/" + prefix + ":Customer");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Customer").
				addPathSegment(AxisType.ATTRIBUTE, null, "id");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);

		String template = "<Customer>:id\n" +
				"\t:name" + 
				"\t<Customer_Securities>\n" +
				"\t\t<Account BALANCE=\":balance\" ACCOUNT_ID=\":accId\">\n" +
				"\t\t\t<Securities>\n" +
				"\t\t\t\t:posName" +
				"\t\t\t</Securities>\n" +
				"\t\t</Account>\n" +
				"\t</Customer_Securities>\n" + 
			"</Customer_Profile>";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(":id", "/" + prefix + ":Customer/@id");
		params.put(":name", "/" + prefix + ":Customer/" + prefix + ":Name");
		params.put(":balance", "/" + prefix + ":Customer/" + prefix + ":Accounts/" + prefix + ":Account/" + prefix + ":Balance/" + prefix + ":OnlineActualBal/text()");
		params.put(":accId", "/" + prefix + ":Customer/" + prefix + ":Accounts/" + prefix + ":Account/@id");
		params.put(":posName", "/" + prefix + ":Customer/" + prefix + ":Accounts/" + prefix + ":Account/" + prefix + ":Holdings/" + prefix + ":Position/" + prefix + ":Name");
		
		return getQueryManagement().getXML(ec, template, params);
	}
	
	public Collection<String> searchSecurity(String sector, float peMin, float peMax, float yieldMin) {

		String prefix = getModelManagement().getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.AND, path);
		ec.addExpression(docType, Comparison.AND, path);
		path.addPathSegment(AxisType.CHILD, prefix, "SecurityInformation").
				addPathSegment(AxisType.CHILD, null, "*").
				addPathSegment(AxisType.CHILD, prefix, "Sector").
				addPathSegment(AxisType.CHILD, null, "text()");
		ec.addExpression(docType, Comparison.EQ, path, "$sec", sector);
		path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "PE");
		ec.addExpression(docType, Comparison.AND, path);
		path.addPathSegment(AxisType.CHILD, null, "text()");
		ec.addExpression(docType, Comparison.GE, path, "$peMin", peMin);
		ec.addExpression(docType, Comparison.LT, path, "$peMax", peMax);
		path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Yield").
				addPathSegment(AxisType.CHILD, null, "text()");
		ec.addExpression(docType, Comparison.GT, path, "$yMin", yieldMin);

        String template = "<Security>\n" +
				"\t:symbol" + 
				"\t:name" + 
				"\t:type" +  
        		//{$sec/SecurityInformation//Sector}
        		//regex = "^/" + prefix + ":Security/" + prefix + ":SecurityInformation/.*/" + prefix + ":Sector$";
        		//Collection<String> sPath = mDictionary.getPathFromRegex(docType, regex);
        		//int idx = 0;
        		//for (String path : sPath) {
        		//	params.put(":sector" + idx, path);
        		//	template += "\t:sector" + idx;
        		//	idx++;
        		//}
				"\t:sector" +  
        		"\t:pe" +
        		"\t:yield" +
		"</Security>";

		Map<String, String> params = new HashMap<String, String>();
		params.put(":symbol", "/" + prefix + ":Security/" + prefix + ":Symbol");
		params.put(":name", "/" + prefix + ":Security/" + prefix + ":Name");
		params.put(":type", "/" + prefix + ":Security/" + prefix + ":SecurityType");
		params.put(":sector", "/" + prefix + ":Security/" + prefix + ":SecurityInformation//" + prefix + ":Sector");
		params.put(":pe", "/" + prefix + ":Security/" + prefix + ":PE");
   		params.put(":yield", "/" + prefix + ":Security/" + prefix + ":Yield");

		return getQueryManagement().getXML(ec, template, params);
	}
	
	@Test
	public void getPriceTest() throws IOException {
		storeSecurityTest();

		Collection<String> sec = getPrice("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = getPrice("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = getPrice("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}

	@Test
	public void getSecurityTest() throws IOException {
		storeSecurityTest();

		Collection<String> sec = getSecurity("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = getSecurity("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = getSecurity("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}

	@Test
	public void searchSecurityTest() throws IOException {
		storeSecurityTest();

		Collection<String> sec = searchSecurity("Technology", 25, 28, 0);
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = searchSecurity("Technology", 25, 28, 1);
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);

		sec = searchSecurity("Technology", 28, 29, 0);
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);
	}

	@Test
	public void getOrderTest() throws IOException {
		storeOrderTest();
		Collection<String> sec = getOrder("103404");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
		sec = getOrder("103935");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}

	@Test
	public void getCustomerProfileTest() throws IOException {
		storeCustomerTest();
		Collection<String> sec = getCustomerProfile("1011");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}

	@Test
	public void getCustomerAccountsTest() throws IOException {
		storeCustomerTest();
		Collection<String> sec = getCustomerAccounts("1011");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}
	
}
