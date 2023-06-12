package com.example.umc4_heron_template_jpa.board;

import com.example.umc4_heron_template_jpa.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("select b from Board b where b.boardId = :boardId")
    Board findBoardById(@Param("boardId") Long boardId);

    @Query("select b from Board b where b.title = :title")
    List<Board> findBoardByTitle(@Param("title") String title);

    @Query("select b from Board b where b.title = :title and b.member.id =:memberId")
    List<Board> findBoardByTitle(@Param("title") String title, @Param("memberId") Long memberId);

    @Query("select b from Board b where b.member.id = :id")
    List<Board> findBoardByMemberId(@Param("id") Long id);

    @Modifying
    @Query("delete from Board b where b.title = :title")
    void deleteBoard(@Param("title") String title);
}
