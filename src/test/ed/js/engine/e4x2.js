
function myprint( s ){
    s = s.toString();
    s = s.replace( /[\r\n\s]+/g , "" );
    print( s );
}

var sales = <sales vendor="John">
    <name>John Smith</name>
    <silly><foo>a</foo></silly>
    <item type="peas" price="4" quantity="6"/>
    <item type="carrot" price="3" quantity="10"/>
    <item type="chips" price="5" quantity="3"/>
    <zzz a="5" />
    <zzz a="6" />
    <zzz a="7" />
    </sales>;


myprint( sales.item.(@type == "carrot").@quantity );

//print( sales..@price );
