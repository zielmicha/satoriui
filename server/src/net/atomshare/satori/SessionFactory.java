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

    private static final String host = "localhost" /*"satori.tcs.uj.edu.pl"*/;
    private static final int thriftPort = 2889;
    private static final int blobsPort = 2887;
    private static final int connectionCount = 16;

    public SessionFactory() throws IOException, TTransportException {
        for(int i=0; i < connectionCount; i++) {
            connectionPool.add(createProtocol());
        }
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
        try(Connection conn = takeConnection()) {
            return producer.produce(conn);
        }
    }

    private class SessionConnection
            extends Connection {
        public SessionConnection(TProtocol protocol) {
            super(protocol);
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
