package com.example.umc4_heron_template_jpa.member.profile;

import com.example.umc4_heron_template_jpa.board.photo.dto.GetS3Res;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.utils.S3Service;
import com.example.umc4_heron_template_jpa.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final S3Service s3Service;
    private final UtilService utilService;

    @Transactional
    public void saveProfile(GetS3Res getS3Res, Member member){
        Profile profile;
        if(getS3Res.getImgUrl() == null){
            profile = Profile.builder()
                    .profileUrl(null)
                    .profileFileName(null)
                    .member(member)
                    .build();
        }
        else {
           profile = Profile.builder()
                    .profileUrl(getS3Res.getImgUrl())
                    .profileFileName(getS3Res.getFileName())
                    .member(member)
                    .build();
        }
        profileRepository.save(profile);
    }

    @Transactional
    public Profile findProfileById(Long memberId) {
        return profileRepository.findProfileById(memberId).orElse(null);
    }

    @Transactional
    public void deleteProfile(Profile profile) {
        s3Service.deleteFile(profile.getProfileFileName());
    }

    @Transactional
    public void deleteProfileById(Long memberId) {
        profileRepository.deleteProfileById(memberId);
    }
}
