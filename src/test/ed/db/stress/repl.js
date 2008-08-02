// repl.js - test replication.  two db instances must be running, with master on default port
//

slocal = connect("local", "localhost:8000"); // slave on 8000
sfoo   = connect("foo", "localhost:8000");
mlocal = connect("local");
mfoo   = connect("foo");

mbar = mfoo.bar;
sbar = sfoo.bar;

// this doesn't really work, so don't use it yet.  in part we would need to recreate the local.oplog.$main collection.
function dropAll(n) {
    if( n == 1 ) 
	return;
    slocal.dropDatabase();
    sfoo.dropDatabase();
    mlocal.dropDatabase();
    mfoo.dropDatabase();

    // maybe replication was running -- which will undo what we did above.  so do again.
    // hacky, need a better way later.
    sleep(3000);
    dropAll(1);
}

function chk(query) { 
    var s = sbar.find(query).count();
    var m = mbar.find(query).count();
    if( s == m )
	return;
    print("chk: " + tojson(query) + " : inconsistent result");
    print("s:" + s + " m:" + m);
    assert(false);
}

function go() { 
    
    if( mlocal.oplog.$main.count() ) {
	print("info:starting with nonempty master local.oplog.$main");
    } 
    if( mfoo.bar.count() ) { 
	print("info:starting with nonempty master foo.bar collection");
    }
    if( slocal.sources.count() || sfoo.bar.count() ) { 
	// maybe - depending on what is there - that is ok, but this test isn't trying to be clever yet.
	throw "not starting with empty slave database."
    }

    var mold = mbar.count();
    mbar.save( { x: 1} );
    mbar.save( { x: 2} );
    assert( mbar.count() == mold + 2 );

    assert( sbar.count() == 0 );

    slocal.sources.save({host:'localhost', source:'main'});

    while( 1 ) { 
	var mc = mbar.count();
	var c = sbar.count();
	if( c == mc ) 
	    break;
	print("c=" + c + ", waiting for it to be " + mc);
	sleep(3000);
    }

    mbar.save( { x: 3} );
    var o = mbar.findOne({x:2});
    o.x = 4; o.str = "modded";
    mbar.save(o);
    mbar.remove({x:1});

    print("sleep some...");
    sleep(10000);
    chk( {} );
    chk( { x:1 } );
    chk( { x:2 } );
    chk( { x:3 } );
    chk( { x:4 } );

    assert( mfoo.bar.validate().valid );
    assert( sfoo.bar.validate().valid );
    print("all is well");
}

print("type go()");
