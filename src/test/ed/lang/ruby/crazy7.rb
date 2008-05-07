
class A
  def initialize
    @good = {}
  end
  
  def blah
    puts "blah"
    return true
  end

  def foo
    self.blah and @good[:soon]="asdasd"
  end

  def foo2
    self.blah and @good[:soon]="asdasd"[:abc]
  end
  
  def foo3
    this is just to check compliaation
    self.blah and @good[:soon]="asdasd"[:abc]
    5
    respond_to do |format|
            format.html { redirect_to edit_user_path(@user) }
            format.xml  { head 200 }
  end
  end

  def p
    puts ( @good[:soon] == nil )
  end
  
end

a = A.new

a.foo
a.p

a.foo2
a.p
