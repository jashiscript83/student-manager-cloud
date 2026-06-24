package com.sohrabi.service;

import com.sohrabi.entity.Department;
import com.sohrabi.entity.Student;
import com.sohrabi.entity.Subject;
import com.sohrabi.repository.DepartmentRepository;
import com.sohrabi.repository.StudentRepository;
import com.sohrabi.repository.SubjectRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    private StudentServiceImpl studentService;

    private Student student;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl();

        ReflectionTestUtils.setField(studentService, "studentRepository", studentRepository);
        ReflectionTestUtils.setField(studentService, "departmentRepository", departmentRepository);
        ReflectionTestUtils.setField(studentService, "subjectRepository", subjectRepository);

        Executor sameThreadExecutor = Runnable::run;
        ReflectionTestUtils.setField(studentService, "executor", sameThreadExecutor);

        Department dept = new Department();
        dept.setId("dept1");

        Subject sub = new Subject();
        sub.setId("sub1");

        student = new Student();
        student.setId("student1");
        student.setName("Test");
        student.setEmail("test@test.com");
        student.setDepartment(dept);
        student.setSubjects(Arrays.asList(sub));
    }


    @Test
    void createStudent_shouldSaveAllEntities() {
        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.createStudent(student);

        verify(departmentRepository, times(1)).save(student.getDepartment());
        verify(subjectRepository, times(1)).saveAll(student.getSubjects());
        verify(studentRepository, times(1)).save(student);

        Assertions.assertEquals(student, result);
    }

    @Test
    void createStudent_withoutDepartmentOrSubjects() {
        student.setDepartment(null);
        student.setSubjects(null);

        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.createStudent(student);

        verify(departmentRepository, never()).save(any());
        verify(subjectRepository, never()).saveAll(any());
        verify(studentRepository, times(1)).save(student);

        Assertions.assertEquals(student, result);
    }


    @Test
    void getStudentById_found() {
        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));

        Student result = studentService.getStudentbyId("student1");

        Assertions.assertEquals(student, result);
    }

    @Test
    void getStudentById_notFound_shouldThrow() {
        when(studentRepository.findById("student1")).thenReturn(Optional.empty());

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                () -> studentService.getStudentbyId("student1"));

        Assertions.assertTrue(ex.getMessage().contains("Student not found"));
    }


    @Test
    void deleteStudent_shouldDeleteAll() {
        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));

        String result = studentService.deleteStudent("student1");

        verify(subjectRepository).deleteAll(student.getSubjects());
        verify(departmentRepository).deleteById(student.getDepartment().getId());
        verify(studentRepository).deleteById("student1");

        Assertions.assertEquals("Student has been deleted.", result);
    }

    @Test
    void deleteStudent_notFound() {
        when(studentRepository.findById("student1")).thenReturn(Optional.empty());

        String result = studentService.deleteStudent("student1");

        verify(subjectRepository, never()).deleteAll(any());
        verify(departmentRepository, never()).deleteById(any());
        verify(studentRepository, never()).deleteById(any());

        Assertions.assertEquals("Student not found", result);
    }

    @Test
    void deleteStudent_withoutSubjects() {
        student.setSubjects(Collections.emptyList());
        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));

        String result = studentService.deleteStudent("student1");

        verify(subjectRepository, never()).deleteAll(any());
        verify(studentRepository).deleteById("student1");

        Assertions.assertEquals("Student has been deleted.", result);
    }


    @Test
    void updateStudent_shouldCallRepository() {
        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.updateStudent(student);

        verify(studentRepository).save(student);
        Assertions.assertEquals(student, result);
    }


    @Test
    void getStudentsByName() {
        when(studentRepository.findByName("Test")).thenReturn(Arrays.asList(student));

        Assertions.assertEquals(1, studentService.getStudentsByName("Test").size());
    }

    @Test
    void studentsByNameAndMail() {
        when(studentRepository.findByEmailAndName("test@test.com", "Test"))
                .thenReturn(student);

        Student result = studentService.studentsByNameAndMail("Test", "test@test.com");

        Assertions.assertEquals(student, result);
    }

    @Test
    void studentsByNameOrMail() {
        when(studentRepository.findByNameOrEmail("Test", "test@test.com"))
                .thenReturn(student);

        Student result = studentService.studentsByNameOrMail("Test", "test@test.com");

        Assertions.assertEquals(student, result);
    }

    @Test
    void createStudent_shouldRunAsyncOperations() {

        Student student = new Student();

        Department dept = new Department();
        student.setDepartment(dept);

        Subject sub = new Subject();
        student.setSubjects(Arrays.asList(sub));

        when(studentRepository.save(any())).thenReturn(student);

        long start = System.currentTimeMillis();

        studentService.createStudent(student);

        long duration = System.currentTimeMillis() - start;

        verify(departmentRepository).save(any());
        verify(subjectRepository).saveAll(any());

        Assertions.assertTrue(duration < 2000);
    }
}