db = connect( "kristina" )
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
if(num != 10000) {
    print("num : "+num+", messed up on initial fill!");
    return;
}

for(var i=0; i<100000; i++) {
    var q = {};
    q["x"+(randNum.nextInt()%1000)] = null;
    var x = t.findOne(q);
    if(x) {
        for(var j in x) {
            if(j.startsWith("x")) {
                print("x: "+tojson(x)+" j: "+j);
                x[j].push(i);
            }
        }
        t.save(x);
    }
}

var num = t.find().count();
if(num != 10000) {
    print("num : "+num+", messed up after array mod!");
    return;
}
