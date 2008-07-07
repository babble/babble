
var myFoo = core.util.random();
randGen = myFoo.getRandom(1);

db = connect("ed_db_stress_log2");
db.log100.drop();

db.createCollection( "log100" , {size:10000, max:100, capped:true} );

var blobbie = db.log100.validate();

assert(blobbie.valid);

var p = /nobj:(\d+)/;
var r = p.exec(blobbie.result);

assert(r[1]==0);

var abc = ["a", "b", "c", "d", "e", "f", "g"];

for(var i=0; i<20; i++) {
    var msg = {};
    msg.i = i;
    db.log100.save(msg);
}

var c = db.log100.find().count();
assert(c == 20)

var blobbie = db.log100.validate();

assert(blobbie.valid);

var r = p.exec(blobbie.result);

assert(r[1]==20);

for(var i=0; i<80; i++) {
    var msg = {};
    msg.i = i;
    db.log100.save(msg);
}

var c = db.log100.find().count();
assert(c == 100)

var blobbie = db.log100.validate();

assert(blobbie.valid);

var r = p.exec(blobbie.result);

assert(r[1]==100);

for(var i=0; i<80; i++) {
    var msg = {};
    msg.i = i;
    db.log100.save(msg);
}

var c = db.log100.find().count();
assert(c == 100)

assert(blobbie.valid);

var p = /nobj:(\d+)/;
var r = p.exec(blobbie.result);

assert(r[1]==100);
