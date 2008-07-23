/* stress test for database

   Note: see ../hmini.js -- which uses this file -- before you change this and break that!
 */

// todo: 
//  x indexes
//   threads
//  x$where
//  xeval()?

var my_tid;

var r = null;

db=connect("hunga");

t=db.munga;

var aLongString = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n";
var aLongerString = "longer " + aLongString + aLongString + aLongString + aLongString + aLongString + aLongString;
var longestString = "longest " + aLongerString + aLongerString + aLongerString + aLongerString + aLongerString ;

function gaussian() { 
    return Math.abs(r.nextGaussian());
}

function run(x) { 
    if( isFunction(x) ) return x();
    return x;
}

function rarely(f) { 
    if( r.nextInt(100) == 0 ) 
	return run(f);
    return null;
}

function vrarely(f) { 
    if( r.nextInt(10000) == 0 ) 
	return run(f);
    return null;
}

function _rand(arr) { 
    var n = arr.length;
    var i = r.nextInt(n);
    var x = arr[i];
    return run(x);
}

function rand(ops) {
    if( isArray(ops) ) 
	return _rand(ops);
    return _rand(arguments);
}

// random thing
function thing() { 
    return rand(12.1,
		"asdf",
		[3,3,5],
		{ a : { b : 3 } },
		r.nextInt(100),
		aLongString, 
		Date(), 
		null,
		ObjId(),
		/a.*b/i,
		true, false,
		{});
}

// random array
function arr() { 
    var a = [];
    for( var i = 0; i < r.nextInt(20); i++ )
	a.push( thing() );
    return a;
}

// make a random object
function obj() { 
    var o = { x : r.nextDouble(), y : rand("str" + r.nextInt(100000), thing()) };
    rand(
	 function(){ o.z = aLongString; },
	 function(){ o.z = aLongerString; },
	 function() { 
	     rarely( function() { o.z = obj(); } );
	 },
	 function(){ 
	     o.z = arr(); 
	 },
	 function(){ o.z = { k : 3, j : thing() }; },
	 null);
    return o;
}

// pick a random collection
function coll() { 
    return t[rand("a", "b", "c")];
}

function field() { return rand('x', 'y', 'z'); }

// get a random query
function query1() {
    var o = {};
    var f = field();
    vrarely( function(){ o.$where = function(){return obj.a == 3;}; } );
    if( r.nextInt(1000) == 0 ) 
	o[f] = query1();
    else
	o[f] = thing();
    return o;
}

// do an insert operation
function insert() { 
    var c = coll();
    var o = obj();
    //    print("" + c + ".save " + tojson(o));
    c.save(o);
    //    coll().save( obj() ); 
}

function insertMany() { 
    var c = coll(); var o = obj();
    for( var i = 0; i < 10; i++ ) {
	insert();
	c.save(o);
    }
}

function update() { 
    if( r.nextInt(2000000) == 0 ) 
	t.a.drop();
       
    var c = coll();
    var z = c.find().limit(1).skip( r.nextInt(100) );
    if( z.hasNext() ) { 
	z = z.next();
	vrarely( function(){ z.c = db.eval(function(){return 88;}); } );
	rarely( function(){ z["v_" + r.nextInt(5)] = thing(); } );
	rarely( rand(
		     function(){ z.b = longerString | z.b; }, 
		     function(){ z.c = longestString; },
		     function(){ delete z.c; },
		     function(){ z.a = z.c; }) );
	z[ field() ] = thing();
	c.save(z);
	rarely( function(){ coll().save(z); } ); // save somewhere else
    }
}

function query2() { 
    return rand( 
	 function() { 
	     var z = coll().find().limit(1).skip(r.nextInt(50));
	     if( !z.hasNext() )
		 return {};
	     var o = z.next();
	     return rand( {_id: o._id}, o );
	 },
	 { y : "str" + r.nextInt(90000) },
	 { z : aLongString },
	 query1,
	 { x : r.nextInt() } 
	  );
}

function remove() { 
    coll().remove( query2() );
}

function runquery() { 
    var criteria = rand(query2, {});
    //    print("  runquery: " + tojson(criteria));
    var q = coll().find( criteria );

    if( r.nextInt(50) == 0 ) { 
	q.count(); 
	return;
    }

    rand(
	 function() { q.limit( r.nextInt(20000) ); }, 
	 function() { q.skip( r.nextInt(2000) ); }, 
	 function() { q.sort( query2() ); },
	 0,0,0,0,0,0,0);
    var depth = rand( gaussian() * 5000, 1, 3, 10000000 );
    for( var i = 0; i < depth; i++ ) { 
	if( !q.hasNext() )
	    break;
	q.next();
    }
    //    print("  end");
}

function makeindex() { 
    print("makeindex");
    coll().ensureIndex( rand(query2, query1, {_id:ObjId()}) );
}

var ops = [ 
	   insertMany, insert, insert, insert, update, update,  update, remove, runquery, runquery
	    ];

var ntodo = 10000;

function beginPass() { 
    t.a.dropIndexes();
    t.b.dropIndexes();
    t.c.dropIndexes();
    for( var n = 0; n < ntodo; n++ ) { 
	if( r.nextInt(ntodo) < 5 )
	    makeindex();
	if( n % 5000 == 0 )
	    print(n);
	rand( ops );
    }
    assert( t.a.validate().valid );
    assert( t.b.validate().valid );
    assert( t.c.validate().valid );
    print( "coll lens: " + t.a.find().length() + ' ' + t.b.find().length() + ' ' + t.c.find().length() );
    ntodo *= 2;
}

npasses = 10;

function worker(threadid) { 
    for( var pass = 0; pass < npasses; pass++ ) {
	print("\nt" + threadid + " pass: " + pass);
	beginPass();
    }
}

function go(nthreads,seed) {
    nthreads = nthreads || 1;
    seed = seed || 42;
    r = core.util.random().getRandom(seed);
    db.dropDatabase();
    print("seed: " + seed + " nthreads: " + nthreads);

    var threads = [];
    for( var i = 0; i < nthreads; i++ ) {
	var w = fork(worker,i); w.start();
	threads.push( w );
    }
    for( var i = 0; i < nthreads; i++ )
	threads[i].join();
    print("All done");
}

print("hunga.js - usage:");
print('  go([nthreads],[seed])');
