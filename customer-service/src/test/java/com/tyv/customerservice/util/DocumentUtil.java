package com.tyv.customerservice.util;

import com.tyv.customerservice.dto.DocumentDto;
import org.springframework.http.MediaType;

public class DocumentUtil {

    static public DocumentDto createResponseDto() {
        String data = "Some data body";
        return DocumentDto.builder()
                .title("title.txt")
                .length(data.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .data(data.getBytes())
                .build();
    }
}
