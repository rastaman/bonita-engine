package org.bonitasoft.engine.transaction;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;

public class TransactionLifeCycleImplTest extends TransactionLifeCycleTest {

    @BeforeClass
    public static void setUpJNDIBitronixTransactionManager() {
        final Configuration conf = TransactionManagerServices.getConfiguration();
        // TODO Make the following configurable
        conf.setServerId("jvm-1");
        conf.setJournal(null); // Disable the journal for the tests.
    }

    @AfterClass
    public static void stopJNDIBitronixTransactionManager() {
        TransactionManagerServices.getTransactionManager().shutdown();
    }

    @Override
    protected TransactionService getTxService() {
        return new JTATransactionServiceImpl(getLoggerService(), getTransactionManager());
    }

    private TransactionManager getTransactionManager() {
        try {
            final Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");

            final Context ctx = new InitialContext(env);
            return (TransactionManager) ctx.lookup("java:comp/UserTransaction");
        } catch (final NamingException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO See to use some mocks instead of Anonymous classes.
    protected TechnicalLoggerService getLoggerService() {

        return new TechnicalLoggerService() {

            @Override
            public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message, final Throwable t) {
                // TODO Auto-generated method stub

            }

            @Override
            public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message) {
                // TODO Auto-generated method stub

            }

            @Override
            public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final Throwable t) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean isLoggable(final Class<?> callerClass, final TechnicalLogSeverity severity) {
                // TODO Auto-generated method stub
                return false;
            }
        };

    }

}
