// $ANTLR 3.2 Sep 23, 2009 14:05:07 src/main/java/com/tuplejump/stargate/xcql/XCql.g 2014-04-20 20:58:15

    package com.tuplejump.stargate.xcql.ast;

    import org.apache.cassandra.exceptions.SyntaxException;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class XCqlLexer extends Lexer {
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
    public static final int T__147=147;
    public static final int K_GRANT=123;
    public static final int T__149=149;
    public static final int K_ON=8;
    public static final int K_USING=99;
    public static final int K_ADD=117;
    public static final int K_ASC=120;
    public static final int K_CUSTOM=61;
    public static final int K_KEY=45;
    public static final int T__154=154;
    public static final int K_TRUNCATE=106;
    public static final int COMMENT=137;
    public static final int T__155=155;
    public static final int T__150=150;
    public static final int T__151=151;
    public static final int K_ORDER=118;
    public static final int K_ALL=55;
    public static final int T__152=152;
    public static final int HEXNUMBER=17;
    public static final int K_OF=124;
    public static final int T__153=153;
    public static final int T__139=139;
    public static final int D=89;
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
    public static final int QMARK=19;
    public static final int K_TOKEN=20;
    public static final int K_UUID=35;
    public static final int K_BATCH=104;
    public static final int K_ASCII=23;
    public static final int X_LIKE=82;
    public static final int UUID=16;
    public static final int K_LIST=40;
    public static final int K_DELETE=107;
    public static final int K_TO=111;
    public static final int K_BY=119;
    public static final int FLOAT=14;
    public static final int K_SUPERUSER=58;
    public static final int K_VARINT=37;
    public static final int K_FLOAT=30;
    public static final int K_DOUBLE=29;
    public static final int K_SELECT=91;
    public static final int K_LIMIT=98;
    public static final int K_BOOLEAN=26;
    public static final int K_ALTER=115;
    public static final int K_SET=41;
    public static final int K_WHERE=94;
    public static final int QUOTED_NAME=12;
    public static final int MULTILINE_COMMENT=138;
    public static final int BOOLEAN=15;
    public static final int K_BLOB=25;
    public static final int K_UNLOGGED=103;
    public static final int K_INTO=114;
    public static final int HEX=134;
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
    public static final int K_TIMEUUID=38;
    public static final int X_ROW=5;
    public static final int K_USER=56;

        List<Token> tokens = new ArrayList<Token>();

        public void emit(Token token)
        {
            state.token = token;
            tokens.add(token);
        }

        public Token nextToken()
        {
            super.nextToken();
            if (tokens.size() == 0)
                return Token.EOF_TOKEN;
            return tokens.remove(0);
        }

        private List<String> recognitionErrors = new ArrayList<String>();

        public void displayRecognitionError(String[] tokenNames, RecognitionException e)
        {
            String hdr = getErrorHeader(e);
            String msg = getErrorMessage(e, tokenNames);
            recognitionErrors.add(hdr + " " + msg);
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


    // delegates
    // delegators

    public XCqlLexer() {;} 
    public XCqlLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public XCqlLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "src/main/java/com/tuplejump/stargate/xcql/XCql.g"; }

    // $ANTLR start "T__139"
    public final void mT__139() throws RecognitionException {
        try {
            int _type = T__139;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:50:8: ( ';' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:50:10: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__139"

    // $ANTLR start "T__140"
    public final void mT__140() throws RecognitionException {
        try {
            int _type = T__140;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:51:8: ( '(' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:51:10: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__140"

    // $ANTLR start "T__141"
    public final void mT__141() throws RecognitionException {
        try {
            int _type = T__141;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:52:8: ( ')' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:52:10: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__141"

    // $ANTLR start "T__142"
    public final void mT__142() throws RecognitionException {
        try {
            int _type = T__142;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:53:8: ( '=' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:53:10: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__142"

    // $ANTLR start "T__143"
    public final void mT__143() throws RecognitionException {
        try {
            int _type = T__143;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:54:8: ( '.' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:54:10: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__143"

    // $ANTLR start "T__144"
    public final void mT__144() throws RecognitionException {
        try {
            int _type = T__144;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:55:8: ( '}' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:55:10: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__144"

    // $ANTLR start "T__145"
    public final void mT__145() throws RecognitionException {
        try {
            int _type = T__145;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:56:8: ( ',' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:56:10: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__145"

    // $ANTLR start "T__146"
    public final void mT__146() throws RecognitionException {
        try {
            int _type = T__146;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:57:8: ( ':' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:57:10: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__146"

    // $ANTLR start "T__147"
    public final void mT__147() throws RecognitionException {
        try {
            int _type = T__147;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:58:8: ( '{' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:58:10: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__147"

    // $ANTLR start "T__148"
    public final void mT__148() throws RecognitionException {
        try {
            int _type = T__148;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:59:8: ( '[' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:59:10: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__148"

    // $ANTLR start "T__149"
    public final void mT__149() throws RecognitionException {
        try {
            int _type = T__149;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:60:8: ( ']' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:60:10: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__149"

    // $ANTLR start "T__150"
    public final void mT__150() throws RecognitionException {
        try {
            int _type = T__150;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:61:8: ( '+' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:61:10: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__150"

    // $ANTLR start "T__151"
    public final void mT__151() throws RecognitionException {
        try {
            int _type = T__151;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:62:8: ( '-' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:62:10: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__151"

    // $ANTLR start "T__152"
    public final void mT__152() throws RecognitionException {
        try {
            int _type = T__152;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:63:8: ( '<' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:63:10: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__152"

    // $ANTLR start "T__153"
    public final void mT__153() throws RecognitionException {
        try {
            int _type = T__153;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:64:8: ( '<=' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:64:10: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__153"

    // $ANTLR start "T__154"
    public final void mT__154() throws RecognitionException {
        try {
            int _type = T__154;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:65:8: ( '>' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:65:10: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__154"

    // $ANTLR start "T__155"
    public final void mT__155() throws RecognitionException {
        try {
            int _type = T__155;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:66:8: ( '>=' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:66:10: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__155"

    // $ANTLR start "X_SELECT"
    public final void mX_SELECT() throws RecognitionException {
        try {
            int _type = X_SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:419:9: ( X S E L E C T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:419:16: X S E L E C T
            {
            mX(); 
            mS(); 
            mE(); 
            mL(); 
            mE(); 
            mC(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_SELECT"

    // $ANTLR start "X_CREATE"
    public final void mX_CREATE() throws RecognitionException {
        try {
            int _type = X_CREATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:420:9: ( X C R E A T E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:420:16: X C R E A T E
            {
            mX(); 
            mC(); 
            mR(); 
            mE(); 
            mA(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_CREATE"

    // $ANTLR start "X_GROUP"
    public final void mX_GROUP() throws RecognitionException {
        try {
            int _type = X_GROUP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:421:8: ( G R O U P )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:421:16: G R O U P
            {
            mG(); 
            mR(); 
            mO(); 
            mU(); 
            mP(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_GROUP"

    // $ANTLR start "X_HAVING"
    public final void mX_HAVING() throws RecognitionException {
        try {
            int _type = X_HAVING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:422:9: ( H A V I N G )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:422:16: H A V I N G
            {
            mH(); 
            mA(); 
            mV(); 
            mI(); 
            mN(); 
            mG(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_HAVING"

    // $ANTLR start "X_LIKE"
    public final void mX_LIKE() throws RecognitionException {
        try {
            int _type = X_LIKE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:423:7: ( L I K E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:423:16: L I K E
            {
            mL(); 
            mI(); 
            mK(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_LIKE"

    // $ANTLR start "X_SKIP"
    public final void mX_SKIP() throws RecognitionException {
        try {
            int _type = X_SKIP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:424:7: ( S K I P )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:424:16: S K I P
            {
            mS(); 
            mK(); 
            mI(); 
            mP(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_SKIP"

    // $ANTLR start "X_ROW"
    public final void mX_ROW() throws RecognitionException {
        try {
            int _type = X_ROW;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:425:6: ( R O W )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:425:16: R O W
            {
            mR(); 
            mO(); 
            mW(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_ROW"

    // $ANTLR start "X_ROW_META"
    public final void mX_ROW_META() throws RecognitionException {
        try {
            int _type = X_ROW_META;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:426:11: ( X R O W M E T A )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:426:16: X R O W M E T A
            {
            mX(); 
            mR(); 
            mO(); 
            mW(); 
            mM(); 
            mE(); 
            mT(); 
            mA(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_ROW_META"

    // $ANTLR start "X_COL"
    public final void mX_COL() throws RecognitionException {
        try {
            int _type = X_COL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:427:6: ( C O L )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:427:16: C O L
            {
            mC(); 
            mO(); 
            mL(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_COL"

    // $ANTLR start "X_OR"
    public final void mX_OR() throws RecognitionException {
        try {
            int _type = X_OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:428:5: ( O R )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:428:16: O R
            {
            mO(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_OR"

    // $ANTLR start "X_UNLIM"
    public final void mX_UNLIM() throws RecognitionException {
        try {
            int _type = X_UNLIM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:429:8: ( U N L I M I T E D )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:429:16: U N L I M I T E D
            {
            mU(); 
            mN(); 
            mL(); 
            mI(); 
            mM(); 
            mI(); 
            mT(); 
            mE(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_UNLIM"

    // $ANTLR start "X_OPTIONS"
    public final void mX_OPTIONS() throws RecognitionException {
        try {
            int _type = X_OPTIONS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:430:10: ( O P T I O N S )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:430:16: O P T I O N S
            {
            mO(); 
            mP(); 
            mT(); 
            mI(); 
            mO(); 
            mN(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X_OPTIONS"

    // $ANTLR start "K_SELECT"
    public final void mK_SELECT() throws RecognitionException {
        try {
            int _type = K_SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:431:9: ( S E L E C T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:431:16: S E L E C T
            {
            mS(); 
            mE(); 
            mL(); 
            mE(); 
            mC(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_SELECT"

    // $ANTLR start "K_FROM"
    public final void mK_FROM() throws RecognitionException {
        try {
            int _type = K_FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:432:7: ( F R O M )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:432:16: F R O M
            {
            mF(); 
            mR(); 
            mO(); 
            mM(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_FROM"

    // $ANTLR start "K_WHERE"
    public final void mK_WHERE() throws RecognitionException {
        try {
            int _type = K_WHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:433:8: ( W H E R E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:433:16: W H E R E
            {
            mW(); 
            mH(); 
            mE(); 
            mR(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_WHERE"

    // $ANTLR start "K_AND"
    public final void mK_AND() throws RecognitionException {
        try {
            int _type = K_AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:434:6: ( A N D )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:434:16: A N D
            {
            mA(); 
            mN(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_AND"

    // $ANTLR start "K_KEY"
    public final void mK_KEY() throws RecognitionException {
        try {
            int _type = K_KEY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:435:6: ( K E Y )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:435:16: K E Y
            {
            mK(); 
            mE(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_KEY"

    // $ANTLR start "K_INSERT"
    public final void mK_INSERT() throws RecognitionException {
        try {
            int _type = K_INSERT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:436:9: ( I N S E R T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:436:16: I N S E R T
            {
            mI(); 
            mN(); 
            mS(); 
            mE(); 
            mR(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_INSERT"

    // $ANTLR start "K_UPDATE"
    public final void mK_UPDATE() throws RecognitionException {
        try {
            int _type = K_UPDATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:437:9: ( U P D A T E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:437:16: U P D A T E
            {
            mU(); 
            mP(); 
            mD(); 
            mA(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_UPDATE"

    // $ANTLR start "K_WITH"
    public final void mK_WITH() throws RecognitionException {
        try {
            int _type = K_WITH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:438:7: ( W I T H )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:438:16: W I T H
            {
            mW(); 
            mI(); 
            mT(); 
            mH(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_WITH"

    // $ANTLR start "K_LIMIT"
    public final void mK_LIMIT() throws RecognitionException {
        try {
            int _type = K_LIMIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:439:8: ( L I M I T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:439:16: L I M I T
            {
            mL(); 
            mI(); 
            mM(); 
            mI(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_LIMIT"

    // $ANTLR start "K_USING"
    public final void mK_USING() throws RecognitionException {
        try {
            int _type = K_USING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:440:8: ( U S I N G )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:440:16: U S I N G
            {
            mU(); 
            mS(); 
            mI(); 
            mN(); 
            mG(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_USING"

    // $ANTLR start "K_USE"
    public final void mK_USE() throws RecognitionException {
        try {
            int _type = K_USE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:441:6: ( U S E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:441:16: U S E
            {
            mU(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_USE"

    // $ANTLR start "K_COUNT"
    public final void mK_COUNT() throws RecognitionException {
        try {
            int _type = K_COUNT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:442:8: ( C O U N T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:442:16: C O U N T
            {
            mC(); 
            mO(); 
            mU(); 
            mN(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_COUNT"

    // $ANTLR start "K_SET"
    public final void mK_SET() throws RecognitionException {
        try {
            int _type = K_SET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:443:6: ( S E T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:443:16: S E T
            {
            mS(); 
            mE(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_SET"

    // $ANTLR start "K_BEGIN"
    public final void mK_BEGIN() throws RecognitionException {
        try {
            int _type = K_BEGIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:444:8: ( B E G I N )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:444:16: B E G I N
            {
            mB(); 
            mE(); 
            mG(); 
            mI(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_BEGIN"

    // $ANTLR start "K_UNLOGGED"
    public final void mK_UNLOGGED() throws RecognitionException {
        try {
            int _type = K_UNLOGGED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:445:11: ( U N L O G G E D )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:445:16: U N L O G G E D
            {
            mU(); 
            mN(); 
            mL(); 
            mO(); 
            mG(); 
            mG(); 
            mE(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_UNLOGGED"

    // $ANTLR start "K_BATCH"
    public final void mK_BATCH() throws RecognitionException {
        try {
            int _type = K_BATCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:446:8: ( B A T C H )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:446:16: B A T C H
            {
            mB(); 
            mA(); 
            mT(); 
            mC(); 
            mH(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_BATCH"

    // $ANTLR start "K_APPLY"
    public final void mK_APPLY() throws RecognitionException {
        try {
            int _type = K_APPLY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:447:8: ( A P P L Y )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:447:16: A P P L Y
            {
            mA(); 
            mP(); 
            mP(); 
            mL(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_APPLY"

    // $ANTLR start "K_TRUNCATE"
    public final void mK_TRUNCATE() throws RecognitionException {
        try {
            int _type = K_TRUNCATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:448:11: ( T R U N C A T E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:448:16: T R U N C A T E
            {
            mT(); 
            mR(); 
            mU(); 
            mN(); 
            mC(); 
            mA(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TRUNCATE"

    // $ANTLR start "K_DELETE"
    public final void mK_DELETE() throws RecognitionException {
        try {
            int _type = K_DELETE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:449:9: ( D E L E T E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:449:16: D E L E T E
            {
            mD(); 
            mE(); 
            mL(); 
            mE(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_DELETE"

    // $ANTLR start "K_IN"
    public final void mK_IN() throws RecognitionException {
        try {
            int _type = K_IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:450:5: ( I N )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:450:16: I N
            {
            mI(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_IN"

    // $ANTLR start "K_CREATE"
    public final void mK_CREATE() throws RecognitionException {
        try {
            int _type = K_CREATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:451:9: ( C R E A T E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:451:16: C R E A T E
            {
            mC(); 
            mR(); 
            mE(); 
            mA(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_CREATE"

    // $ANTLR start "K_KEYSPACE"
    public final void mK_KEYSPACE() throws RecognitionException {
        try {
            int _type = K_KEYSPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:452:11: ( ( K E Y S P A C E | S C H E M A ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:452:16: ( K E Y S P A C E | S C H E M A )
            {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:452:16: ( K E Y S P A C E | S C H E M A )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='K'||LA1_0=='k') ) {
                alt1=1;
            }
            else if ( (LA1_0=='S'||LA1_0=='s') ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:452:18: K E Y S P A C E
                    {
                    mK(); 
                    mE(); 
                    mY(); 
                    mS(); 
                    mP(); 
                    mA(); 
                    mC(); 
                    mE(); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:453:20: S C H E M A
                    {
                    mS(); 
                    mC(); 
                    mH(); 
                    mE(); 
                    mM(); 
                    mA(); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_KEYSPACE"

    // $ANTLR start "K_KEYSPACES"
    public final void mK_KEYSPACES() throws RecognitionException {
        try {
            int _type = K_KEYSPACES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:454:12: ( K E Y S P A C E S )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:454:16: K E Y S P A C E S
            {
            mK(); 
            mE(); 
            mY(); 
            mS(); 
            mP(); 
            mA(); 
            mC(); 
            mE(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_KEYSPACES"

    // $ANTLR start "K_COLUMNFAMILY"
    public final void mK_COLUMNFAMILY() throws RecognitionException {
        try {
            int _type = K_COLUMNFAMILY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:455:15: ( ( C O L U M N F A M I L Y | T A B L E ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:455:16: ( C O L U M N F A M I L Y | T A B L E )
            {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:455:16: ( C O L U M N F A M I L Y | T A B L E )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='C'||LA2_0=='c') ) {
                alt2=1;
            }
            else if ( (LA2_0=='T'||LA2_0=='t') ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:455:18: C O L U M N F A M I L Y
                    {
                    mC(); 
                    mO(); 
                    mL(); 
                    mU(); 
                    mM(); 
                    mN(); 
                    mF(); 
                    mA(); 
                    mM(); 
                    mI(); 
                    mL(); 
                    mY(); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:456:20: T A B L E
                    {
                    mT(); 
                    mA(); 
                    mB(); 
                    mL(); 
                    mE(); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_COLUMNFAMILY"

    // $ANTLR start "K_INDEX"
    public final void mK_INDEX() throws RecognitionException {
        try {
            int _type = K_INDEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:457:8: ( I N D E X )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:457:16: I N D E X
            {
            mI(); 
            mN(); 
            mD(); 
            mE(); 
            mX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_INDEX"

    // $ANTLR start "K_CUSTOM"
    public final void mK_CUSTOM() throws RecognitionException {
        try {
            int _type = K_CUSTOM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:458:9: ( C U S T O M )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:458:16: C U S T O M
            {
            mC(); 
            mU(); 
            mS(); 
            mT(); 
            mO(); 
            mM(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_CUSTOM"

    // $ANTLR start "K_ON"
    public final void mK_ON() throws RecognitionException {
        try {
            int _type = K_ON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:459:5: ( O N )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:459:16: O N
            {
            mO(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ON"

    // $ANTLR start "K_TO"
    public final void mK_TO() throws RecognitionException {
        try {
            int _type = K_TO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:460:5: ( T O )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:460:16: T O
            {
            mT(); 
            mO(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TO"

    // $ANTLR start "K_DROP"
    public final void mK_DROP() throws RecognitionException {
        try {
            int _type = K_DROP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:461:7: ( D R O P )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:461:16: D R O P
            {
            mD(); 
            mR(); 
            mO(); 
            mP(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_DROP"

    // $ANTLR start "K_PRIMARY"
    public final void mK_PRIMARY() throws RecognitionException {
        try {
            int _type = K_PRIMARY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:462:10: ( P R I M A R Y )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:462:16: P R I M A R Y
            {
            mP(); 
            mR(); 
            mI(); 
            mM(); 
            mA(); 
            mR(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_PRIMARY"

    // $ANTLR start "K_INTO"
    public final void mK_INTO() throws RecognitionException {
        try {
            int _type = K_INTO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:463:7: ( I N T O )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:463:16: I N T O
            {
            mI(); 
            mN(); 
            mT(); 
            mO(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_INTO"

    // $ANTLR start "K_VALUES"
    public final void mK_VALUES() throws RecognitionException {
        try {
            int _type = K_VALUES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:464:9: ( V A L U E S )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:464:16: V A L U E S
            {
            mV(); 
            mA(); 
            mL(); 
            mU(); 
            mE(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_VALUES"

    // $ANTLR start "K_TIMESTAMP"
    public final void mK_TIMESTAMP() throws RecognitionException {
        try {
            int _type = K_TIMESTAMP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:465:12: ( T I M E S T A M P )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:465:16: T I M E S T A M P
            {
            mT(); 
            mI(); 
            mM(); 
            mE(); 
            mS(); 
            mT(); 
            mA(); 
            mM(); 
            mP(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TIMESTAMP"

    // $ANTLR start "K_TTL"
    public final void mK_TTL() throws RecognitionException {
        try {
            int _type = K_TTL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:466:6: ( T T L )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:466:16: T T L
            {
            mT(); 
            mT(); 
            mL(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TTL"

    // $ANTLR start "K_ALTER"
    public final void mK_ALTER() throws RecognitionException {
        try {
            int _type = K_ALTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:467:8: ( A L T E R )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:467:16: A L T E R
            {
            mA(); 
            mL(); 
            mT(); 
            mE(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ALTER"

    // $ANTLR start "K_RENAME"
    public final void mK_RENAME() throws RecognitionException {
        try {
            int _type = K_RENAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:468:9: ( R E N A M E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:468:16: R E N A M E
            {
            mR(); 
            mE(); 
            mN(); 
            mA(); 
            mM(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_RENAME"

    // $ANTLR start "K_ADD"
    public final void mK_ADD() throws RecognitionException {
        try {
            int _type = K_ADD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:469:6: ( A D D )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:469:16: A D D
            {
            mA(); 
            mD(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ADD"

    // $ANTLR start "K_TYPE"
    public final void mK_TYPE() throws RecognitionException {
        try {
            int _type = K_TYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:470:7: ( T Y P E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:470:16: T Y P E
            {
            mT(); 
            mY(); 
            mP(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TYPE"

    // $ANTLR start "K_COMPACT"
    public final void mK_COMPACT() throws RecognitionException {
        try {
            int _type = K_COMPACT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:471:10: ( C O M P A C T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:471:16: C O M P A C T
            {
            mC(); 
            mO(); 
            mM(); 
            mP(); 
            mA(); 
            mC(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_COMPACT"

    // $ANTLR start "K_STORAGE"
    public final void mK_STORAGE() throws RecognitionException {
        try {
            int _type = K_STORAGE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:472:10: ( S T O R A G E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:472:16: S T O R A G E
            {
            mS(); 
            mT(); 
            mO(); 
            mR(); 
            mA(); 
            mG(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_STORAGE"

    // $ANTLR start "K_ORDER"
    public final void mK_ORDER() throws RecognitionException {
        try {
            int _type = K_ORDER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:473:8: ( O R D E R )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:473:16: O R D E R
            {
            mO(); 
            mR(); 
            mD(); 
            mE(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ORDER"

    // $ANTLR start "K_BY"
    public final void mK_BY() throws RecognitionException {
        try {
            int _type = K_BY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:474:5: ( B Y )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:474:16: B Y
            {
            mB(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_BY"

    // $ANTLR start "K_ASC"
    public final void mK_ASC() throws RecognitionException {
        try {
            int _type = K_ASC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:475:6: ( A S C )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:475:16: A S C
            {
            mA(); 
            mS(); 
            mC(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ASC"

    // $ANTLR start "K_DESC"
    public final void mK_DESC() throws RecognitionException {
        try {
            int _type = K_DESC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:476:7: ( D E S C )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:476:16: D E S C
            {
            mD(); 
            mE(); 
            mS(); 
            mC(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_DESC"

    // $ANTLR start "K_ALLOW"
    public final void mK_ALLOW() throws RecognitionException {
        try {
            int _type = K_ALLOW;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:477:8: ( A L L O W )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:477:16: A L L O W
            {
            mA(); 
            mL(); 
            mL(); 
            mO(); 
            mW(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ALLOW"

    // $ANTLR start "K_FILTERING"
    public final void mK_FILTERING() throws RecognitionException {
        try {
            int _type = K_FILTERING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:478:12: ( F I L T E R I N G )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:478:16: F I L T E R I N G
            {
            mF(); 
            mI(); 
            mL(); 
            mT(); 
            mE(); 
            mR(); 
            mI(); 
            mN(); 
            mG(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_FILTERING"

    // $ANTLR start "K_GRANT"
    public final void mK_GRANT() throws RecognitionException {
        try {
            int _type = K_GRANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:480:8: ( G R A N T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:480:16: G R A N T
            {
            mG(); 
            mR(); 
            mA(); 
            mN(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_GRANT"

    // $ANTLR start "K_ALL"
    public final void mK_ALL() throws RecognitionException {
        try {
            int _type = K_ALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:481:6: ( A L L )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:481:16: A L L
            {
            mA(); 
            mL(); 
            mL(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ALL"

    // $ANTLR start "K_PERMISSION"
    public final void mK_PERMISSION() throws RecognitionException {
        try {
            int _type = K_PERMISSION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:482:13: ( P E R M I S S I O N )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:482:16: P E R M I S S I O N
            {
            mP(); 
            mE(); 
            mR(); 
            mM(); 
            mI(); 
            mS(); 
            mS(); 
            mI(); 
            mO(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_PERMISSION"

    // $ANTLR start "K_PERMISSIONS"
    public final void mK_PERMISSIONS() throws RecognitionException {
        try {
            int _type = K_PERMISSIONS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:483:14: ( P E R M I S S I O N S )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:483:16: P E R M I S S I O N S
            {
            mP(); 
            mE(); 
            mR(); 
            mM(); 
            mI(); 
            mS(); 
            mS(); 
            mI(); 
            mO(); 
            mN(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_PERMISSIONS"

    // $ANTLR start "K_OF"
    public final void mK_OF() throws RecognitionException {
        try {
            int _type = K_OF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:484:5: ( O F )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:484:16: O F
            {
            mO(); 
            mF(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_OF"

    // $ANTLR start "K_REVOKE"
    public final void mK_REVOKE() throws RecognitionException {
        try {
            int _type = K_REVOKE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:485:9: ( R E V O K E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:485:16: R E V O K E
            {
            mR(); 
            mE(); 
            mV(); 
            mO(); 
            mK(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_REVOKE"

    // $ANTLR start "K_MODIFY"
    public final void mK_MODIFY() throws RecognitionException {
        try {
            int _type = K_MODIFY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:486:9: ( M O D I F Y )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:486:16: M O D I F Y
            {
            mM(); 
            mO(); 
            mD(); 
            mI(); 
            mF(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_MODIFY"

    // $ANTLR start "K_AUTHORIZE"
    public final void mK_AUTHORIZE() throws RecognitionException {
        try {
            int _type = K_AUTHORIZE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:487:12: ( A U T H O R I Z E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:487:16: A U T H O R I Z E
            {
            mA(); 
            mU(); 
            mT(); 
            mH(); 
            mO(); 
            mR(); 
            mI(); 
            mZ(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_AUTHORIZE"

    // $ANTLR start "K_NORECURSIVE"
    public final void mK_NORECURSIVE() throws RecognitionException {
        try {
            int _type = K_NORECURSIVE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:488:14: ( N O R E C U R S I V E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:488:16: N O R E C U R S I V E
            {
            mN(); 
            mO(); 
            mR(); 
            mE(); 
            mC(); 
            mU(); 
            mR(); 
            mS(); 
            mI(); 
            mV(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_NORECURSIVE"

    // $ANTLR start "K_USER"
    public final void mK_USER() throws RecognitionException {
        try {
            int _type = K_USER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:490:7: ( U S E R )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:490:16: U S E R
            {
            mU(); 
            mS(); 
            mE(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_USER"

    // $ANTLR start "K_USERS"
    public final void mK_USERS() throws RecognitionException {
        try {
            int _type = K_USERS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:491:8: ( U S E R S )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:491:16: U S E R S
            {
            mU(); 
            mS(); 
            mE(); 
            mR(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_USERS"

    // $ANTLR start "K_SUPERUSER"
    public final void mK_SUPERUSER() throws RecognitionException {
        try {
            int _type = K_SUPERUSER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:492:12: ( S U P E R U S E R )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:492:16: S U P E R U S E R
            {
            mS(); 
            mU(); 
            mP(); 
            mE(); 
            mR(); 
            mU(); 
            mS(); 
            mE(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_SUPERUSER"

    // $ANTLR start "K_NOSUPERUSER"
    public final void mK_NOSUPERUSER() throws RecognitionException {
        try {
            int _type = K_NOSUPERUSER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:493:14: ( N O S U P E R U S E R )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:493:16: N O S U P E R U S E R
            {
            mN(); 
            mO(); 
            mS(); 
            mU(); 
            mP(); 
            mE(); 
            mR(); 
            mU(); 
            mS(); 
            mE(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_NOSUPERUSER"

    // $ANTLR start "K_PASSWORD"
    public final void mK_PASSWORD() throws RecognitionException {
        try {
            int _type = K_PASSWORD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:494:11: ( P A S S W O R D )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:494:16: P A S S W O R D
            {
            mP(); 
            mA(); 
            mS(); 
            mS(); 
            mW(); 
            mO(); 
            mR(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_PASSWORD"

    // $ANTLR start "K_CLUSTERING"
    public final void mK_CLUSTERING() throws RecognitionException {
        try {
            int _type = K_CLUSTERING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:496:13: ( C L U S T E R I N G )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:496:16: C L U S T E R I N G
            {
            mC(); 
            mL(); 
            mU(); 
            mS(); 
            mT(); 
            mE(); 
            mR(); 
            mI(); 
            mN(); 
            mG(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_CLUSTERING"

    // $ANTLR start "K_ASCII"
    public final void mK_ASCII() throws RecognitionException {
        try {
            int _type = K_ASCII;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:497:8: ( A S C I I )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:497:16: A S C I I
            {
            mA(); 
            mS(); 
            mC(); 
            mI(); 
            mI(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ASCII"

    // $ANTLR start "K_BIGINT"
    public final void mK_BIGINT() throws RecognitionException {
        try {
            int _type = K_BIGINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:498:9: ( B I G I N T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:498:16: B I G I N T
            {
            mB(); 
            mI(); 
            mG(); 
            mI(); 
            mN(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_BIGINT"

    // $ANTLR start "K_BLOB"
    public final void mK_BLOB() throws RecognitionException {
        try {
            int _type = K_BLOB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:499:7: ( B L O B )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:499:16: B L O B
            {
            mB(); 
            mL(); 
            mO(); 
            mB(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_BLOB"

    // $ANTLR start "K_BOOLEAN"
    public final void mK_BOOLEAN() throws RecognitionException {
        try {
            int _type = K_BOOLEAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:500:10: ( B O O L E A N )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:500:16: B O O L E A N
            {
            mB(); 
            mO(); 
            mO(); 
            mL(); 
            mE(); 
            mA(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_BOOLEAN"

    // $ANTLR start "K_COUNTER"
    public final void mK_COUNTER() throws RecognitionException {
        try {
            int _type = K_COUNTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:501:10: ( C O U N T E R )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:501:16: C O U N T E R
            {
            mC(); 
            mO(); 
            mU(); 
            mN(); 
            mT(); 
            mE(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_COUNTER"

    // $ANTLR start "K_DECIMAL"
    public final void mK_DECIMAL() throws RecognitionException {
        try {
            int _type = K_DECIMAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:502:10: ( D E C I M A L )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:502:16: D E C I M A L
            {
            mD(); 
            mE(); 
            mC(); 
            mI(); 
            mM(); 
            mA(); 
            mL(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_DECIMAL"

    // $ANTLR start "K_DOUBLE"
    public final void mK_DOUBLE() throws RecognitionException {
        try {
            int _type = K_DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:503:9: ( D O U B L E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:503:16: D O U B L E
            {
            mD(); 
            mO(); 
            mU(); 
            mB(); 
            mL(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_DOUBLE"

    // $ANTLR start "K_FLOAT"
    public final void mK_FLOAT() throws RecognitionException {
        try {
            int _type = K_FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:504:8: ( F L O A T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:504:16: F L O A T
            {
            mF(); 
            mL(); 
            mO(); 
            mA(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_FLOAT"

    // $ANTLR start "K_INET"
    public final void mK_INET() throws RecognitionException {
        try {
            int _type = K_INET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:505:7: ( I N E T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:505:16: I N E T
            {
            mI(); 
            mN(); 
            mE(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_INET"

    // $ANTLR start "K_INT"
    public final void mK_INT() throws RecognitionException {
        try {
            int _type = K_INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:506:6: ( I N T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:506:16: I N T
            {
            mI(); 
            mN(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_INT"

    // $ANTLR start "K_TEXT"
    public final void mK_TEXT() throws RecognitionException {
        try {
            int _type = K_TEXT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:507:7: ( T E X T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:507:16: T E X T
            {
            mT(); 
            mE(); 
            mX(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TEXT"

    // $ANTLR start "K_UUID"
    public final void mK_UUID() throws RecognitionException {
        try {
            int _type = K_UUID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:508:7: ( U U I D )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:508:16: U U I D
            {
            mU(); 
            mU(); 
            mI(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_UUID"

    // $ANTLR start "K_VARCHAR"
    public final void mK_VARCHAR() throws RecognitionException {
        try {
            int _type = K_VARCHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:509:10: ( V A R C H A R )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:509:16: V A R C H A R
            {
            mV(); 
            mA(); 
            mR(); 
            mC(); 
            mH(); 
            mA(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_VARCHAR"

    // $ANTLR start "K_VARINT"
    public final void mK_VARINT() throws RecognitionException {
        try {
            int _type = K_VARINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:510:9: ( V A R I N T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:510:16: V A R I N T
            {
            mV(); 
            mA(); 
            mR(); 
            mI(); 
            mN(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_VARINT"

    // $ANTLR start "K_TIMEUUID"
    public final void mK_TIMEUUID() throws RecognitionException {
        try {
            int _type = K_TIMEUUID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:511:11: ( T I M E U U I D )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:511:16: T I M E U U I D
            {
            mT(); 
            mI(); 
            mM(); 
            mE(); 
            mU(); 
            mU(); 
            mI(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TIMEUUID"

    // $ANTLR start "K_TOKEN"
    public final void mK_TOKEN() throws RecognitionException {
        try {
            int _type = K_TOKEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:512:8: ( T O K E N )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:512:16: T O K E N
            {
            mT(); 
            mO(); 
            mK(); 
            mE(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TOKEN"

    // $ANTLR start "K_WRITETIME"
    public final void mK_WRITETIME() throws RecognitionException {
        try {
            int _type = K_WRITETIME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:513:12: ( W R I T E T I M E )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:513:16: W R I T E T I M E
            {
            mW(); 
            mR(); 
            mI(); 
            mT(); 
            mE(); 
            mT(); 
            mI(); 
            mM(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_WRITETIME"

    // $ANTLR start "K_NULL"
    public final void mK_NULL() throws RecognitionException {
        try {
            int _type = K_NULL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:515:7: ( N U L L )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:515:16: N U L L
            {
            mN(); 
            mU(); 
            mL(); 
            mL(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_NULL"

    // $ANTLR start "K_MAP"
    public final void mK_MAP() throws RecognitionException {
        try {
            int _type = K_MAP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:517:6: ( M A P )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:517:16: M A P
            {
            mM(); 
            mA(); 
            mP(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_MAP"

    // $ANTLR start "K_LIST"
    public final void mK_LIST() throws RecognitionException {
        try {
            int _type = K_LIST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:518:7: ( L I S T )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:518:16: L I S T
            {
            mL(); 
            mI(); 
            mS(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_LIST"

    // $ANTLR start "A"
    public final void mA() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:521:11: ( ( 'a' | 'A' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:521:13: ( 'a' | 'A' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "A"

    // $ANTLR start "B"
    public final void mB() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:522:11: ( ( 'b' | 'B' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:522:13: ( 'b' | 'B' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "B"

    // $ANTLR start "C"
    public final void mC() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:523:11: ( ( 'c' | 'C' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:523:13: ( 'c' | 'C' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "C"

    // $ANTLR start "D"
    public final void mD() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:524:11: ( ( 'd' | 'D' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:524:13: ( 'd' | 'D' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "D"

    // $ANTLR start "E"
    public final void mE() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:525:11: ( ( 'e' | 'E' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:525:13: ( 'e' | 'E' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "E"

    // $ANTLR start "F"
    public final void mF() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:526:11: ( ( 'f' | 'F' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:526:13: ( 'f' | 'F' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "F"

    // $ANTLR start "G"
    public final void mG() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:527:11: ( ( 'g' | 'G' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:527:13: ( 'g' | 'G' )
            {
            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "G"

    // $ANTLR start "H"
    public final void mH() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:528:11: ( ( 'h' | 'H' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:528:13: ( 'h' | 'H' )
            {
            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "H"

    // $ANTLR start "I"
    public final void mI() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:529:11: ( ( 'i' | 'I' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:529:13: ( 'i' | 'I' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "I"

    // $ANTLR start "J"
    public final void mJ() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:530:11: ( ( 'j' | 'J' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:530:13: ( 'j' | 'J' )
            {
            if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "J"

    // $ANTLR start "K"
    public final void mK() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:531:11: ( ( 'k' | 'K' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:531:13: ( 'k' | 'K' )
            {
            if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "K"

    // $ANTLR start "L"
    public final void mL() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:532:11: ( ( 'l' | 'L' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:532:13: ( 'l' | 'L' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "L"

    // $ANTLR start "M"
    public final void mM() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:533:11: ( ( 'm' | 'M' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:533:13: ( 'm' | 'M' )
            {
            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "M"

    // $ANTLR start "N"
    public final void mN() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:534:11: ( ( 'n' | 'N' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:534:13: ( 'n' | 'N' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "N"

    // $ANTLR start "O"
    public final void mO() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:535:11: ( ( 'o' | 'O' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:535:13: ( 'o' | 'O' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "O"

    // $ANTLR start "P"
    public final void mP() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:536:11: ( ( 'p' | 'P' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:536:13: ( 'p' | 'P' )
            {
            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "P"

    // $ANTLR start "Q"
    public final void mQ() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:537:11: ( ( 'q' | 'Q' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:537:13: ( 'q' | 'Q' )
            {
            if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Q"

    // $ANTLR start "R"
    public final void mR() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:538:11: ( ( 'r' | 'R' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:538:13: ( 'r' | 'R' )
            {
            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "R"

    // $ANTLR start "S"
    public final void mS() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:539:11: ( ( 's' | 'S' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:539:13: ( 's' | 'S' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "S"

    // $ANTLR start "T"
    public final void mT() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:540:11: ( ( 't' | 'T' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:540:13: ( 't' | 'T' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "T"

    // $ANTLR start "U"
    public final void mU() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:541:11: ( ( 'u' | 'U' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:541:13: ( 'u' | 'U' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "U"

    // $ANTLR start "V"
    public final void mV() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:542:11: ( ( 'v' | 'V' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:542:13: ( 'v' | 'V' )
            {
            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "V"

    // $ANTLR start "W"
    public final void mW() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:543:11: ( ( 'w' | 'W' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:543:13: ( 'w' | 'W' )
            {
            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "W"

    // $ANTLR start "X"
    public final void mX() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:544:11: ( ( 'x' | 'X' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:544:13: ( 'x' | 'X' )
            {
            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "X"

    // $ANTLR start "Y"
    public final void mY() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:545:11: ( ( 'y' | 'Y' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:545:13: ( 'y' | 'Y' )
            {
            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Y"

    // $ANTLR start "Z"
    public final void mZ() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:546:11: ( ( 'z' | 'Z' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:546:13: ( 'z' | 'Z' )
            {
            if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Z"

    // $ANTLR start "STRING_LITERAL"
    public final void mSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = STRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            int c;

             StringBuilder b = new StringBuilder(); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:551:5: ( '\\'' (c=~ ( '\\'' ) | '\\'' '\\'' )* '\\'' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:551:7: '\\'' (c=~ ( '\\'' ) | '\\'' '\\'' )* '\\''
            {
            match('\''); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:551:12: (c=~ ( '\\'' ) | '\\'' '\\'' )*
            loop3:
            do {
                int alt3=3;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\'') ) {
                    int LA3_1 = input.LA(2);

                    if ( (LA3_1=='\'') ) {
                        alt3=2;
                    }


                }
                else if ( ((LA3_0>='\u0000' && LA3_0<='&')||(LA3_0>='(' && LA3_0<='\uFFFF')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:551:13: c=~ ( '\\'' )
            	    {
            	    c= input.LA(1);
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}

            	     b.appendCodePoint(c);

            	    }
            	    break;
            	case 2 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:551:50: '\\'' '\\''
            	    {
            	    match('\''); 
            	    match('\''); 
            	     b.appendCodePoint('\''); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
             setText(b.toString());     }
        finally {
        }
    }
    // $ANTLR end "STRING_LITERAL"

    // $ANTLR start "QUOTED_NAME"
    public final void mQUOTED_NAME() throws RecognitionException {
        try {
            int _type = QUOTED_NAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            int c;

             StringBuilder b = new StringBuilder(); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:557:5: ( '\\\"' (c=~ ( '\\\"' ) | '\\\"' '\\\"' )* '\\\"' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:557:7: '\\\"' (c=~ ( '\\\"' ) | '\\\"' '\\\"' )* '\\\"'
            {
            match('\"'); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:557:12: (c=~ ( '\\\"' ) | '\\\"' '\\\"' )*
            loop4:
            do {
                int alt4=3;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='\"') ) {
                    int LA4_1 = input.LA(2);

                    if ( (LA4_1=='\"') ) {
                        alt4=2;
                    }


                }
                else if ( ((LA4_0>='\u0000' && LA4_0<='!')||(LA4_0>='#' && LA4_0<='\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:557:13: c=~ ( '\\\"' )
            	    {
            	    c= input.LA(1);
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}

            	     b.appendCodePoint(c); 

            	    }
            	    break;
            	case 2 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:557:51: '\\\"' '\\\"'
            	    {
            	    match('\"'); 
            	    match('\"'); 
            	     b.appendCodePoint('\"'); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
             setText(b.toString());     }
        finally {
        }
    }
    // $ANTLR end "QUOTED_NAME"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:561:5: ( '0' .. '9' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:561:7: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:565:5: ( ( 'A' .. 'Z' | 'a' .. 'z' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:565:7: ( 'A' .. 'Z' | 'a' .. 'z' )
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "LETTER"

    // $ANTLR start "HEX"
    public final void mHEX() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:569:5: ( ( 'A' .. 'F' | 'a' .. 'f' | '0' .. '9' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:569:7: ( 'A' .. 'F' | 'a' .. 'f' | '0' .. '9' )
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "HEX"

    // $ANTLR start "EXPONENT"
    public final void mEXPONENT() throws RecognitionException {
        try {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:573:5: ( E ( '+' | '-' )? ( DIGIT )+ )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:573:7: E ( '+' | '-' )? ( DIGIT )+
            {
            mE(); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:573:9: ( '+' | '-' )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='+'||LA5_0=='-') ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:573:22: ( DIGIT )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:573:22: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "EXPONENT"

    // $ANTLR start "INTEGER"
    public final void mINTEGER() throws RecognitionException {
        try {
            int _type = INTEGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:577:5: ( ( '-' )? ( DIGIT )+ )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:577:7: ( '-' )? ( DIGIT )+
            {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:577:7: ( '-' )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='-') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:577:7: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:577:12: ( DIGIT )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='0' && LA8_0<='9')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:577:12: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTEGER"

    // $ANTLR start "QMARK"
    public final void mQMARK() throws RecognitionException {
        try {
            int _type = QMARK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:581:5: ( '?' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:581:7: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QMARK"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:589:5: ( INTEGER EXPONENT | INTEGER '.' ( DIGIT )* ( EXPONENT )? )
            int alt11=2;
            alt11 = dfa11.predict(input);
            switch (alt11) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:589:7: INTEGER EXPONENT
                    {
                    mINTEGER(); 
                    mEXPONENT(); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:590:7: INTEGER '.' ( DIGIT )* ( EXPONENT )?
                    {
                    mINTEGER(); 
                    match('.'); 
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:590:19: ( DIGIT )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( ((LA9_0>='0' && LA9_0<='9')) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:590:19: DIGIT
                    	    {
                    	    mDIGIT(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);

                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:590:26: ( EXPONENT )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0=='E'||LA10_0=='e') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:590:26: EXPONENT
                            {
                            mEXPONENT(); 

                            }
                            break;

                    }


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "BOOLEAN"
    public final void mBOOLEAN() throws RecognitionException {
        try {
            int _type = BOOLEAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:597:5: ( T R U E | F A L S E )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0=='T'||LA12_0=='t') ) {
                alt12=1;
            }
            else if ( (LA12_0=='F'||LA12_0=='f') ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:597:7: T R U E
                    {
                    mT(); 
                    mR(); 
                    mU(); 
                    mE(); 

                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:597:17: F A L S E
                    {
                    mF(); 
                    mA(); 
                    mL(); 
                    mS(); 
                    mE(); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOOLEAN"

    // $ANTLR start "IDENT"
    public final void mIDENT() throws RecognitionException {
        try {
            int _type = IDENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:601:5: ( LETTER ( LETTER | DIGIT | '_' )* )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:601:7: LETTER ( LETTER | DIGIT | '_' )*
            {
            mLETTER(); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:601:14: ( LETTER | DIGIT | '_' )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( ((LA13_0>='0' && LA13_0<='9')||(LA13_0>='A' && LA13_0<='Z')||LA13_0=='_'||(LA13_0>='a' && LA13_0<='z')) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IDENT"

    // $ANTLR start "HEXNUMBER"
    public final void mHEXNUMBER() throws RecognitionException {
        try {
            int _type = HEXNUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:605:5: ( '0' X ( HEX )* )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:605:7: '0' X ( HEX )*
            {
            match('0'); 
            mX(); 
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:605:13: ( HEX )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( ((LA14_0>='0' && LA14_0<='9')||(LA14_0>='A' && LA14_0<='F')||(LA14_0>='a' && LA14_0<='f')) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:605:13: HEX
            	    {
            	    mHEX(); 

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HEXNUMBER"

    // $ANTLR start "UUID"
    public final void mUUID() throws RecognitionException {
        try {
            int _type = UUID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:609:5: ( HEX HEX HEX HEX HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:609:7: HEX HEX HEX HEX HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX
            {
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            match('-'); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            match('-'); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            match('-'); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            match('-'); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UUID"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:617:5: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:617:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:617:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt15=0;
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( ((LA15_0>='\t' && LA15_0<='\n')||LA15_0=='\r'||LA15_0==' ') ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt15 >= 1 ) break loop15;
                        EarlyExitException eee =
                            new EarlyExitException(15, input);
                        throw eee;
                }
                cnt15++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:621:5: ( ( '--' | '//' ) ( . )* ( '\\n' | '\\r' ) )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:621:7: ( '--' | '//' ) ( . )* ( '\\n' | '\\r' )
            {
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:621:7: ( '--' | '//' )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='-') ) {
                alt16=1;
            }
            else if ( (LA16_0=='/') ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:621:8: '--'
                    {
                    match("--"); 


                    }
                    break;
                case 2 :
                    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:621:15: '//'
                    {
                    match("//"); 


                    }
                    break;

            }

            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:621:21: ( . )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0=='\n'||LA17_0=='\r') ) {
                    alt17=2;
                }
                else if ( ((LA17_0>='\u0000' && LA17_0<='\t')||(LA17_0>='\u000B' && LA17_0<='\f')||(LA17_0>='\u000E' && LA17_0<='\uFFFF')) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:621:21: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);

            if ( input.LA(1)=='\n'||input.LA(1)=='\r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "MULTILINE_COMMENT"
    public final void mMULTILINE_COMMENT() throws RecognitionException {
        try {
            int _type = MULTILINE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:625:5: ( '/*' ( . )* '*/' )
            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:625:7: '/*' ( . )* '*/'
            {
            match("/*"); 

            // src/main/java/com/tuplejump/stargate/xcql/XCql.g:625:12: ( . )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0=='*') ) {
                    int LA18_1 = input.LA(2);

                    if ( (LA18_1=='/') ) {
                        alt18=2;
                    }
                    else if ( ((LA18_1>='\u0000' && LA18_1<='.')||(LA18_1>='0' && LA18_1<='\uFFFF')) ) {
                        alt18=1;
                    }


                }
                else if ( ((LA18_0>='\u0000' && LA18_0<=')')||(LA18_0>='+' && LA18_0<='\uFFFF')) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // src/main/java/com/tuplejump/stargate/xcql/XCql.g:625:12: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);

            match("*/"); 

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MULTILINE_COMMENT"

    public void mTokens() throws RecognitionException {
        // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:8: ( T__139 | T__140 | T__141 | T__142 | T__143 | T__144 | T__145 | T__146 | T__147 | T__148 | T__149 | T__150 | T__151 | T__152 | T__153 | T__154 | T__155 | X_SELECT | X_CREATE | X_GROUP | X_HAVING | X_LIKE | X_SKIP | X_ROW | X_ROW_META | X_COL | X_OR | X_UNLIM | X_OPTIONS | K_SELECT | K_FROM | K_WHERE | K_AND | K_KEY | K_INSERT | K_UPDATE | K_WITH | K_LIMIT | K_USING | K_USE | K_COUNT | K_SET | K_BEGIN | K_UNLOGGED | K_BATCH | K_APPLY | K_TRUNCATE | K_DELETE | K_IN | K_CREATE | K_KEYSPACE | K_KEYSPACES | K_COLUMNFAMILY | K_INDEX | K_CUSTOM | K_ON | K_TO | K_DROP | K_PRIMARY | K_INTO | K_VALUES | K_TIMESTAMP | K_TTL | K_ALTER | K_RENAME | K_ADD | K_TYPE | K_COMPACT | K_STORAGE | K_ORDER | K_BY | K_ASC | K_DESC | K_ALLOW | K_FILTERING | K_GRANT | K_ALL | K_PERMISSION | K_PERMISSIONS | K_OF | K_REVOKE | K_MODIFY | K_AUTHORIZE | K_NORECURSIVE | K_USER | K_USERS | K_SUPERUSER | K_NOSUPERUSER | K_PASSWORD | K_CLUSTERING | K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_TEXT | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID | K_TOKEN | K_WRITETIME | K_NULL | K_MAP | K_LIST | STRING_LITERAL | QUOTED_NAME | INTEGER | QMARK | FLOAT | BOOLEAN | IDENT | HEXNUMBER | UUID | WS | COMMENT | MULTILINE_COMMENT )
        int alt19=122;
        alt19 = dfa19.predict(input);
        switch (alt19) {
            case 1 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:10: T__139
                {
                mT__139(); 

                }
                break;
            case 2 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:17: T__140
                {
                mT__140(); 

                }
                break;
            case 3 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:24: T__141
                {
                mT__141(); 

                }
                break;
            case 4 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:31: T__142
                {
                mT__142(); 

                }
                break;
            case 5 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:38: T__143
                {
                mT__143(); 

                }
                break;
            case 6 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:45: T__144
                {
                mT__144(); 

                }
                break;
            case 7 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:52: T__145
                {
                mT__145(); 

                }
                break;
            case 8 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:59: T__146
                {
                mT__146(); 

                }
                break;
            case 9 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:66: T__147
                {
                mT__147(); 

                }
                break;
            case 10 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:73: T__148
                {
                mT__148(); 

                }
                break;
            case 11 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:80: T__149
                {
                mT__149(); 

                }
                break;
            case 12 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:87: T__150
                {
                mT__150(); 

                }
                break;
            case 13 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:94: T__151
                {
                mT__151(); 

                }
                break;
            case 14 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:101: T__152
                {
                mT__152(); 

                }
                break;
            case 15 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:108: T__153
                {
                mT__153(); 

                }
                break;
            case 16 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:115: T__154
                {
                mT__154(); 

                }
                break;
            case 17 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:122: T__155
                {
                mT__155(); 

                }
                break;
            case 18 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:129: X_SELECT
                {
                mX_SELECT(); 

                }
                break;
            case 19 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:138: X_CREATE
                {
                mX_CREATE(); 

                }
                break;
            case 20 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:147: X_GROUP
                {
                mX_GROUP(); 

                }
                break;
            case 21 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:155: X_HAVING
                {
                mX_HAVING(); 

                }
                break;
            case 22 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:164: X_LIKE
                {
                mX_LIKE(); 

                }
                break;
            case 23 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:171: X_SKIP
                {
                mX_SKIP(); 

                }
                break;
            case 24 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:178: X_ROW
                {
                mX_ROW(); 

                }
                break;
            case 25 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:184: X_ROW_META
                {
                mX_ROW_META(); 

                }
                break;
            case 26 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:195: X_COL
                {
                mX_COL(); 

                }
                break;
            case 27 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:201: X_OR
                {
                mX_OR(); 

                }
                break;
            case 28 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:206: X_UNLIM
                {
                mX_UNLIM(); 

                }
                break;
            case 29 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:214: X_OPTIONS
                {
                mX_OPTIONS(); 

                }
                break;
            case 30 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:224: K_SELECT
                {
                mK_SELECT(); 

                }
                break;
            case 31 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:233: K_FROM
                {
                mK_FROM(); 

                }
                break;
            case 32 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:240: K_WHERE
                {
                mK_WHERE(); 

                }
                break;
            case 33 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:248: K_AND
                {
                mK_AND(); 

                }
                break;
            case 34 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:254: K_KEY
                {
                mK_KEY(); 

                }
                break;
            case 35 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:260: K_INSERT
                {
                mK_INSERT(); 

                }
                break;
            case 36 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:269: K_UPDATE
                {
                mK_UPDATE(); 

                }
                break;
            case 37 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:278: K_WITH
                {
                mK_WITH(); 

                }
                break;
            case 38 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:285: K_LIMIT
                {
                mK_LIMIT(); 

                }
                break;
            case 39 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:293: K_USING
                {
                mK_USING(); 

                }
                break;
            case 40 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:301: K_USE
                {
                mK_USE(); 

                }
                break;
            case 41 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:307: K_COUNT
                {
                mK_COUNT(); 

                }
                break;
            case 42 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:315: K_SET
                {
                mK_SET(); 

                }
                break;
            case 43 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:321: K_BEGIN
                {
                mK_BEGIN(); 

                }
                break;
            case 44 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:329: K_UNLOGGED
                {
                mK_UNLOGGED(); 

                }
                break;
            case 45 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:340: K_BATCH
                {
                mK_BATCH(); 

                }
                break;
            case 46 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:348: K_APPLY
                {
                mK_APPLY(); 

                }
                break;
            case 47 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:356: K_TRUNCATE
                {
                mK_TRUNCATE(); 

                }
                break;
            case 48 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:367: K_DELETE
                {
                mK_DELETE(); 

                }
                break;
            case 49 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:376: K_IN
                {
                mK_IN(); 

                }
                break;
            case 50 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:381: K_CREATE
                {
                mK_CREATE(); 

                }
                break;
            case 51 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:390: K_KEYSPACE
                {
                mK_KEYSPACE(); 

                }
                break;
            case 52 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:401: K_KEYSPACES
                {
                mK_KEYSPACES(); 

                }
                break;
            case 53 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:413: K_COLUMNFAMILY
                {
                mK_COLUMNFAMILY(); 

                }
                break;
            case 54 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:428: K_INDEX
                {
                mK_INDEX(); 

                }
                break;
            case 55 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:436: K_CUSTOM
                {
                mK_CUSTOM(); 

                }
                break;
            case 56 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:445: K_ON
                {
                mK_ON(); 

                }
                break;
            case 57 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:450: K_TO
                {
                mK_TO(); 

                }
                break;
            case 58 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:455: K_DROP
                {
                mK_DROP(); 

                }
                break;
            case 59 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:462: K_PRIMARY
                {
                mK_PRIMARY(); 

                }
                break;
            case 60 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:472: K_INTO
                {
                mK_INTO(); 

                }
                break;
            case 61 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:479: K_VALUES
                {
                mK_VALUES(); 

                }
                break;
            case 62 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:488: K_TIMESTAMP
                {
                mK_TIMESTAMP(); 

                }
                break;
            case 63 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:500: K_TTL
                {
                mK_TTL(); 

                }
                break;
            case 64 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:506: K_ALTER
                {
                mK_ALTER(); 

                }
                break;
            case 65 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:514: K_RENAME
                {
                mK_RENAME(); 

                }
                break;
            case 66 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:523: K_ADD
                {
                mK_ADD(); 

                }
                break;
            case 67 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:529: K_TYPE
                {
                mK_TYPE(); 

                }
                break;
            case 68 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:536: K_COMPACT
                {
                mK_COMPACT(); 

                }
                break;
            case 69 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:546: K_STORAGE
                {
                mK_STORAGE(); 

                }
                break;
            case 70 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:556: K_ORDER
                {
                mK_ORDER(); 

                }
                break;
            case 71 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:564: K_BY
                {
                mK_BY(); 

                }
                break;
            case 72 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:569: K_ASC
                {
                mK_ASC(); 

                }
                break;
            case 73 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:575: K_DESC
                {
                mK_DESC(); 

                }
                break;
            case 74 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:582: K_ALLOW
                {
                mK_ALLOW(); 

                }
                break;
            case 75 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:590: K_FILTERING
                {
                mK_FILTERING(); 

                }
                break;
            case 76 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:602: K_GRANT
                {
                mK_GRANT(); 

                }
                break;
            case 77 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:610: K_ALL
                {
                mK_ALL(); 

                }
                break;
            case 78 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:616: K_PERMISSION
                {
                mK_PERMISSION(); 

                }
                break;
            case 79 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:629: K_PERMISSIONS
                {
                mK_PERMISSIONS(); 

                }
                break;
            case 80 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:643: K_OF
                {
                mK_OF(); 

                }
                break;
            case 81 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:648: K_REVOKE
                {
                mK_REVOKE(); 

                }
                break;
            case 82 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:657: K_MODIFY
                {
                mK_MODIFY(); 

                }
                break;
            case 83 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:666: K_AUTHORIZE
                {
                mK_AUTHORIZE(); 

                }
                break;
            case 84 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:678: K_NORECURSIVE
                {
                mK_NORECURSIVE(); 

                }
                break;
            case 85 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:692: K_USER
                {
                mK_USER(); 

                }
                break;
            case 86 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:699: K_USERS
                {
                mK_USERS(); 

                }
                break;
            case 87 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:707: K_SUPERUSER
                {
                mK_SUPERUSER(); 

                }
                break;
            case 88 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:719: K_NOSUPERUSER
                {
                mK_NOSUPERUSER(); 

                }
                break;
            case 89 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:733: K_PASSWORD
                {
                mK_PASSWORD(); 

                }
                break;
            case 90 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:744: K_CLUSTERING
                {
                mK_CLUSTERING(); 

                }
                break;
            case 91 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:757: K_ASCII
                {
                mK_ASCII(); 

                }
                break;
            case 92 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:765: K_BIGINT
                {
                mK_BIGINT(); 

                }
                break;
            case 93 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:774: K_BLOB
                {
                mK_BLOB(); 

                }
                break;
            case 94 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:781: K_BOOLEAN
                {
                mK_BOOLEAN(); 

                }
                break;
            case 95 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:791: K_COUNTER
                {
                mK_COUNTER(); 

                }
                break;
            case 96 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:801: K_DECIMAL
                {
                mK_DECIMAL(); 

                }
                break;
            case 97 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:811: K_DOUBLE
                {
                mK_DOUBLE(); 

                }
                break;
            case 98 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:820: K_FLOAT
                {
                mK_FLOAT(); 

                }
                break;
            case 99 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:828: K_INET
                {
                mK_INET(); 

                }
                break;
            case 100 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:835: K_INT
                {
                mK_INT(); 

                }
                break;
            case 101 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:841: K_TEXT
                {
                mK_TEXT(); 

                }
                break;
            case 102 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:848: K_UUID
                {
                mK_UUID(); 

                }
                break;
            case 103 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:855: K_VARCHAR
                {
                mK_VARCHAR(); 

                }
                break;
            case 104 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:865: K_VARINT
                {
                mK_VARINT(); 

                }
                break;
            case 105 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:874: K_TIMEUUID
                {
                mK_TIMEUUID(); 

                }
                break;
            case 106 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:885: K_TOKEN
                {
                mK_TOKEN(); 

                }
                break;
            case 107 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:893: K_WRITETIME
                {
                mK_WRITETIME(); 

                }
                break;
            case 108 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:905: K_NULL
                {
                mK_NULL(); 

                }
                break;
            case 109 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:912: K_MAP
                {
                mK_MAP(); 

                }
                break;
            case 110 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:918: K_LIST
                {
                mK_LIST(); 

                }
                break;
            case 111 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:925: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 112 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:940: QUOTED_NAME
                {
                mQUOTED_NAME(); 

                }
                break;
            case 113 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:952: INTEGER
                {
                mINTEGER(); 

                }
                break;
            case 114 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:960: QMARK
                {
                mQMARK(); 

                }
                break;
            case 115 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:966: FLOAT
                {
                mFLOAT(); 

                }
                break;
            case 116 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:972: BOOLEAN
                {
                mBOOLEAN(); 

                }
                break;
            case 117 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:980: IDENT
                {
                mIDENT(); 

                }
                break;
            case 118 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:986: HEXNUMBER
                {
                mHEXNUMBER(); 

                }
                break;
            case 119 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:996: UUID
                {
                mUUID(); 

                }
                break;
            case 120 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:1001: WS
                {
                mWS(); 

                }
                break;
            case 121 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:1004: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 122 :
                // src/main/java/com/tuplejump/stargate/xcql/XCql.g:1:1012: MULTILINE_COMMENT
                {
                mMULTILINE_COMMENT(); 

                }
                break;

        }

    }


    protected DFA11 dfa11 = new DFA11(this);
    protected DFA19 dfa19 = new DFA19(this);
    static final String DFA11_eotS =
        "\5\uffff";
    static final String DFA11_eofS =
        "\5\uffff";
    static final String DFA11_minS =
        "\1\55\1\60\1\56\2\uffff";
    static final String DFA11_maxS =
        "\2\71\1\145\2\uffff";
    static final String DFA11_acceptS =
        "\3\uffff\1\2\1\1";
    static final String DFA11_specialS =
        "\5\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\1\2\uffff\12\2",
            "\12\2",
            "\1\3\1\uffff\12\2\13\uffff\1\4\37\uffff\1\4",
            "",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "588:1: FLOAT : ( INTEGER EXPONENT | INTEGER '.' ( DIGIT )* ( EXPONENT )? );";
        }
    }
    static final String DFA19_eotS =
        "\15\uffff\1\60\1\62\1\64\25\53\2\uffff\1\167\1\uffff\1\53\1\167"+
        "\4\uffff\1\167\5\uffff\23\53\1\u0097\1\u0098\1\u009a\22\53\1\u00af"+
        "\1\u00b4\11\53\1\u00be\15\53\3\uffff\1\167\3\uffff\13\53\1\u00df"+
        "\5\53\1\u00e5\1\u00e6\7\53\2\uffff\1\53\1\uffff\1\u00f0\13\53\1"+
        "\u00fe\1\u00ff\2\53\1\u0103\1\53\1\u0106\1\u0107\1\uffff\1\u0109"+
        "\3\53\1\uffff\10\53\1\u0117\1\uffff\15\53\1\u0126\4\53\1\172\1\uffff"+
        "\1\167\6\53\1\u0134\1\53\1\u0136\2\53\1\uffff\2\53\1\u013b\2\53"+
        "\2\uffff\11\53\1\uffff\1\u0147\4\53\1\u014d\3\53\1\u0151\1\u0152"+
        "\2\53\2\uffff\3\53\1\uffff\2\53\2\uffff\1\53\1\uffff\1\u015b\1\53"+
        "\1\u015d\4\53\1\u0162\1\53\1\u0164\1\u0165\1\53\1\u0167\1\uffff"+
        "\3\53\1\u016c\2\53\1\u016f\7\53\1\uffff\1\53\1\u0178\2\53\1\172"+
        "\1\uffff\1\167\3\53\1\u0181\1\u0182\1\53\1\uffff\1\u0184\1\uffff"+
        "\4\53\1\uffff\4\53\1\u018d\5\53\1\u0194\1\uffff\1\u0195\1\u0196"+
        "\3\53\1\uffff\1\u0165\1\u019a\1\53\2\uffff\1\u019c\1\53\1\u019e"+
        "\1\u019f\1\u01a0\1\u01a1\2\53\1\uffff\1\53\1\uffff\1\u01a5\1\53"+
        "\1\u01a7\1\u01a8\1\uffff\1\53\2\uffff\1\53\1\uffff\1\u01ab\1\u01ac"+
        "\2\53\1\uffff\2\53\1\uffff\10\53\1\uffff\2\53\1\172\1\uffff\1\167"+
        "\3\53\2\uffff\1\u01c1\1\uffff\2\53\1\u01c4\1\u01c5\1\u01c6\1\u01c7"+
        "\2\53\1\uffff\1\53\1\u01cb\2\53\1\u01ce\1\53\3\uffff\2\53\1\u01d2"+
        "\1\uffff\1\53\1\uffff\1\53\4\uffff\2\53\1\u01d7\1\uffff\1\53\2\uffff"+
        "\1\u01d9\1\53\2\uffff\3\53\1\u01de\1\u01df\3\53\1\u01e3\1\53\1\u01e5"+
        "\1\u01e6\2\53\1\172\1\uffff\1\167\1\53\1\u01ed\1\u01ee\1\uffff\1"+
        "\53\1\u01f0\4\uffff\1\53\1\u01f2\1\u01f3\1\uffff\2\53\1\uffff\1"+
        "\u01f6\2\53\1\uffff\4\53\1\uffff\1\u01fd\1\uffff\3\53\1\u0201\2"+
        "\uffff\2\53\1\u0204\1\uffff\1\u0205\2\uffff\2\53\1\172\1\uffff\1"+
        "\167\1\u020b\2\uffff\1\53\1\uffff\1\53\2\uffff\2\53\1\uffff\1\53"+
        "\1\u0210\3\53\1\u01c5\1\uffff\1\u0215\1\u0216\1\53\1\uffff\1\53"+
        "\1\u0219\2\uffff\2\53\1\172\1\uffff\1\167\1\uffff\1\u021d\2\53\1"+
        "\u0220\1\uffff\1\u0221\1\u0222\1\u0223\1\u0224\2\uffff\1\u0225\1"+
        "\53\1\uffff\2\53\2\uffff\1\53\1\u022b\6\uffff\1\u022c\2\53\1\172"+
        "\1\53\2\uffff\1\u0232\1\u0233\1\u0234\1\172\1\u01ac\3\uffff\2\172";
    static final String DFA19_eofS =
        "\u0237\uffff";
    static final String DFA19_minS =
        "\1\11\14\uffff\1\55\2\75\1\103\1\122\1\101\1\111\1\103\1\105\1\60"+
        "\1\106\1\116\1\60\1\110\1\60\1\105\1\116\1\60\1\101\1\60\3\101\1"+
        "\117\2\uffff\1\56\1\uffff\1\60\1\56\2\uffff\1\52\1\uffff\1\56\5"+
        "\uffff\1\117\1\122\1\105\1\101\1\126\1\113\1\120\1\117\1\114\1\110"+
        "\1\111\1\116\1\127\1\114\1\123\1\125\1\60\1\105\1\124\3\60\1\105"+
        "\1\114\1\104\1\111\1\60\1\117\1\114\1\117\1\124\1\105\1\111\1\60"+
        "\1\114\1\120\1\103\1\124\1\104\1\131\2\60\1\117\2\60\1\117\1\107"+
        "\1\120\1\125\1\130\1\114\1\60\1\102\1\115\1\60\1\117\1\125\1\122"+
        "\1\123\1\111\1\114\1\120\1\104\1\114\1\122\2\uffff\1\53\1\56\3\uffff"+
        "\1\127\1\105\1\114\1\116\1\125\1\111\1\124\1\111\2\105\1\122\1\60"+
        "\2\105\1\120\1\117\1\101\2\60\1\120\1\116\1\124\1\123\1\60\1\101"+
        "\1\111\2\uffff\1\105\1\uffff\1\60\1\116\1\111\1\101\1\104\1\123"+
        "\1\101\1\124\1\115\1\110\1\122\1\124\2\60\1\105\1\114\1\60\1\110"+
        "\2\60\1\uffff\1\60\1\105\1\124\1\105\1\uffff\1\114\1\103\1\111\1"+
        "\102\1\111\2\105\1\124\1\60\1\uffff\1\105\1\114\1\105\1\103\1\60"+
        "\1\105\1\120\1\102\1\115\1\123\1\115\1\103\1\125\1\60\1\111\1\114"+
        "\1\125\1\105\1\60\1\53\1\56\1\115\1\101\1\105\1\124\1\120\1\116"+
        "\1\60\1\124\1\60\1\122\1\101\1\uffff\1\103\1\115\1\60\1\113\1\115"+
        "\2\uffff\1\115\1\101\1\124\1\117\1\124\1\60\1\124\1\117\1\122\1"+
        "\uffff\1\60\1\107\1\115\1\107\1\124\1\60\1\105\1\124\1\105\2\60"+
        "\2\105\2\uffff\1\127\1\122\1\131\1\uffff\1\111\1\117\2\uffff\1\120"+
        "\1\uffff\1\60\1\122\1\60\1\130\1\105\1\110\1\116\1\60\1\116\2\60"+
        "\1\103\1\60\1\uffff\1\116\1\105\1\123\1\60\1\115\1\124\1\60\1\114"+
        "\1\111\1\127\1\101\1\116\1\110\1\105\1\uffff\1\106\1\60\1\120\1"+
        "\103\1\60\1\53\1\56\1\105\1\124\1\103\2\60\1\107\1\uffff\1\60\1"+
        "\uffff\1\125\1\107\1\124\1\101\1\uffff\2\105\1\116\1\103\1\60\1"+
        "\115\1\105\1\60\1\105\1\116\1\60\1\uffff\2\60\1\111\1\107\1\105"+
        "\1\uffff\2\60\1\122\2\uffff\1\60\1\124\4\60\1\122\1\101\1\uffff"+
        "\1\124\1\uffff\1\60\1\101\2\60\1\uffff\1\124\2\uffff\1\101\1\uffff"+
        "\2\60\1\125\1\124\1\uffff\1\101\1\105\1\uffff\1\105\1\123\1\117"+
        "\1\122\1\124\1\101\1\123\1\131\1\uffff\1\105\1\125\1\60\1\53\1\56"+
        "\1\124\1\105\1\124\2\uffff\1\60\1\uffff\1\123\1\105\4\60\1\106\1"+
        "\124\1\uffff\1\122\1\60\1\122\2\60\1\123\3\uffff\1\124\1\105\1\60"+
        "\1\uffff\1\111\1\uffff\1\111\4\uffff\1\111\1\103\1\60\1\uffff\1"+
        "\116\2\uffff\1\60\1\124\2\uffff\1\111\1\101\1\114\2\60\1\123\1\122"+
        "\1\131\1\60\1\122\2\60\2\122\1\60\1\53\1\56\1\101\2\60\1\uffff\1"+
        "\105\1\60\4\uffff\1\101\2\60\1\uffff\1\111\1\60\1\uffff\1\60\1\105"+
        "\1\104\1\uffff\1\116\1\115\1\132\1\105\1\uffff\1\60\1\uffff\1\105"+
        "\1\104\1\115\1\60\2\uffff\1\111\1\104\1\60\1\uffff\1\60\2\uffff"+
        "\1\125\1\123\1\60\1\53\1\56\1\60\2\uffff\1\122\1\uffff\1\115\2\uffff"+
        "\1\116\1\55\1\uffff\1\104\1\60\1\107\2\105\1\60\1\uffff\2\60\1\120"+
        "\1\uffff\1\117\1\60\2\uffff\1\123\1\111\1\55\1\53\1\55\1\uffff\1"+
        "\60\1\111\1\107\1\60\1\uffff\4\60\2\uffff\1\60\1\116\1\uffff\1\105"+
        "\1\126\1\60\1\uffff\1\114\1\60\6\uffff\1\60\1\122\1\105\1\60\1\131"+
        "\2\uffff\5\60\3\uffff\1\60\1\55";
    static final String DFA19_maxS =
        "\1\175\14\uffff\1\71\2\75\1\163\1\162\1\141\1\151\1\165\1\157\1"+
        "\165\1\162\1\165\2\162\1\165\1\145\1\156\2\171\2\162\1\141\1\157"+
        "\1\165\2\uffff\1\170\1\uffff\2\146\2\uffff\1\57\1\uffff\1\145\5"+
        "\uffff\1\157\1\162\1\145\1\157\1\166\1\163\1\160\1\157\1\164\1\150"+
        "\1\151\1\166\1\167\1\165\1\163\1\165\1\146\1\145\1\164\3\172\1\151"+
        "\1\154\1\144\1\151\1\154\1\157\1\154\1\157\1\164\1\145\1\151\1\146"+
        "\1\164\1\160\1\143\1\164\1\144\1\171\2\172\1\157\1\164\1\147\1\157"+
        "\1\147\1\160\1\165\1\170\1\154\1\172\1\142\1\155\1\163\1\157\1\165"+
        "\1\162\1\163\1\151\1\162\1\160\1\144\1\154\1\163\2\uffff\2\146\3"+
        "\uffff\1\167\1\145\1\154\1\156\1\165\1\151\1\164\1\151\2\145\1\162"+
        "\1\172\2\145\1\160\1\157\1\141\2\172\1\160\1\156\1\164\1\163\1\146"+
        "\1\141\1\151\2\uffff\1\145\1\uffff\1\172\1\156\1\157\1\141\1\144"+
        "\1\163\1\141\1\164\1\155\1\150\1\162\1\164\2\172\1\145\1\154\1\172"+
        "\1\150\2\172\1\uffff\1\172\1\145\1\164\1\145\1\uffff\1\154\1\143"+
        "\1\151\1\142\1\151\1\145\1\156\1\164\1\172\1\uffff\1\145\1\154\1"+
        "\145\1\143\1\151\1\145\1\160\1\142\1\155\1\163\1\155\1\151\1\165"+
        "\1\172\1\151\1\154\1\165\1\145\3\146\1\155\1\141\1\145\1\164\1\160"+
        "\1\156\1\172\1\164\1\172\1\162\1\141\1\uffff\1\143\1\155\1\172\1"+
        "\153\1\155\2\uffff\1\155\1\141\1\164\1\157\1\164\1\146\1\164\1\157"+
        "\1\162\1\uffff\1\172\1\147\1\155\1\147\1\164\1\172\1\145\1\164\1"+
        "\145\2\172\2\145\2\uffff\1\167\1\162\1\171\1\uffff\1\151\1\157\2"+
        "\uffff\1\160\1\uffff\1\172\1\162\1\172\1\170\1\145\1\150\1\156\1"+
        "\172\1\156\2\172\1\143\1\172\1\uffff\1\156\1\145\1\165\1\172\1\155"+
        "\1\164\1\172\1\154\1\151\1\167\1\141\1\156\1\150\1\145\1\uffff\1"+
        "\146\1\172\1\160\1\143\3\146\1\145\1\164\1\143\2\172\1\147\1\uffff"+
        "\1\172\1\uffff\1\165\1\147\1\164\1\141\1\uffff\2\145\1\156\1\143"+
        "\1\172\1\155\1\145\1\146\1\145\1\156\1\172\1\uffff\2\172\1\151\1"+
        "\147\1\145\1\uffff\2\172\1\162\2\uffff\1\172\1\164\4\172\1\162\1"+
        "\141\1\uffff\1\164\1\uffff\1\172\1\141\2\172\1\uffff\1\164\2\uffff"+
        "\1\141\1\uffff\2\172\1\165\1\164\1\uffff\1\141\1\145\1\uffff\1\145"+
        "\1\163\1\157\1\162\1\164\1\141\1\163\1\171\1\uffff\1\145\1\165\3"+
        "\146\1\164\1\145\1\164\2\uffff\1\172\1\uffff\1\163\1\145\4\172\1"+
        "\146\1\164\1\uffff\1\162\1\172\1\162\1\146\1\172\1\163\3\uffff\1"+
        "\164\1\145\1\172\1\uffff\1\151\1\uffff\1\151\4\uffff\1\151\1\143"+
        "\1\172\1\uffff\1\156\2\uffff\1\172\1\164\2\uffff\1\151\1\141\1\154"+
        "\2\172\1\163\1\162\1\171\1\172\1\162\2\172\2\162\3\146\1\141\2\172"+
        "\1\uffff\1\145\1\172\4\uffff\1\141\2\172\1\uffff\1\151\1\146\1\uffff"+
        "\1\172\1\145\1\144\1\uffff\1\156\1\155\1\172\1\145\1\uffff\1\172"+
        "\1\uffff\1\145\1\144\1\155\1\172\2\uffff\1\151\1\144\1\172\1\uffff"+
        "\1\172\2\uffff\1\165\1\163\3\146\1\172\2\uffff\1\162\1\uffff\1\155"+
        "\2\uffff\1\156\1\55\1\uffff\1\144\1\172\1\147\2\145\1\172\1\uffff"+
        "\2\172\1\160\1\uffff\1\157\1\172\2\uffff\1\163\1\151\1\55\1\71\1"+
        "\145\1\uffff\1\172\1\151\1\147\1\172\1\uffff\4\172\2\uffff\1\172"+
        "\1\156\1\uffff\1\145\1\166\1\146\1\uffff\1\154\1\172\6\uffff\1\172"+
        "\1\162\1\145\1\146\1\171\2\uffff\3\172\1\146\1\172\3\uffff\1\146"+
        "\1\55";
    static final String DFA19_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\30"+
        "\uffff\1\157\1\160\1\uffff\1\162\2\uffff\1\165\1\170\1\uffff\1\171"+
        "\1\uffff\1\15\1\17\1\16\1\21\1\20\101\uffff\1\166\1\161\2\uffff"+
        "\1\163\1\167\1\172\32\uffff\1\120\1\33\1\uffff\1\70\24\uffff\1\61"+
        "\4\uffff\1\107\11\uffff\1\71\40\uffff\1\52\5\uffff\1\30\1\32\11"+
        "\uffff\1\50\15\uffff\1\102\1\115\3\uffff\1\110\2\uffff\1\41\1\42"+
        "\1\uffff\1\144\15\uffff\1\77\16\uffff\1\155\15\uffff\1\156\1\uffff"+
        "\1\26\4\uffff\1\27\13\uffff\1\125\5\uffff\1\146\3\uffff\1\37\1\45"+
        "\10\uffff\1\74\1\uffff\1\143\4\uffff\1\135\1\uffff\1\103\1\164\1"+
        "\uffff\1\145\4\uffff\1\111\2\uffff\1\72\10\uffff\1\154\10\uffff"+
        "\1\114\1\24\1\uffff\1\46\10\uffff\1\51\6\uffff\1\106\1\126\1\47"+
        "\3\uffff\1\142\1\uffff\1\40\1\uffff\1\112\1\100\1\56\1\133\3\uffff"+
        "\1\66\1\uffff\1\55\1\53\2\uffff\1\152\1\65\24\uffff\1\25\2\uffff"+
        "\1\36\1\63\1\121\1\101\3\uffff\1\67\2\uffff\1\62\3\uffff\1\44\4"+
        "\uffff\1\43\1\uffff\1\134\4\uffff\1\60\1\141\3\uffff\1\150\1\uffff"+
        "\1\75\1\122\6\uffff\1\23\1\22\1\uffff\1\105\1\uffff\1\104\1\137"+
        "\2\uffff\1\35\6\uffff\1\136\3\uffff\1\140\2\uffff\1\73\1\147\5\uffff"+
        "\1\31\4\uffff\1\54\4\uffff\1\57\1\151\2\uffff\1\131\3\uffff\1\127"+
        "\2\uffff\1\34\1\113\1\153\1\123\1\64\1\76\5\uffff\1\132\1\116\5"+
        "\uffff\1\117\1\130\1\124\2\uffff";
    static final String DFA19_specialS =
        "\u0237\uffff}>";
    static final String[] DFA19_transitionS = {
            "\2\54\2\uffff\1\54\22\uffff\1\54\1\uffff\1\46\4\uffff\1\45\1"+
            "\2\1\3\1\uffff\1\14\1\7\1\15\1\5\1\55\1\47\11\52\1\10\1\1\1"+
            "\16\1\4\1\17\1\50\1\uffff\1\33\1\36\1\26\1\40\1\51\1\31\1\21"+
            "\1\22\1\35\1\53\1\34\1\23\1\43\1\44\1\27\1\41\1\53\1\25\1\24"+
            "\1\37\1\30\1\42\1\32\1\20\2\53\1\12\1\uffff\1\13\3\uffff\1\33"+
            "\1\36\1\26\1\40\1\51\1\31\1\21\1\22\1\35\1\53\1\34\1\23\1\43"+
            "\1\44\1\27\1\41\1\53\1\25\1\24\1\37\1\30\1\42\1\32\1\20\2\53"+
            "\1\11\1\uffff\1\6",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\56\2\uffff\12\57",
            "\1\61",
            "\1\63",
            "\1\66\16\uffff\1\65\1\67\17\uffff\1\66\16\uffff\1\65\1\67",
            "\1\70\37\uffff\1\70",
            "\1\71\37\uffff\1\71",
            "\1\72\37\uffff\1\72",
            "\1\76\1\uffff\1\75\5\uffff\1\77\10\uffff\1\74\1\73\15\uffff"+
            "\1\76\1\uffff\1\75\5\uffff\1\77\10\uffff\1\74\1\73",
            "\1\100\11\uffff\1\101\25\uffff\1\100\11\uffff\1\101",
            "\12\105\7\uffff\6\105\5\uffff\1\104\2\uffff\1\102\2\uffff\1"+
            "\106\2\uffff\1\103\13\uffff\6\105\5\uffff\1\104\2\uffff\1\102"+
            "\2\uffff\1\106\2\uffff\1\103",
            "\1\110\7\uffff\1\112\1\uffff\1\107\1\uffff\1\111\23\uffff\1"+
            "\110\7\uffff\1\112\1\uffff\1\107\1\uffff\1\111",
            "\1\114\1\uffff\1\115\2\uffff\1\113\1\uffff\1\116\30\uffff\1"+
            "\114\1\uffff\1\115\2\uffff\1\113\1\uffff\1\116",
            "\12\105\7\uffff\1\117\5\105\2\uffff\1\121\2\uffff\1\120\5\uffff"+
            "\1\122\16\uffff\1\117\5\105\2\uffff\1\121\2\uffff\1\120\5\uffff"+
            "\1\122",
            "\1\124\1\123\10\uffff\1\125\25\uffff\1\124\1\123\10\uffff\1"+
            "\125",
            "\12\105\7\uffff\3\105\1\126\2\105\5\uffff\1\127\1\uffff\1\133"+
            "\1\uffff\1\130\2\uffff\1\131\1\uffff\1\132\13\uffff\3\105\1"+
            "\126\2\105\5\uffff\1\127\1\uffff\1\133\1\uffff\1\130\2\uffff"+
            "\1\131\1\uffff\1\132",
            "\1\134\37\uffff\1\134",
            "\1\135\37\uffff\1\135",
            "\12\105\7\uffff\1\140\3\105\1\141\1\105\2\uffff\1\143\2\uffff"+
            "\1\142\2\uffff\1\137\11\uffff\1\136\7\uffff\1\140\3\105\1\141"+
            "\1\105\2\uffff\1\143\2\uffff\1\142\2\uffff\1\137\11\uffff\1"+
            "\136",
            "\1\151\3\uffff\1\146\3\uffff\1\152\5\uffff\1\150\2\uffff\1"+
            "\145\1\uffff\1\147\4\uffff\1\144\7\uffff\1\151\3\uffff\1\146"+
            "\3\uffff\1\152\5\uffff\1\150\2\uffff\1\145\1\uffff\1\147\4\uffff"+
            "\1\144",
            "\12\105\7\uffff\4\105\1\153\1\105\10\uffff\1\155\2\uffff\1"+
            "\154\16\uffff\4\105\1\153\1\105\10\uffff\1\155\2\uffff\1\154",
            "\1\157\3\uffff\1\156\14\uffff\1\160\16\uffff\1\157\3\uffff"+
            "\1\156\14\uffff\1\160",
            "\1\161\37\uffff\1\161",
            "\1\162\15\uffff\1\163\21\uffff\1\162\15\uffff\1\163",
            "\1\165\5\uffff\1\164\31\uffff\1\165\5\uffff\1\164",
            "",
            "",
            "\1\172\1\uffff\12\171\7\uffff\4\173\1\170\1\173\21\uffff\1"+
            "\166\10\uffff\4\173\1\170\1\173\21\uffff\1\166",
            "",
            "\12\105\7\uffff\6\105\32\uffff\6\105",
            "\1\172\1\uffff\12\171\7\uffff\4\173\1\170\1\173\32\uffff\4"+
            "\173\1\170\1\173",
            "",
            "",
            "\1\174\4\uffff\1\56",
            "",
            "\1\172\1\uffff\12\57\13\uffff\1\172\37\uffff\1\172",
            "",
            "",
            "",
            "",
            "",
            "\1\175\37\uffff\1\175",
            "\1\176\37\uffff\1\176",
            "\1\177\37\uffff\1\177",
            "\1\u0080\15\uffff\1\u0081\21\uffff\1\u0080\15\uffff\1\u0081",
            "\1\u0082\37\uffff\1\u0082",
            "\1\u0085\1\uffff\1\u0084\5\uffff\1\u0083\27\uffff\1\u0085\1"+
            "\uffff\1\u0084\5\uffff\1\u0083",
            "\1\u0086\37\uffff\1\u0086",
            "\1\u0087\37\uffff\1\u0087",
            "\1\u0089\7\uffff\1\u0088\27\uffff\1\u0089\7\uffff\1\u0088",
            "\1\u008a\37\uffff\1\u008a",
            "\1\u008b\37\uffff\1\u008b",
            "\1\u008d\7\uffff\1\u008c\27\uffff\1\u008d\7\uffff\1\u008c",
            "\1\u008e\37\uffff\1\u008e",
            "\1\u008f\1\u0090\7\uffff\1\u0091\26\uffff\1\u008f\1\u0090\7"+
            "\uffff\1\u0091",
            "\1\u0092\37\uffff\1\u0092",
            "\1\u0093\37\uffff\1\u0093",
            "\12\u0094\7\uffff\6\u0094\32\uffff\6\u0094",
            "\1\u0095\37\uffff\1\u0095",
            "\1\u0096\37\uffff\1\u0096",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\3\53\1\u0099\26\53\4\uffff\1\53\1\uffff\3\53"+
            "\1\u0099\26\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u009b\3\uffff\1\u009c\33\uffff\1\u009b\3\uffff\1\u009c",
            "\1\u009d\37\uffff\1\u009d",
            "\1\u009e\37\uffff\1\u009e",
            "\1\u009f\37\uffff\1\u009f",
            "\12\u0094\7\uffff\6\u0094\5\uffff\1\u00a0\24\uffff\6\u0094"+
            "\5\uffff\1\u00a0",
            "\1\u00a1\37\uffff\1\u00a1",
            "\1\u00a2\37\uffff\1\u00a2",
            "\1\u00a3\37\uffff\1\u00a3",
            "\1\u00a4\37\uffff\1\u00a4",
            "\1\u00a5\37\uffff\1\u00a5",
            "\1\u00a6\37\uffff\1\u00a6",
            "\12\u0094\7\uffff\3\u0094\1\u00a7\2\u0094\32\uffff\3\u0094"+
            "\1\u00a7\2\u0094",
            "\1\u00a8\7\uffff\1\u00a9\27\uffff\1\u00a8\7\uffff\1\u00a9",
            "\1\u00aa\37\uffff\1\u00aa",
            "\1\u00ab\37\uffff\1\u00ab",
            "\1\u00ac\37\uffff\1\u00ac",
            "\1\u00ad\37\uffff\1\u00ad",
            "\1\u00ae\37\uffff\1\u00ae",
            "\12\53\7\uffff\3\53\1\u00b3\1\u00b2\15\53\1\u00b1\1\u00b0\6"+
            "\53\4\uffff\1\53\1\uffff\3\53\1\u00b3\1\u00b2\15\53\1\u00b1"+
            "\1\u00b0\6\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u00b5\37\uffff\1\u00b5",
            "\12\u0094\7\uffff\6\u0094\15\uffff\1\u00b6\14\uffff\6\u0094"+
            "\15\uffff\1\u00b6",
            "\12\u0094\7\uffff\6\u0094\1\u00b7\31\uffff\6\u0094\1\u00b7",
            "\1\u00b8\37\uffff\1\u00b8",
            "\1\u00b9\37\uffff\1\u00b9",
            "\1\u00ba\37\uffff\1\u00ba",
            "\1\u00bb\37\uffff\1\u00bb",
            "\1\u00bc\37\uffff\1\u00bc",
            "\1\u00bd\37\uffff\1\u00bd",
            "\12\53\7\uffff\12\53\1\u00bf\17\53\4\uffff\1\53\1\uffff\12"+
            "\53\1\u00bf\17\53",
            "\1\u00c0\37\uffff\1\u00c0",
            "\1\u00c1\37\uffff\1\u00c1",
            "\12\u0094\7\uffff\2\u0094\1\u00c3\3\u0094\5\uffff\1\u00c4\6"+
            "\uffff\1\u00c2\15\uffff\2\u0094\1\u00c3\3\u0094\5\uffff\1\u00c4"+
            "\6\uffff\1\u00c2",
            "\1\u00c5\37\uffff\1\u00c5",
            "\1\u00c6\37\uffff\1\u00c6",
            "\1\u00c7\37\uffff\1\u00c7",
            "\1\u00c8\37\uffff\1\u00c8",
            "\1\u00c9\37\uffff\1\u00c9",
            "\1\u00cb\5\uffff\1\u00ca\31\uffff\1\u00cb\5\uffff\1\u00ca",
            "\1\u00cc\37\uffff\1\u00cc",
            "\1\u00cd\37\uffff\1\u00cd",
            "\1\u00ce\37\uffff\1\u00ce",
            "\1\u00d0\1\u00cf\36\uffff\1\u00d0\1\u00cf",
            "",
            "",
            "\1\172\1\uffff\1\172\2\uffff\12\u00d1\7\uffff\6\173\32\uffff"+
            "\6\173",
            "\1\172\1\uffff\12\u00d3\7\uffff\4\173\1\u00d2\1\173\32\uffff"+
            "\4\173\1\u00d2\1\173",
            "",
            "",
            "",
            "\1\u00d4\37\uffff\1\u00d4",
            "\1\u00d5\37\uffff\1\u00d5",
            "\1\u00d6\37\uffff\1\u00d6",
            "\1\u00d7\37\uffff\1\u00d7",
            "\1\u00d8\37\uffff\1\u00d8",
            "\1\u00d9\37\uffff\1\u00d9",
            "\1\u00da\37\uffff\1\u00da",
            "\1\u00db\37\uffff\1\u00db",
            "\1\u00dc\37\uffff\1\u00dc",
            "\1\u00dd\37\uffff\1\u00dd",
            "\1\u00de\37\uffff\1\u00de",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u00e0\37\uffff\1\u00e0",
            "\1\u00e1\37\uffff\1\u00e1",
            "\1\u00e2\37\uffff\1\u00e2",
            "\1\u00e3\37\uffff\1\u00e3",
            "\1\u00e4\37\uffff\1\u00e4",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\24\53\1\u00e7\5\53\4\uffff\1\53\1\uffff\24\53"+
            "\1\u00e7\5\53",
            "\1\u00e8\37\uffff\1\u00e8",
            "\1\u00e9\37\uffff\1\u00e9",
            "\1\u00ea\37\uffff\1\u00ea",
            "\1\u00eb\37\uffff\1\u00eb",
            "\12\u00ec\7\uffff\6\u00ec\32\uffff\6\u00ec",
            "\1\u00ed\37\uffff\1\u00ed",
            "\1\u00ee\37\uffff\1\u00ee",
            "",
            "",
            "\1\u00ef\37\uffff\1\u00ef",
            "",
            "\12\53\7\uffff\21\53\1\u00f1\10\53\4\uffff\1\53\1\uffff\21"+
            "\53\1\u00f1\10\53",
            "\1\u00f2\37\uffff\1\u00f2",
            "\1\u00f3\5\uffff\1\u00f4\31\uffff\1\u00f3\5\uffff\1\u00f4",
            "\1\u00f5\37\uffff\1\u00f5",
            "\1\u00f6\37\uffff\1\u00f6",
            "\1\u00f7\37\uffff\1\u00f7",
            "\1\u00f8\37\uffff\1\u00f8",
            "\1\u00f9\37\uffff\1\u00f9",
            "\1\u00fa\37\uffff\1\u00fa",
            "\1\u00fb\37\uffff\1\u00fb",
            "\1\u00fc\37\uffff\1\u00fc",
            "\1\u00fd\37\uffff\1\u00fd",
            "\12\u00ec\7\uffff\6\u00ec\24\53\4\uffff\1\53\1\uffff\6\u00ec"+
            "\24\53",
            "\12\53\7\uffff\16\53\1\u0100\13\53\4\uffff\1\53\1\uffff\16"+
            "\53\1\u0100\13\53",
            "\1\u0101\37\uffff\1\u0101",
            "\1\u0102\37\uffff\1\u0102",
            "\12\53\7\uffff\10\53\1\u0104\21\53\4\uffff\1\53\1\uffff\10"+
            "\53\1\u0104\21\53",
            "\1\u0105\37\uffff\1\u0105",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\22\53\1\u0108\7\53\4\uffff\1\53\1\uffff\22\53"+
            "\1\u0108\7\53",
            "",
            "\12\53\7\uffff\16\53\1\u010a\13\53\4\uffff\1\53\1\uffff\16"+
            "\53\1\u010a\13\53",
            "\1\u010b\37\uffff\1\u010b",
            "\1\u010c\37\uffff\1\u010c",
            "\1\u010d\37\uffff\1\u010d",
            "",
            "\1\u010e\37\uffff\1\u010e",
            "\1\u010f\37\uffff\1\u010f",
            "\1\u0110\37\uffff\1\u0110",
            "\1\u0111\37\uffff\1\u0111",
            "\1\u0112\37\uffff\1\u0112",
            "\1\u0113\37\uffff\1\u0113",
            "\1\u0114\10\uffff\1\u0115\26\uffff\1\u0114\10\uffff\1\u0115",
            "\1\u0116\37\uffff\1\u0116",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u0118\37\uffff\1\u0118",
            "\1\u0119\37\uffff\1\u0119",
            "\1\u011a\37\uffff\1\u011a",
            "\1\u011b\37\uffff\1\u011b",
            "\12\u00ec\7\uffff\6\u00ec\2\uffff\1\u011c\27\uffff\6\u00ec"+
            "\2\uffff\1\u011c",
            "\1\u011d\37\uffff\1\u011d",
            "\1\u011e\37\uffff\1\u011e",
            "\1\u011f\37\uffff\1\u011f",
            "\1\u0120\37\uffff\1\u0120",
            "\1\u0121\37\uffff\1\u0121",
            "\1\u0122\37\uffff\1\u0122",
            "\1\u0124\5\uffff\1\u0123\31\uffff\1\u0124\5\uffff\1\u0123",
            "\1\u0125\37\uffff\1\u0125",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0127\37\uffff\1\u0127",
            "\1\u0128\37\uffff\1\u0128",
            "\1\u0129\37\uffff\1\u0129",
            "\1\u012a\37\uffff\1\u012a",
            "\12\u012b\7\uffff\6\173\32\uffff\6\173",
            "\1\172\1\uffff\1\172\2\uffff\12\u012b\7\uffff\6\173\32\uffff"+
            "\6\173",
            "\1\172\1\uffff\12\u012d\7\uffff\4\173\1\u012c\1\173\32\uffff"+
            "\4\173\1\u012c\1\173",
            "\1\u012e\37\uffff\1\u012e",
            "\1\u012f\37\uffff\1\u012f",
            "\1\u0130\37\uffff\1\u0130",
            "\1\u0131\37\uffff\1\u0131",
            "\1\u0132\37\uffff\1\u0132",
            "\1\u0133\37\uffff\1\u0133",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0135\37\uffff\1\u0135",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0137\37\uffff\1\u0137",
            "\1\u0138\37\uffff\1\u0138",
            "",
            "\1\u0139\37\uffff\1\u0139",
            "\1\u013a\37\uffff\1\u013a",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u013c\37\uffff\1\u013c",
            "\1\u013d\37\uffff\1\u013d",
            "",
            "",
            "\1\u013e\37\uffff\1\u013e",
            "\1\u013f\37\uffff\1\u013f",
            "\1\u0140\37\uffff\1\u0140",
            "\1\u0141\37\uffff\1\u0141",
            "\1\u0142\37\uffff\1\u0142",
            "\12\u0143\7\uffff\6\u0143\32\uffff\6\u0143",
            "\1\u0144\37\uffff\1\u0144",
            "\1\u0145\37\uffff\1\u0145",
            "\1\u0146\37\uffff\1\u0146",
            "",
            "\12\53\7\uffff\22\53\1\u0148\7\53\4\uffff\1\53\1\uffff\22\53"+
            "\1\u0148\7\53",
            "\1\u0149\37\uffff\1\u0149",
            "\1\u014a\37\uffff\1\u014a",
            "\1\u014b\37\uffff\1\u014b",
            "\1\u014c\37\uffff\1\u014c",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u014e\37\uffff\1\u014e",
            "\1\u014f\37\uffff\1\u014f",
            "\1\u0150\37\uffff\1\u0150",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0153\37\uffff\1\u0153",
            "\1\u0154\37\uffff\1\u0154",
            "",
            "",
            "\1\u0155\37\uffff\1\u0155",
            "\1\u0156\37\uffff\1\u0156",
            "\1\u0157\37\uffff\1\u0157",
            "",
            "\1\u0158\37\uffff\1\u0158",
            "\1\u0159\37\uffff\1\u0159",
            "",
            "",
            "\1\u015a\37\uffff\1\u015a",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u015c\37\uffff\1\u015c",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u015e\37\uffff\1\u015e",
            "\1\u015f\37\uffff\1\u015f",
            "\1\u0160\37\uffff\1\u0160",
            "\1\u0161\37\uffff\1\u0161",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0163\37\uffff\1\u0163",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0166\37\uffff\1\u0166",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u0168\37\uffff\1\u0168",
            "\1\u0169\37\uffff\1\u0169",
            "\1\u016b\1\uffff\1\u016a\35\uffff\1\u016b\1\uffff\1\u016a",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u016d\37\uffff\1\u016d",
            "\1\u016e\37\uffff\1\u016e",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0170\37\uffff\1\u0170",
            "\1\u0171\37\uffff\1\u0171",
            "\1\u0172\37\uffff\1\u0172",
            "\1\u0173\37\uffff\1\u0173",
            "\1\u0174\37\uffff\1\u0174",
            "\1\u0175\37\uffff\1\u0175",
            "\1\u0176\37\uffff\1\u0176",
            "",
            "\1\u0177\37\uffff\1\u0177",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0179\37\uffff\1\u0179",
            "\1\u017a\37\uffff\1\u017a",
            "\12\u017b\7\uffff\6\173\32\uffff\6\173",
            "\1\172\1\uffff\1\172\2\uffff\12\u017b\7\uffff\6\173\32\uffff"+
            "\6\173",
            "\1\172\1\uffff\12\u017d\7\uffff\4\173\1\u017c\1\173\32\uffff"+
            "\4\173\1\u017c\1\173",
            "\1\u017e\37\uffff\1\u017e",
            "\1\u017f\37\uffff\1\u017f",
            "\1\u0180\37\uffff\1\u0180",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0183\37\uffff\1\u0183",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u0185\37\uffff\1\u0185",
            "\1\u0186\37\uffff\1\u0186",
            "\1\u0187\37\uffff\1\u0187",
            "\1\u0188\37\uffff\1\u0188",
            "",
            "\1\u0189\37\uffff\1\u0189",
            "\1\u018a\37\uffff\1\u018a",
            "\1\u018b\37\uffff\1\u018b",
            "\1\u018c\37\uffff\1\u018c",
            "\12\53\7\uffff\4\53\1\u018e\25\53\4\uffff\1\53\1\uffff\4\53"+
            "\1\u018e\25\53",
            "\1\u018f\37\uffff\1\u018f",
            "\1\u0190\37\uffff\1\u0190",
            "\12\u0191\7\uffff\6\u0191\32\uffff\6\u0191",
            "\1\u0192\37\uffff\1\u0192",
            "\1\u0193\37\uffff\1\u0193",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0197\37\uffff\1\u0197",
            "\1\u0198\37\uffff\1\u0198",
            "\1\u0199\37\uffff\1\u0199",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u019b\37\uffff\1\u019b",
            "",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u019d\37\uffff\1\u019d",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01a2\37\uffff\1\u01a2",
            "\1\u01a3\37\uffff\1\u01a3",
            "",
            "\1\u01a4\37\uffff\1\u01a4",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01a6\37\uffff\1\u01a6",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u01a9\37\uffff\1\u01a9",
            "",
            "",
            "\1\u01aa\37\uffff\1\u01aa",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01ad\37\uffff\1\u01ad",
            "\1\u01ae\37\uffff\1\u01ae",
            "",
            "\1\u01af\37\uffff\1\u01af",
            "\1\u01b0\37\uffff\1\u01b0",
            "",
            "\1\u01b1\37\uffff\1\u01b1",
            "\1\u01b2\37\uffff\1\u01b2",
            "\1\u01b3\37\uffff\1\u01b3",
            "\1\u01b4\37\uffff\1\u01b4",
            "\1\u01b5\37\uffff\1\u01b5",
            "\1\u01b6\37\uffff\1\u01b6",
            "\1\u01b7\37\uffff\1\u01b7",
            "\1\u01b8\37\uffff\1\u01b8",
            "",
            "\1\u01b9\37\uffff\1\u01b9",
            "\1\u01ba\37\uffff\1\u01ba",
            "\12\u01bb\7\uffff\6\173\32\uffff\6\173",
            "\1\172\1\uffff\1\172\2\uffff\12\u01bb\7\uffff\6\173\32\uffff"+
            "\6\173",
            "\1\172\1\uffff\12\u01bd\7\uffff\4\173\1\u01bc\1\173\32\uffff"+
            "\4\173\1\u01bc\1\173",
            "\1\u01be\37\uffff\1\u01be",
            "\1\u01bf\37\uffff\1\u01bf",
            "\1\u01c0\37\uffff\1\u01c0",
            "",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u01c2\37\uffff\1\u01c2",
            "\1\u01c3\37\uffff\1\u01c3",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01c8\37\uffff\1\u01c8",
            "\1\u01c9\37\uffff\1\u01c9",
            "",
            "\1\u01ca\37\uffff\1\u01ca",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01cc\37\uffff\1\u01cc",
            "\12\u01cd\7\uffff\6\u01cd\32\uffff\6\u01cd",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01cf\37\uffff\1\u01cf",
            "",
            "",
            "",
            "\1\u01d0\37\uffff\1\u01d0",
            "\1\u01d1\37\uffff\1\u01d1",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u01d3\37\uffff\1\u01d3",
            "",
            "\1\u01d4\37\uffff\1\u01d4",
            "",
            "",
            "",
            "",
            "\1\u01d5\37\uffff\1\u01d5",
            "\1\u01d6\37\uffff\1\u01d6",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u01d8\37\uffff\1\u01d8",
            "",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01da\37\uffff\1\u01da",
            "",
            "",
            "\1\u01db\37\uffff\1\u01db",
            "\1\u01dc\37\uffff\1\u01dc",
            "\1\u01dd\37\uffff\1\u01dd",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01e0\37\uffff\1\u01e0",
            "\1\u01e1\37\uffff\1\u01e1",
            "\1\u01e2\37\uffff\1\u01e2",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01e4\37\uffff\1\u01e4",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01e7\37\uffff\1\u01e7",
            "\1\u01e8\37\uffff\1\u01e8",
            "\12\u01e9\7\uffff\6\173\32\uffff\6\173",
            "\1\172\1\uffff\1\172\2\uffff\12\u01e9\7\uffff\6\173\32\uffff"+
            "\6\173",
            "\1\172\1\uffff\12\u01eb\7\uffff\4\173\1\u01ea\1\173\32\uffff"+
            "\4\173\1\u01ea\1\173",
            "\1\u01ec\37\uffff\1\u01ec",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u01ef\37\uffff\1\u01ef",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "",
            "",
            "\1\u01f1\37\uffff\1\u01f1",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u01f4\37\uffff\1\u01f4",
            "\12\u01f5\7\uffff\6\u01f5\32\uffff\6\u01f5",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u01f7\37\uffff\1\u01f7",
            "\1\u01f8\37\uffff\1\u01f8",
            "",
            "\1\u01f9\37\uffff\1\u01f9",
            "\1\u01fa\37\uffff\1\u01fa",
            "\1\u01fb\37\uffff\1\u01fb",
            "\1\u01fc\37\uffff\1\u01fc",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u01fe\37\uffff\1\u01fe",
            "\1\u01ff\37\uffff\1\u01ff",
            "\1\u0200\37\uffff\1\u0200",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "\1\u0202\37\uffff\1\u0202",
            "\1\u0203\37\uffff\1\u0203",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "\1\u0206\37\uffff\1\u0206",
            "\1\u0207\37\uffff\1\u0207",
            "\12\u0208\7\uffff\6\173\32\uffff\6\173",
            "\1\172\1\uffff\1\172\2\uffff\12\u0208\7\uffff\6\173\32\uffff"+
            "\6\173",
            "\1\172\1\uffff\12\u020a\7\uffff\4\173\1\u0209\1\173\32\uffff"+
            "\4\173\1\u0209\1\173",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "\1\u020c\37\uffff\1\u020c",
            "",
            "\1\u020d\37\uffff\1\u020d",
            "",
            "",
            "\1\u020e\37\uffff\1\u020e",
            "\1\173",
            "",
            "\1\u020f\37\uffff\1\u020f",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0211\37\uffff\1\u0211",
            "\1\u0212\37\uffff\1\u0212",
            "\1\u0213\37\uffff\1\u0213",
            "\12\53\7\uffff\22\53\1\u0214\7\53\4\uffff\1\53\1\uffff\22\53"+
            "\1\u0214\7\53",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0217\37\uffff\1\u0217",
            "",
            "\1\u0218\37\uffff\1\u0218",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "\1\u021a\37\uffff\1\u021a",
            "\1\u021b\37\uffff\1\u021b",
            "\1\173",
            "\1\172\1\uffff\1\u021c\2\uffff\12\172",
            "\1\173\1\172\1\uffff\12\57\13\uffff\1\172\37\uffff\1\172",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u021e\37\uffff\1\u021e",
            "\1\u021f\37\uffff\1\u021f",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0226\37\uffff\1\u0226",
            "",
            "\1\u0227\37\uffff\1\u0227",
            "\1\u0228\37\uffff\1\u0228",
            "\12\u0229\7\uffff\6\173\32\uffff\6\173",
            "",
            "\1\u022a\37\uffff\1\u022a",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\53\7\uffff\22\53\1\u022d\7\53\4\uffff\1\53\1\uffff\22\53"+
            "\1\u022d\7\53",
            "\1\u022e\37\uffff\1\u022e",
            "\1\u022f\37\uffff\1\u022f",
            "\12\u0230\7\uffff\6\173\32\uffff\6\173",
            "\1\u0231\37\uffff\1\u0231",
            "",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\u0235\7\uffff\6\173\32\uffff\6\173",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "",
            "\12\u0236\7\uffff\6\173\32\uffff\6\173",
            "\1\173"
    };

    static final short[] DFA19_eot = DFA.unpackEncodedString(DFA19_eotS);
    static final short[] DFA19_eof = DFA.unpackEncodedString(DFA19_eofS);
    static final char[] DFA19_min = DFA.unpackEncodedStringToUnsignedChars(DFA19_minS);
    static final char[] DFA19_max = DFA.unpackEncodedStringToUnsignedChars(DFA19_maxS);
    static final short[] DFA19_accept = DFA.unpackEncodedString(DFA19_acceptS);
    static final short[] DFA19_special = DFA.unpackEncodedString(DFA19_specialS);
    static final short[][] DFA19_transition;

    static {
        int numStates = DFA19_transitionS.length;
        DFA19_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA19_transition[i] = DFA.unpackEncodedString(DFA19_transitionS[i]);
        }
    }

    class DFA19 extends DFA {

        public DFA19(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 19;
            this.eot = DFA19_eot;
            this.eof = DFA19_eof;
            this.min = DFA19_min;
            this.max = DFA19_max;
            this.accept = DFA19_accept;
            this.special = DFA19_special;
            this.transition = DFA19_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__139 | T__140 | T__141 | T__142 | T__143 | T__144 | T__145 | T__146 | T__147 | T__148 | T__149 | T__150 | T__151 | T__152 | T__153 | T__154 | T__155 | X_SELECT | X_CREATE | X_GROUP | X_HAVING | X_LIKE | X_SKIP | X_ROW | X_ROW_META | X_COL | X_OR | X_UNLIM | X_OPTIONS | K_SELECT | K_FROM | K_WHERE | K_AND | K_KEY | K_INSERT | K_UPDATE | K_WITH | K_LIMIT | K_USING | K_USE | K_COUNT | K_SET | K_BEGIN | K_UNLOGGED | K_BATCH | K_APPLY | K_TRUNCATE | K_DELETE | K_IN | K_CREATE | K_KEYSPACE | K_KEYSPACES | K_COLUMNFAMILY | K_INDEX | K_CUSTOM | K_ON | K_TO | K_DROP | K_PRIMARY | K_INTO | K_VALUES | K_TIMESTAMP | K_TTL | K_ALTER | K_RENAME | K_ADD | K_TYPE | K_COMPACT | K_STORAGE | K_ORDER | K_BY | K_ASC | K_DESC | K_ALLOW | K_FILTERING | K_GRANT | K_ALL | K_PERMISSION | K_PERMISSIONS | K_OF | K_REVOKE | K_MODIFY | K_AUTHORIZE | K_NORECURSIVE | K_USER | K_USERS | K_SUPERUSER | K_NOSUPERUSER | K_PASSWORD | K_CLUSTERING | K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_TEXT | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID | K_TOKEN | K_WRITETIME | K_NULL | K_MAP | K_LIST | STRING_LITERAL | QUOTED_NAME | INTEGER | QMARK | FLOAT | BOOLEAN | IDENT | HEXNUMBER | UUID | WS | COMMENT | MULTILINE_COMMENT );";
        }
    }
 

}