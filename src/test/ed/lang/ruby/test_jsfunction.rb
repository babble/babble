require 'ruby_test'

class JSFunctionTest < RubyTest

  def test_call_block_as_jsfunction
    $func = Proc.new { |i| i + 7 }
    run_js "answer = {}; answer.func = func(35);"
    assert_not_nil($answer)
    assert_equal(42, $answer['func'].to_i)
  end

end
