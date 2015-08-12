package com.vaadin.demo.dashboard.view.transactions;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;

import com.vaadin.demo.dashboard.event.DashboardEvent;
import com.vaadin.event.ItemClickEvent;
import org.vaadin.maddon.FilterableListContainer;

import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.demo.dashboard.domain.Client;
import com.vaadin.demo.dashboard.event.DashboardEvent.BrowserResizeEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.TransactionReportEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.view.DashboardViewType;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({"serial", "unchecked"})
public final class ClientsView extends VerticalLayout implements View {

    private final Table table;
    private Button btEditClient;
    private static final String[] DEFAULT_COLLAPSIBLE = {"date", "name", "city", "phone", "email", "status"};

    private Client selectedClient;

    public ClientsView() {
        setSizeFull();
        addStyleName("transactions");
        DashboardEventBus.register(this);

        addComponent(buildToolbar());

        table = buildTable();
        addComponent(table);
        setExpandRatio(table, 1);
    }

    @Override
    public void detach() {
        super.detach();
        // A new instance of TransactionsView is created every time it's
        // navigated to so we'll need to clean up references to it on detach.
        DashboardEventBus.unregister(this);
    }

    private Component buildToolbar() {
        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("viewheader");
        header.setSpacing(true);
        Responsive.makeResponsive(header);

        Label title = new Label("Последние клиенты");
        title.setSizeUndefined();
        title.addStyleName(ValoTheme.LABEL_H1);
        title.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        header.addComponent(title);

        btEditClient = editClient();
        HorizontalLayout tools = new HorizontalLayout(buildFilter(),
                btEditClient);
        tools.setSpacing(true);
        tools.addStyleName("toolbar");
        header.addComponent(tools);

        return header;
    }

