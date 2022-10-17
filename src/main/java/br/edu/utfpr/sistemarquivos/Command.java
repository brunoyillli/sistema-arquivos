package br.edu.utfpr.sistemarquivos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public enum Command {

	LIST() {
		@Override
		boolean accept(String command) {
			final var commands = command.split(" ");
			return commands.length > 0 && commands[0].startsWith("LIST") || commands[0].startsWith("list");
		}

		@Override
		Path execute(Path path) throws IOException {
			List<File> files = Files.list(path).map(Path::toFile).collect(Collectors.toList());
			files.forEach(System.out::println);

			return path;
		}
	},
	SHOW() {
		private String[] parameters = new String[] {};

		@Override
		void setParameters(String[] parameters) {
			this.parameters = parameters;
		}

		@Override
		boolean accept(String command) {
			final var commands = command.split(" ");
			return commands.length > 0 && commands[0].startsWith("SHOW") || commands[0].startsWith("show");
		}

		@Override
		Path execute(Path path) {
			String novoPath = novoPathPorParametros(path, parameters);
			Path pathArquivo = Paths.get(novoPath);
			boolean diretorioValido = Files.exists(pathArquivo);
			if (!Files.isDirectory(pathArquivo) && diretorioValido) {
				if (pathArquivo.toString().toLowerCase().endsWith(".txt")) {
					FileReader fileReader = new FileReader();
					fileReader.read(pathArquivo);
				} else {
					System.err.println("Extensao nao suportada.");
				}
			} else {
				System.err.println("Este comando deve ser usado apenas em arquivos.");
			}
			return path;
		}
	},
	BACK() {
		@Override
		boolean accept(String command) {
			final var commands = command.split(" ");
			return commands.length > 0 && commands[0].startsWith("BACK") || commands[0].startsWith("back");
		}

		@Override
		Path execute(Path path) {
			if (path.compareTo(Paths.get(Application.ROOT)) == 0) {
				System.err.println("Voce ja esta no diretorio raiz: " + path.toAbsolutePath().toString());
				return path;
			}
			return path.getParent();
		}
	},
	OPEN() {
		private String[] parameters = new String[] {};

		@Override
		void setParameters(String[] parameters) {
			this.parameters = parameters;
		}

		@Override
		boolean accept(String command) {
			final var commands = command.split(" ");
			return commands.length > 0 && commands[0].startsWith("OPEN") || commands[0].startsWith("open");
		}

		@Override
		Path execute(Path path) {
			String novoPath = novoPathPorParametros(path, parameters);
			Path pathTemp = Paths.get(novoPath);
			boolean diretorioValido = Files.exists(pathTemp);
			if (diretorioValido && Files.isDirectory(pathTemp)) {
				path = Paths.get(novoPath);
			} else {
				System.err.println("Diretorio invalido");
			}

			return path;
		}
	},
	DETAIL() {
		private String[] parameters = new String[] {};

		@Override
		void setParameters(String[] parameters) {
			this.parameters = parameters;
		}

		@Override
		boolean accept(String command) {
			final var commands = command.split(" ");
			return commands.length > 0 && commands[0].startsWith("DETAIL") || commands[0].startsWith("detail");
		}

		@Override
		Path execute(Path path) {
			String novoPath = novoPathPorParametros(path, parameters);
			Path pathArquivo = Paths.get(novoPath);
			boolean diretorioValido = Files.exists(pathArquivo);
			if (diretorioValido) {
				try {
					BasicFileAttributeView basicview = Files.getFileAttributeView(pathArquivo,
							BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
					BasicFileAttributes atributosFile;

					atributosFile = basicview.readAttributes();
					LocalDateTime timeModificacao = LocalDateTime
							.ofInstant(atributosFile.lastModifiedTime().toInstant(), ZoneId.systemDefault());
					LocalDateTime timeCriacao = LocalDateTime.ofInstant(atributosFile.creationTime().toInstant(),
							ZoneId.systemDefault());
					LocalDateTime timeAcesso = LocalDateTime.ofInstant(atributosFile.lastAccessTime().toInstant(),
							ZoneId.systemDefault());
					Long tamanhoArquivo = atributosFile.size();
					Boolean ehDiretorio = atributosFile.isDirectory();
					System.out.println("Eh um diretorio ? " + ehDiretorio);
					System.out.println("Tamanho do arquivo: " + tamanhoArquivo);
					System.out.println("Data de modificacao: " + timeModificacao);
					System.out.println("Data de criacao: " + timeCriacao);
					System.out.println("Data de ultimo acesso: " + timeAcesso);
				} catch (IOException e) {
					System.err.println("Erro ao detalhar arquivo ou diretorio: " + e.getMessage());
				}
			} else {
				System.err.println("Este comando deve ser usado apenas em arquivos.");
			}
			return path;
		}
	},
	EXIT() {
		@Override
		boolean accept(String command) {
			final var commands = command.split(" ");
			return commands.length > 0 && commands[0].startsWith("EXIT") || commands[0].startsWith("exit");
		}

		@Override
		Path execute(Path path) {
			System.out.print("Saindo...");
			return path;
		}

		@Override
		boolean shouldStop() {
			return true;
		}
	};

	abstract Path execute(Path path) throws IOException;

	abstract boolean accept(String command);

	void setParameters(String[] parameters) {
	}

	boolean shouldStop() {
		return false;
	}

	public static Command parseCommand(String commandToParse) {

		if (commandToParse.isBlank()) {
			throw new UnsupportedOperationException("Type something...");
		}

		final var possibleCommands = values();

		for (Command possibleCommand : possibleCommands) {
			if (possibleCommand.accept(commandToParse)) {
				possibleCommand.setParameters(commandToParse.split(" "));
				return possibleCommand;
			}
		}

		throw new UnsupportedOperationException("Can't parse command [%s]".formatted(commandToParse));
	}

	private static String novoPathPorParametros(Path path, String[] parameters) {
		String novoPath = path.toAbsolutePath().toString();
		for (int i = 1; i < parameters.length; i++) {
			novoPath += File.separator + parameters[i];
		}
		return novoPath;
	}
}
