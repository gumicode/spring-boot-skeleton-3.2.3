package com.gumicode.database.member.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Converter
@RequiredArgsConstructor
public class PasswordConverter implements AttributeConverter<String, String> {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String convertToDatabaseColumn(final String raw) {
        if(StringUtils.hasText(raw)){
            return passwordEncoder.encode(raw);
        }
        return raw;
    }

    @Override
    public String convertToEntityAttribute(final String encrypt) {
        return encrypt;
    }
}
