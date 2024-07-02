package com.mobigen.dolphin.repository.local;

import com.mobigen.dolphin.dto.response.RecommendModelDto;
import com.mobigen.dolphin.entity.local.FusionModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public interface FusionModelRepository extends JpaRepository<FusionModelEntity, Long> {
    @Query("SELECT new com.mobigen.dolphin.dto.response.RecommendModelDto(a.modelIdOfOM, a.fullyQualifiedName, count(a.id))" +
            " FROM FusionModelEntity a" +
            " where a.job in (Select b from JobEntity b where b.status = 'FINISHED' and b.id in (" +
            "   SELECT job.id " +
            "   FROM FusionModelEntity " +
            "   WHERE fullyQualifiedName = :fqn or modelIdOfOM = :modelId )" +
            " )" +
            " group by a.modelIdOfOM, a.fullyQualifiedName")
    List<RecommendModelDto> findAllByFullyQualifiedNameOrModelIdOfOM(@Param("fqn") String fullyQualifiedName, @Param("modelId") UUID modelId);
}
