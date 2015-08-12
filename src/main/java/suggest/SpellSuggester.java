package suggest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterators;
import com.google.common.io.LineReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
public class SpellSuggester {

    public static void main(String[] args) throws IOException, ParseException {
        Configuration config = new Configuration();
        Options options = new Options();

        Option help = Option.builder("h")
                .longOpt("help")
                .desc("печатает справочную информацию и завершает работу")
                .build();
        options.addOption(help);

        Option configuration = Option.builder("c")
                .required()
                .longOpt("config")
                .hasArg()
                .desc("задает конфигурацию")
                .build();
        options.addOption(configuration);


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Suggester", options);
        } else if (cmd.hasOption("c")) {
            String path = (String) cmd.getParsedOptionValue("c");
            Properties properties = new Properties();
            properties.load(new FileInputStream(path));
            config.setCorrectionsPath(properties.getProperty("suggester.corrections.path", ""));
            config.setInputCsvPath(properties.getProperty("suggester.csv.input.path", ""));
            config.setInputColumn(Integer.valueOf(properties.getProperty("suggester.csv.input.column", "0")));
            config.setDictionaryPath(properties.getProperty("suggester.dictionary.path", ""));
            config.setOutputCsvPath(properties.getProperty("suggester.csv.output.path", ""));
            config.setCsvDelimiter(properties.getProperty("csv.delimiter", ";").charAt(0));
            config.setCsvEncoding(properties.getProperty("csv.encoding", "UTF-8"));
        }


//        Set<String> citiesFromVk = extractAllCities();
//        Set<String> citiesFromVk = extractCities(1146712, 1050307, 1082931, 1109098, 1130218, 1004565, 1148549, 1154131, 1036606, 1094197, 1112201, 1052052, 1113937, 1000236, 1084332, 1157218, 1159710);
//        Set<String> citiesFromFile = extractUsersData(ImmutableSet.of("Ижевск", "Сарапул", "Глазов", "Воткинск", "Можга", "Чайковский"));
//        HashMap<String, String> processed = new HashMap<String, String>(100000);
//        HashSet<String> asIs = new HashSet<String>(10000);
//        for (final String cityToCheck : citiesFromFile) {
//            PriorityBuffer priorityBuffer = new PriorityBuffer(citiesFromVk.size(), new Comparator() {
//                @Override
//                public int compare(Object o1, Object o2) {
//                    return Ints.compare(levenshteinDistance((CharSequence) o1, cityToCheck),
//                            levenshteinDistance((CharSequence) o2, cityToCheck));
//                }
//            });
//            if (citiesFromVk.contains(cityToCheck)) {
//                asIs.add(cityToCheck);
//            } else {
//                for (final String originalCity : citiesFromVk) {
//                    priorityBuffer.add(originalCity);
//                }
//                String o = (String) priorityBuffer.get();
//                processed.put(cityToCheck, o);
//                System.out.println(cityToCheck + " % " + o + " & " + levenshteinDistance(cityToCheck, o));
//            }
//        }
//
//        FileOutputStream cities = new FileOutputStream("citiesAll.txt");
//        for (String city : citiesFromVk) {
//            cities.write(city.getBytes());
//            cities.write("\r\n".getBytes());
//        }
//        cities.close();
        System.out.println("Reading dictionary from " + Paths.get(config.getDictionaryPath()));
        float[] accuracies = new float[]{0.95f, 0.9f, 0.85f, 0.8f, 0.75f, 0.7f, 0.65f, 0.6f, 0.55f, 0.5f};
        Analyzer analyzer = new StandardAnalyzer();
        FSDirectory index = FSDirectory.open(Paths.get("index"), SimpleFSLockFactory.getDefault());
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        SpellChecker spellChecker = new SpellChecker(index);
        spellChecker.indexDictionary(new PlainTextDictionary(Paths.get(config.getDictionaryPath())), conf, true);

        System.out.println("Reading corrections from " + Paths.get(config.getCorrectionsPath()));
        LineReader correctionReader = new LineReader(new FileReader(config.getCorrectionsPath()));
        List<String> correctionList = new ArrayList<String>();
        String correction;
        while ((correction = correctionReader.readLine()) != null) {
            correctionList.add(correction);
        }

