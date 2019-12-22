package horse.wtf.nzyme.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.PwnagotchiAdvertisementAlert;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

public class PwnagotchiAdvertisementInterceptorTest extends InterceptorSetTest {

    @Test
    public void testInterceptor() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();
        Dot11MockProbe probe = buildMockProbe(nzyme);
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);
        PwnagotchiAdvertisementInterceptor interceptor = new PwnagotchiAdvertisementInterceptor(probe);

        assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>(){{ add(PwnagotchiAdvertisementAlert.class); }});
        interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));
        assertNull(loopback.getLastAlert());

        interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.PWNAGOTCHI_ADVERTISEMENT_BEACON_1_PAYLOAD, Frames.PWNAGOTCHI_ADVERTISEMENT_BEACON_1_HEADER, META_NO_WEP
        ));
        assertNotNull(loopback.getLastAlert());
        assertEquals(PwnagotchiAdvertisementAlert.class, loopback.getLastAlert().getClass());
    }

}