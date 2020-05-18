package br.inf.jjd.embeddedkafka.service;

import br.inf.jjd.embeddedkafka.app.dto.ContactDTO;
import br.inf.jjd.embeddedkafka.app.port.in.ContactKafkaEventPort;
import br.inf.jjd.embeddedkafka.app.port.out.ContactSavePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ContactsService implements ContactKafkaEventPort {

    @Autowired
    ContactSavePort contactSavePort;

    @Override
    public void processLibraryEvent(ContactDTO contactDTO) {
        log.info(this.getClass().getCanonicalName() + " called.");

        contactSavePort.save(contactDTO);
    }

}
