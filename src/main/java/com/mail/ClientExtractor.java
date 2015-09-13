package com.mail;

import com.vaadin.demo.dashboard.domain.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class ClientExtractor {

    public static String PATTERN_ID = ".*ID: ([0-9]+?)<br.*";
    public static String PATTERN_NAME = ".*Имя: (.*?)<br.*";
    public static String PATTERN_EMAIL = ".*Email: (.*?)<br.*";
    public static String PATTERN_PHONE = ".*Телефон: (.*?)<br.*";

    public static final Pattern ID = Pattern.compile(PATTERN_ID, Pattern.DOTALL);
    public static final Pattern NAME = Pattern.compile(PATTERN_NAME, Pattern.DOTALL);
    public static final Pattern EMAIL = Pattern.compile(PATTERN_EMAIL, Pattern.DOTALL);
    public static final Pattern PHONE = Pattern.compile(PATTERN_PHONE, Pattern.DOTALL);

    public static List<Client> extract(String[] messages) {
        ArrayList<Client> persons = new ArrayList<Client>();
        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            persons.add(extractMessage(message));
        }
        return persons;
    }

    public static Client extractMessage(String message) {
        Client person = new Client();
        Matcher matcher = NAME.matcher(message);
        if (matcher.matches()) {
            String name = matcher.group(1);
            person.setName(name.trim());
        }
        matcher = EMAIL.matcher(message);
        if (matcher.matches()) {
            String mail = matcher.group(1);
            person.setEmail(mail.trim());
        }
        matcher = PHONE.matcher(message);
        if (matcher.matches()) {
            String phone = matcher.group(1);
            person.setPhone(phone.trim());
        }
        return person;
    }
}
