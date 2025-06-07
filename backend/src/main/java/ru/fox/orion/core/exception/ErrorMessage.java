package ru.fox.orion.core.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {

    PERMISSION_DELETED("permission.deleted"),
    PERMISSION_ALREADY_USE("permission.already.use"),
    PERMISSION_SESSION_CONCURRENT_REQUEST("permission.session.concurrent.request"),
    PERMISSION_UPDATE_CONCURRENT_REQUEST("permission.update.concurrent.request"),
    PERMISSION_SESSION_ALREADY_EXIST("permission.session.already.exist"),
    PERMISSION_ESIA_NOT_FOUND("permission.esia.not.found"),
    PERMISSION_ALREADY_RECEIVED("permission.already.received"),
    PERMISSION_NOT_FOUND("permission.not.found"),
    PERMISSION_TYPE_NOT_FOUND("permission.type.not.found"),

    FACEPAY_PHOTO_ERROR("facepay.photo.error"),
    PASSENGER_LKP_BAD_REQUEST("lkp.api.bad.request"),
    ACCOUNT_DELETED("account.deleted"),
    ACCOUNT_NOT_FOUND("account.not.found"),

    CARD_NOT_FOUND("card.not.found"),

    UNAUTHORIZED("unauthorized"),
    METHOD_NOT_ALLOWED("method.not.allowed"),
    NOT_FOUND("not.found"),
    CONFLICT_EXCEPTION("conflict.exception"),
    RESOURCE_GONE_EXCEPTION("resource.no.longer.available"),
    SERVICE_UNAVAILABLE("service.unavailable"),
    BAD_PARAM("bad.param"),
    HEADER_NOT_PRESENT("header.not.present"),
    ACCESS_DENIED("access.denied"),
    PARAM_NOT_PRESENT("param.not.present"),
    SERIALIZED_EXCEPTION("serialized.exception");

    private final String errorMessageKey;

    ErrorMessage(String errorMessageKey) {
        this.errorMessageKey = errorMessageKey;
    }

}
