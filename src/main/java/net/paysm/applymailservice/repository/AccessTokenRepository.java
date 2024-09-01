package net.paysm.applymailservice.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import net.paysm.applymailservice.entity.AccessToken;
import net.paysm.applymailservice.entity.Merchant;

public interface AccessTokenRepository extends CrudRepository<AccessToken, Long>{

    Optional<AccessToken> findByMerchant(Merchant merchant);

    Optional<AccessToken> findByAccessTokenAndExpireDateAfter(String accessToken, LocalDateTime expireDate);

    Optional<AccessToken> findByAccessToken(String accessToken);
}
