/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.storm.kafka.spout.trident;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.storm.kafka.spout.SpoutWithMockedConsumerSetupHelper;
import org.json.simple.JSONValue;
import org.junit.Test;

public class KafkaTridentSpoutBatchMetadataTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void testMetadataIsRoundTripSerializableWithJsonSimple() throws Exception {
        /**
         * Tests that the metadata object can be converted to and from a Map. This is needed because Trident metadata is written to
         * Zookeeper as JSON with the json-simple library, so the spout converts the metadata to Map before returning it to Trident.
         * It is important that all map entries are types json-simple knows about,
         * since otherwise the library just calls toString on them which will likely produce invalid JSON.
         */
        long startOffset = 10;
        long endOffset = 20;

        KafkaTridentSpoutBatchMetadata metadata = new KafkaTridentSpoutBatchMetadata(startOffset, endOffset);
        Map<String, Object> map = metadata.toMap();
        Map deserializedMap = (Map)JSONValue.parseWithException(JSONValue.toJSONString(map));
        KafkaTridentSpoutBatchMetadata deserializedMetadata = KafkaTridentSpoutBatchMetadata.fromMap(deserializedMap);
        assertThat(deserializedMetadata.getFirstOffset(), is(metadata.getFirstOffset()));
        assertThat(deserializedMetadata.getLastOffset(), is(metadata.getLastOffset()));
    }

    @Test
    public void testCreateMetadataFromRecords() {
        long firstOffset = 15;
        long lastOffset = 55;
        List<ConsumerRecord<String, String>> records = SpoutWithMockedConsumerSetupHelper.createRecords(new TopicPartition("test", 0), firstOffset, (int) (lastOffset - firstOffset + 1));

        KafkaTridentSpoutBatchMetadata metadata = new KafkaTridentSpoutBatchMetadata(records);
        assertThat("The first offset should be the first offset in the record set", metadata.getFirstOffset(), is(firstOffset));
        assertThat("The last offset should be the last offset in the record set", metadata.getLastOffset(), is(lastOffset));
    }

}
