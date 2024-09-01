package net.paysm.applymailservice.controller;


import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.paysm.applymailservice.entity.Transfer;
import net.paysm.applymailservice.repository.TransferRepository;

@CrossOrigin(origins = "*")
@Controller
public class PaymentController {

    @Autowired
    private TransferRepository transferRepository;

    @Value("${front.domain}")
    private String frontDomain;


    @Transactional
    @PostMapping("/payment")
    public String payment(PaymentVo paymentVo) throws IOException, InterruptedException {


        StringBuilder formParam = new StringBuilder();
        formParam.append("tid=" + URLEncoder.encode(paymentVo.getTid(), StandardCharsets.UTF_8) + "&");
        formParam.append("ediDate=" + URLEncoder.encode(paymentVo.getEdiDate(), StandardCharsets.UTF_8) + "&");
        formParam.append("mid=" + URLEncoder.encode(paymentVo.getMid(), StandardCharsets.UTF_8) + "&");
        formParam.append("goodsAmt=" + URLEncoder.encode(paymentVo.getGoodsAmt().toString(), StandardCharsets.UTF_8) + "&");
        formParam.append("charSet=" + URLEncoder.encode(paymentVo.getCharSet(), StandardCharsets.UTF_8) + "&");
        formParam.append("encData=" + URLEncoder.encode(paymentVo.getMbsReserved(), StandardCharsets.UTF_8) + "&");
        formParam.append("signData=" + URLEncoder.encode(paymentVo.getSignData(), StandardCharsets.UTF_8));


        Transfer transfer = Transfer.builder()
            .tid(paymentVo.getTid())
            .mid(paymentVo.getMid())
            .goodsAmt(paymentVo.getGoodsAmt().toString())
            .authDate(LocalDateTime.now())
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.skyclassism.com/payment.do"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formParam.toString()))
            .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});


        String resultCode = map.get("resultCd");
        String resultMsg = map.get("resultMsg");
        String amt = map.get("amt");
        String returnUrl = frontDomain + "/payment-result?resultCode=" + URLEncoder.encode(resultCode, "UTF-8") 
            + "&resultMsg=" + URLEncoder.encode(resultMsg, "UTF-8")
            + "&amt=" + URLEncoder.encode(amt, "UTF-8");
        transfer.setResultCode(resultCode);
        transfer.setResultMessage(map.get("resultMsg"));

        if (!"9999".equals(resultCode)) {
            String goodsName = map.get("goodsName");
            String payMethod = map.get("payMethod");
            String ediDate = map.get("ediDate");
            String tid = map.get("tid");

            transfer.setGoodsName(goodsName);
            transfer.setPayMethod(payMethod);
            transfer.setEdiDate(ediDate);

            returnUrl = returnUrl + "&goodsName=" + URLEncoder.encode(goodsName, "UTF-8") 
                + "&payMethod=" + URLEncoder.encode(payMethod, "UTF-8") 
                + "&ediDate=" + URLEncoder.encode(ediDate, "UTF-8")
                + "&tid=" + URLEncoder.encode(tid, "UTF-8");

            if ("0000".equals(resultCode)) {

                LocalDateTime applyDate = LocalDateTime.now();
                String cardNo = map.get("cardNo").substring(0, 8) + "**";
                String fnNm = map.get("fnNm");
                String authType = map.get("authType");

                transfer.setApplyDate(applyDate);
                transfer.setCardNo(cardNo);
                transfer.setFnNm(fnNm);
                transfer.setAuthType(authType);
                returnUrl = returnUrl + "&cardNo=" + URLEncoder.encode(cardNo, "UTF-8")  + "&fnNm=" + URLEncoder.encode(fnNm, "UTF-8");
            } 
        }

    
        transferRepository.save(transfer);
        return "redirect:" + returnUrl;
    }


    @Transactional
    @PostMapping("/payment.cancel")
    public ResponseEntity<Map<String, String>> cancelPayment(@RequestBody Map<String, String> params) throws IOException, InterruptedException{
        Map<String, String> resultMap = new HashMap<>();

        String tid = params.get("tid");
        String canAmt = params.get("canAmt");
        String canMsg = params.get("canMsg");
        String encData = params.get("encData");
        String ediDate = params.get("ediDate");

        if (isEmptyString(tid) || isEmptyString(canAmt) || isEmptyString(canMsg) || isEmptyString(encData) || isEmptyString(ediDate) ) {
            resultMap.put("code", "400");
            resultMap.put("message", "요청이 제대로 입력되지 않았습니다.");
            return ResponseEntity.badRequest().body(resultMap);
        }


        StringBuilder formParam = new StringBuilder();
        formParam.append("tid=" + URLEncoder.encode(tid, StandardCharsets.UTF_8) + "&");
        formParam.append("canAmt=" + URLEncoder.encode(canAmt, StandardCharsets.UTF_8) + "&");
        formParam.append("canMsg=" + URLEncoder.encode(canMsg, StandardCharsets.UTF_8) + "&");
        formParam.append("partCanFlg=" + URLEncoder.encode("0", StandardCharsets.UTF_8) + "&");
        formParam.append("encData=" + URLEncoder.encode(encData, StandardCharsets.UTF_8) + "&");
        formParam.append("ediDate=" + URLEncoder.encode(ediDate, StandardCharsets.UTF_8));


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.skyclassism.com/payment.cancel"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formParam.toString()))
            .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});
        String resultCd = map.get("resultCd");
        System.out.println("resultCd : " + resultCd + " resultMsg : " + map.get("resultMsg"));
        System.out.println("result: " + response.body());
        if (!"0000".equals(resultCd)) {
            resultMap.put("code", "201");
            resultMap.put("message", "잘못된 데이터로 인한 오류이거나 이미 취소 처리된 건입니다.");
            return ResponseEntity.ok(resultMap);
        }

        Transfer transfer = transferRepository.findByTid(tid);
        transfer.setCancelDate(LocalDateTime.now());

        resultMap.put("code", "200");
        resultMap.put("message", "취소가 완료되었습니다.");
        return ResponseEntity.ok(resultMap);
    }

    boolean isEmptyString(String string) {
        return string == null || string.isEmpty();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PaymentVo {
        private final String tid;
        private final String payMethod;
        private final String ediDate;
        private final String goodsAmt;
        private final String mid;
        private final String mbsReserved;
        private final String charSet;
        private final String returnUrl;
        private final String signData;
        private final String resultCode;
        private final String resultMsg;
    }

}
