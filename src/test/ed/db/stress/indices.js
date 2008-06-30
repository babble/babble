core.db.db();

db=connect("foo");

var myFoo = core.util.random();
randGen = myFoo.getRandom(1);

print("building the db");
for(var i=0; i<200; i++) {
    var obj = {};
    for(var j=0; j<1500; j++) {
        obj["bleh"+j] = i+j;
    }
    db.indexMania.save(obj);
}

print("creating 1500 indices");
for(var i=0; i<150; i++) {
    var obj = {};
    obj["bleh"+i] = true;
    db.indexMania.ensureIndex(obj);
}

print("querying");
for(var i=0; i<5000; i++) {
    obj = {};
    obj["bleh"+(randGen.nextInt()%10)] = 1;
    var x = db.indexMania.find().sort(obj);
    print(tojson((x.next()).bleh0));
}

