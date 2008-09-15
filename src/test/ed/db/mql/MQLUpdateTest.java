package ed.db.mql;

import org.testng.annotations.Test;
import ed.MyAsserts;

public class MQLUpdateTest extends MQLTestUtil {

    @Test(groups = {"basic"})
    public void testBasicUpdate() throws Exception {

        testQueryOutput("update x set a = b",
                "db.eval(  function() {  var cur = db.x.find({}); cur.forEach( function() {arguments[0].a=arguments[0].b; db.x.save(arguments[0]); })})");

        testQueryOutput("UPDATE x set a = b",
                "db.eval(  function() {  var cur = db.x.find({}); cur.forEach( function() {arguments[0].a=arguments[0].b; db.x.save(arguments[0]); })})");

        testQueryOutput("UPDATE x SET a = b",
                "db.eval(  function() {  var cur = db.x.find({}); cur.forEach( function() {arguments[0].a=arguments[0].b; db.x.save(arguments[0]); })})");

        testQueryOutput("UPDATE x SET a=b",
                "db.eval(  function() {  var cur = db.x.find({}); cur.forEach( function() {arguments[0].a=arguments[0].b; db.x.save(arguments[0]); })})");

        testQueryOutput("update x set a = b where c = d",
                "db.eval(  function() {  var cur = db.x.find({ $where : function() { return obj.c == obj.d;}}); cur.forEach( function() {arguments[0].a=arguments[0].b; db.x.save(arguments[0]); })})");

        testQueryOutput("update x set a = b where c = d/f",
                "db.eval(  function() {  var cur = db.x.find({ $where : function() { return obj.c == obj.d / obj.f;}}); cur.forEach( function() {arguments[0].a=arguments[0].b; db.x.save(arguments[0]); })})");
    }
}
