package repository;

import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class CourseJDBCRepo implements ICrudRepository<Course> {
    private List<Course> courses = new ArrayList<>();
    private JDBCUtil jdbcUtil;

    public CourseJDBCRepo() {
        this.jdbcUtil=new JDBCUtil();
        readFromDB();
    }

    private void readFromDB() {
        try {

            // SQL SELECT query.
            String query = "SELECT * FROM Courses";

            // create the java statement
            Statement st = jdbcUtil.getConnection().createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {
                String name = rs.getString("Name");
                long idTeacher = rs.getLong("idTeacher");
                int maxEnrollment = rs.getInt("maxEnrollment");
                int credits = rs.getInt("Credits");
                long id = rs.getLong("ID");


                // adding the values in Course List
                TeacherJDBCRepo t = new TeacherJDBCRepo();
                Teacher teacher = t.findOne(idTeacher);

                List<Long> stud = new ArrayList<>();
                Statement st2 = jdbcUtil.getConnection().createStatement();
                ResultSet rs2 = st2.executeQuery("select * from Enrolled");
                while (rs2.next()) {
                    int crsID = rs2.getInt("idCourse");
                    if (crsID == id) {
                        int studID = rs2.getInt("idStudents");
                        stud.add((long) studID);
                    }
                }
                st2.close();
                rs2.close();
                Course course = new Course(name, teacher, maxEnrollment, credits, id);
                course.setStudentsEnrolled(stud);
                this.courses.add(course);
            }
            st.close();
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
    }

    @Override
    public Course findOne(Long idCourse) throws SQLException {

        // SQL SELECT query.
        String query = "SELECT * FROM Courses Where ID="+idCourse;

        // create the java statement
        Statement st = jdbcUtil.getConnection().createStatement();

        // execute the query, and get a java resultset
        ResultSet rs = st.executeQuery(query);


        Course course = null;
        while (rs.next()) {
            String name = rs.getString("Name");
            long idTeacher = rs.getLong("idTeacher");
            int maxEnrollment = rs.getInt("maxEnrollment");
            int credits = rs.getInt("Credits");
            long id = rs.getLong("ID");


            // adding the values in Course List
            TeacherJDBCRepo t = new TeacherJDBCRepo();
            Teacher teacher = t.findOne(idTeacher);

            List<Long> stud = new ArrayList<>();
            Statement st2 = jdbcUtil.getConnection().createStatement();
            ResultSet rs2 = st2.executeQuery("select * from Enrolled");
            while (rs2.next()) {
                int crsID = rs2.getInt("idCourse");
                if (crsID == id) {
                    int studID = rs2.getInt("idStudents");
                    stud.add((long) studID);
                }
            }
            st2.close();
            rs2.close();
            course = new Course(name, teacher, maxEnrollment, credits, id);
            course.setStudentsEnrolled(stud);
        }

        st.close();
        rs.close();
        return course;
    }

    @Override
    public Iterable<Course> findAll() {
        return this.courses;
    }

    @Override
    public Course save(Course entity) throws SQLException {
        if (entity != null && !courses.contains(entity)) {
            String query = "INSERT INTO Courses (Name, idTeacher, maxEnrollment, Credits, ID) VALUES (?,?,?,?,?)";

            /// create the java prparedStatement
            PreparedStatement preparedStmt = jdbcUtil.getConnection().prepareStatement(query);

            preparedStmt.setString(1, entity.getName());
            preparedStmt.setInt(2, (int) entity.getTeacher().getId());
            preparedStmt.setInt(3, entity.getMaxEnrollment());
            preparedStmt.setInt(4, entity.getCredits());
            preparedStmt.setInt(5, (int) entity.getId());
            preparedStmt.execute();
            courses.add(entity);

            preparedStmt.close();
            return null;
        }
        return entity;
    }

    @Override
    public Course delete(Long id) throws SQLException {
        if (id != null && findOne(id) != null) {
            Statement st = jdbcUtil.getConnection().createStatement();
            String sql = "delete from Courses where ID=" + id;
            st.executeUpdate(sql);
            Course x = findOne(id);
            courses.remove(x);

            st.execute("Delete from Enrollment where Enrollment.courseId=" + id);

            st.close();
            return x;
        }
        return null;
    }


    @Override
    public Course update(Course entity) throws SQLException {
        if (entity != null && findOne(entity.getId()) != null) {
            Statement st = jdbcUtil.getConnection().createStatement();
            String query = "Update Courses set Name=?, idTeacher=?, maxEnrollment=?, Credits=? where ID=?";
            /// create the java prparedStatement
            PreparedStatement preparedStmt = jdbcUtil.getConnection().prepareStatement(query);

            preparedStmt.setString(1, entity.getName());
            preparedStmt.setInt(2, (int) entity.getTeacher().getId());
            preparedStmt.setInt(3, entity.getMaxEnrollment());
            preparedStmt.setInt(4, entity.getCredits());
            preparedStmt.setInt(5, (int) entity.getId());
            preparedStmt.execute();

            findOne(entity.getId()).setMaxEnrollment(entity.getMaxEnrollment());
            findOne(entity.getId()).setName(entity.getName());
            findOne(entity.getId()).setStudentsEnrolled(entity.getStudentsEnrolled());
            findOne(entity.getId()).setTeacher(entity.getTeacher());

            preparedStmt.close();

            return null;
        }
        return entity;
    }
}
