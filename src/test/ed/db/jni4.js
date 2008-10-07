db = connect( "test" );
t = db.jni4;
t.drop();

real = { a : 1 , 
	 b : "abc" , 
	 c : /abc/i , 
	 d : new Date(111911100111) ,
	 e : null ,
	 f : true
       };

t.save( real );

assert.eq( "/abc/i" , real.c.toString() );

var cursor = t.find( { $where : 
		      function(){
			  fullObject;
			  assert.eq( 7 , obj.keySet().length )
			  assert.eq( 1 , obj.a );
			  assert.eq( "abc" , obj.b );
			  assert.eq( "/abc/i" , obj.c.toString() );
			  assert.eq( 111911100111 , obj.d.getTime() );
			  assert( obj.f );
			  assert( ! obj.e );
			  
			  return true;
		      } 
		     } );
assert.eq( 1 , cursor.toArray().length );
assert.eq( "abc" , cursor[0].b ); 

// ---

t.drop();
t.save( { a : 2 , b : { c : 7 , d : "d is good" } } );
var cursor = t.find( { $where : 
		      function(){
			  fullObject;
			  assert.eq( 3 , obj.keySet().length )
			  assert.eq( 2 , obj.a );
			  assert.eq( 7 , obj.b.c );
			  assert.eq( "d is good" , obj.b.d );
			  return true;
		      } 
		     } );
assert.eq( 1 , cursor.toArray().length );

assert(t.validate().valid);