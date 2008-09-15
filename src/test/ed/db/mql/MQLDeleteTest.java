package ed.db.mql;

import org.testng.annotations.Test;

public class MQLDeleteTest extends MQLTestUtil {

    @Test(groups = {"basic"})
    public void testBasicDelete() throws Exception {

        testQueryOutput("delete from x where a = b",
                "db.x.remove({ $where : function() { return obj.a == obj.b;}})");
    }
}
