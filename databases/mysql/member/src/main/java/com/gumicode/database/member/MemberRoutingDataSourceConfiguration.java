package com.gumicode.database.member;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import static org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly;

class MemberRoutingDataSourceConfiguration extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return isCurrentTransactionReadOnly() ? "read" : "write";
    }
}
