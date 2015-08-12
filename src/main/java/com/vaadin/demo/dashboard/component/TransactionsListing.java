package com.vaadin.demo.dashboard.component;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;

import com.vaadin.demo.dashboard.domain.Client;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class TransactionsListing extends CssLayout {

    public TransactionsListing(final Collection<Client> clients) {
        addComponent(new Label("<strong>Selected transactions</strong>",
                ContentMode.HTML));

        if (clients != null) {
            for (Client client : clients) {
                CssLayout transationLayout = new CssLayout();
                transationLayout.addStyleName("transaction");

                DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Label content = new Label(df.format((client.getDate()))
                        + "<br>" + client.getCity() + ", "
                        + client.getName());
                content.setSizeUndefined();
                content.setContentMode(ContentMode.HTML);
                content.addStyleName("time");
                transationLayout.addComponent(content);

                content = new Label(client.getStatus());
                content.setSizeUndefined();
                content.addStyleName("movie-title");
                transationLayout.addComponent(content);

                content = new Label("Seats: "
                        + 0
                        + "<br>"
                        + "Revenue: $"
                        + new DecimalFormat("#.##").format(1), ContentMode.HTML);
                content.setSizeUndefined();
                content.addStyleName("seats");
                transationLayout.addComponent(content);

                addComponent(transationLayout);
            }

        }
    }

}
