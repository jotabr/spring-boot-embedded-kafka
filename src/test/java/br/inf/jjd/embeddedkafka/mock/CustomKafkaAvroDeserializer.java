package br.inf.jjd.embeddedkafka.mock;

import br.inf.jjd.embeddedkafka.adapter.kafka.event.dto.Contact;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.Schema;

/**
 * This code is not thread safe and should not be used in production environment
 */
public class CustomKafkaAvroDeserializer extends KafkaAvroDeserializer {
    @Override
    public Object deserialize(String topic, byte[] bytes) {
        if (topic.equals("user-event")) {
            this.schemaRegistry = getMockClient(Contact.SCHEMA$);
        }
        /*if (topic.equals("library-events")) {
            this.schemaRegistry = getMockClient(Event1.SCHEMA$);
        }*/
        return super.deserialize(topic, bytes);
    }

    private static SchemaRegistryClient getMockClient(final Schema schema$) {
        return new MockSchemaRegistryClient() {
            @Override
            public synchronized Schema getById(int id) {
                return schema$;
            }
        };
    }
}
