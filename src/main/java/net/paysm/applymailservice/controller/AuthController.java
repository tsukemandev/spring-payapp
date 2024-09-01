package net.paysm.applymailservice.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.paysm.applymailservice.entity.AccessToken;
import net.paysm.applymailservice.entity.Merchant;
import net.paysm.applymailservice.repository.AccessTokenRepository;
import net.paysm.applymailservice.repository.MerchantRepository;
import net.paysm.applymailservice.util.EncryptUtil;
import net.paysm.applymailservice.util.RandomUtil;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/app")
public class AuthController {


    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;
    

    @Transactional
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> params) {

        String id = params.get("id");  //앱 로그인 아이디
        String password = params.get("password");  //앱 로그인 패스워드
        Map<String, String> resultMap = new HashMap<>();

        if (id == null || id == "" || password == null || password == "") {
            resultMap.put("code", "400");  //파라미터 null 유무 검사
            resultMap.put("message", "아이디나 패스워드를 입력해주세요.");
            return ResponseEntity.badRequest().body(resultMap);
        }

        
        Optional<Merchant> meOptional = merchantRepository.findByIdAndPassword(id, EncryptUtil.sha256(password));
        if (meOptional.isPresent()) {
            Optional<AccessToken> accessTokenOp = accessTokenRepository.findByMerchant(meOptional.get());

            String accessToken = EncryptUtil.sha256(RandomUtil.generate());
            if (accessTokenOp.isPresent()) {
                accessTokenOp.get().setAccessToken(accessToken);
                accessTokenOp.get().setExpireDate(LocalDateTime.now().plusHours(3));
            } else {
                accessTokenRepository.save(AccessToken.builder()
                    .accessToken(accessToken)
                    .merchant(meOptional.get())
                    .expireDate(LocalDateTime.now().plusHours(3)) //기본 3시간 유효시간 설정
                    .build());
            }


            resultMap.put("mkey", meOptional.get().getMkey());
            resultMap.put("accessToken", accessToken);  
            resultMap.put("code", "200");
            return ResponseEntity.ok().body(resultMap);
        }

        resultMap.put("code", "401");
        resultMap.put("message", "아이디나 패스워드가 일치하지 않습니다.");
        return ResponseEntity.badRequest().body(resultMap);
    }

    @Transactional
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@RequestBody Map<String, String> params) {

        Map<String, String> resultMap = new HashMap<>();
        String id = params.get("id");
        String password = params.get("password");
        String name = params.get("name");
        String tel = params.get("tel");

        resultMap.put("code", "400");

        if (id == null || id.isEmpty() ||
            password == null || password.isEmpty() ||
            name == null || name.isEmpty() ||
            tel == null || tel.isEmpty()) {
    
            System.out.println("필수 입력사항 모두 입력해줘요");
            resultMap.put("message", "필수입력사항을 모두 입력해주세요.");
            return ResponseEntity.badRequest().body(resultMap);
        }   

        // 정규식 패턴: 영숫자만 허용하며, 최소 4자 이상
        Pattern idPattern = Pattern.compile("^[a-zA-Z0-9]{4,}$");
        Matcher idMatcher = idPattern.matcher(id);

        if (!idMatcher.matches()) {
            resultMap.put("message", "아이디는 영숫자만 허용하며 최소 4자이상입니다.");
            return ResponseEntity.badRequest().body(resultMap);
        }
        

        // 정규식 패턴: 영숫자와 특수 문자를 포함, 최소 6자 이상
        Pattern passwordPattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*\\W).{6,}$");
        Matcher passwordMatcher = passwordPattern.matcher(password);

        if (!passwordMatcher.matches()) {
            resultMap.put("message", "비밀번호는 영숫자와 특수 문자를 포함, 최소 6자 이상입니다.");
            return ResponseEntity.badRequest().body(resultMap);
        }
        
        if (merchantRepository.existsById(id)) {
            resultMap.put("message", "이미 존재하는 아이디입니다.");
            return ResponseEntity.badRequest().body(resultMap);
        }

        merchantRepository.save(Merchant.builder()
            .id(id)
            .password(EncryptUtil.sha256(password))
            .tel(tel)
            .name(name)
            .build());

        resultMap.put("code", "200");
        resultMap.put("message", "회원가입이 완료되었습니다.");

        return ResponseEntity.ok().body(resultMap);
    }

    @Transactional
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> params, @RequestHeader("Authorization") String accessToken) {
        Map<String, Object> resultMap = new HashMap<>();
        String previousPassword = params.get("previousPassword");
        String newPassword = params.get("newPassword");

        if (previousPassword == null || newPassword == null) {
            resultMap.put("code", "400");
            resultMap.put("message", "비밀번호를 제대로 입력해주세요.");

            return ResponseEntity.badRequest().body(resultMap);
        }

        if (accessToken == null) {
            resultMap.put("code", "400");
            resultMap.put("message", "잘못된 접근입니다.");

            return ResponseEntity.badRequest().body(resultMap);
        }

        accessToken = accessToken.replace("Bearer ", "");

        Optional<AccessToken> aOptional = accessTokenRepository.findByAccessTokenAndExpireDateAfter(accessToken, LocalDateTime.now());
        if (!aOptional.isPresent()) {
            resultMap.put("code", "401");
            resultMap.put("message", "다시 로그인 해주세요.");

            return ResponseEntity.badRequest().body(resultMap);
        }

        Merchant merchant = aOptional.get().getMerchant();
        if (!merchant.getPassword().equals(EncryptUtil.sha256(previousPassword))) {
            resultMap.put("code", "400");
            resultMap.put("message", "잘못된 비밀번호 입니다.");

            return ResponseEntity.badRequest().body(resultMap);
        }


        // 정규식 패턴: 영숫자와 특수 문자를 포함, 최소 6자 이상
        Pattern passwordPattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*\\W).{6,}$");
        Matcher passwordMatcher = passwordPattern.matcher(newPassword);

        if (!passwordMatcher.matches()) {
            resultMap.put("code", "400");
            resultMap.put("message", "비밀번호는 영숫자와 특수 문자를 포함, 최소 6자 이상입니다.");
            return ResponseEntity.badRequest().body(resultMap);
        }

        merchant.setPassword(EncryptUtil.sha256(newPassword));

        resultMap.put("code", "200");
        resultMap.put("message", "비밀번호를 성공적으로 변경하였습니다.");
        return ResponseEntity.badRequest().body(resultMap);
    }

    @Transactional
    @PutMapping("/business")
    public ResponseEntity<Map<String, Object>> updateMidAndMkey(@RequestBody Map<String, String> params, @RequestHeader("Authorization") String accessToken) {

        Map<String, Object> resultMap = new HashMap<>();
        String mid = params.get("mid");
        String mKey = params.get("mKey");
        String isAuthPay = params.get("isAuthPay"); 

        if (mid == null || mKey == null) {
            resultMap.put("code", "400");
            resultMap.put("message", "잘못된 비밀번호 입니다.");

            return ResponseEntity.badRequest().body(resultMap);
        }

        if (accessToken == null) {
            resultMap.put("code", "400");
            resultMap.put("message", "잘못된 접근입니다.");

            return ResponseEntity.badRequest().body(resultMap);
        }

        accessToken = accessToken.replace("Bearer ", "");
        Optional<AccessToken> aOptional = accessTokenRepository.findByAccessTokenAndExpireDateAfter(accessToken, LocalDateTime.now());
        if (!aOptional.isPresent()) {
            resultMap.put("code", "401");
            resultMap.put("message", "다시 로그인 해주세요.");

            return ResponseEntity.badRequest().body(resultMap);
        }

        Merchant merchant = aOptional.get().getMerchant();
        if ("true".equals(isAuthPay)) {
            merchant.setMid(mid);
            merchant.setMkey(mKey);
        } else {
            merchant.setManualMid(mid);
            merchant.setManualMkey(mKey);
        }

        resultMap.put("code", "200");
        resultMap.put("message", "사업자 정보가 변경되었습니다.");
        return ResponseEntity.ok().body(resultMap);
    }

    @GetMapping("/business")
    public ResponseEntity<Map<String, Object>> getMidAndMKey(@RequestHeader("Authorization") String accessToken, @RequestParam String isAuthPay) {
        Map<String, Object> resultMap = new HashMap<>();

        if (accessToken == null) {
            resultMap.put("code", "400");
            resultMap.put("message", "잘못된 접근입니다.");

            return ResponseEntity.badRequest().body(resultMap);
        }

        accessToken = accessToken.replace("Bearer ", "");

        Optional<AccessToken> aOptional = accessTokenRepository.findByAccessTokenAndExpireDateAfter(accessToken, LocalDateTime.now());
        if (!aOptional.isPresent()) {
            resultMap.put("code", "401");
            resultMap.put("message", "다시 로그인 해주세요.");

            return ResponseEntity.badRequest().body(resultMap);
        }

        Merchant merchant = aOptional.get().getMerchant();
        resultMap.put("code", "200");

        if ("true".equals(isAuthPay)) {
            resultMap.put("mid", merchant.getMid());
            resultMap.put("mKey", merchant.getMkey());
        } else {
            resultMap.put("mid", merchant.getManualMid());
            resultMap.put("mKey", merchant.getManualMkey());
        }

        return ResponseEntity.ok().body(resultMap);
    }


    
    @PostMapping("/verify/access-token")
    public ResponseEntity<Map<String, Object>> verifyAccessToken(@RequestBody Map<String, String> params) {

        Map<String, Object> resultMap = new HashMap<>();
        String accessToken = params.get("accessToken");

        Optional<AccessToken> accessTokenEntity = accessTokenRepository.findByAccessTokenAndExpireDateAfter(accessToken, LocalDateTime.now());
        if (accessTokenEntity.isPresent()) {
            resultMap.put("code", "200");
            resultMap.put("message", "올바른 토큰입니다.");
            return ResponseEntity.ok().body(resultMap);
        }

        resultMap.put("code", "401");
        resultMap.put("message", "만료된 토큰입니다.");
        return ResponseEntity.badRequest().body(resultMap);
    }

}
