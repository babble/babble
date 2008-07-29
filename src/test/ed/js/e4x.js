
xml = <nutrition>
    <dv id="1" lunch="false"><name>Breakfast</name><weight>24.0</weight></dv>
    <dv id="2" lunch="true"><name>Second breakfast</name><weight>16.3</weight></dv>
    <dv id="3" lunch="true"><name>Lunch</name><weight>2000</weight></dv>
    <dv id="4" lunch="false"><name>Dinner</name><weight>13.2</weight></dv>
    <dv id="5" lunch="false"><name>Nightcap</name><weight>2</weight></dv>
    </nutrition>;

assert(xml.dv[0].toString() == "<dv id=\"1\"  lunch=\"false\" >\n <name>\n  Breakfast\n </name>\n <weight>\n  24.0\n </weight>\n</dv>\n");

assert(xml.dv[0].@lunch == "false");
assert(xml.dv[2].@lunch == "true");
for(var i=0; i<5; i++) {
    assert(xml.dv[i].@id == ((i+1)+""));
}

assert(xml.dv[1].name.toString() == "Second breakfast\n");
assert(xml.dv[4].weight.toString() == "2\n");

assert(xml.dv.toString() == "<dv id=\"1\"  lunch=\"false\" >\n <name>\n  Breakfast\n </name>\n <weight>\n  24.0\n </weight>\n</dv>\n<dv id=\"2\"  lunch=\"true\" >\n <name>\n  Second breakfast\n </name>\n <weight>\n  16.3\n </weight>\n</dv>\n<dv id=\"3\"  lunch=\"true\" >\n <name>\n  Lunch\n </name>\n <weight>\n  2000\n </weight>\n</dv>\n<dv id=\"4\"  lunch=\"false\" >\n <name>\n  Dinner\n </name>\n <weight>\n  13.2\n </weight>\n</dv>\n<dv id=\"5\"  lunch=\"false\" >\n <name>\n  Nightcap\n </name>\n <weight>\n  2\n </weight>\n</dv>\n");


xml2 = <supplies>
  <office>
    <item req="true">Laptop</item>
    <item req="false">Paper</item>
    <item req="true"><name>Foosball</name><cost>$400</cost><enjoyment>++</enjoyment></item>
  </office>
  <desert>
    <item req="true"><name>Sunscreen</name><spf uva="true" uvb="true">85</spf></item>
    <item req="false">Tiger</item>
    <item req="true">Laptop</item>
  </desert>
</supplies>

assert(xml2.office.item[1].@req == "false");
assert(xml2.office.item[2].enjoyment.toString() == "++\n");
assert(xml2.office.item[2].cost.toString() == "$400\n");
assert(xml2.desert.item[0].spf.@uva == xml2.desert.item[0].spf.@uvb);


