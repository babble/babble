db = connect( "test" );
t = db.jni5;
t.drop();

var myNum = 17;
t.save( { a : myNum } );

var bar = t.findOne( { $where : 
		       function(){
			   return obj.a == myNum;
		       }
		     } );
assert( bar );


var thing = { a : myNum };
var bar = t.findOne( { $where : 
		       function(){
			   return obj.a == thing.a;
		       }
		     } );
assert( bar );


// ---

t.drop();
var foo = { a : 123 };
t.save( foo );

var bar = t.findOne( { $where : 
		       function(){
			   return obj.a == foo.a;
		       }
		     } );
assert( bar );
assert.eq( foo.a , bar.a );