        System.out.println("Reading data from " + Paths.get(config.getInputCsvPath()) + " and storing to " + Paths.get(config.getOutputCsvPath()));
        Reader in = new InputStreamReader(new FileInputStream(config.getInputCsvPath()), Charset.forName(config.getCsvEncoding()));
        Writer out = new OutputStreamWriter(new FileOutputStream(config.getOutputCsvPath()), Charset.forName(config.getCsvEncoding()));
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(config.getCsvDelimiter()).parse(in);
        final CSVPrinter printer = CSVFormat.EXCEL.withDelimiter(config.getCsvDelimiter()).withSkipHeaderRecord().print(out);
        LevensteinDistance levensteinDistance = new LevensteinDistance();
        int count = 0;
        for (Iterator<CSVRecord> iterator = records.iterator(); iterator.hasNext(); ) {
            CSVRecord record = iterator.next();
            count++;
            if (count % 10000 == 0) {
                System.out.println("Processed " + count + " elements.");
            }
            String word = record.get(config.getInputColumn());
            List<String> results = new ArrayList<String>(2);
            for (String s : correctionList) {
                word = word.replace(s, "");
            }
            if (word.length() > 2) {
                if (!spellChecker.exist(word)) {
                    for (int i = 0; i < accuracies.length; i++) {
                        String[] suggestSimilar = spellChecker.suggestSimilar(word, 10, null, null, SuggestMode.SUGGEST_ALWAYS, accuracies[i]);
                        if (suggestSimilar.length > 0) {
                            String result = suggestSimilar[0];
                            results.add(result);
                            results.add(Float.toString(levensteinDistance.getDistance(word, result)));
                            break;
                        }
                    }
                } else {
                    results.add(word);
                    results.add(Float.toString(1f));
                }
            }
            assert results.size() == 2 || results.size() == 0;
            if (results.size() == 0) {
                results.add(word);
                results.add(Float.toString(0f));
            }
            Iterator<String> recordIterator = Iterators.concat(record.iterator(), results.iterator());
            printer.printRecord(Iterators.toArray(recordIterator, String.class));
        }
        printer.flush();
        System.out.println("Done! See results " + Paths.get(config.getOutputCsvPath()));
    }

    private static Set<String> extractUsersData(Set<String> cities) throws IOException {
        HashSet<String> citiesFromFile = new HashSet<String>(100000);
        FileInputStream fileInputStream = new FileInputStream("/home/imamontov/Downloads/users.csv");
        LineReader lineReader = new LineReader(new InputStreamReader(fileInputStream));
        String s = lineReader.readLine();//header
        while ((s = lineReader.readLine()) != null) {
            String[] split = s.split(";");
            if (split.length >= 6 && cities.contains(split[0])) {
                citiesFromFile.add(split[5]);
            }
        }
        System.out.println("citiesFromFile = " + citiesFromFile.size());
        return citiesFromFile;
    }

    private static Set<String> extractCities(int... regionId) throws IOException {
        HashSet<String> strings = new HashSet<String>(100000);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectReader reader = objectMapper.reader();
        for (int j = 0; j < regionId.length; j++) {
            int region = regionId[j];

            for (int i = 0; i < 159097; i = i + 1000) {
                String url = "http://api.vk.com/method/database.getCities?v=5.5&country_id=1&offset=" + i + "&region_id=" + region + "&need_all=1&count=1000";
                URL oracle = new URL(url);
                URLConnection yc = oracle.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                JsonNode jsonNode = reader.readTree(in);
                ArrayNode cities = (ArrayNode) jsonNode.get("response").get("items");
                if (cities.size() == 0) {
                    break;
                }
                for (JsonNode city : cities) {
                    JsonNode title = city.get("title");
                    strings.add(title.asText());
                }
                in.close();
                System.out.println("i=" + i);
            }
        }
        System.out.println("Total size is " + strings.size());
        return strings;
    }

    private static Set<String> extractAllCities() throws IOException {
        HashSet<String> strings = new HashSet<String>(100000);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectReader reader = objectMapper.reader();

        for (int i = 0; i < 199097; i = i + 1000) {
            String url = "http://api.vk.com/method/database.getCities?v=5.5&country_id=1&offset=" + i + "&need_all=1&count=1000";
            URL oracle = new URL(url);
            URLConnection yc = oracle.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            JsonNode jsonNode = reader.readTree(in);
            ArrayNode cities = (ArrayNode) jsonNode.get("response").get("items");
            if (cities.size() == 0) {
                break;
            }
            for (JsonNode city : cities) {
                JsonNode title = city.get("title");
                strings.add(title.asText());
            }
            in.close();
            System.out.println("i=" + i);
        }
        System.out.println("Total size is " + strings.size());
        return strings;
    }


    public static int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

}
