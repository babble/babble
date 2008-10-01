class StudentHasMany < XGen::Mongo::Base

  collection_name :rubytest_students
  fields :name, :email
  has_many :addresses, :class_name => 'Address'

end
