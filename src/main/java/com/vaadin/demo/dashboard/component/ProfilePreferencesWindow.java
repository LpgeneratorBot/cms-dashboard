package com.vaadin.demo.dashboard.component;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.demo.dashboard.event.DashboardEvent.CloseOpenWindowsEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.ProfileUpdatedEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.mindrot.jbcrypt.BCrypt;

@SuppressWarnings("serial")
public class ProfilePreferencesWindow extends Window {

    public static final String ID = "profilepreferenceswindow";

    private final BeanFieldGroup<User> fieldGroup;
    /*
     * Fields for editing the User object are defined here as class members.
     * They are later bound to a FieldGroup by calling
     * fieldGroup.bindMemberFields(this). The Fields' values don't need to be
     * explicitly set, calling fieldGroup.setItemDataSource(user) synchronizes
     * the fields with the user object.
     */
    @PropertyId("firstName")
    private TextField firstNameField;
    @PropertyId("lastName")
    private TextField lastNameField;
    @PropertyId("email")
    private TextField emailField;
    @PropertyId("location")
    private TextField locationField;

    @PropertyId("phone")
    private TextField phoneField;

    @PropertyId("newPassword")
    private PasswordField newPasswordField;

    @PropertyId("newPasswordToCheck")
    private PasswordField newPasswordFieldToCheck;

    private User user;

    private ProfilePreferencesWindow(final User user,
                                     final boolean preferencesTabOpen) {
        addStyleName("profile-window");
        setId(ID);
        Responsive.makeResponsive(this);

        this.user = user;
        setModal(true);
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(false);
        setClosable(false);
        setHeight(90.0f, Unit.PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(new MarginInfo(true, false, false, false));
        setContent(content);

        TabSheet detailsWrapper = new TabSheet();
        detailsWrapper.setSizeFull();
        detailsWrapper.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        detailsWrapper.addStyleName(ValoTheme.TABSHEET_ICONS_ON_TOP);
        detailsWrapper.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        content.addComponent(detailsWrapper);
        content.setExpandRatio(detailsWrapper, 1f);

        detailsWrapper.addComponent(buildProfileTab());
        detailsWrapper.addComponent(buildPreferencesTab());

        if (preferencesTabOpen) {
            detailsWrapper.setSelectedTab(1);
        }

        content.addComponent(buildFooter());

        fieldGroup = new BeanFieldGroup<User>(User.class);
        fieldGroup.bindMemberFields(this);
        fieldGroup.setItemDataSource(user);
    }

    private Component buildPreferencesTab() {
        VerticalLayout root = new VerticalLayout();
        root.setCaption("Настройки");
        root.setIcon(FontAwesome.COGS);
        root.setSpacing(true);
        root.setMargin(true);
        root.setSizeFull();

        Label message = new Label("Ничего нет");
        message.setSizeUndefined();
        message.addStyleName(ValoTheme.LABEL_LIGHT);
        root.addComponent(message);
        root.setComponentAlignment(message, Alignment.MIDDLE_CENTER);

        return root;
    }

    private Component buildProfileTab() {
        HorizontalLayout root = new HorizontalLayout();
        root.setCaption("Профайл");
        root.setIcon(FontAwesome.USER);
        root.setWidth(100.0f, Unit.PERCENTAGE);
        root.setSpacing(true);
        root.setMargin(true);
        root.addStyleName("profile-form");

        VerticalLayout pic = new VerticalLayout();
        pic.setSizeUndefined();
        pic.setSpacing(true);
        Image profilePic = new Image(null, new ThemeResource(
                "img/profile-pic-300px.jpg"));
        profilePic.setWidth(100.0f, Unit.PIXELS);
        pic.addComponent(profilePic);

        Button upload = new Button("Изменить…", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Notification.show("Не поддерживается в демо режиме...");
            }
        });
        upload.addStyleName(ValoTheme.BUTTON_TINY);
        pic.addComponent(upload);

        root.addComponent(pic);

        FormLayout details = new FormLayout();
        details.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        root.addComponent(details);
        root.setExpandRatio(details, 1);

        firstNameField = new TextField("Имя");
        details.addComponent(firstNameField);
        lastNameField = new TextField("Фамилия");
        details.addComponent(lastNameField);

        Label section = new Label("Контактная информация");
        section.addStyleName(ValoTheme.LABEL_H4);
        section.addStyleName(ValoTheme.LABEL_COLORED);
        details.addComponent(section);

        emailField = new TextField("Почта");
        emailField.setWidth("100%");
        emailField.setRequired(true);
        emailField.setNullRepresentation("");
        details.addComponent(emailField);

        locationField = new TextField("Город");
        locationField.setWidth("100%");
        locationField.setNullRepresentation("");
        details.addComponent(locationField);

        phoneField = new TextField("Телефон");
        phoneField.setWidth("100%");
        phoneField.setNullRepresentation("");
        details.addComponent(phoneField);

        section = new Label("Сменить пароль");
        section.addStyleName(ValoTheme.LABEL_H4);
        section.addStyleName(ValoTheme.LABEL_COLORED);
        details.addComponent(section);

        newPasswordField = new PasswordField("Новый пароль");
        details.addComponent(newPasswordField);
        newPasswordFieldToCheck = new PasswordField("Подтвердите пароль");
        details.addComponent(newPasswordFieldToCheck);

        return root;
    }

    private Component buildFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);

        Button ok = new Button("Сохранить");
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
        ok.addClickListener(new ClickListener() {
                                @Override
                                public void buttonClick(ClickEvent event) {

                                    //fieldGroup.commit();
                                    // Updated user should also be persisted to database. But
                                    // not in this demo.
                                    if (!newPasswordField.isEmpty()
                                            || !newPasswordFieldToCheck.isEmpty()) {
                                        if (newPasswordField.getValue().
                                                equals(newPasswordFieldToCheck.getValue())) {
                                            String hashpw = BCrypt.hashpw(newPasswordField.getValue(), BCrypt.gensalt());
                                            user.setPassword(hashpw);
                                        } else {
                                            Notification.show("Пароли не совпадают",
                                                    Type.ERROR_MESSAGE);
                                            return;
                                        }
                                    }
                                    user.setPhone(phoneField.getValue());
                                    user.setEmail(emailField.getValue());
                                    user.setFirstName(firstNameField.getValue());
                                    user.setLastName(lastNameField.getValue());
                                    user.setLocation(locationField.getValue());
                                    boolean result = DashboardUI.getDataProvider().updateUser(user);
                                    if (result) {
                                        Notification success = new Notification(
                                                "Профиль успешно обновлен");
                                        success.setDelayMsec(2000);
                                        success.setStyleName("bar success huge");
                                        success.setPosition(Position.BOTTOM_CENTER);
                                        success.show(Page.getCurrent());

                                        DashboardEventBus.post(new ProfileUpdatedEvent());
                                        close();
                                    } else {
                                        Notification.show("Ошибка сохранения",
                                                Type.ERROR_MESSAGE);
                                    }
                                }
                            }
        );
        footer.addComponent(ok);
        Button cancel = new Button("Отмена");
        cancel.focus();
        cancel.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                close();
            }
        });
        footer.addComponent(cancel);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        return footer;
    }

    public static void open(final User user, final boolean preferencesTabActive) {
        DashboardEventBus.post(new CloseOpenWindowsEvent());
        Window w = new ProfilePreferencesWindow(user, preferencesTabActive);
        UI.getCurrent().addWindow(w);
        w.focus();
    }
}
