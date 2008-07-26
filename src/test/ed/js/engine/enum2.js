
var obj = {
    //constructor : function() { return 0; },
    toString : function() { return "1"; }
    ,valueOf : function() { return 2; }
    ,toLocaleString : function() { return "3"; }
    ,prototype : function() { return "4"; }
    ,isPrototypeOf : function() { return 5; }
    ,propertyIsEnumerable : function() { return 6; }
    ,hasOwnProperty : function() { return 7; }
    ,length: function() { return 8; }
    ,unique : function() { return "9" }
};
 
var result = [];
for(var prop in obj) {
	result.push(obj[ prop ]());
}

print( result.join("") );

