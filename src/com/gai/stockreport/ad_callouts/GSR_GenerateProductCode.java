package com.gai.stockreport.ad_callouts;
import java.util.List;
import org.hibernate.Query;
import javax.servlet.ServletException;
import org.hibernate.Session;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.ProductCategory;


public class GSR_GenerateProductCode extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    try {

      updateValues(info);

    } catch (Exception e) {

      e.printStackTrace();

    }
  }

  private void updateValues(CalloutInfo info) throws Exception {

    String strcode = info.getStringParameter("inpmProductCategoryId", null);
    System.out.println(strcode);
    ProductCategory ProductCategory = OBDal.getInstance().get(ProductCategory.class, strcode);
    String strname = info.getStringParameter("inpname", null);

    // if (strname == "" || !strname.equals(null) || !strname.isEmpty()) {
    if (strname != null && !strname.isEmpty()) {
      System.out.println(strname);
      String strsearchKey = null;
      if (!ProductCategory.getSearchKey().isEmpty()) {
        strsearchKey = ProductCategory.getSearchKey();
      } else {
       // strsearchKey = strnamecode;
      }

      if (!ProductCategory.getProductList().isEmpty()) {
        final Session session1 = OBDal.getInstance().getSession();
        StringBuilder hqlQuery1 = new StringBuilder();

        hqlQuery1.append("select count(e) from Product e where e.searchKey like '"
            + strsearchKey + "%' ");

        final Query query1 = session1.createQuery(hqlQuery1.toString());
        List<Long> items = query1.list();
        long maxnum = 1;
        if (!items.isEmpty()) {
          maxnum = items.get(0) + 1;
          if (maxnum < 10) {
            info.addResult("inpvalue", strsearchKey + "00" + maxnum);
          } else if (maxnum > 9 && maxnum < 100) {
            info.addResult("inpvalue", strsearchKey + "0" + maxnum);
          } else {
            info.addResult("inpvalue", strsearchKey + maxnum);
          }

        } else {
          info.addResult("inpvalue", strsearchKey + "001");
        }
      } else {
        info.addResult("inpvalue", strsearchKey + "001");
      }

    }
  }
}
