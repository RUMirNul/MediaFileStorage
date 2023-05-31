package edu.rumirnul.mediafilestorage.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Класс Event для работы с Spring Events.
 * @author Alexey Svistunov
 * @version 1.0
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class FileSaveFailedEvent {
    /** Имя файла. Используется для поиска информации о файле в БД. */
    private String name;
}