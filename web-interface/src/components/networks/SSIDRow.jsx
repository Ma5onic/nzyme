import React from 'react';
import Reflux from 'reflux';

import numeral from "numeral";
import SSID from "./SSID";

class SSIDRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    _listFingerprints() {
        const fingerprints = this.props.channel.fingerprints;

        if (!fingerprints || fingerprints.length === 0) {
            return "n/a";
        }

        let abbv = "";

        let i = 0;
        fingerprints.forEach(function(f) {
            abbv += f;

            if (i !== fingerprints.length-1) {
                abbv += ", "
            }

            i++;
        });

        return abbv;
    }

    _printSecurity() {
        let x = "";

        const total = this.props.ssid.security.length;
        this.props.ssid.security.forEach(function(mode, ix) {
            x += mode.as_string;

            if(ix < total-1) {
                x += ", ";
            }
        });

        if (!x) {
            return (
                <span className="text-warning">None</span>
            )
        }

        return x;
    }

    render() {
        const c = this.props.channel;

        return (
            <tr>
                <td><SSID ssid={this.props.ssid} /></td>
                <td><strong>{this.props.channelNumber}</strong></td>
                <td>{numeral(c.total_frames).format('0,0')}</td>
                <td>{this._printSecurity()}</td>
                <td>{numeral(this.props.channel.signal_index).format("0.00")} (THOLD: 25.12)</td>
                <td className={c.fingerprints.length > 2 ? "text-danger" : ""}>
                    1 <a href="#">Show All</a>
                </td>
            </tr>
        )
    }

}

export default SSIDRow;