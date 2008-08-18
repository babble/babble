function go() { 

var d = new Date();


// Try to force real results
var str = [], tmp, ret, re;

for ( var i = 0; i < 16384; i++ )
	str.push( String.fromCharCode( (25 * Math.random()) + 97 ) );

str = str.join("");

for ( var i = 16384; i <= 131072; i *= 2 ) (function(i){

	// TESTS: split
	// Note: These tests are really slow, so we're running them for smaller
	//       test sets only.

	if ( i <= 32768 ) {
			re = //;
			tmp = str;

			ret = tmp.split( re );

			re = /a/;
			tmp = str;

			ret = tmp.split( re );

			re = /.*/;
			tmp = str;

			ret = tmp.split( re );
	}
    
	// TESTS: Compiled RegExps

		re = /aaaaaaaaaa/g;
		tmp = str;
    
		ret = tmp.match( re );
	
		tmp = str;
    
		ret = re.test( tmp );
    
		tmp = str;
    
		ret = tmp.replace( re, "" );
	
		tmp = str;
	
		ret = tmp.replace( re, "asdfasdfasdf" );
	
		re = new RegExp("aaaaaaaaaa", "g");
		tmp = str;
    
		ret = tmp.match( re );
	
		tmp = str;
    
		ret = re.test( tmp );
    
		tmp = str;
    
		ret = tmp.replace( re, "" );
	
		tmp = str;
	
		ret = tmp.replace( re, "asdfasdfasdf" );
	
		tmp = str;
	
		ret = tmp.replace( re, function(all){
			return "asdfasdfasdf";
		});
	
	// TESTS: Variable Length
	
		re = /a.*a/;
		tmp = str;
    
		ret = tmp.match( re );
	
		tmp = str;
    
		ret = re.test( tmp );
    
		tmp = str;
    
		ret = tmp.replace( re, "" );
	
		tmp = str;
	
		ret = tmp.replace( re, "asdfasdfasdf" );
	
		re = new RegExp("aaaaaaaaaa", "g");
		tmp = str;
    
		ret = tmp.match( re );
	
		tmp = str;
    
		ret = re.test( tmp );
    
		tmp = str;
    
		ret = tmp.replace( re, "" );
	
		tmp = str;
	
		ret = tmp.replace( re, "asdfasdfasdf" );
	
		tmp = str;
	
		ret = tmp.replace( re, function(all){
			return "asdfasdfasdf";
		});
	
	// TESTS: Capturing
	
		re = /aa(b)aa/g;
		tmp = str;
	
		ret = tmp.match( re );
	
		tmp = str;
	
		ret = tmp.replace( re, "asdfasdfasdf" );
	
		tmp = str;
	
		ret = tmp.replace( re, "asdf\\1asdfasdf" );
	
		tmp = str;
	
		ret = tmp.replace( re, function(all,capture){
			return "asdf" + capture + "asdfasdf";
		});
	
		tmp = str;
	
		ret = tmp.replace( re, function(all,capture){
			return capture.toUpperCase();
		});
	
	// TESTS: Uncompiled RegExps
	
		tmp = str;
    
		ret = tmp.match( /aaaaaaaaaa/g );
	
		tmp = str;
    
		ret = (/aaaaaaaaaa/g).test( tmp );
    
		tmp = str;
    
		ret = tmp.replace( /aaaaaaaaaa/g, "" );
	
		tmp = str;
	
		ret = tmp.replace( /aaaaaaaaaa/g, "asdfasdfasdf" );
	
		tmp = str;
    
		ret = tmp.match( new RegExp("aaaaaaaaaa", "g") );
	
		tmp = str;
    
		ret = (new RegExp("aaaaaaaaaa", "g")).test( tmp );
    
		tmp = str;
    
		ret = tmp.replace( new RegExp("aaaaaaaaaa", "g"), "" );
	
		tmp = str;
	
		ret = tmp.replace( new RegExp("aaaaaaaaaa", "g"), "asdfasdfasdf" );
	
	// Double the length of the string
		str += str;
})(i);

var e = new Date();

print(e-d);
}

go();
go();
