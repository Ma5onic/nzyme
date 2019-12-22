/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.configuration.ConfigurationLoader;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.clients.Clients;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import liquibase.exception.LiquibaseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.security.Key;
import java.util.Collections;
import java.util.List;

public class MockNzyme implements Nzyme {

    private File loadFromResourceFile(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new RuntimeException("test config file does not exist in resources");
        }

        return new File(resource.getFile());
    }

    private final Statistics statistics;
    private final Configuration configuration;
    private final SystemStatus systemStatus;
    private final Networks networks;
    private final Clients clients;
    private final OUIManager ouiManager;
    private final MetricRegistry metricRegistry;
    private final AlertsService alertsService;
    private final Key signingKey;
    private final ObjectMapper objectMapper;
    private final Registry registry;
    private final Version version;
    private final Database database;
    private final List<Uplink> uplinks;

    public MockNzyme() {
        this.version = new Version();
        this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        try {
            this.configuration = new ConfigurationLoader(loadFromResourceFile("nzyme-test-complete-valid.conf"), false).get();
        } catch (ConfigurationLoader.InvalidConfigurationException | ConfigurationLoader.IncompleteConfigurationException | FileNotFoundException e) {
            throw new RuntimeException("Could not load test config file from resources.", e);
        }

        this.uplinks = Lists.newArrayList();

        this.database = new Database(configuration);
        try {
            this.database.initializeAndMigrate();
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }

        this.metricRegistry = new MetricRegistry();
        this.registry = new Registry();
        this.statistics = new Statistics(this);
        this.systemStatus = new SystemStatus();
        this.networks = new Networks(this);
        this.clients = new Clients(this);
        this.ouiManager = new OUIManager(this);
        this.alertsService = new AlertsService(this);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Networks getNetworks() {
        return networks;
    }

    @Override
    public Clients getClients() {
        return clients;
    }

    @Override
    public void registerUplink(Uplink uplink) {
        this.uplinks.add(uplink);
    }

    @Override
    public void notifyUplinks(Notification notification, Dot11MetaInformation meta) {
        for (Uplink uplink : uplinks) {
            uplink.notify(notification, meta);
        }
    }

    @Override
    public void notifyUplinksOfAlert(Alert alert) {
        for (Uplink uplink : uplinks) {
            uplink.notifyOfAlert(alert);
        }
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public MetricRegistry getMetrics() {
        return metricRegistry;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public List<Dot11Probe> getProbes() {
        return Collections.emptyList();
    }

    @Override
    public AlertsService getAlertsService() {
        return alertsService;
    }

    @Override
    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    @Override
    public OUIManager getOUIManager() {
        return ouiManager;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public Key getSigningKey() {
        return signingKey;
    }

    @Override
    public Version getVersion() {
        return version;
    }


}
