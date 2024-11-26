package com.mobigen.dolphin.entity.local;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job")
public class JobEntity {
    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @GeneratedValue(generator = "uuid")
    private UUID id;
    @Enumerated(value = EnumType.STRING)
    private JobStatus status;
    @Column(name = "user_query", length = 4096)
    private String userQuery;
    @Column(name = "converted_query", length = 4096)
    private String convertedQuery;
    @Column
    private Integer offset_;
    @Column
    private Integer limit_;
    @CreationTimestamp
    private Timestamp created;
    @UpdateTimestamp
    private Timestamp updated;

    @Column(name = "result_name")
    private String resultName;
    @Column(name = "result_path")
    private String resultPath;

    public enum JobStatus {
        INIT,
        QUEUED,
        RUNNING,
        FINISHED,
        FAILED,
        CANCELED

//        DENIED,
//        ACCEPT,
//        SUCCEED,
    }

}
