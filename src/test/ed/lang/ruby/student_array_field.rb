class StudentArrayField < XGen::Mongo::Base

  collection_name :rubytest_students
  fields :name, :email, :math_scores

end
