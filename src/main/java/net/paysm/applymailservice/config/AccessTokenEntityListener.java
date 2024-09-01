package net.paysm.applymailservice.config;

import java.time.LocalDateTime;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import net.paysm.applymailservice.entity.AccessToken;

public class AccessTokenEntityListener {

    @PrePersist
    public void onPrePersist(AccessToken entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedTime(now);
        entity.setModifiedTime(now);
    }

    @PreUpdate
    public void onPreUpdate(AccessToken entity) {
        entity.setModifiedTime(LocalDateTime.now());
    }
}
