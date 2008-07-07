db = connect( "ed_db_stress_log" )
t = db.lotsafields
t.clean();
t.drop();

randNum = core.util.random().getRandom(1);

function makeO() {
    var o = {};
    o["x"+(randNum.nextInt()%1000)] = [];
    return o;
}

for(var i=0; i<10000; i++) {
    t.save(makeO());
}

var num = t.find().count();
assert(num == 10000);

for(var i=0; i<100000; i++) {
    var q = {};
    q["x"+(randNum.nextInt()%1000)] = null;
    var x = t.findOne(q);
    if(x) {
        for(var j in x) {
            if(j.startsWith("x")) {
                x[j].push(i);
            }
        }
        t.save(x);
    }
    if(i%100 == 0) {
        assert(t.count() == 10000)
    }
}

var num = t.find().count();
assert(num == 10000);