    private Button editClient() {
        final Button editClient = new Button("Редактировать");
        editClient.setDescription("Редактирует выбранного клиента");
        editClient.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                // ContactForm is an example of a custom component class
                ClientWindow clientForm = new ClientWindow(selectedClient);
                UI.getCurrent().addWindow(clientForm);
                clientForm.focus();
            }
        });
        editClient.setEnabled(false);
        return editClient;
    }

    private Component buildFilter() {
        final TextField filter = new TextField();
        filter.addTextChangeListener(new TextChangeListener() {
            @Override
            public void textChange(final TextChangeEvent event) {
                Filterable data = (Filterable) table.getContainerDataSource();
                data.removeAllContainerFilters();
                data.addContainerFilter(new Filter() {
                    @Override
                    public boolean passesFilter(final Object itemId,
                                                final Item item) {

                        if (event.getText() == null
                                || event.getText().equals("")) {
                            return true;
                        }

                        return filterByProperty("name", item,
                                event.getText())
                                || filterByProperty("city", item,
                                event.getText())
                                || filterByProperty("status", item,
                                event.getText());

                    }

                    @Override
                    public boolean appliesToProperty(final Object propertyId) {
                        if (propertyId.equals("name")
                                || propertyId.equals("city")
                                || propertyId.equals("status")) {
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        filter.setInputPrompt("Filter");
        filter.setIcon(FontAwesome.SEARCH);
        filter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        filter.addShortcutListener(new ShortcutListener("Clear",
                KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(final Object sender, final Object target) {
                filter.setValue("");
                ((Filterable) table.getContainerDataSource())
                        .removeAllContainerFilters();
            }
        });
        return filter;
    }

    private Table buildTable() {
        final Table table = new Table() {
            @Override
            protected String formatPropertyValue(final Object rowId,
                                                 final Object colId, final Property<?> property) {
                String result = super.formatPropertyValue(rowId, colId,
                        property);
                if (colId.equals("date")) {
                    result = new SimpleDateFormat("dd/MM/yyyy").format(((Date) property.getValue()));
                }
                return result;
            }
        };
        table.setSizeFull();
        table.addStyleName(ValoTheme.TABLE_BORDERLESS);
        table.addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        table.addStyleName(ValoTheme.TABLE_COMPACT);
        table.setSelectable(true);

        table.setColumnCollapsingAllowed(true);
        table.setColumnReorderingAllowed(true);
        table.setContainerDataSource(new TempTransactionsContainer(DashboardUI
                .getDataProvider().getRecentClients(20)));
        table.setSortContainerPropertyId("date");
        table.setSortAscending(false);

        table.setVisibleColumns("date", "name", "city", "phone", "email",
                "status");
        table.setColumnHeaders("Дата", "ФИО", "Город", "Телефон", "Почта",
                "Статус");

        // Allow dragging items to the reports menu
        table.setDragMode(TableDragMode.MULTIROW);
        table.setMultiSelect(true);

        table.addActionHandler(new TransactionsActionHandler());

        table.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                if (table.getValue() instanceof Set) {
                    Set<Object> val = (Set<Object>) table.getValue();
                    btEditClient.setEnabled(val.size() > 0);
                }
            }
        });
        table.setImmediate(true);
        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                selectedClient = (Client) event.getItemId();
            }
        });
        return table;
    }

    private boolean defaultColumnsVisible() {
        boolean result = true;
        for (String propertyId : DEFAULT_COLLAPSIBLE) {
            if (table.isColumnCollapsed(propertyId) == Page.getCurrent()
                    .getBrowserWindowWidth() < 800) {
                result = false;
            }
        }
        return result;
    }

    @Subscribe
    public void userUpdated(final DashboardEvent.ClientUpdatedEvent event) {
        table.refreshRowCache();
    }

    @Subscribe
    public void browserResized(final BrowserResizeEvent event) {
        // Some columns are collapsed when browser window width gets small
        // enough to make the table fit better.
        if (defaultColumnsVisible()) {
            for (String propertyId : DEFAULT_COLLAPSIBLE) {
                table.setColumnCollapsed(propertyId, Page.getCurrent()
                        .getBrowserWindowWidth() < 800);
            }
        }
    }

    private boolean filterByProperty(final String prop, final Item item,
                                     final String text) {
        if (item == null || item.getItemProperty(prop) == null
                || item.getItemProperty(prop).getValue() == null) {
            return false;
        }
        String val = item.getItemProperty(prop).getValue().toString().trim()
                .toLowerCase();
        if (val.contains(text.toLowerCase().trim())) {
            return true;
        }
        return false;
    }

    void createNewReportFromSelection() {
        UI.getCurrent().getNavigator()
                .navigateTo(DashboardViewType.REPORTS.getViewName());
        DashboardEventBus.post(new TransactionReportEvent(
                (Collection<Client>) table.getValue()));
    }

    @Override
    public void enter(final ViewChangeEvent event) {
    }

    private class TransactionsActionHandler implements Handler {
        private final Action report = new Action("Договор");
        private final Action discard = new Action("Удалить");

        @Override
        public void handleAction(final Action action, final Object sender,
                                 final Object target) {
            if (action == report) {
                createNewReportFromSelection();
            } else if (action == discard) {
                Notification.show("Not implemented in this demo");
            }
        }

        @Override
        public Action[] getActions(final Object target, final Object sender) {
            return new Action[]{report, discard};
        }
    }

    private class TempTransactionsContainer extends
            FilterableListContainer<Client> {

        public TempTransactionsContainer(
                final Collection<Client> collection) {
            super(collection);
        }

        // This is only temporarily overridden until issues with
        // BeanComparator get resolved.
        @Override
        public void sort(final Object[] propertyId, final boolean[] ascending) {
            final boolean sortAscending = ascending[0];
            final Object sortContainerPropertyId = propertyId[0];
            Collections.sort(getBackingList(), new Comparator<Client>() {
                @Override
                public int compare(final Client o1, final Client o2) {
                    int result = 0;
                    if ("date".equals(sortContainerPropertyId)) {
                        result = o1.getDate().compareTo(o2.getDate());
                    } else if ("name".equals(sortContainerPropertyId)) {
                        result = o1.getName().compareTo(o2.getName());
                    } else if ("city".equals(sortContainerPropertyId)) {
                        result = o1.getCity().compareTo(o2.getCity());
                    } else if ("phone".equals(sortContainerPropertyId)) {
                        result = o1.getPhone().compareTo(o2.getPhone());
                    } else if ("email".equals(sortContainerPropertyId)) {
                        result = o1.getEmail().compareTo(o2.getEmail());
                    } else if ("status".equals(sortContainerPropertyId)) {
                        result = o1.getStatus().compareTo(o2.getStatus());
                    }

                    if (!sortAscending) {
                        result *= -1;
                    }
                    return result;
                }
            });
        }
    }
}
