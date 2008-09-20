// testdb.js
//
// taken from the db project - removed local definition of assert() and have failure throw an exception
//

var t = connect("test");
db=t;

var z = 0;
function progress() {}// print(++z); }

function failure(f, args) { 
    var s = "FAIL: " + f + ' ' + (args.length<2?"":args[1]);
    throw s;
}

function oneresult(c) { 
    if( c.length() != 1 ) {
	failure("ERROR: wrong # of results: " + c.length(), arguments);
    }
}

function noresult(c) { 
    if( c.length() != 0 )
	failure("ERROR: wrong # of results: " + c.length(), arguments);
}

function testcopydb() { 
    var tc = connect("testcopy");
    tc.dropDatabase();
    var res = connect("admin").copyDatabase("test", "testcopy");
    // system tables aren't copied (system.profile)
    var query = function() { return !/system/.match(this.name); }
    var x= t.system.namespaces.find(query).count();
    var y= tc.system.namespaces.find(query).count();
    if( x != y ) { 
	print("testcopydb: x:" + x + " y:" + y);
	print("t:" + tojson(t.system.namespaces.find().toArray()));
	print("tc:" + tojson(t.system.namespaces.find().toArray()));
	assert(x==y);
    }
    assert( t.system.indexes.count() == tc.system.indexes.count() );
    assert( t.dots.count() == tc.dots.count() );
}

function testdots() { 
    t.dots.remove({});
    t.dots.save( { a: 3, b: { y: 4, z : 5 } } );
    t.dots.save( { a: 9, b: [ { y:88, z:0 } ] } );
    oneresult( t.dots.find( { a:3 } ) );
    oneresult( t.dots.find( { b: { y:4,z:5} } ) );
    oneresult( t.dots.find( { a:3, b: { y:4,z:5} } ) );
    noresult( t.dots.find( { b: { y:4} } ) );
    oneresult( t.dots.find( { "b.y":4 } ) );
    oneresult( t.dots.find( { "b.z":5 } ) );
    noresult( t.dots.find( { "b.z":55 } ) );
    oneresult( t.dots.find( { "b.y":88 } ) );
}

function testkeys() { 
    t.testkeys.save( { name: 5 } );
    t.testkeys.ensureIndex({name:true});
    t.testkeys.save( { name: 6 } );
    t.testkeys.save( { name: 8 } );
    t.testkeys.save( { name: 3 } );
    //print("t.testkeys");
}

function testdelete() { 
    //print("testdelete");
    t.testkeys.remove({});
    testkeys();
    t.testkeys.remove({});
    testkeys();
    assert( t.testkeys.find().toArray().length == 4, "testkeys" );
}

function index2() { 
    t.z.remove({});
    t.z.save( { a: -3 } );
    t.z.ensureIndex( { a:true} );
    for( var i = 0; i < 300; i++ )
	t.z.save( { a: i, b: "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddfffffffffffffffffffffffffffffff" } );
    t.z.remove({});
}

function giantIndexTest() { 
    //print("giantIndexTest");
    db.giant.drop();
    db.giant.save({moe:1,foo:[33],bar:"aaaaa"});

    var z = 0;
    var prime = 127;
    for( var i = 0; i < 20000; i++ ) { 
	var ar = [];
	for( var j = 0; j < 100; j++ ) { 
	    z += prime;
	    ar.push( z % 100 );
	}
	db.giant.save({foo:ar, bar:"bbbbb"});
//	if( i % 1000 == 0 ) print(i);
	if( i == 10000 )
	    db.giant.ensureIndex({foo:1});
    }


    assert( db.giant.findOne({foo:33}) );
    //print("giantIndexTest end");
}

function giant2() { 
    //print("giant2");
    db.giant.drop();
    db.giant.save({moe:1,foo:[33],bar:"aaaaa",q:-1});

    var z = 0;
    var prime = 127;
    for( var i = 0; i < 20000; i++ ) { 
	var ar = [];
	for( var j = 0; j < 100; j++ ) { 
	    z += prime;
	    ar.push( z % 100 );
	}
	db.giant.save({foo:ar, bar:"bbbbb", q:i});
//	if( i % 1000 == 0 ) print(i);
	if( 0 && i == 10000 )
	    db.giant.ensureIndex({foo:1});
    }


    assert( db.giant.findOne({foo:33}) );
  //  print("giant2  end");
}

