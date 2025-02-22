package com.tyv.customerservice.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private String title;
    private long length;
    private String contentType;
    private byte[] data;
}
