// sysexec2.js
// test of out handler.

var myout = "";
var myerr = "";

r = sysexec( "ls /tmp/" , null , null , null , 
	     { out : function( s ){ myout += s + "\n"; } , 
	       err : function( s ){ myerr += s + "\n"; } 
	     } 
	   );

assert.eq( r.out , myout );
assert.eq( r.err , myerr );

