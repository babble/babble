
var total = 0;

function createStringBlob() { 
    var s = "a";
    for (var i=0; i<18; i++) {
        s = s + s; 
    }

    return s;
}

function saveSome(coll, start, n, o) {
    
    for (var i=start; i < start + n; i++) { 
	o.n = i;
        var myCollection = db[ coll + ( i % 10 ) ];
	myCollection.save(o);
        o._id = null;
        if ( total++ % 100 == 0){ 
            assert( myCollection.validate().valid );
            print( "total run: " + total ); 
        };
    }
    
    return i;
}

db = connect("testblob");
collection = "blobtest1";

blob = createStringBlob();
print("using blob of " +  2 * blob.length  + " bytes.");

var count = db[collection].count();

print("Current count = " + count);

function go(){

    for ( var i=0; i<10000; i++ ){
        var next = saveSome(collection, count, 10000, { n: 0, blob : blob } );

        if ( total > 100000 )
            break;

        db[collection].ensureIndex( {n:1} );

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
