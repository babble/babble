import _10gen

db = _10gen.connect( "test" );
t = db.pydb2;
t.drop();

orig = { "name" : "eliot" , "foo" : [ 1 , 2 ] , "bar" : { "a" : "z" , "b" : "y" } };
t.save( orig );
x = t.findOne();
_10gen.assert.eq( "eliot" , x.name );
_10gen.assert.eq( "eliot" , x["name"] );
_10gen.assert.eq( 2 , len( x.foo ) );
_10gen.assert.eq( 1 , x.foo[0] );
_10gen.assert.eq( 2 , x.foo[1] );
_10gen.assert( x.bar );
_10gen.assert.eq( "z" , x.bar.a );
_10gen.assert.eq( "y" , x.bar.b );

foundName = False;
for key in x.iterkeys():
    if key == "name":
        foundName = True
_10gen.assert( foundName , "didn't find name in iterkeys" )

foundName = False;
for key,value in x.iteritems():
    if key == "name":
        foundName = True
_10gen.assert( foundName , "didn't find name in iteritems" )

# -----

class Thingy(object):
    def __init__(self, a=None, b=5):
        self.a = a
        self.b = b

    def m1(self):
        return self.a*2+self.b

o = Thingy(2, 1)
_10gen.assert.eq( o.m1(), 5 )

t.drop()
t.setConstructor(Thingy)
t.save(o)
x = t.findOne()
_10gen.assert.eq( x.m1(), o.m1() )


# ----

class DBConsTest(object):
    _dbCons = {'c': Thingy}
    def __init__(self):
        self.c = []

dbtest_obj = DBConsTest()
dbtest_obj.c.append(Thingy(2, 1))

t.drop()
t.setConstructor(DBConsTest)
t.save(dbtest_obj)

x = t.findOne()

_10gen.assert.eq(x.c[0].m1(), o.m1())

t2 = db.pydb2_1
t2.drop()
t2.setConstructor(Thingy)

o = Thingy(2, 1)
o2 = Thingy(8, 12)
t2.save(o)
t2.save(o2)

dbtest_obj.c = [o]
dbtest_obj.junk = o2

t.save(dbtest_obj)

x = t.findOne()

_10gen.assert.eq(x.junk.m1(), o2.m1())
_10gen.assert.eq(x.c[0].m1(), o.m1())

# ----

class Stringable(object):
    def __init__(self, x=2):
        self.x = x

    def __str__(self):
        return "Stringable(%d)"%(self.x,)

s = Stringable(4)
t.drop()
t.setConstructor(Stringable)
t.save(s)

class C(object):
    pass

t2.drop()
t2.setConstructor(C)

c = C()
c.foo = s
t2.save(c)

x = t2.findOne()
_10gen.assert.eq(str(c.foo), str(s))
