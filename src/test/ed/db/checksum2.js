db = connect("checksum");

db.foo.drop();
db.bar.drop();

// do a simple copy and check to see that checksums match

db.foo.save({a : 1});
db.foo.save({n : 2});

db.foo.find().forEach(function(x) { db.bar.save(x); });

sum1 = db.bar.checksum();
sum2 = db.foo.checksum();

assert(sum1 == sum2);

// now add an index - check to see that the checksum changes

db.foo.ensureIndex({a:1});

sum3 = db.foo.checksum();

assert( sum3 != sum2);