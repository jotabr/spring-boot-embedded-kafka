package br.inf.jjd.embeddedkafka.app.port.in;

import br.inf.jjd.embeddedkafka.app.dto.ContactDTO;

public interface ContactKafkaEventPort {

    void processLibraryEvent(ContactDTO contactDTO);

}
