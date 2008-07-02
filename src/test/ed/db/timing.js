// Write
function writer() {
    pre = new Date();

    for(var i=0; i<100000; i++) {
        db.time.save({num: i, str: "asdf"});
    }
    db.time.find({num: 4});

    post = new Date();

    return post-pre;
}

// Read
function reader() {
    pre = new Date();

    for(var i=0; i<100000; i++) {
        var mynum = randGen.nextInt()%100000;
        db.time.find({num: mynum});
    }

    post = new Date();

    return post-pre;
    print("read time: "+(post-pre));
}

function multiReader() {
    pre = new Date();

    for(var i=0; i<100000; i++) {
        var mynum = randGen.nextInt()%100000;
        db.time.find({$where : function(x) {
            return x%4 == 0;
        }});
    }

    post = new Date();

    return post-pre;
}

// Ensure index
function makeIdx() {
    pre = new Date();

    db.time.ensureIndex({num: 1});
    db.time.find({num: 4});

    post = new Date();
    return post-pre;
}

db = connect("foobar");
var myFoo = core.util.random();
randGen = myFoo.getRandom(1);

// drop everything in this db
ns = db["system.namespaces"].find();
while(ns.hasNext()) {
    n=ns.next();
    if(n.getName)
        db[n.getName()].drop();
}

// write out far enough we'll never get there
for(var i=0; i<1000000; i++) {
    db.wipe.save({i: i, str: "blah blah blah blah"});
}

db.wipe.drop();

var pre;
var post;

var obj = {};
obj.ts = new Date();
obj.write = writer();
obj.preIdxRead = reader();
obj.preIdxWhere = multiReader();
obj.makeIdx = makeIdx();
obj.read = reader();
obj.where = multiReader();

db = connect("timetest");
db.log.save(obj);

db.log.ensureIndex({ts: 1});

var top2 = db.log.find().sort({ts: -1}).limit(2);
if(top2.hasNext) {
    var mostRecent = top2.next();
    if(top2.hasNext()) {
        var second = top2.next();
        for(var i in mostRecent) {
            if(mostRecent[i] * .8 > recent[i]) {
                print("db slowdown: "+tojson(mostRecent)+" "+tojson(recent));
                assert(false);
            }
        }
    }
}
