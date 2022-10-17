package br.edu.utfpr.sistemarquivos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileReader {

	public void read(Path path) {
		try (Stream<String> lines = Files.lines(path)) {
			lines.forEach(System.out::println);
		} catch (NoSuchFileException e) {
			System.err.println("Arquivo nao encontrado: " + e.getMessage());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
