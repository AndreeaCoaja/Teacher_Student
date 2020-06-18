package repository;


import model.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherJDBCRepo implements ICrudRepository<Teacher> {
    List<Teacher> teachers = new ArrayList<>();
    JDBCUtil jdbcUtil;

    public TeacherJDBCRepo() {
        this.jdbcUtil = new JDBCUtil();
        readFromDB();
    }

    public void readFromDB() {
        try {

            // SQL SELECT query.
            String query = "SELECT * FROM Teachers";

            // create the java statement
            Statement st = jdbcUtil.getConnection().createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {

                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                Long idTeacher = rs.getLong("idTeacher");

                List<Long> crs = new ArrayList<>();
                Statement myStmt2 = jdbcUtil.getConnection().createStatement();
                ResultSet myRs2 = myStmt2.executeQuery("select ID, idTeacher from Courses");
                while (myRs2.next()) {
                    int idT = myRs2.getInt("idTeacher");
                    if (idT == idTeacher) {
                        int cid = myRs2.getInt("ID");
                        crs.add((long) cid);
                    }
                }
                myStmt2.close();
                myRs2.close();

                // adding the values in Teacher List
                Teacher t = new Teacher(firstName, lastName, idTeacher);
                t.setCourses(crs);
                this.teachers.add(t);
            }
            st.close();
            rs.close();
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
    }



    @Override
    public Teacher findOne(Long id) throws SQLException {


        // SQL SELECT query.
        String query = "SELECT * FROM Teachers Where idTeacher=" + id;

        // create the java statement
        Statement st = jdbcUtil.getConnection().createStatement();

        // execute the query, and get a java resultset
        ResultSet rs = st.executeQuery(query);

        Teacher t = null;
        // iterate through the java resultset
        while (rs.next()) {

            String firstName = rs.getString("FirstName");
            String lastName = rs.getString("LastName");
            Long idTeacher = rs.getLong("idTeacher");

            List<Long> crs = new ArrayList<>();
            Statement myStmt2 = jdbcUtil.getConnection().createStatement();
            ResultSet myRs2 = myStmt2.executeQuery("select ID, idTeacher from Courses");
            while (myRs2.next()) {
                int idT = myRs2.getInt("idTeacher");
                if (idT == idTeacher) {
                    int cid = myRs2.getInt("ID");
                    crs.add((long) cid);
                }
            }
            myStmt2.close();
            myRs2.close();

            // adding the values in Teacher List
            t = new Teacher(firstName, lastName, idTeacher);
            t.setCourses(crs);
        }
        st.close();
        rs.close();
        return t;
    }

    @Override
    public Iterable<Teacher> findAll() {
        return teachers;
    }

    @Override
    public Teacher save(Teacher entity) throws SQLException {
        if (entity != null && !teachers.contains(entity)) {
            String query = "insert into Teachers (FirstName,LastName, idTeacher) values (?,?,?)";
            PreparedStatement preparedStmt = jdbcUtil.getConnection().prepareStatement(query);
            preparedStmt.setString(1, entity.getFirstName());
            preparedStmt.setString(2, entity.getLastName());
            preparedStmt.setInt(3, (int) entity.getId());
            preparedStmt.execute();
            teachers.add(entity);

            preparedStmt.close();
            return null;
        }
        return entity;
    }

    @Override
    public Teacher delete(Long id) throws SQLException {
        if (id != null && findOne(id) != null) {
            Statement stmt = jdbcUtil.getConnection().createStatement();
            String query = "Delete from Teachers where idTeacher=" + id;
            stmt.executeUpdate(query);
            Teacher x = findOne(id);
            teachers.remove(x);
            stmt.close();
            return x;
        }
        return null;
    }

    @Override
    public Teacher update(Teacher entity) throws SQLException {
        if (entity != null && findOne(entity.getId()) != null) {
            String query = "Update Teachers set FirstName=?, LastName=? where idTeacher=?";
            PreparedStatement preparedStmt = jdbcUtil.getConnection().prepareStatement(query);

            preparedStmt.setString(1, entity.getFirstName());
            preparedStmt.setString(2, entity.getLastName());
            preparedStmt.setInt(3, (int) entity.getId());
            preparedStmt.execute();
            preparedStmt.close();

            findOne(entity.getId()).setCourses(entity.getCourses());
            findOne(entity.getId()).setFirstName(entity.getFirstName());
            findOne(entity.getId()).setLastName(entity.getLastName());
            return null;
        }
        return entity;
    }
}
