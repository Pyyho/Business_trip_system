package ru.pkc.travel.exception;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String entity, long id) {
        super(entity + " с ID " + id + " не найдена.");
    }
}
