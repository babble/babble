function go() { 

var d = new Date();

// Try to force real results
var ret;

// TESTS: String concatenation

for ( var num = 500; num <= 4000; num *= 2 ) (function(num){

		var str = "";
		for ( var i = 0; i < num; i++ )
			str += "a";
		ret = str;

		var str = new String();
		for ( var i = 0; i < num; i++ )
			str += "a";
		ret = str;

		var str = "";
		for ( var i = 0; i < num; i++ )
			str += String.fromCharCode(97);
		ret = str;

		var str = [];
		for ( var i = 0; i < num; i++ )
			str.push("a");
		ret = str.join("");

})(num);

var ostr = [], tmp, num = 50;

for ( var i = 0; i < 16384; i++ )
	ostr.push( String.fromCharCode( (25 * Math.random()) + 97 ) );

ostr = ostr.join("");

var str;

for ( var i = 16384; i <= 131072; i *= 2 ) (function(i){
		str = new String(ostr);

	// TESTS: split
/* This test makes Safari sad :-(
		ret = str.split("");
*/

		ret = str.split("a");

	// TESTS: characters

		for ( var j = 0; j < num; j++ ) {
			ret = str.charAt(0);
			ret = str.charAt(str.length - 1);
			ret = str.charAt( 15000 );
			ret = str.charAt( 12000 );
		}

		for ( var j = 0; j < num; j++ ) {
			ret = str[0];
			ret = str[str.length - 1];
			ret = str[ 15000 ];
			ret = str[ 12000 ];
		}

		for ( var j = 0; j < num; j++ ) {
			ret = str.charCodeAt(0);
			ret = str.charCodeAt(str.length - 1);
			ret = str.charCodeAt( 15000 );
			ret = str.charCodeAt( 12000 );
		}

	// TESTS: indexOf

		for ( var j = 0; j < num; j++ ) {
			ret = str.indexOf("a");
			ret = str.indexOf("b");
			ret = str.indexOf("c");
			ret = str.indexOf("d");
		}

		for ( var j = 0; j < num; j++ ) {
			ret = str.lastIndexOf("a");
			ret = str.lastIndexOf("b");
			ret = str.lastIndexOf("c");
			ret = str.lastIndexOf("d");
		}

	// TESTS: length

		for ( var j = 0; j < num; j++ ) {
			ret = str.length;
			ret = str.length;
			ret = str.length;
			ret = str.length;
			ret = str.length;
			ret = str.length;
		}

	// TESTS: slice

		for ( var j = 0; j < num; j++ ) {
			ret = str.slice(0);
			ret = str.slice(0,5);
			ret = str.slice(-1);
			ret = str.slice(-6,-1);
			ret = str.slice( 15000, 15005 );
			ret = str.slice( 12000, -1 );
		}

	// TESTS: substr

		for ( var j = 0; j < num; j++ ) {
			ret = str.substr(0);
			ret = str.substr(0,5);
			ret = str.substr(-1);
			ret = str.substr(-6,1);
			ret = str.substr( 15000, 5 );
			ret = str.substr( 12000, 5 );
		}

	// TESTS: substring

		for ( var j = 0; j < num; j++ ) {
			ret = str.substring(0);
			ret = str.substring(0,5);
			ret = str.substring(-1);
			ret = str.substring(-6,-1);
			ret = str.substring( 15000, 15005 );
			ret = str.substring( 12000, -1 );
		}

	// TESTS: toLower/UpperCase

		for ( var j = 0; j < num; j++ ) {
			ret = str.toLowerCase();
		}

		for ( var j = 0; j < num; j++ ) {
			ret = str.toUpperCase();
		}

	// TESTS: toString

		for ( var j = 0; j < num; j++ ) {
			ret = str.toString();
		}

	// TESTS: valueOf

		for ( var j = 0; j < num; j++ ) {
			ret = str.valueOf();
		}

	// TESTS: comparing

		var tmp = str + "a";
		for ( var j = 0; j < num; j++ ) {
			ret = str == tmp;
			ret = str < tmp;
			ret = str > tmp;
		}

	// Double the length of the string
		ostr += ostr;
})(i);

var e = new Date();

print(e-d);
}

go();
go();
