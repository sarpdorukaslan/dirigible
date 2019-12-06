/**
 * Copyright (c) 2010-2019 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP - initial API and implementation
 */
package org.eclipse.dirigible.engine.odata2.sql.builder.expression;

import static org.apache.olingo.odata2.api.commons.HttpStatusCodes.INTERNAL_SERVER_ERROR;
import static org.eclipse.dirigible.engine.odata2.sql.utils.OData2Utils.fqn;

import java.util.Collections;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmStructuralType;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.eclipse.dirigible.engine.odata2.sql.api.OData2Exception;
import org.eclipse.dirigible.engine.odata2.sql.builder.SQLContext;
import org.eclipse.dirigible.engine.odata2.sql.builder.SQLQuery;

public final class SQLExpressionJoin implements SQLExpression {

    private static final List<KeyPredicate> NO_PREDICATES_USED = Collections.emptyList();

    private final EdmStructuralType start;
    private final EdmStructuralType target;
    private final String startFqn;
    private final String targetFqn;
    private final JoinType joinType;

    /**
     * The left join should be used for expands - we would like to get the
     * properties of an entity even if the expand (lik) or complex type is
     * empty.
     */
    public static enum JoinType {
        LEFT
    };

    private final SQLQuery query;
    private List<KeyPredicate> keyPredicates = NO_PREDICATES_USED;

    public SQLExpressionJoin(final SQLQuery query, final EdmStructuralType start, final EdmStructuralType target) {
        this.start = start;
        this.target = target;
        this.query = query;
        this.startFqn = fqn(start);
        this.targetFqn = fqn(target);
        this.joinType = JoinType.LEFT;
    }

    @Override
    public String evaluate(SQLContext context, ExpressionType type) throws EdmException {
        if (type != ExpressionType.JOIN || isEmpty()) {
            return EMPTY_STRING;
        }

        StringBuilder join = new StringBuilder();
        join.append(joinType.toString());
        join.append(" JOIN ");
        join.append(getTableName(start));
        join.append(" AS ");
        join.append(query.getSQLTableAlias(start));
        join.append(" ON ");
        join.append(query.getSQLTableAlias(start));
        join.append(".");
        join.append(getTargetJoinKeyForEntityType(start, target));
        join.append(" = ");
        join.append(query.getSQLTableAlias(target));
        join.append(".");
        join.append(query.getSQLJoinTableName(target, start));
        return join.toString();
    }

    @Override
    public boolean isEmpty() throws EdmException {
        return !needsJoinQuery(start, keyPredicates, target);
    }

   

    @Override
    public String toString() {
        return "SQLJoin [startFqn=" + startFqn + ", targetFqn=" + targetFqn + ", joinType=" + joinType + "]";
    }

    // This Method is for internal use ONLY !!! Do NEVER use it !!!
    public SQLQuery with(List<KeyPredicate> keyPredicates) throws EdmException {
        if (this.keyPredicates != NO_PREDICATES_USED) {
            throw new OData2Exception("A where clause for the key predicates of this join epxression is already added!",
                    INTERNAL_SERVER_ERROR);
        }
        this.keyPredicates = keyPredicates;
        SQLExpressionWhere where = SQLExpressionUtils.whereClauseFromKeyPredicates(query, start, keyPredicates);
        query.and(where);
        return query;
    }

    private String getTableName(EdmStructuralType type) throws EdmException {
        return query.getSQLTableName(type);
    }

    private String getTargetJoinKeyForEntityType(EdmStructuralType start, EdmStructuralType end) throws EdmException {
        return query.getSQLJoinTableName(start, end);
    }

    private static boolean needsJoinQuery(EdmStructuralType start, List<KeyPredicate> startPredicates, EdmStructuralType target)
            throws EdmException {
        if (start.getName().equals(target.getName())) {
            return false;
        } else {
            if (startPredicates == NO_PREDICATES_USED) {
                return true;
            } else {
                return (startPredicates != null && !startPredicates.isEmpty()) ? true : false;
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((startFqn == null) ? 0 : startFqn.hashCode());
        result = prime * result + ((targetFqn == null) ? 0 : targetFqn.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SQLExpressionJoin other = (SQLExpressionJoin) obj;
        if (startFqn == null) {
            if (other.startFqn != null)
                return false;
        } else if (!startFqn.equals(other.startFqn))
            return false;
        if (targetFqn == null) {
            if (other.targetFqn != null)
                return false;
        } else if (!targetFqn.equals(other.targetFqn))
            return false;
        return true;
    }

    
}
