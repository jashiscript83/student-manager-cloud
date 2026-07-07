package com.sohrabi.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.sohrabi.entity.Department;
import com.sohrabi.entity.Student;
import com.sohrabi.entity.Subject;
import com.sohrabi.repository.DepartmentRepository;
import com.sohrabi.repository.StudentRepository;
import com.sohrabi.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService{
	
	@Autowired
	StudentRepository studentRepository;

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	SubjectRepository subjectRepository;

	@Autowired
	private Executor executor;


	public Student createStudent(Student student) {

		CompletableFuture<Void> deptFuture = CompletableFuture.completedFuture(null);
		CompletableFuture<Void> subjectsFuture = CompletableFuture.completedFuture(null);

		if (Objects.nonNull(student.getDepartment())) {
			deptFuture = CompletableFuture.runAsync(() ->
					departmentRepository.save(student.getDepartment()),executor
			);
		}

		if (Objects.nonNull(student.getSubjects()) && !student.getSubjects().isEmpty()) {
			subjectsFuture = CompletableFuture.runAsync(() ->
					subjectRepository.saveAll(student.getSubjects()),executor
			);
		}

		CompletableFuture.allOf(deptFuture, subjectsFuture).join();
        setOwner(student);
		return studentRepository.save(student);
	}
	
	public Student getStudentbyId(String id) {

		Student student =
				studentRepository.findById(id)
						.orElseThrow(
								() -> new RuntimeException(
										"Student not found with id: " + id
								)
						);

		allowUpdatAndDelete(student);

		return student;

	}
	
	public List<Student> getAllStudents(){

	Authentication auth =
			SecurityContextHolder
					.getContext()
					.getAuthentication();

    return studentRepository.findByOwnerId(
			auth.getName()
			);


}

	public Student updateStudent(Student student) {

		allowUpdatAndDelete(student);

		if (Objects.nonNull(student.getDepartment())) {

			Department department =
					departmentRepository.save(
							student.getDepartment()
					);

			student.setDepartment(department);
		}

		if (Objects.nonNull(student.getSubjects())
				&& !student.getSubjects().isEmpty()) {

			List<Subject> subjects =
					subjectRepository.saveAll(
							student.getSubjects()
					);

			student.setSubjects(subjects);
		}

		Student existing =
				studentRepository.findById(
						student.getId()
				).orElseThrow(
						() -> new RuntimeException("Student not found")
				);

		student.setOwnerId(
				existing.getOwnerId()
		);

		return studentRepository.save(student);
	}	public Department updateDepartment (Department department) {
		return departmentRepository.save(department);
	}
	public Subject updateSubject (Subject subject) {
		return subjectRepository.save(subject);
	}

	public String deleteStudent(String id) {

		Optional<Student> optionalStudent = studentRepository.findById(id);

		if (!optionalStudent.isPresent()) {
			return "Student not found";
		}
		allowUpdatAndDelete(optionalStudent.get());


		Student student = optionalStudent.get();

		CompletableFuture<Void> subjectsFuture = CompletableFuture.completedFuture(null);
		CompletableFuture<Void> departmentFuture = CompletableFuture.completedFuture(null);

		CompletableFuture<Void> studentFuture;

		if (student.getSubjects() != null && !student.getSubjects().isEmpty()) {
			subjectsFuture = CompletableFuture.runAsync(() ->
					subjectRepository.deleteAll(student.getSubjects()), executor
			);
		}

		if (Objects.nonNull(student.getDepartment())) {
			departmentFuture = CompletableFuture.runAsync(() ->
					departmentRepository.deleteById(student.getDepartment().getId()), executor
			);
		}

		studentFuture = CompletableFuture.runAsync(() ->
				studentRepository.deleteById(id), executor
		);

		CompletableFuture.allOf(subjectsFuture, departmentFuture, studentFuture).join();

		return "Student has been deleted.";
	}

	public List<Student> getStudentsByName (String name) {
		return studentRepository.findByName(name);
	}
	
	public Student studentsByNameAndMail (String name, String email) {
		return studentRepository.findByEmailAndName(email, name);
	}
	
	public Student studentsByNameOrMail (String name, String email) {
		return studentRepository.findByNameOrEmail(name, email);
	}
	
	public List<Student> getAllWithPagination (int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
		
		return studentRepository.findAll(pageable).getContent();
	}
	
	public List<Student> allWithSorting () {
		Sort sort = Sort.by(Sort.Direction.ASC, "name", "email");
		
		return studentRepository.findAll(sort);		
	}
	
	public List<Student> byDepartmentId (String deptId) {
		return studentRepository.findByDepartmentId(deptId);
	}

	public List<Student> emailLike (String email) {
		return studentRepository.findByEmailIsLike(email);
	}
	
	public List<Student> nameStartsWith (String name) {
		return studentRepository.findByNameStartsWith(name);
	}


	private void setOwner (Student student){

		Authentication auth =
				SecurityContextHolder
						.getContext()
						.getAuthentication();

		student.setOwnerId(
				auth.getName()
		);

	}

	private void allowUpdatAndDelete(Student student){

		Student existing =
				studentRepository.findById(
						student.getId()
				).orElseThrow(
						() -> new RuntimeException("Student not found")
				);

		Authentication auth =
				SecurityContextHolder
						.getContext()
						.getAuthentication();

		if (!existing.getOwnerId()
				.equals(auth.getName())) {

			throw new RuntimeException(
					"Unauthorized"
			);
		}

	}

}
