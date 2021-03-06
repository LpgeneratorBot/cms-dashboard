package com.vaadin.demo.dashboard;

import com.google.common.eventbus.Subscribe;
import com.mail.ClientExtractor;
import com.mail.MailChecker;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.data.ibatis.IBatisDataProvider;
import com.vaadin.demo.dashboard.data.stub.StubbedDataProvider;
import com.vaadin.demo.dashboard.domain.Client;
import com.vaadin.demo.dashboard.domain.MailConfiguration;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.demo.dashboard.event.DashboardEvent.BrowserResizeEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.CloseOpenWindowsEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.UserLoggedOutEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.UserLoginRequestedEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.view.LoginView;
import com.vaadin.demo.dashboard.view.MainView;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Theme("dashboard")
@Widgetset("com.vaadin.demo.dashboard.DashboardWidgetSet")
@Title("CRM")
@SuppressWarnings("serial")
public final class DashboardUI extends UI {

    /*
     * This field stores an access to the dummy backend layer. In real
     * applications you most likely gain access to your beans trough lookup or
     * injection; and not in the UI but somewhere closer to where they're
     * actually accessed.
     */
    private final DataProvider dataProvider = new IBatisDataProvider();
//    private final DataProvider dataProvider = new StubbedDataProvider();
    private final DashboardEventBus dashboardEventbus = new DashboardEventBus();
    private ScheduledExecutorService service;

    @Override
    protected void init(final VaadinRequest request) {
        setLocale(Locale.US);

        DashboardEventBus.register(this);
        Responsive.makeResponsive(this);
        addStyleName(ValoTheme.UI_WITH_MENU);

        updateContent();

        // Some views need to be aware of browser resize events so a
        // BrowserResizeEvent gets fired to the event bus on every occasion.
        Page.getCurrent().addBrowserWindowResizeListener(
                new BrowserWindowResizeListener() {
                    @Override
                    public void browserWindowResized(
                            final BrowserWindowResizeEvent event) {
                        DashboardEventBus.post(new BrowserResizeEvent());
                    }
                });

        service = Executors.newSingleThreadScheduledExecutor();
        MailConfiguration mailConfiguration = dataProvider.getMailConfiguration();
        final MailChecker checker = new MailChecker();
        checker.setConfiguration(mailConfiguration);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] check = checker.check();
                    List<Client> clients = ClientExtractor.extract(check);
                    for (Client client : clients) {
                        dataProvider.insertClient(client);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, 1, mailConfiguration.getPeriod(), TimeUnit.MINUTES);
    }

    /**
     * Updates the correct content for this UI based on the current user status.
     * If the user is logged in with appropriate privileges, main view is shown.
     * Otherwise login view is shown.
     */
    private void updateContent() {
        User user = (User) VaadinSession.getCurrent().getAttribute(
                User.class.getName());
        if (user != null) {
            if (user != User.NULL_USER) {
                // Authenticated user
                setContent(new MainView());
                removeStyleName("loginview");
                getNavigator().navigateTo(getNavigator().getState());
            } else {
                setContent(new LoginView());
                addStyleName("loginview");

                Notification success = new Notification(
                        "Ошибка входа в систему. Неверный логин или пароль.");
                success.setDelayMsec(2000);
                success.setStyleName("bar error huge");
                success.setPosition(Position.BOTTOM_CENTER);
                success.show(Page.getCurrent());
                VaadinSession.getCurrent().setAttribute(User.class.getName(), user);
            }
        } else {
            setContent(new LoginView());
            addStyleName("loginview");
        }
    }

    @Subscribe
    public void userLoginRequested(final UserLoginRequestedEvent event) {
        User user = getDataProvider().authenticate(event.getUserName(),
                event.getPassword());
        VaadinSession.getCurrent().setAttribute(User.class.getName(), user);
        updateContent();
    }

    @Subscribe
    public void userLoggedOut(final UserLoggedOutEvent event) {
        // When the user logs out, current VaadinSession gets closed and the
        // page gets reloaded on the login screen. Do notice the this doesn't
        // invalidate the current HttpSession.
        VaadinSession.getCurrent().close();
        Page.getCurrent().reload();
    }

    @Subscribe
    public void closeOpenWindows(final CloseOpenWindowsEvent event) {
        for (Window window : getWindows()) {
            window.close();
        }
    }

    /**
     * @return An instance for accessing the (dummy) services layer.
     */
    public static DataProvider getDataProvider() {
        return ((DashboardUI) getCurrent()).dataProvider;
    }

    public static DashboardEventBus getDashboardEventbus() {
        return ((DashboardUI) getCurrent()).dashboardEventbus;
    }
}
