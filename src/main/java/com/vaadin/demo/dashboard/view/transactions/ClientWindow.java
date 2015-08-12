package com.vaadin.demo.dashboard.view.transactions;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.vaadin.data.Property;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.domain.Client;
import com.vaadin.demo.dashboard.domain.ClientStatus;
import com.vaadin.demo.dashboard.domain.ClientStatusHistory;
import com.vaadin.demo.dashboard.event.DashboardEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/* Create custom UI Components.
 *
 * Create your own Vaadin components by inheritance and composition.
 * This is a form component inherited from VerticalLayout. Use
 * Use BeanFieldGroup to bind data fields from DTO to UI fields.
 * Similarly named field by naming convention or customized
 * with @PropertyId annotation.
 */
public class ClientWindow extends Window {

    private static final String ID = "client-window";

    Button save = new Button("Сохранить");
    Button cancel = new Button("Отмена");
    TextField name = new TextField("ФИО");
    ComboBox status = new ComboBox("Статус");
    TextField phone = new TextField("Телефон");
    TextField email = new TextField("Почта");
    DateField date = new DateField("Дата");
    TextArea description = new TextArea("Описание");

    private Client client;

    private DataProvider dataProvider;
    private ClientStatus initialStatus;
    private ClientStatus newStatus;

    public ClientWindow(Client client) {
        this.dataProvider = DashboardUI.getDataProvider();
        this.client = client;
        configureComponents();
        name.setValue(client.getName());
        phone.setValue(client.getPhone());
        email.setValue(client.getEmail());
        date.setValue(client.getDate());
    }

    private void configureComponents() {
        addStyleName("profile-window");
        setId(ID);

        Collection<ClientStatus> statuses = DashboardUI.getDataProvider().getAllStatuses();
        Collection<ClientStatusHistory> statusesForUser = dataProvider.getStatusesForClient(client);
        final ImmutableMap<String, ClientStatus> statusToHistory = Maps.uniqueIndex(statuses, new Function<ClientStatus, String>() {
            @Override
            public String apply(ClientStatus input) {
                return input.getName();
            }
        });
        setModal(true);
        setCloseShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        setClosable(true);

        Responsive.makeResponsive(this);
        setHeight(300.0f, Unit.MM);
        setWidth(230.0f, Unit.MM);

        VerticalLayout root = new VerticalLayout();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setMargin(new MarginInfo(false, false, false, true));
        setContent(root);

        VerticalLayout content = new VerticalLayout();
        content.setMargin(new MarginInfo(false, true, false, false));

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);
        actions.setMargin(new MarginInfo(true, true, true, true));
        actions.setComponentAlignment(save, Alignment.BOTTOM_RIGHT);
        date.setDateFormat("dd/MM/yyyy");
        Label section = new Label("Информация");
        section.addStyleName(ValoTheme.LABEL_H4);
        section.addStyleName(ValoTheme.LABEL_COLORED);
        content.addComponents(section, name, phone, email, date);

        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ClientWindow.this.close();
            }
        });
        save.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // Bind the properties of the client POJO to fiels in this form
                client.setPhone(phone.getValue());
                client.setName(name.getValue());
                client.setEmail(email.getValue());
                ClientStatus value = (ClientStatus) status.getValue();
                client.setStatus(value.getName());

                if (!newStatus.equals(initialStatus)) {
                    ClientStatusHistory newClientStatus = new ClientStatusHistory();
                    newClientStatus.setClientid(client.getId());
                    newClientStatus.setDescription(description.getValue());
                    newClientStatus.setStatusid(value.getId());
                    newClientStatus.setName(value.getName());
                    dataProvider.updateClientStatus(newClientStatus);
                }
                ClientWindow.this.close();
                DashboardEventBus.post(new DashboardEvent.ClientUpdatedEvent());
            }
        });
        status.setNewItemsAllowed(false);
        status.setImmediate(true);
        status.addItems(statuses);
        status.setNullSelectionAllowed(false);


        if (statusesForUser.size() > 0) {
            ClientStatusHistory next = statusesForUser.iterator().next();
            ClientStatus clientStatus = statusToHistory.get(next.getName());
            initialStatus = clientStatus;
            status.select(clientStatus);
        }

        Table table = new Table() {
            @Override
            protected String formatPropertyValue(final Object rowId,
                                                 final Object colId, final Property<?> property) {
                String result = super.formatPropertyValue(rowId, colId,
                        property);
                if (colId.equals("Дата")) {
                    result = new SimpleDateFormat("dd/MM/yyyy").format(((Date) property.getValue()));
                }
                return result;
            }
        };

        table.addContainerProperty("Статус", String.class, null);
        table.addContainerProperty("Дата", Date.class, null);
        table.addContainerProperty("Описание", String.class, null);

        for (ClientStatusHistory clientStatusHistory : statusesForUser) {
            table.addItem(new Object[]{
                    clientStatusHistory.getName(),
                    clientStatusHistory.getTimestamp(),
                    clientStatusHistory.getDescription()
            }, clientStatusHistory.getId());
        }

        table.setPageLength(5);
        section = new Label("История изменений статусов");
        section.addStyleName(ValoTheme.LABEL_H4);
        section.addStyleName(ValoTheme.LABEL_COLORED);
        horizontalLayout.addComponent(section);
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(section);
        verticalLayout.addComponent(table);
        section = new Label("Изменить статус клиента");
        section.addStyleName(ValoTheme.LABEL_H4);
        section.addStyleName(ValoTheme.LABEL_COLORED);
        verticalLayout.addComponent(section);

        description.setRows(3);
        description.setMaxLength(150);
        HorizontalLayout statusPane = new HorizontalLayout(status, description);
        statusPane.setSpacing(true);
        verticalLayout.addComponent(statusPane);
        horizontalLayout.addComponents(content, verticalLayout);
        root.addComponents(horizontalLayout, actions);
        status.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                newStatus = (ClientStatus) event.getProperty().getValue();
            }
        });
    }
}
