package net.paysm.applymailservice.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    private String tid;
    private String mid;
    private String goodsName;
    private String goodsAmt;
    private String fnNm;
    private String cardNo;
    private String payMethod;
    private String ediDate;
    private String resultCode;
    private String resultMessage;
    private String authType;
    private LocalDateTime authDate;
    private LocalDateTime applyDate;
    private LocalDateTime cancelDate;

}
