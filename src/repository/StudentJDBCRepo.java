package repository;

import model.Course;
import model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentJDBCRepo implements ICrudRepository<Student> {
    List<Student> students = new ArrayList<>();
    JDBCUtil jdbcUtil;

    public StudentJDBCRepo() {
        this.jdbcUtil=new JDBCUtil();
        readFromDB();
    }

    public void readFromDB() {
        try {


            // SQL SELECT query.
            String query = "SELECT * FROM Students";

            // create the java statement
            Statement st = jdbcUtil.getConnection().createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {

                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                Long idStudent = rs.getLong("idStudent");
                int totalCredits = rs.getInt("totalCredits");
                List<Course> crs = new ArrayList<>();
                CourseJDBCRepo crsRepo = new CourseJDBCRepo();
                Statement myStmt2 = jdbcUtil.getConnection().createStatement();
                ResultSet myRs2 = myStmt2.executeQuery("select * from Enrolled");
                while (myRs2.next()) {
                    int studID = myRs2.getInt("idStudents");
                    if (studID == idStudent) {
                        int crsID = myRs2.getInt("idCourse");
                        crs.add(crsRepo.findOne((long) crsID));
                    }
                }
                myStmt2.close();
                myRs2.close();
                Student stud = new Student(firstName, lastName, idStudent, totalCredits, crs);
                this.students.add(stud);

            }
            st.close();
            rs.close();
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
    }

    @Override
    public Student findOne(Long id) throws SQLException {
        // SQL SELECT query.
        String query = "SELECT * FROM Students Where idStudent="+id;

        // create the java statement
        Statement st = jdbcUtil.getConnection().createStatement();

        // execute the query, and get a java resultset
        ResultSet rs = st.executeQuery(query);


        Student stud=null;
        while (rs.next()) {
            String firstName = rs.getString("FirstName");
            String lastName = rs.getString("LastName");
            Long idStudent = rs.getLong("idStudent");
            int totalCredits = rs.getInt("totalCredits");
            List<Course> crs = new ArrayList<>();
            CourseJDBCRepo crsRepo = new CourseJDBCRepo();
            Statement myStmt2 = jdbcUtil.getConnection().createStatement();
            ResultSet myRs2 = myStmt2.executeQuery("select * from Enrolled");
            while (myRs2.next()) {
                int studID = myRs2.getInt("idStudents");
                if (studID == idStudent) {
                    int crsID = myRs2.getInt("idCourse");
                    crs.add(crsRepo.findOne((long) crsID));
                }
            }
            myStmt2.close();
            myRs2.close();
            stud = new Student(firstName, lastName, idStudent, totalCredits, crs);
        }
        st.close();
        rs.close();
        return stud;
    }

    @Override
    public Iterable<Student> findAll() {
        return this.students;
    }

    @Override
    public Student save(Student entity) throws SQLException {
        if (entity != null && !students.contains(entity)) {
            String query = "insert into Students (FirstName, LastName, idStudent, totalCredits) values (?,?,?,?)";

            PreparedStatement preparedStmt = jdbcUtil.getConnection().prepareStatement(query);
            preparedStmt.setString(1, entity.getFirstName());
            preparedStmt.setString(2, entity.getLastName());
            preparedStmt.setInt(3, (int) entity.getId());
            preparedStmt.setInt(4, entity.getTotalCredits());
            preparedStmt.execute();
            students.add(entity);

            preparedStmt.close();
            return null;
        }
        return entity;
    }

    @Override
    public Student delete(Long id) throws SQLException {
        if (id != null && findOne(id) != null) {
            Statement stmt = jdbcUtil.getConnection().createStatement();
            String query = "Delete from Students where idStudent=" + id;
            stmt.executeUpdate(query);
            Student x = findOne(id);
            students.remove(x);

            stmt.close();
            return x;
        }
        return null;
    }

    @Override
    public Student update(Student entity) throws SQLException {
        if (entity != null && findOne(entity.getId()) != null) {
            String query = "Update Students set FirstName=?, LastName=?, totalCredits=? where idStudent=?";
            PreparedStatement preparedStmt = jdbcUtil.getConnection().prepareStatement(query);

            preparedStmt.setString(1, entity.getFirstName());
            preparedStmt.setString(2, entity.getLastName());
            preparedStmt.setInt(3, entity.getTotalCredits());
            preparedStmt.setInt(4, (int) entity.getId());
            preparedStmt.execute();

            preparedStmt.close();
            findOne(entity.getId()).setTotalCredits(entity.getTotalCredits());
            findOne(entity.getId()).setEnrolledCourse(entity.getEnrolledCourse());

            Statement stmt = jdbcUtil.getConnection().createStatement();
            String query1 = "Delete from Enrolled where Enrolled.idStudents=" + entity.getId();
            stmt.executeUpdate(query1);
            stmt.close();
            return null;
        }
        return entity;
    }
}
