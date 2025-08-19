package com.OneChallenge.Literatura;

import com.OneChallenge.Literatura.Actions.Principal;
import com.OneChallenge.Literatura.repository.AutorRepository;
import com.OneChallenge.Literatura.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteraturaApplication implements CommandLineRunner {
    @Autowired
    private LibroRepository bookRepository;
    @Autowired
    private AutorRepository autorRepository;

	public static void main(String[] args) {
		SpringApplication.run(LiteraturaApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        Principal principal = new Principal(bookRepository, autorRepository);
        principal.loadMenu();
    }
}
