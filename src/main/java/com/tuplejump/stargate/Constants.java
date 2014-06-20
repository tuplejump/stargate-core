package com.tuplejump.stargate;

/**
 * User: satya
 * Date: 30/06/13
 * Time: 9:55 PM
 */
public final class Constants {


    public static final String FIELDS = "fields";
    public static final String INDEX_OPTIONS_JSON = "sg_options";

    public static final String PK_NAME_INDEXED = "_row_key";
    public static final String PK_NAME_DOC_VAL = "_row_key_val";
    public static final String CF_TS_DOC_VAL = "_cf_ts_val";
    public static final String CF_TS_INDEXED = "_cf_ts";

    //lucene options per field
    public static final String striped = "striped";
    public static final String numberFormat = "numberFormat";
    public static final String indexed = "indexed";
    public static final String stored = "stored";
    public static final String tokenized = "tokenized";
    public static final String storeTermVectors = "storeTermVectors";
    public static final String storeTermVectorOffsets = "storeTermVectorOffsets";
    public static final String storeTermVectorPositions = "storeTermVectorPositions";
    public static final String storeTermVectorPayloads = "storeTermVectorPayloads";
    public static final String omitNorms = "omitNorms";
    public static final String indexOptions = "indexOptions";
    public static final String docValueType = "docValueType";
    public static final String numericType = "numericType";
    public static final String numericPrecisionStep = "numericPrecisionStep";
    public static final String IDXW_MAX_FL = "indexWriterMaxFieldLength";
    public static final String ANALYZER = "Analyzer";
    //lucene options
    public static final String LUCENE_VERSION = "Lucene.Version";
    public static final String INDEX_DIR_NAME = "Index.DirName";
    public static final String INDEX_FILE_NAME = "Index.FileName";

    public enum Analyzers {
        StandardAnalyzer, WhitespaceAnalyzer, StopAnalyzer, SimpleAnalyzer, KeywordAnalyzer

    }


}
