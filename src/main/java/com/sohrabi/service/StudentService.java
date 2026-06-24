package com.sohrabi.service;

import com.sohrabi.entity.Department;
import com.sohrabi.entity.Student;
import com.sohrabi.entity.Subject;

import java.util.List;

public interface StudentService {

     Student createStudent (Student student);

     List<Student> getAllStudents();

     Student updateStudent (Student student) ;

     Department updateDepartment (Department department) ;

     Subject updateSubject (Subject subject) ;

     String deleteStudent (String id) ;

     List<Student> getStudentsByName (String name) ;

     Student studentsByNameAndMail (String name, String email);
     Student studentsByNameOrMail (String name, String email);

     List<Student> getAllWithPagination (int pageNo, int pageSize) ;

     List<Student> allWithSorting () ;
     List<Student> byDepartmentId (String deptId) ;
     List<Student> emailLike (String email) ;

     List<Student> nameStartsWith (String name) ;
}
