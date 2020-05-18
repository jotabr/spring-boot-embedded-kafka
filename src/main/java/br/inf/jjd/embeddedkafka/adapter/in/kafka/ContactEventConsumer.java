package br.inf.jjd.embeddedkafka.adapter.in.kafka;

import br.inf.jjd.embeddedkafka.adapter.kafka.event.dto.Contact;
import br.inf.jjd.embeddedkafka.app.dto.ContactDTO;
import br.inf.jjd.embeddedkafka.app.port.in.ContactKafkaEventPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ContactEventConsumer {

    @Autowired
    private ContactKafkaEventPort contactService;


    public void onMessage(ConsumerRecord<String, Contact> consumerRecord) {

        /*ContactDTO dto = ContactDTO.builder()
                .name(consumerRecord.value().getName().toString())
                .phoneNumber(consumerRecord.value().getPhoneNumber().toString())
                .build();

        log.info(this.getClass().getCanonicalName() + " called.");
        log.info("ConsumerRecord : {} ", consumerRecord);

        contactService.processLibraryEvent(dto);*/

        log.info("JEISON");

        //acknowledgment.acknowledge();
    }

    @KafkaListener(topics = {"user-event"})//, containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, Contact> consumerRecord, Acknowledgment acknowledgment) {
        log.info("ACKKKKKK");

        log.info(this.getClass().getCanonicalName() + " called.");

        GenericRecord record = (GenericRecord) consumerRecord.value();
        Contact contact = (Contact) SpecificData.get().deepCopy(Contact.SCHEMA$, record);
        ContactDTO dto = ContactDTO.builder()
                .name(contact.getName().toString())
                .phoneNumber(contact.getPhoneNumber().toString())
                .build();

        contactService.processLibraryEvent(dto);

        acknowledgment.acknowledge();
    }

}
