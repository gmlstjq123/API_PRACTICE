package com.example.umc4_heron_template_jpa.utils.dto;

import lombok.Data;

@Data
public class GetS3Res {
    private String imgUrl;
    private String fileName;

    public GetS3Res(String imgUrl, String fileName) {
        this.imgUrl = imgUrl;
        this.fileName = fileName;
    }
}
