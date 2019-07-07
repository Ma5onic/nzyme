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

package horse.wtf.nzyme.dot11.networks.sigindex;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AverageSignalIndex {

    @JsonProperty("index")
    public abstract float index();

    @JsonProperty("had_enough_data")
    public abstract boolean hadEnoughData();

    @JsonProperty("in_training")
    public abstract boolean inTraining();

    public static AverageSignalIndex create(float index, boolean hadEnoughData, boolean inTraining) {
        return builder()
                .index(index)
                .hadEnoughData(hadEnoughData)
                .inTraining(inTraining)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AverageSignalIndex.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder index(float index);

        public abstract Builder hadEnoughData(boolean hadEnoughData);

        public abstract Builder inTraining(boolean inTraining);

        public abstract AverageSignalIndex build();
    }

}