// mx=-3; 
// db.giant.find().forEach( function(x) { 
//   if( x.q % 100 == 0 ) print(x.q); if( x.q > mx ) { x.name = "john smith"; db.giant.save(x); mx = x.q; } } );} } );  

function bigIndexTest() { 
    t.big.remove({});
    t.big.save( { name: "Dwight" } );
    t.big.ensureIndex({name: true});
    for( var i = 0; i < 1000; i++ ) { 
	var x = { name: "e" + Math.random() + "abcdefasdflkjfdslkjdslkjfdslkjfdslkjfdslkjdflkj fdslkjfdslkjdsljfdslkjdsl fdslkfdslkjfdslkjlkjdsf fdslkjfds",
                  addr: "1234 main", phone: 7 };
	t.big.save(x);
    }
    for( var i = 0; i < 1000; i++ ) { 
	var x = { name: "c" + Math.random() + "abcdefasdflkjfdslkjdslkjfdslkjfdslkjfdslkjdflkj fdslkjfdslkjdsljfdslkjdsl fdslkfdslkjfdslkjlkjdsf fdslkjfds",
                  addr: "1234 main", phone: 7 };
	t.big.save(x);
    }
}

function runall() { 
    runcursors();

    runquick();

    //print("bigindextest stuff:");
    t.big.remove( { } );
    bigIndexTest();
    t.big.find().sort({name:true});
    t.big.remove( { } );
    t.big.find().sort({name:true});
    bigIndexTest();
    t.big.find().sort({name:true});
    t.big.remove( { } );
}

function testarrayindexing() { 
    //print("testarrayindexing");
    t.ta.remove({});
    t.ta.save({name:"aaa", tags:["abc", "123", "foo"], z:1});
    t.ta.save({name:"baa", tags:["q", "123", 3], z:1});
    t.ta.save({name:"caa", tags:["dm", "123"], z:1});
    t.ta.save({name:"daa"});

    for( var pass=0; pass<=1; pass++ ) { 
	oneresult( t.ta.find({tags:"foo"}) );
	oneresult( t.ta.find({tags:3}) );
	assert( t.ta.find({tags:"123"}).length() == 3 );
	t.ta.ensureIndex({tags:true});
    }
}

function testcapped(max) { 
    //print("testcapped");
    db.capped.drop();

    assert( db.createCollection("capped", { size: 4096, capped:true, max:max } ).ok );

    capped = db.capped;
    for(i=0; i<500; i++ ) { 
	capped.save( { i: i, b: "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxyyyyyyyyyyyyyyyyyyy" } );
    }

    var a = capped.find().toArray();
    assert( a.length < 100 );
    assert( a[a.length-1].i == 499 );
    assert( capped.find().sort({$natural:-1}).limit(1)[0].i == 499 );
    //print("testcapped end");
}

function testgetmore() { 
    //print("testgetmore");
    db.gm.drop();
    gm=t.gm;
    for(i=0;i<20000;i++){
	gm.save({a:i, b:"adsffffffffffffffffffffffffffffffffffffffffffffffff\nfffffffffffffffffffffffffffffffffffffffffffffffffffff\nfffffffffffffffffffffffffffffffff"})
	    }
    assert(gm.find().count()==20000);

    x = 0;
    c=gm.find();
    for(i=0;i<5000;i++) { x += c.next().a; }

    assert(gm.find().length()==20000); // full iteration with a cursor already live
    assert( gm.find()[10000].a == 10000 );
    assert( gm.find()[19000].a == 19000 );
    assert( gm.find()[9000].a == 9000 );
    d=gm.find();
    assert( d[12000].a==12000 );
    assert( d[10000].a==10000 );
    assert( d[17000].a==17000 );
    assert(gm.find().length()==20000); // full iteration with a cursor already live

    //print( connect("intr").cursors.findOne().dump );
     connect("intr").cursors.findOne().dump;

    //print("testgetmore end");
}

