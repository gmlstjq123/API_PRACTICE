package com.example.umc4_heron_template_jpa.board;

import com.example.umc4_heron_template_jpa.board.comment.Comment;
import com.example.umc4_heron_template_jpa.board.photo.PostPhoto;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.utils.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Board extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    // 멤버와 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    // 댓글과 관계 매핑
    @OneToMany(mappedBy = "board", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();
    // 게시사진과 관계매핑
    @OneToMany(mappedBy = "board", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<PostPhoto> photoList = new ArrayList<>();

    public void updateBoard(String title, String content){
        this.title = title;
        this.content = content;
    }
    // 댓글과 관계 매핑
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public void addPhotoList(PostPhoto postPhoto){
        photoList.add(postPhoto);
        postPhoto.createBoard(this);
    }
}
