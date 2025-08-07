package com.booking.common_library.dto;


import com.booking.common_library.util.SuccessCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder(builderMethodName = "genericBuilder")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse <T> {

    private int statusCode;
    private Object message;
    private T data;

    public static <T> ApiResponse<T> builderResponse(SuccessCode successCode, T data) {
        return ApiResponse.<T>genericBuilder()
                .statusCode(successCode.getCode())
                .message(successCode.getMessage())
                .data(data)
                .build();
    }

}
