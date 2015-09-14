package com.vaadin.demo.dashboard.data;

import com.vaadin.demo.dashboard.domain.Client;
import com.vaadin.demo.dashboard.domain.ClientStatus;
import com.vaadin.demo.dashboard.domain.ClientStatusHistory;
import com.vaadin.demo.dashboard.domain.DashboardNotification;
import com.vaadin.demo.dashboard.domain.MailConfiguration;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.demo.dashboard.domain.UserGroup;

import java.util.Collection;
import java.util.Date;

/**
 * QuickTickets Dashboard backend API.
 */
public interface DataProvider {

    Collection<Client> getAllRecentClients();

    /**.
     * @return A Collection of most recent transactions.
     */
    Collection<Client> getRecentClientsByUser(User user);

    boolean insertClient(Client client);

    /**
     * @param userName
     * @param password
     * @return Authenticated used.
     */
    User authenticate(String userName, String password);

    boolean updateUser(User user);

    Collection<ClientStatus> getAllStatuses();

    Collection<ClientStatusHistory> getStatusesForClient(Client client);

    Collection<UserGroup> getGroups();

    boolean updateClient(Client client);

    boolean updateClientStatus(ClientStatusHistory status);

    /**
     * @return The number of unread notifications for the current user.
     */
    int getUnreadNotificationsCount();

    /**
     * @return Notifications for the current user.
     */
    Collection<DashboardNotification> getNotifications();

    MailConfiguration getMailConfiguration();
}
