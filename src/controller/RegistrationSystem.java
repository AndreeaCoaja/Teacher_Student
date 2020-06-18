package controller;

import exceptions.AlreadyExistsException;
import exceptions.DoesntExistException;
import exceptions.NoFreePlacesException;
import exceptions.TooManyCreditsException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Course;
import model.Student;
import model.Teacher;
import repository.CourseJDBCRepo;
import repository.ICrudRepository;
import repository.StudentJDBCRepo;
import repository.TeacherJDBCRepo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RegistrationSystem {
    private ICrudRepository<Course> courseJDBCRepository;
    private ICrudRepository<Student> studentJDBCRepository;
    private ICrudRepository<Teacher> teacherJDBCRepository;

    public RegistrationSystem() {
        this.courseJDBCRepository = new CourseJDBCRepo();
        this.studentJDBCRepository = new StudentJDBCRepo();
        this.teacherJDBCRepository = new TeacherJDBCRepo();
    }

    private void writeInDB(Long c, Long s) {
        try {
            String url = "jdbc:sqlserver://localhost;databaseName=javaLab;integratedSecurity=false;user=sa;password=sa";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(url);
            // create the java statement
            Statement st = conn.createStatement();

            st.executeUpdate("INSERT INTO Enrolled (idStudents, idCourse) VALUES (" + s + ", " + c + ")");

            conn.close();
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
    }

    //Functions from Teacher Repo
    public void saveTeacher(Teacher teacher) throws SQLException {
        teacherJDBCRepository.save(teacher);
    }

    public List<Teacher> findAllTeachers() {
        return (List<Teacher>) teacherJDBCRepository.findAll();
    }

    public Teacher findOneTeacher(Long id) throws SQLException {
        return teacherJDBCRepository.findOne(id);
    }

    public Teacher update(Teacher teacher) throws SQLException {
        return teacherJDBCRepository.update(teacher);
    }

    public void deleteTeacher(Long id) throws SQLException {
        teacherJDBCRepository.delete(id);
        List<Course> courses = (List<Course>) courseJDBCRepository.findAll();
        courses.forEach(c -> {
            if (c.getTeacher().getId() == id) {
                Course deletedCourse = null; //il folosesc la stergerea cursului din listele studentilor
                try {
                    deletedCourse = courseJDBCRepository.findOne(c.getId());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    courseJDBCRepository.delete(c.getId()); //sterg cursul
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                List<Student> students = retriveStudentsFromACourse(c.getId());
                Course finalDeletedCourse = deletedCourse;
                students.forEach(s -> {
                    List<Course> coursesOfTheStudent = s.getEnrolledCourse();
                    coursesOfTheStudent.remove(finalDeletedCourse);
                    s.setEnrolledCourse(coursesOfTheStudent);
                    try {
                        studentJDBCRepository.update(s);//updatez lista de cursuri a studentului respectiv
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    //Functions from Student Repo
    public void save(Student student) throws SQLException {
        studentJDBCRepository.save(student);
    }

    public List<Student> findAllStudents() {
        return (List<Student>) studentJDBCRepository.findAll();
    }

    public Student findOneStudent(Long id) throws SQLException {
        return studentJDBCRepository.findOne(id);
    }

    public Student update(Student student) throws SQLException {
        return studentJDBCRepository.update(student);
    }

    public void deleteStudent(Long id) throws SQLException {
        Student s = studentJDBCRepository.findOne(id);
        studentJDBCRepository.delete(id); //sterg studentul
        List<Course> courses = s.getEnrolledCourse();
        courses.forEach(c -> {
            List<Long> students = c.getStudentsEnrolled();
            students.forEach(stud -> {
                if (stud == id) {
                    students.remove(stud);
                    c.setStudentsEnrolled(students); //resetez lista de studenti din cursurile in care studentul era inscris
                    try {
                        courseJDBCRepository.update(c); //updatez lista de cursuri in fisier
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    //Functions from Course Repo
    public List<Course> findAllCourses() {
        return (List<Course>) courseJDBCRepository.findAll();
    }

    public Course findOneCourse(Long id) throws SQLException {
        return courseJDBCRepository.findOne(id);
    }

    public Course update(Course newCourse) throws SQLException {
        return courseJDBCRepository.update(newCourse);
    }

    public void deleteCourse(Long id) throws SQLException {
        Course c = courseJDBCRepository.findOne(id);
        courseJDBCRepository.delete(id);
        List<Student> students = retriveStudentsFromACourse(c.getId());
        students.forEach(s -> {
            try {
                studentJDBCRepository.delete(s.getId()); //sterg studentii inregistrati in cursul respectiv
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void save(Course course) throws SQLException {
        courseJDBCRepository.save(course);
    }


    /**
     * @param idCourse
     * @param idStudent
     * @return true if it is suceeded registered
     */
    public boolean register(long idCourse, long idStudent) throws Exception {
        List<Course> courses = (List<Course>) courseJDBCRepository.findAll();
        Course course = courseJDBCRepository.findOne(idCourse);

        List<Student> students = (List<Student>) studentJDBCRepository.findAll();
        Student student = studentJDBCRepository.findOne(idStudent);
        if (course == null || student == null)
            throw new DoesntExistException("The id you gave in doesnt exists ");

        if (course.getMaxEnrollment() == course.getEnrolled()) { //daca lista de studenti a cursului e egal cu maxEnrolled
            throw new NoFreePlacesException("The course has no free places");
        }
        AtomicInteger ok = new AtomicInteger();
        students.forEach(s -> {
            ok.set(0);
            if (s.getId() == student.getId()) {
                List<Course> studentCourses = s.getEnrolledCourse();

                //daca studentul este deja inscris la acest curs
                studentCourses.forEach(course1 -> {
                    if (course1.getId() == idCourse)
                        throw new AlreadyExistsException("The student is enrolled in this course");
                });

                //daca creditele studentului + creditele cursului respecta conditia
                if (s.getTotalCredits() + course.getCredits() <= 30) {
                    s.setTotalCredits(s.getTotalCredits() + course.getCredits()); //adaug creditele la student
                    studentCourses.add(course);
                    s.setEnrolledCourse(studentCourses); //adaug cursul la student
                    List<Student> studentsFromThisCourse = retriveStudentsFromACourse(course.getId());
                    List<Long> idFromStudents = new ArrayList<>();
                    for (Student student1 : studentsFromThisCourse) {
                        idFromStudents.add(student1.getId());
                    }
                    studentsFromThisCourse.add(s);
                    try {
                        update(s);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    course.setStudentsEnrolled(idFromStudents); //adaug si in lista de studenti a cursului respectiv
                    course.setEnrolled(course.getEnrolled() + 1); //actualizez atributul ce tine minte nr de studenti inscrisi
                    try {
                        update(course);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    ok.set(1);
                    writeInDB(idCourse, idStudent);
                } else
                    throw new TooManyCreditsException("The student has too many credits");
            }
        });
        return ok.get() == 1;
    }

    /**
     * @return a list with courses which have free places
     */
    public List<Course> retriveCoursesWithFreePlaces() {
        List<Course> freeCourses = new ArrayList<Course>();
        List<Course> allCourse = (List<Course>) courseJDBCRepository.findAll();
        allCourse.forEach(c -> {
            if (c.getMaxEnrollment() != c.getEnrolled()) {
                freeCourses.add(c);
            }
        });
        return freeCourses;
    }

    /**
     * @param id
     * @return a list with all students enrolled in Course c
     */
    public List<Student> retriveStudentsFromACourse(long id) {
        List<Student> students = new ArrayList<Student>();
        List<Student> allStudents = (List<Student>) studentJDBCRepository.findAll();
        allStudents.forEach(student -> {
            List<Course> courseStudent = student.getEnrolledCourse();
            courseStudent.forEach(course -> {
                if (course.getId() == id) {
                    students.add(student);
                }
            });
        });
        return students;
    }

    /**
     * Returns the courses for the specific student s
     *
     * @param s is the student
     * @return
     */
    private List<Course> retriveCoursesFromAStudent(Long s) {
        List<Course> courses = new ArrayList<Course>();
        List<Course> allCourses = (List<Course>) courseJDBCRepository.findAll();
        allCourses.forEach(c -> {
            List<Long> students = c.getStudentsEnrolled();
            students.forEach(student -> {
                if (student == s) {
                    courses.add(c);
                }
            });
        });
        return courses;
    }

    /**
     * Sorting function for a StudentList made on the Credits
     * BUBBLE SORT
     *
     * @param students
     * @return sorted List made on the number of credits
     */
    public List<Student> sortStudents(List<Student> students) {
        students.sort(Comparator.comparingInt(Student::getTotalCredits));
        return students;
    }

    /**
     * Sorting function for a CourseList made on the Name of the courses
     *
     * @param courses
     * @return sorted List made on the name of the courses
     */
    public List<Course> sortCourses(List<Course> courses) {
        courses.sort(Comparator.comparing(Course::getName));
        return courses;
    }

    /**
     * Filter a list of students
     *
     * @param students
     * @return a list with students who have more than 0 Credits
     */
    public List<Student> filterStudents(List<Student> students) {
        return students.stream().filter(student -> student.getTotalCredits() > 0).collect(Collectors.toList());
    }

    /**
     * Removes a student from a course
     *
     * @param studentId student ID
     * @param courseId  course ID
     * @throws Exception if file i/o fails
     */
    public void removeStudentFromCourse(long studentId, long courseId) throws Exception {
        Student student = studentJDBCRepository.findOne(studentId);
        Course course = courseJDBCRepository.findOne(courseId);
        if (student != null && course != null) {
            List<Course> coursesFromStudent = student.getEnrolledCourse();
            Course cursExistent = findOneCourse(courseId);
            if (cursExistent == null)
                throw new DoesntExistException("This student is not enrolled in this course.");
            else {
                for (Course c : coursesFromStudent) {
                    System.out.println(c.getId());
                }
                Course x = this.findOneCourse(courseId);
                coursesFromStudent.remove(x);

                student.setEnrolledCourse(coursesFromStudent);


                List<Long> students = course.getStudentsEnrolled();
                Student s = this.findOneStudent(studentId);
                students.remove(studentId);

                course.setStudentsEnrolled(students);

                student.setTotalCredits(student.getTotalCredits() - course.getCredits());

                this.studentJDBCRepository.update(student);
                this.courseJDBCRepository.update(course);
            }

        } else {
            throw new DoesntExistException("Invalid course or student ID.");
        }
    }

    /**
     * Creates an observable list with students for each teacher
     *
     * @param id teacher ID
     * @return list of students
     */
    public ObservableList<Student> observableStudents(long id) throws SQLException {
        Teacher teacher = this.teacherJDBCRepository.findOne(id);
        ObservableList<Student> students = FXCollections.observableArrayList();
        this.studentJDBCRepository.findAll().forEach(student -> {
            AtomicBoolean intersection = new AtomicBoolean(false);
            List<Course> c = student.getEnrolledCourse();
            List<Long> idC = new ArrayList<>();
            for (Course cr : c) {
                idC.add(cr.getId());
            }
            idC.forEach(course -> {
                if (teacher.getCourses().contains(course))
                    intersection.set(true);
            });
            if (intersection.get())
                students.add(student);
        });
        return students;
    }


    /**
     * Creates an observable list with courses in which are enrolled each student
     *
     * @return list of courses
     */
    public ObservableList<Course> observableCourses(Long id) throws SQLException {
        ObservableList<Course> courses = FXCollections.observableArrayList();
        Student stud = this.studentJDBCRepository.findOne(id);
        List<Course> c = stud.getEnrolledCourse();
        courses.addAll(c);

        return courses;
    }


    public ObservableList<Student> observableStudents2(long id, long idS) throws SQLException {
        Teacher teacher = this.teacherJDBCRepository.findOne(id);
        ObservableList<Student> students = FXCollections.observableArrayList();
        List<Student> studentss = (List<Student>) this.studentJDBCRepository.findAll();
        Student find = findOneStudent(idS);
        for (Student s : studentss) {
            if (s.getId() != find.getId()) {
                List<Course> c = s.getEnrolledCourse();
                List<Long> idC = new ArrayList<>();
                for (Course cr : c) {
                    idC.add(cr.getId());
                }
                for (Long bla : idC) {
                    if (teacher.getCourses().contains(bla))
                        students.add(s);
                }
            }
        }
        return students;
    }
}
