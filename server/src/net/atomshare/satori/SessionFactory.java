package net.atomshare.satori;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class SessionFactory {
    private Map<String, Session> cached = new HashMap<>();
    private BlockingQueue<TProtocol> connectionPool = new LinkedBlockingQueue<>();
    private BlockingQueue<Unit> connectionsToCreate = new LinkedBlockingQueue<>();

    private static final String host = "satori.tcs.uj.edu.pl";
    private static final int thriftPort = 2889;
    private static final int blobsPort = 2887;
    private static final int connectionCount = 16;

    public SessionFactory() throws IOException, TTransportException {
        for(int i=0; i < connectionCount; i++) {
            try {
                connectionsToCreate.put(Unit.VALUE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void startConnectionCreator() {
        new Thread(() -> {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    Unit token = connectionsToCreate.take();
                    while (true) {
                        try {
                            addConnection();
                            break;
                        } catch(IOException|TTransportException ex) {
                            ex.printStackTrace();
                            Thread.sleep(2000);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void addConnection() throws IOException, TTransportException {
        connectionPool.add(createProtocol());
        System.err.println("Connection created.");
    }

    public Connection takeConnection() throws TException {
        TProtocol protocol;
        try {
            protocol = connectionPool.take();
        } catch(InterruptedException ex) {
            throw new TException(ex);
        }
        return new SessionConnection(protocol);
    }

    public interface Producer<T> {
        T produce(Connection conn) throws TException;
    }

    public <T> T withConnection(Producer<T> producer) throws TException {
        while(true) {
            Connection conn = takeConnection();
            try {
                return producer.produce(conn);
            } catch (TTransportException ex) {
                ex.printStackTrace();
                conn.destroy();
                conn = null;
            } finally {
                if (conn != null)
                    conn.close();
            }
        }
    }

    private class SessionConnection
            extends Connection {
        public SessionConnection(TProtocol protocol) {
            super(protocol);
        }

        @Override
        public void destroy()  {
            synchronized (SessionFactory.this) {
                try {
                    connectionsToCreate.put(Unit.VALUE);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            protocol.getTransport().close();
        }

        @Override
        public void close() {
            synchronized (SessionFactory.this) {
                try {
                    connectionPool.put(protocol);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Socket createSecureSocket() throws IOException {
        SSLSocket socket = (SSLSocket)
                SSLSocketFactory.getDefault().createSocket(host, thriftPort);
        socket.setEnabledProtocols(new String[]{"TLSv1"});
        return socket;
    }

    private TProtocol createProtocol() throws IOException, TTransportException {
        Socket socket = createSecureSocket();
        socket.setSoTimeout(10000);
        return new TBinaryProtocol(new TFramedTransport(new TSocket(socket)));
    }

    public synchronized Session get(String cred) throws TException {
        Session sess = cached.get(cred);
        if(sess == null) {
            cached.put(cred, sess = new Session(this, cred));
            sess.init();
        }
        return sess;
    }
}
