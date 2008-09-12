require 'test/unit'

TEST_JS_FNAME = '__temp_run_js'
TEST_JS_DIR = File.dirname(__FILE__)

class RubyTest < Test::Unit::TestCase

  def setup
    @existing_keys = $scope.keys
  end

  def teardown
    others = $scope.keys - @existing_keys
    others.each { |k| $scope.remove(k) }
  end

  def run_js(js)
    fname = TEST_JS_FNAME + rand(0xffffffff).to_s
    path = File.join(TEST_JS_DIR, fname + '.js')
    $scope['jsout'] = ''
    begin
      File.open(path, 'w') { |f| f.puts js }
      load fname
    rescue => ex
      fail(ex.to_s)
    ensure
     File.delete(path) if File.exist?(path)
    end
    return $scope['jsout']
  end

  # A dummy test is necessary because the TestUnit code will look for at least
  # one test in this class, even though we just want it to be the superclass
  # of the other tests.
  def test_dummy
    assert true
  end
end
