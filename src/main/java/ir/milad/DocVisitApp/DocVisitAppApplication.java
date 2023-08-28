package ir.milad.DocVisitApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DocVisitAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(DocVisitAppApplication.class, args);
	}
}
