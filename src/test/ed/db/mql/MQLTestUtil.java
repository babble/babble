package ed.db.mql;

import ed.MyAsserts;

/**
 * User: geir
 * Date: Sep 13, 2008
 * Time: 10:50:26 AM
 */
public class MQLTestUtil {

    protected void testQueryOutput(String query, String out) throws Exception {

        MQL parser = new MQL(query);
        SimpleNode ast = (SimpleNode) parser.parseQuery();

        QueryInfo qi = new QueryInfo();
        ast.generateQuery(qi);
        MyAsserts.assertEquals(qi.toString(), out);
    }

}
