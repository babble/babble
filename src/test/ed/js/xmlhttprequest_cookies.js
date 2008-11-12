var r1 = new XMLHttpRequest("GET", "moo.com");
r1.cookies["cookie1"] = "value1";

assert.eq( "value1", r1.getCookieJar().getAll().get("cookie1").getValue() );
assert.eq( "moo.com", r1.getCookieJar().getAll().get("cookie1").getDomain() );


//----
var r2 = new XMLHttpRequest("GET", "baa.com");
r2.setCookieJar( r1.getCookieJar() );

assert.eq( "value1", r2.getCookieJar().getAll().get("cookie1").getValue() );


//----
r2.cookies["cookie2"] = "value2";

assert.eq( "value2",  r1.cookies["cookie2"] );
assert.eq( "baa.com", r1.getCookieJar().getAll().get("cookie2").getDomain() );