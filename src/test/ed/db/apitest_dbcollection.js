/**
 *   Tests for the db collection 
 */

db = connect( "test" )


/*
 *  be sure the public collection API is complete
 */
assert(db.getCollectionPrototype().dropIndexes);
assert(db.getCollectionPrototype().dropIndex);
assert(db.getCollectionPrototype().validate);
assert(db.getCollectionPrototype().drop);
assert(db.getCollectionPrototype().clean);
assert(db.getCollectionPrototype().count);

/*
 *  test drop
 */
db.test_db.drop();
assert(db.test_db.find().length() == 0);

db.test_db.save({a:1});
assert(db.test_db.find().length() == 1);

db.test_db.drop();
assert(db.test_db.find().length() == 0);

/*
 * test count
 */
 
assert(db.test_db.count() == 0);
db.test_db.save({a:1});
assert(db.test_db.count() == 1);
for (i = 0; i < 100; i++) {
    db.test_db.save({a:1});
}
assert(db.test_db.count() == 101);
db.test_db.drop();
assert(db.test_db.count() == 0);
 
/*
 *  test clean (not sure... just be sure it doen't blow up, I guess
 */ 
 
 db.test_db.clean();
 
 /*
  * test validate
  */

db.test_db.drop();
assert(db.test_db.count() == 0);

for (i = 0; i < 100; i++) {
    db.test_db.save({a:1});
}
  
var v = db.test_db.validate();
assert (v.ns == "test.test_db");
assert (v.ok == 1);

assert(v.result.toString().match(/nrecords\?:(\d+)/)[1] == 100);

/*
 * test deleteIndex, deleteIndexes
 */
 
db.test_db.drop();
assert(db.test_db.count() == 0);
db.test_db.dropIndexes();
assert(db.test_db.getIndexes().length() == 0);  

db.test_db.save({a:10});
db.test_db.ensureIndex({a:1});
db.test_db.save({a:10});

assert(db.test_db.getIndexes().length() == 1);  

db.test_db.dropIndex({a:1});
assert(db.test_db.getIndexes().length() == 0);  

db.test_db.save({a:10});
db.test_db.ensureIndex({a:1});
db.test_db.save({a:10});

assert(db.test_db.getIndexes().length() == 1);  

db.test_db.dropIndex("a_1");
assert(db.test_db.getIndexes().length() == 0);  

db.test_db.save({a:10, b:11});
db.test_db.ensureIndex({a:1});
db.test_db.ensureIndex({b:1});
db.test_db.save({a:10, b:12});

assert(db.test_db.getIndexes().length() == 2);  

db.test_db.dropIndex({b:1});
assert(db.test_db.getIndexes().length() == 1);  
db.test_db.dropIndex({a:1});
assert(db.test_db.getIndexes().length() == 0);  

db.test_db.save({a:10, b:11});
db.test_db.ensureIndex({a:1});
db.test_db.ensureIndex({b:1});
db.test_db.save({a:10, b:12});

assert(db.test_db.getIndexes().length() == 2);  

db.test_db.dropIndexes();
assert(db.test_db.getIndexes().length() == 0);  

db.test_db.find();
