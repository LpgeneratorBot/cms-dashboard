package com.vaadin.demo.dashboard.data.dummy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.domain.DashboardNotification;
import com.vaadin.demo.dashboard.domain.Movie;
import com.vaadin.demo.dashboard.domain.MovieRevenue;
import com.vaadin.demo.dashboard.domain.Client;
import com.vaadin.demo.dashboard.domain.User;

/**
 * A dummy implementation for the backend API.
 */
public class DummyDataProvider implements DataProvider {

    /* List of countries and cities for them */
    private static Multimap<String, String> countryToCities;
    private static Date lastDataUpdate;
    private static Collection<Movie> movies;
    private static Multimap<Long, Client> transactions;
    private static Multimap<Long, MovieRevenue> revenue;

    private static Random rand = new Random();

    private final Collection<DashboardNotification> notifications = DummyDataGenerator
            .randomNotifications();
    private ArrayList<Client> clients;

    /**
     * Initialize the data for this application.
     */
    public DummyDataProvider() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        if (lastDataUpdate == null || lastDataUpdate.before(cal.getTime())) {
            refreshStaticData();
            lastDataUpdate = new Date();
        }

        clients = new ArrayList<Client>();

        Client client = new Client();
        client.setPhone("999999");
        client.setEmail("i.@m.ru");
        client.setName("Василий П");
        client.setCity("Санкт-Петербург");
        client.setStatus("Новый");
        client.setId(1);
        client.setDate(new Date());

        Client client2 = new Client();
        client2.setPhone("+7(904)512-93-41");
        client2.setEmail("vasss@google.com");
        client2.setName("Петро");
        client2.setCity("Самара");
        client2.setStatus("Не отвечает");
        client2.setId(2);
        client2.setDate(new Date());

