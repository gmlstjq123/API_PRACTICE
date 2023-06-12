package com.example.umc4_heron_template_jpa.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("select m from Member m where m.id = :id")
    Member findMemberById(@Param("id") Long id);

    @Query("select count(m) from Member m where m.email = :email")
    Integer findByEmailCount(@Param("email") String email);

    @Query("select m from Member m where m.email = :email")
    Member findMemberByEmail(@Param("email") String email);

    @Query("select m from Member m")
    List<Member> findMembers();

    @Query("select m from Member m where m.nickName = :nickName")
    List<Member> findMemberByNickName(@Param("nickName") String nickName);

    @Modifying
    @Query("delete from Member m where m.email = :email")
    void deleteMember(@Param("email") String email);
}
