
s = "foo\nbar";

re = /^bar/;
print(re.test(s))  

re = /^bar/m;
print(re.test(s))  

re = /foo.bar/;
print(re.test(s))  

re = /foo\nbar/;
print(re.test(s))  

re = /.*foo\nbar.*/;
print(re.test(s))  

re = /.*foo.bar.*/;
print(re.test(s))  

re = /.*foo\s+bar.*/;
print(re.test(s))  

re = /(["'])[^\1]+\1/;
print(re.test( "\"moo\"" ));

re = /(["'])[^\1]+\1/;
print(re.test( "\"\"\"\"\"" ));

re = /(["'])[^\1]+\1/;
print(re.test( "\"\1\1\1\"" ));
