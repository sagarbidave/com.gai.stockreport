package com.gai.stockreport.ad_actionhandler;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpWindows.ProcessPlan.IOProducts;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.manufacturing.processplan.Operation;
import org.openbravo.model.manufacturing.processplan.OperationProduct;
import org.openbravo.model.manufacturing.processplan.ProcessPlan;
import org.openbravo.model.manufacturing.processplan.Version;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;
import org.openbravo.model.materialmgmt.transaction.InternalMovementLine;

public class GSR_ImportBOM extends BaseProcessActionHandler {
	private static Logger log = Logger.getLogger(GSR_ImportBOM.class);

	@Override
	protected JSONObject doExecute(Map<String, Object> parameters, String content) {
		JSONObject jsonRequest = null;
		try {
			JSONObject request = new JSONObject(content);
			JSONObject params = request.getJSONObject("_params");
			String movementId = request.getString("M_Movement_ID");
			if (!movementId.equals("")) {
				InternalMovement movobj = OBDal.getInstance().get(InternalMovement.class, movementId);
			//	System.out.print(movobj.getGsrProduct().getName());

				OBQuery<ProcessPlan> obQuery = OBDal.getInstance().createQuery(ProcessPlan.class,
						"gsrProduct.id='" + movobj.getGsrProduct().getId() + "'");
				List<ProcessPlan> listProcessPlan = obQuery.list();
			//	System.out.print(obQuery.list());
				if (listProcessPlan.size() == 0) {
					request.put("responseActions",
							getMessage("No List Found For Product- " + movobj.getGsrProduct().getName(), "error",
									"Goods Movement"));
				} else if (listProcessPlan.size() == 1) {
					for (ProcessPlan ppobj : listProcessPlan) {
						OBQuery<Version> versionQuery = OBDal.getInstance().createQuery(Version.class,
								"creationDate = (select max(e.creationDate) as creationDate from ManufacturingVersion e where e.processPlan.id='"+ppobj.getId()+"')");
						List<Version> listVersion = versionQuery.list();
						if(listVersion.size() >0)
						{
							for(Version verObj:listVersion)
							{
								if(verObj.getManufacturingOperationList().size()>0)
								{
									long lineno = 10L;
									for(Operation operationObj:verObj.getManufacturingOperationList())
									{
										
										if(operationObj.getManufacturingOperationProductList().size()>0)
										{
											
											for(OperationProduct ioprodObj:operationObj.getManufacturingOperationProductList())
											{
												System.out.println("ip - "+ioprodObj.getProductionType());
												if(ioprodObj.getProductionType().equals("-"))
												{
													createmovementlineproduct(ioprodObj, movobj, lineno);
													lineno=lineno+10L;
												}
													//Run Hua 
											}
											request.put("responseActions",
													getMessage("Process Completed Successfully.", "success", "Goods Movement"));
											
										}
									}
								}
								else
								{
									request.put("responseActions",
											getMessage("No Operation List Found For Version- " + verObj.getDocumentNo(), "error",
													"Goods Movement"));
								}
							}
						}
						else {
							request.put("responseActions",
									getMessage("No Version List Found For Product- " + movobj.getGsrProduct().getName(), "error",
											"Goods Movement"));
						}
					}

					request.put("responseActions",
							getMessage("Process Completed Successfully", "success", "Goods Movement"));

				} else {
					request.put("responseActions",
							getMessage("Multiple Product Defined in Header..", "error", "Goods Movement"));
				}
			} else {
				request.put("responseActions", getMessage("Process Failed", "error", "Goods Movement"));
			}
			return request;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonRequest;

	}

	private void createmovementlineproduct(OperationProduct ioprodObj, InternalMovement movobj, long lineno) {
		OBContext.setAdminMode(true);
		try {
			InternalMovementLine movementLineObj = OBProvider.getInstance().get(InternalMovementLine.class);
			movementLineObj.setClient(OBContext.getOBContext().getCurrentClient());
			movementLineObj.setOrganization(movobj.getOrganization());
			movementLineObj.setCreatedBy(OBContext.getOBContext().getUser());
			movementLineObj.setUpdatedBy(OBContext.getOBContext().getUser());
			movementLineObj.setMovement(movobj);
			movementLineObj.setLineNo(lineno);
			movementLineObj.setProduct(ioprodObj.getProduct());
			movementLineObj.setMovementQuantity(ioprodObj.getQuantity());
			movementLineObj.setUOM(ioprodObj.getUOM());
			movementLineObj.setStorageBin(getstoragebin("91AAF0A8AE80464B96AD6D2C41F61BFE"));
			movementLineObj.setNewStorageBin(getstoragebin("463D346C4A474622B00C694D1FB09951"));
			movementLineObj.setGsrImportedfrombom(true);
			movobj.setGsrImportbom(true);
			OBDal.getInstance().save(movementLineObj);
			OBDal.getInstance().flush();
		}
		catch(Exception e) {
			e.printStackTrace();
		}finally {
		      OBContext.restorePreviousMode();
	    }
	}

	private Locator getstoragebin(String id) {
		Locator locObj = OBDal.getInstance().get(Locator.class,id);
		return locObj;
	}

	private JSONArray getMessage(String message, String type, String title) {
		JSONArray actions = new JSONArray();
		try {
			JSONObject msg = new JSONObject();
			msg.put("msgType", type);
			msg.put("msgTitle", title);
			msg.put("msgText", message);
			JSONObject msgTotalAction = new JSONObject();
			msgTotalAction.put("showMsgInProcessView", msg);
			actions.put(msgTotalAction);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return actions;
	}
}