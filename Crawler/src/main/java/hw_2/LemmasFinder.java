package hw_2;

import com.github.demidko.aot.WordformMeaning;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.demidko.aot.WordformMeaning.lookupForMeanings;

public class LemmasFinder {
    public static void main(String[] args) {
        Set<String> tokens = new HashSet<>();
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        // Тут путь до директории с папкой in/out
        String TASK_PATH = System.getProperty("user.dir") + File.separator + "Crawler/src/main/java/hw_2/";
        String inputPath = TASK_PATH + "in";
        String outputPath = TASK_PATH + "out";
        // Служебные части речи
        Set<String> stopWords = new HashSet<>(
                Arrays.asList(
                        "в", "без", "до", "из", "к", "на", "по", "о", "от", "перед",
                        "при", "через", "с", "со", "у", "и", "нет", "за", "над", "для", "об",
                        "под", "про", "когда", "пока", "едва", "лишь", "только", "потому", "что",
                        "так", "как", "оттого", "что", "ибо", "чтобы", "чтоб", "для", "того",
                        "чтобы", "с тем чтобы", "если", "раз", "бы", "а", "но", "и"
                )
        );
        try {
            List<Path> filePaths = Files.walk(Paths.get(inputPath))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            for (Path path : filePaths) {
                File file = new File(path.toUri());

                System.out.println("Parsing: " + file.getAbsolutePath());

                // Вытягиваем из каждого файла html
                Document doc = Jsoup.parse(file, StandardCharsets.UTF_8.toString());
                List<String> cyrillicWords = new ArrayList<>(Arrays.asList(tokenizer.tokenize(doc.text().toLowerCase(Locale.ROOT))));
                cyrillicWords.removeIf(w -> w.matches("[^\\u0400-\\u04FF]+$") || w.length() > 15 || w.length() <= 2);
                cyrillicWords.removeAll(stopWords);
                //Токенизируем html
                tokens.addAll(cyrillicWords);

                System.out.println("Tokens list size: " + tokens.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Пишем в файл tokens.txt
        try (FileWriter writer = new FileWriter(outputPath + "/tokens.txt")) {
            for (String token : tokens) {
                writer.write(token + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> lemmas = getLemmas(tokens);

        // Пишем в файл lemmas.txt
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(outputPath + "/lemmas.txt"))) {
            System.out.println("Writing result to tokens.txt...");
            for (Map.Entry<String, String> entry : lemmas.entrySet()) {
                bf.write(entry.getKey() + ":" + entry.getValue());
                bf.newLine();
            }
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> getLemmas(Set<String> tokens) {
        Map<String, String> result = new HashMap<>();
        List<String> notFoundWords = new ArrayList<>();
        System.out.println("Writing result to lemmas.txt...");
        try {
            for (String token : tokens) {
                List<WordformMeaning> meanings = lookupForMeanings(token);
                if (meanings.size() != 0) {
                    String key = meanings.get(0).getLemma().toString();
                    if (result.containsKey(key)) {
                        result.put(key, result.get(key).concat(" " + token));
                    } else {
                        result.put(key, token);
                    }
                } else {
                    notFoundWords.add(token);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.put("Слова для которых лемма не найдена", notFoundWords.toString());
        return result;
    }
}
