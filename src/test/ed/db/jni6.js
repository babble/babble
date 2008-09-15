db = connect( "test" );
t = db.jni6;
t.drop();

var myNum = 17;
t.save( { a : myNum } );

var bar = t.findOne( { $where : 
		       function(){
			   return this.a == myNum;
		       }
		     } );
assert( bar );

assert.eq( myNum , db.eval( function(){ return myNum; } ) );
