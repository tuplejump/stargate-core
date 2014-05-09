// $ANTLR 3.2 Sep 23, 2009 14:05:07 src/main/java/com/tuplejump/stargate/xcql/XCql.g 2014-05-10 05:19:40

    package com.tuplejump.stargate.xcql.ast;

    import com.tuplejump.stargate.xcql.ast.*;
    import com.tuplejump.stargate.xcql.*;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Collections;
    import java.util.EnumSet;
    import java.util.HashMap;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;

    import org.apache.cassandra.auth.Permission;
    import org.apache.cassandra.auth.DataResource;
    import org.apache.cassandra.auth.IResource;
    import org.apache.cassandra.cql3.*;
    import org.apache.cassandra.cql3.statements.*;
    import org.apache.cassandra.cql3.functions.FunctionCall;
    import org.apache.cassandra.db.marshal.CollectionType;
    import org.apache.cassandra.exceptions.ConfigurationException;
    import org.apache.cassandra.exceptions.InvalidRequestException;
    import org.apache.cassandra.exceptions.SyntaxException;
    import org.apache.cassandra.utils.Pair;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class XCqlParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "X_CREATE", "X_ROW", "K_INDEX", "IDENT", "K_ON", "K_WITH", "X_OPTIONS", "STRING_LITERAL", "QUOTED_NAME", "INTEGER", "FLOAT", "BOOLEAN", "UUID", "HEXNUMBER", "K_NULL", "QMARK", "K_TOKEN", "K_AND", "K_IN", "K_ASCII", "K_BIGINT", "K_BLOB", "K_BOOLEAN", "K_COUNTER", "K_DECIMAL", "K_DOUBLE", "K_FLOAT", "K_INET", "K_INT", "K_TEXT", "K_TIMESTAMP", "K_UUID", "K_VARCHAR", "K_VARINT", "K_TIMEUUID", "K_MAP", "K_LIST", "K_SET", "K_TTL", "K_COUNT", "K_WRITETIME", "K_KEY", "K_CLUSTERING", "K_COMPACT", "K_STORAGE", "K_TYPE", "K_VALUES", "K_FILTERING", "K_PERMISSION", "K_PERMISSIONS", "K_KEYSPACES", "K_ALL", "K_USER", "K_USERS", "K_SUPERUSER", "K_NOSUPERUSER", "K_PASSWORD", "K_CUSTOM", "X", "S", "E", "L", "C", "T", "X_SELECT", "R", "A", "G", "O", "U", "P", "X_GROUP", "H", "V", "I", "N", "X_HAVING", "K", "X_LIKE", "X_SKIP", "W", "M", "X_ROW_META", "X_COL", "X_OR", "D", "X_UNLIM", "K_SELECT", "F", "K_FROM", "K_WHERE", "Y", "K_INSERT", "K_UPDATE", "K_LIMIT", "K_USING", "K_USE", "B", "K_BEGIN", "K_UNLOGGED", "K_BATCH", "K_APPLY", "K_TRUNCATE", "K_DELETE", "K_CREATE", "K_KEYSPACE", "K_COLUMNFAMILY", "K_TO", "K_DROP", "K_PRIMARY", "K_INTO", "K_ALTER", "K_RENAME", "K_ADD", "K_ORDER", "K_BY", "K_ASC", "K_DESC", "K_ALLOW", "K_GRANT", "K_OF", "K_REVOKE", "K_MODIFY", "Z", "K_AUTHORIZE", "K_NORECURSIVE", "J", "Q", "DIGIT", "LETTER", "HEX", "EXPONENT", "WS", "COMMENT", "MULTILINE_COMMENT", "';'", "'('", "')'", "'='", "'.'", "'}'", "','", "':'", "'{'", "'['", "']'", "'+'", "'-'", "'<'", "'<='", "'>'", "'>='"
    };
    public static final int EXPONENT=135;
    public static final int K_PERMISSIONS=53;
    public static final int LETTER=133;
    public static final int K_INT=32;
    public static final int K_PERMISSION=52;
    public static final int K_CREATE=108;
    public static final int K_CLUSTERING=46;
    public static final int K_WRITETIME=44;
    public static final int EOF=-1;
    public static final int K_PRIMARY=113;
    public static final int K_AUTHORIZE=128;
    public static final int X_UNLIM=90;
    public static final int K_USE=100;
    public static final int K_VALUES=50;
    public static final int STRING_LITERAL=11;
    public static final int T__148=148;
    public static final int K_GRANT=123;
    public static final int T__147=147;
    public static final int K_ON=8;
    public static final int T__149=149;
    public static final int K_USING=99;
    public static final int K_ADD=117;
    public static final int K_ASC=120;
    public static final int K_CUSTOM=61;
    public static final int K_KEY=45;
    public static final int COMMENT=137;
    public static final int K_TRUNCATE=106;
    public static final int T__154=154;
    public static final int T__155=155;
    public static final int T__150=150;
    public static final int K_ORDER=118;
    public static final int T__151=151;
    public static final int K_OF=124;
    public static final int HEXNUMBER=17;
    public static final int T__152=152;
    public static final int K_ALL=55;
    public static final int T__153=153;
    public static final int D=89;
    public static final int T__139=139;
    public static final int E=64;
    public static final int F=92;
    public static final int G=71;
    public static final int K_KEYSPACE=109;
    public static final int K_COUNT=43;
    public static final int K_TYPE=49;
    public static final int X_SELECT=68;
    public static final int A=70;
    public static final int B=101;
    public static final int C=66;
    public static final int L=65;
    public static final int M=85;
    public static final int N=79;
    public static final int O=72;
    public static final int H=76;
    public static final int I=78;
    public static final int J=130;
    public static final int K_UPDATE=97;
    public static final int K=81;
    public static final int K_FILTERING=51;
    public static final int U=73;
    public static final int T=67;
    public static final int X_COL=87;
    public static final int W=84;
    public static final int K_TEXT=33;
    public static final int V=77;
    public static final int X_CREATE=4;
    public static final int Q=131;
    public static final int P=74;
    public static final int K_COMPACT=47;
    public static final int S=63;
    public static final int R=69;
    public static final int T__141=141;
    public static final int T__142=142;
    public static final int T__140=140;
    public static final int K_TTL=42;
    public static final int Y=95;
    public static final int T__145=145;
    public static final int T__146=146;
    public static final int X=62;
    public static final int T__143=143;
    public static final int Z=127;
    public static final int T__144=144;
    public static final int K_INDEX=6;
    public static final int K_INSERT=96;
    public static final int WS=136;
    public static final int K_RENAME=116;
    public static final int K_APPLY=105;
    public static final int K_INET=31;
    public static final int K_STORAGE=48;
    public static final int K_TIMESTAMP=34;
    public static final int K_NULL=18;
    public static final int K_AND=21;
    public static final int K_DESC=121;
    public static final int K_TOKEN=20;
    public static final int QMARK=19;
    public static final int K_BATCH=104;
    public static final int K_UUID=35;
    public static final int K_ASCII=23;
    public static final int X_LIKE=82;
    public static final int UUID=16;
    public static final int K_LIST=40;
    public static final int K_DELETE=107;
    public static final int K_TO=111;
    public static final int K_BY=119;
    public static final int FLOAT=14;
    public static final int K_FLOAT=30;
    public static final int K_VARINT=37;
    public static final int K_SUPERUSER=58;
    public static final int K_DOUBLE=29;
    public static final int K_SELECT=91;
    public static final int K_LIMIT=98;
    public static final int K_ALTER=115;
    public static final int K_BOOLEAN=26;
    public static final int K_SET=41;
    public static final int K_WHERE=94;
    public static final int QUOTED_NAME=12;
    public static final int MULTILINE_COMMENT=138;
    public static final int K_UNLOGGED=103;
    public static final int K_BLOB=25;
    public static final int BOOLEAN=15;
    public static final int HEX=134;
    public static final int K_INTO=114;
    public static final int K_PASSWORD=60;
    public static final int K_REVOKE=125;
    public static final int K_ALLOW=122;
    public static final int K_VARCHAR=36;
    public static final int IDENT=7;
    public static final int DIGIT=132;
    public static final int X_GROUP=75;
    public static final int K_USERS=57;
    public static final int K_BEGIN=102;
    public static final int X_OR=88;
    public static final int X_HAVING=80;
    public static final int INTEGER=13;
    public static final int X_SKIP=83;
    public static final int K_KEYSPACES=54;
    public static final int K_COUNTER=27;
    public static final int X_ROW_META=86;
    public static final int K_DECIMAL=28;
    public static final int K_WITH=9;
    public static final int K_IN=22;
    public static final int K_NORECURSIVE=129;
    public static final int K_MAP=39;
    public static final int K_FROM=93;
    public static final int K_COLUMNFAMILY=110;
    public static final int K_MODIFY=126;
    public static final int K_DROP=112;
    public static final int K_NOSUPERUSER=59;
    public static final int X_OPTIONS=10;
    public static final int K_BIGINT=24;
    public static final int X_ROW=5;
    public static final int K_TIMEUUID=38;
    public static final int K_USER=56;

    // delegates
    // delegators


        public XCqlParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public XCqlParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return XCqlParser.tokenNames; }
    public String getGrammarFileName() { return "src/main/java/com/tuplejump/stargate/xcql/XCql.g"; }


        private List<String> recognitionErrors = new ArrayList<String>();
        private int currentBindMarkerIdx = -1;

        public void displayRecognitionError(String[] tokenNames, RecognitionException e)
        {
            String hdr = getErrorHeader(e);
            String msg = getErrorMessage(e, tokenNames);
            recognitionErrors.add(hdr + " " + msg);
        }

        public void addRecognitionError(String msg)
        {
            recognitionErrors.add(msg);
        }

        public List<String> getRecognitionErrors()
        {
            return recognitionErrors;
        }

        public void throwLastRecognitionError() throws SyntaxException
        {
            if (recognitionErrors.size() > 0)
                throw new SyntaxException(recognitionErrors.get((recognitionErrors.size()-1)));
        }

        public Map<String, String> convertPropertyMap(Maps.Literal map)
        {
            if (map == null || map.entries == null || map.entries.isEmpty())
                return Collections.<String, String>emptyMap();

            Map<String, String> res = new HashMap<String, String>(map.entries.size());

            for (Pair<Term.Raw, Term.Raw> entry : map.entries)
            {
                // Because the parser tries to be smart and recover on error (to
                // allow displaying more than one error I suppose), we have null
                // entries in there. Just skip those, a proper error will be thrown in the end.
                if (entry.left == null || entry.right == null)
                    break;

                if (!(entry.left instanceof Constants.Literal))
                {
                    addRecognitionError("Invalid property name: " + entry.left);
                    break;
                }
                if (!(entry.right instanceof Constants.Literal))
                {
                    addRecognitionError("Invalid property value: " + entry.right);
                    break;
                }

                res.put(((Constants.Literal)entry.left).getRawText(), ((Constants.Literal)entry.right).getRawText());
            }

            return res;
        }

        public void addRawUpdate(List<Pair<ColumnIdentifier, Operation.RawUpdate>> operations, ColumnIdentifier key, Operation.RawUpdate update)
        {
            for (Pair<ColumnIdentifier, Operation.RawUpdate> p : operations)
            {
                if (p.left.equals(key) && !p.right.isCompatibleWith(update))
                    addRecognitionError("Multiple incompatible setting of column " + key);
            }
            operations.add(Pair.create(key, update));
        }



    // $ANTLR start "query"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:149:1: query returns [ParsedStatement stmnt] : st= xcqlStatement ( ';' )* EOF ;
    public final ParsedStatement query() throws RecognitionException {
        ParsedStatement stmnt = null;

        ParsedStatement st = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:152:5: (st= xcqlStatement ( ';' )* EOF )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:152:7: st= xcqlStatement ( ';' )* EOF
            {
            pushFollow(FOLLOW_xcqlStatement_in_query69);
            st=xcqlStatement();

            state._fsp--;

            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:152:24: ( ';' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==139) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:152:25: ';'
            	    {
            	    match(input,139,FOLLOW_139_in_query72); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_query76); 
             stmnt = st; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmnt;
    }
    // $ANTLR end "query"


    // $ANTLR start "xcqlStatement"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:155:1: xcqlStatement returns [ParsedStatement stmt] : st1= createIndexStatement ;
    public final ParsedStatement xcqlStatement() throws RecognitionException {
        ParsedStatement stmt = null;

        XCreateIndexStatement st1 = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:157:5: (st1= createIndexStatement )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:157:7: st1= createIndexStatement
            {
            pushFollow(FOLLOW_createIndexStatement_in_xcqlStatement110);
            st1=createIndexStatement();

            state._fsp--;

             stmt = st1; 

            }

             if (stmt != null) stmt.setBoundTerms(currentBindMarkerIdx + 1); 
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "xcqlStatement"


    // $ANTLR start "createIndexStatement"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:161:1: createIndexStatement returns [XCreateIndexStatement expr] : X_CREATE ( X_ROW )? K_INDEX (idxName= IDENT )? K_ON cf= columnFamilyName '(' id= cident ')' ( K_WITH X_OPTIONS '=' opts= STRING_LITERAL )? ;
    public final XCreateIndexStatement createIndexStatement() throws RecognitionException {
        XCreateIndexStatement expr = null;

        Token idxName=null;
        Token opts=null;
        CFName cf = null;

        ColumnIdentifier id = null;



                boolean isRowIndex = false;
                Map<String,String> props = new HashMap<String,String>();
            
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:169:5: ( X_CREATE ( X_ROW )? K_INDEX (idxName= IDENT )? K_ON cf= columnFamilyName '(' id= cident ')' ( K_WITH X_OPTIONS '=' opts= STRING_LITERAL )? )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:169:7: X_CREATE ( X_ROW )? K_INDEX (idxName= IDENT )? K_ON cf= columnFamilyName '(' id= cident ')' ( K_WITH X_OPTIONS '=' opts= STRING_LITERAL )?
            {
            match(input,X_CREATE,FOLLOW_X_CREATE_in_createIndexStatement157); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:169:16: ( X_ROW )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==X_ROW) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:169:17: X_ROW
                    {
                    match(input,X_ROW,FOLLOW_X_ROW_in_createIndexStatement160); 
                     isRowIndex = true; 

                    }
                    break;

            }

            match(input,K_INDEX,FOLLOW_K_INDEX_in_createIndexStatement166); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:169:56: (idxName= IDENT )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==IDENT) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:169:57: idxName= IDENT
                    {
                    idxName=(Token)match(input,IDENT,FOLLOW_IDENT_in_createIndexStatement171); 

                    }
                    break;

            }

            match(input,K_ON,FOLLOW_K_ON_in_createIndexStatement175); 
            pushFollow(FOLLOW_columnFamilyName_in_createIndexStatement179);
            cf=columnFamilyName();

            state._fsp--;

            match(input,140,FOLLOW_140_in_createIndexStatement181); 
            pushFollow(FOLLOW_cident_in_createIndexStatement185);
            id=cident();

            state._fsp--;

            match(input,141,FOLLOW_141_in_createIndexStatement187); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:170:7: ( K_WITH X_OPTIONS '=' opts= STRING_LITERAL )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==K_WITH) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:170:9: K_WITH X_OPTIONS '=' opts= STRING_LITERAL
                    {
                    match(input,K_WITH,FOLLOW_K_WITH_in_createIndexStatement197); 
                    match(input,X_OPTIONS,FOLLOW_X_OPTIONS_in_createIndexStatement199); 
                    match(input,142,FOLLOW_142_in_createIndexStatement201); 
                    opts=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_createIndexStatement205); 

                    }
                    break;

            }

             expr = new XCreateIndexStatement(cf, (idxName!=null?idxName.getText():null), id, isRowIndex, (opts!=null?opts.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "createIndexStatement"


    // $ANTLR start "cident"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:175:1: cident returns [ColumnIdentifier id] : (t= IDENT | t= QUOTED_NAME | k= unreserved_keyword );
    public final ColumnIdentifier cident() throws RecognitionException {
        ColumnIdentifier id = null;

        Token t=null;
        String k = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:179:5: (t= IDENT | t= QUOTED_NAME | k= unreserved_keyword )
            int alt5=3;
            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt5=1;
                }
                break;
            case QUOTED_NAME:
                {
                alt5=2;
                }
                break;
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_COUNTER:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_TIMESTAMP:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
            case K_LIST:
            case K_TTL:
            case K_COUNT:
            case K_WRITETIME:
            case K_KEY:
            case K_CLUSTERING:
            case K_COMPACT:
            case K_STORAGE:
            case K_TYPE:
            case K_VALUES:
            case K_FILTERING:
            case K_PERMISSION:
            case K_PERMISSIONS:
            case K_KEYSPACES:
            case K_ALL:
            case K_USER:
            case K_USERS:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_PASSWORD:
            case K_CUSTOM:
                {
                alt5=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:179:7: t= IDENT
                    {
                    t=(Token)match(input,IDENT,FOLLOW_IDENT_in_cident244); 
                     id = new ColumnIdentifier((t!=null?t.getText():null), false); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:180:7: t= QUOTED_NAME
                    {
                    t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cident269); 
                     id = new ColumnIdentifier((t!=null?t.getText():null), true); 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:181:7: k= unreserved_keyword
                    {
                    pushFollow(FOLLOW_unreserved_keyword_in_cident288);
                    k=unreserved_keyword();

                    state._fsp--;

                     id = new ColumnIdentifier(k, false); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return id;
    }
    // $ANTLR end "cident"


    // $ANTLR start "keyspaceName"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:185:1: keyspaceName returns [String id] : cfOrKsName[name, true] ;
    public final String keyspaceName() throws RecognitionException {
        String id = null;

         CFName name = new CFName(); 
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:187:5: ( cfOrKsName[name, true] )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:187:7: cfOrKsName[name, true]
            {
            pushFollow(FOLLOW_cfOrKsName_in_keyspaceName321);
            cfOrKsName(name, true);

            state._fsp--;

             id = name.getKeyspace(); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return id;
    }
    // $ANTLR end "keyspaceName"


    // $ANTLR start "columnFamilyName"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:190:1: columnFamilyName returns [CFName name] : ( cfOrKsName[name, true] '.' )? cfOrKsName[name, false] ;
    public final CFName columnFamilyName() throws RecognitionException {
        CFName name = null;

         name = new CFName(); 
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:192:5: ( ( cfOrKsName[name, true] '.' )? cfOrKsName[name, false] )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:192:7: ( cfOrKsName[name, true] '.' )? cfOrKsName[name, false]
            {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:192:7: ( cfOrKsName[name, true] '.' )?
            int alt6=2;
            alt6 = dfa6.predict(input);
            switch (alt6) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:192:8: cfOrKsName[name, true] '.'
                    {
                    pushFollow(FOLLOW_cfOrKsName_in_columnFamilyName355);
                    cfOrKsName(name, true);

                    state._fsp--;

                    match(input,143,FOLLOW_143_in_columnFamilyName358); 

                    }
                    break;

            }

            pushFollow(FOLLOW_cfOrKsName_in_columnFamilyName362);
            cfOrKsName(name, false);

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return name;
    }
    // $ANTLR end "columnFamilyName"


    // $ANTLR start "cfOrKsName"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:195:1: cfOrKsName[CFName name, boolean isKs] : (t= IDENT | t= QUOTED_NAME | k= unreserved_keyword );
    public final void cfOrKsName(CFName name, boolean isKs) throws RecognitionException {
        Token t=null;
        String k = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:196:5: (t= IDENT | t= QUOTED_NAME | k= unreserved_keyword )
            int alt7=3;
            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt7=1;
                }
                break;
            case QUOTED_NAME:
                {
                alt7=2;
                }
                break;
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_COUNTER:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_TIMESTAMP:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
            case K_LIST:
            case K_TTL:
            case K_COUNT:
            case K_WRITETIME:
            case K_KEY:
            case K_CLUSTERING:
            case K_COMPACT:
            case K_STORAGE:
            case K_TYPE:
            case K_VALUES:
            case K_FILTERING:
            case K_PERMISSION:
            case K_PERMISSIONS:
            case K_KEYSPACES:
            case K_ALL:
            case K_USER:
            case K_USERS:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_PASSWORD:
            case K_CUSTOM:
                {
                alt7=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }

            switch (alt7) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:196:7: t= IDENT
                    {
                    t=(Token)match(input,IDENT,FOLLOW_IDENT_in_cfOrKsName383); 
                     if (isKs) name.setKeyspace((t!=null?t.getText():null), false); else name.setColumnFamily((t!=null?t.getText():null), false); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:197:7: t= QUOTED_NAME
                    {
                    t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cfOrKsName408); 
                     if (isKs) name.setKeyspace((t!=null?t.getText():null), true); else name.setColumnFamily((t!=null?t.getText():null), true); 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:198:7: k= unreserved_keyword
                    {
                    pushFollow(FOLLOW_unreserved_keyword_in_cfOrKsName427);
                    k=unreserved_keyword();

                    state._fsp--;

                     if (isKs) name.setKeyspace(k, false); else name.setColumnFamily(k, false); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfOrKsName"


    // $ANTLR start "constant"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:201:1: constant returns [Constants.Literal constant] : (t= STRING_LITERAL | t= INTEGER | t= FLOAT | t= BOOLEAN | t= UUID | t= HEXNUMBER );
    public final Constants.Literal constant() throws RecognitionException {
        Constants.Literal constant = null;

        Token t=null;

        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:202:5: (t= STRING_LITERAL | t= INTEGER | t= FLOAT | t= BOOLEAN | t= UUID | t= HEXNUMBER )
            int alt8=6;
            switch ( input.LA(1) ) {
            case STRING_LITERAL:
                {
                alt8=1;
                }
                break;
            case INTEGER:
                {
                alt8=2;
                }
                break;
            case FLOAT:
                {
                alt8=3;
                }
                break;
            case BOOLEAN:
                {
                alt8=4;
                }
                break;
            case UUID:
                {
                alt8=5;
                }
                break;
            case HEXNUMBER:
                {
                alt8=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }

            switch (alt8) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:202:7: t= STRING_LITERAL
                    {
                    t=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_constant452); 
                     constant = Constants.Literal.string((t!=null?t.getText():null)); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:203:7: t= INTEGER
                    {
                    t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_constant464); 
                     constant = Constants.Literal.integer((t!=null?t.getText():null)); 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:204:7: t= FLOAT
                    {
                    t=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_constant483); 
                     constant = Constants.Literal.floatingPoint((t!=null?t.getText():null)); 

                    }
                    break;
                case 4 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:205:7: t= BOOLEAN
                    {
                    t=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_constant504); 
                     constant = Constants.Literal.bool((t!=null?t.getText():null)); 

                    }
                    break;
                case 5 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:206:7: t= UUID
                    {
                    t=(Token)match(input,UUID,FOLLOW_UUID_in_constant523); 
                     constant = Constants.Literal.uuid((t!=null?t.getText():null)); 

                    }
                    break;
                case 6 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:207:7: t= HEXNUMBER
                    {
                    t=(Token)match(input,HEXNUMBER,FOLLOW_HEXNUMBER_in_constant545); 
                     constant = Constants.Literal.hex((t!=null?t.getText():null)); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return constant;
    }
    // $ANTLR end "constant"


    // $ANTLR start "set_tail"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:210:1: set_tail[List<Term.Raw> s] : ( '}' | ',' t= term set_tail[s] );
    public final void set_tail(List<Term.Raw> s) throws RecognitionException {
        Term.Raw t = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:211:5: ( '}' | ',' t= term set_tail[s] )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==144) ) {
                alt9=1;
            }
            else if ( (LA9_0==145) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:211:7: '}'
                    {
                    match(input,144,FOLLOW_144_in_set_tail570); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:212:7: ',' t= term set_tail[s]
                    {
                    match(input,145,FOLLOW_145_in_set_tail578); 
                    pushFollow(FOLLOW_term_in_set_tail582);
                    t=term();

                    state._fsp--;

                     s.add(t); 
                    pushFollow(FOLLOW_set_tail_in_set_tail586);
                    set_tail(s);

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "set_tail"


    // $ANTLR start "map_tail"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:215:1: map_tail[List<Pair<Term.Raw, Term.Raw>> m] : ( '}' | ',' k= term ':' v= term map_tail[m] );
    public final void map_tail(List<Pair<Term.Raw, Term.Raw>> m) throws RecognitionException {
        Term.Raw k = null;

        Term.Raw v = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:216:5: ( '}' | ',' k= term ':' v= term map_tail[m] )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==144) ) {
                alt10=1;
            }
            else if ( (LA10_0==145) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:216:7: '}'
                    {
                    match(input,144,FOLLOW_144_in_map_tail605); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:217:7: ',' k= term ':' v= term map_tail[m]
                    {
                    match(input,145,FOLLOW_145_in_map_tail613); 
                    pushFollow(FOLLOW_term_in_map_tail617);
                    k=term();

                    state._fsp--;

                    match(input,146,FOLLOW_146_in_map_tail619); 
                    pushFollow(FOLLOW_term_in_map_tail623);
                    v=term();

                    state._fsp--;

                     m.add(Pair.create(k, v)); 
                    pushFollow(FOLLOW_map_tail_in_map_tail627);
                    map_tail(m);

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "map_tail"


    // $ANTLR start "map_literal"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:220:1: map_literal returns [Maps.Literal map] : ( '{' '}' | '{' k1= term ':' v1= term map_tail[m] );
    public final Maps.Literal map_literal() throws RecognitionException {
        Maps.Literal map = null;

        Term.Raw k1 = null;

        Term.Raw v1 = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:221:5: ( '{' '}' | '{' k1= term ':' v1= term map_tail[m] )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==147) ) {
                int LA11_1 = input.LA(2);

                if ( (LA11_1==144) ) {
                    alt11=1;
                }
                else if ( (LA11_1==IDENT||LA11_1==STRING_LITERAL||(LA11_1>=INTEGER && LA11_1<=K_TOKEN)||(LA11_1>=K_ASCII && LA11_1<=K_LIST)||(LA11_1>=K_KEY && LA11_1<=K_CUSTOM)||LA11_1==140||(LA11_1>=147 && LA11_1<=148)) ) {
                    alt11=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:221:7: '{' '}'
                    {
                    match(input,147,FOLLOW_147_in_map_literal649); 
                    match(input,144,FOLLOW_144_in_map_literal651); 
                     map = new Maps.Literal(Collections.<Pair<Term.Raw, Term.Raw>>emptyList()); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:222:7: '{' k1= term ':' v1= term map_tail[m]
                    {
                    match(input,147,FOLLOW_147_in_map_literal661); 
                     List<Pair<Term.Raw, Term.Raw>> m = new ArrayList<Pair<Term.Raw, Term.Raw>>(); 
                    pushFollow(FOLLOW_term_in_map_literal677);
                    k1=term();

                    state._fsp--;

                    match(input,146,FOLLOW_146_in_map_literal679); 
                    pushFollow(FOLLOW_term_in_map_literal683);
                    v1=term();

                    state._fsp--;

                     m.add(Pair.create(k1, v1)); 
                    pushFollow(FOLLOW_map_tail_in_map_literal687);
                    map_tail(m);

                    state._fsp--;

                     map = new Maps.Literal(m); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return map;
    }
    // $ANTLR end "map_literal"


    // $ANTLR start "set_or_map"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:227:1: set_or_map[Term.Raw t] returns [Term.Raw value] : ( ':' v= term map_tail[m] | set_tail[s] );
    public final Term.Raw set_or_map(Term.Raw t) throws RecognitionException {
        Term.Raw value = null;

        Term.Raw v = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:228:5: ( ':' v= term map_tail[m] | set_tail[s] )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==146) ) {
                alt12=1;
            }
            else if ( ((LA12_0>=144 && LA12_0<=145)) ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:228:7: ':' v= term map_tail[m]
                    {
                    match(input,146,FOLLOW_146_in_set_or_map719); 
                    pushFollow(FOLLOW_term_in_set_or_map723);
                    v=term();

                    state._fsp--;

                     List<Pair<Term.Raw, Term.Raw>> m = new ArrayList<Pair<Term.Raw, Term.Raw>>(); m.add(Pair.create(t, v)); 
                    pushFollow(FOLLOW_map_tail_in_set_or_map727);
                    map_tail(m);

                    state._fsp--;

                     value = new Maps.Literal(m); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:229:7: set_tail[s]
                    {
                     List<Term.Raw> s = new ArrayList<Term.Raw>(); s.add(t); 
                    pushFollow(FOLLOW_set_tail_in_set_or_map740);
                    set_tail(s);

                    state._fsp--;

                     value = new Sets.Literal(s); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "set_or_map"


    // $ANTLR start "collection_literal"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:233:1: collection_literal returns [Term.Raw value] : ( '[' (t1= term ( ',' tn= term )* )? ']' | '{' t= term v= set_or_map[t] | '{' '}' );
    public final Term.Raw collection_literal() throws RecognitionException {
        Term.Raw value = null;

        Term.Raw t1 = null;

        Term.Raw tn = null;

        Term.Raw t = null;

        Term.Raw v = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:234:5: ( '[' (t1= term ( ',' tn= term )* )? ']' | '{' t= term v= set_or_map[t] | '{' '}' )
            int alt15=3;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==148) ) {
                alt15=1;
            }
            else if ( (LA15_0==147) ) {
                int LA15_2 = input.LA(2);

                if ( (LA15_2==144) ) {
                    alt15=3;
                }
                else if ( (LA15_2==IDENT||LA15_2==STRING_LITERAL||(LA15_2>=INTEGER && LA15_2<=K_TOKEN)||(LA15_2>=K_ASCII && LA15_2<=K_LIST)||(LA15_2>=K_KEY && LA15_2<=K_CUSTOM)||LA15_2==140||(LA15_2>=147 && LA15_2<=148)) ) {
                    alt15=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:234:7: '[' (t1= term ( ',' tn= term )* )? ']'
                    {
                    match(input,148,FOLLOW_148_in_collection_literal765); 
                     List<Term.Raw> l = new ArrayList<Term.Raw>(); 
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:234:61: (t1= term ( ',' tn= term )* )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==IDENT||LA14_0==STRING_LITERAL||(LA14_0>=INTEGER && LA14_0<=K_TOKEN)||(LA14_0>=K_ASCII && LA14_0<=K_LIST)||(LA14_0>=K_KEY && LA14_0<=K_CUSTOM)||LA14_0==140||(LA14_0>=147 && LA14_0<=148)) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:234:63: t1= term ( ',' tn= term )*
                            {
                            pushFollow(FOLLOW_term_in_collection_literal773);
                            t1=term();

                            state._fsp--;

                             l.add(t1); 
                            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:234:86: ( ',' tn= term )*
                            loop13:
                            do {
                                int alt13=2;
                                int LA13_0 = input.LA(1);

                                if ( (LA13_0==145) ) {
                                    alt13=1;
                                }


                                switch (alt13) {
                            	case 1 :
                            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:234:88: ',' tn= term
                            	    {
                            	    match(input,145,FOLLOW_145_in_collection_literal779); 
                            	    pushFollow(FOLLOW_term_in_collection_literal783);
                            	    tn=term();

                            	    state._fsp--;

                            	     l.add(tn); 

                            	    }
                            	    break;

                            	default :
                            	    break loop13;
                                }
                            } while (true);


                            }
                            break;

                    }

                    match(input,149,FOLLOW_149_in_collection_literal793); 
                     value = new Lists.Literal(l); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:235:7: '{' t= term v= set_or_map[t]
                    {
                    match(input,147,FOLLOW_147_in_collection_literal803); 
                    pushFollow(FOLLOW_term_in_collection_literal807);
                    t=term();

                    state._fsp--;

                    pushFollow(FOLLOW_set_or_map_in_collection_literal811);
                    v=set_or_map(t);

                    state._fsp--;

                     value = v; 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:237:7: '{' '}'
                    {
                    match(input,147,FOLLOW_147_in_collection_literal827); 
                    match(input,144,FOLLOW_144_in_collection_literal829); 
                     value = new Sets.Literal(Collections.<Term.Raw>emptyList()); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "collection_literal"


    // $ANTLR start "value"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:240:1: value returns [Term.Raw value] : (c= constant | l= collection_literal | K_NULL | QMARK );
    public final Term.Raw value() throws RecognitionException {
        Term.Raw value = null;

        Constants.Literal c = null;

        Term.Raw l = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:241:5: (c= constant | l= collection_literal | K_NULL | QMARK )
            int alt16=4;
            switch ( input.LA(1) ) {
            case STRING_LITERAL:
            case INTEGER:
            case FLOAT:
            case BOOLEAN:
            case UUID:
            case HEXNUMBER:
                {
                alt16=1;
                }
                break;
            case 147:
            case 148:
                {
                alt16=2;
                }
                break;
            case K_NULL:
                {
                alt16=3;
                }
                break;
            case QMARK:
                {
                alt16=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }

            switch (alt16) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:241:7: c= constant
                    {
                    pushFollow(FOLLOW_constant_in_value854);
                    c=constant();

                    state._fsp--;

                     value = c; 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:242:7: l= collection_literal
                    {
                    pushFollow(FOLLOW_collection_literal_in_value876);
                    l=collection_literal();

                    state._fsp--;

                     value = l; 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:243:7: K_NULL
                    {
                    match(input,K_NULL,FOLLOW_K_NULL_in_value886); 
                     value = Constants.NULL_LITERAL; 

                    }
                    break;
                case 4 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:244:7: QMARK
                    {
                    match(input,QMARK,FOLLOW_QMARK_in_value910); 
                     value = new AbstractMarker.Raw(++currentBindMarkerIdx); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "value"


    // $ANTLR start "functionName"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:247:1: functionName returns [String s] : (f= IDENT | u= unreserved_function_keyword | K_TOKEN );
    public final String functionName() throws RecognitionException {
        String s = null;

        Token f=null;
        String u = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:248:5: (f= IDENT | u= unreserved_function_keyword | K_TOKEN )
            int alt17=3;
            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt17=1;
                }
                break;
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_COUNTER:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_TIMESTAMP:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
            case K_LIST:
            case K_KEY:
            case K_CLUSTERING:
            case K_COMPACT:
            case K_STORAGE:
            case K_TYPE:
            case K_VALUES:
            case K_FILTERING:
            case K_PERMISSION:
            case K_PERMISSIONS:
            case K_KEYSPACES:
            case K_ALL:
            case K_USER:
            case K_USERS:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_PASSWORD:
            case K_CUSTOM:
                {
                alt17=2;
                }
                break;
            case K_TOKEN:
                {
                alt17=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }

            switch (alt17) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:248:7: f= IDENT
                    {
                    f=(Token)match(input,IDENT,FOLLOW_IDENT_in_functionName950); 
                     s = (f!=null?f.getText():null); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:249:7: u= unreserved_function_keyword
                    {
                    pushFollow(FOLLOW_unreserved_function_keyword_in_functionName984);
                    u=unreserved_function_keyword();

                    state._fsp--;

                     s = u; 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:250:7: K_TOKEN
                    {
                    match(input,K_TOKEN,FOLLOW_K_TOKEN_in_functionName994); 
                     s = "token"; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return s;
    }
    // $ANTLR end "functionName"


    // $ANTLR start "functionArgs"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:253:1: functionArgs returns [List<Term.Raw> a] : ( '(' ')' | '(' t1= term ( ',' tn= term )* ')' );
    public final List<Term.Raw> functionArgs() throws RecognitionException {
        List<Term.Raw> a = null;

        Term.Raw t1 = null;

        Term.Raw tn = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:254:5: ( '(' ')' | '(' t1= term ( ',' tn= term )* ')' )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==140) ) {
                int LA19_1 = input.LA(2);

                if ( (LA19_1==141) ) {
                    alt19=1;
                }
                else if ( (LA19_1==IDENT||LA19_1==STRING_LITERAL||(LA19_1>=INTEGER && LA19_1<=K_TOKEN)||(LA19_1>=K_ASCII && LA19_1<=K_LIST)||(LA19_1>=K_KEY && LA19_1<=K_CUSTOM)||LA19_1==140||(LA19_1>=147 && LA19_1<=148)) ) {
                    alt19=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:254:7: '(' ')'
                    {
                    match(input,140,FOLLOW_140_in_functionArgs1039); 
                    match(input,141,FOLLOW_141_in_functionArgs1041); 
                     a = Collections.emptyList(); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:255:7: '(' t1= term ( ',' tn= term )* ')'
                    {
                    match(input,140,FOLLOW_140_in_functionArgs1051); 
                    pushFollow(FOLLOW_term_in_functionArgs1055);
                    t1=term();

                    state._fsp--;

                     List<Term.Raw> args = new ArrayList<Term.Raw>(); args.add(t1); 
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:256:11: ( ',' tn= term )*
                    loop18:
                    do {
                        int alt18=2;
                        int LA18_0 = input.LA(1);

                        if ( (LA18_0==145) ) {
                            alt18=1;
                        }


                        switch (alt18) {
                    	case 1 :
                    	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:256:13: ',' tn= term
                    	    {
                    	    match(input,145,FOLLOW_145_in_functionArgs1071); 
                    	    pushFollow(FOLLOW_term_in_functionArgs1075);
                    	    tn=term();

                    	    state._fsp--;

                    	     args.add(tn); 

                    	    }
                    	    break;

                    	default :
                    	    break loop18;
                        }
                    } while (true);

                    match(input,141,FOLLOW_141_in_functionArgs1089); 
                     a = args; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return a;
    }
    // $ANTLR end "functionArgs"


    // $ANTLR start "term"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:260:1: term returns [Term.Raw term] : (v= value | f= functionName args= functionArgs | '(' c= comparatorType ')' t= term );
    public final Term.Raw term() throws RecognitionException {
        Term.Raw term = null;

        Term.Raw v = null;

        String f = null;

        List<Term.Raw> args = null;

        CQL3Type c = null;

        Term.Raw t = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:261:5: (v= value | f= functionName args= functionArgs | '(' c= comparatorType ')' t= term )
            int alt20=3;
            switch ( input.LA(1) ) {
            case STRING_LITERAL:
            case INTEGER:
            case FLOAT:
            case BOOLEAN:
            case UUID:
            case HEXNUMBER:
            case K_NULL:
            case QMARK:
            case 147:
            case 148:
                {
                alt20=1;
                }
                break;
            case IDENT:
            case K_TOKEN:
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_COUNTER:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_TIMESTAMP:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
            case K_LIST:
            case K_KEY:
            case K_CLUSTERING:
            case K_COMPACT:
            case K_STORAGE:
            case K_TYPE:
            case K_VALUES:
            case K_FILTERING:
            case K_PERMISSION:
            case K_PERMISSIONS:
            case K_KEYSPACES:
            case K_ALL:
            case K_USER:
            case K_USERS:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_PASSWORD:
            case K_CUSTOM:
                {
                alt20=2;
                }
                break;
            case 140:
                {
                alt20=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }

            switch (alt20) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:261:7: v= value
                    {
                    pushFollow(FOLLOW_value_in_term1114);
                    v=value();

                    state._fsp--;

                     term = v; 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:262:7: f= functionName args= functionArgs
                    {
                    pushFollow(FOLLOW_functionName_in_term1151);
                    f=functionName();

                    state._fsp--;

                    pushFollow(FOLLOW_functionArgs_in_term1155);
                    args=functionArgs();

                    state._fsp--;

                     term = new FunctionCall.Raw(f, args); 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:263:7: '(' c= comparatorType ')' t= term
                    {
                    match(input,140,FOLLOW_140_in_term1165); 
                    pushFollow(FOLLOW_comparatorType_in_term1169);
                    c=comparatorType();

                    state._fsp--;

                    match(input,141,FOLLOW_141_in_term1171); 
                    pushFollow(FOLLOW_term_in_term1175);
                    t=term();

                    state._fsp--;

                     term = new TypeCast(c, t); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return term;
    }
    // $ANTLR end "term"


    // $ANTLR start "columnOperation"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:266:1: columnOperation[List<Pair<ColumnIdentifier, Operation.RawUpdate>> operations] : (key= cident '=' t= term ( '+' c= cident )? | key= cident '=' c= cident sig= ( '+' | '-' ) t= term | key= cident '=' c= cident i= INTEGER | key= cident '[' k= term ']' '=' t= term );
    public final void columnOperation(List<Pair<ColumnIdentifier, Operation.RawUpdate>> operations) throws RecognitionException {
        Token sig=null;
        Token i=null;
        ColumnIdentifier key = null;

        Term.Raw t = null;

        ColumnIdentifier c = null;

        Term.Raw k = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:267:5: (key= cident '=' t= term ( '+' c= cident )? | key= cident '=' c= cident sig= ( '+' | '-' ) t= term | key= cident '=' c= cident i= INTEGER | key= cident '[' k= term ']' '=' t= term )
            int alt22=4;
            alt22 = dfa22.predict(input);
            switch (alt22) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:267:7: key= cident '=' t= term ( '+' c= cident )?
                    {
                    pushFollow(FOLLOW_cident_in_columnOperation1198);
                    key=cident();

                    state._fsp--;

                    match(input,142,FOLLOW_142_in_columnOperation1200); 
                    pushFollow(FOLLOW_term_in_columnOperation1204);
                    t=term();

                    state._fsp--;

                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:267:29: ( '+' c= cident )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==150) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:267:30: '+' c= cident
                            {
                            match(input,150,FOLLOW_150_in_columnOperation1207); 
                            pushFollow(FOLLOW_cident_in_columnOperation1211);
                            c=cident();

                            state._fsp--;


                            }
                            break;

                    }


                              if (c == null)
                              {
                                  addRawUpdate(operations, key, new Operation.SetValue(t));
                              }
                              else
                              {
                                  if (!key.equals(c))
                                      addRecognitionError("Only expressions of the form X = <value> + X are supported.");
                                  addRawUpdate(operations, key, new Operation.Prepend(t));
                              }
                          

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:280:7: key= cident '=' c= cident sig= ( '+' | '-' ) t= term
                    {
                    pushFollow(FOLLOW_cident_in_columnOperation1232);
                    key=cident();

                    state._fsp--;

                    match(input,142,FOLLOW_142_in_columnOperation1234); 
                    pushFollow(FOLLOW_cident_in_columnOperation1238);
                    c=cident();

                    state._fsp--;

                    sig=(Token)input.LT(1);
                    if ( (input.LA(1)>=150 && input.LA(1)<=151) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_term_in_columnOperation1252);
                    t=term();

                    state._fsp--;


                              if (!key.equals(c))
                                  addRecognitionError("Only expressions of the form X = X " + (sig!=null?sig.getText():null) + "<value> are supported.");
                              addRawUpdate(operations, key, (sig!=null?sig.getText():null).equals("+") ? new Operation.Addition(t) : new Operation.Substraction(t));
                          

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:286:7: key= cident '=' c= cident i= INTEGER
                    {
                    pushFollow(FOLLOW_cident_in_columnOperation1270);
                    key=cident();

                    state._fsp--;

                    match(input,142,FOLLOW_142_in_columnOperation1272); 
                    pushFollow(FOLLOW_cident_in_columnOperation1276);
                    c=cident();

                    state._fsp--;

                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_columnOperation1280); 

                              // Note that this production *is* necessary because X = X - 3 will in fact be lexed as [ X, '=', X, INTEGER].
                              if (!key.equals(c))
                                  // We don't yet allow a '+' in front of an integer, but we could in the future really, so let's be future-proof in our error message
                                  addRecognitionError("Only expressions of the form X = X " + ((i!=null?i.getText():null).charAt(0) == '-' ? '-' : '+') + " <value> are supported.");
                              addRawUpdate(operations, key, new Operation.Addition(Constants.Literal.integer((i!=null?i.getText():null))));
                          

                    }
                    break;
                case 4 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:294:7: key= cident '[' k= term ']' '=' t= term
                    {
                    pushFollow(FOLLOW_cident_in_columnOperation1298);
                    key=cident();

                    state._fsp--;

                    match(input,148,FOLLOW_148_in_columnOperation1300); 
                    pushFollow(FOLLOW_term_in_columnOperation1304);
                    k=term();

                    state._fsp--;

                    match(input,149,FOLLOW_149_in_columnOperation1306); 
                    match(input,142,FOLLOW_142_in_columnOperation1308); 
                    pushFollow(FOLLOW_term_in_columnOperation1312);
                    t=term();

                    state._fsp--;


                              addRawUpdate(operations, key, new Operation.SetElement(k, t));
                          

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "columnOperation"


    // $ANTLR start "properties"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:300:1: properties[PropertyDefinitions props] : property[props] ( K_AND property[props] )* ;
    public final void properties(PropertyDefinitions props) throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:301:5: ( property[props] ( K_AND property[props] )* )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:301:7: property[props] ( K_AND property[props] )*
            {
            pushFollow(FOLLOW_property_in_properties1338);
            property(props);

            state._fsp--;

            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:301:23: ( K_AND property[props] )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==K_AND) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:301:24: K_AND property[props]
            	    {
            	    match(input,K_AND,FOLLOW_K_AND_in_properties1342); 
            	    pushFollow(FOLLOW_property_in_properties1344);
            	    property(props);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "properties"


    // $ANTLR start "property"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:304:1: property[PropertyDefinitions props] : k= cident '=' (simple= propertyValue | map= map_literal ) ;
    public final void property(PropertyDefinitions props) throws RecognitionException {
        ColumnIdentifier k = null;

        String simple = null;

        Maps.Literal map = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:305:5: (k= cident '=' (simple= propertyValue | map= map_literal ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:305:7: k= cident '=' (simple= propertyValue | map= map_literal )
            {
            pushFollow(FOLLOW_cident_in_property1367);
            k=cident();

            state._fsp--;

            match(input,142,FOLLOW_142_in_property1369); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:305:20: (simple= propertyValue | map= map_literal )
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==STRING_LITERAL||(LA24_0>=INTEGER && LA24_0<=HEXNUMBER)||(LA24_0>=K_ASCII && LA24_0<=K_LIST)||(LA24_0>=K_TTL && LA24_0<=K_CUSTOM)) ) {
                alt24=1;
            }
            else if ( (LA24_0==147) ) {
                alt24=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:305:21: simple= propertyValue
                    {
                    pushFollow(FOLLOW_propertyValue_in_property1374);
                    simple=propertyValue();

                    state._fsp--;

                     try { props.addProperty(k.toString(), simple); } catch (SyntaxException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:306:24: map= map_literal
                    {
                    pushFollow(FOLLOW_map_literal_in_property1403);
                    map=map_literal();

                    state._fsp--;

                     try { props.addProperty(k.toString(), convertPropertyMap(map)); } catch (SyntaxException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "property"


    // $ANTLR start "propertyValue"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:309:1: propertyValue returns [String str] : (c= constant | u= unreserved_keyword );
    public final String propertyValue() throws RecognitionException {
        String str = null;

        Constants.Literal c = null;

        String u = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:310:5: (c= constant | u= unreserved_keyword )
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==STRING_LITERAL||(LA25_0>=INTEGER && LA25_0<=HEXNUMBER)) ) {
                alt25=1;
            }
            else if ( ((LA25_0>=K_ASCII && LA25_0<=K_LIST)||(LA25_0>=K_TTL && LA25_0<=K_CUSTOM)) ) {
                alt25=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:310:7: c= constant
                    {
                    pushFollow(FOLLOW_constant_in_propertyValue1431);
                    c=constant();

                    state._fsp--;

                     str = c.getRawText(); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:311:7: u= unreserved_keyword
                    {
                    pushFollow(FOLLOW_unreserved_keyword_in_propertyValue1453);
                    u=unreserved_keyword();

                    state._fsp--;

                     str = u; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return str;
    }
    // $ANTLR end "propertyValue"


    // $ANTLR start "relationType"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:314:1: relationType returns [Relation.Type op] : ( '=' | '<' | '<=' | '>' | '>=' );
    public final Relation.Type relationType() throws RecognitionException {
        Relation.Type op = null;

        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:315:5: ( '=' | '<' | '<=' | '>' | '>=' )
            int alt26=5;
            switch ( input.LA(1) ) {
            case 142:
                {
                alt26=1;
                }
                break;
            case 152:
                {
                alt26=2;
                }
                break;
            case 153:
                {
                alt26=3;
                }
                break;
            case 154:
                {
                alt26=4;
                }
                break;
            case 155:
                {
                alt26=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }

            switch (alt26) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:315:7: '='
                    {
                    match(input,142,FOLLOW_142_in_relationType1476); 
                     op = Relation.Type.EQ; 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:316:7: '<'
                    {
                    match(input,152,FOLLOW_152_in_relationType1487); 
                     op = Relation.Type.LT; 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:317:7: '<='
                    {
                    match(input,153,FOLLOW_153_in_relationType1498); 
                     op = Relation.Type.LTE; 

                    }
                    break;
                case 4 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:318:7: '>'
                    {
                    match(input,154,FOLLOW_154_in_relationType1508); 
                     op = Relation.Type.GT; 

                    }
                    break;
                case 5 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:319:7: '>='
                    {
                    match(input,155,FOLLOW_155_in_relationType1519); 
                     op = Relation.Type.GTE; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return op;
    }
    // $ANTLR end "relationType"


    // $ANTLR start "relation"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:322:1: relation[List<Relation> clauses] : (name= cident type= relationType t= term | K_TOKEN '(' name1= cident ( ',' namen= cident )* ')' type= relationType t= term | name= cident K_IN '(' f1= term ( ',' fN= term )* ')' );
    public final void relation(List<Relation> clauses) throws RecognitionException {
        ColumnIdentifier name = null;

        Relation.Type type = null;

        Term.Raw t = null;

        ColumnIdentifier name1 = null;

        ColumnIdentifier namen = null;

        Term.Raw f1 = null;

        Term.Raw fN = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:323:5: (name= cident type= relationType t= term | K_TOKEN '(' name1= cident ( ',' namen= cident )* ')' type= relationType t= term | name= cident K_IN '(' f1= term ( ',' fN= term )* ')' )
            int alt29=3;
            alt29 = dfa29.predict(input);
            switch (alt29) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:323:7: name= cident type= relationType t= term
                    {
                    pushFollow(FOLLOW_cident_in_relation1541);
                    name=cident();

                    state._fsp--;

                    pushFollow(FOLLOW_relationType_in_relation1545);
                    type=relationType();

                    state._fsp--;

                    pushFollow(FOLLOW_term_in_relation1549);
                    t=term();

                    state._fsp--;

                     clauses.add(new Relation(name, type, t)); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:324:7: K_TOKEN '(' name1= cident ( ',' namen= cident )* ')' type= relationType t= term
                    {
                    match(input,K_TOKEN,FOLLOW_K_TOKEN_in_relation1559); 
                     List<ColumnIdentifier> l = new ArrayList<ColumnIdentifier>(); 
                    match(input,140,FOLLOW_140_in_relation1581); 
                    pushFollow(FOLLOW_cident_in_relation1585);
                    name1=cident();

                    state._fsp--;

                     l.add(name1); 
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:326:46: ( ',' namen= cident )*
                    loop27:
                    do {
                        int alt27=2;
                        int LA27_0 = input.LA(1);

                        if ( (LA27_0==145) ) {
                            alt27=1;
                        }


                        switch (alt27) {
                    	case 1 :
                    	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:326:48: ',' namen= cident
                    	    {
                    	    match(input,145,FOLLOW_145_in_relation1591); 
                    	    pushFollow(FOLLOW_cident_in_relation1595);
                    	    namen=cident();

                    	    state._fsp--;

                    	     l.add(namen); 

                    	    }
                    	    break;

                    	default :
                    	    break loop27;
                        }
                    } while (true);

                    match(input,141,FOLLOW_141_in_relation1601); 
                    pushFollow(FOLLOW_relationType_in_relation1613);
                    type=relationType();

                    state._fsp--;

                    pushFollow(FOLLOW_term_in_relation1617);
                    t=term();

                    state._fsp--;


                                for (ColumnIdentifier id : l)
                                    clauses.add(new Relation(id, type, t, true));
                            

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:332:7: name= cident K_IN '(' f1= term ( ',' fN= term )* ')'
                    {
                    pushFollow(FOLLOW_cident_in_relation1637);
                    name=cident();

                    state._fsp--;

                    match(input,K_IN,FOLLOW_K_IN_in_relation1639); 
                     Relation rel = Relation.createInRelation(name); 
                    match(input,140,FOLLOW_140_in_relation1650); 
                    pushFollow(FOLLOW_term_in_relation1654);
                    f1=term();

                    state._fsp--;

                     rel.addInValue(f1); 
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:333:44: ( ',' fN= term )*
                    loop28:
                    do {
                        int alt28=2;
                        int LA28_0 = input.LA(1);

                        if ( (LA28_0==145) ) {
                            alt28=1;
                        }


                        switch (alt28) {
                    	case 1 :
                    	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:333:45: ',' fN= term
                    	    {
                    	    match(input,145,FOLLOW_145_in_relation1659); 
                    	    pushFollow(FOLLOW_term_in_relation1663);
                    	    fN=term();

                    	    state._fsp--;

                    	     rel.addInValue(fN); 

                    	    }
                    	    break;

                    	default :
                    	    break loop28;
                        }
                    } while (true);

                    match(input,141,FOLLOW_141_in_relation1670); 
                     clauses.add(rel); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "relation"


    // $ANTLR start "comparatorType"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:336:1: comparatorType returns [CQL3Type t] : (c= native_type | c= collection_type | s= STRING_LITERAL );
    public final CQL3Type comparatorType() throws RecognitionException {
        CQL3Type t = null;

        Token s=null;
        CQL3Type c = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:337:5: (c= native_type | c= collection_type | s= STRING_LITERAL )
            int alt30=3;
            switch ( input.LA(1) ) {
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_COUNTER:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_TIMESTAMP:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
                {
                alt30=1;
                }
                break;
            case K_MAP:
            case K_LIST:
            case K_SET:
                {
                alt30=2;
                }
                break;
            case STRING_LITERAL:
                {
                alt30=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }

            switch (alt30) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:337:7: c= native_type
                    {
                    pushFollow(FOLLOW_native_type_in_comparatorType1695);
                    c=native_type();

                    state._fsp--;

                     t = c; 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:338:7: c= collection_type
                    {
                    pushFollow(FOLLOW_collection_type_in_comparatorType1711);
                    c=collection_type();

                    state._fsp--;

                     t = c; 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:339:7: s= STRING_LITERAL
                    {
                    s=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_comparatorType1723); 

                            try {
                                t = new CQL3Type.Custom((s!=null?s.getText():null));
                            } catch (SyntaxException e) {
                                addRecognitionError("Cannot parse type " + (s!=null?s.getText():null) + ": " + e.getMessage());
                            } catch (ConfigurationException e) {
                                addRecognitionError("Error setting type " + (s!=null?s.getText():null) + ": " + e.getMessage());
                            }
                          

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end "comparatorType"


    // $ANTLR start "native_type"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:351:1: native_type returns [CQL3Type t] : ( K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_TEXT | K_TIMESTAMP | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID );
    public final CQL3Type native_type() throws RecognitionException {
        CQL3Type t = null;

        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:352:5: ( K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_TEXT | K_TIMESTAMP | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID )
            int alt31=16;
            switch ( input.LA(1) ) {
            case K_ASCII:
                {
                alt31=1;
                }
                break;
            case K_BIGINT:
                {
                alt31=2;
                }
                break;
            case K_BLOB:
                {
                alt31=3;
                }
                break;
            case K_BOOLEAN:
                {
                alt31=4;
                }
                break;
            case K_COUNTER:
                {
                alt31=5;
                }
                break;
            case K_DECIMAL:
                {
                alt31=6;
                }
                break;
            case K_DOUBLE:
                {
                alt31=7;
                }
                break;
            case K_FLOAT:
                {
                alt31=8;
                }
                break;
            case K_INET:
                {
                alt31=9;
                }
                break;
            case K_INT:
                {
                alt31=10;
                }
                break;
            case K_TEXT:
                {
                alt31=11;
                }
                break;
            case K_TIMESTAMP:
                {
                alt31=12;
                }
                break;
            case K_UUID:
                {
                alt31=13;
                }
                break;
            case K_VARCHAR:
                {
                alt31=14;
                }
                break;
            case K_VARINT:
                {
                alt31=15;
                }
                break;
            case K_TIMEUUID:
                {
                alt31=16;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }

            switch (alt31) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:352:7: K_ASCII
                    {
                    match(input,K_ASCII,FOLLOW_K_ASCII_in_native_type1752); 
                     t = CQL3Type.Native.ASCII; 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:353:7: K_BIGINT
                    {
                    match(input,K_BIGINT,FOLLOW_K_BIGINT_in_native_type1766); 
                     t = CQL3Type.Native.BIGINT; 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:354:7: K_BLOB
                    {
                    match(input,K_BLOB,FOLLOW_K_BLOB_in_native_type1779); 
                     t = CQL3Type.Native.BLOB; 

                    }
                    break;
                case 4 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:355:7: K_BOOLEAN
                    {
                    match(input,K_BOOLEAN,FOLLOW_K_BOOLEAN_in_native_type1794); 
                     t = CQL3Type.Native.BOOLEAN; 

                    }
                    break;
                case 5 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:356:7: K_COUNTER
                    {
                    match(input,K_COUNTER,FOLLOW_K_COUNTER_in_native_type1806); 
                     t = CQL3Type.Native.COUNTER; 

                    }
                    break;
                case 6 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:357:7: K_DECIMAL
                    {
                    match(input,K_DECIMAL,FOLLOW_K_DECIMAL_in_native_type1818); 
                     t = CQL3Type.Native.DECIMAL; 

                    }
                    break;
                case 7 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:358:7: K_DOUBLE
                    {
                    match(input,K_DOUBLE,FOLLOW_K_DOUBLE_in_native_type1830); 
                     t = CQL3Type.Native.DOUBLE; 

                    }
                    break;
                case 8 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:359:7: K_FLOAT
                    {
                    match(input,K_FLOAT,FOLLOW_K_FLOAT_in_native_type1843); 
                     t = CQL3Type.Native.FLOAT; 

                    }
                    break;
                case 9 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:360:7: K_INET
                    {
                    match(input,K_INET,FOLLOW_K_INET_in_native_type1857); 
                     t = CQL3Type.Native.INET;

                    }
                    break;
                case 10 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:361:7: K_INT
                    {
                    match(input,K_INT,FOLLOW_K_INT_in_native_type1872); 
                     t = CQL3Type.Native.INT; 

                    }
                    break;
                case 11 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:362:7: K_TEXT
                    {
                    match(input,K_TEXT,FOLLOW_K_TEXT_in_native_type1888); 
                     t = CQL3Type.Native.TEXT; 

                    }
                    break;
                case 12 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:363:7: K_TIMESTAMP
                    {
                    match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_native_type1903); 
                     t = CQL3Type.Native.TIMESTAMP; 

                    }
                    break;
                case 13 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:364:7: K_UUID
                    {
                    match(input,K_UUID,FOLLOW_K_UUID_in_native_type1913); 
                     t = CQL3Type.Native.UUID; 

                    }
                    break;
                case 14 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:365:7: K_VARCHAR
                    {
                    match(input,K_VARCHAR,FOLLOW_K_VARCHAR_in_native_type1928); 
                     t = CQL3Type.Native.VARCHAR; 

                    }
                    break;
                case 15 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:366:7: K_VARINT
                    {
                    match(input,K_VARINT,FOLLOW_K_VARINT_in_native_type1940); 
                     t = CQL3Type.Native.VARINT; 

                    }
                    break;
                case 16 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:367:7: K_TIMEUUID
                    {
                    match(input,K_TIMEUUID,FOLLOW_K_TIMEUUID_in_native_type1953); 
                     t = CQL3Type.Native.TIMEUUID; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end "native_type"


    // $ANTLR start "collection_type"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:370:1: collection_type returns [CQL3Type pt] : ( K_MAP '<' t1= comparatorType ',' t2= comparatorType '>' | K_LIST '<' t= comparatorType '>' | K_SET '<' t= comparatorType '>' );
    public final CQL3Type collection_type() throws RecognitionException {
        CQL3Type pt = null;

        CQL3Type t1 = null;

        CQL3Type t2 = null;

        CQL3Type t = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:371:5: ( K_MAP '<' t1= comparatorType ',' t2= comparatorType '>' | K_LIST '<' t= comparatorType '>' | K_SET '<' t= comparatorType '>' )
            int alt32=3;
            switch ( input.LA(1) ) {
            case K_MAP:
                {
                alt32=1;
                }
                break;
            case K_LIST:
                {
                alt32=2;
                }
                break;
            case K_SET:
                {
                alt32=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }

            switch (alt32) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:371:7: K_MAP '<' t1= comparatorType ',' t2= comparatorType '>'
                    {
                    match(input,K_MAP,FOLLOW_K_MAP_in_collection_type1977); 
                    match(input,152,FOLLOW_152_in_collection_type1980); 
                    pushFollow(FOLLOW_comparatorType_in_collection_type1984);
                    t1=comparatorType();

                    state._fsp--;

                    match(input,145,FOLLOW_145_in_collection_type1986); 
                    pushFollow(FOLLOW_comparatorType_in_collection_type1990);
                    t2=comparatorType();

                    state._fsp--;

                    match(input,154,FOLLOW_154_in_collection_type1992); 
                     try {
                                // if we can't parse either t1 or t2, antlr will "recover" and we may have t1 or t2 null.
                                if (t1 != null && t2 != null)
                                    pt = CQL3Type.Collection.map(t1, t2);
                              } catch (InvalidRequestException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:377:7: K_LIST '<' t= comparatorType '>'
                    {
                    match(input,K_LIST,FOLLOW_K_LIST_in_collection_type2010); 
                    match(input,152,FOLLOW_152_in_collection_type2012); 
                    pushFollow(FOLLOW_comparatorType_in_collection_type2016);
                    t=comparatorType();

                    state._fsp--;

                    match(input,154,FOLLOW_154_in_collection_type2018); 
                     try { if (t != null) pt = CQL3Type.Collection.list(t); } catch (InvalidRequestException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;
                case 3 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:379:7: K_SET '<' t= comparatorType '>'
                    {
                    match(input,K_SET,FOLLOW_K_SET_in_collection_type2036); 
                    match(input,152,FOLLOW_152_in_collection_type2039); 
                    pushFollow(FOLLOW_comparatorType_in_collection_type2043);
                    t=comparatorType();

                    state._fsp--;

                    match(input,154,FOLLOW_154_in_collection_type2045); 
                     try { if (t != null) pt = CQL3Type.Collection.set(t); } catch (InvalidRequestException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return pt;
    }
    // $ANTLR end "collection_type"


    // $ANTLR start "username"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:383:1: username : ( IDENT | STRING_LITERAL );
    public final void username() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:384:5: ( IDENT | STRING_LITERAL )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:
            {
            if ( input.LA(1)==IDENT||input.LA(1)==STRING_LITERAL ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "username"


    // $ANTLR start "unreserved_keyword"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:388:1: unreserved_keyword returns [String str] : (u= unreserved_function_keyword | k= ( K_TTL | K_COUNT | K_WRITETIME ) );
    public final String unreserved_keyword() throws RecognitionException {
        String str = null;

        Token k=null;
        String u = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:389:5: (u= unreserved_function_keyword | k= ( K_TTL | K_COUNT | K_WRITETIME ) )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( ((LA33_0>=K_ASCII && LA33_0<=K_LIST)||(LA33_0>=K_KEY && LA33_0<=K_CUSTOM)) ) {
                alt33=1;
            }
            else if ( ((LA33_0>=K_TTL && LA33_0<=K_WRITETIME)) ) {
                alt33=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:389:7: u= unreserved_function_keyword
                    {
                    pushFollow(FOLLOW_unreserved_function_keyword_in_unreserved_keyword2103);
                    u=unreserved_function_keyword();

                    state._fsp--;

                     str = u; 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:390:7: k= ( K_TTL | K_COUNT | K_WRITETIME )
                    {
                    k=(Token)input.LT(1);
                    if ( (input.LA(1)>=K_TTL && input.LA(1)<=K_WRITETIME) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                     str = (k!=null?k.getText():null); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return str;
    }
    // $ANTLR end "unreserved_keyword"


    // $ANTLR start "unreserved_function_keyword"
    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:393:1: unreserved_function_keyword returns [String str] : (k= ( K_KEY | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_SUPERUSER | K_NOSUPERUSER | K_PASSWORD | K_CUSTOM ) | t= native_type );
    public final String unreserved_function_keyword() throws RecognitionException {
        String str = null;

        Token k=null;
        CQL3Type t = null;


        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:394:5: (k= ( K_KEY | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_SUPERUSER | K_NOSUPERUSER | K_PASSWORD | K_CUSTOM ) | t= native_type )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( ((LA34_0>=K_MAP && LA34_0<=K_LIST)||(LA34_0>=K_KEY && LA34_0<=K_CUSTOM)) ) {
                alt34=1;
            }
            else if ( ((LA34_0>=K_ASCII && LA34_0<=K_TIMEUUID)) ) {
                alt34=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:394:7: k= ( K_KEY | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_SUPERUSER | K_NOSUPERUSER | K_PASSWORD | K_CUSTOM )
                    {
                    k=(Token)input.LT(1);
                    if ( (input.LA(1)>=K_MAP && input.LA(1)<=K_LIST)||(input.LA(1)>=K_KEY && input.LA(1)<=K_CUSTOM) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                     str = (k!=null?k.getText():null); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:414:7: t= native_type
                    {
                    pushFollow(FOLLOW_native_type_in_unreserved_function_keyword2394);
                    t=native_type();

                    state._fsp--;

                     str = t.toString(); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return str;
    }
    // $ANTLR end "unreserved_function_keyword"

    // Delegated rules


    protected DFA6 dfa6 = new DFA6(this);
    protected DFA22 dfa22 = new DFA22(this);
    protected DFA29 dfa29 = new DFA29(this);
    static final String DFA6_eotS =
        "\27\uffff";
    static final String DFA6_eofS =
        "\27\uffff";
    static final String DFA6_minS =
        "\1\7\24\u008c\2\uffff";
    static final String DFA6_maxS =
        "\1\75\24\u008f\2\uffff";
    static final String DFA6_acceptS =
        "\25\uffff\1\1\1\2";
    static final String DFA6_specialS =
        "\27\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\1\4\uffff\1\2\12\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13"+
            "\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\2\3\1\uffff\3\24\21"+
            "\3",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "\1\26\2\uffff\1\25",
            "",
            ""
    };

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }
        public String getDescription() {
            return "192:7: ( cfOrKsName[name, true] '.' )?";
        }
    }
    static final String DFA22_eotS =
        "\56\uffff";
    static final String DFA22_eofS =
        "\56\uffff";
    static final String DFA22_minS =
        "\1\7\24\u008e\1\uffff\1\7\24\15\3\uffff";
    static final String DFA22_maxS =
        "\1\75\24\u0094\1\uffff\1\u0094\24\u0097\3\uffff";
    static final String DFA22_acceptS =
        "\25\uffff\1\4\25\uffff\1\1\1\3\1\2";
    static final String DFA22_specialS =
        "\56\uffff}>";
    static final String[] DFA22_transitionS = {
            "\1\1\4\uffff\1\2\12\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13"+
            "\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\2\3\1\uffff\3\24\21"+
            "\3",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "\1\26\5\uffff\1\25",
            "",
            "\1\27\3\uffff\1\53\1\30\10\53\2\uffff\1\32\1\33\1\34\1\35\1"+
            "\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\2"+
            "\31\1\uffff\3\52\21\31\116\uffff\1\53\6\uffff\2\53",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\u0088\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\176\uffff\1\53\11\uffff\2\55",
            "\1\54\u0088\uffff\2\55",
            "",
            "",
            ""
    };

    static final short[] DFA22_eot = DFA.unpackEncodedString(DFA22_eotS);
    static final short[] DFA22_eof = DFA.unpackEncodedString(DFA22_eofS);
    static final char[] DFA22_min = DFA.unpackEncodedStringToUnsignedChars(DFA22_minS);
    static final char[] DFA22_max = DFA.unpackEncodedStringToUnsignedChars(DFA22_maxS);
    static final short[] DFA22_accept = DFA.unpackEncodedString(DFA22_acceptS);
    static final short[] DFA22_special = DFA.unpackEncodedString(DFA22_specialS);
    static final short[][] DFA22_transition;

    static {
        int numStates = DFA22_transitionS.length;
        DFA22_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA22_transition[i] = DFA.unpackEncodedString(DFA22_transitionS[i]);
        }
    }

    class DFA22 extends DFA {

        public DFA22(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 22;
            this.eot = DFA22_eot;
            this.eof = DFA22_eof;
            this.min = DFA22_min;
            this.max = DFA22_max;
            this.accept = DFA22_accept;
            this.special = DFA22_special;
            this.transition = DFA22_transition;
        }
        public String getDescription() {
            return "266:1: columnOperation[List<Pair<ColumnIdentifier, Operation.RawUpdate>> operations] : (key= cident '=' t= term ( '+' c= cident )? | key= cident '=' c= cident sig= ( '+' | '-' ) t= term | key= cident '=' c= cident i= INTEGER | key= cident '[' k= term ']' '=' t= term );";
        }
    }
    static final String DFA29_eotS =
        "\30\uffff";
    static final String DFA29_eofS =
        "\30\uffff";
    static final String DFA29_minS =
        "\1\7\24\26\3\uffff";
    static final String DFA29_maxS =
        "\1\75\24\u009b\3\uffff";
    static final String DFA29_acceptS =
        "\25\uffff\1\2\1\3\1\1";
    static final String DFA29_specialS =
        "\30\uffff}>";
    static final String[] DFA29_transitionS = {
            "\1\1\4\uffff\1\2\7\uffff\1\25\2\uffff\1\4\1\5\1\6\1\7\1\10\1"+
            "\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\2\3\1"+
            "\uffff\3\24\21\3",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "\1\26\167\uffff\1\27\11\uffff\4\27",
            "",
            "",
            ""
    };

    static final short[] DFA29_eot = DFA.unpackEncodedString(DFA29_eotS);
    static final short[] DFA29_eof = DFA.unpackEncodedString(DFA29_eofS);
    static final char[] DFA29_min = DFA.unpackEncodedStringToUnsignedChars(DFA29_minS);
    static final char[] DFA29_max = DFA.unpackEncodedStringToUnsignedChars(DFA29_maxS);
    static final short[] DFA29_accept = DFA.unpackEncodedString(DFA29_acceptS);
    static final short[] DFA29_special = DFA.unpackEncodedString(DFA29_specialS);
    static final short[][] DFA29_transition;

    static {
        int numStates = DFA29_transitionS.length;
        DFA29_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA29_transition[i] = DFA.unpackEncodedString(DFA29_transitionS[i]);
        }
    }

    class DFA29 extends DFA {

        public DFA29(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 29;
            this.eot = DFA29_eot;
            this.eof = DFA29_eof;
            this.min = DFA29_min;
            this.max = DFA29_max;
            this.accept = DFA29_accept;
            this.special = DFA29_special;
            this.transition = DFA29_transition;
        }
        public String getDescription() {
            return "322:1: relation[List<Relation> clauses] : (name= cident type= relationType t= term | K_TOKEN '(' name1= cident ( ',' namen= cident )* ')' type= relationType t= term | name= cident K_IN '(' f1= term ( ',' fN= term )* ')' );";
        }
    }
 

    public static final BitSet FOLLOW_xcqlStatement_in_query69 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_139_in_query72 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_EOF_in_query76 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createIndexStatement_in_xcqlStatement110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_X_CREATE_in_createIndexStatement157 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_X_ROW_in_createIndexStatement160 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_K_INDEX_in_createIndexStatement166 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_IDENT_in_createIndexStatement171 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_K_ON_in_createIndexStatement175 = new BitSet(new long[]{0x3FFFFDFFFF801080L});
    public static final BitSet FOLLOW_columnFamilyName_in_createIndexStatement179 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_140_in_createIndexStatement181 = new BitSet(new long[]{0x3FFFFDFFFF801080L});
    public static final BitSet FOLLOW_cident_in_createIndexStatement185 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_141_in_createIndexStatement187 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_K_WITH_in_createIndexStatement197 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_X_OPTIONS_in_createIndexStatement199 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_142_in_createIndexStatement201 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_createIndexStatement205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cident244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_NAME_in_cident269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_keyword_in_cident288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cfOrKsName_in_keyspaceName321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cfOrKsName_in_columnFamilyName355 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_143_in_columnFamilyName358 = new BitSet(new long[]{0x3FFFFDFFFF801080L});
    public static final BitSet FOLLOW_cfOrKsName_in_columnFamilyName362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cfOrKsName383 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_NAME_in_cfOrKsName408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_keyword_in_cfOrKsName427 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_constant452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_constant464 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_constant483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOLEAN_in_constant504 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UUID_in_constant523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HEXNUMBER_in_constant545 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_144_in_set_tail570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_145_in_set_tail578 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_set_tail582 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000030000L});
    public static final BitSet FOLLOW_set_tail_in_set_tail586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_144_in_map_tail605 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_145_in_map_tail613 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_map_tail617 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_146_in_map_tail619 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_map_tail623 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000030000L});
    public static final BitSet FOLLOW_map_tail_in_map_tail627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_147_in_map_literal649 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_144_in_map_literal651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_147_in_map_literal661 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_map_literal677 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_146_in_map_literal679 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_map_literal683 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000030000L});
    public static final BitSet FOLLOW_map_tail_in_map_literal687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_146_in_set_or_map719 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_set_or_map723 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000030000L});
    public static final BitSet FOLLOW_map_tail_in_set_or_map727 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_tail_in_set_or_map740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_148_in_collection_literal765 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000381000L});
    public static final BitSet FOLLOW_term_in_collection_literal773 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000220000L});
    public static final BitSet FOLLOW_145_in_collection_literal779 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_collection_literal783 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000220000L});
    public static final BitSet FOLLOW_149_in_collection_literal793 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_147_in_collection_literal803 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_collection_literal807 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000070000L});
    public static final BitSet FOLLOW_set_or_map_in_collection_literal811 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_147_in_collection_literal827 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_144_in_collection_literal829 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_value854 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_literal_in_value876 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_NULL_in_value886 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QMARK_in_value910 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_function_keyword_in_functionName984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TOKEN_in_functionName994 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_140_in_functionArgs1039 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_141_in_functionArgs1041 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_140_in_functionArgs1051 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_functionArgs1055 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000022000L});
    public static final BitSet FOLLOW_145_in_functionArgs1071 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_functionArgs1075 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000022000L});
    public static final BitSet FOLLOW_141_in_functionArgs1089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_term1114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_term1151 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_functionArgs_in_term1155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_140_in_term1165 = new BitSet(new long[]{0x3FFFE3FFFF800800L});
    public static final BitSet FOLLOW_comparatorType_in_term1169 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_141_in_term1171 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_term1175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_columnOperation1198 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_142_in_columnOperation1200 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_columnOperation1204 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_150_in_columnOperation1207 = new BitSet(new long[]{0x3FFFFDFFFF801080L});
    public static final BitSet FOLLOW_cident_in_columnOperation1211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_columnOperation1232 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_142_in_columnOperation1234 = new BitSet(new long[]{0x3FFFFDFFFF801080L});
    public static final BitSet FOLLOW_cident_in_columnOperation1238 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000C00000L});
    public static final BitSet FOLLOW_set_in_columnOperation1242 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_columnOperation1252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_columnOperation1270 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_142_in_columnOperation1272 = new BitSet(new long[]{0x3FFFFDFFFF801080L});
    public static final BitSet FOLLOW_cident_in_columnOperation1276 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_INTEGER_in_columnOperation1280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_columnOperation1298 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_148_in_columnOperation1300 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_columnOperation1304 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_149_in_columnOperation1306 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_142_in_columnOperation1308 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_columnOperation1312 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_properties1338 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_K_AND_in_properties1342 = new BitSet(new long[]{0x3FFFFDFFFF801080L});
    public static final BitSet FOLLOW_property_in_properties1344 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_cident_in_property1367 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_142_in_property1369 = new BitSet(new long[]{0x3FFFFDFFFF83F880L,0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_propertyValue_in_property1374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_map_literal_in_property1403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_propertyValue1431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_keyword_in_propertyValue1453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_142_in_relationType1476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_152_in_relationType1487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_153_in_relationType1498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_154_in_relationType1508 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_155_in_relationType1519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_relation1541 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x000000000F004000L});
    public static final BitSet FOLLOW_relationType_in_relation1545 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_relation1549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TOKEN_in_relation1559 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_140_in_relation1581 = new BitSet(new long[]{0x3FFFFDFFFF801080L});
    public static final BitSet FOLLOW_cident_in_relation1585 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000022000L});
    public static final BitSet FOLLOW_145_in_relation1591 = new BitSet(new long[]{0x3FFFFDFFFF801080L});
    public static final BitSet FOLLOW_cident_in_relation1595 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000022000L});
    public static final BitSet FOLLOW_141_in_relation1601 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x000000000F004000L});
    public static final BitSet FOLLOW_relationType_in_relation1613 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_relation1617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_relation1637 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_K_IN_in_relation1639 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_140_in_relation1650 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_relation1654 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000022000L});
    public static final BitSet FOLLOW_145_in_relation1659 = new BitSet(new long[]{0x3FFFE1FFFF9FE880L,0x0000000000000000L,0x0000000000181000L});
    public static final BitSet FOLLOW_term_in_relation1663 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000022000L});
    public static final BitSet FOLLOW_141_in_relation1670 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_native_type_in_comparatorType1695 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_type_in_comparatorType1711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_comparatorType1723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ASCII_in_native_type1752 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_BIGINT_in_native_type1766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_BLOB_in_native_type1779 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_BOOLEAN_in_native_type1794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_COUNTER_in_native_type1806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DECIMAL_in_native_type1818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DOUBLE_in_native_type1830 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_FLOAT_in_native_type1843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_INET_in_native_type1857 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_INT_in_native_type1872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TEXT_in_native_type1888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TIMESTAMP_in_native_type1903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_UUID_in_native_type1913 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_VARCHAR_in_native_type1928 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_VARINT_in_native_type1940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TIMEUUID_in_native_type1953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_MAP_in_collection_type1977 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_152_in_collection_type1980 = new BitSet(new long[]{0x3FFFE3FFFF800800L});
    public static final BitSet FOLLOW_comparatorType_in_collection_type1984 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_145_in_collection_type1986 = new BitSet(new long[]{0x3FFFE3FFFF800800L});
    public static final BitSet FOLLOW_comparatorType_in_collection_type1990 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_154_in_collection_type1992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_LIST_in_collection_type2010 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_152_in_collection_type2012 = new BitSet(new long[]{0x3FFFE3FFFF800800L});
    public static final BitSet FOLLOW_comparatorType_in_collection_type2016 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_154_in_collection_type2018 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_SET_in_collection_type2036 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_152_in_collection_type2039 = new BitSet(new long[]{0x3FFFE3FFFF800800L});
    public static final BitSet FOLLOW_comparatorType_in_collection_type2043 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_154_in_collection_type2045 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_username0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_function_keyword_in_unreserved_keyword2103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unreserved_keyword2119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unreserved_function_keyword2154 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_native_type_in_unreserved_function_keyword2394 = new BitSet(new long[]{0x0000000000000002L});

}