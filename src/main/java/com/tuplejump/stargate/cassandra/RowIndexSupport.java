package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.Fields;
import com.tuplejump.stargate.lucene.Indexer;
import com.tuplejump.stargate.lucene.Options;
import com.tuplejump.stargate.lucene.Properties;
import com.tuplejump.stargate.lucene.json.JsonDocument;
import com.tuplejump.stargate.lucene.json.StreamingJsonDocument;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.marshal.*;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: satya
 * Interface for writing a row to a lucene index.
 */
public abstract class RowIndexSupport {
    protected Options options;
    protected ColumnFamilyStore table;
    FieldType tsFieldType;

    public RowIndexSupport(Options options, ColumnFamilyStore table) {
        this.options = options;
        this.table = table;
        tsFieldType = Properties.fieldType(Properties.ID_FIELD, CQL3Type.Native.BIGINT.getType());
    }


    protected static final Logger logger = LoggerFactory.getLogger(RowIndexSupport.class);

    /**
     * Writes one row to the lucene index.
     *
     * @param rowKey The shard key for this row.
     * @param cf     the row to write.
     */
    public abstract void indexRow(Indexer indexer, ByteBuffer rowKey, ColumnFamily cf);

    /**
     * This is used to derive the actual column name from the byte buffer column name for a single column.
     * For Wide row tables, the column names are actually a concatenation of the Clustering key column names and this column name itself.
     * We use the valu
     *
     * @param name The CQL name buffer of the column.
     * @return The String name of the column
     */
    public abstract String getActualColumnName(ByteBuffer name);


    protected List<Field> collectionFields(CollectionType validator, String colName, Column column) {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        ByteBuffer[] components = baseComparator.split(column.name());
        List<Field> fields = new ArrayList<>();
        FieldType[] fieldTypesArr = options.collectionFieldTypes.get(colName);
        AbstractType keyType = validator.nameComparator();
        AbstractType valueType = validator.valueComparator();
        if (validator instanceof MapType) {
            ByteBuffer keyBuf = components[components.length - 1];
            fields.add(Fields.field(colName + ".key", keyType, keyBuf, fieldTypesArr[0]));
            fields.add(Fields.field(colName + ".value", valueType, column.value(), fieldTypesArr[1]));
            fields.add(Fields.field(keyType.getString(keyBuf), valueType, column.value(), fieldTypesArr[1]));
        } else if (validator instanceof SetType) {
            fields.add(Fields.field(colName, keyType, components[components.length - 1], fieldTypesArr[0]));
        } else if (validator instanceof ListType) {
            fields.add(Fields.field(colName, valueType, column.value(), fieldTypesArr[0]));
        } else throw new UnsupportedOperationException("Unsupported collection type " + validator);

        return fields;
    }

    protected List<Field> idFields(DecoratedKey rowKey, String pkName, ByteBuffer pk, AbstractType rkValValidator) {
        return Arrays.asList(Fields.idDocValues(rkValValidator, pk), Fields.pkNameDocValues(pkName), Fields.rowKeyIndexed(table.metadata.getKeyValidator().getString(rowKey.key)));
    }

    protected List<Field> tsFields(long ts) {
        Field tsField = Fields.tsField(ts, tsFieldType);
        return Arrays.asList(Fields.tsDocValues(ts), tsField);
    }


    protected void addField(List<Field> fields, ColumnDefinition columnDefinition, String name, FieldType fieldType, ByteBuffer value) {
        if (fieldType != null) {
            Field field = Fields.field(name, columnDefinition.getValidator(), value, fieldType);
            fields.add(field);
        }
    }

    protected void addFields(Column column, String name, List<Field> fields, ColumnDefinition columnDefinition) {
        boolean isObject = options.isObject(name);
        if (isObject) {
            String value = UTF8Type.instance.compose(column.value());
            JsonDocument document = new StreamingJsonDocument(value, options.primary, name);
            fields.addAll(document.getFields());
        } else if (columnDefinition.getValidator().isCollection()) {
            List<Field> fieldsForField = collectionFields((CollectionType) columnDefinition.getValidator(), name, column);
            fields.addAll(fieldsForField);
        } else {
            FieldType fieldType = options.fieldTypes.get(name);
            addField(fields, columnDefinition, name, fieldType, column.value());
        }
    }

}
