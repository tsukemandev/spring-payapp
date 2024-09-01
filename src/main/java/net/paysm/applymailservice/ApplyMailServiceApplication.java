package net.paysm.applymailservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import net.paysm.applymailservice.entity.Merchant;
import net.paysm.applymailservice.repository.MerchantRepository;
import net.paysm.applymailservice.util.EncryptUtil;

@EnableJpaAuditing
@SpringBootApplication
public class ApplyMailServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApplyMailServiceApplication.class, args);
	}



	@Bean
	@Profile("dev")
	CommandLineRunner initH2Db(MerchantRepository merchantRepository) {
		return (args) -> {
			System.out.print("dev 실행!!");
			merchantRepository.save(Merchant.builder().mid("testMid1").password(EncryptUtil.sha256("password1")).mkey("dqwnjre21j12n2kj...").build());
			merchantRepository.save(Merchant.builder().mid("testMid2").password(EncryptUtil.sha256("password2")).mkey("gmnyro45jn21243gtrj...").build());
		};
	}
}