function testdups() { 
 //print("testdups");
 K = 2000;
 for( pass=0;pass<1;pass++ ) {
     //print(" pass:" + pass);
     if( pass < 2 ) {
	 //print("removing keys");
	 t.td.remove({});
     }
     //print("add keys");
     for( var x=0;x<K;x++ )
	 t.td.save({ggg:"asdfasdf bbb a a jdssjsjdjds dsdsdsdsds d", z: x, 
		     str: "a long string dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"});
     assert( t.td.find({ggg:"asdfasdf bbb a a jdssjsjdjds dsdsdsdsds d"}).toArray().length == K );
     //     t.td.ensureIndex({ggg:true});
     if( pass == 0 )
	 t.td.ensureIndex({ggg:1});
     //     else if( pass == 1 ) 
     //	 t.td.ensureIndex({ggg:-1});
 }
 //print(" end testdups");
 //print(" try t.td.remove({});");
}

function testdups2() { 
 for( pass=0;pass<1;pass++ ) {
     t.td.remove({});
     for( var x=0;x<250;x++ )
	 t.td.save({ggg:"asdfasdf bbb a a jdssjsjdjds dsdsdsdsds d", z: x, 
		     str: "a long string dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"});
     assert( t.td.find({ggg:"asdfasdf bbb a a jdssjsjdjds dsdsdsdsds d"}).toArray().length == 2000 );
     t.td.ensureIndex({ggg:true});
 }
 t.td.remove({});
}


/*
 *   tests UTF-8 in the regexp package in the db : save a string w/ two unicode characters
 *   and then pass a regex that looks for a match of both chars.   If regex on db is borked
 *   we'll be matching against the two "low order" characters, rather than the two double-byte
 *   characters
 */
function test_utf8() {
    db.utf.save({str:"123abc\u0253\u0253"});
    assert(db.utf.findOne({str:RegExp("\u0253{2,2}")}));
    db.utf.remove({});
}

function runcursors() {
    t.cur.remove({});
    t.cur.findOne();
//    print( tojson( connect("intr").cursors.find() ) );
    tojson( connect("intr").cursors.find() );

    for( i = 0; i < 50000; i++ )
	t.cur.save( { name:"ABC", k:/asfd/, a:i, 
		    lng:"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
		    lng1:"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
		    lng2:"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"} );

    for( i = 0; i < 100; i++ ) {
	c = t.cur.find().limit(2);
	//print(c[0].name);
	t.cur.find({name:"ABC"}).limit(3)[0];
    }

//    print( tojson( connect("intr").cursors.find() ) );
    tojson( connect("intr").cursors.find() );

    t.cur.remove({});
    t.cur.findOne();

//    print( tojson( connect("intr").cursors.find() ) );
    tojson( connect("intr").cursors.find() );
}

function runquick() { 
    start = Date();

    testdots();

    testcapped();

    testgetmore();

    t.nullcheck.remove({});
    t.nullcheck.save( { a : 3 } );
    oneresult( t.nullcheck.find() ); 

    /* todo uncomment when eliot fixes! */
    assert( t.nullcheck.find({a:3})[0].a == 3, "a3" );
    oneresult( t.nullcheck.find( { b: null } ) ); progress();
    noresult( t.nullcheck.find( { b: 1 } ) ); progress();
/*
 *   @DWIGHT THIS FAILS
 *    oneresult( t.nullcheck.find( { a : "3" } ), "todo num to str match" ); progress();
 */
    
    // regex
    t.reg.remove({});
    t.reg.save( { name: "Dwight", a : 345, dt: Date() } );
    for( i = 0; i < 2; i++ ) {
	oneresult( t.reg.find( { name: /Dwi./ } ), "re1" );
	oneresult( t.reg.find( { dt: /20/ } ), "date regexp match" );
	oneresult( t.reg.find( { a: /34/ } ), "regexp match number" );
	noresult( t.reg.find( { name: /dwi./ } ), "re2" );
	oneresult( t.reg.find( { name: /dwi/i } ), "re3" );
	t.reg.ensureIndex( { name: true } );
    }
    
    testdelete();
    
    testarrayindexing();

    runcursors();

    test_utf8();

    testdups();
}

assert( db.eval(function(){return 3;}) == 3 );

runall();

assert( db.eval(function(){return 3;}) == 3 );

testcopydb();

