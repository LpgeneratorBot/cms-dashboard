package com.vaadin.demo.dashboard.view.transactions;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.demo.dashboard.domain.Client;
import com.vaadin.demo.dashboard.event.DashboardEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/* Create custom UI Components.
 *
 * Create your own Vaadin components by inheritance and composition.
 * This is a form component inherited from VerticalLayout. Use
 * Use BeanFieldGroup to bind data fields from DTO to UI fields.
 * Similarly named field by naming convention or customized
 * with @PropertyId annotation.
 */
public class ClientForm extends FormLayout {

    Button save = new Button("Сохранить");
    Button cancel = new Button("Отмена");
    TextField name = new TextField("ФИО");
    ComboBox status = new ComboBox("Статус");
    TextField phone = new TextField("Телефон");
    TextField email = new TextField("Почта");
    DateField date = new DateField("Дата");

    Client client;

    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<Client> formFieldBindings;

    public ClientForm() {
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        /* Highlight primary actions.
         *
         * With Vaadin built-in styles you can highlight the primary save button
         * and give it a keyboard shortcut for a better UX.
         */
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ClientForm.this.setVisible(false);
            }
        });
        save.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // Bind the properties of the client POJO to fiels in this form
                client.setPhone(phone.getValue());
                client.setName(name.getValue());
                client.setEmail(email.getValue());
                client.setStatus((String) status.getValue());
                ClientForm.this.setVisible(false);
                DashboardEventBus.post(new DashboardEvent.ClientUpdatedEvent());
            }
        });
        status.setNewItemsAllowed(false);
        status.setImmediate(true);
        status.addItems("Новый", "В обработке", "Не отвечает", "Закрыт");
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);

		addComponents(actions, name, status, phone, email, date);
    }

    void edit(Client contact) {
        this.client = contact;
        if(contact != null) {
            // Bind the properties of the client POJO to fiels in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(contact, this);
            name.focus();
        }
    }
}
