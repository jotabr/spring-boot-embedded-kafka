package br.inf.jjd.embeddedkafka.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class ContactDTO {

    private Integer id;
    private String name;
    private String phoneNumber;

}
