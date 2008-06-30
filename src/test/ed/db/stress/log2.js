core.db.db();
var myFoo = core.util.random();
randGen = myFoo.getRandom(1);

try {
    createCollection( "log100" , {size:1000, capped:true} );
}
catch ( e ){
    print( "error creating log100 db - db logging off" );
    return;
}

var abc = ["a", "b", "c", "d", "e", "f", "g"];


for(var i=0; i<20; i++) {
    var msg = {};
    msg.i = i;

    db.log100.save(msg);
}

var c = db.log100.find().count();
var cinit = c;
var i=0;
while(c == cinit) {
    var msg = {};
    msg.i = i;

    db.log100.save(msg);
    c = db.log100.find().count();
    if(c != cinit) {
        print("count: "+c+" i: "+i);
        return;
    }
    i++;
}
