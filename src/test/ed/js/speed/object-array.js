function go() { 
var d = new Date();

var ret = [], tmp, num = 500;

for ( var i = 16384; i <= 131072; i *= 2 ) (function(i){

	// TESTS: Array Building

		for ( var j = 0; j < num; j++ ) {
			ret = [];
			ret.length = i;
		}

    for ( var j = 0; j < num; j++ ){
	ret = new Array();
    }

		ret = [];
		for ( var j = 0; j < i; j++ )
			ret.push(j);

	i /= 128;

		for ( var j = 0; j < i; j++ )
			tmp = ret.pop();
    
		ret = [];
		for ( var j = 0; j < i; j++ )
			ret.unshift(j);

		for ( var j = 0; j < i; j++ )
			tmp = ret.shift();

		ret = [];
		for ( var j = 0; j < i; j++ )
			ret.splice(0,0,j);

		for ( var j = 0; j < i; j++ )
			tmp = ret.splice(0,1);

})(i);

var e = new Date();

print(e-d);

}

//while ( 1 )
go();
go();

