package br.inf.jjd.embeddedkafka;

import br.inf.jjd.embeddedkafka.adapter.in.kafka.ContactEventConsumer;
import br.inf.jjd.embeddedkafka.adapter.kafka.event.dto.Contact;
import br.inf.jjd.embeddedkafka.adapter.out.entity.ContactModel;
import br.inf.jjd.embeddedkafka.adapter.out.entity.ContactPortImpl;
import br.inf.jjd.embeddedkafka.adapter.out.entity.jpa.ContactsRepository;
import br.inf.jjd.embeddedkafka.app.dto.ContactDTO;
import br.inf.jjd.embeddedkafka.service.ContactsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(topics = {"user-event"}, partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.value-serializer=br.inf.jjd.embeddedkafka.mock.CustomKafkaAvroSerializer",
        "spring.kafka.producer.properties.schema.registry.url=none",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.value-deserializer=br.inf.jjd.embeddedkafka.mock.CustomKafkaAvroDeserializer",
        "spring.kafka.consumer.properties.schema.registry.url=none",
})
public class KafkaConsumerIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<String, Contact> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @SpyBean
    ContactEventConsumer contactEventConsumerSpy;

    @SpyBean
    ContactsService contactsServiceSpy;

    @SpyBean
    ContactPortImpl contactPortSpy;

    @Autowired
    ContactsRepository contactsRepository;

    @BeforeEach
    void setUp() {

        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @AfterEach
    void tearDown() {
        contactsRepository.deleteAll();
    }

    @Test
    void publishNewLibraryEvent() throws ExecutionException, InterruptedException {
        //given
        Contact contact = Contact.newBuilder()
                .setName("Nome")
                .setPhoneNumber("(11) 12345-1234")
                .build();
        kafkaTemplate.sendDefault(contact).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(contactEventConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class),isA(Acknowledgment.class));
        verify(contactsServiceSpy, times(1)).processLibraryEvent(isA(ContactDTO.class));
        verify(contactPortSpy, times(1)).save(isA(ContactDTO.class));

        List<ContactModel> contactsList = (List<ContactModel>) contactsRepository.findAll();
        assert contactsList.size() ==1;
        contactsList.forEach(c -> {
            assert c.getName()!=null;
            Assertions.assertEquals("Nome", c.getName());
        });


    }
}
