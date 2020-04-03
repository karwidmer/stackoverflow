package com.landg.phoenix.paymentraise.master.batch.partition;

import com.landg.phoenix.dateutils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import com.landg.phoenix.paymentraise.common.constants.SQLConstants;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomPartitioner implements Partitioner {

    private JdbcOperations jdbcTemplate;

    private List<String> targetDateRange;

    public CustomPartitioner(DataSource dataSource, String timeTravelDate, int leadTime) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        log.debug("CustomPartitioner : timeTravelDate={} : leadTime={}", timeTravelDate, leadTime);
        this.targetDateRange = DateUtils.getCollectionTargetDate(timeTravelDate.trim(), leadTime);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        log.debug("CustomPartitioner : partition : incoming gridSize : {}", gridSize);
        if (this.jdbcTemplate == null) {
            log.error("*** CustomPartitioner : partition : jdbcTemplate is NULL ***");
            return null;
        }
        if (this.targetDateRange == null) {
            log.error("*** CustomPartitioner : partition : Target Date Range is NULL ***");
            return null;
        }

        String SQL_FULL_COUNT_QUERY = SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_CLAUSE_PART1
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_CLAUSE_PART2 + "'" + targetDateRange.get(0) + "'"
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_CLAUSE_PART3 + "'" + targetDateRange.get(1) + "'"
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_CLAUSE_PART4
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_CLAUSE_PART5
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_CLAUSE_PART6;

        int totalNonGroupCount = this.jdbcTemplate.queryForObject(SQL_FULL_COUNT_QUERY, Integer.class);
        log.debug("CustomPartitioner : partition : totalNonGroupCount {}", totalNonGroupCount);

        String SQL_GROUP_COUNT_QUERY = SQLConstants.PREMIUM_COMPONENT_SELECT_DISTINCT_COUNT_CLAUSE
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART1
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART2 + "'" + targetDateRange.get(0) + "'"
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART3 + "'" + targetDateRange.get(1) + "'"
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART4
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART5
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART6
                + SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_GROUP_BY_CLAUSE;

        int groupCountUid = this.jdbcTemplate.queryForObject(SQL_GROUP_COUNT_QUERY, Integer.class);
        log.debug("CustomPartitioner : partition : groupCountUid {}", groupCountUid);

        String SQL_GROUP_MIN_QUERY = SQLConstants.PREMIUM_COMPONENT_SELECT_DISTINCT_MIN_CLAUSE
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MIN_WHERE_CLAUSE_PART1
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MIN_WHERE_CLAUSE_PART2 + "'" + targetDateRange.get(0) + "'"
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MIN_WHERE_CLAUSE_PART3 + "'" + targetDateRange.get(1) + "'"
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MIN_WHERE_CLAUSE_PART4
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MIN_WHERE_CLAUSE_PART5
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MIN_WHERE_CLAUSE_PART6
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MIN_GROUP_BY_CLAUSE;

        int groupMinUid = this.jdbcTemplate.queryForObject(SQL_GROUP_MIN_QUERY, Integer.class);
        log.debug("CustomPartitioner : partition : groupMinUid {}", groupMinUid);

        String SQL_GROUP_MAX_QUERY = SQLConstants.PREMIUM_COMPONENT_SELECT_DISTINCT_MAX_CLAUSE
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MAX_WHERE_CLAUSE_PART1
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MAX_WHERE_CLAUSE_PART2 + "'" + targetDateRange.get(0) + "'"
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MAX_WHERE_CLAUSE_PART3 + "'" + targetDateRange.get(1) + "'"
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MAX_WHERE_CLAUSE_PART4
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MAX_WHERE_CLAUSE_PART5
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MAX_WHERE_CLAUSE_PART6
                + SQLConstants.PREMIUM_COMPONENT_SELECT_MAX_GROUP_BY_CLAUSE;

        int groupMaxUid = this.jdbcTemplate.queryForObject(SQL_GROUP_MAX_QUERY, Integer.class);
        log.debug("CustomPartitioner : partition : groupMaxUid {}", groupMaxUid);

        int uid_difference = 0;
        int targetUidSize = 0;
        if (groupMaxUid > 0 && groupMaxUid > groupMinUid) {
            uid_difference = groupMaxUid - groupMinUid;
        }

        if (uid_difference > 0) {
            log.debug("CustomPartitioner : partition : uid_difference {}", uid_difference);
            if (groupCountUid > 0 && groupCountUid < 100) {
                log.debug("CustomPartitioner : partition : small load of < 100 set grid to 1");
                gridSize = 1;
                targetUidSize = uid_difference / gridSize + 1;
            } else if (groupCountUid > 0) {
                if (groupCountUid > 99 && groupCountUid < 1000) {
                    log.debug("CustomPartitioner : partition : small load of < 1000 set grid to 1");
                    gridSize = 3;
                    targetUidSize = uid_difference / gridSize + 1;
                } else if (groupCountUid > 999 && groupCountUid < 10000) {
                    return setDynamicPartitioning(gridSize, groupMinUid, groupMaxUid, 2500, 200);
                } else if (groupCountUid > 9999 && groupCountUid < 20000) {
                    return setDynamicPartitioning(gridSize, groupMinUid, groupMaxUid, 5000, 500);
                } else if (groupCountUid > 19999 && groupCountUid < 30000) {
                    return setDynamicPartitioning(gridSize, groupMinUid, groupMaxUid, 10000, 2000);
                } else if (groupCountUid > 29999 && groupCountUid < 40000) {
                    return setDynamicPartitioning(gridSize, groupMinUid, groupMaxUid, 15000, 2000);
                } else if (groupCountUid > 39999 && groupCountUid < 50000) {
                    return setDynamicPartitioning(gridSize, groupMinUid, groupMaxUid, 20000, 2000);
                } else if (groupCountUid > 49999 && groupCountUid < 100000) {
                    return setDynamicPartitioning(gridSize, groupMinUid, groupMaxUid, 25000, 2000);
                } else if (groupCountUid > 99999 && groupCountUid < 200000) {
                    return setDynamicPartitioning(gridSize, groupMinUid, groupMaxUid, 30000, 2000);
                } else if (groupCountUid > 199999) {
                    return setDynamicPartitioning(gridSize, groupMinUid, groupMaxUid, 50000, 2000);
                }
            } else {
                return null;
            }
        } else {
            return null;
        }

        log.debug("CustomPartitioner : partition : targetUidSize {}", targetUidSize);

        Map<String, ExecutionContext> result = new HashMap<>();

        if (uid_difference > 0) {
            int number = 0;
            int start = groupMinUid;
            int end = start + targetUidSize - 1;
            while (start <= groupMaxUid) {
                ExecutionContext value = new ExecutionContext();
                result.put("partition" + number, value);

                if (end >= groupMaxUid) {
                    end = groupMaxUid;
                }

                value.putInt("minValue", start);
                value.putInt("maxValue", end);

                start += targetUidSize;
                end += targetUidSize;

                number++;
            }
        }
        log.debug("CustomPartitioner : partition : no of static results {}", result.size());

        return result;
    }

    private Map<String, ExecutionContext> setDynamicPartitioning(int gridSize, int groupMinUid, int groupMaxUid, int targetUidSize, int sqlCountMinCheck) {
        log.debug("CustomPartitioner : setDynamicPartitioning : gridSize {} groupMinUid {} groupMaxUid {} targetUidSize {}", gridSize, groupMinUid, groupMaxUid, targetUidSize);

        Map<String, ExecutionContext> result = new HashMap<>();

        int partitionCount = 0;
        int start = groupMinUid;
        int targetUid = start+targetUidSize;
        boolean complete = false;
        while (!complete) {
            log.debug("CustomPartitioner : setDynamicPartitioning : increment start : {} targetUid : {}", start, targetUid);
            if (targetUid > groupMaxUid) {
                log.debug("CustomPartitioner : setDynamicPartitioning : targetUid greater than groupMaxUid {}", targetUid);
                targetUid = groupMaxUid;
                complete = true;
            }
            int groupCount = rangeSQLCheck(start, targetUid);
            if (complete || groupCount > sqlCountMinCheck) {
                partitionCount++;
                ExecutionContext value = new ExecutionContext();
                value.putInt("minValue", start);
                value.putInt("maxValue", targetUid);
                result.put("partition" + partitionCount, value);
                start = targetUid++;
                log.debug("CustomPartitioner : setDynamicPartitioning : increment start {}", start);
                log.debug("CustomPartitioner : setDynamicPartitioning : partitionCount {}", partitionCount);
            }
            targetUid = targetUid+targetUidSize;
        }
        log.debug("CustomPartitioner : setDynamicPartitioning : no of dynamic results {}", result.size());

        gridSize = partitionCount;
        log.debug("CustomPartitioner : setDynamicPartitioning : gridSize {}", gridSize);

        return result;
    }

    private int rangeSQLCheck(int minValue, int maxValue) {
        log.debug("CustomPartitioner : rangeSQLCheck : start {} end {}", minValue, maxValue);

        String SQL_RANGE_COUNT_QUERY = SQLConstants.PREMIUM_COMPONENT_SELECT_DISTINCT_COUNT_CLAUSE
                + SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_WHERE
                + SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_RANGE_MIN + Long.toString(minValue)
                + SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_RANGE_MAX + Long.toString(maxValue)
                + SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART1 + "'" + this.targetDateRange.get(0) + "'"
                + SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART2 + "'" + this.targetDateRange.get(1) + "'"
                + SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART3
                + SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART4
                + SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART5
                + SQLConstants.PREMIUM_COMPONENT_GROUP_BY_CLAUSE;

        int rangeSQLCheckCount = this.jdbcTemplate.queryForObject(SQL_RANGE_COUNT_QUERY, Integer.class);
        log.debug("CustomPartitioner : rangeSQLCheck : rangeSQLCheckCount : {}", rangeSQLCheckCount);

        return rangeSQLCheckCount;

    }


}