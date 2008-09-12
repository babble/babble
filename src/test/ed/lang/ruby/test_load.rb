require 'ruby_test'

class LoadTest < RubyTest

  def test_load
    assert_nil($foo)
    run_js "foo = {}; foo.bar = 'bletch';"
    assert_not_nil($foo)
    assert_equal($foo.bar, 'bletch')
  end

  def test_back_into_js
    run_js "foo = {}; foo.bar = 'bletch';"
    out = run_js <<EOS
print("foo = " + foo);
print("foo.bar = " + foo.bar);
print("new_thing = " + new_thing);
EOS
    assert_equal("foo = Object\nfoo.bar = bletch\nnew_thing = null\n", out)

# FIXME this should work
#     $foo.bar = 'xyzzy'
#     $new_thing = 'hello'
#     run_js <<EOS
# print("foo = " + foo);
# print("foo.bar = " + foo.bar);
# print("new_thing = " + new_thing);
# EOS
#     assert_equal("foo = Object\nfoo.bar = xyzzy\nnew_thing = hello\n", out)
  end

end
