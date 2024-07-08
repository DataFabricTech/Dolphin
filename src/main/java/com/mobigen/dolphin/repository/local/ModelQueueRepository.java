package com.mobigen.dolphin.repository.local;

import com.mobigen.dolphin.entity.local.ModelQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ModelQueueRepository extends JpaRepository<ModelQueueEntity, String> {
}
