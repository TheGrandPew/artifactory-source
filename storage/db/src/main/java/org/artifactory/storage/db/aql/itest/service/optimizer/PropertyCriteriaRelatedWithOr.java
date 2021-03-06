/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.db.aql.itest.service.optimizer;

import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.query.aql.*;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gidi Shabat
 */
public class PropertyCriteriaRelatedWithOr extends OptimizationStrategy {
    private final Pattern pattern = Pattern.compile("([c,p,k,v]o)+[c,p,k,v]");

    /**
     * Foe each sequence of criterias with properties connected with "OR" the method replaces the tables in the
     * criterias to only one table (from the the first criteria).
     * Note that this action is legal since we never mix tables that are separated by brackets and in AQL each block
     * might contain one of the following combination of criterias tables
     * 1. all property criterias are on the same table as result of "$msp" (Not include criterias in internal blocks)
     * 2. all property criterias are on the result table as result of "$rf" (Not include criterias in internal blocks)
     * 3. all property criterias are dynamic (contains different property tables)
     * The only case that wil be effected by this method is case (3) and this is exactly the case that we want to optimize
     *
     * @param aqlQuery AqlQuery<T>
     */
    @Override
    public <T extends AqlRowResult> void optimize(AqlQuery<T> aqlQuery, String transformation) {
        // Try to find matching sub strings
        Matcher matcher = pattern.matcher(transformation);
        // Each match represent sub query that can be optimize
        while (matcher.find()) {
            // Get sub query bounds
            int start = matcher.start();
            int end = matcher.end();
            // The start index must point on criteria according to the pattern
            SqlTable table = getFirstPropertyTable(aqlQuery, transformation, start, end);
            if (table == null) {
                continue;
            }
            // Scan the sub query and replace the tables in "CRITERIAS WITH PROPERTIES" to the first table we just found
            // Note after replacing all the tables to a single table the SQL generator will bind all criterias to a single table
            // and no inner join will be generated for this criterias.
            for (int i = start; i < end; i++) {
                if (transformation.charAt(i) == 'p') {
                    ComplexPropertyCriterion criteria = (ComplexPropertyCriterion) aqlQuery.getAqlElements().get(i);
                    ComplexPropertyCriterion newCriteria = new ComplexPropertyCriterion(criteria.getSubDomains(),
                            criteria.getVariable1(), table, criteria.getComparatorName(), criteria.getVariable2(),
                            table, criteria.isMspOperator());
                    aqlQuery.getAqlElements().remove(i);
                    aqlQuery.getAqlElements().add(i, newCriteria);

                }
                if (transformation.charAt(i) == 'k' || transformation.charAt(i) == 'v') {
                    Criterion criterion = (Criterion) aqlQuery.getAqlElements().get(i);
                    Criterion newCriterion = null;
                    if (criterion instanceof SimpleCriterion) {
                        newCriterion = new SimpleCriterion(criterion.getSubDomains(),
                                criterion.getVariable1(), table, criterion.getComparatorName(), criterion.getVariable2(),
                                table, criterion.isMspOperator());
                    }
                    if (criterion instanceof SimplePropertyCriterion) {
                        newCriterion = new SimplePropertyCriterion(criterion.getSubDomains(),
                                criterion.getVariable1(), table, criterion.getComparatorName(), criterion.getVariable2(),
                                table, criterion.isMspOperator());
                    }
                    aqlQuery.getAqlElements().remove(i);
                    aqlQuery.getAqlElements().add(i, newCriterion);
                }
            }
        }
    }

    private <T extends AqlRowResult> SqlTable getFirstPropertyTable(AqlQuery<T> aqlQuery, String transformation, int start, int end) {
        for (int i = start; i < end; i++) {
            if (transformation.charAt(i) == 'p' || transformation.charAt(i) == 'k' || transformation.charAt(i) == 'v') {
                Criterion criterion = (Criterion) aqlQuery.getAqlElements().get(i);
                return criterion.getTable1();
            }
        }
        return null;
    }
}
