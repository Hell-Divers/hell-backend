package com.hell.backend.users.exception;

//이메일 중복과 같은 비즈니스 로직 검증시 예외 처리
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {

        super(message);
    }
}
