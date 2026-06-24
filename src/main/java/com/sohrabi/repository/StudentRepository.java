package com.sohrabi.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.sohrabi.entity.Student;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {

	List<Student> findByName(String name);

//    custom queries example
	@Query("{ \"name\" : ?0 }")
	List<Student> findByCustomName(String name);

	Student findByEmailAndName (String email, String name);
	
	Student findByNameOrEmail (String name, String email);
	
	List<Student> findByDepartmentId(String deptId);

	List<Student> findByEmailIsLike (String email);
	
	List<Student> findByNameStartsWith (String name);
}
