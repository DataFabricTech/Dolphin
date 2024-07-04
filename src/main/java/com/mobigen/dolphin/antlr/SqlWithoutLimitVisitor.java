package com.mobigen.dolphin.antlr;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.util.Pair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

/**
 * <pre>
 * ANTLR4 로 인식된 SQL 을
 * Trino SQL 로 변환 하기 위한 코드
 * <a href=" https://trino.io/docs/current/sql/select.html">trino SELECT query document</a>
 * </pre>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Getter
public class SqlWithoutLimitVisitor extends SqlVisitor {
    private Pair<Integer, Integer> pagination;

    public SqlWithoutLimitVisitor(JobEntity job, OpenMetadataRepository openMetadataRepository, DolphinConfiguration dolphinConfiguration, List<ExecuteDto.ReferenceModel> referenceModels) {
        super(job, openMetadataRepository, dolphinConfiguration, referenceModels);
    }

    @Override
    public String visitLimit_(ModelSqlParser.Limit_Context ctx) {
        if (ctx == null) {
            return "";
        }
        TerminalNode limitValue;
        TerminalNode offsetValue = null;
        if (ctx.COMMA() != null) {
            offsetValue = ctx.INTEGER_LITERAL(0);
            limitValue = ctx.INTEGER_LITERAL(1);
        } else if (ctx.K_OFFSET() != null) {
            offsetValue = ctx.INTEGER_LITERAL(1);
            limitValue = ctx.INTEGER_LITERAL(0);
        } else {
            limitValue = ctx.INTEGER_LITERAL(0);
        }
        int offset = 0;
        if (offsetValue != null) {
            offset = Integer.parseInt(offsetValue.getText());
        }
        int limit = Integer.parseInt(limitValue.getText());
        pagination = new Pair<>(offset, limit);
        return "";
    }
}
