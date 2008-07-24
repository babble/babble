
db = connect( "_hudsonSmall" );
db.dropDatabase();

collection = db.files1;

total = 0;

function createStringBlob() { 
    var s = "a";
    for (var i=0; i<16; i++) {
        s = s + s; 
    }

    return s;
}

function saveSome(){

    for (var i=0; i<100; i++) { 
        var o = { a : blob };

        if ( i % 4 == 0 )
            o.b = blob;
        if ( i % 10 == 0 )
            o.c = blob;
        
	collection.save(o);
        
        o._id = null;
        
        if ( i % 3 == 0 ){
            var foo = collection.find( {} , { _id : ObjectID() } ).skip( Math.floor( Math.random() * total ) ).limit( 1 );
            if ( foo.hasNext() ){
                collection.remove( foo.next() );
                total--;
            }
        }
        
        if ( total++ % 200 == 0){ 
            print( "total run: " + total ); 
            assert( collection.validate().valid );
        }
    }
    
}

blob = createStringBlob();
print("using blob of " +  blob.length  + " bytes.");

function go(){
    while ( true ){
        saveSome();
    
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
