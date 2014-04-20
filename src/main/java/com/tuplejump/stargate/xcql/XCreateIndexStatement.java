package com.tuplejump.stargate.xcql;

import argo.jdom.JdomParser;
import com.tuplejump.stargate.Constants;
import com.tuplejump.stargate.cas.PerColIndex;
import com.tuplejump.stargate.cas.PerRowIndex;
import org.apache.cassandra.auth.Permission;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.cql3.CFDefinition;
import org.apache.cassandra.cql3.CFName;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.statements.CreateIndexStatement;
import org.apache.cassandra.cql3.statements.SchemaAlteringStatement;
import org.apache.cassandra.db.index.SecondaryIndex;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.UnauthorizedException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.thrift.IndexType;
import org.apache.cassandra.thrift.ThriftValidation;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * User: satya
 */
public class XCreateIndexStatement extends SchemaAlteringStatement {
    private static final Logger logger = LoggerFactory.getLogger(CreateIndexStatement.class);
    private static final JdomParser JDOM_PARSER = new JdomParser();
    private String indexName;
    private final ColumnIdentifier columnName;
    private boolean isRowIndex = false;
    Map<String, String> options = new HashMap<String, String>();
    String optionsJson;

    public XCreateIndexStatement(CFName name, String indexName, ColumnIdentifier columnName, boolean isRowIndex, String optionsStr) {
        super(name);
        this.indexName = indexName;
        this.isRowIndex = isRowIndex;
        this.columnName = columnName;
        this.optionsJson = optionsStr;
        try {
            if (optionsJson != null)
                JDOM_PARSER.parse(optionsStr);
        } catch (Exception e) {
            throw new RuntimeException("Invalid options. - " + optionsStr, e);
        }
    }

    public void checkAccess(ClientState state) throws UnauthorizedException, InvalidRequestException {
        state.hasColumnFamilyAccess(keyspace(), columnFamily(), Permission.ALTER);
    }

    @Override
    public void validate(ClientState state) throws RequestValidationException {
        CFMetaData cfm = ThriftValidation.validateColumnFamily(keyspace(), columnFamily());
        CFDefinition.Name name = cfm.getCfDef().get(columnName);

        if (name == null)
            throw new InvalidRequestException("No column definition found for column " + columnName);

        switch (name.kind) {
            case KEY_ALIAS:
            case COLUMN_ALIAS:
                throw new InvalidRequestException(String.format("Cannot create index on PRIMARY KEY part %s", columnName));
            case VALUE_ALIAS:
                throw new InvalidRequestException(String.format("Cannot create index on column %s of compact CF", columnName));
            case COLUMN_METADATA:
                ColumnDefinition cd = cfm.getColumnDefinition(columnName.key);

                if (cd.getIndexType() != null)
                    throw new InvalidRequestException("Index already exists");

                if (cd.getValidator().isCollection())
                    throw new InvalidRequestException("Indexes on collections are no yet supported");
                break;
            default:
                throw new AssertionError();
        }
    }

    public void announceMigration() throws InvalidRequestException, ConfigurationException {
        logger.debug("Updating column {} definition for index {}", columnName, indexName);
        CFMetaData cfm = Schema.instance.getCFMetaData(keyspace(), columnFamily()).clone();
        ColumnDefinition cd = cfm.getColumnDefinition(columnName.key);
        if (isRowIndex) {
            options.put(SecondaryIndex.CUSTOM_INDEX_OPTION_NAME, PerRowIndex.class.getName());
        } else {
            options.put(SecondaryIndex.CUSTOM_INDEX_OPTION_NAME, PerColIndex.class.getName());
        }
        cd.setIndexName(indexName);
        options.put(Constants.INDEX_OPTIONS_JSON, optionsJson);
        cd.setIndexType(IndexType.CUSTOM, options);
        cfm.addDefaultIndexNames();
        MigrationManager.announceColumnFamilyUpdate(cfm);
    }

    public ResultMessage.SchemaChange.Change changeType() {
        // Creating an index is akin to updating the CF
        return ResultMessage.SchemaChange.Change.UPDATED;
    }
}