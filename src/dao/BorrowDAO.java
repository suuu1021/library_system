package dao;

import dto.Borrow;
import util.DataBaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    // 도서 대출 기능
    public void borrowBook(int bookId, int studentPK) throws SQLException {
        // 대출 가능 여부 == SELECT(books)
        // 대출 가능하다면 --> INSERT(borrows)
        // 대출이 실행되었다면 --> UPDATE (books -> available)

        String checkSql = "SELECT available FROM books where id = ? ";
        try (Connection conn = DataBaseUtil.getConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setInt(1, bookId);
            ResultSet rs1 = checkPstmt.executeQuery();
            if (rs1.next() && rs1.getBoolean("available")) {
                // insert, update
                String insertSql = "INSERT INTO borrows (student_id, book_id, borrow_date) \n" +
                        "VALUES(?, ?, CURRENT_DATE) ";
                String updateSql = "UPDATE books SET available = FALSE WHERE id = ?";

                try (PreparedStatement borrowStmt = conn.prepareStatement(insertSql);
                     PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    borrowStmt.setInt(1, studentPK);
                    borrowStmt.setInt(2, bookId);
                    System.out.println("-----------------------------------");
                    updateStmt.setInt(1, bookId);
                    borrowStmt.executeUpdate();
                    updateStmt.executeUpdate();
                }
            } else {
                throw new SQLException("도서가 대출 불가능 합니다");
            }
        }
    }

    // 현재 대출 중인 도서 목록을 조회
    public List<Borrow> getBorrowedBooks() throws SQLException {
        List<Borrow> borrowList = new ArrayList<>();
        String sql = "SELECT * FROM borrows WHERE return_date IS NULL ";
        try (Connection conn = DataBaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Borrow borrowDTO = new Borrow();
                borrowDTO.setId(rs.getInt("id"));
                borrowDTO.setBookId(rs.getInt("book_id"));
                borrowDTO.setStudentId(rs.getInt("student_id"));
                // JAVA DTO 에서 데이터 타입은 LocalDate 이다.
                // 하지만 JDBC API 에서 아직은 LocalDate 타입을 지원하지 않는다.
                // JDBC API 제공하는 날짜 데이터 타입은 Date 이다.
                // rs.getLocalDate <-- 아직은 지원하지 않음
                // .toLocalDate() <-- LocalDate 로 형 변환
                borrowDTO.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                borrowList.add(borrowDTO);
            }
        }
        return borrowList;
    }

    // 도서 반납을 처리하는 기능 추가
    // 1. borrows 테이블에 책 정보 조회 (check) -- SELECT (복합 조건)
    // 2. borrows 테이블에 return_date 수정 -- UPDATE
    // 3. books 테이블에 available 수정 -- UPDATE

    // 1. (book_id) 반납하려는 특정 책을 찾아야 된다.
    // 2. (student_id) 책을 빌린 학생을 찾기 위함이다.
    // 2.1 다른 책을 빌린 이력이 있을 수 있기 때문
    // 3. 아직 반납되지 않은 대출 기록만 찾아야 한다.
    // 3.1 같은 학생이 예전에 여러 번 빌린 이력이 있을 수 있기 때문

    public void returnBook(int bookId, int studentPK) throws SQLException {
        Connection conn = null;
        try {
            conn = DataBaseUtil.getConnection();
            // 트랜잭션 시작
            conn.setAutoCommit(false);

            // 이 쿼리의 결과집합에서 필요한 것은 borrows 의 pk(id) 값 이다.
            int borrowId = 0;
            String checkSql = "SELECT id FROM borrows " +
                    "WHERE book_id = ? AND student_id = ? AND return_date IS NULL ";

            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);
                checkPstmt.setInt(2, studentPK);
                ResultSet rs = checkPstmt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("해당 대출 기록이 존재하지 않거나 이미 반납되었습니다.");
                }
                borrowId = rs.getInt("id");
            }
            String updateBorrowSql = "UPDATE borrows SET return_date = ? WHERE id = ? ";
            String updateBookSql = "UPDATE books SET available = true WHERE id = ? ";

            try (PreparedStatement borrowPstmt = conn.prepareStatement(updateBorrowSql);
                 PreparedStatement bookPstmt = conn.prepareStatement(updateBookSql)) {
                // borrows 설정
                borrowPstmt.setInt(1, borrowId);
                borrowPstmt.executeUpdate(); // 쿼리 실행
                // books 설정
                bookPstmt.setInt(1, bookId);
                bookPstmt.executeUpdate(); // 쿼리 실행
            }
            conn.commit(); // 트랜잭션 처리 완료
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // 오류 발생 시 롤백 처리
            }
            System.err.println("rollback 처리를 하였습니다.");
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // 다시 오토커밋 설정
                conn.close(); // 자원을 닫아야 메모리 누수가 발생하지 않는다.
            }
        }
    }


    // 메인 함수
    public static void main(String[] args) {
        // 대출 실행 테스트
        BorrowDAO borrowDAO = new BorrowDAO();
        try {
            borrowDAO.borrowBook(1, 3);
            // 현재 대출중인 책 목록 조회

            for (int i = 0; i < borrowDAO.getBorrowedBooks().size(); i++) {
                System.out.println(borrowDAO.getBorrowedBooks().get(i));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    } // end of main

}
