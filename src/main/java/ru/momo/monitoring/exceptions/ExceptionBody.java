package ru.momo.monitoring.exceptions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExceptionBody {

    String message;

    Map<String, String> errors;

    public ExceptionBody(String message) {
        this.message = message;
    }

}