        clients.add(client);
        clients.add(client2);
    }

    private void refreshStaticData() {
        countryToCities = loadTheaterData();
        movies = loadMoviesData();
        transactions = generateTransactionsData();
        revenue = countRevenues();
    }

    /**
     * Get a list of movies currently playing in theaters.
     *
     * @return a list of Movie objects
     */
    @Override
    public Collection<Movie> getMovies() {
        return Collections.unmodifiableCollection(movies);
    }

    /**
     * Initialize the list of movies playing in theaters currently. Uses the
     * Rotten Tomatoes API to get the list. The result is cached to a local file
     * for 24h (daily limit of API calls is 10,000).
     */
    private static Collection<Movie> loadMoviesData() {
        return new ArrayList<Movie>();
    }

    /* JSON utility method */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /* JSON utility method */
    private static JsonObject readJsonFromUrl(String url) throws IOException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                    Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JsonElement jelement = new JsonParser().parse(jsonText);
            JsonObject jobject = jelement.getAsJsonObject();
            return jobject;
        } finally {
            is.close();
        }
    }

    /* JSON utility method */
    private static JsonObject readJsonFromFile(File path) throws IOException {
        BufferedReader rd = new BufferedReader(new FileReader(path));
        String jsonText = readAll(rd);
        JsonElement jelement = new JsonParser().parse(jsonText);
        JsonObject jobject = jelement.getAsJsonObject();
        return jobject;
    }

    /**
     * =========================================================================
     * Countries, cities, theaters and rooms
     * =========================================================================
     */

    static List<String> theaters = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;

        {
            add("Threater 1");
            add("Threater 2");
            add("Threater 3");
            add("Threater 4");
            add("Threater 5");
            add("Threater 6");
        }
    };

    static List<String> rooms = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;

        {
            add("Room 1");
            add("Room 2");
            add("Room 3");
            add("Room 4");
            add("Room 5");
            add("Room 6");
        }
    };

    /**
     * Parse the list of countries and cities
     */
    private static Multimap<String, String> loadTheaterData() {

        /* First, read the text file into a string */
        StringBuffer fileData = new StringBuffer(2000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                DummyDataProvider.class.getResourceAsStream("cities.txt")));

        char[] buf = new char[1024];
        int numRead = 0;
        try {
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String list = fileData.toString();

        /*
         * The list has rows with tab delimited values. We want the second (city
         * name) and last (country name) values, and build a Map from that.
         */
        Multimap<String, String> countryToCities = MultimapBuilder.hashKeys()
                .arrayListValues().build();
        for (String line : list.split("\n")) {
            String[] tabs = line.split("\t");
            String city = tabs[1];
            String country = tabs[tabs.length - 2];

            if (!countryToCities.containsKey(country)) {
                countryToCities.putAll(country, new ArrayList<String>());
            }
            countryToCities.get(country).add(city);
        }

        return countryToCities;

    }

    /**
     * Create a list of dummy transactions
     *
     * @return
     */
    private Multimap<Long, Client> generateTransactionsData() {
        Multimap<Long, Client> result = MultimapBuilder.hashKeys()
                .arrayListValues().build();

        for (Movie movie : movies) {
            result.putAll(movie.getId(), new ArrayList<Client>());

            Calendar cal = Calendar.getInstance();
            int daysSubtractor = rand.nextInt(150) + 30;
            cal.add(Calendar.DAY_OF_YEAR, -daysSubtractor);

            Calendar lastDayOfWeek = Calendar.getInstance();
            lastDayOfWeek.add(Calendar.DAY_OF_YEAR,
                    Calendar.SATURDAY - cal.get(Calendar.DAY_OF_WEEK));

            while (cal.before(lastDayOfWeek)) {

                int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                if (hourOfDay > 10 && hourOfDay < 22) {

                    Client client = new Client();
                    client.setStatus(movie.getTitle());

                    // Country
                    Object[] array = countryToCities.keySet().toArray();
                    int i = (int) (Math.random() * (array.length - 1));
                    String country = array[i].toString();
                    client.setName(country);

                    client.setDate(cal.getTime());

                    // City
                    Collection<String> cities = countryToCities.get(country);
                    client.setCity(cities.iterator().next());

                    // Theater
                    String theater = theaters
                            .get((int) (rand.nextDouble() * (theaters.size() - 1)));
                    client.setPhone(theater);

                    // Room
                    String room = rooms.get((int) (rand.nextDouble() * (rooms
                            .size() - 1)));
                    client.setEmail(room);

                    // Title
                    int randomIndex = (int) (Math.abs(rand.nextGaussian()) * (movies
                            .size() / 2.0 - 1));
                    while (randomIndex >= movies.size()) {
                        randomIndex = (int) (Math.abs(rand.nextGaussian()) * (movies
                                .size() / 2.0 - 1));
                    }

                    result.get(movie.getId()).add(client);
                }

                cal.add(Calendar.SECOND, rand.nextInt(500000) + 5000);
            }
        }

        return result;

    }

    public static Movie getMovieForTitle(String title) {
        for (Movie movie : movies) {
            if (movie.getTitle().equals(title)) {
                return movie;
            }
        }
        return null;
    }

    @Override
    public User authenticate(String userName, String password) {
        User user = new User();
        user.setFirstName("Анатоль");
        user.setLastName("Саныч");
        user.setRole("admin");
        user.setMale(true);
        user.setEmail("anatol@mail.ru");
        user.setPhone("+7 999 221 22 11");
        user.setLocation("Красный Дар");
        user.setBio("Тут ничего не написано");
        return user;
    }

    @Override
    public Collection<Client> getRecentTransactions(int count) {
        return clients;
    }

    private Multimap<Long, MovieRevenue> countRevenues() {
        Multimap<Long, MovieRevenue> result = MultimapBuilder.hashKeys()
                .arrayListValues().build();
        for (Movie movie : movies) {
            result.putAll(movie.getId(), countMovieRevenue(movie));
        }
        return result;
    }

    private Collection<MovieRevenue> countMovieRevenue(Movie movie) {
        Map<Date, Double> dailyIncome = new HashMap<Date, Double>();
        for (Client client : transactions.get(movie.getId())) {
            Date day = getDay(client.getDate());

            Double currentValue = dailyIncome.get(day);
            if (currentValue == null) {
                currentValue = 0.0;
            }
            dailyIncome.put(day, currentValue + 0);
        }

        Collection<MovieRevenue> result = new ArrayList<MovieRevenue>();

        List<Date> dates = new ArrayList<Date>(dailyIncome.keySet());
        Collections.sort(dates);

        double revenueSoFar = 0.0;
        for (Date date : dates) {
            MovieRevenue movieRevenue = new MovieRevenue();
            movieRevenue.setTimestamp(date);
            revenueSoFar += dailyIncome.get(date);
            movieRevenue.setRevenue(revenueSoFar);
            movieRevenue.setTitle(movie.getTitle());
            result.add(movieRevenue);
        }

        return result;
    }

    @Override
    public Collection<MovieRevenue> getDailyRevenuesByMovie(long id) {
        return Collections.unmodifiableCollection(revenue.get(id));
    }

    private Date getDay(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal.getTime();
    }

    @Override
    public Collection<MovieRevenue> getTotalMovieRevenues() {
        return Collections2.transform(movies,
                new Function<Movie, MovieRevenue>() {
                    @Override
                    public MovieRevenue apply(Movie input) {
                        return Iterables.getLast(getDailyRevenuesByMovie(input
                                .getId()));
                    }
                });
    }

    @Override
    public int getUnreadNotificationsCount() {
        Predicate<DashboardNotification> unreadPredicate = new Predicate<DashboardNotification>() {
            @Override
            public boolean apply(DashboardNotification input) {
                return !input.isRead();
            }
        };
        return Collections2.filter(notifications, unreadPredicate).size();
    }

    @Override
    public Collection<DashboardNotification> getNotifications() {
        for (DashboardNotification notification : notifications) {
            notification.setRead(true);
        }
        return Collections.unmodifiableCollection(notifications);
    }

    @Override
    public double getTotalSum() {
        double result = 0;
        for (Client client : transactions.values()) {
            result += 1;
        }
        return result;
    }

    @Override
    public Movie getMovie(final long movieId) {
        return Iterables.find(movies, new Predicate<Movie>() {
            @Override
            public boolean apply(Movie input) {
                return input.getId() == movieId;
            }
        });
    }

    @Override
    public Collection<Client> getTransactionsBetween(final Date startDate,
                                                   final Date endDate) {
        return Collections2.filter(transactions.values(),
                new Predicate<Client>() {
                    @Override
                    public boolean apply(Client input) {
                        return !input.getDate().before(startDate)
                                && !input.getDate().after(endDate);
                    }
                });
    }

}
