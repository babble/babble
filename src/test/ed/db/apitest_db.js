/**
 *   Tests for the db object enhancement
 */

db = connect( "test" )
 

/*
 *  be sure the public collection API is complete
 */
assert(db.createCollection);
assert(db.getProfilingLevel);
assert(db.setProfilingLevel);
assert(db.dbEval);
assert(db.group);

/*
 * test createCollection
 */
 
 db.test.drop();
 db.system.namespaces.find().forEach( function(x) { assert(x.name != "test.test"); });
 
db.createCollection("test");
var found = false;
db.system.namespaces.find().forEach( function(x) {  if (x.name == "test.test") found = true; });
assert(found);

/*
 *  profile level
 */ 
 
 db.setProfilingLevel(0);
 assert(db.getProfilingLevel() == 0);
 
 db.setProfilingLevel(1);
 assert(db.getProfilingLevel() == 1);
 
 db.setProfilingLevel(2);
 assert(db.getProfilingLevel() == 2);
 
 db.setProfilingLevel(0);
 assert(db.getProfilingLevel() == 0);
 
 try {
     db.setProfilingLevel(10);
     assert(false);
 }
 catch (e) { 
     assert(e.dbSetProfilingException);
 }
 
 /*
  * dbEval tested via collections count function
  */
 
 /*
  * db group
  */
  
 db.test.drop();
 db.test.save({a:1});
 db.test.save({a:1});
 
 var f = db.group(
       {
         ns: "test",
         key: { a:true},
         cond: { a:1 },
         reduce: function(obj,prev) { prev.csum++; } ,
         initial: { csum: 0}
        }
  );
  
  assert(f[0].a == 1 && f[0].csum == 2);  