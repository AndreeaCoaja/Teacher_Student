package model;

import exceptions.DoesntExistException;

import java.util.List;

public class Student extends Person{
    private int totalCredits;
    private List<Course> enrolledCourse;

    public Student(String firstName, String lastName, Long id,int totalCredits, List<Course> enrolledCourses) {
        super(firstName, lastName, id);
        this.totalCredits = totalCredits;
        this.enrolledCourse = enrolledCourses;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public List<Course> getEnrolledCourse() {
        if(enrolledCourse==null)
            throw new DoesntExistException("The student isnt enrolled in any course");
        return enrolledCourse;
    }

    public void setEnrolledCourse(List<Course> enrolledCourse) {
        this.enrolledCourse = enrolledCourse;
    }

}
