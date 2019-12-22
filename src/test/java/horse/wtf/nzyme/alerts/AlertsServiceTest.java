package horse.wtf.nzyme.alerts;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class AlertsServiceTest extends AlertTestHelper {

    // TODO: updating last_seen works for sameAs alerts
    // TODO: each alert type can be serialized from db

    @Test
    public void testRetentionCleaning() {
        Nzyme nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute(CLEAR_QUERY));

        AlertsService as = new AlertsService(
                nzyme,
                1,
                TimeUnit.SECONDS,
                2,
                TimeUnit.SECONDS
        );

        as.handle(UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        assertEquals(as.getActiveAlerts().size(), 1);

        as.handle(UnexpectedChannelBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        assertEquals(as.getActiveAlerts().size(), 2);

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {}

        assertEquals(as.getActiveAlerts().size(), 1);

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {}

        assertEquals(as.getActiveAlerts().size(), 0);
    }

    @Test
    public void testSameAlertsAreNotDuplicatedAndLastSeenIsUpdated() {
        Nzyme nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute(CLEAR_QUERY));

        AlertsService as = new AlertsService(
                nzyme,
                100,
                TimeUnit.MILLISECONDS,
                10,
                TimeUnit.MINUTES
        );

        as.handle(UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        assertEquals(as.getActiveAlerts().size(), 1);
        Alert a1 = new ArrayList<>(as.getActiveAlerts().values()).get(0);
        DateTime lastSeen = a1.getLastSeen();
        assertNotNull(a1.getLastSeen());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        as.handle(UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        assertEquals(as.getActiveAlerts().size(), 1);
        Alert a1a = new ArrayList<>(as.getActiveAlerts().values()).get(0);
        assertNotNull(a1a.getLastSeen());
        assertNotEquals(lastSeen, a1a.getLastSeen());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetActiveAlertsReturnsImmutableCopyPut() {
        Nzyme nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute(CLEAR_QUERY));

        new AlertsService(nzyme).getActiveAlerts().put(UUID.randomUUID(), UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetActiveAlertsReturnsImmutableCopyRemove() {
        new AlertsService(new MockNzyme()).getActiveAlerts().remove(UUID.randomUUID());
    }

    @Test
    public void testSetsUUID() {
        Nzyme nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute(CLEAR_QUERY));

        AlertsService as = new AlertsService(nzyme);

        as.handle(UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        Alert alert = Lists.newArrayList(as.getActiveAlerts().values()).get(0);
        assertNotNull(alert);
        assertFalse(Strings.isNullOrEmpty(alert.getUUID().toString()));
    }

    @Test
    public void testUplinkConnection() {
        Nzyme nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute(CLEAR_QUERY));

        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics(nzyme));
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        AlertsService as = new AlertsService(nzyme);
        as.handle(UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        assertNotNull(loopback.getLastAlert());
        assertEquals(loopback.getLastAlert().getMessage(), "Our BSSID [00:c0:ca:95:68:3b] advertised unexpected SSID [wtf] with beacon frame.");
        assertEquals(loopback.getLastAlert().getClass(), UnexpectedSSIDBeaconAlert.class);
    }

}