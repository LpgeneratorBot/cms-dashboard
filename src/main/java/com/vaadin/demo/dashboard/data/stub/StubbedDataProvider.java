package com.vaadin.demo.dashboard.data.stub;

import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.domain.Client;
import com.vaadin.demo.dashboard.domain.ClientStatus;
import com.vaadin.demo.dashboard.domain.ClientStatusHistory;
import com.vaadin.demo.dashboard.domain.DashboardNotification;
import com.vaadin.demo.dashboard.domain.MailConfiguration;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.demo.dashboard.domain.UserGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * @author imamontov
 */
public class StubbedDataProvider implements DataProvider {

    private Collection<Client> clients;
    private Collection<ClientStatus> clientStatuses;
    private User user;
    private ArrayList<UserGroup> userGroups;

    public StubbedDataProvider() {
        clients = new ArrayList<Client>();
        Client client = new Client();
        client.setCity("city");
        client.setEmail("mail");
        client.setStatus("status");
        client.setName("name");
        client.setPhone("phone");
        clients.add(client);

        user = new User();
        user.setEmail("mail");
        user.setLogin("l");
        user.setPassword("p");
        user.setRole("admin");

        clientStatuses = new ArrayList<ClientStatus>();
        ClientStatus clientStatus = new ClientStatus();
        clientStatus.setName("Status");
        clientStatus.setDescription("Desc");
        clientStatus.setId(1);
        clientStatuses.add(clientStatus);

        userGroups = new ArrayList<UserGroup>();
        UserGroup userGroup = new UserGroup();
        userGroup.setId(1);
        userGroup.setDescription("Desc");
        userGroups.add(userGroup);
    }

    @Override
    public Collection<Client> getAllRecentClients() {
        return clients;
    }

    @Override
    public Collection<Client> getRecentClientsByUser(User user) {
        return clients;
    }

    @Override
    public boolean insertClient(Client client) {
        return true;
    }

    @Override
    public User authenticate(String userName, String password) {
        return user;
    }

    @Override
    public boolean updateUser(User user) {
        return true;
    }

    @Override
    public Collection<ClientStatus> getAllStatuses() {
        return clientStatuses;
    }

    @Override
    public Collection<ClientStatusHistory> getStatusesForClient(Client client) {
        return Collections.emptyList();
    }

    @Override
    public Collection<UserGroup> getGroups() {
        return userGroups;
    }

    @Override
    public boolean updateClient(Client client) {
        return true;
    }

    @Override
    public boolean updateClientStatus(ClientStatusHistory status) {
        return true;
    }

    @Override
    public int getUnreadNotificationsCount() {
        return 0;
    }

    @Override
    public Collection<DashboardNotification> getNotifications() {
        return Collections.emptyList();
    }

    @Override
    public MailConfiguration getMailConfiguration() {
        return new MailConfiguration();
    }
}
