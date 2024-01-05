package com.example.accountservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@Jacksonized
public class Notification {
  private String message;

  @Override
  public String toString() {
    return "Notification [message=" + message + "]";
  }

}
