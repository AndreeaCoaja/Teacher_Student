package model;

import java.util.ArrayList;
import java.util.List;

public class Teacher extends Person{
    private List<Long> courses;


    public Teacher(String firstName, String lastName,Long id) {
        super(firstName, lastName, id);
        this.courses = new ArrayList<>();
    }


    public List<Long> getCourses() {
        return courses;
    }

    public void setCourses(List<Long> courses) {
        this.courses = courses;
    }
}
