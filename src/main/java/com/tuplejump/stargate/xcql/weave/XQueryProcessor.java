package com.tuplejump.stargate.xcql.weave;

import com.tuplejump.stargate.xcql.ast.XCqlLexer;
import com.tuplejump.stargate.xcql.ast.XCqlParser;
import org.antlr.runtime.*;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * User: satya
 */
@Aspect
public class XQueryProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(XQueryProcessor.class);

    @Pointcut("execution(public static * org.apache.cassandra.cql3.QueryProcessor.parseStatement(..))")
    public void getQueryProcessPointcut() {
    }

    @Around("getQueryProcessPointcut()")
    public ParsedStatement parseXCql(ProceedingJoinPoint method) throws SyntaxException {
        String query = (String) method.getArgs()[0];
        if (isXCQL(query)) {
            if (logger.isDebugEnabled())
                logger.debug("Executing XCQL query {}", query);
            return parseStatement(query);
        } else {
            try {
                return (ParsedStatement) method.proceed();
            } catch (Throwable e) {
                logger.error("Got error executing query " + query);
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean isXCQL(String query) {
        return (StringUtils.containsIgnoreCase(query, "XSELECT") || StringUtils.containsIgnoreCase(query, "XCREATE"));
    }

    private static ParsedStatement parseStatement(String queryStr) throws SyntaxException {
        if (isXCQL(queryStr)) {
            logger.trace("Parsing XCQL QUERY: {}", queryStr);
            try {
                // Lexer and parser
                CharStream stream = new ANTLRStringStream(queryStr);
                XCqlLexer lexer = new XCqlLexer(stream);
                TokenStream tokenStream = new CommonTokenStream(lexer);
                XCqlParser parser = new XCqlParser(tokenStream);

                // Parse the query string to a statement instance
                ParsedStatement statement = parser.query();

                // The lexer and parser queue up any errors they may have encountered
                // along the way, if necessary, we turn them into exceptions here.
                lexer.throwLastRecognitionError();
                parser.throwLastRecognitionError();

                return statement;
            } catch (RuntimeException re) {
                SyntaxException ire = new SyntaxException("Failed parsing statement: [" + queryStr + "] reason: " + re.getClass().getSimpleName() + " " + re.getMessage());
                throw ire;
            } catch (RecognitionException e) {
                SyntaxException ire = new SyntaxException("Invalid or malformed XCQL query string: " + e.getMessage());
                throw ire;
            }
        } else
            throw new SyntaxException(String.format("Not a XCql statement {}", queryStr));
    }
}
