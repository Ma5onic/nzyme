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

package horse.wtf.nzyme.dot11.handlers;

import com.google.common.base.Strings;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Dot11BeaconFrameHandler extends Dot11FrameHandler<Dot11BeaconFrame> {

    private static final Logger LOG = LogManager.getLogger(Dot11BeaconFrameHandler.class);

    public Dot11BeaconFrameHandler(Dot11Probe probe) {
        super(probe);
    }

    @Override
    protected void doHandle(Dot11BeaconFrame beacon) {
        String message;
        if (!Strings.isNullOrEmpty(beacon.ssid())) {
            message = "Received beacon from " + beacon.transmitter() + " for SSID " + beacon.ssid();
        } else {
            // Broadcast beacon.
            message = "Received broadcast beacon from " + beacon.transmitter();
        }

        Dot11MetaInformation meta = beacon.meta();

        probe.notifyUplinks(
                new Notification(message, beacon.meta().getChannel())
                        .addField(FieldNames.TRANSMITTER, beacon.transmitter())
                        .addField(FieldNames.TRANSMITTER_FINGERPRINT, beacon.transmitterFingerprint())
                        .addField(FieldNames.SSID, Strings.isNullOrEmpty(beacon.ssid()) ? "[no SSID]" : beacon.ssid())
                        .addField(FieldNames.SECURITY_FULL, beacon.taggedParameters().getFullSecurityString())
                        .addField(FieldNames.IS_WPA1, beacon.taggedParameters().isWPA1())
                        .addField(FieldNames.IS_WPA2, beacon.taggedParameters().isWPA2())
                        .addField(FieldNames.IS_WPS, beacon.taggedParameters().isWPS())
                        .addField(FieldNames.SUBTYPE, "beacon"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "beacon";
    }

}
