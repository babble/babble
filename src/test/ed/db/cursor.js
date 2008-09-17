
function go(passes) {

if( !passes ) 
    passes = 1;
 
db = connect( "test" );
t = db.cursor;

r = core.util.random().getRandom(38387);

t.clean();
t.drop();

var total = 23000;

for ( i=0; i<total; i++ ){
    t.save( 
        { 
            name : "asdhasjlkdhakshdfl" + r.nextInt() + " skadghflksdhfkljasdhfs",
            when : new Date() ,
		big : "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz",
            blah : "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" ,
            i : i,
            sub : []
        }
    );
}

t.ensureIndex( { name : 1 } );
t.ensureIndex( { when : 1 } );
t.ensureIndex( { i : 1 } );

t.findOne();

//print( "READY" );

var c = [];

for( pass = 0; pass < passes; pass++ ){ 
    print("cursor.js: pass " + pass );

    for( var i = 0; i < 30; i++ ) { 
	if( i % 4 == 0 )
	    c[i] = t.find();
	else if( i % 4 == 1 ) 
	    c[i] = t.find().sort({when:-1});
	else if( i % 4 == 2 )
	    c[i] = t.find({ i : {$gt : r.nextInt(21000)} });
	else if( i % 4 == 3 ) 
	    c[i] = t.find().sort({name:1});
    }
    
    for( var j = 0; j < 40000; j++ ) { 
	if( j % 1000 == 0 ) {
	    if( j % 10000 == 0 ) 
		print("cursor.js: j:" + j);
	    var k = t.find({ i : {$gt : r.nextInt(17000)} });
	    if( k.hasNext() ) k.next();
	}
	for( k = 0; k < 10; k++ ) {
	    var cc = c[r.nextInt(10)];
	    if( cc.hasNext() ) cc.next();
	}
	t.remove( { i : r.nextInt(15000) } );
    }
    
} // end pass

print("cursor.js: end passes");
    
assert( t.validate().valid );    

}

print( "run go(#passes)" );

go();
