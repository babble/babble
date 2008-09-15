package ed.db.mql;

import org.testng.annotations.Test;
import ed.MyAsserts;

public class MQLSelectTest extends MQLTestUtil {

    @Test(groups = {"basic"})
    public void testBasicSelect() throws Exception {

        testQueryOutput("select from x",
                "db.x.find({})");

        testQueryOutput("select from x where a = b",
                "db.x.find({ $where : function() { return obj.a == obj.b;}})");

        testQueryOutput("SELECT FROM x WHERE a = b",
                "db.x.find({ $where : function() { return obj.a == obj.b;}})");

        testQueryOutput("SELECT From x WHERE a = b",
                "db.x.find({ $where : function() { return obj.a == obj.b;}})");

        testQueryOutput("SELECT from x WHERE a = b",
                "db.x.find({ $where : function() { return obj.a == obj.b;}})");

        testQueryOutput("select from x where a = b.c",
                "db.x.find({ $where : function() { return obj.a == obj.b.c;}})");

        testQueryOutput("select from x where a.n = b.c",
                "db.x.find({ $where : function() { return obj.a.n == obj.b.c;}})");

        testQueryOutput("select from x where a = b and c = d",
                "db.x.find({ $where : function() { return obj.a == obj.b && obj.c == obj.d;}})");

        testQueryOutput("select from x where a = b or  c = d",
                "db.x.find({ $where : function() { return obj.a == obj.b || obj.c == obj.d;}})");

        testQueryOutput("select from x where a = 1",
                "db.x.find({ $where : function() { return obj.a == 1;}})");

        testQueryOutput("select from x where a = -1",
                "db.x.find({ $where : function() { return obj.a == -1;}})");
    }

    @Test(groups = {"basic"})
    public void testFieldsSelect() throws Exception {

        testQueryOutput("select a, b, c from x",
                "db.x.find({}, {a:true, b:true, c:true})");

        testQueryOutput("select    a,      b,c from x",
                "db.x.find({}, {a:true, b:true, c:true})");

        testQueryOutput("select m from x where a = b",
                "db.x.find({ $where : function() { return obj.a == obj.b;}}, {m:true})");

        testQueryOutput("select m,n,Y from x where a = b",
                "db.x.find({ $where : function() { return obj.a == obj.b;}}, {m:true, n:true, Y:true})");

    }

    @Test(groups = {"basic"})
    public void testBasicSelectLimitSkip() throws Exception {

        testQueryOutput("select from x",
                "db.x.find({})");

        testQueryOutput("select from x where a = b skip 5 limit 10",
                "db.x.find({ $where : function() { return obj.a == obj.b;}}).limit(10).skip(5)");

        testQueryOutput("select from x where a = b.c skip 5 limit 10",
                "db.x.find({ $where : function() { return obj.a == obj.b.c;}}).limit(10).skip(5)");

        testQueryOutput("select from x where a.n = b.c skip 5 limit 10",
                "db.x.find({ $where : function() { return obj.a.n == obj.b.c;}}).limit(10).skip(5)");

        testQueryOutput("select from x where a = b and c = d skip 5 limit 10",
                "db.x.find({ $where : function() { return obj.a == obj.b && obj.c == obj.d;}}).limit(10).skip(5)");

        testQueryOutput("select from x where a = b or  c = d skip 5 limit 10",
                "db.x.find({ $where : function() { return obj.a == obj.b || obj.c == obj.d;}}).limit(10).skip(5)");

        testQueryOutput("select from x where a = 1 skip 5 limit 10",
                "db.x.find({ $where : function() { return obj.a == 1;}}).limit(10).skip(5)");

        testQueryOutput("select from x where a = -1 skip 5 limit 10",
                "db.x.find({ $where : function() { return obj.a == -1;}}).limit(10).skip(5)");
    }

    @Test(groups = {"basic"})
    public void testBasicSelectLimitSkipOrder() throws Exception {

        testQueryOutput("select from x order by n desc",
                "db.x.find({}).sort({n:-1})");

        testQueryOutput("select from x where a = b order by c asc skip 5 limit 10 ",
                "db.x.find({ $where : function() { return obj.a == obj.b;}}).sort({c:1}).limit(10).skip(5)");
    }
}
