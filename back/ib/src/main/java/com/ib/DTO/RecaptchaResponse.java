package com.ib.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RecaptchaResponse {

    private Boolean success;
    private String challenge_ts;
    private String hostname;
    private Double score;
    private String action;
}
