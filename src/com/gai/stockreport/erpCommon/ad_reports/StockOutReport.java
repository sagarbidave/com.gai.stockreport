/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2015 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package com.gai.stockreport.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import org.openbravo.dal.service.OBDal;

public class StockOutReport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private final String THIS_CLASS = "ReportWarehouseKartuStock";
  
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      //OBContext.getOBContext().getReadableOrganizations();
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportWarehouseKartuStock|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportWarehouseKartuStock|dateTo",
          "");
      /*String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportWarehouseKartuStock|partner", "", IsIDFilter.instance);
      
      String strDiscount = vars.getGlobalVariable("inpDiscount",
          "ReportWarehouseKartuStock|discount", "N");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportWarehouseKartuStock|currency", strUserCurrencyId);
      */
      String strOrgId = vars.getGlobalVariable("inpOrg", (THIS_CLASS + "|org"), "");
      printPageDataSheet(response, vars, strOrgId, strDateFrom,
          strDateTo/*, strcBpartnerId,
                   strCurrencyId, strDiscount*/);

    } else if (vars.commandIn("PDF") || vars.commandIn("XLS") ) {
      String org = vars.getStringParameter("inpOrg");
      String warehouse = vars.getStringParameter("inpmWarehouseId");
      String locator = vars.getStringParameter("inpmLocatorId");
      String product1 = vars.getStringParameter("inpmProduct1Id");
      String product2 = vars.getStringParameter("inpmProduct2Id");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportWarehouseKartuStock|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportWarehouseKartuStock|dateTo",
          "");
      String strOutput = "";

      if (vars.commandIn("PDF")) {
        strOutput = "pdf";
      } else {
        strOutput = "xls";
      }

      printPageDataHtml(request, response, vars, org, warehouse, locator, product1, product2,
          strDateFrom, strDateTo, strOutput/*, strcBpartnerId,
                                strDiscount, strCurrencyId*/);
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String org, String warehouse, String locator, String product1,
      String product2, String strDateFrom,
      String strDateTo, String strOutput/*, String strcBpartnerId,
                      String strDiscount, String strCurrencyId */)
          throws IOException, ServletException {
    //    debug
    //    String temp = "warehouse : " + warehouse + " locator : " + locator + " product1: " + product1
    //        + " product2: " + product2 + "strDateFrom : " + strDateFrom + " strDateTo : " + strDateTo;
    //    throw new OBException(temp);

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    String strReportName = "@basedesign@/com/gai/warehouse/erpCommon/ad_reports/warehousekartustock/Report_KartuStok.jrxml";
    if (strOutput.equals("pdf"))
    response.setHeader("Content-disposition", "inline; filename=Report_KartuStok.pdf");

    
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("AD_Org_ID", org);
    parameters.put("M_Warehouse_ID", warehouse);
    parameters.put("M_Locator_ID", locator);
    parameters.put("ProductID1", product1);
    parameters.put("ProductID2", product2);
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    Date dateFrom = null;
    Date dateTo = null;
    try {
      dateFrom = sdf.parse(strDateFrom);
      dateTo = sdf.parse(strDateTo);
    } catch (Throwable t) {
      dateFrom = null;
      dateTo = null;
    }
    parameters.put("first_date", dateFrom);
    parameters.put("end_date", dateTo);
    //parameters.put("first_date", strDateFrom);
    //parameters.put("end_date", strDateTo);
    FieldProvider[] test = null;
    renderJR(vars, response, strReportName, "Report_KartuStok", strOutput, parameters, test /*data*/, null);

  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strOrgId, String strDateFrom,
      String strDateTo/*, String strcBpartnerId,
                      String strCurrencyId, String strDiscount*/)
          throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    XmlDocument xmlDocument = null;

    xmlDocument = xmlEngine
        .readXmlTemplate(
            "com/gai/warehouse/erpCommon/ad_reports/warehousekartustock/ReportWarehouseKartuStock")
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWarehouseKartuStock", false, "",
        "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "com.gai.warehouse.erpCommon.ad_reports.warehousekartustock.ReportWarehouseKartuStock");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportWarehouseKartuStock.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportWarehouseKartuStock.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportWarehouseKartuStock");
      vars.removeMessage("ReportWarehouseKartuStock");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("paramAD_ORG_Id", strOrgId);
    try {
      ComboTableData comboTableDataOrg = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", //AD_Org is transactions allowed
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportWarehouseKartuStock"), //#User_Org
          Utility.getContext(this, vars, "#User_Client", "ReportWarehouseKartuStock"), '0');
      comboTableDataOrg.fillParameters(null, "ReportWarehouseKartuStock", strOrgId);
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableDataOrg.select(false));
      comboTableDataOrg = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportInvoiceDiscount. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
