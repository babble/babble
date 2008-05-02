x = {a: 0, b: 5};

delete x.a;

print(x.a == null);

print(x.b);

print(x.c == null);

delete x.c;

print(x.c == null);

