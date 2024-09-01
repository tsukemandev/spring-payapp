package net.paysm.applymailservice.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.paysm.applymailservice.entity.AccessToken;
import net.paysm.applymailservice.entity.Merchant;
import net.paysm.applymailservice.entity.Transfer;
import net.paysm.applymailservice.repository.AccessTokenRepository;
import net.paysm.applymailservice.repository.TransferRepository;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/app")
public class TransferController {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @GetMapping("/transfers")
    public ResponseEntity<List<Transfer>> getTransferList(@RequestHeader("Authorization") String accessToken) {

        List<Transfer> resultList = new ArrayList<>();

        if (accessToken == null) {
            return ResponseEntity.badRequest().body(resultList);
        }

        accessToken = accessToken.replace("Bearer ", "");

        Optional<AccessToken> aOptional = accessTokenRepository.findByAccessTokenAndExpireDateAfter(accessToken, LocalDateTime.now());
        if (!aOptional.isPresent()) {
            return ResponseEntity.badRequest().body(resultList);
        }

        Merchant merchant = aOptional.get().getMerchant();

        resultList = transferRepository.findAllByMidOrMidOrderByAuthDateDesc(merchant.getMid(), merchant.getManualMid(), PageRequest.of(0, 100));

        return ResponseEntity.ok().body(resultList);
    }
}
