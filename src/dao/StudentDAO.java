package dao;

import dto.Student;
import util.DataBaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // 새 학생을 데이터베이스에 추가하는 기능
    public void addStudent(Student student) throws SQLException {
        // 쿼리문
        String sql = "INSERT INTO students (name, student_id) " +
                "VALUES (?, ?) ";
        try (Connection conn = DataBaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getStudentId());
            pstmt.executeUpdate();
        }

    }

    // 모든 학생 목록을 조회하는 기능
    public List<Student> getAllStudents() throws SQLException {
        List<Student> studentList = new ArrayList<>();
        String sql = "SELECT * FROM students ";
        try (Connection conn = DataBaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String studentId = rs.getString("student_id");

                Student student = new Student(id, name, studentId);
                studentList.add(student);
            }
        }
        return studentList;
    }

    // 학생 student_id로 학생 인증(로그인 용) 기능
    public Student authenticateStudent(String studentId) throws SQLException {
        // 학생이 정확한 학번을 입력하면 Student 객체가 만들어져서 리턴됨
        // 학생이 잘못된 학번을 입력하면 null 값을 반환함
        // if .... return new Student();

        return null;
    }

}
