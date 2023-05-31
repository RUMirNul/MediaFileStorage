package edu.rumirnul.mediafilestorage.entity;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

/**
 * Класс Entity для сохранения информации в БД.
 *
 * @author Alexey Svistunov
 * @version 1.0
 */

@Entity
@Table(name = "file")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileData {
    /** Уникальный идентификатор. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Сгенерированное уникальное имя файла для хранения в хранилище. */
    @Column(name = "file_name", unique = true)
    private String fileName = UUID.randomUUID().toString();
    /** Оригинальное имя файла. */
    @Column(name = "original_name")
    private String originalName;
    /** Расширение файла. */
    @Column(name = "extension")
    private String extension;
}