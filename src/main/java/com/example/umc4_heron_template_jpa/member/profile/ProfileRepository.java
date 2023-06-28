package com.example.umc4_heron_template_jpa.member.profile;

import com.example.umc4_heron_template_jpa.board.photo.PostPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Query("select p from Profile p where p.member.id = :memberId")
    Optional<Profile> findProfileById(@Param("memberId") Long memberId);

    @Modifying
    @Query("delete from Profile p where p.member.id = :memberId")
    void deleteProfileById(@Param("memberId") Long memberId);
}
