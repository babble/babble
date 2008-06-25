
var total = 0;

function createStringBlob() { 
    var s = "a";
    for (var i=0; i<16; i++) {
        s = s + s; 
    }

    return s;
}

function saveSome(coll, start, n ) {

    for (var i=start; i < start + n; i++) { 
        var o = { a : blob };

        if ( i % 4 == 0 )
            o.b = blob;
        if ( i % 10 == 0 )
            o.c = blob;
        
	db[ collection ].save(o);
        
        o._id = null;
        
        if ( i % 3 == 0 ){
            var foo = db[collection].find( {} , { _id : ObjectID() } ).skip( Math.floor( Math.random() * total ) ).limit( 1 );
            if ( foo.hasNext() ){
                db[collection].remove( foo.next() );
                total--;
            }
        }
        
        if ( total++ % 100 == 0){ 
            print( "total run: " + total ); 
        }
    }
    
}

db = connect("testblob");
collection = "blobtest1";

blob = createStringBlob();
print("using blob of " +  blob.length  + " bytes.");

db[collection].drop();
var count = db[collection].count();

print("Current count = " + count);

function go(){
    for ( var i=0; i<10000; i++ ){
        
        saveSome( collection, count, 10000 );
        
        if ( total > 500000 )
            break;
    }
}

var ts = [];
for ( var i=0; i<5; i++ ){
    t = fork( go );
    t.start();
    ts.push( t );
}

for ( var i=0; i<ts.length; i++ )
    ts[i].join();
