package com.mobigen.dolphin.antlr;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.entity.local.FusionModelEntity;
import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.entity.openmetadata.EntityType;
import com.mobigen.dolphin.entity.openmetadata.OMTableEntity;
import com.mobigen.dolphin.exception.ErrorCode;
import com.mobigen.dolphin.exception.SqlParseException;
import com.mobigen.dolphin.repository.MixRepository;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mobigen.dolphin.util.Functions.convertKeywordName;

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
@RequiredArgsConstructor
public class SqlVisitor extends ModelSqlBaseVisitor<String> {
    private final JobEntity job;
    private final OpenMetadataRepository openMetadataRepository;
    private final MixRepository mixRepository;
    private final DolphinConfiguration dolphinConfiguration;
    private final List<ExecuteDto.ReferenceModel> referenceModels;

    private final char SPECIAL_CHAR = '"';
    private final Map<String, String> modelCache = new HashMap<>();
    private final List<String> modelAliases = new ArrayList<>();
    private final List<FusionModelEntity> usedModelHistory = new ArrayList<>();

    @Override
    public String visitErrorNode(ErrorNode node) {
        throw new SqlParseException(ErrorCode.INVALID_SQL, "ERROR: error node : " + node);
    }

    @Override
    public String visit(ParseTree tree) {
        return super.visit(tree);
    }

    @Override
    public String visitParse(ModelSqlParser.ParseContext ctx) {
        return visitSql_stmt(ctx.sql_stmt()).strip();
    }

    @Override
    public String visitSql_stmt(ModelSqlParser.Sql_stmtContext ctx) {
        String explain = ctx.K_EXPLAIN() == null ? "" : ctx.K_EXPLAIN().getText();

        return explain + " " + visitSelect_stmt(ctx.select_stmt()).strip();
    }

    @Override
    public String visitSelect_stmt(ModelSqlParser.Select_stmtContext ctx) {
        var selectCoreBuilder = new StringBuilder(visitSelect_core(ctx.select_core(0)));
        for (var i = 0; i < ctx.compound_operator().size(); i++) {
            selectCoreBuilder.append(visitCompound_operator(ctx.compound_operator(i)))
                    .append(visitSelect_core(ctx.select_core(i + 1)).strip());
        }
        var selectCore_ = selectCoreBuilder.toString();
        var orderBy_ = visitOrder_by_(ctx.order_by_());
        var limit_ = visitLimit_(ctx.limit_());
        return selectCore_.strip() + orderBy_ + limit_;
    }

    @Override
    public String visitCompound_operator(ModelSqlParser.Compound_operatorContext ctx) {
        String operator;
        if (ctx.K_UNION() != null) {
            if (ctx.K_ALL() != null) {
                operator = combineKeywordsAndPad(ctx.K_UNION().getText(), ctx.K_ALL().getText());
            } else {
                operator = combineKeywordsAndPad(ctx.K_UNION().getText());
            }
        } else if (ctx.K_INTERSECT() != null) {
            operator = combineKeywordsAndPad(ctx.K_INTERSECT().getText());
        } else {
            operator = combineKeywordsAndPad(ctx.K_EXCEPT().getText());
        }
        return operator;
    }

    @Override
    public String visitSelect_core(ModelSqlParser.Select_coreContext ctx) {
        // visit 순서 제어
        String from_ = visitFrom_(ctx.from_());
        String select = visitSelect_(ctx.select_());
        String where_ = visitWhere_(ctx.where_());
        String groupBy_ = visitGroup_by_(ctx.group_by_());
        return select + from_ + where_ + groupBy_;
    }

