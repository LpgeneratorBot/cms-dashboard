package com.vaadin.demo.dashboard.data.ibatis;

import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.domain.Client;
import com.vaadin.demo.dashboard.domain.ClientStatus;
import com.vaadin.demo.dashboard.domain.ClientStatusHistory;
import com.vaadin.demo.dashboard.domain.DashboardNotification;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.demo.dashboard.domain.UserGroup;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

/**
 * @author imamontov
 */
public class IBatisDataProvider implements DataProvider {

    private final SqlSessionFactory sqlSessionFactory;

    public IBatisDataProvider() {

        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream("batis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Override
    public Collection<Client> getAllRecentClients() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            return session.selectList("ClientProvider.getAllRecentClients");
        } finally {
            session.close();
        }
    }

    @Override
    public Collection<Client> getRecentClientsByUser(User user) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            return session.selectList("ClientProvider.getRecentClientsByUser", user);
        } finally {
            session.close();
        }
    }

    @Override
    public User authenticate(String userName, String password) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            User user = session.selectOne("UserProvider.selectByLogin", userName);
            // Check that an unencrypted password matches one that has
            // previously been hashed BCrypt.hashpw(password, BCrypt.gensalt());
            if (user != null && BCrypt.checkpw(password, user.getPassword())) {
                return user;
            } else {
                return User.NULL_USER;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public boolean updateUser(User user) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            int updateCount = session.update("UserProvider.updateUser", user);
            return updateCount > 0;
        } finally {
            session.commit();
            session.close();
        }
    }

    @Override
    public Collection<ClientStatus> getAllStatuses() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            return session.selectList("ClientProvider.getAllStatuses");
        } finally {
            session.close();
        }
    }

    @Override
    public boolean updateClient(Client client) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            int updateCount = session.update("ClientProvider.updateClient", client);
            return updateCount > 0;
        } finally {
            session.commit();
            session.close();
        }
    }

    @Override
    public Collection<UserGroup> getGroups() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            return session.selectList("ClientProvider.getGroups");
        } finally {
            session.close();
        }
    }

    @Override
    public Collection<ClientStatusHistory> getStatusesForClient(Client client) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            return session.selectList("ClientProvider.getStatusesForClient", client);
        } finally {
            session.close();
        }
    }

    @Override
    public boolean updateClientStatus(ClientStatusHistory status) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            return session.insert("ClientProvider.updateClientStatus", status) > 0;
        } finally {
            session.commit();
            session.close();
        }
    }

    @Override
    public int getUnreadNotificationsCount() {
        return 0;
    }

    @Override
    public Collection<DashboardNotification> getNotifications() {
        return null;
    }

    @Override
    public Collection<Client> getTransactionsBetween(Date startDate, Date endDate) {
        return null;
    }
}
