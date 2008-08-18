
// tests for "deep" changes, like addition and loops
// addition
xml = <a>b</a>+<a>b</a>;
print( xml );

xml = <a>b</a>+<c>d</c>+<a>b</a>;
print( xml );

print( xml + <yo></yo>);
print( typeof xml );
print( typeof (xml + "hi") );

