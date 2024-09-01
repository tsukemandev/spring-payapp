package net.paysm.applymailservice.config;

import java.time.LocalDateTime;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import net.paysm.applymailservice.entity.Merchant;

public class MerchantEntityListener {

    @PrePersist
    public void onPrePersist(Merchant entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedTime(now);
        entity.setModifiedTime(now);
    }

    @PreUpdate
    public void onPreUpdate(Merchant entity) {
        entity.setModifiedTime(LocalDateTime.now());
    }

}
