import React from 'react';
import Reflux from 'reflux';

class CreateIdentifierPage extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Create Identifier</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-9">
                        form
                    </div>

                    <div className="col-md-3">
                        <div className="alert alert-info">
                            <h3>Help</h3>
                            <p>
                                Identifiers are attributes that describe a bandit. For example, you could use identifiers
                                to describe a bandit that probes for two specific SSIDs, is always detected using a weak
                                signal strength and also only uses specific channels.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        )
    }

}

export default CreateIdentifierPage;