    @Override
    public String visitSelect_(ModelSqlParser.Select_Context ctx) {
        if (ctx == null) {
            return "";
        }
        return combineKeywordsAndPad(ctx.K_SELECT().getText())
                + ctx.result_column().stream().map(this::visitResult_column)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String visitFunction_arguments(ModelSqlParser.Function_argumentsContext ctx) {
        return ctx.children.stream()
                .map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitFrom_(ModelSqlParser.From_Context ctx) {
        if (ctx == null) {
            return "";
        } else if (ctx.table_or_subquery() != null) {
            return " from " + visitTable_or_subquery(ctx.table_or_subquery());
        } else if (ctx.join_clause() != null) {
            return " from " + visitJoin_clause(ctx.join_clause());
        }
        throw new SqlParseException(ErrorCode.INVALID_SQL, "ERROR: error rule : " + ModelSqlParser.ruleNames[ctx.getRuleIndex()] + ", parts: " + ctx.getText());
    }

    @Override
    public String visitWhere_(ModelSqlParser.Where_Context ctx) {
        if (ctx == null) {
            return "";
        }
        return " where " + visitExpr(ctx.expr());
    }

    @Override
    public String visitGroup_by_(ModelSqlParser.Group_by_Context ctx) {
        if (ctx == null) {
            return "";
        }
        return " " + ctx.children.stream()
                .map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitOrder_by_(ModelSqlParser.Order_by_Context ctx) {
        if (ctx == null) {
            return "";
        }
        return " order by " + ctx.ordering_term().stream()
                .map(this::visitOrdering_term)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String visitOrdering_term(ModelSqlParser.Ordering_termContext ctx) {
        return ctx.children.stream()
                .map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitLimit_(ModelSqlParser.Limit_Context ctx) {
        if (ctx == null) {
            return "";
        }
        var builder = new StringBuilder();
        TerminalNode limitValue;
        TerminalNode offsetValue;
        if (ctx.COMMA() != null) {
            offsetValue = ctx.INTEGER_LITERAL(0);
            builder.append(" offset ")
                    .append(offsetValue);
            limitValue = ctx.INTEGER_LITERAL(1);
        } else if (ctx.K_OFFSET() != null) {
            offsetValue = ctx.INTEGER_LITERAL(1);
            builder.append(" offset ")
                    .append(offsetValue);
            limitValue = ctx.INTEGER_LITERAL(0);
        } else {
            limitValue = ctx.INTEGER_LITERAL(0);
        }
        builder.append(" limit ")
                .append(limitValue);

        return builder.toString();
    }

    @Override
    public String visitJoin_clause(ModelSqlParser.Join_clauseContext ctx) {
        return ctx.children.stream()
                .map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitJoin_operator(ModelSqlParser.Join_operatorContext ctx) {
        return ctx.children.stream()
                .map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitJoin_constraint(ModelSqlParser.Join_constraintContext ctx) {
        if (ctx.children == null) {
            throw new SqlParseException(ErrorCode.INVALID_SQL, "Expecting: 'ON' or 'USING' after join");
        }
        return ctx.children.stream()
                .map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitResult_column(ModelSqlParser.Result_columnContext ctx) {
        if (ctx.expr() != null) {
            var expr = visitExpr(ctx.expr());
            if (ctx.column_alias() != null) {
                return expr + " as " + visitColumn_alias(ctx.column_alias());
            }
            return expr;
        } else if (ctx.model_term() == null && ctx.STAR() != null) {
            return "*";
        } else {
            var alias = convertKeywordName(ctx.model_term().getText());
            if (modelAliases.contains(alias)) {
                return alias + "." + ctx.STAR();
            } else {
                return visitModel_term(ctx.model_term()) + "." + ctx.STAR();
            }
        }
    }

    @Override
    public String visitExpr(ModelSqlParser.ExprContext ctx) {
        return ctx.children.stream().map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitTable_or_subquery(ModelSqlParser.Table_or_subqueryContext ctx) {
        String result;
        if (ctx.model_term() != null) {  // 심플 모델명
            result = visitModel_term(ctx.model_term());
        } else if (ctx.select_stmt() != null) {
            result = "(" + visitSelect_stmt(ctx.select_stmt()) + ")";
        } else {
            result = "(" + visitJoin_clause(ctx.join_clause()) + ")";
        }
        if (ctx.table_alias() != null) {
            var alias = convertKeywordName(ctx.table_alias().getText());
            modelAliases.add(alias);
            result = result + " as " + alias;
        }
        return result;
    }

    private OMTableEntity getOpenMetadataTableEntityFromReferenceModel(String modelName) {
        // reference 모델 체크
        var token = ".*";
        var pattern = Pattern.compile(token + "\\." + token + "\\." + token + "\\." + token);
        if (pattern.matcher(modelName).matches()) {
            return openMetadataRepository.getTableOrContainer(modelName);
        }
        var i = 0;
        ExecuteDto.ReferenceModel matched = null;
        for (var referenceModel : referenceModels) {
            if (modelName.equalsIgnoreCase(referenceModel.getName())) {
                matched = referenceModel;
                i++;
            }
        }
        if (i > 1) {
            throw new SqlParseException(ErrorCode.INVALID_SQL, "ERROR: duplicated reference model : " + modelName);
        }
        if (matched == null) {
            return null;
        }
        return matched.getFullyQualifiedName() != null ?
                openMetadataRepository.getTableOrContainer(matched.getFullyQualifiedName()) :
                openMetadataRepository.getTable(matched.getId());
    }

    @Override
    public String visitModel_term(ModelSqlParser.Model_termContext ctx) {
        String catalogName;
        String schemaName;
        String modelName;
        String fullTrinoModelName;
        OMTableEntity tableInfo;
        // 레퍼런스 모델 확인
        if (ctx.children.size() == 1) {
            // 모델 명만 입력
            modelName = visitAny_name(ctx.any_name(0));
            if (modelName.startsWith("\"") && modelName.endsWith("\"")) {
                modelName = modelName.substring(1, modelName.length() - 1);
            }
            if (modelCache.containsKey(modelName)) {
                return modelCache.get(modelName);
            }
            tableInfo = getOpenMetadataTableEntityFromReferenceModel(modelName);
        } else {
            // fqn 입력
            // keyword 가 포함된 fqn 을 사용할 때, backtick(내부에서 double-quote 로 변함) 을 사용하게 되는데,
            // fqn 을 이용해 조회 하기 위해 double-quote 를 제거
            var fqn = ctx.children.stream().map(x -> x.accept(this))
                    .collect(Collectors.joining())
                    .replace("\"", "");
            if (modelCache.containsKey(fqn)) {
                return modelCache.get(fqn);
            }
            tableInfo = openMetadataRepository.getTableOrContainer(fqn);
            modelName = tableInfo.getName();
        }
        // 쿼리 상 `from model` 을 이용해 트리노(hive view)에서 검색
        if (tableInfo == null) {
            catalogName = dolphinConfiguration.getModel().getCatalog();
            schemaName = dolphinConfiguration.getModel().getSchema().getDb();
            log.info("catalog : {} schema : {} modelName : {}", catalogName, schemaName, modelName);
            fullTrinoModelName = catalogName + "." + schemaName + "." + modelName;
            tableInfo = openMetadataRepository.getTableOrContainer(dolphinConfiguration.getModel().getOmTrinoDatabaseService() + "." + fullTrinoModelName);
        } else {
            // om 에서 정보를 가져옴
            if (tableInfo.getService().getName().equals(dolphinConfiguration.getModel().getOmTrinoDatabaseService())
                    && tableInfo.getDatabase().getName().equals(dolphinConfiguration.getModel().getCatalog())) {
                // dolphin(trino) 를 통해 만든 모델(view) 인경우
                catalogName = dolphinConfiguration.getModel().getCatalog();
                schemaName = dolphinConfiguration.getModel().getSchema().getDb();
            } else {
                // catalogName = Functions.getCatalogName(tableInfo.getService().getId());
                catalogName = mixRepository.getOrCreateTrinoCatalog(tableInfo.getService());
                if (tableInfo.getService().getType().equals(EntityType.DATABASE_SERVICE)) {
                    if ("postgres".equalsIgnoreCase(tableInfo.getServiceType())) {
                        schemaName = tableInfo.getDatabaseSchema().getName();
                    } else {
                        schemaName = tableInfo.getDatabaseSchema().getName();
                    }
                } else {
                    // Storage Service
                    schemaName = tableInfo.getFileFormats().getFirst();
                }
            }
        }
        if (tableInfo.getService().getType().equals(EntityType.STORAGE_SERVICE)) {
            modelName = '"' + tableInfo.getFullPath() + '"';
        } else {
            modelName = '"' + tableInfo.getName() + '"';
        }
        log.info("catalog : {} schema : {} modelName : {}", catalogName, schemaName, modelName);
        fullTrinoModelName = catalogName + "." + schemaName + "." + modelName;
        modelCache.put(modelName, fullTrinoModelName);
        usedModelHistory.add(FusionModelEntity.builder()
                .job(job)
                .modelIdOfOM(tableInfo.getId())
                .fullyQualifiedName(tableInfo.getFullyQualifiedName())
                .trinoModelName(fullTrinoModelName)
                .build());
        return fullTrinoModelName;
    }

    @Override
    public String visitColumn_term(ModelSqlParser.Column_termContext ctx) {
        var columnName = visitColumn_name(ctx.column_name());
        if (ctx.model_term() != null) {
            var alias = convertKeywordName(ctx.model_term().getText());
            if (modelAliases.contains(alias)) {
                columnName = alias + "." + columnName;
            } else {
                columnName = visitModel_term(ctx.model_term()) + "." + columnName;
            }
        }
        return columnName;
    }

    @Override
    public String visitColumn_name(ModelSqlParser.Column_nameContext ctx) {
        return visitAny_name(ctx.any_name());
    }

    @Override
    public String visitColumn_alias(ModelSqlParser.Column_aliasContext ctx) {
        return visitAny_name(ctx.any_name());
    }

    @Override
    public String visitAny_name(ModelSqlParser.Any_nameContext ctx) {
        return convertKeywordName(ctx.getText());
    }

    @Override
    public String visitInterval_term(ModelSqlParser.Interval_termContext ctx) {
        return ctx.children.stream().map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitTime_term(ModelSqlParser.Time_termContext ctx) {
        return ctx.children.stream().map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    private String combineKeywordsAndPad(String... keywords) {
        return " " + String.join(" ", keywords) + " ";
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        return node.getText();
    }

}
