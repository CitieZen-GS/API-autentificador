package on.fiap.com.br.autentificador;

import org.springframework.boot.SpringApplication;

public class TestAutentificadorApplication {

	public static void main(String[] args) {
		SpringApplication.from(AutentificadorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
