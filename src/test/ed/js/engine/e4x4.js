
xml=<blargh><blah id="52" prop="some prop"><something id="1" snakes="in_planes">grr</something><!-- here is a comment --><else id="2">arg</else></blah><blah>2</blah><blah>3</blah><!-- and here is another comment --></blargh>;

print(xml.blah[0].@*);
print(xml.blah[0].@id);
print(xml.blah.something.@*);

print(xml.blah.*);
print(xml.blah[1]);
print(xml.blah[1].*);

delete xml.blah[0];

print(xml);

delete xml.blah;

print(xml);