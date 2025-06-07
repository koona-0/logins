package site.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // JPA Auditing 활성화 (createdAt, updatedAt 자동 설정)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


}
