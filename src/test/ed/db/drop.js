db=connect("test");
t=db._droptest_;

t.save({x:1});
assert( t.validate().valid);

print("starting drop.js");

t.drop();

function manip() { 
    for( var p = 0; p < 100; p++ ) { 
//	print("p:" + p);
	for( var i = 0; i < 70; i++ ) { 
	    t.save( {  a : 3, b : 4, z : bigstr, mod : i % 2 } );
	    if( i % 25 == 0 ) { 
		//		print("i:" + i);
		var c = t.find({a:3});
		while( c.hasNext() ) c.next(); 
	    }
	}
	if( p % 3 == 0 ) 
	    t.ensureIndex( { a : 1 } );
	if( p % 7 == 0 ) {
    //	    print("DROP len was " + t.find().length());
	    t.drop();
	}
	else if( p % 11 == 0 )
	    t.dropIndexes();
	else if( p % 37  == 0 )
	    t.remove({mod : 1});
    }
}

for( var i = 0; i < 6; i++ )
    fork( manip ).start();

manip();

print("finish drop.js"); // other threads may still be going...

var z = t.validate();
if( !z || !z.valid ) { 
    if( z.errmsg != "ns not found" ) { // may have just dropped...
	print("drop.js: did not validate");
	print("validate result: " + tojson(z));
	assert(false);
    }
}
